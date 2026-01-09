/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for self-healing agent management.
 */

import com.github.thomasvincent.jenkinsscripts.healing.SelfHealingAgentManager

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
static SelfHealingAgentManager manager

def call() {
    return statistics()
}

/**
 * Start the self-healing agent manager.
 */
def start(Map config = [:]) {
    if (manager != null) {
        echo "Self-healing agent manager already running"
        return
    }

    manager = new SelfHealingAgentManager(config)

    // Set up callbacks
    manager.onRecoveryStart { agentName, attempt ->
        echo "Starting recovery for agent ${agentName} (attempt ${attempt})"
    }

    manager.onRecoverySuccess { agentName ->
        echo "Agent ${agentName} recovered successfully"
    }

    manager.onRecoveryFailure { agentName, reason ->
        echo "Failed to recover agent ${agentName}: ${reason}"
    }

    manager.onAgentReplaced { agentName ->
        echo "Agent ${agentName} has been replaced"
    }

    manager.start()
    echo "Self-healing agent manager started"
}

/**
 * Stop the self-healing agent manager.
 */
def stop() {
    if (manager != null) {
        manager.stop()
        manager = null
        echo "Self-healing agent manager stopped"
    }
}

/**
 * Perform an immediate health check.
 */
def healthCheck() {
    if (manager != null) {
        manager.performHealthCheck()
    } else {
        echo "Self-healing agent manager not running"
    }
}

/**
 * Get statistics about healing operations.
 */
def statistics() {
    if (manager != null) {
        return manager.getStatistics()
    }
    return [error: 'Manager not running']
}

/**
 * Get status of all agents.
 */
def agentStatuses() {
    if (manager != null) {
        return manager.getAgentStatuses()
    }
    return []
}

/**
 * Check if manager is running.
 */
def isRunning() {
    return manager != null
}
