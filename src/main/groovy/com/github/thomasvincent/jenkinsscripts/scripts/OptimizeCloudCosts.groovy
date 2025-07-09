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

package com.github.thomasvincent.jenkinsscripts.scripts

import com.github.thomasvincent.jenkinsscripts.cost.CloudCostOptimizer
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import jenkins.model.Jenkins
import groovy.json.JsonBuilder
import java.util.logging.Logger

/**
 * Jenkins CLI script for cloud cost optimization across multiple providers.
 *
 * This script provides comprehensive cloud cost management:
 * - Multi-cloud cost analysis (AWS, Azure, GCP, Kubernetes)
 * - Idle agent detection and termination
 * - Spot/preemptible instance recommendations
 * - Right-sizing recommendations
 * - Cost allocation tracking
 * - Budget monitoring
 *
 * Usage:
 *   jenkins-cli groovy = < OptimizeCloudCosts.groovy <command> [options]
 *
 * Commands:
 *   analyze   - Analyze current cloud costs
 *   optimize  - Find and apply cost optimizations
 *   allocate  - Show cost allocations by team/project
 *   budget    - Monitor budget status
 *   help      - Show this help message
 *
 * Examples:
 *   jenkins-cli groovy = < OptimizeCloudCosts.groovy analyze
 *   jenkins-cli groovy = < OptimizeCloudCosts.groovy optimize --dry-run
 *   jenkins-cli groovy = < OptimizeCloudCosts.groovy allocate --format json
 *   jenkins-cli groovy = < OptimizeCloudCosts.groovy budget --budgets '{"total":5000,"team:dev":2000}'
 *
 * @author Thomas Vincent
 * @since 1.4.0
 */

// Initialize logger
Logger logger = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.OptimizeCloudCosts')

// Parse command line arguments
def cli = args as List
if (cli.isEmpty() || cli[0] in ['-h', '--help', 'help']) {
    showHelp(logger)
    return
}

String command = cli[0]
def options = parseOptions(cli.drop(1))

// Main execution
ErrorHandler.withErrorHandling('optimize cloud costs', logger) {
    def jenkins = Jenkins.get()
    def optimizer = new CloudCostOptimizer(jenkins)
    
    switch (command.toLowerCase()) {
        case 'analyze':
            handleAnalyze(optimizer, options, logger)
            break
            
        case 'optimize':
            handleOptimize(optimizer, options, logger)
            break
            
        case 'allocate':
            handleAllocate(optimizer, options, logger)
            break
            
        case 'budget':
            handleBudget(optimizer, options, logger)
            break
            
        default:
            logger.severe("Unknown command: ${command}")
            showHelp(logger)
            System.exit(1)
    }
}

/**
 * Shows help message.
 */
void showHelp(Logger logger) {
    logger.info """
Cloud Cost Optimizer
===================

Comprehensive cloud cost management for Jenkins across AWS, Azure, GCP, and Kubernetes.

Usage: jenkins-cli groovy = < OptimizeCloudCosts.groovy <command> [options]

Commands:
  analyze   Analyze current cloud costs and identify savings opportunities
  optimize  Apply cost optimizations (terminate idle, convert to spot, rightsize)
  allocate  Show cost allocations by team/project/pipeline
  budget    Monitor budget status and generate alerts
  help      Show this help message

Options:
  --format <format>        Output format: text, json, summary (default: summary)
  --dry-run               Preview optimizations without applying them
  --include-allocations   Include detailed cost allocations in analysis
  --budgets <json>        Budget configuration as JSON for monitoring
  --threshold <number>    Cost threshold for recommendations (default: 0.10)

Examples:
  Analyze cloud costs with allocations:
    jenkins-cli groovy = < OptimizeCloudCosts.groovy analyze --include-allocations
  
  Preview cost optimizations:
    jenkins-cli groovy = < OptimizeCloudCosts.groovy optimize --dry-run
  
  Export cost allocations as JSON:
    jenkins-cli groovy = < OptimizeCloudCosts.groovy allocate --format json > allocations.json
  
  Monitor budgets:
    jenkins-cli groovy = < OptimizeCloudCosts.groovy budget --budgets '{"total":5000,"team:dev":2000}'

Cost Optimization Features:
- Idle Agent Detection: Identifies agents idle for >30 minutes
- Spot Instances: Recommends spot/preemptible instances for >30% savings
- Right-sizing: Suggests smaller instances for underutilized agents
- Multi-cloud Support: Works with AWS, Azure, GCP, and Kubernetes
"""
}

