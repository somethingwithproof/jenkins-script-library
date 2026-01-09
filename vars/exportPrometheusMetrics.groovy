/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for exporting Prometheus metrics.
 */

import com.github.thomasvincent.jenkinsscripts.monitoring.PrometheusMetricsExporter

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
static PrometheusMetricsExporter exporter = new PrometheusMetricsExporter()

def call() {
    return exporter.exportMetrics()
}

def call(Map params) {
    if (params.namespace || params.subsystem) {
        exporter = new PrometheusMetricsExporter(
            params.namespace ?: 'jenkins',
            params.subsystem ?: 'pipeline',
            params.histogramBuckets ?: 10
        )
    }
    return exporter.exportMetrics()
}

/**
 * Record a build completion event.
 */
def recordBuild(String jobName, String result, long durationMs) {
    exporter.recordBuildCompletion(jobName, result, durationMs)
}

/**
 * Record a stage execution.
 */
def recordStage(String jobName, String stageName, long durationMs, boolean success = true) {
    exporter.recordStageExecution(jobName, stageName, durationMs, success)
}

/**
 * Get metrics summary as a map.
 */
def summary() {
    return exporter.getMetricsSummary()
}

/**
 * Reset all metrics.
 */
def reset() {
    exporter.resetMetrics()
}

/**
 * Get the exporter instance for advanced usage.
 */
def getExporter() {
    return exporter
}
