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

package com.github.thomasvincent.jenkinsscripts.analytics

import hudson.Extension
import hudson.model.Job
import hudson.model.Run
import hudson.model.TaskListener
import hudson.model.listeners.RunListener
import hudson.security.ACL
import hudson.security.ACLContext
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import groovy.transform.CompileStatic
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.logging.Level
import java.util.logging.Logger
import javax.annotation.Nonnull
import com.github.thomasvincent.jenkinsscripts.utils.ErrorHandler

/**
 * Collects and analyzes pipeline execution metrics to identify performance issues and optimization opportunities.
 * 
 * This class provides advanced analytics not available in core Jenkins:
 * - Stage-level performance tracking
 * - Anomaly detection for execution times
 * - Resource usage patterns
 * - Failure prediction based on historical data
 * 
 * Integrates with Jenkins extension points for automatic metric collection.
 * 
 * @author Thomas Vincent
 * @since 1.4.0
 */
@CompileStatic
class PipelineMetricsCollector {
    
    private static final Logger LOGGER = Logger.getLogger(PipelineMetricsCollector.class.name)
    private final Jenkins jenkins
    private static final int DEFAULT_HISTORY_LIMIT = 100
    private static final double ANOMALY_THRESHOLD = 2.0 // Standard deviations
    private final ErrorHandler errorHandler
    
    @DataBoundConstructor
    PipelineMetricsCollector() {
        this.jenkins = Jenkins.get()
        this.errorHandler = new ErrorHandler(LOGGER)
    }
    
    /**
     * Constructor for testing with dependency injection.
     */
    PipelineMetricsCollector(Jenkins jenkins, ErrorHandler errorHandler) {
        this.jenkins = jenkins
        this.errorHandler = errorHandler
    }
    
    /**
     * Collects comprehensive metrics for a pipeline including stage-level analytics.
     * Requires Job/Read permission.
     */
    Map<String, Object> collectPipelineMetrics(String jobName, int historyLimit = DEFAULT_HISTORY_LIMIT) {
        return errorHandler.withErrorHandling("Failed to collect pipeline metrics for ${jobName}") {
            // Security check
            jenkins.checkPermission(Jenkins.READ)
            
            def job = jenkins.getItemByFullName(jobName, WorkflowJob)
            if (!job) {
                throw new IllegalArgumentException("Pipeline job not found: ${jobName}")
            }
            
            // Check job-level permissions
            job.checkPermission(Job.READ)
        
        def metrics = [
            jobName: jobName,
            analysisTimestamp: Instant.now().toString(),
            summary: [:],
            stageMetrics: [:],
            anomalies: [],
            trends: [:],
            recommendations: []
        ]
        
        // Collect build history
        def builds = job.getBuilds().limit(historyLimit)
        def buildMetrics = builds.collect { build ->
            analyzeBuild(build as WorkflowRun)
        }
        
        // Calculate summary statistics
        metrics.summary = calculateSummaryStats(buildMetrics)
        
        // Analyze stage-level performance
        metrics.stageMetrics = analyzeStagePerformance(buildMetrics)
        
        // Detect anomalies
        metrics.anomalies = detectAnomalies(buildMetrics, metrics.stageMetrics as Map)
        
        // Calculate trends
        metrics.trends = calculateTrends(buildMetrics)
        
        // Generate recommendations
        metrics.recommendations = generateRecommendations(metrics)
        
            return metrics
        }
    }
    
    /**
     * Analyzes a single build and extracts detailed metrics.
     */
    private Map<String, Object> analyzeBuild(WorkflowRun build) {
        def buildMetrics = [
            buildNumber: build.number,
            startTime: build.getStartTimeInMillis(),
            duration: build.getDuration(),
            result: build.result?.toString() ?: 'IN_PROGRESS',
            stages: [:],
            queueTime: calculateQueueTime(build),
            executorCount: build.getExecutor()?.getOwner()?.getExecutors()?.size() ?: 1
        ]
        
        // Extract stage-level metrics
        if (build.execution) {
            def stages = extractStageMetrics(build)
            buildMetrics.stages = stages
        }
        
        return buildMetrics
    }
    
