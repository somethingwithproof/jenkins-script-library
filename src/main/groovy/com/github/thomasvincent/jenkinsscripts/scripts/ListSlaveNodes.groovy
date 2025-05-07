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

import com.github.thomasvincent.jenkinsscripts.nodes.SlaveInfoManager
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Lists Jenkins slave nodes with detailed status information.
 * 
 * '''Usage:'''
 * ```groovy
 * # List all slave nodes
 * ./ListSlaveNodes.groovy
 * 
 * # List all slave nodes in JSON format
 * ./ListSlaveNodes.groovy --json
 * 
 * # Get details about a specific slave node
 * ./ListSlaveNodes.groovy my-slave-node-name
 * 
 * # Get details about a specific node in JSON format
 * ./ListSlaveNodes.groovy --json my-slave-node-name
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

/**
 * Creates a command-line argument parser for the script.
 * 
 * <p>Configures the available command-line options that can be used
 * when running this script, including help, output format options,
 * and operation modes.</p>
 */
def cli = new CliBuilder(usage: 'groovy ListSlaveNodes [options] [slaveName]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    a(longOpt: 'all', 'List all slaves (default if no slave name is provided)')
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
 * Extract the slave node name from command-line arguments if provided.
 * 
 * <p>If a specific slave node name is provided as an argument, it will be used
 * to get information about that node. Otherwise, information about all slave
 * nodes will be returned.</p>
 */
def extraArgs = options.arguments()
def slaveName = extraArgs ? extraArgs[0] : null

/**
 * Get the current Jenkins instance.
 * 
 * <p>This provides access to the Jenkins API for retrieving node information.</p>
 */
def jenkins = Jenkins.get()

/**
 * Create a manager for handling slave node information.
 * 
 * <p>The SlaveInfoManager class encapsulates the logic for retrieving and
 * formatting slave node information.</p>
 */
def slaveInfoManager = new SlaveInfoManager(jenkins)

/**
 * Execute the requested operation based on command-line arguments.
 * 
 * <p>If a specific slave name is provided, information for that slave is returned.
 * Otherwise, information for all slave nodes is listed.</p>
 */
if (slaveName) {
    /**
     * Retrieve and display information for a specific slave node.
     * 
     * <p>Fetches detailed information about the specified slave node
     * and outputs it in the requested format (JSON or formatted text).</p>
     */
    def slaveInfo = slaveInfoManager.getSlaveInfo(slaveName)
    
    if (slaveInfo) {
        if (options.j) {
            // Output in JSON format for machine processing
            println JsonOutput.prettyPrint(JsonOutput.toJson(slaveInfo))
        } else {
            // Output in human-readable format
            println slaveInfoManager.formatSlaveInfo(slaveInfo)
        }
    } else {
        println "Slave node not found: ${slaveName}"
    }
} else {
    /**
     * List information for all slave nodes.
     * 
     * <p>Retrieves information about all slave nodes registered in Jenkins
     * and outputs it in the requested format (JSON or formatted text).</p>
     */
    def allSlaves = slaveInfoManager.listAllSlaves()
    
    if (options.j) {
        // Output in JSON format for machine processing
        println JsonOutput.prettyPrint(JsonOutput.toJson(allSlaves))
    } else {
        // Output in human-readable format
        if (allSlaves.isEmpty()) {
            println "No slave nodes found"
        } else {
            allSlaves.each { slaveInfo ->
                println slaveInfoManager.formatSlaveInfo(slaveInfo)
                println "-" * 80  // Separator line for readability
            }
            println "Total: ${allSlaves.size()} slave nodes"
        }
    }
}