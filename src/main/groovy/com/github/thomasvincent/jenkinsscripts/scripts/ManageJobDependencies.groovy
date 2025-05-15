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

import com.github.thomasvincent.jenkinsscripts.jobs.JobDependencyManager
import com.github.thomasvincent.jenkinsscripts.jobs.JobDependencyGraph
import com.github.thomasvincent.jenkinsscripts.jobs.CompleteDependencyGraph
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Manages job dependencies in Jenkins.
 * 
 * '''Usage:'''
 * ```groovy
 * # Analyze dependencies for a job
 * ./ManageJobDependencies.groovy --job my-job
 * 
 * # Add a dependency between jobs
 * ./ManageJobDependencies.groovy --add --upstream build-job --downstream test-job
 * 
 * # Remove a dependency
 * ./ManageJobDependencies.groovy --remove --upstream build-job --downstream test-job
 * 
 * # Find orphaned jobs
 * ./ManageJobDependencies.groovy --orphaned
 * 
 * # Find circular dependencies
 * ./ManageJobDependencies.groovy --circular
 * 
 * # Generate a DOT diagram of all dependencies
 * ./ManageJobDependencies.groovy --diagram --output dependencies.dot
 * 
 * # Show help
 * ./ManageJobDependencies.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy ManageJobDependencies [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Analyze dependencies for a job')
    a(longOpt: 'add', 'Add a dependency between jobs')
    r(longOpt: 'remove', 'Remove a dependency between jobs')
    u(longOpt: 'upstream', args: 1, argName: 'jobName', 'Upstream job (used with --add or --remove)')
    d(longOpt: 'downstream', args: 1, argName: 'jobName', 'Downstream job (used with --add or --remove)')
    o(longOpt: 'orphaned', 'Find orphaned jobs (no dependencies)')
    e(longOpt: 'exclude', args: 1, argName: 'pattern', 'Regex pattern to exclude from orphaned jobs')
    c(longOpt: 'circular', 'Find circular dependencies')
    D(longOpt: 'diagram', 'Generate a complete dependency diagram in DOT format')
    O(longOpt: 'output', args: 1, argName: 'file', 'Output file for diagram or results')
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

// Create dependency manager
def manager = new JobDependencyManager(jenkins)

// Check for valid command combinations
if (options.a && options.r) {
    println "Error: Cannot specify both --add and --remove"
    cli.usage()
    return
}

if ((options.a || options.r) && (!options.u || !options.d)) {
    println "Error: Both --upstream and --downstream are required with --add or --remove"
    cli.usage()
    return
}

// Define output file
String outputFile = options.O

// Execute requested operation
if (options.j) {
    // Analyze dependencies for a job
    def jobName = options.j
    println "Analyzing dependencies for job: ${jobName}"
    
    def graph = manager.getJobDependencyGraph(jobName)
    if (graph) {
        println "Dependency Analysis for ${jobName}:"
        println "- Upstream jobs (${graph.upstreamJobs.size()}):"
        
        if (graph.upstreamJobs.isEmpty()) {
            println "  None"
        } else {
            graph.upstreamJobs.sort().each { upstream ->
                println "  * ${upstream}"
            }
        }
        
        println "- Downstream jobs (${graph.downstreamJobs.size()}):"
        
        if (graph.downstreamJobs.isEmpty()) {
            println "  None"
        } else {
            graph.downstreamJobs.sort().each { downstream ->
                println "  * ${downstream}"
            }
        }
        
        if (graph.isOrphaned()) {
            println "\nWarning: This job is orphaned (no upstream or downstream dependencies)."
        }
    } else {
        println "Job not found or error analyzing dependencies"
    }
} else if (options.a) {
    // Add a dependency
    def upstreamJob = options.u
    def downstreamJob = options.d
    println "Adding dependency: ${upstreamJob} → ${downstreamJob}"
    
    boolean result = manager.addJobDependency(upstreamJob, downstreamJob)
    if (result) {
        println "Successfully added dependency"
    } else {
        println "Failed to add dependency"
    }
} else if (options.r) {
    // Remove a dependency
    def upstreamJob = options.u
    def downstreamJob = options.d
    println "Removing dependency: ${upstreamJob} → ${downstreamJob}"
    
    boolean result = manager.removeJobDependency(upstreamJob, downstreamJob)
    if (result) {
        println "Successfully removed dependency"
    } else {
        println "Failed to remove dependency"
    }
} else if (options.o) {
    // Find orphaned jobs
    String excludePattern = options.e
    println "Finding orphaned jobs${excludePattern ? " (excluding pattern: ${excludePattern})" : ""}"
    
    List<String> orphanedJobs = manager.findOrphanedJobs(excludePattern)
    
    println "Orphaned Jobs (${orphanedJobs.size()}):"
    if (orphanedJobs.isEmpty()) {
        println "None"
    } else {
        orphanedJobs.each { jobName ->
            println "- ${jobName}"
        }
    }
    
    if (outputFile) {
        new File(outputFile).text = orphanedJobs.join('\n')
        println "\nOrphaned jobs list saved to ${outputFile}"
    }
} else if (options.c) {
    // Find circular dependencies
    println "Finding circular dependencies"
    
    List<List<String>> cycles = manager.findCircularDependencies()
    
    println "Circular Dependencies (${cycles.size()}):"
    if (cycles.isEmpty()) {
        println "None"
    } else {
        cycles.eachWithIndex { cycle, index ->
            println "${index + 1}. ${cycle.join(' → ')} → ${cycle[0]}"
        }
    }
    
    if (outputFile) {
        new File(outputFile).withWriter { writer ->
            cycles.eachWithIndex { cycle, index ->
                writer.writeLine("${index + 1}. ${cycle.join(' → ')} → ${cycle[0]}")
            }
        }
        println "\nCircular dependencies saved to ${outputFile}"
    }
} else if (options.D) {
    // Generate dependency diagram
    println "Generating complete dependency diagram"
    
    def completeGraph = manager.getCompleteDependencyGraph()
    String dotContent = completeGraph.toDotFormat()
    
    if (outputFile) {
        new File(outputFile).text = dotContent
        println "Dependency diagram saved in DOT format to ${outputFile}"
        println "You can visualize this diagram using tools like Graphviz:"
        println "  dot -Tpng ${outputFile} -o dependencies.png"
    } else {
        println "Dependency Diagram (DOT format):"
        println dotContent
    }
} else {
    // Default to showing a summary
    println "Jenkins Job Dependency Summary"
    
    def completeGraph = manager.getCompleteDependencyGraph()
    int totalJobs = completeGraph.jobGraphs.size()
    
    List<String> orphanedJobs = manager.findOrphanedJobs()
    List<List<String>> cycles = manager.findCircularDependencies()
    
    println "Total Jobs: ${totalJobs}"
    println "Orphaned Jobs: ${orphanedJobs.size()}"
    println "Circular Dependencies: ${cycles.size()}"
    
    println "\nTo view detailed information, use one of the following options:"
    println "- Analyze a specific job: --job JOB_NAME"
    println "- List orphaned jobs: --orphaned"
    println "- List circular dependencies: --circular"
    println "- Generate dependency diagram: --diagram --output FILE"
}