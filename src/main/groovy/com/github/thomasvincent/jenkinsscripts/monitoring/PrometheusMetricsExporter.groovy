// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Prometheus metrics exporter for Jenkins pipeline and agent monitoring.
 * Exposes metrics in OpenMetrics format for Prometheus scraping.
 */
package com.github.thomasvincent.jenkinsscripts.monitoring

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import hudson.model.Computer
import hudson.model.Executor
import hudson.model.Queue
import jenkins.model.Jenkins

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

/**
 * Exports Jenkins metrics in Prometheus format.
 * Provides metrics for pipelines, agents, queues, and system health.
 */
@CompileStatic
class PrometheusMetricsExporter {

    private static final Logger LOGGER = Logger.getLogger(PrometheusMetricsExporter.class.name)
    private static final int MAX_DURATION_SAMPLES = 100

    /**
     * Immutable composite key for stage metrics avoiding delimiter collisions.
     */
    @EqualsAndHashCode
    static final class StageKey {
        final String jobName
        final String stageName

        StageKey(String jobName, String stageName) {
            this.jobName = jobName ?: ''
            this.stageName = stageName ?: ''
        }

        @Override
        String toString() {
            return "${jobName}:${stageName}"
        }
    }

    // Metric counters
    private final AtomicLong buildsTotal = new AtomicLong(0)
    private final AtomicLong buildsSuccessful = new AtomicLong(0)
    private final AtomicLong buildsFailed = new AtomicLong(0)
    private final AtomicLong buildsUnstable = new AtomicLong(0)
    private final AtomicLong buildsAborted = new AtomicLong(0)

    // Build duration tracking (bounded deques per job)
    private final ConcurrentHashMap<String, Deque<Long>> buildDurations = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, Long> lastBuildTimestamp = new ConcurrentHashMap<>()

    // Stage metrics keyed by StageKey(jobName, stageName)
    private final ConcurrentHashMap<StageKey, AtomicLong> stageExecutionCounts = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<StageKey, Deque<Long>> stageDurations = new ConcurrentHashMap<>()

    // Configuration
    private final String namespace
    private final String subsystem
    private final int histogramBuckets

    PrometheusMetricsExporter(String namespace = 'jenkins', String subsystem = 'pipeline', int histogramBuckets = 10) {
        this.namespace = namespace
        this.subsystem = subsystem
        this.histogramBuckets = histogramBuckets
    }

    /**
     * Record a build completion event.
     */
    void recordBuildCompletion(String jobName, String result, long durationMs) {
        buildsTotal.incrementAndGet()

        switch (result?.toUpperCase()) {
            case 'SUCCESS':
                buildsSuccessful.incrementAndGet()
                break
            case 'FAILURE':
                buildsFailed.incrementAndGet()
                break
            case 'UNSTABLE':
                buildsUnstable.incrementAndGet()
                break
            case 'ABORTED':
                buildsAborted.incrementAndGet()
                break
        }

        Deque<Long> durations = buildDurations.computeIfAbsent(jobName) { new ArrayDeque<Long>(MAX_DURATION_SAMPLES) }
        synchronized (durations) {
            durations.addLast(durationMs)
            while (durations.size() > MAX_DURATION_SAMPLES) {
                durations.pollFirst()
            }
        }

        lastBuildTimestamp.put(jobName, System.currentTimeMillis())
        LOGGER.fine("Recorded build completion: ${jobName} - ${result} in ${durationMs}ms")
    }

    /**
     * Record a stage execution.
     */
    void recordStageExecution(String jobName, String stageName, long durationMs, boolean success) {
        StageKey key = new StageKey(jobName, stageName)

        stageExecutionCounts.computeIfAbsent(key) { new AtomicLong(0) }.incrementAndGet()

        Deque<Long> durations = stageDurations.computeIfAbsent(key) { new ArrayDeque<Long>(MAX_DURATION_SAMPLES) }
        synchronized (durations) {
            durations.addLast(durationMs)
            while (durations.size() > MAX_DURATION_SAMPLES) {
                durations.pollFirst()
            }
        }
    }

