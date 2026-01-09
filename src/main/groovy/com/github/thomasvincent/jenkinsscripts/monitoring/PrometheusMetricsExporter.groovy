/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Prometheus metrics exporter for Jenkins pipeline and agent monitoring.
 * Exposes metrics in OpenMetrics format for Prometheus scraping.
 */
package com.github.thomasvincent.jenkinsscripts.monitoring

import groovy.transform.CompileStatic
import hudson.Extension
import hudson.model.Computer
import hudson.model.Executor
import hudson.model.Node
import hudson.model.Queue
import hudson.model.Run
import jenkins.model.Jenkins

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

/**
 * Exports Jenkins metrics in Prometheus format.
 * Provides comprehensive metrics for pipelines, agents, queues, and system health.
 */
@CompileStatic
class PrometheusMetricsExporter {

    private static final Logger LOGGER = Logger.getLogger(PrometheusMetricsExporter.class.name)

    // Metric counters
    private final AtomicLong buildsTotal = new AtomicLong(0)
    private final AtomicLong buildsSuccessful = new AtomicLong(0)
    private final AtomicLong buildsFailed = new AtomicLong(0)
    private final AtomicLong buildsUnstable = new AtomicLong(0)
    private final AtomicLong buildsAborted = new AtomicLong(0)

    // Build duration tracking
    private final ConcurrentHashMap<String, List<Long>> buildDurations = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, Long> lastBuildTimestamp = new ConcurrentHashMap<>()

    // Stage metrics
    private final ConcurrentHashMap<String, AtomicLong> stageExecutionCounts = new ConcurrentHashMap<>()
    private final ConcurrentHashMap<String, List<Long>> stageDurations = new ConcurrentHashMap<>()

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

        // Track duration per job
        buildDurations.computeIfAbsent(jobName) { new ArrayList<Long>() }
        List<Long> durations = buildDurations.get(jobName)
        synchronized (durations) {
            durations.add(durationMs)
            // Keep only last 100 durations
            if (durations.size() > 100) {
                durations.remove(0)
            }
        }

        lastBuildTimestamp.put(jobName, System.currentTimeMillis())
        LOGGER.fine("Recorded build completion: ${jobName} - ${result} in ${durationMs}ms")
    }

    /**
     * Record a stage execution.
     */
    void recordStageExecution(String jobName, String stageName, long durationMs, boolean success) {
        String key = "${jobName}:${stageName}"

        stageExecutionCounts.computeIfAbsent(key) { new AtomicLong(0) }
        stageExecutionCounts.get(key).incrementAndGet()

        stageDurations.computeIfAbsent(key) { new ArrayList<Long>() }
        List<Long> durations = stageDurations.get(key)
        synchronized (durations) {
            durations.add(durationMs)
            if (durations.size() > 100) {
                durations.remove(0)
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

        // Per-job duration metrics
        buildDurations.each { jobName, durations ->
            if (!durations.isEmpty()) {
                double avg = durations.sum() / durations.size()
                double max = durations.max()
                double min = durations.min()

                sb.append(formatMetricWithLabels('build_duration_seconds_avg', 'gauge',
                        'Average build duration', avg / 1000.0, [job: jobName]))
                sb.append(formatMetricWithLabels('build_duration_seconds_max', 'gauge',
                        'Maximum build duration', max / 1000.0, [job: jobName]))
                sb.append(formatMetricWithLabels('build_duration_seconds_min', 'gauge',
                        'Minimum build duration', min / 1000.0, [job: jobName]))
            }
        }

        // Stage metrics
        stageExecutionCounts.each { key, count ->
            String[] parts = key.split(':')
            if (parts.length == 2) {
                sb.append(formatMetricWithLabels('stage_executions_total', 'counter',
                        'Total stage executions', count.get(), [job: parts[0], stage: parts[1]]))
            }
        }

        return sb.toString()
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

        jenkins.computers.each { Computer computer ->
            totalAgents++

            if (computer.isOnline()) {
                onlineAgents++

                int busyExecutors = computer.executors.count { Executor e -> e.isBusy() }
                if (busyExecutors > 0) {
                    busyAgents++
                } else {
                    idleAgents++
                }

                // Per-agent metrics
                String nodeName = computer.name ?: 'master'
                sb.append(formatMetricWithLabels('agent_executors_total', 'gauge',
                        'Total executors on agent', computer.numExecutors, [agent: nodeName]))
                sb.append(formatMetricWithLabels('agent_executors_busy', 'gauge',
                        'Busy executors on agent', busyExecutors, [agent: nodeName]))
                sb.append(formatMetricWithLabels('agent_executors_idle', 'gauge',
                        'Idle executors on agent', computer.numExecutors - busyExecutors, [agent: nodeName]))

                // Disk space (if available)
                try {
                    def diskSpace = computer.getMonitorData()?.get('hudson.node_monitors.DiskSpaceMonitor')
                    if (diskSpace?.hasProperty('freeSize')) {
                        sb.append(formatMetricWithLabels('agent_disk_free_bytes', 'gauge',
                                'Free disk space on agent', diskSpace.freeSize, [agent: nodeName]))
                    }
                } catch (Exception ignored) {
                    // Monitor data not available
                }
            }
        }

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
        int blocked = items.count { it.isBlocked() }
        int buildable = items.count { it.isBuildable() }

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
     * Format a metric with labels.
     */
    private String formatMetricWithLabels(String name, String type, String help, Number value, Map<String, String> labels) {
        String fullName = "${namespace}_${subsystem}_${name}"
        String labelStr = labels.collect { k, v -> "${k}=\"${escapeLabel(v)}\"" }.join(',')

        return """# HELP ${fullName} ${help}
# TYPE ${fullName} ${type}
${fullName}{${labelStr}} ${value}
"""
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
                stages : stageExecutionCounts.keySet().toList()
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
