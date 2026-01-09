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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages automatic recovery of failed Jenkins agents.
 * Implements various healing strategies including restart, reconnect, and replacement.
 */
@CompileStatic
class SelfHealingAgentManager {

    private static final Logger LOGGER = Logger.getLogger(SelfHealingAgentManager.class.name)

    // Configuration
    private final int maxRetryAttempts
    private final long retryDelayMs
    private final long healthCheckIntervalMs
    private final boolean enableAutoReconnect
    private final boolean enableAutoRestart
    private final boolean enableAutoReplace

    // State tracking
    private final ConcurrentHashMap<String, AgentHealthState> agentStates = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>()
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2)

    // Healing statistics
    private final AtomicInteger totalRecoveryAttempts = new AtomicInteger(0)
    private final AtomicInteger successfulRecoveries = new AtomicInteger(0)
    private final AtomicInteger failedRecoveries = new AtomicInteger(0)

    // Callbacks
    private Closure onRecoveryStart
    private Closure onRecoverySuccess
    private Closure onRecoveryFailure
    private Closure onAgentReplaced

    SelfHealingAgentManager(Map<String, Object> config = [:]) {
        this.maxRetryAttempts = (config.maxRetryAttempts as Integer) ?: 3
        this.retryDelayMs = (config.retryDelayMs as Long) ?: 30000L
        this.healthCheckIntervalMs = (config.healthCheckIntervalMs as Long) ?: 60000L
        this.enableAutoReconnect = config.enableAutoReconnect != false
        this.enableAutoRestart = config.enableAutoRestart != false
        this.enableAutoReplace = config.enableAutoReplace ?: false
    }

    /**
     * Start the self-healing monitor.
     */
    void start() {
        LOGGER.info("Starting self-healing agent manager with ${healthCheckIntervalMs}ms interval")

        scheduler.scheduleAtFixedRate({
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
    void stop() {
        LOGGER.info("Stopping self-healing agent manager")
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
        LOGGER.info("Self-healing agent manager stopped")
    }

    /**
     * Perform health check on all agents.
     */
    void performHealthCheck() {
        Jenkins jenkins = Jenkins.getInstanceOrNull()
        if (jenkins == null) {
            LOGGER.warning("Jenkins instance not available")
            return
        }

        LOGGER.fine("Performing agent health check")

        jenkins.computers.each { Computer computer ->
            if (computer == jenkins.toComputer()) {
                // Skip master node
                return
            }

            String agentName = computer.name
            AgentHealthState state = getOrCreateState(agentName)

            try {
                checkAgentHealth(computer, state)
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking agent ${agentName}", e)
            }
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
            // Agent is online - check for other health issues
            if (state.status == AgentStatus.RECOVERING) {
                // Recovery was successful
                state.status = AgentStatus.HEALTHY
                state.lastHealthyTime = System.currentTimeMillis()
                resetRetryCounter(agentName)
                successfulRecoveries.incrementAndGet()
                onRecoverySuccess?.call(agentName)
                LOGGER.info("Agent ${agentName} recovered successfully")
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

        // Check retry limit
        int retries = getRetryCount(agentName)
        if (retries >= maxRetryAttempts) {
            LOGGER.warning("Agent ${agentName} exceeded max retry attempts (${maxRetryAttempts})")
            state.status = AgentStatus.FAILED
            failedRecoveries.incrementAndGet()
            onRecoveryFailure?.call(agentName, "Max retries exceeded")

            if (enableAutoReplace) {
                attemptAgentReplacement(computer, state)
            }
            return
        }

        // Attempt recovery
        state.status = AgentStatus.RECOVERING
        totalRecoveryAttempts.incrementAndGet()
        incrementRetryCounter(agentName)
        onRecoveryStart?.call(agentName, retries + 1)

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
                computer.connect(true)

                // Schedule a delayed check to verify reconnection
                scheduler.schedule({
                    if (computer.isOnline()) {
                        LOGGER.info("Agent ${agentName} reconnected successfully")
                    } else {
                        LOGGER.warning("Agent ${agentName} reconnection failed")
                        // Try restart if available
                        if (enableAutoRestart) {
                            attemptRestart(computer, state)
                        }
                    }
                }, retryDelayMs, TimeUnit.MILLISECONDS)

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
                    // Disconnect
                    slaveComputer.disconnect(new OfflineCause.ByCLI("Self-healing restart"))

                    // Schedule reconnection
                    scheduler.schedule({
                        try {
                            slaveComputer.connect(true)
                            LOGGER.info("Restart initiated for agent ${agentName}")
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to restart agent ${agentName}", e)
                        }
                    }, 5000, TimeUnit.MILLISECONDS)
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

        // This would integrate with cloud providers to provision a new agent
        // For now, log the event and notify
        state.status = AgentStatus.REPLACING
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
                long freeBytes = diskSpace.freeSize as long
                long freeMB = freeBytes / (1024 * 1024)

                if (freeMB < 1024) {  // Less than 1GB
                    LOGGER.warning("Agent ${agentName} has low disk space: ${freeMB}MB")
                    state.addWarning("Low disk space: ${freeMB}MB")
                }
            }

            // Check response time
            def responseTime = monitorData?.get('hudson.node_monitors.ResponseTimeMonitor')
            if (responseTime?.hasProperty('average')) {
                long avgMs = responseTime.average as long

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
     * Increment retry counter.
     */
    private void incrementRetryCounter(String agentName) {
        retryCounters.computeIfAbsent(agentName) { new AtomicInteger(0) }.incrementAndGet()
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
                agents       : agentStates.collectEntries { name, state ->
                    [name, state.toMap()]
                }
        ]
    }

    /**
     * Get status of all agents.
     */
    List<Map<String, Object>> getAgentStatuses() {
        return agentStates.collect { name, state -> state.toMap() }
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
        AgentStatus status = AgentStatus.UNKNOWN
        long lastCheckTime = 0
        long lastHealthyTime = 0
        long lastFailureTime = 0
        final List<String> warnings = []
        final List<String> recoveryHistory = []

        AgentHealthState(String agentName) {
            this.agentName = agentName
        }

        void addWarning(String warning) {
            synchronized (warnings) {
                warnings.add("[${new Date()}] ${warning}")
                if (warnings.size() > 10) {
                    warnings.remove(0)
                }
            }
        }

        void addRecoveryEvent(String event) {
            synchronized (recoveryHistory) {
                recoveryHistory.add("[${new Date()}] ${event}")
                if (recoveryHistory.size() > 20) {
                    recoveryHistory.remove(0)
                }
            }
        }

        Map<String, Object> toMap() {
            return [
                    agentName      : agentName,
                    status         : status.name(),
                    lastCheckTime  : lastCheckTime,
                    lastHealthyTime: lastHealthyTime,
                    lastFailureTime: lastFailureTime,
                    warnings       : new ArrayList<>(warnings),
                    recoveryHistory: new ArrayList<>(recoveryHistory)
            ]
        }
    }
}