    /**
     * Generate metrics in Prometheus/OpenMetrics format.
     */
    String exportMetrics() {
        StringBuilder sb = new StringBuilder()

        // Build metrics
        sb.append(formatMetric('builds_total', 'counter', 'Total number of builds', buildsTotal.get()))
        sb.append(formatMetric('builds_successful_total', 'counter', 'Total successful builds', buildsSuccessful.get()))
        sb.append(formatMetric('builds_failed_total', 'counter', 'Total failed builds', buildsFailed.get()))
        sb.append(formatMetric('builds_unstable_total', 'counter', 'Total unstable builds', buildsUnstable.get()))
        sb.append(formatMetric('builds_aborted_total', 'counter', 'Total aborted builds', buildsAborted.get()))

        // Agent metrics
        sb.append(exportAgentMetrics())

        // Queue metrics
        sb.append(exportQueueMetrics())

        // JVM metrics
        sb.append(exportJvmMetrics())

        // Per-job duration metrics (single synchronized snapshot and single-pass stats)
        List<Map<String, Object>> buildAvgSamples = []
        List<Map<String, Object>> buildMaxSamples = []
        List<Map<String, Object>> buildMinSamples = []

        buildDurations.each { String jobName, Deque<Long> durations ->
            double[] stats = computeDurationStats(durations)
            if (stats != null) {
                Map<String, String> labels = [job: jobName]
                buildAvgSamples.add([labels: labels, value: stats[0] / 1000.0d] as Map<String, Object>)
                buildMaxSamples.add([labels: labels, value: stats[1] / 1000.0d] as Map<String, Object>)
                buildMinSamples.add([labels: labels, value: stats[2] / 1000.0d] as Map<String, Object>)
            }
        }

        appendMetricFamily(sb, 'build_duration_seconds_avg', 'gauge', 'Average build duration', buildAvgSamples)
        appendMetricFamily(sb, 'build_duration_seconds_max', 'gauge', 'Maximum build duration', buildMaxSamples)
        appendMetricFamily(sb, 'build_duration_seconds_min', 'gauge', 'Minimum build duration', buildMinSamples)

        // Stage execution count metrics
        List<Map<String, Object>> stageCountSamples = []
        stageExecutionCounts.each { StageKey key, AtomicLong count ->
            stageCountSamples.add([
                labels: [job: key.jobName, stage: key.stageName],
                value: count.get()
            ] as Map<String, Object>)
        }
        appendMetricFamily(sb, 'stage_executions_total', 'counter', 'Total stage executions', stageCountSamples)

        // Stage duration metrics
        List<Map<String, Object>> stageAvgSamples = []
        List<Map<String, Object>> stageMaxSamples = []
        List<Map<String, Object>> stageMinSamples = []
        stageDurations.each { StageKey key, Deque<Long> durations ->
            double[] stats = computeDurationStats(durations)
            if (stats != null) {
                Map<String, String> labels = [job: key.jobName, stage: key.stageName]
                stageAvgSamples.add([labels: labels, value: stats[0] / 1000.0d] as Map<String, Object>)
                stageMaxSamples.add([labels: labels, value: stats[1] / 1000.0d] as Map<String, Object>)
                stageMinSamples.add([labels: labels, value: stats[2] / 1000.0d] as Map<String, Object>)
            }
        }
        appendMetricFamily(sb, 'stage_duration_seconds_avg', 'gauge', 'Average stage duration', stageAvgSamples)
        appendMetricFamily(sb, 'stage_duration_seconds_max', 'gauge', 'Maximum stage duration', stageMaxSamples)
        appendMetricFamily(sb, 'stage_duration_seconds_min', 'gauge', 'Minimum stage duration', stageMinSamples)

        return sb.toString()
    }

