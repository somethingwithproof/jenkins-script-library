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
import hudson.security.Permission
import java.util.logging.Logger

/**
 * Disables all buildable jobs in a Jenkins instance securely and responsibly.
 * 
 * '''Usage:'''
 * ```groovy
 * # Disable all buildable jobs
 * ./DisableJobTriggers.groovy
 * 
 * # Disable specific jobs (comma-separated)
 * ./DisableJobTriggers.groovy --jobs job1,job2,job3
 * 
 * # Disable jobs with a pattern match
 * ./DisableJobTriggers.groovy --pattern "test-.*"
 * 
 * # Show help
 * ./DisableJobTriggers.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

/**
 * Logger for this script
 */
private static final Logger LOGGER = Logger.getLogger("DisableJobTriggers.groovy")

/**
 * Initialize the command line argument parser with appropriate options
 */
def cli = new CliBuilder(usage: 'groovy DisableJobTriggers [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'jobs', args: 1, argName: 'jobsList', 'Comma-separated list of job names to disable')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Regex pattern to match job names')
    d(longOpt: 'dry-run', 'List jobs that would be disabled without actually disabling them')
}

/**
 * Parse command line arguments
 */
def options = cli.parse(args)
if (!options) {
    return
}

/**
 * Show help if requested
 */
if (options.h) {
    cli.usage()
    return
}

/**
 * Get Jenkins instance and verify admin permissions
 */
def jenkins = Jenkins.get()
if (!jenkins.hasPermission(Permission.ADMINISTER)) {
    LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
    println "Error: You need administrative privileges to run this script"
    return
}

/**
 * Create job disabler with appropriate options
 */
def jobDisabler = new JobDisabler(jenkins)

/**
 * Set up the filters based on command line options
 */
if (options.j) {
    // Parse comma-separated job list
    def jobNames = options.j.split(",").collect { it.trim() }
    println "Targeting specific jobs: ${jobNames.join(', ')}"
    jobDisabler.withJobNames(jobNames)
} else if (options.p) {
    // Use pattern matching
    println "Targeting jobs matching pattern: ${options.p}"
    jobDisabler.withPattern(options.p)
} else {
    // Default to all jobs
    println "Targeting all buildable jobs"
}

/**
 * Execute the job disablement
 */
if (options.d) {
    // Dry run mode - just list the jobs that would be disabled
    def jobsToDisable = jobDisabler.findJobsToDisable()
    println "\nJobs that would be disabled in dry-run mode:"
    jobsToDisable.each { job ->
        println "- ${job.fullName}"
    }
    println "\nTotal: ${jobsToDisable.size()} jobs would be disabled"
} else {
    // Actually disable the jobs
    def count = jobDisabler.disableJobs()
    println "\nDisabled ${count} jobs successfully"
}