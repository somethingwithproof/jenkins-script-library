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

import com.github.thomasvincent.jenkinsscripts.jobs.JobScheduler
import com.github.thomasvincent.jenkinsscripts.jobs.ThrottleConfig
import com.github.thomasvincent.jenkinsscripts.jobs.QueueAnalysisReport
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Optimizes Jenkins job scheduling and throttling.
 * 
 * '''Usage:'''
 * ```groovy
 * # Analyze the build queue
 * ./OptimizeJobScheduling.groovy --analyze
 * 
 * # Set a throttle for a job
 * ./OptimizeJobScheduling.groovy --throttle-job my-job --max 3
 * 
 * # Set a rate limit for a job
 * ./OptimizeJobScheduling.groovy --throttle-job api-job --max 5 --period 60
 * 
 * # Set a throttle for a label
 * ./OptimizeJobScheduling.groovy --throttle-label database --max 2
 * 
 * # Remove a throttle
 * ./OptimizeJobScheduling.groovy --remove-throttle-job my-job
 * 
 * # Get recommendations
 * ./OptimizeJobScheduling.groovy --recommendations
 * 
 * # Auto-optimize throttling
 * ./OptimizeJobScheduling.groovy --auto-optimize
 * 
 * # Show help
 * ./OptimizeJobScheduling.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy OptimizeJobScheduling [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    a(longOpt: 'analyze', 'Analyze the build queue and executor load')
    tj(longOpt: 'throttle-job', args: 1, argName: 'jobName', 'Set throttle for a job')
    tl(longOpt: 'throttle-label', args: 1, argName: 'labelName', 'Set throttle for a label')
    m(longOpt: 'max', args: 1, argName: 'limit', 'Maximum concurrent builds (used with --throttle-*)')
    p(longOpt: 'period', args: 1, argName: 'seconds', 'Time period for rate limiting in seconds (used with --throttle-*)')
    rtj(longOpt: 'remove-throttle-job', args: 1, argName: 'jobName', 'Remove throttle from a job')
    rtl(longOpt: 'remove-throttle-label', args: 1, argName: 'labelName', 'Remove throttle from a label')
    r(longOpt: 'recommendations', 'Generate scheduling recommendations')
    ao(longOpt: 'auto-optimize', 'Automatically apply optimized throttle settings')
    lt(longOpt: 'list-throttles', 'List current throttle settings')
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for analysis or recommendations')
    j(longOpt: 'json', 'Output in JSON format')
}

// Parse the command line
def options = cli.parse(args)
if (!options) {
    return
}

// Show help and exit if requested
if (options.h) {
    cli.usage()
    return
}

// Validate option combinations
if ((options.tj || options.tl) && !options.m) {
    println "Error: --max is required with --throttle-job or --throttle-label"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create scheduler
def scheduler = new JobScheduler(jenkins)

// Determine output format
boolean jsonFormat = options.j ?: false
String outputFile = options.o

// Execute requested operation
if (options.a) {
    // Analyze queue
    println "Analyzing Jenkins build queue and executor load..."
    QueueAnalysisReport report = scheduler.analyzeQueue()
    
    if (jsonFormat) {
        def reportMap = [
            queueSize: report.queueSize,
            blockedJobs: report.blockedJobs,
            stuckJobs: report.stuckJobs,
            runningJobs: report.runningJobs,
            offlineNodes: report.offlineNodes,
            nodeUtilization: report.nodeUtilization,
            availableExecutors: report.availableExecutors,
            labelDemand: report.labelDemand,
            labelCapacity: report.labelCapacity,
            labelPressure: report.labelPressure
        ]
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(reportMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Analysis saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        String reportText = report.generateReport()
        
        if (outputFile) {
            new File(outputFile).text = reportText
            println "Analysis saved to ${outputFile}"
        } else {
            println reportText
        }
    }
} else if (options.tj) {
    // Set job throttle
    def jobName = options.tj
    def maxConcurrent = Integer.parseInt(options.m)
    def periodSeconds = options.p ? Integer.parseInt(options.p) : 0
    
    println "Setting throttle for job ${jobName}: max ${maxConcurrent} concurrent" + 
            (periodSeconds > 0 ? ", max ${maxConcurrent} per ${periodSeconds}s" : "")
    
    boolean result = scheduler.setJobThrottle(jobName, maxConcurrent, periodSeconds)
    if (result) {
        println "Successfully set throttle for job ${jobName}"
    } else {
        println "Failed to set throttle for job ${jobName}"
    }
} else if (options.tl) {
    // Set label throttle
    def labelName = options.tl
    def maxConcurrent = Integer.parseInt(options.m)
    def periodSeconds = options.p ? Integer.parseInt(options.p) : 0
    
    println "Setting throttle for label ${labelName}: max ${maxConcurrent} concurrent" + 
            (periodSeconds > 0 ? ", max ${maxConcurrent} per ${periodSeconds}s" : "")
    
    boolean result = scheduler.setLabelThrottle(labelName, maxConcurrent, periodSeconds)
    if (result) {
        println "Successfully set throttle for label ${labelName}"
    } else {
        println "Failed to set throttle for label ${labelName}"
    }
} else if (options.rtj) {
    // Remove job throttle
    def jobName = options.rtj
    println "Removing throttle for job ${jobName}"
    
    boolean result = scheduler.removeJobThrottle(jobName)
    if (result) {
        println "Successfully removed throttle for job ${jobName}"
    } else {
        println "No throttle found for job ${jobName}"
    }
} else if (options.rtl) {
    // Remove label throttle
    def labelName = options.rtl
    println "Removing throttle for label ${labelName}"
    
    boolean result = scheduler.removeLabelThrottle(labelName)
    if (result) {
        println "Successfully removed throttle for label ${labelName}"
    } else {
        println "No throttle found for label ${labelName}"
    }
} else if (options.r) {
    // Generate recommendations
    println "Generating scheduling recommendations..."
    
    List<String> recommendations = scheduler.generateRecommendations()
    
    if (jsonFormat) {
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(recommendations))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Recommendations saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Scheduling Recommendations:\n")
        
        if (recommendations.isEmpty()) {
            sb.append("No recommendations at this time\n")
        } else {
            recommendations.eachWithIndex { recommendation, index ->
                sb.append("${index + 1}. ${recommendation}\n")
            }
        }
        
        String recommendationsText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = recommendationsText
            println "Recommendations saved to ${outputFile}"
        } else {
            println recommendationsText
        }
    }
} else if (options.ao) {
    // Auto-optimize throttling
    println "Automatically optimizing throttle settings..."
    
    List<String> applied = scheduler.autoOptimizeThrottling()
    
    if (applied.isEmpty()) {
        println "No throttle settings applied"
    } else {
        println "Applied ${applied.size()} throttle settings:"
        applied.each { change ->
            println "- ${change}"
        }
    }
} else if (options.lt) {
    // List current throttle settings
    Map<String, ThrottleConfig> jobThrottles = scheduler.getJobThrottles()
    Map<String, ThrottleConfig> labelThrottles = scheduler.getLabelThrottles()
    
    if (jsonFormat) {
        def throttlesMap = [
            jobThrottles: jobThrottles.collectEntries { jobName, config ->
                [(jobName): [maxConcurrent: config.maxConcurrent, periodSeconds: config.periodSeconds]]
            },
            labelThrottles: labelThrottles.collectEntries { labelName, config ->
                [(labelName): [maxConcurrent: config.maxConcurrent, periodSeconds: config.periodSeconds]]
            }
        ]
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(throttlesMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Throttle settings saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Current Throttle Settings:\n")
        
        if (jobThrottles.isEmpty() && labelThrottles.isEmpty()) {
            sb.append("No throttle settings configured\n")
        } else {
            if (!jobThrottles.isEmpty()) {
                sb.append("\nJob Throttles:\n")
                jobThrottles.each { jobName, config ->
                    sb.append(String.format("- %-30s: max %d concurrent", jobName, config.maxConcurrent))
                    if (config.periodSeconds > 0) {
                        sb.append(String.format(", max %d per %ds", config.maxConcurrent, config.periodSeconds))
                    }
                    sb.append("\n")
                }
            }
            
            if (!labelThrottles.isEmpty()) {
                sb.append("\nLabel Throttles:\n")
                labelThrottles.each { labelName, config ->
                    sb.append(String.format("- %-30s: max %d concurrent", labelName, config.maxConcurrent))
                    if (config.periodSeconds > 0) {
                        sb.append(String.format(", max %d per %ds", config.maxConcurrent, config.periodSeconds))
                    }
                    sb.append("\n")
                }
            }
        }
        
        String throttlesText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = throttlesText
            println "Throttle settings saved to ${outputFile}"
        } else {
            println throttlesText
        }
    }
} else {
    // Default to showing a summary and recommendations
    println "Jenkins Job Scheduling Summary"
    
    QueueAnalysisReport report = scheduler.analyzeQueue()
    println "Queue size: ${report.queueSize}"
    println "Blocked jobs: ${report.blockedJobs.size()}"
    
    if (report.offlineNodes.size() > 0) {
        println "Offline nodes: ${report.offlineNodes.join(', ')}"
    }
    
    println "\nTop 3 Node Utilization:"
    report.nodeUtilization.sort { -it.value }.take(3).each { node, utilization ->
        println String.format("- %-20s: %5.1f%%", node, utilization)
    }
    
    println "\nTop 3 Label Pressure Points:"
    report.labelPressure.sort { -it.value }.take(3).each { label, pressure ->
        if (pressure >= 0) {
            println String.format("- %-20s: %5.1f%%", label, pressure)
        }
    }
    
    println "\nRecommendations:"
    List<String> recommendations = scheduler.generateRecommendations()
    if (recommendations.isEmpty()) {
        println "No recommendations at this time"
    } else {
        recommendations.take(3).each { recommendation ->
            println "- ${recommendation}"
        }
        
        if (recommendations.size() > 3) {
            println "...and ${recommendations.size() - 3} more recommendations"
        }
    }
    
    println "\nUse --analyze for detailed queue analysis"
    println "Use --recommendations for full recommendations"
    println "Use --auto-optimize to apply recommendations automatically"
}