    /**
     * Snapshots a duration deque under its lock and computes [avg, max, min] in one pass.
     */
    private static double[] computeDurationStats(Deque<Long> durations) {
        List<Long> snapshot
        synchronized (durations) {
            if (durations.isEmpty()) {
                return null
            }
            snapshot = new ArrayList<>(durations)
        }

        long sum = 0L
        long min = Long.MAX_VALUE
        long max = Long.MIN_VALUE
        for (Long d : snapshot) {
            long val = d.longValue()
            sum += val
            if (val < min) {
                min = val
            }
            if (val > max) {
                max = val
            }
        }
        double avg = (double) sum / snapshot.size()
        return [avg, (double) max, (double) min] as double[]
    }

    /**
     * Export agent/node metrics.
     */
    private String exportAgentMetrics() {
        StringBuilder sb = new StringBuilder()

        Jenkins jenkins = Jenkins.getInstanceOrNull()
        if (jenkins == null) {
            return sb.toString()
        }

        int totalAgents = 0
        int onlineAgents = 0
        int busyAgents = 0
        int idleAgents = 0

        List<Map<String, Object>> totalExecSamples = []
        List<Map<String, Object>> busyExecSamples = []
        List<Map<String, Object>> idleExecSamples = []
        List<Map<String, Object>> diskFreeSamples = []

        jenkins.computers.each { Computer computer ->
            totalAgents++

            if (computer.isOnline()) {
                onlineAgents++

                int busyExecutors = computer.executors.count { Executor e -> e.isBusy() }.intValue()
                if (busyExecutors > 0) {
                    busyAgents++
                } else {
                    idleAgents++
                }

                String nodeName = computer.name ?: 'master'
                Map<String, String> labels = [agent: nodeName]
                totalExecSamples.add([labels: labels, value: computer.numExecutors] as Map<String, Object>)
                busyExecSamples.add([labels: labels, value: busyExecutors] as Map<String, Object>)
                idleExecSamples.add([labels: labels, value: computer.numExecutors - busyExecutors] as Map<String, Object>)

                // Disk space (if available)
                try {
                    def diskSpace = computer.getMonitorData()?.get('hudson.node_monitors.DiskSpaceMonitor')
                    if (diskSpace?.hasProperty('freeSize')) {
                        Object freeSize = org.codehaus.groovy.runtime.InvokerHelper.getProperty(diskSpace, 'freeSize')
                        diskFreeSamples.add([labels: labels, value: freeSize] as Map<String, Object>)
                    }
                } catch (Exception ignored) {
                    // Monitor data not available
                }
            }
        }

        appendMetricFamily(sb, 'agent_executors_total', 'gauge', 'Total executors on agent', totalExecSamples)
        appendMetricFamily(sb, 'agent_executors_busy', 'gauge', 'Busy executors on agent', busyExecSamples)
        appendMetricFamily(sb, 'agent_executors_idle', 'gauge', 'Idle executors on agent', idleExecSamples)
        appendMetricFamily(sb, 'agent_disk_free_bytes', 'gauge', 'Free disk space on agent', diskFreeSamples)

        sb.append(formatMetric('agents_total', 'gauge', 'Total number of agents', totalAgents))
        sb.append(formatMetric('agents_online', 'gauge', 'Online agents', onlineAgents))
        sb.append(formatMetric('agents_offline', 'gauge', 'Offline agents', totalAgents - onlineAgents))
        sb.append(formatMetric('agents_busy', 'gauge', 'Busy agents', busyAgents))
        sb.append(formatMetric('agents_idle', 'gauge', 'Idle agents', idleAgents))

        return sb.toString()
    }

    /**
     * Export queue metrics.
     */
    private String exportQueueMetrics() {
        StringBuilder sb = new StringBuilder()

        Jenkins jenkins = Jenkins.getInstanceOrNull()
        if (jenkins == null) {
            return sb.toString()
        }

        Queue queue = jenkins.queue
        Queue.Item[] items = queue.items

        sb.append(formatMetric('queue_size', 'gauge', 'Current queue size', items.length))

        // Queue wait time analysis
        long now = System.currentTimeMillis()
        long totalWaitTime = 0
        long maxWaitTime = 0

        items.each { Queue.Item item ->
            long waitTime = now - item.inQueueSince
            totalWaitTime += waitTime
            maxWaitTime = Math.max(maxWaitTime, waitTime)
        }

        if (items.length > 0) {
            sb.append(formatMetric('queue_avg_wait_seconds', 'gauge',
                    'Average queue wait time', (totalWaitTime / items.length) / 1000.0))
            sb.append(formatMetric('queue_max_wait_seconds', 'gauge',
                    'Maximum queue wait time', maxWaitTime / 1000.0))
        }

        // Blocked vs buildable
        int blocked = items.count { it.isBlocked() }.intValue()
        int buildable = items.count { it.isBuildable() }.intValue()

        sb.append(formatMetric('queue_blocked', 'gauge', 'Blocked items in queue', blocked))
        sb.append(formatMetric('queue_buildable', 'gauge', 'Buildable items in queue', buildable))

        return sb.toString()
    }

