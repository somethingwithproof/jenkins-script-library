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

import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import com.github.thomasvincent.jenkinsscripts.util.JenkinsEnvironment
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Cleans build history from Jenkins jobs with option to reset build numbers.
 *
 * '''Exit Codes:'''
 * - 0: Success
 * - 1: Job not found or cleaning failed
 * - 2: Invalid arguments
 *
 * '''Usage:'''
 * ```groovy
 * # Clean job history with default settings (100 builds)
 * ./CleanBuildHistory.groovy my-jenkins-job
 *
 * # Clean 50 builds and reset build number to 1
 * ./CleanBuildHistory.groovy --reset --limit 50 my-jenkins-job
 *
 * # Use larger batch size for jobs with many builds (better memory usage)
 * ./CleanBuildHistory.groovy --batch 100 --limit 1000 my-jenkins-job
 *
 * # Dry run - show what would be deleted without actually deleting
 * ./CleanBuildHistory.groovy --dry-run my-jenkins-job
 *
 * # Show help
 * ./CleanBuildHistory.groovy --help
 * ```
 *
 * @author Thomas Vincent
 * @since 1.0
 */

// Exit codes for automation integration
final int EXIT_SUCCESS = 0
final int EXIT_FAILURE = 1
final int EXIT_INVALID_ARGS = 2

/**
 * Define command line options for the script.
 *
 * This creates a command-line interface with options for help,
 * reset build numbers, batch size, and limiting the number of builds to clean.
 */
def cli = new CliBuilder(usage: 'groovy CleanBuildHistory [options] jobName',
                          header: 'Options:',
                          footer: '\nExit codes: 0=success, 1=failure, 2=invalid arguments')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    r(longOpt: 'reset', 'Reset build number to 1 after cleaning')
    l(longOpt: 'limit', args: 1, argName: 'count', type: Integer,
      'Maximum number of builds to clean (default: 100)')
    b(longOpt: 'batch', args: 1, argName: 'size', type: Integer,
      'Number of builds to process per batch for memory efficiency (default: 25)')
    d(longOpt: 'dry-run', 'Show what would be deleted without actually deleting')
    v(longOpt: 'verbose', 'Enable verbose output')
    q(longOpt: 'quiet', 'Suppress non-error output')
}

/**
 * Helper method to output messages respecting quiet mode.
 */
def output = { String msg, boolean quiet -> if (!quiet) println msg }

/**
 * Parse the command line arguments.
 *
 * @param args The arguments passed to the script
 * @return The parsed options or null if parsing failed
 */
def options = cli.parse(args)
if (!options) {
    System.exit(EXIT_INVALID_ARGS)
    return
}

/**
 * Show help and exit if requested with the --help or -h option.
 */
if (options.h) {
    cli.usage()
    System.exit(EXIT_SUCCESS)
    return
}

/**
 * Get remaining arguments (job name) after parsing options.
 * The job name is a required parameter and must be provided.
 */
def extraArgs = options.arguments()
if (!extraArgs) {
    System.err.println "Error: Job name is required"
    cli.usage()
    System.exit(EXIT_INVALID_ARGS)
    return
}

/**
 * Extract and process command line arguments.
 *
 * @param extraArgs The remaining arguments after options are parsed
 * @param options The parsed command line options
 */
def jobName = extraArgs[0]
def resetBuildNumber = options.r ?: false
def buildTotal = options.l ? options.l as Integer : 100
def batchSize = options.b ? options.b as Integer : 25
def dryRun = options.d ?: false
def verbose = options.v ?: false
def quiet = options.q ?: false

// Validate batch size
if (batchSize < 1 || batchSize > 1000) {
    System.err.println "Error: Batch size must be between 1 and 1000"
    System.exit(EXIT_INVALID_ARGS)
    return
}

// Validate build total
if (buildTotal < 1) {
    System.err.println "Error: Limit must be a positive number"
    System.exit(EXIT_INVALID_ARGS)
    return
}

/**
 * Display the configuration being used for the cleaning operation.
 * This provides feedback to the user about what will be done.
 */
if (!quiet) {
    println "Cleaning build history for job: ${jobName}"
    println "Reset build number: ${resetBuildNumber}"
    println "Max builds to clean: ${buildTotal}"
    println "Batch size: ${batchSize}"
    if (dryRun) {
        println "*** DRY RUN MODE - No changes will be made ***"
    }
}

/**
 * Get the Jenkins instance - works in both CLI and Script Console environments.
 */
def jenkins = JenkinsEnvironment.getJenkinsInstance()

/**
 * Create and run job cleaner.
 *
 * @param jenkins The Jenkins instance
 * @param jobName The name of the job to clean
 * @param resetBuildNumber Whether to reset the build number to 1 after cleaning
 * @param batchSize Number of builds to clean in each batch
 * @param buildTotal Maximum number of builds to clean
 */
def cleaner = new JobCleaner(jenkins, jobName, resetBuildNumber, batchSize, buildTotal)

// Set dry run mode if requested
if (dryRun) {
    cleaner.setDryRun(true)
}

// Set verbose mode if requested
if (verbose) {
    cleaner.setVerbose(true)
}

def result = cleaner.clean()

/**
 * Print the result and exit with appropriate code.
 */
if (result) {
    output("Successfully cleaned build history for job: ${jobName}", quiet)
    if (dryRun && !quiet) {
        println "Note: Dry run completed - no builds were actually deleted"
    }
    System.exit(EXIT_SUCCESS)
} else {
    System.err.println "Failed to clean build history for job: ${jobName}"
    System.exit(EXIT_FAILURE)
}