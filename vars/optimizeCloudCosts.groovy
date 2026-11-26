// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.cost.CloudCostOptimizer
import jenkins.model.Jenkins

/**
 * Pipeline step for cloud cost optimization across multiple providers.
 * 
 * This step provides Jenkins Pipeline integration for cloud cost analysis,
 * optimization recommendations, and budget monitoring.
 * 
 * Usage in Pipeline:
 * 
 * @Library('jenkins-script-library') _
 * 
 * pipeline {
 *     agent any
 *     triggers {
 *         // Run cost analysis daily
 *         cron('0 9 * * *')
 *     }
 *     stages {
 *         stage('Cost Analysis') {
 *             steps {
 *                 script {
 *                     // Analyze current costs
 *                     def costs = optimizeCloudCosts.analyze()
 *                     echo "Total monthly cost: \$${costs.totalCost.monthly}"
 *                     
 *                     // Check for savings opportunities
 *                     if (costs.savings.total > 100) {
 *                         echo "Potential savings: \$${costs.savings.monthlyTotal}/month"
 *                         
 *                         // Apply optimizations (with safety check)
 *                         def optimized = optimizeCloudCosts.optimize(dryRun: false)
 *                         echo "Applied ${optimized.actions.size()} optimizations"
 *                     }
 *                     
 *                     // Monitor budgets
 *                     def budgets = optimizeCloudCosts.checkBudgets([
 *                         'total': 5000,
 *                         'team:platform': 2000,
 *                         'team:apps': 3000
 *                     ])
 *                     
 *                     budgets.alerts.each { alert ->
 *                         if (alert.severity == 'HIGH') {
 *                             error "Budget alert: ${alert.message}"
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 *     post {
 *         always {
 *             // Send cost report
 *             script {
 *                 def report = optimizeCloudCosts.generateReport()
 *                 emailext(
 *                     to: 'devops@company.com',
 *                     subject: 'Jenkins Cloud Cost Report',
 *                     body: report
 *                 )
 *             }
 *         }
 *     }
 * }
 */
def call(Map args = [:]) {
    String action = (args.action ?: 'analyze') as String

    try {
        switch (action.toLowerCase()) {
            case 'analyze':
                echo "[optimizeCloudCosts] Analyzing cloud costs across all providers..."
                Map analysis = analyzeCostsNonCps(args)

                echo "[optimizeCloudCosts] Cost analysis summary:"
                echo "  Total hourly cost: \$${formatCurrency(analysis.totalCost.hourly)}"
                echo "  Total monthly cost: \$${formatCurrency(analysis.totalCost.monthly)}"
                echo "  Potential monthly savings: \$${formatCurrency(analysis.savings.monthlyTotal)}"

                return analysis

            case 'optimize':
                boolean dryRun = args.dryRun != false

                echo "[optimizeCloudCosts] ${dryRun ? 'Finding' : 'Applying'} cost optimizations..."
                Map results = optimizeResourcesNonCps(dryRun)

                echo "[optimizeCloudCosts] Optimization results:"
                echo "  Actions: ${results.actions.size()}"
                echo "  Estimated savings: \$${formatCurrency(results.estimatedMonthlySavings)}/month"

                if (!dryRun) {
                    currentBuild.description = "Saved \$${formatCurrency(results.estimatedMonthlySavings)}/month"
                }

                return results

            case 'allocate':
                echo "[optimizeCloudCosts] Calculating cost allocations..."
                Map allocations = trackCostAllocationNonCps()

                if (allocations.byTeam) {
                    echo "[optimizeCloudCosts] Top teams by cost:"
                    allocations.byTeam.sort { -it.value }.take(5).each { team, cost ->
                        echo "  ${team}: \$${formatCurrency(cost * 24 * 30)}/month"
                    }
                }

                return allocations

            case 'budget':
                Map budgets = (args.budgets ?: [:]) as Map
                if (budgets.isEmpty()) {
                    error "Budget monitoring requires budget configuration"
                }

                echo "[optimizeCloudCosts] Monitoring budget status..."
                Map monitoring = monitorBudgetNonCps(budgets)

                monitoring.alerts.each { alert ->
                    if (alert.severity == 'HIGH') {
                        echo "[optimizeCloudCosts] ERROR: ${alert.message}"
                    } else {
                        echo "[optimizeCloudCosts] WARNING: ${alert.message}"
                    }
                }

                return monitoring

            default:
                error "Unknown cost action: ${action}. Valid actions: analyze, optimize, allocate, budget"
        }

    } catch (Exception e) {
        echo "[optimizeCloudCosts] ERROR: Cloud cost optimization failed: ${e.message}"
        throw e
    }
}

