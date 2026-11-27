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

package com.github.thomasvincent.jenkinsscripts.analytics

import edu.umd.cs.findbugs.annotations.NonNull
import hudson.Extension
import hudson.model.AbstractDescribableImpl
import hudson.model.Job
import hudson.model.Run
import hudson.model.TaskListener
import hudson.model.listeners.RunListener
import hudson.security.ACL
import hudson.security.ACLContext
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.actions.ErrorAction
import org.jenkinsci.plugins.workflow.actions.LabelAction
import org.jenkinsci.plugins.workflow.actions.TimingAction
import org.jenkinsci.plugins.workflow.graph.BlockEndNode
import org.jenkinsci.plugins.workflow.graph.BlockStartNode
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.kohsuke.stapler.DataBoundConstructor
import groovy.transform.CompileStatic
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
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
class PipelineMetricsCollector extends AbstractDescribableImpl<PipelineMetricsCollector> {

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
     * Constructor using a provided Jenkins instance and default ErrorHandler.
     */
    PipelineMetricsCollector(Jenkins jenkins) {
        this(jenkins, new ErrorHandler(LOGGER))
    }

    /**
     * Constructor for testing with dependency injection.
     */
    PipelineMetricsCollector(Jenkins jenkins, ErrorHandler errorHandler) {
        this.jenkins = jenkins
        this.errorHandler = errorHandler
    }