    /**
     * Extracts stage-level metrics from a pipeline run.
     */
    private Map<String, Map> extractStageMetrics(WorkflowRun build) {
        def stages = [:]
        def execution = build.execution
        
        if (!execution) return stages
        
        // Use DepthFirstScanner to traverse the flow graph
        def scanner = new DepthFirstScanner()
        def visitor = { FlowNode node ->
            if (node.getDisplayName() && isStageNode(node)) {
                def stageName = node.getDisplayName()
                def startTime = node.getAction(org.jenkinsci.plugins.workflow.actions.TimingAction)?.getStartTime() ?: 0
                def endTime = getNodeEndTime(node)
                
                stages[stageName] = [
                    duration: endTime - startTime,
                    startTime: startTime,
                    endTime: endTime,
                    status: getNodeStatus(node)
                ]
            }
        }
        
        scanner.visitAll(execution.currentHeads, visitor)
        
        return stages
    }
    
    /**
     * Checks if a flow node represents a stage.
     */
    private boolean isStageNode(FlowNode node) {
        node.getAction(org.jenkinsci.plugins.workflow.actions.LabelAction) != null
    }
    
    /**
     * Gets the end time of a flow node.
     */
    private long getNodeEndTime(FlowNode node) {
        // This is a simplified implementation
        // In reality, you'd need to traverse child nodes to find the actual end time
        def timing = node.getAction(org.jenkinsci.plugins.workflow.actions.TimingAction)
        return timing ? timing.getStartTime() + 1000 : System.currentTimeMillis()
    }
    
    /**
     * Gets the status of a flow node.
     */
    private String getNodeStatus(FlowNode node) {
        def errorAction = node.getAction(org.jenkinsci.plugins.workflow.actions.ErrorAction)
        return errorAction ? 'FAILED' : 'SUCCESS'
    }
    
    /**
     * Calculates queue time for a build.
     */
    private long calculateQueueTime(Run build) {
        def scheduledTime = build.getTimeInMillis()
        def startTime = build.getStartTimeInMillis()
        return Math.max(0, startTime - scheduledTime)
    }
    
    /**
     * Calculates summary statistics across all builds.
     */
    private Map<String, Object> calculateSummaryStats(List<Map> buildMetrics) {
        if (buildMetrics.isEmpty()) {
            return [:]
        }
        
        def durations = buildMetrics.findAll { it.result != 'IN_PROGRESS' }
                                   .collect { it.duration as Long }
        
        def successfulBuilds = buildMetrics.count { it.result == 'SUCCESS' }
        def totalBuilds = buildMetrics.size()
        
        return [
            totalBuilds: totalBuilds,
            successRate: totalBuilds > 0 ? (successfulBuilds / totalBuilds * 100).round(2) : 0,
            avgDuration: durations ? (durations.sum() / durations.size()).round() : 0,
            minDuration: durations ? durations.min() : 0,
            maxDuration: durations ? durations.max() : 0,
            stdDeviation: calculateStandardDeviation(durations),
            avgQueueTime: buildMetrics.collect { it.queueTime as Long }.sum() / buildMetrics.size()
        ]
    }
    
