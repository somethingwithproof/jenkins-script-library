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

import com.github.thomasvincent.jenkinsscripts.cost.CloudCostOptimizer
import com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger
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
    def action = args.action ?: 'analyze'
    def jenkins = Jenkins.get()
    def logger = new JenkinsLogger(currentBuild.rawBuild.getListener(), 'optimizeCloudCosts')
    
    try {
        def optimizer = new CloudCostOptimizer(jenkins)
        
        switch (action.toLowerCase()) {
            case 'analyze':
                logger.info("Analyzing cloud costs across all providers...")
                def analysis = optimizer.analyzeCosts(args)
                
                // Log summary
                logger.info("Cost analysis summary:")
                logger.info("  Total hourly cost: \$${formatCurrency(analysis.totalCost.hourly)}")
                logger.info("  Total monthly cost: \$${formatCurrency(analysis.totalCost.monthly)}")
                logger.info("  Potential monthly savings: \$${formatCurrency(analysis.savings.monthlyTotal)}")
                
                // Add Pipeline-friendly methods
                analysis.metaClass.hasSavingsOpportunity = { threshold = 100 ->
                    analysis.savings.monthlyTotal >= threshold
                }
                
                analysis.metaClass.getCostByProvider = { provider ->
                    analysis.providers[provider]?.monthlyCost ?: 0
                }
                
                analysis.metaClass.getTopRecommendations = { count = 3 ->
                    analysis.recommendations.take(count)
                }
                
                return analysis
                
            case 'optimize':
                def dryRun = args.dryRun != false  // Default to dry run for safety
                
                logger.info("${dryRun ? 'Finding' : 'Applying'} cost optimizations...")
                def results = optimizer.optimizeResources(dryRun)
                
                logger.info("Optimization results:")
                logger.info("  Actions: ${results.actions.size()}")
                logger.info("  Estimated savings: \$${formatCurrency(results.estimatedMonthlySavings)}/month")
                
                if (!dryRun) {
                    // Add to build description
                    currentBuild.description = "Saved \$${formatCurrency(results.estimatedMonthlySavings)}/month"
                }
                
                return results
                
            case 'allocate':
                logger.info("Calculating cost allocations...")
                def allocations = optimizer.trackCostAllocation()
                
                // Log top consumers
                if (allocations.byTeam) {
                    logger.info("Top teams by cost:")
                    allocations.byTeam.sort { -it.value }.take(5).each { team, cost ->
                        logger.info("  ${team}: \$${formatCurrency(cost * 24 * 30)}/month")
                    }
                }
                
                return allocations
                
            case 'budget':
                def budgets = args.budgets ?: [:]
                if (budgets.isEmpty()) {
                    error "Budget monitoring requires budget configuration"
                }
                
                logger.info("Monitoring budget status...")
                def monitoring = optimizer.monitorBudget(budgets)
                
                // Log alerts
                monitoring.alerts.each { alert ->
                    if (alert.severity == 'HIGH') {
                        logger.error(alert.message)
                    } else {
                        logger.warning(alert.message)
                    }
                }
                
                return monitoring
                
            default:
                error "Unknown cost action: ${action}. Valid actions: analyze, optimize, allocate, budget"
        }
        
    } catch (Exception e) {
        logger.error("Cloud cost optimization failed: ${e.message}")
        throw e
    }
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
private String formatCurrency(def value) {
    if (value instanceof Number) {
        return String.format("%.2f", value)
    }
    return "0.00"
}