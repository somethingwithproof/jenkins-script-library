#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/**
 * Controller admin script: inventory cloud agents, estimate costs, and check agent health.
 *
 * Run from Manage Jenkins -> Script Console or via:
 *   java -jar jenkins-cli.jar -s <url> groovy = < admin-scripts/optimize-agents.groovy
 *
 * Optional environment or binding variables:
 *   RECOVER_OFFLINE  true/false (default: false; read-only inspection by default)
 */

import com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory
import com.github.thomasvincent.jenkinsscripts.cost.CloudCostOptimizer
import com.github.thomasvincent.jenkinsscripts.healing.SelfHealingAgentManager
import jenkins.model.Jenkins
import java.util.logging.Level
import java.util.logging.Logger

Logger log = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.admin.optimizeAgents')

def resolveFlag = { String name, boolean defaultValue ->
    String raw = binding.hasVariable(name) ? binding.getVariable(name)?.toString() : System.getenv(name)
    if (raw == null || raw.trim().isEmpty()) {
        return defaultValue
    }
    return raw.trim().equalsIgnoreCase('true')
}

try {
    Jenkins jenkins = Jenkins.get()
    boolean recoverOffline = resolveFlag('RECOVER_OFFLINE', false)

    Map inventory = new CloudInventory().inspect(jenkins)
    println "Configured clouds: ${(inventory.clouds ?: []).size()}"
    println "Active cloud agents: ${(inventory.agents ?: []).size()}"

    def optimizer = new CloudCostOptimizer(jenkins)
    Map costs = optimizer.analyzeCosts()
    println "Estimated monthly agent cost: \$${String.format('%.2f', ((costs.totalCost?.monthly ?: 0) as Number).doubleValue())}"
    println "Estimated monthly savings:    \$${String.format('%.2f', ((costs.savings?.monthlyTotal ?: 0) as Number).doubleValue())}"

    def healer = new SelfHealingAgentManager([
        enableAutoReconnect: recoverOffline,
        enableAutoRestart  : recoverOffline,
        enableAutoReplace  : false,
        readOnly           : !recoverOffline,
        singlePass         : true
    ], jenkins)
    try {
        healer.performHealthCheck()
        Map stats = healer.getStatistics()
        println "Agent health check: ${(stats.agents ?: [:]).size()} agents inspected, ${stats.totalAttempts} recovery attempts"
        log.info("Agent optimization summary completed (recoverOffline=${recoverOffline})")
    } finally {
        healer.stop()
    }
} catch (Exception e) {
    log.log(Level.SEVERE, 'Agent optimization script failed', e)
    throw e
}
