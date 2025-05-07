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

import com.github.thomasvincent.jenkinsscripts.nodes.ComputerLauncher
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Starts offline Jenkins slave nodes individually or in bulk.
 * 
 * '''Usage:'''
 * ```groovy
 * # Start a specific offline node
 * ./StartOfflineSlaveNodes.groovy worker-01
 * 
 * # Start all offline slave nodes
 * ./StartOfflineSlaveNodes.groovy --all
 * 
 * # Show help
 * ./StartOfflineSlaveNodes.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

/**
 * Initialize the command line argument parser with appropriate options and help text.
 */
def cli = new CliBuilder(
    usage: 'groovy StartOfflineSlaveNodes [options] [computerName]',
    header: 'Options:'
)
cli.with {
    h(longOpt: 'help', 'Show usage information')
    a(longOpt: 'all', 'Start all offline slave nodes')
}

/**
 * Parse the command line arguments.
 * Exit early if parsing fails.
 */
def options = cli.parse(args)
if (!options) {
    return
}

/**
 * Display help information if requested and exit.
 */
if (options.h) {
    cli.usage()
    return
}

/**
 * Extract the computer name from the remaining arguments, if provided.
 */
def extraArgs = options.arguments()
def computerName = extraArgs ? extraArgs[0] : null

/**
 * Validate that either a specific computer name or the "all" flag is provided.
 * Display an error message and usage information if neither is present.
 */
if (!options.a && !computerName) {
    println "Error: Either specify a computer name or use --all to start all offline nodes"
    cli.usage()
    return
}

/**
 * Get the current Jenkins instance to interact with the Jenkins API.
 */
def jenkins = Jenkins.get()

/**
 * Create a ComputerLauncher instance to handle the node launching logic.
 * This utility encapsulates the details of connecting to offline nodes.
 */
def launcher = new ComputerLauncher(jenkins)

/**
 * Execute the requested operation based on the provided arguments.
 */
if (options.a) {
    // Start all offline nodes
    println "Starting all offline slave nodes..."
    def count = launcher.startAllOfflineComputers()
    println "Started ${count} offline slave nodes"
} else {
    // Start a specific node
    println "Starting computer: ${computerName}..."
    def result = launcher.startComputer(computerName)
    if (result) {
        println "Successfully started computer: ${computerName}"
    } else {
        println "Failed to start computer: ${computerName}"
    }
}