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
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Cleans build history from Jenkins jobs with option to reset build numbers.
 * 
 * '''Usage:'''
 * ```groovy
 * # Clean job history with default settings (100 builds)
 * ./CleanBuildHistory.groovy my-jenkins-job
 * 
 * # Clean 50 builds and reset build number to 1
 * ./CleanBuildHistory.groovy --reset --limit 50 my-jenkins-job
 * 
 * # Show help
 * ./CleanBuildHistory.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

/**
 * Define command line options for the script.
 * 
 * This creates a command-line interface with options for help,
 * reset build numbers, and limiting the number of builds to clean.
 */
def cli = new CliBuilder(usage: 'groovy CleanBuildHistory [options] jobName',
                          header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    r(longOpt: 'reset', 'Reset build number to 1 after cleaning')
    l(longOpt: 'limit', args: 1, argName: 'limit', type: Integer, 'Maximum number of builds to clean (default: 100)')
}

/**
 * Parse the command line arguments.
 * 
 * @param args The arguments passed to the script
 * @return The parsed options or null if parsing failed
 */
def options = cli.parse(args)
if (!options) {
    return
}

/**
 * Show help and exit if requested with the --help or -h option.
 */
if (options.h) {
    cli.usage()
    return
}

/**
 * Get remaining arguments (job name) after parsing options.
 * The job name is a required parameter and must be provided.
 */
def extraArgs = options.arguments()
if (!extraArgs) {
    println "Error: Job name is required"
    cli.usage()
    return
}

/**
 * Extract and process command line arguments.
 * 
 * @param extraArgs The remaining arguments after options are parsed
 * @param options The parsed command line options
 */
def jobName = extraArgs[0]
def resetBuildNumber = options.r
def buildTotal = options.l ? options.l : 100

/**
 * Display the configuration being used for the cleaning operation.
 * This provides feedback to the user about what will be done.
 */
println "Cleaning build history for job: ${jobName}"
println "Reset build number: ${resetBuildNumber}"
println "Max builds to clean: ${buildTotal}"

/**
 * Get the Jenkins instance to interact with the Jenkins server.
 */
def jenkins = Jenkins.get()

/**
 * Create and run job cleaner.
 * 
 * @param jenkins The Jenkins instance
 * @param jobName The name of the job to clean
 * @param resetBuildNumber Whether to reset the build number to 1 after cleaning
 * @param batchSize Number of builds to clean in each batch (default: 25)
 * @param buildTotal Maximum number of builds to clean
 */
def cleaner = new JobCleaner(jenkins, jobName, resetBuildNumber, 25, buildTotal)
def result = cleaner.clean()

/**
 * Print the result of the cleaning operation.
 */
if (result) {
    println "Successfully cleaned build history for job: ${jobName}"
} else {
    println "Failed to clean build history for job: ${jobName}"
}