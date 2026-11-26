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

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.analytics.PipelineMetricsCollector
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
    String jobName = (args.jobName ?: env.JOB_NAME) as String
    int historyLimit = (args.historyLimit ?: 100) as int
    boolean includeRecommendations = args.includeRecommendations != false

    // Validate inputs
    if (!jobName) {
        error "Pipeline metrics analysis requires a job name"
    }

    echo "[analyzePipelineMetrics] Analyzing pipeline metrics for: ${jobName}"

    try {
        // Confine Jenkins and collector inside @NonCPS and return plain serializable map
        Map metrics = collectMetricsNonCps(jobName, historyLimit)

        echo "[analyzePipelineMetrics] Analysis complete:"
        echo "  Total builds analyzed: ${metrics.summary.totalBuilds}"
        echo "  Success rate: ${metrics.summary.successRate}%"
        echo "  Average duration: ${formatDuration((metrics.summary.avgDuration ?: 0) as long)}"

        if (includeRecommendations && metrics.recommendations) {
            echo "[analyzePipelineMetrics] Top recommendations:"
            metrics.recommendations.take(3).each { rec ->
                echo "  [${rec.priority}] ${rec.message}"
            }
        }

        return metrics
    } catch (Exception e) {
        echo "[analyzePipelineMetrics] ERROR: Failed to analyze pipeline metrics: ${e.message}"
        throw e
    }
}

@NonCPS
private Map collectMetricsNonCps(String jobName, int historyLimit) {
    def jenkins = Jenkins.get()
    def collector = new PipelineMetricsCollector(jenkins)
    return collector.collectPipelineMetrics(jobName, historyLimit)
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
@NonCPS
private String formatDuration(long millis) {
    if (millis < 1000) {
        return "${millis}ms"
    } else if (millis < 60000) {
        return "${(millis / 1000.0).round(1)}s"
    } else {
        long minutes = millis.intdiv(60000).longValue()
        long seconds = Math.round((millis % 60000) / 1000.0)
        return "${minutes}m ${seconds}s"
    }
}