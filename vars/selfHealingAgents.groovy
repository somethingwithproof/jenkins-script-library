// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for self-healing agent management.
 */

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.healing.SelfHealingAgentManager
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger

/**
 * Manage self-healing agents in Jenkins.
 *
 * Usage:
 *   // Start self-healing with default config
 *   selfHealingAgents.start()
 *
 *   // Start with custom config
 *   selfHealingAgents.start([
 *       maxRetryAttempts: 5,
 *       retryDelayMs: 60000,
 *       healthCheckIntervalMs: 120000
 *   ])
 *
 *   // Get statistics
 *   def stats = selfHealingAgents.statistics()
 *
 *   // Stop the manager
 *   selfHealingAgents.stop()
 */

@groovy.transform.Field
private static final AtomicReference<SelfHealingAgentManager> MANAGER_REF = new AtomicReference<>()

@groovy.transform.Field
private static final Logger LOGGER = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.vars.selfHealingAgents')

def call() {
    return statistics()
}

/**
 * Start the self-healing agent manager.
 */
def start(Map config = [:]) {
    boolean started = startManagerNonCps(config)
    if (started) {
        echo "Self-healing agent manager started"
    } else {
        echo "Self-healing agent manager already running"
    }
}

/**
 * Stop the self-healing agent manager.
 */
def stop() {
    if (stopManagerNonCps()) {
        echo "Self-healing agent manager stopped"
    }
}

/**
 * Perform an immediate health check.
 */
def healthCheck() {
    if (!performHealthCheckNonCps()) {
        echo "Self-healing agent manager not running"
    }
}

/**
 * Get statistics about healing operations.
 */
def statistics() {
    return getStatisticsNonCps()
}

/**
 * Get status of all agents.
 */
def agentStatuses() {
    return getAgentStatusesNonCps()
}

/**
 * Check if manager is running.
 */
def isRunning() {
    return isRunningNonCps()
}

@NonCPS
private boolean startManagerNonCps(Map config) {
    // Use plain java.util.logging.Logger callbacks (never CPS echo closures) so completed
    // WorkflowRun references are never captured or invoked on background scheduler threads.
    SelfHealingAgentManager candidate = new SelfHealingAgentManager(config ?: [:])
    if (!MANAGER_REF.compareAndSet(null, candidate)) {
        return false
    }

    Logger log = LOGGER
    candidate.onRecoveryStart { agentName, attempt ->
        log.info("Starting recovery for agent ${agentName} (attempt ${attempt})")
    }
    candidate.onRecoverySuccess { agentName ->
        log.info("Agent ${agentName} recovered successfully")
    }
    candidate.onRecoveryFailure { agentName, reason ->
        log.warning("Failed to recover agent ${agentName}: ${reason}")
    }
    candidate.onAgentReplaced { agentName ->
        log.info("Agent ${agentName} has been replaced")
    }

    candidate.start()
    return true
}

@NonCPS
private boolean stopManagerNonCps() {
    SelfHealingAgentManager current = MANAGER_REF.getAndSet(null)
    if (current != null) {
        current.stop()
        return true
    }
    return false
}

@NonCPS
private boolean performHealthCheckNonCps() {
    SelfHealingAgentManager current = MANAGER_REF.get()
    if (current != null) {
        current.performHealthCheck()
        return true
    }
    return false
}

@NonCPS
private Map getStatisticsNonCps() {
    SelfHealingAgentManager current = MANAGER_REF.get()
    if (current != null) {
        return current.getStatistics()
    }
    return [error: 'Manager not running']
}

@NonCPS
private List getAgentStatusesNonCps() {
    SelfHealingAgentManager current = MANAGER_REF.get()
    if (current != null) {
        return current.getAgentStatuses()
    }
    return []
}

@NonCPS
private boolean isRunningNonCps() {
    SelfHealingAgentManager current = MANAGER_REF.get()
    return current != null && current.isRunning()
}
