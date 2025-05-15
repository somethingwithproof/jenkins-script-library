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

import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Manages AWS EC2 agent nodes in Jenkins.
 * 
 * '''Usage:'''
 * ```groovy
 * # List all EC2 instances
 * ./ManageEC2Agents.groovy --list
 * 
 * # List all EC2 templates
 * ./ManageEC2Agents.groovy --templates
 * 
 * # Provision a new EC2 instance using a template
 * ./ManageEC2Agents.groovy --provision "My EC2 Template"
 * 
 * # Terminate an EC2 instance by ID
 * ./ManageEC2Agents.groovy --terminate i-1234567890abcdef0
 * 
 * # Output in JSON format
 * ./ManageEC2Agents.groovy --list --json
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */

/**
 * Creates a command-line argument parser for the script.
 * 
 * <p>Configures the available command-line options that can be used
 * when running this script, including help, output format options,
 * and operation modes.</p>
 */
def cli = new CliBuilder(usage: 'groovy ManageEC2Agents [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    l(longOpt: 'list', 'List all EC2 instances')
    t(longOpt: 'templates', 'List all EC2 templates')
    p(longOpt: 'provision', args: 1, argName: 'template', 'Provision a new EC2 instance using the specified template')
    c(longOpt: 'cloud', args: 1, argName: 'name', 'Specify EC2 cloud name (for provision)')
    x(longOpt: 'terminate', args: 1, argName: 'instanceId', 'Terminate the specified EC2 instance')
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
 * Get the current Jenkins instance.
 * 
 * <p>This provides access to the Jenkins API for retrieving node information.</p>
 */
def jenkins = Jenkins.get()

/**
 * Create an AWSNodeManager to work with EC2 instances.
 * 
 * <p>The AWSNodeManager handles operations related to EC2 agents.</p>
 */
def awsManager = new AWSNodeManager(jenkins)

/**
 * Check if EC2 cloud plugin is configured.
 * 
 * <p>Verify the environment before attempting operations.</p>
 */
if (!awsManager.isEC2CloudConfigured()) {
    println "Error: EC2 cloud is not configured in this Jenkins instance."
    println "Please make sure the EC2 Plugin is installed and configured correctly."
    return
}

/**
 * Handle the requested operation.
 * 
 * <p>Perform the operation specified by the command-line arguments.</p>
 */
if (options.l) {
    /**
     * List all EC2 instances.
     * 
     * <p>Retrieves and displays information about all EC2 agent nodes.</p>
     */
    def nodes = awsManager.getEC2NodesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(nodes))
    } else {
        // Output in human-readable format
        if (nodes.isEmpty()) {
            println "No EC2 nodes found"
        } else {
            nodes.each { nodeInfo ->
                println awsManager.formatNodeInfo(nodeInfo)
                println "-" * 80  // Separator line for readability
            }
            println "Total: ${nodes.size()} EC2 nodes"
        }
    }
} else if (options.t) {
    /**
     * List all EC2 templates.
     * 
     * <p>Retrieves and displays information about all EC2 templates.</p>
     */
    def templates = awsManager.getEC2TemplatesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(templates))
    } else {
        // Output in human-readable format
        if (templates.isEmpty()) {
            println "No EC2 templates found"
        } else {
            println "EC2 TEMPLATES:"
            println "=============="
            
            templates.each { template ->
                println "Template: ${template.description}"
                println "  AMI: ${template.ami}"
                println "  Instance Type: ${template.instanceType}"
                println "  Labels: ${template.labels}"
                println "  Executors: ${template.numExecutors}"
                println "  Remote FS: ${template.remoteFS}"
                println "  Security Groups: ${template.securityGroups}"
                println "  Spot Instance: ${template.spotInstance}"
                
                if (template.spotInstance && template.spotDetails) {
                    println "    Max Bid Price: ${template.spotDetails.maxBidPrice}"
                    println "    Fallback to On-Demand: ${template.spotDetails.fallbackToOnDemand}"
                }
                
                println ""
            }
            
            println "Total: ${templates.size()} EC2 templates"
        }
    }
} else if (options.p) {
    /**
     * Provision a new EC2 instance.
     * 
     * <p>Creates a new EC2 instance using the specified template.</p>
     */
    def templateDescription = options.p
    def cloudName = options.c
    
    def result = awsManager.provisionNewInstance(templateDescription, cloudName)
    
    if (result) {
        println "Successfully initiated provisioning of new EC2 instance"
        println "Template: ${templateDescription}"
        if (cloudName) {
            println "Cloud: ${cloudName}"
        }
        println ""
        println "Note: The instance may take a few minutes to appear in Jenkins"
    } else {
        println "Failed to provision new EC2 instance"
        println "Please check the template name and credentials"
    }
} else if (options.x) {
    /**
     * Terminate an EC2 instance.
     * 
     * <p>Terminates the specified EC2 instance.</p>
     */
    def instanceId = options.x
    
    def result = awsManager.terminateInstance(instanceId)
    
    if (result) {
        println "Successfully initiated termination of EC2 instance: ${instanceId}"
        println "Note: It may take a few minutes for the instance to be fully terminated"
    } else {
        println "Failed to terminate EC2 instance: ${instanceId}"
        println "Please check the instance ID and make sure the instance is still active"
    }
} else {
    /**
     * If no operation is specified, display usage information.
     * 
     * <p>Shows the available options if no command is provided.</p>
     */
    cli.usage()
}