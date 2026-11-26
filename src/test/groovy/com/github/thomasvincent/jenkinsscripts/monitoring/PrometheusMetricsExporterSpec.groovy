// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.monitoring

import spock.lang.Specification
import spock.lang.Subject

@Subject(PrometheusMetricsExporter)
class PrometheusMetricsExporterSpec extends Specification {

    def "emits HELP and TYPE once per metric family and handles colon-containing names"() {
        given:
        def exporter = new PrometheusMetricsExporter('jenkins', 'pipeline')
        exporter.recordBuildCompletion('folder:job-a', 'SUCCESS', 1000L)
        exporter.recordBuildCompletion('folder:job-a', 'FAILURE', 3000L)
        exporter.recordBuildCompletion('job-b', 'SUCCESS', 5000L)
        exporter.recordStageExecution('folder:job-a', 'stage:build:1', 400L, true)
        exporter.recordStageExecution('folder:job-a', 'stage:build:1', 600L, true)
        exporter.recordStageExecution('job-b', 'test', 200L, false)

        when:
        String output = exporter.exportMetrics()

        then:
        output.count('# HELP jenkins_pipeline_build_duration_seconds_avg') == 1
        output.count('# TYPE jenkins_pipeline_build_duration_seconds_avg gauge') == 1
        output.count('# HELP jenkins_pipeline_stage_executions_total') == 1
        output.count('# TYPE jenkins_pipeline_stage_executions_total counter') == 1
        output.count('# HELP jenkins_pipeline_stage_duration_seconds_avg') == 1
        output.contains('jenkins_pipeline_build_duration_seconds_avg{job="folder:job-a"} 2.0')
        output.contains('jenkins_pipeline_build_duration_seconds_min{job="folder:job-a"} 1.0')
        output.contains('jenkins_pipeline_build_duration_seconds_max{job="folder:job-a"} 3.0')
        output.contains('jenkins_pipeline_stage_executions_total{job="folder:job-a",stage="stage:build:1"} 2')
        output.contains('jenkins_pipeline_stage_duration_seconds_avg{job="folder:job-a",stage="stage:build:1"} 0.5')
        output.contains('jenkins_pipeline_stage_duration_seconds_min{job="folder:job-a",stage="stage:build:1"} 0.4')
        output.contains('jenkins_pipeline_stage_duration_seconds_max{job="folder:job-a",stage="stage:build:1"} 0.6')
    }

    def "bounds duration window to last 100 samples using ArrayDeque"() {
        given:
        def exporter = new PrometheusMetricsExporter()
        // Record 150 builds: first 50 have 1000ms, next 100 have 4000ms
        50.times { exporter.recordBuildCompletion('bounded-job', 'SUCCESS', 1000L) }
        100.times { exporter.recordBuildCompletion('bounded-job', 'SUCCESS', 4000L) }

        when:
        String output = exporter.exportMetrics()

        then:
        output.contains('jenkins_pipeline_build_duration_seconds_min{job="bounded-job"} 4.0')
        output.contains('jenkins_pipeline_build_duration_seconds_max{job="bounded-job"} 4.0')
        output.contains('jenkins_pipeline_build_duration_seconds_avg{job="bounded-job"} 4.0')
    }
}
