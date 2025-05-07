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

import com.github.thomasvincent.jenkinsscripts.jobs.JobDisabler
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Disables Jenkins jobs individually or in bulk.
 * 
 * '''Usage:'''
 * ```groovy
 * # Disable a specific job
 * ./DisableJobs.groovy my-jenkins-job
 * 
 * # Disable all buildable jobs 
 * ./DisableJobs.groovy --all
 * 
 * # Show help
 * ./DisableJobs.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy DisableJobs [options] [jobName]',
                          header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    a(longOpt: 'all', 'Disable all buildable jobs')
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

// Get remaining arguments (job name)
def extraArgs = options.arguments()

// Check for valid arguments
if (!options.a && !extraArgs) {
    println "Error: Either specify a job name or use --all to disable all jobs"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create job disabler
def jobDisabler = new JobDisabler(jenkins)

// Execute requested operation
if (options.a) {
    println "Disabling all buildable jobs..."
    def count = jobDisabler.disableAllJobs()
    println "Successfully disabled ${count} jobs"
} else {
    def jobName = extraArgs[0]
    println "Disabling job: ${jobName}..."
    def result = jobDisabler.disableJob(jobName)
    if (result) {
        println "Successfully disabled job: ${jobName}"
    } else {
        println "Failed to disable job: ${jobName}"
    }
}