#!/usr/bin/env groovy

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

package com.github.thomasvincent.jenkinsscripts.scripts

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import jenkins.model.Jenkins
import hudson.model.Job
import hudson.model.Run
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.graph.FlowGraphWalker
import org.jenkinsci.plugins.workflow.actions.TimingAction
import org.jenkinsci.plugins.workflow.actions.LabelAction
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Analyzes Jenkins pipeline performance and identifies bottlenecks.
 * 
 * '''Usage:'''
 * ```groovy
 * # Analyze a specific pipeline job's recent builds
 * ./AnalyzePipelinePerformance.groovy --job "my-pipeline" --builds 10
 * 
 * # Analyze multiple pipeline jobs matching a pattern
 * ./AnalyzePipelinePerformance.groovy --pattern "deploy-*" --builds 5
 * 
 * # Show only stages that take longer than a threshold
 * ./AnalyzePipelinePerformance.groovy --job "my-pipeline" --threshold 60
 * 
 * # Output in JSON format
 * ./AnalyzePipelinePerformance.groovy --job "my-pipeline" --json
 * 
 * # Include stage history trends
 * ./AnalyzePipelinePerformance.groovy --job "my-pipeline" --trends
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */

/**
 * Logger for this script.
 */
private static final Logger LOGGER = Logger.getLogger("AnalyzePipelinePerformance.groovy")

/**
 * Creates a command-line argument parser for the script.
 * 
 * <p>Configures the available command-line options that can be used
 * when running this script.</p>
 */
def cli = new CliBuilder(usage: 'groovy AnalyzePipelinePerformance [options]',
                        header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    j(longOpt: 'job', args: 1, argName: 'name', 'Pipeline job name to analyze')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Pattern to match pipeline job names')
    b(longOpt: 'builds', args: 1, argName: 'count', 'Number of recent builds to analyze (default: 5)')
    t(longOpt: 'threshold', args: 1, argName: 'seconds', 'Only show stages longer than threshold in seconds (default: 0)')
    r(longOpt: 'trends', 'Include performance trends over time')
}

/**
 * Parse the provided command-line arguments.
 * 
 * <p>If parsing fails, the script will exit.</p>
 */
def options = cli.parse(args)
if (!options) {
    return
}

/**
 * Display help information if requested.
 * 
 * <p>If the help option is specified, shows usage information and exits.</p>
 */
if (options.h) {
    cli.usage()
    return
}

/**
 * Validate required parameters.
 * 
 * <p>Either job name or pattern must be provided.</p>
 */
if (!options.job && !options.pattern) {
    println "Error: Either --job or --pattern must be specified"
    cli.usage()
    return
}

/**
 * Parse numeric parameters with defaults.
 */
def buildCount = options.builds ? options.builds as int : 5
def thresholdSeconds = options.threshold ? options.threshold as int : 0
def includeTrends = options.trends ?: false

/**
 * Get the current Jenkins instance.
 */
def jenkins = Jenkins.get()

/**
 * Find pipeline jobs matching criteria.
 * 
 * <p>Gets jobs matching either the specific name or pattern.</p>
 */
List<WorkflowJob> pipelineJobs = []

ErrorHandler.withErrorHandling("finding pipeline jobs", {
    if (options.job) {
        def job = jenkins.getItemByFullName(options.job, Job.class)
        if (job instanceof WorkflowJob) {
            pipelineJobs.add(job)
        } else if (job != null) {
            println "Warning: Job '${options.job}' exists but is not a pipeline job"
        } else {
            println "Warning: Job '${options.job}' not found"
        }
    } else if (options.pattern) {
        def pattern = options.pattern
        jenkins.getAllItems(WorkflowJob.class).each { job ->
            if (job.fullName =~ pattern) {
                pipelineJobs.add(job)
            }
        }
    }
}, LOGGER)

if (pipelineJobs.isEmpty()) {
    println "No matching pipeline jobs found"
    return
}

/**
 * Collect build data for each pipeline job.
 */
def allJobsData = []

pipelineJobs.each { job ->
    def jobData = [name: job.fullName, builds: []]
    
    def builds = job.builds.take(buildCount)
    builds.each { build ->
        if (build instanceof WorkflowRun) {
            def buildData = analyzeBuild(build, thresholdSeconds)
            if (buildData) {
                jobData.builds.add(buildData)
            }
        }
    }
    
    // Calculate stage trends if requested
    if (includeTrends && jobData.builds) {
        jobData.trends = calculateTrends(jobData.builds)
    }
    
    // Calculate job-level stats
    if (jobData.builds) {
        jobData.stats = calculateJobStats(jobData.builds)
    }
    
    allJobsData.add(jobData)
}

/**
 * Output the results in the requested format.
 */
if (options.json) {
    // Output in JSON format
    println JsonOutput.prettyPrint(JsonOutput.toJson(allJobsData))
} else {
    // Output in human-readable format
    allJobsData.each { jobData ->
        println "=".multiply(80)
        println "PIPELINE: ${jobData.name}"
        println "=".multiply(80)
        
        if (jobData.stats) {
            println "\nSUMMARY:"
            println "  Average Build Duration: ${formatDuration(jobData.stats.avgDuration)}"
            println "  Success Rate: ${String.format("%.1f%%", jobData.stats.successRate * 100)}"
            println "  Most Time-Consuming Stage: ${jobData.stats.slowestStage.name} (avg: ${formatDuration(jobData.stats.slowestStage.avgDuration)})"
            println "  Most Unstable Stage: ${jobData.stats.unstableStage.name} (failure rate: ${String.format("%.1f%%", jobData.stats.unstableStage.failureRate * 100)})"
            println ""
        }
        
        if (jobData.trends) {
            println "TRENDS:"
            jobData.trends.each { stageName, trend ->
                println "  ${stageName}:"
                println "    First Build: ${formatDuration(trend.first)}"
                println "    Last Build: ${formatDuration(trend.last)}"
                println "    Change: ${trend.increasing ? '+' : ''}${String.format("%.1f%%", trend.changePercent)}"
                println ""
            }
        }
        
        println "RECENT BUILDS:"
        jobData.builds.each { build ->
            println "  #${build.number} (${build.result}):"
            println "    Started: ${build.timestamp}"
            println "    Duration: ${formatDuration(build.duration)}"
            println "    Stages:"
            
            build.stages.sort { -it.duration }.each { stage ->
                println "      ${stage.name}: ${formatDuration(stage.duration)}${stage.status != 'SUCCESS' ? ' [' + stage.status + ']' : ''}" 
            }
            println ""
        }
    }
}

/**
 * Analyzes a workflow build to extract stage timing information.
 * 
 * @param build The WorkflowRun to analyze
 * @param thresholdSeconds Only include stages longer than this threshold
 * @return Map with build data or null if build data cannot be extracted
 */
def analyzeBuild(WorkflowRun build, int thresholdSeconds) {
    return ErrorHandler.withErrorHandling("analyzing build #${build.number}", {
        if (!build.execution) {
            return null
        }
        
        def buildData = [
            number: build.number,
            timestamp: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(build.startTimeInMillis)),
            duration: build.duration,
            result: build.result?.toString() ?: "IN_PROGRESS",
            stages: []
        ]
        
        // Extract stage information
        def stageNodes = []
        def walker = new FlowGraphWalker(build.execution)
        
        walker.forEach { FlowNode node ->
            def stageAction = node.getAction(LabelAction.class)
            if (stageAction && stageAction.displayName.startsWith("Stage: ")) {
                stageNodes.add(node)
            }
        }
        
        stageNodes.each { node ->
            def stageName = node.getAction(LabelAction.class).displayName - "Stage: "
            def timingAction = node.getAction(TimingAction.class)
            
            if (timingAction) {
                def startTime = timingAction.startTime
                
                // Find the end time (either from the next stage or the end of the build)
                def endTime = build.startTimeInMillis + build.duration
                def nextStageIndex = stageNodes.indexOf(node) + 1
                if (nextStageIndex < stageNodes.size()) {
                    def nextNode = stageNodes[nextStageIndex]
                    def nextTimingAction = nextNode.getAction(TimingAction.class)
                    if (nextTimingAction) {
                        endTime = nextTimingAction.startTime
                    }
                }
                
                def stageDuration = endTime - startTime
                
                // Only include stages longer than threshold
                if (stageDuration >= thresholdSeconds * 1000) {
                    buildData.stages.add([
                        name: stageName,
                        duration: stageDuration,
                        status: getStageStatus(node, build)
                    ])
                }
            }
        }
        
        return buildData
    }, LOGGER, null)
}

/**
 * Determines the status of a pipeline stage.
 * 
 * @param stageNode The stage FlowNode
 * @param build The WorkflowRun containing the stage
 * @return Status string (SUCCESS, FAILURE, UNSTABLE, etc.)
 */
def getStageStatus(FlowNode stageNode, WorkflowRun build) {
    // A simplified approach - in a real implementation, you would need to analyze
    // the nodes more thoroughly to determine if a specific stage failed
    return "SUCCESS"
}

/**
 * Calculates performance trends for stages across builds.
 * 
 * @param builds List of build data maps
 * @return Map of stage names to trend information
 */
def calculateTrends(List<Map> builds) {
    def trends = [:]
    
    // Group stages by name across builds
    def stagesByName = [:]
    
    builds.each { build ->
        build.stages.each { stage ->
            if (!stagesByName.containsKey(stage.name)) {
                stagesByName[stage.name] = []
            }
            stagesByName[stage.name].add([
                buildNumber: build.number,
                duration: stage.duration
            ])
        }
    }
    
    // Calculate trends for each stage
    stagesByName.each { stageName, stageDurations ->
        if (stageDurations.size() >= 2) {
            def sortedDurations = stageDurations.sort { it.buildNumber }
            def first = sortedDurations.first().duration
            def last = sortedDurations.last().duration
            
            def changePercent = ((last - first) / first) * 100
            
            trends[stageName] = [
                first: first,
                last: last,
                changePercent: changePercent,
                increasing: last > first
            ]
        }
    }
    
    return trends
}

/**
 * Calculates statistics for a job based on its builds.
 * 
 * @param builds List of build data maps
 * @return Map with job statistics
 */
def calculateJobStats(List<Map> builds) {
    def stats = [:]
    
    // Calculate average build duration
    def totalDuration = builds.sum { it.duration } ?: 0
    stats.avgDuration = totalDuration / builds.size()
    
    // Calculate success rate
    def successfulBuilds = builds.count { it.result == "SUCCESS" }
    stats.successRate = successfulBuilds / builds.size()
    
    // Find slowest stage
    def stageStats = [:]
    builds.each { build ->
        build.stages.each { stage ->
            if (!stageStats.containsKey(stage.name)) {
                stageStats[stage.name] = [durations: [], failures: 0]
            }
            stageStats[stage.name].durations.add(stage.duration)
            if (stage.status != "SUCCESS") {
                stageStats[stage.name].failures++
            }
        }
    }
    
    // Calculate stage stats
    stageStats.each { stageName, data ->
        data.avgDuration = data.durations.sum() / data.durations.size()
        data.failureRate = data.failures / builds.size()
        data.name = stageName
    }
    
    // Find slowest and most unstable stages
    def stageValues = stageStats.values() as List
    if (!stageValues.isEmpty()) {
        stats.slowestStage = stageValues.max { it.avgDuration }
        stats.unstableStage = stageValues.max { it.failureRate }
    } else {
        stats.slowestStage = [name: "N/A", avgDuration: 0]
        stats.unstableStage = [name: "N/A", failureRate: 0]
    }
    
    return stats
}

/**
 * Formats a duration in milliseconds to a human-readable string.
 * 
 * @param durationMs Duration in milliseconds
 * @return Formatted duration string
 */
def formatDuration(long durationMs) {
    def seconds = durationMs / 1000
    def minutes = (int)(seconds / 60)
    def hours = (int)(minutes / 60)
    
    minutes = minutes % 60
    seconds = seconds % 60
    
    if (hours > 0) {
        return String.format("%dh %02dm %02ds", hours, minutes, (int)seconds)
    } else if (minutes > 0) {
        return String.format("%dm %02ds", minutes, (int)seconds)
    } else {
        return String.format("%.1fs", seconds)
    }
}