    /**
     * Collects metrics for a pipeline including stage-level analytics.
     * Requires Job/Read permission.
     */
    Map<String, Object> collectPipelineMetrics(String jobName, int historyLimit = DEFAULT_HISTORY_LIMIT) {
        return errorHandler.withErrorHandling("Failed to collect pipeline metrics for ${jobName}") {
            // Security check
            jenkins.checkPermission(Jenkins.READ)

            WorkflowJob job = jenkins.getItemByFullName(jobName, WorkflowJob)
            if (!job) {
                throw new IllegalArgumentException("Pipeline job not found: ${jobName}")
            }

            // Check job-level permissions
            job.checkPermission(Job.READ)

            Map<String, Object> metrics = [
                jobName: jobName,
                analysisTimestamp: Instant.now().toString(),
                summary: [:],
                stageMetrics: [:],
                anomalies: [],
                trends: [:],
                recommendations: []
            ] as Map<String, Object>

            // Collect build history
            List<WorkflowRun> builds = job.getBuilds().limit(historyLimit)
            List<Map<String, Object>> buildMetrics = builds.collect { WorkflowRun build ->
                analyzeBuild(build)
            }

            // Calculate summary statistics
            metrics.summary = calculateSummaryStats(buildMetrics)

            // Analyze stage-level performance
            metrics.stageMetrics = analyzeStagePerformance(buildMetrics)

            // Detect anomalies
            metrics.anomalies = detectAnomalies(buildMetrics, (Map<String, Map<String, Object>>) metrics.stageMetrics)

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
        Map<String, Object> buildMetrics = [
            buildNumber: build.number,
            startTime: build.getStartTimeInMillis(),
            duration: build.getDuration(),
            result: build.result?.toString() ?: 'IN_PROGRESS',
            stages: [:],
            queueTime: calculateQueueTime(build),
            executorCount: build.getExecutor()?.getOwner()?.getExecutors()?.size() ?: 1
        ] as Map<String, Object>

        // Extract stage-level metrics
        if (build.execution) {
            buildMetrics.stages = extractStageMetrics(build)
        }

        return buildMetrics
    }

    /**
     * Extracts stage-level metrics from a pipeline run by matching BlockStartNode to BlockEndNode.
     */
    private Map<String, Map<String, Object>> extractStageMetrics(WorkflowRun build) {
        Map<String, Map<String, Object>> stages = [:]
        def execution = build.execution

        if (!execution) {
            return stages
        }

        Map<String, BlockEndNode> endNodesByStartId = [:]
        List<FlowNode> stageNodes = []
        FlowGraphWalker walker = new FlowGraphWalker(execution)
        for (FlowNode node : walker) {
            if (node instanceof BlockEndNode) {
                BlockEndNode endNode = (BlockEndNode) node
                BlockStartNode startNode = endNode.getStartNode()
                if (startNode != null) {
                    endNodesByStartId[startNode.id] = endNode
                }
            }
            if (node.getDisplayName() && isStageNode(node)) {
                stageNodes.add(node)
            }
        }

        // Sort stage nodes chronologically for fallback end-time computation
        stageNodes.sort { FlowNode n -> n.getAction(TimingAction)?.getStartTime() ?: 0L }

        long buildEndTime = build.result != null
                ? (build.getStartTimeInMillis() + build.getDuration())
                : System.currentTimeMillis()

        for (int i = 0; i < stageNodes.size(); i++) {
            FlowNode node = stageNodes[i]
            String stageName = node.getDisplayName()
            long startTime = node.getAction(TimingAction)?.getStartTime() ?: 0L
            BlockEndNode matchingEnd = endNodesByStartId[node.id]
            long endTime = resolveNodeEndTime(node, matchingEnd, stageNodes, i, startTime, buildEndTime)
            long duration = Math.max(0L, endTime - startTime)

            stages[stageName] = [
                duration: duration,
                startTime: startTime,
                endTime: endTime,
                status: getNodeStatus(node, matchingEnd)
            ] as Map<String, Object>
        }

        return stages
    }

    /**
     * Checks if a flow node represents a stage.
     */
    private boolean isStageNode(FlowNode node) {
        return node.getAction(LabelAction) != null
    }

    /**
     * Resolves the actual end time of a stage FlowNode using its matching BlockEndNode,
     * falling back to the next stage start or build end time.
     */
    private long resolveNodeEndTime(FlowNode node, BlockEndNode matchingEnd, List<FlowNode> stageNodes,
                                    int index, long startTime, long buildEndTime) {
        TimingAction endTiming = matchingEnd?.getAction(TimingAction)
        if (endTiming != null) {
            return endTiming.getStartTime()
        }
        if (index + 1 < stageNodes.size()) {
            TimingAction nextTiming = stageNodes[index + 1].getAction(TimingAction)
            if (nextTiming != null) {
                return nextTiming.getStartTime()
            }
        }
        return startTime > 0L ? Math.max(startTime, buildEndTime) : startTime
    }

    /**
     * Gets the status of a flow node.
     */
    private String getNodeStatus(FlowNode node, BlockEndNode endNode = null) {
        ErrorAction errorAction = node.getAction(ErrorAction) ?: endNode?.getAction(ErrorAction)
        return errorAction ? 'FAILED' : 'SUCCESS'
    }

    /**
     * Calculates queue time for a build.
     */
    private long calculateQueueTime(Run build) {
        long scheduledTime = build.getTimeInMillis()
        long startTime = build.getStartTimeInMillis()
        return Math.max(0L, startTime - scheduledTime)
    }

    /**
     * Filters out builds that are still in progress.
     */
    private List<Map<String, Object>> filterCompletedBuilds(List<Map<String, Object>> buildMetrics) {
        return buildMetrics.findAll { Map<String, Object> b -> b.result != 'IN_PROGRESS' }
    }

    /**
     * Calculates summary statistics across completed builds.
     */
    private Map<String, Object> calculateSummaryStats(List<Map<String, Object>> buildMetrics) {
        List<Map<String, Object>> completed = filterCompletedBuilds(buildMetrics)
        if (completed.isEmpty()) {
            return [
                totalBuilds: 0,
                successRate: 0.0d,
                avgDuration: 0L,
                minDuration: 0L,
                maxDuration: 0L,
                stdDeviation: 0.0d,
                avgQueueTime: 0.0d
            ] as Map<String, Object>
        }

        List<Long> durations = completed.collect { Map<String, Object> b -> ((Number) b.duration).longValue() }
        int successfulBuilds = completed.count { Map<String, Object> b -> b.result == 'SUCCESS' }.intValue()
        int totalBuilds = completed.size()

        long durationSum = 0L
        long minDuration = Long.MAX_VALUE
        long maxDuration = Long.MIN_VALUE
        for (Long d : durations) {
            long val = d.longValue()
            durationSum += val
            if (val < minDuration) minDuration = val
            if (val > maxDuration) maxDuration = val
        }

        long queueSum = 0L
        for (Map<String, Object> b : completed) {
            queueSum += ((Number) b.queueTime).longValue()
        }

        return [
            totalBuilds: totalBuilds,
            successRate: Math.round((successfulBuilds * 100.0d / totalBuilds) * 100.0d) / 100.0d,
            avgDuration: Math.round((double) durationSum / durations.size()),
            minDuration: minDuration,
            maxDuration: maxDuration,
            stdDeviation: calculateStandardDeviation(durations),
            avgQueueTime: (double) queueSum / totalBuilds
        ] as Map<String, Object>
    }

    /**
     * Analyzes stage-level performance across completed builds.
     */
    private Map<String, Map<String, Object>> analyzeStagePerformance(List<Map<String, Object>> buildMetrics) {
        List<Map<String, Object>> completed = filterCompletedBuilds(buildMetrics)
        Map<String, List<Long>> stageDurationsMap = [:]
        Map<String, Integer> stageFailuresMap = [:]
        Map<String, Integer> stageExecutionsMap = [:]

        completed.each { Map<String, Object> build ->
            Map<String, Map<String, Object>> stages = (Map<String, Map<String, Object>>) build.stages
            stages?.each { String stageName, Map<String, Object> stageData ->
                List<Long> durs = stageDurationsMap.computeIfAbsent(stageName) { new ArrayList<Long>() }
                durs.add(((Number) stageData.duration).longValue())
                stageExecutionsMap[stageName] = (stageExecutionsMap[stageName] ?: 0) + 1
                if (stageData.status != 'SUCCESS') {
                    stageFailuresMap[stageName] = (stageFailuresMap[stageName] ?: 0) + 1
                }
            }
        }

        Map<String, Map<String, Object>> result = [:]
        stageDurationsMap.each { String stageName, List<Long> durations ->
            if (durations.isEmpty()) {
                return
            }
            long sum = 0L
            long min = Long.MAX_VALUE
            long max = Long.MIN_VALUE
            for (Long d : durations) {
                long val = d.longValue()
                sum += val
                if (val < min) min = val
                if (val > max) max = val
            }
            int executions = stageExecutionsMap[stageName] ?: durations.size()
            int failures = stageFailuresMap[stageName] ?: 0
            result[stageName] = [
                avgDuration: (double) sum / durations.size(),
                minDuration: min,
                maxDuration: max,
                stdDeviation: calculateStandardDeviation(durations),
                failureRate: executions > 0 ? Math.round((failures * 100.0d / executions) * 100.0d) / 100.0d : 0.0d,
                executions: executions
            ] as Map<String, Object>
        }

        return result
    }

    /**
     * Detects anomalies in build and stage performance across completed builds.
     */
    private List<Map<String, Object>> detectAnomalies(List<Map<String, Object>> buildMetrics,
                                                      Map<String, Map<String, Object>> stageMetrics) {
        List<Map<String, Object>> anomalies = []
        List<Map<String, Object>> completed = filterCompletedBuilds(buildMetrics)
        if (completed.isEmpty()) {
            return anomalies
        }

        List<Long> buildDurations = completed.collect { Map<String, Object> b -> ((Number) b.duration).longValue() }
        long sum = 0L
        for (Long d : buildDurations) {
            sum += d.longValue()
        }
        double avgBuildDuration = (double) sum / completed.size()
        double stdDev = calculateStandardDeviation(buildDurations)

        if (stdDev > 0.0d) {
            completed.each { Map<String, Object> build ->
                long duration = ((Number) build.duration).longValue()
                double deviation = Math.abs(duration - avgBuildDuration) / stdDev
                if (deviation > ANOMALY_THRESHOLD) {
                    anomalies.add([
                        type: 'BUILD_DURATION',
                        buildNumber: build.buildNumber,
                        value: duration,
                        expectedValue: avgBuildDuration,
                        deviation: deviation,
                        severity: deviation > 3.0d ? 'HIGH' : 'MEDIUM'
                    ] as Map<String, Object>)
                }
            }
        }

        // Check for stage anomalies
        completed.each { Map<String, Object> build ->
            Map<String, Map<String, Object>> stages = (Map<String, Map<String, Object>>) build.stages
            stages?.each { String stageName, Map<String, Object> stageData ->
                Map<String, Object> stageStats = stageMetrics[stageName]
                if (stageStats) {
                    double avgDuration = ((Number) stageStats.avgDuration).doubleValue()
                    double stageStdDev = ((Number) stageStats.stdDeviation).doubleValue()
                    if (stageStdDev > 0.0d) {
                        long stageDuration = ((Number) stageData.duration).longValue()
                        double deviation = Math.abs(stageDuration - avgDuration) / stageStdDev
                        if (deviation > ANOMALY_THRESHOLD) {
                            anomalies.add([
                                type: 'STAGE_DURATION',
                                buildNumber: build.buildNumber,
                                stageName: stageName,
                                value: stageDuration,
                                expectedValue: avgDuration,
                                deviation: deviation,
                                severity: deviation > 3.0d ? 'HIGH' : 'MEDIUM'
                            ] as Map<String, Object>)
                        }
                    }
                }
            }
        }

        return anomalies
    }

    /**
     * Calculates performance trends across completed builds.
     */
    private Map<String, Object> calculateTrends(List<Map<String, Object>> buildMetrics) {
        List<Map<String, Object>> completed = filterCompletedBuilds(buildMetrics)
        if (completed.size() < 5) {
            return [message: 'Insufficient data for trend analysis'] as Map<String, Object>
        }

        int midpoint = completed.size().intdiv(2).intValue()
        List<Map<String, Object>> recent = completed.subList(0, midpoint)
        List<Map<String, Object>> historical = completed.subList(midpoint, completed.size())

        long recentSum = 0L
        for (Map<String, Object> b : recent) {
            recentSum += ((Number) b.duration).longValue()
        }
        long historicalSum = 0L
        for (Map<String, Object> b : historical) {
            historicalSum += ((Number) b.duration).longValue()
        }

        double recentAvg = (double) recentSum / recent.size()
        double historicalAvg = (double) historicalSum / historical.size()
        double percentChange = historicalAvg > 0.0d
                ? Math.round(((recentAvg - historicalAvg) / historicalAvg * 100.0d) * 100.0d) / 100.0d
                : 0.0d

        double recentSuccessRate = recent.count { Map<String, Object> b -> b.result == 'SUCCESS' } * 100.0d / recent.size()
        double historicalSuccessRate = historical.count { Map<String, Object> b -> b.result == 'SUCCESS' } * 100.0d / historical.size()

        return [
            durationTrend: percentChange > 0.0d ? 'INCREASING' : 'DECREASING',
            durationChange: percentChange,
            successRateTrend: recentSuccessRate >= historicalSuccessRate ? 'IMPROVING' : 'DEGRADING',
            successRateChange: Math.round((recentSuccessRate - historicalSuccessRate) * 100.0d) / 100.0d
        ] as Map<String, Object>
    }

    /**
     * Generates actionable recommendations based on metrics.
     */
    private List<Map<String, Object>> generateRecommendations(Map<String, Object> metrics) {
        List<Map<String, Object>> recommendations = []

        Map<String, Object> summary = (Map<String, Object>) metrics.summary
        if (summary && ((Number) (summary.totalBuilds ?: 0)).intValue() > 0) {
            double successRate = ((Number) (summary.successRate ?: 0.0d)).doubleValue()
            if (successRate < 80.0d) {
                recommendations.add([
                    type: 'STABILITY',
                    priority: 'HIGH',
                    message: "Success rate is ${successRate}%. Consider adding more tests or fixing flaky tests.",
                    impact: 'Improving stability can reduce time spent on debugging failed builds'
                ] as Map<String, Object>)
            }

            double avgQueueTime = ((Number) (summary.avgQueueTime ?: 0.0d)).doubleValue()
            if (avgQueueTime > 60000.0d) {
                recommendations.add([
                    type: 'CAPACITY',
                    priority: 'MEDIUM',
                    message: "Average queue time is ${avgQueueTime / 1000.0d}s. Consider adding more executors or agents.",
                    impact: 'Reducing queue time can improve developer feedback loops'
                ] as Map<String, Object>)
            }
        }

        Map<String, Map<String, Object>> stageMetrics = (Map<String, Map<String, Object>>) metrics.stageMetrics
        stageMetrics?.each { String stageName, Map<String, Object> stats ->
            double avgDuration = ((Number) (stats.avgDuration ?: 0.0d)).doubleValue()
            if (avgDuration > 300000.0d) {
                recommendations.add([
                    type: 'PERFORMANCE',
                    priority: 'MEDIUM',
                    message: "Stage '${stageName}' averages ${avgDuration / 1000.0d}s. Consider parallelizing or optimizing this stage.",
                    impact: 'Optimizing slow stages can significantly reduce overall build time'
                ] as Map<String, Object>)
            }

            if (avgDuration > 0.0d) {
                double stdDev = ((Number) (stats.stdDeviation ?: 0.0d)).doubleValue()
                double cv = stdDev / avgDuration
                if (cv > 0.5d) {
                    recommendations.add([
                        type: 'CONSISTENCY',
                        priority: 'LOW',
                        message: "Stage '${stageName}' has high variance. Consider investigating environmental factors.",
                        impact: 'Reducing variance improves predictability and planning'
                    ] as Map<String, Object>)
                }
            }
        }

        return recommendations
    }

    /**
     * Calculates standard deviation for a list of numbers.
     */
    private double calculateStandardDeviation(List<Long> values) {
        if (values == null || values.size() < 2) {
            return 0.0d
        }

        double sum = 0.0d
        for (Long v : values) {
            sum += v.doubleValue()
        }
        double mean = sum / values.size()
        double sqDiffSum = 0.0d
        for (Long v : values) {
            double diff = v.doubleValue() - mean
            sqDiffSum += diff * diff
        }
        return Math.sqrt(sqDiffSum / values.size())
    }

    /**
     * Extension point for automatic metric collection on build completion.
     */
    @Extension
    static class MetricsRunListener extends RunListener<WorkflowRun> {
        private static final Logger LISTENER_LOGGER = Logger.getLogger(MetricsRunListener.class.name)

        @Override
        void onCompleted(WorkflowRun run, @NonNull TaskListener listener) {
            String jobName = ((Job) run.parent).fullName
            int buildNumber = run.number
            long duration = run.getDuration()
            String result = run.result?.toString()
            String displayName = run.fullDisplayName

            jenkins.util.Timer.get().schedule({
                try (ACLContext ctx = ACL.as(ACL.SYSTEM)) {
                    LISTENER_LOGGER.log(Level.FINE, "Collected metrics for {0} #{1} (duration={2}ms, result={3})",
                        [jobName, buildNumber, duration, result] as Object[])
                } catch (Exception e) {
                    LISTENER_LOGGER.log(Level.WARNING,
                        "Failed to collect metrics for ${displayName}", e)
                }
            } as Runnable, 5L, TimeUnit.SECONDS)
        }

        @Override
        void onStarted(WorkflowRun run, TaskListener listener) {
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
