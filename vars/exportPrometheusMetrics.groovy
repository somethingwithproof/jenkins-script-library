// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for exporting Prometheus metrics.
 */

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.monitoring.PrometheusMetricsExporter
import java.util.concurrent.ConcurrentHashMap

/**
 * Export Jenkins metrics in Prometheus format.
 *
 * Usage:
 *   // Get all metrics as string
 *   def metrics = exportPrometheusMetrics()
 *
 *   // Record a build completion
 *   exportPrometheusMetrics.recordBuild('my-job', 'SUCCESS', 12345)
 *
 *   // Get metrics summary
 *   def summary = exportPrometheusMetrics.summary()
 */

@groovy.transform.Field
private static final PrometheusMetricsExporter DEFAULT_EXPORTER = new PrometheusMetricsExporter()

@groovy.transform.Field
private static final ConcurrentHashMap<String, PrometheusMetricsExporter> SCOPED_EXPORTERS = new ConcurrentHashMap<>()

def call() {
    return exportWithNonCps(DEFAULT_EXPORTER)
}

def call(Map params) {
    if (!params || (!params.namespace && !params.subsystem && !params.histogramBuckets)) {
        return exportWithNonCps(DEFAULT_EXPORTER)
    }
    PrometheusMetricsExporter target = resolveExporter(
        (params.namespace ?: 'jenkins') as String,
        (params.subsystem ?: 'pipeline') as String,
        (params.histogramBuckets ?: 10) as int
    )
    return exportWithNonCps(target)
}

/**
 * Record a build completion event.
 */
def recordBuild(String jobName, String result, long durationMs) {
    recordBuildNonCps(jobName, result, durationMs)
}

/**
 * Record a stage execution.
 */
def recordStage(String jobName, String stageName, long durationMs, boolean success = true) {
    recordStageNonCps(jobName, stageName, durationMs, success)
}

/**
 * Get metrics summary as a map.
 */
def summary() {
    return summaryNonCps()
}

/**
 * Reset all metrics.
 */
def reset() {
    resetNonCps()
}

/**
 * Get the default exporter instance for advanced usage.
 */
@NonCPS
def getExporter() {
    return DEFAULT_EXPORTER
}

@NonCPS
private PrometheusMetricsExporter resolveExporter(String namespace, String subsystem, int histogramBuckets) {
    if (namespace == 'jenkins' && subsystem == 'pipeline' && histogramBuckets == 10) {
        return DEFAULT_EXPORTER
    }
    String key = "${namespace}\u0000${subsystem}\u0000${histogramBuckets}"
    return SCOPED_EXPORTERS.computeIfAbsent(key) {
        new PrometheusMetricsExporter(namespace, subsystem, histogramBuckets)
    }
}

@NonCPS
private String exportWithNonCps(PrometheusMetricsExporter target) {
    return target.exportMetrics()
}

@NonCPS
private void recordBuildNonCps(String jobName, String result, long durationMs) {
    DEFAULT_EXPORTER.recordBuildCompletion(jobName, result, durationMs)
}

@NonCPS
private void recordStageNonCps(String jobName, String stageName, long durationMs, boolean success) {
    DEFAULT_EXPORTER.recordStageExecution(jobName, stageName, durationMs, success)
}

@NonCPS
private Map summaryNonCps() {
    return DEFAULT_EXPORTER.getMetricsSummary()
}

@NonCPS
private void resetNonCps() {
    DEFAULT_EXPORTER.resetMetrics()
}
