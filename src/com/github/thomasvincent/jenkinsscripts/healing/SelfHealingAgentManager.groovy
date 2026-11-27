// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Self-healing agent manager for Jenkins.
 * Automatically detects and recovers failed agents with configurable strategies.
 */
package com.github.thomasvincent.jenkinsscripts.healing

import groovy.transform.CompileStatic
import hudson.model.Computer
import hudson.model.Node
import hudson.slaves.OfflineCause
import hudson.slaves.SlaveComputer
import jenkins.model.Jenkins

import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages automatic recovery of failed Jenkins agents.
 * Implements various healing strategies including restart, reconnect, and replacement.
 */
@CompileStatic
class SelfHealingAgentManager {

    private static final Logger LOGGER = Logger.getLogger(SelfHealingAgentManager.class.name)
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1)

    // Configuration
    private final int maxRetryAttempts
    private final long retryDelayMs
    private final long healthCheckIntervalMs
    private final boolean enableAutoReconnect
    private final boolean enableAutoRestart
    private final boolean enableAutoReplace
    private final boolean singlePass
    private final boolean readOnly
    private final Jenkins injectedJenkins

    // State tracking
    private final ConcurrentHashMap<String, AgentHealthState> agentStates = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingRecoveries = new ConcurrentHashMap<>()
    // External connect() futures observed without cancelling on stop()
    private final ConcurrentHashMap<String, Future<?>> pendingConnects = new ConcurrentHashMap<>()
    private ScheduledExecutorService scheduler
    private ScheduledFuture<?> healthCheckFuture

    // Healing statistics
    private final AtomicInteger totalRecoveryAttempts = new AtomicInteger(0)
    private final AtomicInteger successfulRecoveries = new AtomicInteger(0)
    private final AtomicInteger failedRecoveries = new AtomicInteger(0)

    // Callbacks (volatile for safe cross-thread publication)
    private volatile Closure onRecoveryStart
    private volatile Closure onRecoverySuccess
    private volatile Closure onRecoveryFailure
    private volatile Closure onAgentReplaced

    SelfHealingAgentManager(Map<String, Object> config = [:], Jenkins jenkins = null) {
        this.maxRetryAttempts = config.maxRetryAttempts != null ? (config.maxRetryAttempts as Integer) : 3
        this.retryDelayMs = config.retryDelayMs != null ? (config.retryDelayMs as Long) : 30000L
        this.healthCheckIntervalMs = config.healthCheckIntervalMs != null ? (config.healthCheckIntervalMs as Long) : 60000L
        this.enableAutoReconnect = config.enableAutoReconnect != false
        this.enableAutoRestart = config.enableAutoRestart != false
        this.enableAutoReplace = (config.enableAutoReplace as Boolean) ?: false
        this.singlePass = (config.singlePass as Boolean) ?: false
        this.readOnly = (config.readOnly as Boolean) ?: false
        this.injectedJenkins = jenkins
    }

    private Jenkins resolveJenkins() {
        return injectedJenkins ?: Jenkins.getInstanceOrNull()
    }

    private synchronized ScheduledExecutorService getOrCreateScheduler() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(2, new ThreadFactory() {
                @Override
                Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "self-healing-agent-manager-${THREAD_COUNTER.getAndIncrement()}")
                    t.setDaemon(true)
                    return t
                }
            })
        }
        return scheduler
    }

    synchronized boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown() && healthCheckFuture != null && !healthCheckFuture.isCancelled()
    }

    /**
     * Start the self-healing monitor.
     */
    synchronized void start() {
        if (isRunning()) {
            LOGGER.info("Self-healing agent manager is already running")
            return
        }

        LOGGER.info("Starting self-healing agent manager with ${healthCheckIntervalMs}ms interval")

        ScheduledExecutorService exec = getOrCreateScheduler()
        healthCheckFuture = exec.scheduleAtFixedRate({
            try {
                performHealthCheck()
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during health check", e)
            }
        }, healthCheckIntervalMs, healthCheckIntervalMs, TimeUnit.MILLISECONDS)

        LOGGER.info("Self-healing agent manager started")
    }

    /**
     * Stop the self-healing monitor.
     */
    synchronized void stop() {
        LOGGER.info("Stopping self-healing agent manager")
        if (healthCheckFuture != null) {
            healthCheckFuture.cancel(false)
            healthCheckFuture = null
        }
        pendingRecoveries.values().each { ScheduledFuture<?> f -> f.cancel(false) }
        pendingRecoveries.clear()

        if (scheduler != null) {
            scheduler.shutdown()
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow()
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow()
                Thread.currentThread().interrupt()
            } finally {
                scheduler = null
            }
        }
        LOGGER.info("Self-healing agent manager stopped")
    }

    /**
     * Perform health check on all agents and prune state for removed ephemeral computers.
     */
    void performHealthCheck() {
        Jenkins jenkins = resolveJenkins()
        if (jenkins == null) {
            LOGGER.warning("Jenkins instance not available")
            return
        }

        LOGGER.fine("Performing agent health check")

        Computer controllerComputer = jenkins.toComputer()
        Set<String> activeAgentNames = new HashSet<>()

        jenkins.computers.each { Computer computer ->
            if (computer == controllerComputer) {
                // Skip controller node
                return
            }

            String agentName = computer.name
            activeAgentNames.add(agentName)
            AgentHealthState state = getOrCreateState(agentName)

            try {
                checkAgentHealth(computer, state)
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking agent ${agentName}", e)
            }
        }

        pruneRemovedAgents(activeAgentNames)
    }

    private void pruneRemovedAgents(Set<String> activeAgentNames) {
        agentStates.keySet().removeIf { String name -> !activeAgentNames.contains(name) }
        retryCounters.keySet().removeIf { String name -> !activeAgentNames.contains(name) }
        pendingConnects.keySet().removeIf { String name -> !activeAgentNames.contains(name) }
        pendingRecoveries.entrySet().removeIf { Map.Entry<String, ScheduledFuture<?>> entry ->
            if (!activeAgentNames.contains(entry.key)) {
                entry.value?.cancel(false)
                return true
            }
            return false
        }
    }

    /**
     * Check health of a single agent.
     */
    private void checkAgentHealth(Computer computer, AgentHealthState state) {
        String agentName = computer.name

        if (computer.isOffline()) {
            handleOfflineAgent(computer, state)
        } else if (computer.isOnline()) {
            pendingConnects.remove(agentName)
            AgentStatus previousStatus = state.status
            if (previousStatus == AgentStatus.RECOVERING || previousStatus == AgentStatus.FAILED
                    || previousStatus == AgentStatus.REPLACING) {
                state.status = AgentStatus.HEALTHY
                state.lastHealthyTime = System.currentTimeMillis()
                state.addRecoveryEvent("Agent recovered and is online")
                resetRetryCounter(agentName)
                cancelPendingRecovery(agentName)
                successfulRecoveries.incrementAndGet()
                onRecoverySuccess?.call(agentName)
                LOGGER.info("Agent ${agentName} recovered successfully")
            } else if (previousStatus == AgentStatus.UNKNOWN || previousStatus == AgentStatus.MANUALLY_OFFLINE
                    || previousStatus == AgentStatus.OFFLINE) {
                state.status = AgentStatus.HEALTHY
                state.lastHealthyTime = System.currentTimeMillis()
                resetRetryCounter(agentName)
            }

            // Check for resource issues
            checkAgentResources(computer, state)
        }

        state.lastCheckTime = System.currentTimeMillis()
    }

    /**
     * Handle an offline agent.
     */
    private void handleOfflineAgent(Computer computer, AgentHealthState state) {
        String agentName = computer.name
        OfflineCause cause = computer.offlineCause

        LOGGER.info("Agent ${agentName} is offline: ${cause?.toString() ?: 'Unknown cause'}")

        // Check if temporarily offline (manual)
        if (cause instanceof OfflineCause.UserCause) {
            LOGGER.fine("Agent ${agentName} is manually offline, skipping recovery")
            state.status = AgentStatus.MANUALLY_OFFLINE
            return
        }

        if (readOnly) {
            LOGGER.fine("Agent ${agentName} is offline (read-only check, skipping recovery)")
            state.status = AgentStatus.OFFLINE
            state.lastFailureTime = System.currentTimeMillis()
            return
        }

        // Transition once into FAILED/REPLACING per outage episode
        if (state.status == AgentStatus.FAILED || state.status == AgentStatus.REPLACING) {
            LOGGER.fine("Agent ${agentName} already in terminal outage state ${state.status}, skipping duplicate failure handling")
            return
        }

        // Deduplicate while a delayed recovery task or asynchronous launch is already in flight
        if (hasPendingRecovery(agentName)) {
            LOGGER.fine("Recovery task already in flight for agent ${agentName}, skipping duplicate attempt")
            return
        }

        // Check retry limit
        int retries = getRetryCount(agentName)
        if (retries >= maxRetryAttempts) {
            LOGGER.warning("Agent ${agentName} exceeded max retry attempts (${maxRetryAttempts})")
            state.status = AgentStatus.FAILED
            state.lastFailureTime = System.currentTimeMillis()
            state.addRecoveryEvent("Exceeded max retry attempts (${maxRetryAttempts})")
            failedRecoveries.incrementAndGet()
            onRecoveryFailure?.call(agentName, "Max retries exceeded")

            if (enableAutoReplace) {
                attemptAgentReplacement(computer, state)
            }
            return
        }

        // Attempt recovery
        state.status = AgentStatus.RECOVERING
        state.lastFailureTime = System.currentTimeMillis()
        totalRecoveryAttempts.incrementAndGet()
        int attemptNumber = retryCounters.computeIfAbsent(agentName) { new AtomicInteger(0) }.incrementAndGet()
        state.addRecoveryEvent("Starting recovery attempt #${attemptNumber}")
        onRecoveryStart?.call(agentName, attemptNumber)

        attemptRecovery(computer, state)
    }

    /**
     * Attempt to recover an agent.
     */
    private void attemptRecovery(Computer computer, AgentHealthState state) {
        String agentName = computer.name
        LOGGER.info("Attempting recovery for agent ${agentName}")

        // Strategy 1: Try to reconnect
        if (enableAutoReconnect) {
            try {
                LOGGER.info("Attempting to reconnect agent ${agentName}")
                Future<?> connectFuture = computer.connect(true)

                if (singlePass) {
                    if (connectFuture != null && !connectFuture.isDone()) {
                        pendingConnects.put(agentName, connectFuture)
                        LOGGER.info("Reconnect still pending for agent ${agentName}; skipping restart while launch is in progress")
                        return
                    }
                    if (connectFuture != null) {
                        try {
                            connectFuture.get()
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt()
                            LOGGER.log(Level.WARNING, "Interrupted while checking reconnect result for agent ${agentName}", e)
                            return
                        } catch (ExecutionException | CancellationException e) {
                            LOGGER.log(Level.WARNING, "Reconnect attempt failed for agent ${agentName}", e)
                        }
                    }
                    if (!computer.isOnline() && enableAutoRestart) {
                        attemptRestart(computer, state)
                    }
                    return
                }

                scheduleRecoveryTask(agentName, retryDelayMs) {
                    if (computer.isOnline()) {
                        LOGGER.info("Agent ${agentName} reconnected successfully")
                    } else {
                        LOGGER.warning("Agent ${agentName} reconnection failed")
                        if (enableAutoRestart) {
                            attemptRestart(computer, state)
                        }
                    }
                }

                return
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to reconnect agent ${agentName}", e)
            }
        }

        // Strategy 2: Try to restart (if supported)
        if (enableAutoRestart) {
            attemptRestart(computer, state)
        }
    }

    private void scheduleRecoveryTask(String agentName, long delayMs, Runnable task) {
        ScheduledExecutorService exec = getOrCreateScheduler()
        ScheduledFuture<?>[] holder = new ScheduledFuture<?>[1]
        ScheduledFuture<?> future = exec.schedule({
            try {
                task.run()
            } finally {
                if (holder[0] != null) {
                    pendingRecoveries.remove(agentName, holder[0])
                } else {
                    pendingRecoveries.remove(agentName)
                }
            }
        }, delayMs, TimeUnit.MILLISECONDS)
        holder[0] = future
        ScheduledFuture<?> previous = pendingRecoveries.put(agentName, future)
        if (previous != null && !previous.isDone()) {
            previous.cancel(false)
        }
    }

    private void cancelPendingRecovery(String agentName) {
        ScheduledFuture<?> future = pendingRecoveries.remove(agentName)
        if (future != null && !future.isDone()) {
            future.cancel(false)
        }
    }

    /**
     * Attempt to restart an agent.
     */
    private void attemptRestart(Computer computer, AgentHealthState state) {
        String agentName = computer.name
        LOGGER.info("Attempting to restart agent ${agentName}")

        try {
            // For cloud agents, disconnect and reconnect
            if (computer instanceof SlaveComputer) {
                SlaveComputer slaveComputer = (SlaveComputer) computer
                Node node = slaveComputer.node

                if (node != null) {
                    Future<?> disconnectFuture = slaveComputer.disconnect(new OfflineCause.ByCLI("Self-healing restart"))

                    if (singlePass) {
                        if (disconnectFuture != null) {
                            long disconnectTimeoutMs = retryDelayMs > 0L ? retryDelayMs : 10000L
                            try {
                                disconnectFuture.get(disconnectTimeoutMs, TimeUnit.MILLISECONDS)
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt()
                                LOGGER.log(Level.WARNING, "Interrupted while waiting for agent ${agentName} disconnect; skipping reconnect", e)
                                return
                            } catch (TimeoutException | ExecutionException | CancellationException e) {
                                LOGGER.log(Level.WARNING, "Disconnect for agent ${agentName} did not complete cleanly within ${disconnectTimeoutMs}ms; skipping reconnect", e)
                                return
                            }
                        }
                        try {
                            Future<?> reconnectFuture = slaveComputer.connect(true)
                            if (reconnectFuture != null && !reconnectFuture.isDone()) {
                                pendingConnects.put(agentName, reconnectFuture)
                            }
                            LOGGER.info("Restart initiated for agent ${agentName}")
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to restart agent ${agentName}", e)
                        }
                    } else {
                        scheduleRecoveryTask(agentName, 5000L) {
                            try {
                                slaveComputer.connect(true)
                                LOGGER.info("Restart initiated for agent ${agentName}")
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Failed to restart agent ${agentName}", e)
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during restart of agent ${agentName}", e)
        }
    }

    /**
     * Attempt to replace a failed agent.
     */
    private void attemptAgentReplacement(Computer computer, AgentHealthState state) {
        String agentName = computer.name
        LOGGER.warning("Attempting to replace failed agent ${agentName}")

        state.status = AgentStatus.REPLACING
        state.addRecoveryEvent("Agent replacement requested")
        onAgentReplaced?.call(agentName)

        LOGGER.info("Agent replacement requested for ${agentName}")
    }

    /**
     * Check agent resources for potential issues.
     */
    private void checkAgentResources(Computer computer, AgentHealthState state) {
        String agentName = computer.name

        try {
            // Check disk space
            def monitorData = computer.getMonitorData()
            def diskSpace = monitorData?.get('hudson.node_monitors.DiskSpaceMonitor')

            if (diskSpace?.hasProperty('freeSize')) {
                long freeBytes = (org.codehaus.groovy.runtime.InvokerHelper.getProperty(diskSpace, 'freeSize') as Number).longValue()
                long freeMB = freeBytes.intdiv(1024L * 1024L).longValue()

                if (freeMB < 1024) {  // Less than 1GB
                    LOGGER.warning("Agent ${agentName} has low disk space: ${freeMB}MB")
                    state.addWarning("Low disk space: ${freeMB}MB")
                }
            }

            // Check response time
            def responseTime = monitorData?.get('hudson.node_monitors.ResponseTimeMonitor')
            if (responseTime?.hasProperty('average')) {
                long avgMs = (org.codehaus.groovy.runtime.InvokerHelper.getProperty(responseTime, 'average') as Number).longValue()

                if (avgMs > 5000) {  // More than 5 seconds
                    LOGGER.warning("Agent ${agentName} has slow response time: ${avgMs}ms")
                    state.addWarning("Slow response: ${avgMs}ms")
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not check resources for ${agentName}", e)
        }
    }

    /**
     * Get or create agent state.
     */
    private AgentHealthState getOrCreateState(String agentName) {
        return agentStates.computeIfAbsent(agentName) { new AgentHealthState(agentName) }
    }

    /**
     * Get retry count for agent.
     */
    private int getRetryCount(String agentName) {
        return retryCounters.computeIfAbsent(agentName) { new AtomicInteger(0) }.get()
    }

    /**
     * Reset retry counter.
     */
    private void resetRetryCounter(String agentName) {
        retryCounters.get(agentName)?.set(0)
    }

    /**
     * Get healing statistics.
     */
    Map<String, Object> getStatistics() {
        return [
                totalAttempts: totalRecoveryAttempts.get(),
                successful   : successfulRecoveries.get(),
                failed       : failedRecoveries.get(),
                agents       : agentStates.collectEntries { String name, AgentHealthState state ->
                    [name, state.toMap()]
                }
        ]
    }

    /**
     * Returns true when a delayed recovery task or asynchronous launch future is still in flight for the agent.
     */
    boolean hasPendingRecovery(String agentName) {
        ScheduledFuture<?> scheduled = pendingRecoveries.get(agentName)
        if (scheduled != null && !scheduled.isDone()) {
            return true
        }
        Future<?> connectFuture = pendingConnects.get(agentName)
        if (connectFuture != null) {
            if (!connectFuture.isDone()) {
                return true
            }
            pendingConnects.remove(agentName, connectFuture)
        }
        return false
    }

    /**
     * Get status of all agents.
     */
    List<Map<String, Object>> getAgentStatuses() {
        return agentStates.collect { String name, AgentHealthState state -> state.toMap() }
    }

    // Callback setters
    void onRecoveryStart(Closure callback) { this.onRecoveryStart = callback }
    void onRecoverySuccess(Closure callback) { this.onRecoverySuccess = callback }
    void onRecoveryFailure(Closure callback) { this.onRecoveryFailure = callback }
    void onAgentReplaced(Closure callback) { this.onAgentReplaced = callback }

    /**
     * Agent health status enum.
     */
    static enum AgentStatus {
        HEALTHY,
        OFFLINE,
        RECOVERING,
        FAILED,
        MANUALLY_OFFLINE,
        REPLACING,
        UNKNOWN
    }

    /**
     * Agent health state tracking.
     */
    static class AgentHealthState {
        final String agentName
        private final AtomicReference<AgentStatus> statusRef = new AtomicReference<>(AgentStatus.UNKNOWN)
        private final AtomicLong lastCheckTimeRef = new AtomicLong(0L)
        private final AtomicLong lastHealthyTimeRef = new AtomicLong(0L)
        private final AtomicLong lastFailureTimeRef = new AtomicLong(0L)
        final List<String> warnings = []
        final List<String> recoveryHistory = []

        AgentHealthState(String agentName) {
            this.agentName = agentName
        }

        AgentStatus getStatus() {
            return statusRef.get()
        }

        void setStatus(AgentStatus status) {
            statusRef.set(status)
        }

        long getLastCheckTime() {
            return lastCheckTimeRef.get()
        }

        void setLastCheckTime(long timestamp) {
            lastCheckTimeRef.set(timestamp)
        }

        long getLastHealthyTime() {
            return lastHealthyTimeRef.get()
        }

        void setLastHealthyTime(long timestamp) {
            lastHealthyTimeRef.set(timestamp)
        }

        long getLastFailureTime() {
            return lastFailureTimeRef.get()
        }

        void setLastFailureTime(long timestamp) {
            lastFailureTimeRef.set(timestamp)
        }

        void addWarning(String warning) {
            synchronized (warnings) {
                warnings.add("[${new Date()}] ${warning}".toString())
                if (warnings.size() > 10) {
                    warnings.remove(0)
                }
            }
        }

        void addRecoveryEvent(String event) {
            synchronized (recoveryHistory) {
                recoveryHistory.add("[${new Date()}] ${event}".toString())
                if (recoveryHistory.size() > 20) {
                    recoveryHistory.remove(0)
                }
            }
        }

        Map<String, Object> toMap() {
            List<String> warningsSnapshot
            synchronized (warnings) {
                warningsSnapshot = new ArrayList<>(warnings)
            }
            List<String> historySnapshot
            synchronized (recoveryHistory) {
                historySnapshot = new ArrayList<>(recoveryHistory)
            }
            return [
                    agentName      : agentName,
                    status         : getStatus().name(),
                    lastCheckTime  : getLastCheckTime(),
                    lastHealthyTime: getLastHealthyTime(),
                    lastFailureTime: getLastFailureTime(),
                    warnings       : warningsSnapshot,
                    recoveryHistory: historySnapshot
            ]
        }
    }
}