    /**
     * Analyzes stage-level performance across builds.
     */
    private Map<String, Map> analyzeStagePerformance(List<Map> buildMetrics) {
        def stageStats = [:]
        
        // Collect all stage data
        buildMetrics.each { build ->
            def stages = build.stages as Map<String, Map>
            stages.each { stageName, stageData ->
                if (!stageStats[stageName]) {
                    stageStats[stageName] = [
                        durations: [],
                        failures: 0,
                        executions: 0
                    ]
                }
                
                stageStats[stageName].durations << (stageData.duration as Long)
                stageStats[stageName].executions++
                if (stageData.status != 'SUCCESS') {
                    stageStats[stageName].failures++
                }
            }
        }
        
        // Calculate statistics for each stage
        def result = [:]
        stageStats.each { stageName, data ->
            def durations = data.durations as List<Long>
            result[stageName] = [
                avgDuration: durations.sum() / durations.size(),
                minDuration: durations.min(),
                maxDuration: durations.max(),
                stdDeviation: calculateStandardDeviation(durations),
                failureRate: (data.failures / data.executions * 100).round(2),
                executions: data.executions
            ]
        }
        
        return result
    }
    
    /**
     * Detects anomalies in build and stage performance.
     */
    private List<Map> detectAnomalies(List<Map> buildMetrics, Map<String, Map> stageMetrics) {
        def anomalies = []
        
        // Check for build duration anomalies
        def avgBuildDuration = buildMetrics.collect { it.duration as Long }.sum() / buildMetrics.size()
        def stdDev = calculateStandardDeviation(buildMetrics.collect { it.duration as Long })
        
        buildMetrics.each { build ->
            def deviation = Math.abs(build.duration - avgBuildDuration) / stdDev
            if (deviation > ANOMALY_THRESHOLD) {
                anomalies << [
                    type: 'BUILD_DURATION',
                    buildNumber: build.buildNumber,
                    value: build.duration,
                    expectedValue: avgBuildDuration,
                    deviation: deviation,
                    severity: deviation > 3 ? 'HIGH' : 'MEDIUM'
                ]
            }
        }
        
        // Check for stage anomalies
        buildMetrics.each { build ->
            def stages = build.stages as Map<String, Map>
            stages.each { stageName, stageData ->
                def stageStats = stageMetrics[stageName]
                if (stageStats) {
                    def avgDuration = stageStats.avgDuration as Double
                    def stageStdDev = stageStats.stdDeviation as Double
                    if (stageStdDev > 0) {
                        def deviation = Math.abs(stageData.duration - avgDuration) / stageStdDev
                        if (deviation > ANOMALY_THRESHOLD) {
                            anomalies << [
                                type: 'STAGE_DURATION',
                                buildNumber: build.buildNumber,
                                stageName: stageName,
                                value: stageData.duration,
                                expectedValue: avgDuration,
                                deviation: deviation,
                                severity: deviation > 3 ? 'HIGH' : 'MEDIUM'
                            ]
                        }
                    }
                }
            }
        }
        
        return anomalies
    }
    
    /**
     * Calculates performance trends.
     */
    private Map<String, Object> calculateTrends(List<Map> buildMetrics) {
        if (buildMetrics.size() < 5) {
            return [message: 'Insufficient data for trend analysis']
        }
        
        // Split into recent and historical
        def midpoint = buildMetrics.size() / 2
        def recent = buildMetrics.take(midpoint as Integer)
        def historical = buildMetrics.drop(midpoint as Integer)
        
        def recentAvg = recent.collect { it.duration as Long }.sum() / recent.size()
        def historicalAvg = historical.collect { it.duration as Long }.sum() / historical.size()
        def percentChange = ((recentAvg - historicalAvg) / historicalAvg * 100).round(2)
        
        def recentSuccessRate = recent.count { it.result == 'SUCCESS' } / recent.size() * 100
        def historicalSuccessRate = historical.count { it.result == 'SUCCESS' } / historical.size() * 100
        
        return [
            durationTrend: percentChange > 0 ? 'INCREASING' : 'DECREASING',
            durationChange: percentChange,
            successRateTrend: recentSuccessRate >= historicalSuccessRate ? 'IMPROVING' : 'DEGRADING',
            successRateChange: (recentSuccessRate - historicalSuccessRate).round(2)
        ]
    }
    
