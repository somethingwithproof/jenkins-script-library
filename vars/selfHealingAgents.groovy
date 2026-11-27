// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024-2026 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for single-pass offline-agent health checks.
 */

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.healing.SelfHealingAgentManager
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Runs a single-pass health check and recovery cycle against offline Jenkins agents.
 *
 * Unlike a long-lived controller daemon, each invocation executes synchronously
 * within the build without creating background scheduler threads or mutating
 * shared static state across builds. For continuous controller-wide monitoring,
 * schedule a periodic job calling this step or implement an AsyncPeriodicWork
 * extension in a Jenkins plugin.
 */

@groovy.transform.Field
private static final Logger LOGGER = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.vars.selfHealingAgents')

def call(Map config = [:]) {
    Map result = runSinglePassNonCps(config ?: [:])
    List statuses = (result.agentStatuses ?: []) as List
    echo "[selfHealingAgents] Checked ${statuses.size()} agents: " +
         "${result.totalAttempts} recovery attempts, ${result.successful} recovered, ${result.failed} failed"
    return result
}

/**
 * Performs one immediate health-check and recovery cycle.
 */
def healthCheck(Map config = [:]) {
    return call(config)
}

/**
 * Returns a single-pass agent health snapshot without triggering reconnects unless enabled.
 */
def statistics(Map config = [:]) {
    Map userConfig = config ?: [:]
    boolean recoveryRequested = (userConfig.enableAutoReconnect == true) ||
        (userConfig.enableAutoRestart == true) ||
        (userConfig.enableAutoReplace == true)
    Map defaults = [
        enableAutoReconnect: false,
        enableAutoRestart  : false,
        enableAutoReplace  : false,
        readOnly           : !recoveryRequested
    ]
    return runSinglePassNonCps(defaults + userConfig)
}

/**
 * Returns plain serializable status maps for all non-controller agents.
 */
def agentStatuses(Map config = [:]) {
    Map snapshot = statistics(config)
    return (snapshot.agentStatuses ?: []) as List
}

@NonCPS
private Map runSinglePassNonCps(Map config) {
    int maxRetryAttempts = config.maxRetryAttempts != null ? (config.maxRetryAttempts as Integer) : 1
    if (maxRetryAttempts < 0) {
        throw new IllegalArgumentException("maxRetryAttempts must be >= 0, got: ${maxRetryAttempts}")
    }
    long retryDelayMs = config.retryDelayMs != null ? (config.retryDelayMs as Long) : 0L
    if (retryDelayMs < 0L) {
        throw new IllegalArgumentException("retryDelayMs must be >= 0, got: ${retryDelayMs}")
    }
    boolean readOnly = config.readOnly == true

    Map<String, Object> effectiveConfig = [
        maxRetryAttempts   : maxRetryAttempts,
        retryDelayMs       : retryDelayMs,
        enableAutoReconnect: config.enableAutoReconnect != false,
        enableAutoRestart  : config.enableAutoRestart != false,
        enableAutoReplace  : config.enableAutoReplace == true,
        readOnly           : readOnly,
        singlePass         : true
    ]

    SelfHealingAgentManager manager = new SelfHealingAgentManager(effectiveConfig)
    Logger log = LOGGER
    manager.onRecoveryStart { agentName, attempt ->
        log.info("Starting single-pass recovery for agent ${agentName} (attempt ${attempt})")
    }
    manager.onRecoverySuccess { agentName ->
        log.info("Agent ${agentName} recovered successfully")
    }
    manager.onRecoveryFailure { agentName, reason ->
        log.warning("Failed to recover agent ${agentName}: ${reason}")
    }
    manager.onAgentReplaced { agentName ->
        log.info("Agent ${agentName} marked for replacement")
    }

    try {
        int maxPasses = readOnly ? 1 : (maxRetryAttempts + 1)
        for (int pass = 0; pass < maxPasses; pass++) {
            manager.performHealthCheck()
            if (readOnly || pass + 1 >= maxPasses) {
                break
            }
            boolean hasActionableRecovering = manager.getAgentStatuses().any { Map<String, Object> status ->
                status.status == SelfHealingAgentManager.AgentStatus.RECOVERING.name() &&
                    !manager.hasPendingRecovery(status.agentName as String)
            }
            if (!hasActionableRecovering) {
                break
            }
            if (retryDelayMs > 0L) {
                try {
                    Thread.sleep(retryDelayMs)
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt()
                    throw ie
                }
            }
        }
        Map<String, Object> stats = new LinkedHashMap<>(manager.getStatistics())
        List<Map<String, Object>> statuses = manager.getAgentStatuses()
        stats.put('agentStatuses', statuses)
        return stats
    } catch (Exception e) {
        log.log(Level.WARNING, "Single-pass agent health check failed", e)
        throw e
    } finally {
        manager.stop()
    }
}
