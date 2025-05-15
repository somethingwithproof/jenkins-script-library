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

import com.github.thomasvincent.jenkinsscripts.jobs.JobHealthAnalyzer
import com.github.thomasvincent.jenkinsscripts.jobs.JobHealthReport
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Analyzes Jenkins job health and generates reports.
 * 
 * '''Usage:'''
 * ```groovy
 * # Analyze a specific job
 * ./AnalyzeJobHealth.groovy --job my-job
 * 
 * # Find unstable jobs
 * ./AnalyzeJobHealth.groovy --unstable --threshold 75
 * 
 * # Find jobs with increasing duration
 * ./AnalyzeJobHealth.groovy --duration-trend 10
 * 
 * # Output in JSON format
 * ./AnalyzeJobHealth.groovy --job my-job --json
 * 
 * # Save report to file
 * ./AnalyzeJobHealth.groovy --job my-job --output report.txt
 * 
 * # Show help
 * ./AnalyzeJobHealth.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy AnalyzeJobHealth [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Name of job to analyze')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Pattern to match job names')
    u(longOpt: 'unstable', 'Find unstable jobs')
    t(longOpt: 'threshold', args: 1, argName: 'percent', 'Success rate threshold (default: 80)')
    d(longOpt: 'duration-trend', args: 1, argName: 'percent', 'Find jobs with increasing duration above threshold')
    c(longOpt: 'count', args: 1, argName: 'number', 'Number of builds to analyze (default: 20)')
    D(longOpt: 'days', args: 1, argName: 'number', 'Number of days to analyze (default: 30)')
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for report')
    J(longOpt: 'json', 'Output in JSON format')
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

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create analyzer with specified options
int buildCount = options.c ? Integer.parseInt(options.c) : 20
int daysToAnalyze = options.D ? Integer.parseInt(options.D) : 30
def analyzer = new JobHealthAnalyzer(jenkins, buildCount, daysToAnalyze)

println "Analyzing Jenkins jobs..."
println "- Build count limit: ${buildCount}"
println "- Days to analyze: ${daysToAnalyze}"

// Determine output format
boolean jsonFormat = options.J ?: false
String outputFile = options.o

// Store reports
def reports = []

// Execute requested operation
if (options.j) {
    // Analyze a specific job
    def jobName = options.j
    println "Analyzing job: ${jobName}"
    
    def report = analyzer.analyzeJob(jobName)
    if (report) {
        reports.add(report)
        outputReport(report, jsonFormat, outputFile)
    } else {
        println "Job not found or no builds to analyze"
    }
} else if (options.p) {
    // Analyze jobs matching a pattern
    def pattern = options.p
    println "Analyzing jobs matching pattern: ${pattern}"
    
    reports = analyzer.analyzeAllJobs(pattern)
    if (reports) {
        outputReports(reports, jsonFormat, outputFile)
    } else {
        println "No matching jobs found or no builds to analyze"
    }
} else if (options.u) {
    // Find unstable jobs
    double threshold = options.t ? Double.parseDouble(options.t) : 80.0
    println "Finding unstable jobs (below ${threshold}% success rate)"
    
    reports = analyzer.findUnstableJobs(threshold)
    if (reports) {
        outputReports(reports, jsonFormat, outputFile)
    } else {
        println "No unstable jobs found"
    }
} else if (options.d) {
    // Find jobs with increasing duration
    double threshold = Double.parseDouble(options.d)
    println "Finding jobs with increasing duration (above ${threshold}% increase)"
    
    reports = analyzer.findJobsWithIncreasingDuration(threshold)
    if (reports) {
        outputReports(reports, jsonFormat, outputFile)
    } else {
        println "No jobs with increasing duration found"
    }
} else {
    // Default to analyzing all jobs
    println "Analyzing all jobs..."
    
    reports = analyzer.analyzeAllJobs()
    if (reports) {
        println "Analyzed ${reports.size()} jobs"
        outputSummary(reports, jsonFormat, outputFile)
    } else {
        println "No jobs found to analyze"
    }
}

/**
 * Outputs a single job health report.
 * 
 * @param report Report to output
 * @param jsonFormat Whether to output in JSON format
 * @param outputFile File to output to (null for console)
 */
void outputReport(JobHealthReport report, boolean jsonFormat, String outputFile) {
    String output
    
    if (jsonFormat) {
        def reportMap = [
            jobName: report.jobName,
            buildCount: report.buildCount,
            successRate: report.successRate,
            successCount: report.successCount,
            failureCount: report.failureCount,
            unstableCount: report.unstableCount,
            abortedCount: report.abortedCount,
            averageDuration: report.averageDuration,
            minDuration: report.minDuration,
            maxDuration: report.maxDuration,
            durationTrend: report.durationTrend,
            failurePatterns: report.failurePatterns
        ]
        output = JsonOutput.prettyPrint(JsonOutput.toJson(reportMap))
    } else {
        output = report.generateReport()
    }
    
    if (outputFile) {
        new File(outputFile).text = output
        println "Report saved to ${outputFile}"
    } else {
        println output
    }
}

/**
 * Outputs multiple job health reports.
 * 
 * @param reports Reports to output
 * @param jsonFormat Whether to output in JSON format
 * @param outputFile File to output to (null for console)
 */
void outputReports(List<JobHealthReport> reports, boolean jsonFormat, String outputFile) {
    String output
    
    if (jsonFormat) {
        def reportMaps = reports.collect { report ->
            [
                jobName: report.jobName,
                buildCount: report.buildCount,
                successRate: report.successRate,
                successCount: report.successCount,
                failureCount: report.failureCount,
                unstableCount: report.unstableCount,
                abortedCount: report.abortedCount,
                averageDuration: report.averageDuration,
                minDuration: report.minDuration,
                maxDuration: report.maxDuration,
                durationTrend: report.durationTrend,
                failurePatterns: report.failurePatterns
            ]
        }
        output = JsonOutput.prettyPrint(JsonOutput.toJson(reportMaps))
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Jenkins Job Health Report\n")
        sb.append("========================\n")
        sb.append("Generated: ${new Date()}\n")
        sb.append("Jobs analyzed: ${reports.size()}\n\n")
        
        reports.each { report ->
            sb.append(report.generateReport())
            sb.append("\n---\n\n")
        }
        
        output = sb.toString()
    }
    
    if (outputFile) {
        new File(outputFile).text = output
        println "Report saved to ${outputFile}"
    } else {
        println output
    }
}

/**
 * Outputs a summary of job health reports.
 * 
 * @param reports Reports to summarize
 * @param jsonFormat Whether to output in JSON format
 * @param outputFile File to output to (null for console)
 */
void outputSummary(List<JobHealthReport> reports, boolean jsonFormat, String outputFile) {
    // Find interesting metrics
    def unstableJobs = reports.findAll { it.successRate < 80.0 && it.buildCount > 0 }
    def slowingJobs = reports.findAll { it.durationTrend > 10.0 && it.buildCount >= 5 }
    
    // Sort jobs
    unstableJobs.sort { a, b -> a.successRate <=> b.successRate }
    slowingJobs.sort { a, b -> b.durationTrend <=> a.durationTrend }
    
    String output
    
    if (jsonFormat) {
        def summary = [
            totalJobs: reports.size(),
            averageSuccessRate: reports.sum { it.successRate } / reports.size(),
            unstableJobsCount: unstableJobs.size(),
            slowingJobsCount: slowingJobs.size(),
            unstableJobs: unstableJobs.collect { [name: it.jobName, successRate: it.successRate] },
            slowingJobs: slowingJobs.collect { [name: it.jobName, durationTrend: it.durationTrend] }
        ]
        output = JsonOutput.prettyPrint(JsonOutput.toJson(summary))
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Jenkins Job Health Summary\n")
        sb.append("=========================\n")
        sb.append("Generated: ${new Date()}\n")
        sb.append("Total jobs analyzed: ${reports.size()}\n")
        sb.append("Average success rate: ${String.format("%.1f", reports.sum { it.successRate } / reports.size())}%\n\n")
        
        sb.append("Unstable Jobs (< 80% success rate):\n")
        if (unstableJobs.isEmpty()) {
            sb.append("None\n")
        } else {
            unstableJobs.take(10).each { report ->
                sb.append("- ${report.jobName}: ${String.format("%.1f", report.successRate)}% success rate\n")
            }
            
            if (unstableJobs.size() > 10) {
                sb.append("...and ${unstableJobs.size() - 10} more\n")
            }
        }
        
        sb.append("\nJobs with Increasing Duration (> 10% increase):\n")
        if (slowingJobs.isEmpty()) {
            sb.append("None\n")
        } else {
            slowingJobs.take(10).each { report ->
                sb.append("- ${report.jobName}: ${String.format("%.1f", report.durationTrend)}% increase\n")
            }
            
            if (slowingJobs.size() > 10) {
                sb.append("...and ${slowingJobs.size() - 10} more\n")
            }
        }
        
        output = sb.toString()
    }
    
    if (outputFile) {
        new File(outputFile).text = output
        println "Summary saved to ${outputFile}"
    } else {
        println output
    }
}