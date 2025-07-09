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

import com.github.thomasvincent.jenkinsscripts.analytics.PipelineMetricsCollector
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import jenkins.model.Jenkins
import groovy.json.JsonBuilder
import java.util.logging.Logger

/**
 * Jenkins CLI script that analyzes pipeline performance metrics and provides actionable insights.
 *
 * This script collects comprehensive metrics including:
 * - Stage-level performance analysis
 * - Anomaly detection
 * - Trend analysis
 * - Actionable recommendations
 *
 * Usage:
 *   jenkins-cli groovy = < AnalyzePipelineMetrics.groovy <job-name> [history-limit] [output-format]
 *
 * Parameters:
 *   job-name      - Full name of the pipeline job to analyze
 *   history-limit - Number of builds to analyze (default: 100)
 *   output-format - Output format: json, summary, or detailed (default: summary)
 *
 * Examples:
 *   jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-folder/my-pipeline"
 *   jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-pipeline" 50 detailed
 *   jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-pipeline" 200 json > metrics.json
 *
 * @author Thomas Vincent
 * @since 1.4.0
 */

// Initialize logger
Logger logger = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.AnalyzePipelineMetrics')

// Parse command line arguments
def cli = args as List
if (cli.isEmpty() || cli[0] in ['-h', '--help']) {
    logger.info """
Pipeline Metrics Analyzer
========================

Analyzes pipeline execution metrics to identify performance issues and optimization opportunities.

Usage: jenkins-cli groovy = < AnalyzePipelineMetrics.groovy <job-name> [history-limit] [output-format]

Parameters:
  job-name      - Full name of the pipeline job to analyze
  history-limit - Number of builds to analyze (default: 100)
  output-format - Output format: json, summary, or detailed (default: summary)

Examples:
  Analyze last 100 builds with summary output:
    jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-folder/my-pipeline"
  
  Analyze last 50 builds with detailed output:
    jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-pipeline" 50 detailed
  
  Export metrics as JSON:
    jenkins-cli groovy = < AnalyzePipelineMetrics.groovy "my-pipeline" 200 json > metrics.json

Output Formats:
  summary  - High-level metrics and top recommendations
  detailed - Complete analysis with all metrics and anomalies
  json     - Machine-readable JSON format
"""
    return
}

// Parse arguments
String jobName = cli[0]
int historyLimit = cli.size() > 1 ? Integer.parseInt(cli[1]) : 100
String outputFormat = cli.size() > 2 ? cli[2] : 'summary'

// Validate inputs
try {
    ValidationUtils.requireNonEmpty(jobName, 'job-name')
    ValidationUtils.requireInRange(historyLimit, 1, 1000, 'history-limit')
    if (!(outputFormat in ['json', 'summary', 'detailed'])) {
        throw new IllegalArgumentException("Invalid output format: ${outputFormat}")
    }
} catch (IllegalArgumentException e) {
    logger.severe("Invalid input: ${e.message}")
    System.exit(1)
}

// Main execution
ErrorHandler.withErrorHandling('analyze pipeline metrics', logger) {
    def jenkins = Jenkins.get()
    def collector = new PipelineMetricsCollector(jenkins)
    
    logger.info("Analyzing pipeline: ${jobName}")
    logger.info("History limit: ${historyLimit} builds")
    
    def metrics = collector.collectPipelineMetrics(jobName, historyLimit)
    
    switch (outputFormat) {
        case 'json':
            outputJson(metrics)
            break
        case 'detailed':
            outputDetailed(metrics)
            break
        case 'summary':
        default:
            outputSummary(metrics)
            break
    }
}

/**
 * Outputs metrics in JSON format.
 */
void outputJson(Map metrics) {
    def json = new JsonBuilder(metrics)
    logger.info(json.toPrettyString())
}

/**
 * Outputs a summary view of the metrics.
 */
