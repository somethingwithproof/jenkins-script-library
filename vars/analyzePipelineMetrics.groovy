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

import com.github.thomasvincent.jenkinsscripts.analytics.PipelineMetricsCollector
import com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger
import jenkins.model.Jenkins

/**
 * Pipeline step for analyzing pipeline performance metrics.
 * 
 * This step provides Jenkins Pipeline integration for the PipelineMetricsCollector,
 * making it easy to analyze pipeline performance from within a Pipeline.
 * 
 * Usage in Pipeline:
 * 
 * @Library('jenkins-script-library') _
 * 
 * pipeline {
 *     agent any
 *     stages {
 *         stage('Analyze Performance') {
 *             steps {
 *                 script {
 *                     // Analyze current job
 *                     def metrics = analyzePipelineMetrics()
 *                     echo "Success rate: ${metrics.summary.successRate}%"
 *                     
 *                     // Analyze specific job
 *                     def otherMetrics = analyzePipelineMetrics(
 *                         jobName: 'my-folder/my-pipeline',
 *                         historyLimit: 50
 *                     )
 *                     
 *                     // Get recommendations
 *                     if (metrics.recommendations) {
 *                         metrics.recommendations.each { rec ->
 *                             echo "[${rec.priority}] ${rec.message}"
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 */
def call(Map args = [:]) {
    // Default to current job if not specified
    def jobName = args.jobName ?: env.JOB_NAME
    def historyLimit = args.historyLimit ?: 100
    def includeRecommendations = args.includeRecommendations != false
    
    // Validate inputs
    if (!jobName) {
        error "Pipeline metrics analysis requires a job name"
    }
    
    def jenkins = Jenkins.get()
    def logger = new JenkinsLogger(currentBuild.rawBuild.getListener(), 'analyzePipelineMetrics')
    
    logger.info("Analyzing pipeline metrics for: ${jobName}")
    
    try {
        def collector = new PipelineMetricsCollector(jenkins)
        def metrics = collector.collectPipelineMetrics(jobName, historyLimit)
        
        // Log summary to build console
        logger.info("Analysis complete:")
        logger.info("  Total builds analyzed: ${metrics.summary.totalBuilds}")
        logger.info("  Success rate: ${metrics.summary.successRate}%")
        logger.info("  Average duration: ${formatDuration(metrics.summary.avgDuration)}")
        
        // Log top recommendations if enabled
        if (includeRecommendations && metrics.recommendations) {
            logger.info("Top recommendations:")
            metrics.recommendations.take(3).each { rec ->
                logger.info("  [${rec.priority}] ${rec.message}")
            }
        }
        
        // Add convenience methods to the result
        metrics.metaClass.hasAnomalies = { -> 
            metrics.anomalies && !metrics.anomalies.isEmpty() 
        }
        
        metrics.metaClass.getTopRecommendations = { int count = 3 ->
            metrics.recommendations?.take(count) ?: []
        }
        
        metrics.metaClass.isHealthy = { ->
            metrics.summary.successRate >= 80 && 
            metrics.anomalies.count { it.severity == 'HIGH' } == 0
        }
        
        return metrics
        
    } catch (Exception e) {
        logger.error("Failed to analyze pipeline metrics: ${e.message}")
        throw e
    }
}

/**
 * Overloaded method for simple string parameter.
 */
def call(String jobName) {
    return call(jobName: jobName)
}

/**
 * Formats duration for display.
 */
private String formatDuration(long millis) {
    if (millis < 1000) {
        return "${millis}ms"
    } else if (millis < 60000) {
        return "${(millis / 1000).round(1)}s"
    } else {
        def minutes = millis / 60000
        def seconds = (millis % 60000) / 1000
        return "${minutes.toInteger()}m ${seconds.round()}s"
    }
}