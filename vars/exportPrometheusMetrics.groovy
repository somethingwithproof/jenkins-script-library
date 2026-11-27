// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2024-2026 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for exporting build-scoped Prometheus metrics.
 */

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.monitoring.PrometheusMetricsExporter
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Exports Jenkins build, stage, agent, queue, and JVM metrics in Prometheus text format.
 *
 * Metrics state is scoped to the current pipeline script instance rather than a
 * shared static singleton, so concurrent builds never overwrite or reset one another.
 */

@groovy.transform.Field
private transient PrometheusMetricsExporter buildExporter

@groovy.transform.Field
private static final Logger LOGGER = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.vars.exportPrometheusMetrics')

def call(Map params = [:]) {
    try {
        if (!params || (!params.namespace && !params.subsystem && !params.histogramBuckets)) {
            return exportBuildScopedNonCps()
        }
        String namespace = (params.namespace ?: 'jenkins') as String
        String subsystem = (params.subsystem ?: 'pipeline') as String
        int histogramBuckets = (params.histogramBuckets ?: 10) as int
        return exportWithOptionsNonCps(namespace, subsystem, histogramBuckets)
    } catch (Exception e) {
        LOGGER.log(Level.WARNING, 'Failed to export Prometheus metrics', e)
        throw e
    }
}

/**
 * Records a build completion event in the current build's exporter.
 */
def recordBuild(String jobName, String result, long durationMs) {
    recordBuildNonCps(jobName, result, durationMs)
}

/**
 * Records a stage execution in the current build's exporter.
 */
def recordStage(String jobName, String stageName, long durationMs, boolean success = true) {
    recordStageNonCps(jobName, stageName, durationMs, success)
}

/**
 * Returns a plain serializable summary of metrics recorded in this build.
 */
def summary() {
    return summaryNonCps()
}

/**
 * Resets metrics recorded in the current build's exporter.
 */
def reset() {
    resetNonCps()
}

@NonCPS
private PrometheusMetricsExporter getOrCreateExporter() {
    if (buildExporter == null) {
        buildExporter = new PrometheusMetricsExporter()
    }
    return buildExporter
}

@NonCPS
private String exportBuildScopedNonCps() {
    return getOrCreateExporter().exportMetrics()
}

@NonCPS
private String exportWithOptionsNonCps(String namespace, String subsystem, int histogramBuckets) {
    if (namespace == 'jenkins' && subsystem == 'pipeline' && histogramBuckets == 10) {
        return getOrCreateExporter().exportMetrics()
    }
    // Create an isolated exporter for custom naming/buckets without mutating buildExporter.
    PrometheusMetricsExporter scoped = new PrometheusMetricsExporter(namespace, subsystem, histogramBuckets)
    return scoped.exportMetrics()
}

@NonCPS
private void recordBuildNonCps(String jobName, String result, long durationMs) {
    getOrCreateExporter().recordBuildCompletion(jobName, result, durationMs)
}

@NonCPS
private void recordStageNonCps(String jobName, String stageName, long durationMs, boolean success) {
    getOrCreateExporter().recordStageExecution(jobName, stageName, durationMs, success)
}

@NonCPS
private Map summaryNonCps() {
    return getOrCreateExporter().getMetricsSummary()
}

@NonCPS
private void resetNonCps() {
    getOrCreateExporter().resetMetrics()
}