/**
 * Parses command line options.
 */
Map parseOptions(List<String> args) {
    def options = [
        format: 'summary',
        dryRun: false,
        includeAllocations: false,
        budgets: [:],
        threshold: 0.10
    ]
    
    def iterator = args.iterator()
    while (iterator.hasNext()) {
        def arg = iterator.next()
        switch (arg) {
            case '--format':
                if (iterator.hasNext()) {
                    options.format = iterator.next()
                }
                break
            case '--dry-run':
                options.dryRun = true
                break
            case '--include-allocations':
                options.includeAllocations = true
                break
            case '--budgets':
                if (iterator.hasNext()) {
                    options.budgets = parseJsonBudgets(iterator.next())
                }
                break
            case '--threshold':
                if (iterator.hasNext()) {
                    options.threshold = Double.parseDouble(iterator.next())
                }
                break
        }
    }
    
    return options
}

/**
 * Parses JSON budget configuration.
 */
Map parseJsonBudgets(String json) {
    try {
        def parsed = new groovy.json.JsonSlurper().parseText(json) as Map
        return parsed.collectEntries { k, v -> [k, v as Double] }
    } catch (Exception e) {
        logger.warning("Failed to parse budgets: ${e.message}")
        return [:]
    }
}

/**
 * Handles analyze command.
 */
void handleAnalyze(CloudCostOptimizer optimizer, Map options, Logger logger) {
    logger.info("Analyzing cloud costs...")
    
    def analysis = optimizer.analyzeCosts(options)
    
    switch (options.format) {
        case 'json':
            outputJson(analysis, logger)
            break
        case 'text':
            outputDetailedAnalysis(analysis, logger)
            break
        case 'summary':
        default:
            outputAnalysisSummary(analysis, logger)
            break
    }
}

/**
 * Handles optimize command.
 */
void handleOptimize(CloudCostOptimizer optimizer, Map options, Logger logger) {
    logger.info("Finding cost optimization opportunities...")
    
    def results = optimizer.optimizeResources(options.dryRun)
    
    switch (options.format) {
        case 'json':
            outputJson(results, logger)
            break
        default:
            outputOptimizationResults(results, logger)
            break
    }
}

/**
 * Handles allocate command.
 */
void handleAllocate(CloudCostOptimizer optimizer, Map options, Logger logger) {
    logger.info("Calculating cost allocations...")
    
    def allocations = optimizer.trackCostAllocation()
    
    switch (options.format) {
        case 'json':
            outputJson(allocations, logger)
            break
        default:
            outputAllocations(allocations, logger)
            break
    }
}

/**
 * Handles budget command.
 */
void handleBudget(CloudCostOptimizer optimizer, Map options, Logger logger) {
    if (options.budgets.isEmpty()) {
        logger.severe("No budgets specified. Use --budgets option with JSON configuration.")
        return
    }
    
    logger.info("Monitoring budget status...")
    
    def monitoring = optimizer.monitorBudget(options.budgets as Map<String, Double>)
    
    switch (options.format) {
        case 'json':
            outputJson(monitoring, logger)
            break
        default:
            outputBudgetMonitoring(monitoring, logger)
            break
    }
}

/**
 * Outputs JSON format.
 */
void outputJson(Object data, Logger logger) {
    def json = new JsonBuilder(data)
    logger.info(json.toPrettyString())
}

/**
 * Outputs analysis summary.
 */
