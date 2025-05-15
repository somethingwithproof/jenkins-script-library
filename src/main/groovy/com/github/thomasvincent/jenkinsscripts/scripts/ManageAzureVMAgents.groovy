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

import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Manages Azure VM agent nodes in Jenkins.
 * 
 * '''Usage:'''
 * ```groovy
 * # List all Azure VM nodes
 * ./ManageAzureVMAgents.groovy --list
 * 
 * # List all Azure VM templates
 * ./ManageAzureVMAgents.groovy --templates
 * 
 * # Provision a new Azure VM using a template
 * ./ManageAzureVMAgents.groovy --provision "Windows VM"
 * 
 * # Clean up an Azure VM
 * ./ManageAzureVMAgents.groovy --cleanup jenkins-agent-win1
 * 
 * # Output in JSON format
 * ./ManageAzureVMAgents.groovy --list --json
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
def cli = new CliBuilder(usage: 'groovy ManageAzureVMAgents [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    l(longOpt: 'list', 'List all Azure VM nodes')
    t(longOpt: 'templates', 'List all Azure VM templates')
    p(longOpt: 'provision', args: 1, argName: 'template', 'Provision a new Azure VM using the specified template')
    c(longOpt: 'cloud', args: 1, argName: 'name', 'Specify Azure cloud name (for provision)')
    u(longOpt: 'cleanup', args: 1, argName: 'nodeName', 'Clean up the specified Azure VM')
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
 * Create an AzureNodeManager to work with Azure VMs.
 * 
 * <p>The AzureNodeManager handles operations related to Azure VM agents.</p>
 */
def azureManager = new AzureNodeManager(jenkins)

/**
 * Check if Azure VM cloud plugin is configured.
 * 
 * <p>Verify the environment before attempting operations.</p>
 */
if (!azureManager.isAzureCloudConfigured()) {
    println "Error: Azure VM cloud is not configured in this Jenkins instance."
    println "Please make sure the Azure VM Agents Plugin is installed and configured correctly."
    return
}

/**
 * Handle the requested operation.
 * 
 * <p>Perform the operation specified by the command-line arguments.</p>
 */
if (options.l) {
    /**
     * List all Azure VM nodes.
     * 
     * <p>Retrieves and displays information about all Azure VM agent nodes.</p>
     */
    def nodes = azureManager.getAzureNodesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(nodes))
    } else {
        // Output in human-readable format
        if (nodes.isEmpty()) {
            println "No Azure VM nodes found"
        } else {
            nodes.each { nodeInfo ->
                println azureManager.formatNodeInfo(nodeInfo)
                println "-" * 80  // Separator line for readability
            }
            println "Total: ${nodes.size()} Azure VM nodes"
        }
    }
} else if (options.t) {
    /**
     * List all Azure VM templates.
     * 
     * <p>Retrieves and displays information about all Azure VM templates.</p>
     */
    def templates = azureManager.getAzureTemplatesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(templates))
    } else {
        // Output in human-readable format
        if (templates.isEmpty()) {
            println "No Azure VM templates found"
        } else {
            println "AZURE VM TEMPLATES:"
            println "==================="
            
            templates.each { template ->
                println "Template: ${template.templateName}"
                println "  Labels: ${template.labels}"
                println "  Location: ${template.location}"
                println "  VM Size: ${template.vmSize}"
                println "  OS Type: ${template.osType}"
                println "  Image Type: ${template.imageTopLevelType}"
                println "  Launch Method: ${template.launchMethod}"
                println "  Storage Type: ${template.storageAccountType}"
                println "  Disk Type: ${template.diskType}"
                println "  Executors: ${template.noOfParallelJobs}"
                println "  Usage Mode: ${template.usageMode ?: 'Normal'}"
                println "  Retention Strategy: ${template.retentionStrategy ?: 'Default'}"
                println ""
            }
            
            println "Total: ${templates.size()} Azure VM templates"
        }
    }
} else if (options.p) {
    /**
     * Provision a new Azure VM.
     * 
     * <p>Creates a new Azure VM using the specified template.</p>
     */
    def templateName = options.p
    def cloudName = options.c
    
    def result = azureManager.provisionNewVM(templateName, cloudName)
    
    if (result) {
        println "Successfully initiated provisioning of new Azure VM"
        println "Template: ${templateName}"
        if (cloudName) {
            println "Cloud: ${cloudName}"
        }
        println ""
        println "Note: The VM may take a few minutes to appear in Jenkins"
    } else {
        println "Failed to provision new Azure VM"
        println "Please check the template name and credentials"
    }
} else if (options.u) {
    /**
     * Clean up an Azure VM.
     * 
     * <p>Cleans up resources for the specified Azure VM.</p>
     */
    def nodeName = options.u
    
    def result = azureManager.cleanupVM(nodeName)
    
    if (result) {
        println "Successfully initiated cleanup of Azure VM: ${nodeName}"
        println "Note: It may take a few minutes for the VM to be fully cleaned up"
    } else {
        println "Failed to clean up Azure VM: ${nodeName}"
        println "Please check the node name and make sure the VM still exists"
    }
} else {
    /**
     * If no operation is specified, display usage information.
     * 
     * <p>Shows the available options if no command is provided.</p>
     */
    cli.usage()
}