    /**
     * Export JVM metrics.
     */
    private String exportJvmMetrics() {
        StringBuilder sb = new StringBuilder()

        Runtime runtime = Runtime.getRuntime()

        sb.append(formatMetric('jvm_memory_used_bytes', 'gauge',
                'JVM used memory', runtime.totalMemory() - runtime.freeMemory()))
        sb.append(formatMetric('jvm_memory_free_bytes', 'gauge',
                'JVM free memory', runtime.freeMemory()))
        sb.append(formatMetric('jvm_memory_total_bytes', 'gauge',
                'JVM total memory', runtime.totalMemory()))
        sb.append(formatMetric('jvm_memory_max_bytes', 'gauge',
                'JVM max memory', runtime.maxMemory()))

        sb.append(formatMetric('jvm_processors', 'gauge',
                'Available processors', runtime.availableProcessors()))

        // Thread metrics
        sb.append(formatMetric('jvm_threads_total', 'gauge',
                'Total JVM threads', Thread.activeCount()))

        return sb.toString()
    }

    /**
     * Format a simple metric.
     */
    private String formatMetric(String name, String type, String help, Number value) {
        String fullName = "${namespace}_${subsystem}_${name}"
        return """# HELP ${fullName} ${help}
# TYPE ${fullName} ${type}
${fullName} ${value}
"""
    }

    /**
     * Appends a metric family with a single # HELP and # TYPE header followed by all labeled samples.
     */
    private void appendMetricFamily(StringBuilder sb, String name, String type, String help,
                                    List<Map<String, Object>> samples) {
        if (samples.isEmpty()) {
            return
        }
        String fullName = "${namespace}_${subsystem}_${name}"
        sb.append("# HELP ${fullName} ${help}\n")
        sb.append("# TYPE ${fullName} ${type}\n")
        for (Map<String, Object> sample : samples) {
            Map<String, String> labels = (Map<String, String>) sample.get('labels')
            Object value = sample.get('value')
            String labelStr = labels.collect { String k, String v -> "${k}=\"${escapeLabel(v)}\"" }.join(',')
            sb.append("${fullName}{${labelStr}} ${value}\n")
        }
    }

    /**
     * Escape label values for Prometheus format.
     */
    private static String escapeLabel(String value) {
        return value?.replace('\\', '\\\\')
                ?.replace('"', '\\"')
                ?.replace('\n', '\\n') ?: ''
    }

    /**
     * Get a summary of current metrics state.
     */
    Map<String, Object> getMetricsSummary() {
        return [
                builds: [
                        total     : buildsTotal.get(),
                        successful: buildsSuccessful.get(),
                        failed    : buildsFailed.get(),
                        unstable  : buildsUnstable.get(),
                        aborted   : buildsAborted.get()
                ],
                jobs   : buildDurations.keySet().toList(),
                stages : stageExecutionCounts.keySet().collect { StageKey k -> k.toString() }
        ]
    }

    /**
     * Reset all metrics (useful for testing).
     */
    void resetMetrics() {
        buildsTotal.set(0)
        buildsSuccessful.set(0)
        buildsFailed.set(0)
        buildsUnstable.set(0)
        buildsAborted.set(0)
        buildDurations.clear()
        lastBuildTimestamp.clear()
        stageExecutionCounts.clear()
        stageDurations.clear()
        LOGGER.info("All metrics reset")
    }
}