@NonCPS
private Map analyzeCostsNonCps(Map args) {
    def optimizer = new CloudCostOptimizer(Jenkins.get())
    return optimizer.analyzeCosts(args)
}

@NonCPS
private Map optimizeResourcesNonCps(boolean dryRun) {
    def optimizer = new CloudCostOptimizer(Jenkins.get())
    return optimizer.optimizeResources(dryRun)
}

@NonCPS
private Map trackCostAllocationNonCps() {
    def optimizer = new CloudCostOptimizer(Jenkins.get())
    return optimizer.trackCostAllocation()
}

@NonCPS
private Map monitorBudgetNonCps(Map budgets) {
    def optimizer = new CloudCostOptimizer(Jenkins.get())
    Map<String, Double> typedBudgets = budgets.collectEntries { k, v -> [k.toString(), (v as Number).doubleValue()] }
    return optimizer.monitorBudget(typedBudgets)
}

/**
 * Analyze costs (default action).
 */
def analyze(Map args = [:]) {
    return call(args + [action: 'analyze'])
}

/**
 * Optimize resources.
 */
def optimize(Map args = [:]) {
    return call(args + [action: 'optimize'])
}

/**
 * Get cost allocations.
 */
def getAllocations() {
    return call(action: 'allocate')
}

/**
 * Check budgets.
 */
def checkBudgets(Map budgets) {
    return call(action: 'budget', budgets: budgets)
}

/**
 * Generate a formatted cost report.
 */
def generateReport(Map args = [:]) {
    def analysis = analyze(args)
    def report = new StringBuilder()
    
    report << "Jenkins Cloud Cost Report\n"
    report << "========================\n\n"
    report << "Generated: ${analysis.timestamp}\n\n"
    
    report << "TOTAL COSTS:\n"
    report << "  Hourly:  \$${formatCurrency(analysis.totalCost.hourly)}\n"
    report << "  Daily:   \$${formatCurrency(analysis.totalCost.daily)}\n"
    report << "  Monthly: \$${formatCurrency(analysis.totalCost.monthly)}\n"
    report << "  Yearly:  \$${formatCurrency(analysis.totalCost.yearly)}\n\n"
    
    report << "PROVIDER BREAKDOWN:\n"
    analysis.providers.each { name, provider ->
        report << "  ${name}: \$${formatCurrency(provider.monthlyCost)}/month (${provider.activeAgents} agents)\n"
    }
    
    if (analysis.savings.total > 0) {
        report << "\nPOTENTIAL SAVINGS:\n"
        report << "  Monthly: \$${formatCurrency(analysis.savings.monthlyTotal)}\n"
        report << "  - Idle agents: \$${formatCurrency(analysis.savings.idleAgents * 24 * 30)}\n"
        report << "  - Spot instances: \$${formatCurrency(analysis.savings.spotInstances * 24 * 30)}\n"
        report << "  - Right-sizing: \$${formatCurrency(analysis.savings.rightsizing * 24 * 30)}\n"
    }
    
    if (analysis.recommendations) {
        report << "\nRECOMMENDATIONS:\n"
        analysis.recommendations.each { rec ->
            report << "  [${rec.priority}] ${rec.message}\n"
        }
    }
    
    return report.toString()
}

/**
 * Format currency for display.
 */
@NonCPS
private String formatCurrency(def value) {
    if (value instanceof Number) {
        return String.format("%.2f", value.doubleValue())
    }
    return "0.00"
}