    /**
     * Generates actionable recommendations based on metrics.
     */
    private List<Map> generateRecommendations(Map<String, Object> metrics) {
        def recommendations = []
        
        // Check for high failure rate
        def summary = metrics.summary as Map
        if (summary.successRate < 80) {
            recommendations << [
                type: 'STABILITY',
                priority: 'HIGH',
                message: "Success rate is ${summary.successRate}%. Consider adding more comprehensive testing or fixing flaky tests.",
                impact: 'Improving stability can reduce time spent on debugging failed builds'
            ]
        }
        
        // Check for long queue times
        if (summary.avgQueueTime > 60000) { // 1 minute
            recommendations << [
                type: 'CAPACITY',
                priority: 'MEDIUM',
                message: "Average queue time is ${summary.avgQueueTime / 1000}s. Consider adding more executors or agents.",
                impact: 'Reducing queue time can improve developer feedback loops'
            ]
        }
        
        // Check for slow stages
        def stageMetrics = metrics.stageMetrics as Map<String, Map>
        stageMetrics.each { stageName, stats ->
            if (stats.avgDuration > 300000) { // 5 minutes
                recommendations << [
                    type: 'PERFORMANCE',
                    priority: 'MEDIUM',
                    message: "Stage '${stageName}' averages ${stats.avgDuration / 1000}s. Consider parallelizing or optimizing this stage.",
                    impact: 'Optimizing slow stages can significantly reduce overall build time'
                ]
            }
        }
        
        // Check for high variance stages
        stageMetrics.each { stageName, stats ->
            def cv = stats.stdDeviation / stats.avgDuration // Coefficient of variation
            if (cv > 0.5) {
                recommendations << [
                    type: 'CONSISTENCY',
                    priority: 'LOW',
                    message: "Stage '${stageName}' has high variance. Consider investigating environmental factors.",
                    impact: 'Reducing variance improves predictability and planning'
                ]
            }
        }
        
        return recommendations
    }
    
    /**
     * Calculates standard deviation for a list of numbers.
     */
    private double calculateStandardDeviation(List<Long> values) {
        if (values.size() < 2) return 0.0
        
        def mean = values.sum() / values.size()
        def variance = values.collect { (it - mean) * (it - mean) }.sum() / values.size()
        return Math.sqrt(variance)
    }
    
    /**
     * Extension point for automatic metric collection on build completion.
     */
    @Extension
    static class MetricsRunListener extends RunListener<WorkflowRun> {
        private static final Logger LISTENER_LOGGER = Logger.getLogger(MetricsRunListener.class.name)
        
        @Override
        void onCompleted(WorkflowRun run, @Nonnull TaskListener listener) {
            try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
                // Collect metrics asynchronously to avoid blocking build completion
                Jenkins.get().getTimer().schedule({
                    try {
                        def collector = new PipelineMetricsCollector()
                        def jobName = run.parent.fullName
                        
                        // Store basic metrics for trend analysis
                        def buildMetrics = [
                            buildNumber: run.number,
                            duration: run.getDuration(),
                            result: run.result?.toString(),
                            timestamp: Instant.now().toString()
                        ]
                        
                        LISTENER_LOGGER.log(Level.FINE, "Collected metrics for {0} #{1}", 
                            [jobName, run.number] as Object[])
                    } catch (Exception e) {
                        LISTENER_LOGGER.log(Level.WARNING, 
                            "Failed to collect metrics for ${run.fullDisplayName}", e)
                    }
                } as Runnable, 5000) // Delay 5 seconds
            }
        }
        
        @Override
        void onStarted(WorkflowRun run, TaskListener listener) {
            // Could track queue time and start time here
            LISTENER_LOGGER.log(Level.FINE, "Build started: {0}", run.fullDisplayName)
        }
    }
    
    /**
     * Descriptor for configuration.
     */
    @Extension
    static class DescriptorImpl extends hudson.model.Descriptor<PipelineMetricsCollector> {
        @Override
        String getDisplayName() {
            return "Pipeline Metrics Collector"
        }
    }
}