void outputAnalysisSummary(Map analysis, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("CLOUD COST ANALYSIS SUMMARY")
    logger.info("="*80)
    
    logger.info("\nTimestamp: ${analysis.timestamp}")
    
    def total = analysis.totalCost as Map
    logger.info("\nTOTAL COSTS:")
    logger.info("  Hourly:  \$${formatCurrency(total.hourly)}")
    logger.info("  Daily:   \$${formatCurrency(total.daily)}")
    logger.info("  Monthly: \$${formatCurrency(total.monthly)}")
    logger.info("  Yearly:  \$${formatCurrency(total.yearly)}")
    
    logger.info("\nPROVIDER BREAKDOWN:")
    analysis.providers.each { name, provider ->
        logger.info("\n${name}:")
        logger.info("  Active Agents: ${provider.activeAgents}")
        logger.info("  Hourly Cost: \$${formatCurrency(provider.totalCost)}")
        logger.info("  Monthly Cost: \$${formatCurrency(provider.monthlyCost)}")
    }
    
    def savings = analysis.savings as Map
    if (savings.total > 0) {
        logger.info("\nPOTENTIAL SAVINGS:")
        logger.info("  Idle Agents: \$${formatCurrency(savings.idleAgents)}/hour")
        logger.info("  Spot Instances: \$${formatCurrency(savings.spotInstances)}/hour")
        logger.info("  Right-sizing: \$${formatCurrency(savings.rightsizing)}/hour")
        logger.info("  Total: \$${formatCurrency(savings.total)}/hour (\$${formatCurrency(savings.monthlyTotal)}/month)")
    }
    
    def recommendations = analysis.recommendations as List
    if (recommendations) {
        logger.info("\nTOP RECOMMENDATIONS:")
        recommendations.take(3).each { rec ->
            logger.info("\n[${rec.priority}] ${rec.type}")
            logger.info("  ${rec.message}")
            logger.info("  Impact: ${rec.impact}")
        }
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs detailed analysis.
 */
void outputDetailedAnalysis(Map analysis, Logger logger) {
    outputAnalysisSummary(analysis, logger)
    
    logger.info("\nINSTANCE DETAILS:")
    logger.info("="*80)
    
    analysis.providers.each { name, provider ->
        if (provider.instances) {
            logger.info("\n${name} Instances:")
            provider.instances.each { instance ->
                logger.info("\n  ${instance.name}")
                logger.info("    Type: ${instance.type}")
                logger.info("    Cost: \$${formatCurrency(instance.hourlyCost)}/hour")
                logger.info("    Spot: ${instance.isSpot ? 'Yes' : 'No'}")
                logger.info("    Uptime: ${instance.uptime} minutes")
                logger.info("    Idle: ${instance.idleTime} minutes")
                logger.info("    Utilization: ${instance.utilization}%")
                logger.info("    Labels: ${instance.labels}")
            }
        }
    }
    
    if (analysis.allocations && !analysis.allocations.isEmpty()) {
        logger.info("\nCOST ALLOCATIONS:")
        logger.info("="*80)
        outputAllocations(analysis.allocations as Map, logger)
    }
}

/**
 * Outputs optimization results.
 */
void outputOptimizationResults(Map results, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("CLOUD COST OPTIMIZATION RESULTS")
    logger.info("="*80)
    
    logger.info("\nMode: ${results.dryRun ? 'DRY RUN (Preview Only)' : 'LIVE EXECUTION'}")
    logger.info("Timestamp: ${results.timestamp}")
    
    def actions = results.actions as List
    if (actions.isEmpty()) {
        logger.info("\nNo optimization opportunities found.")
    } else {
        logger.info("\nOPTIMIZATION ACTIONS:")
        
        def byType = actions.groupBy { it.type }
        
        byType.each { type, typeActions ->
            logger.info("\n${type}:")
            typeActions.each { action ->
                switch (type) {
                    case 'TERMINATE_IDLE':
                        logger.info("  - ${action.agent}")
                        logger.info("    Idle Time: ${action.idleTime} minutes")
                        logger.info("    Savings: \$${formatCurrency(action.savingsPerHour)}/hour")
                        break
                    case 'CONVERT_TO_SPOT':
                        logger.info("  - ${action.agent}")
                        logger.info("    Current: \$${formatCurrency(action.currentCost)}/hour")
                        logger.info("    Spot: \$${formatCurrency(action.spotCost)}/hour")
                        logger.info("    Savings: \$${formatCurrency(action.savingsPerHour)}/hour")
                        break
                    case 'RIGHTSIZE_INSTANCE':
                        logger.info("  - ${action.agent}")
                        logger.info("    From: ${action.currentType} (\$${formatCurrency(action.currentCost)}/hour)")
                        logger.info("    To: ${action.recommendedType} (\$${formatCurrency(action.newCost)}/hour)")
                        logger.info("    Savings: \$${formatCurrency(action.savingsPerHour)}/hour")
                        break
                }
            }
        }
        
        logger.info("\nSUMMARY:")
        logger.info("  Total Actions: ${actions.size()}")
        logger.info("  Estimated Savings: \$${formatCurrency(results.estimatedSavings)}/hour")
        logger.info("  Monthly Savings: \$${formatCurrency(results.estimatedMonthlySavings)}")
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs cost allocations.
 */
void outputAllocations(Map allocations, Logger logger) {
    logger.info("\nBY TEAM:")
    if (allocations.byTeam.isEmpty()) {
        logger.info("  No team allocations found")
    } else {
        allocations.byTeam.each { team, cost ->
            logger.info("  ${team}: \$${formatCurrency(cost)}/hour (\$${formatCurrency(cost * 24 * 30)}/month)")
        }
    }
    
    logger.info("\nBY PROJECT:")
    if (allocations.byProject.isEmpty()) {
        logger.info("  No project allocations found")
    } else {
        allocations.byProject.each { project, cost ->
            logger.info("  ${project}: \$${formatCurrency(cost)}/hour (\$${formatCurrency(cost * 24 * 30)}/month)")
        }
    }
    
    logger.info("\nBY PIPELINE:")
    if (allocations.byPipeline.isEmpty()) {
        logger.info("  No pipeline allocations found")
    } else {
        def topPipelines = allocations.byPipeline.sort { -it.value }.take(10)
        topPipelines.each { pipeline, cost ->
            logger.info("  ${pipeline}: \$${formatCurrency(cost)}/hour")
        }
        if (allocations.byPipeline.size() > 10) {
            logger.info("  ... and ${allocations.byPipeline.size() - 10} more")
        }
    }
    
    if (allocations.unallocated > 0) {
        logger.info("\nUNALLOCATED: \$${formatCurrency(allocations.unallocated)}/hour")
    }
}

/**
 * Outputs budget monitoring results.
 */
void outputBudgetMonitoring(Map monitoring, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("BUDGET MONITORING REPORT")
    logger.info("="*80)
    
    logger.info("\nTimestamp: ${monitoring.timestamp}")
    
    logger.info("\nBUDGET STATUS:")
    monitoring.budgetStatus.each { category, status ->
        logger.info("\n${category}:")
        logger.info("  Budget: \$${formatCurrency(status.budget)}")
        logger.info("  Spent: \$${formatCurrency(status.spent)} (${status.percentUsed}%)")
        logger.info("  Remaining: \$${formatCurrency(status.remaining)}")
        
        def bar = createProgressBar(status.percentUsed as Double)
        logger.info("  Progress: ${bar}")
    }
    
    def alerts = monitoring.alerts as List
    if (alerts) {
        logger.info("\nALERTS:")
        alerts.each { alert ->
            logger.info("  [${alert.severity}] ${alert.message}")
        }
    } else {
        logger.info("\nNo budget alerts.")
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Formats currency values.
 */
String formatCurrency(def value) {
    if (value instanceof Number) {
        return String.format("%.2f", value)
    }
    return "0.00"
}

/**
 * Creates a visual progress bar.
 */
String createProgressBar(double percent) {
    def width = 30
    def filled = Math.min((percent / 100 * width) as Integer, width)
    def empty = width - filled
    
    def bar = "[" + "=" * filled + " " * empty + "]"
    
    // Add color indicators
    if (percent >= 90) {
        return "${bar} CRITICAL"
    } else if (percent >= 75) {
        return "${bar} WARNING"
    } else {
        return "${bar} OK"
    }
}

logger.info("Cloud cost optimization analysis complete.")