void outputSummary(Map metrics) {
    logger.info("\n" + "="*80)
    logger.info("PIPELINE METRICS SUMMARY")
    logger.info("="*80)
    
    logger.info("\nJob: ${metrics.jobName}")
    logger.info("Analysis Date: ${metrics.analysisTimestamp}")
    
    def summary = metrics.summary as Map
    logger.info("\nOVERALL METRICS:")
    logger.info("-" * 40)
    logger.info("Total Builds Analyzed: ${summary.totalBuilds}")
    logger.info("Success Rate: ${summary.successRate}%")
    logger.info("Average Duration: ${formatDuration(summary.avgDuration as Long)}")
    logger.info("Duration Range: ${formatDuration(summary.minDuration as Long)} - ${formatDuration(summary.maxDuration as Long)}")
    logger.info("Average Queue Time: ${formatDuration(summary.avgQueueTime as Long)}")
    
    def trends = metrics.trends as Map
    if (trends && !trends.containsKey('message')) {
        logger.info("\nTRENDS:")
        logger.info("-" * 40)
        logger.info("Duration Trend: ${trends.durationTrend} (${trends.durationChange > 0 ? '+' : ''}${trends.durationChange}%)")
        logger.info("Success Rate Trend: ${trends.successRateTrend} (${trends.successRateChange > 0 ? '+' : ''}${trends.successRateChange}%)")
    }
    
    def anomalies = metrics.anomalies as List
    if (anomalies) {
        logger.info("\nANOMALIES DETECTED: ${anomalies.size()}")
        logger.info("-" * 40)
        def highSeverity = anomalies.count { it.severity == 'HIGH' }
        if (highSeverity > 0) {
            logger.info("High Severity: ${highSeverity}")
        }
        def mediumSeverity = anomalies.count { it.severity == 'MEDIUM' }
        if (mediumSeverity > 0) {
            logger.info("Medium Severity: ${mediumSeverity}")
        }
    }
    
    def recommendations = metrics.recommendations as List
    if (recommendations) {
        logger.info("\nTOP RECOMMENDATIONS:")
        logger.info("-" * 40)
        recommendations.sort { -priorityValue(it.priority as String) }
                      .take(3)
                      .each { rec ->
            logger.info("\n[${rec.priority}] ${rec.type}")
            logger.info("  ${rec.message}")
            logger.info("  Impact: ${rec.impact}")
        }
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs detailed metrics.
 */
void outputDetailed(Map metrics) {
    // Start with summary
    outputSummary(metrics)
    
    // Add stage metrics
    def stageMetrics = metrics.stageMetrics as Map
    if (stageMetrics) {
        logger.info("\nSTAGE PERFORMANCE:")
        logger.info("="*80)
        stageMetrics.each { stageName, stats ->
            logger.info("\nStage: ${stageName}")
            logger.info("-" * 40)
            logger.info("  Executions: ${stats.executions}")
            logger.info("  Average Duration: ${formatDuration(stats.avgDuration as Long)}")
            logger.info("  Min/Max Duration: ${formatDuration(stats.minDuration as Long)} / ${formatDuration(stats.maxDuration as Long)}")
            logger.info("  Standard Deviation: ${formatDuration(stats.stdDeviation as Long)}")
            logger.info("  Failure Rate: ${stats.failureRate}%")
        }
    }
    
    // Add anomaly details
    def anomalies = metrics.anomalies as List
    if (anomalies) {
        logger.info("\nANOMALY DETAILS:")
        logger.info("="*80)
        anomalies.each { anomaly ->
            logger.info("\nBuild #${anomaly.buildNumber} - ${anomaly.type}")
            logger.info("  Severity: ${anomaly.severity}")
            logger.info("  Actual Value: ${formatDuration(anomaly.value as Long)}")
            logger.info("  Expected Value: ${formatDuration(anomaly.expectedValue as Long)}")
            logger.info("  Deviation: ${anomaly.deviation.round(2)} standard deviations")
            if (anomaly.stageName) {
                logger.info("  Stage: ${anomaly.stageName}")
            }
        }
    }
    
    // All recommendations
    def recommendations = metrics.recommendations as List
    if (recommendations) {
        logger.info("\nALL RECOMMENDATIONS:")
        logger.info("="*80)
        recommendations.sort { -priorityValue(it.priority as String) }
                      .each { rec ->
            logger.info("\n[${rec.priority}] ${rec.type}")
            logger.info("  ${rec.message}")
            logger.info("  Impact: ${rec.impact}")
        }
    }
}

/**
 * Formats duration in milliseconds to human-readable format.
 */
String formatDuration(long millis) {
    if (millis < 1000) {
        return "${millis}ms"
    } else if (millis < 60000) {
        return "${(millis / 1000).round(1)}s"
    } else if (millis < 3600000) {
        def minutes = millis / 60000
        def seconds = (millis % 60000) / 1000
        return "${minutes.toInteger()}m ${seconds.round()}s"
    } else {
        def hours = millis / 3600000
        def minutes = (millis % 3600000) / 60000
        return "${hours.toInteger()}h ${minutes.round()}m"
    }
}

/**
 * Gets numeric priority value for sorting.
 */
int priorityValue(String priority) {
    switch (priority) {
        case 'HIGH': return 3
        case 'MEDIUM': return 2
        case 'LOW': return 1
        default: return 0
    }
}

logger.info("Pipeline metrics analysis complete.")