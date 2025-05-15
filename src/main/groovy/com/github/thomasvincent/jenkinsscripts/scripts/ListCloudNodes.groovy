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

import com.github.thomasvincent.jenkinsscripts.cloud.CloudNodesManager
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Lists Jenkins cloud nodes with detailed status information.
 * 
 * '''Usage:'''
 * ```groovy
 * # List all cloud nodes
 * ./ListCloudNodes.groovy
 * 
 * # List all cloud nodes in JSON format
 * ./ListCloudNodes.groovy --json
 * 
 * # List specific cloud provider nodes
 * ./ListCloudNodes.groovy --aws
 * ./ListCloudNodes.groovy --kubernetes
 * ./ListCloudNodes.groovy --azure
 * 
 * # Get cloud stats only
 * ./ListCloudNodes.groovy --stats
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
def cli = new CliBuilder(usage: 'groovy ListCloudNodes [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    s(longOpt: 'stats', 'Show statistics only, not detailed node information')
    a(longOpt: 'aws', 'Show AWS EC2 nodes only')
    k(longOpt: 'kubernetes', 'Show Kubernetes nodes only')
    z(longOpt: 'azure', 'Show Azure VM nodes only')
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
 * Process all cloud providers or a specific one based on command line options.
 * 
 * <p>If a specific provider is requested (AWS, Kubernetes, Azure),
 * only nodes for that provider will be shown.</p>
 */
if (options.s) {
    /**
     * Show statistics only.
     * 
     * <p>Displays counts of nodes by cloud provider and status.</p>
     */
    def cloudManager = new CloudNodesManager(jenkins)
    def stats = cloudManager.getCloudNodeStats()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(stats))
    } else {
        // Output in human-readable format
        if (stats.isEmpty()) {
            println "No cloud nodes found"
        } else {
            println "Cloud Node Statistics:"
            println "======================"
            
            stats.each { cloudType, providerStats ->
                println "${cloudType}:"
                println "  Total: ${providerStats.total}"
                println "  Online: ${providerStats.online}"
                println "  Offline: ${providerStats.offline}"
                println ""
            }
            
            def totalNodes = stats.values().sum { it.total }
            println "Total cloud nodes across all providers: ${totalNodes}"
        }
    }
    return
}

/**
 * Process specific cloud provider(s) based on options.
 * 
 * <p>Creates appropriate manager instances and retrieves node information.</p>
 */
def results = [:]
    
if (options.a || (!options.k && !options.z)) {
    // Process AWS nodes
    def awsManager = new AWSNodeManager(jenkins)
    if (awsManager.isEC2CloudConfigured()) {
        results.aws = awsManager.getEC2NodesInfo()
    }
}

if (options.k || (!options.a && !options.z)) {
    // Process Kubernetes nodes
    def kubeManager = new KubernetesNodeManager(jenkins)
    if (kubeManager.isKubernetesCloudConfigured()) {
        results.kubernetes = kubeManager.getKubernetesNodesInfo()
    }
}

if (options.z || (!options.a && !options.k)) {
    // Process Azure nodes
    def azureManager = new AzureNodeManager(jenkins)
    if (azureManager.isAzureCloudConfigured()) {
        results.azure = azureManager.getAzureNodesInfo()
    }
}

/**
 * Output results in the requested format.
 * 
 * <p>Either as JSON or as formatted text.</p>
 */
if (options.j) {
    // Output in JSON format
    println JsonOutput.prettyPrint(JsonOutput.toJson(results))
} else {
    // Output in human-readable format
    def totalNodes = 0
    
    results.each { provider, nodes ->
        if (nodes.isEmpty()) {
            println "No ${provider} nodes found"
        } else {
            println "${provider.toUpperCase()} NODES:"
            println "=" * (provider.length() + 7)  // Underline the heading
            
            nodes.each { nodeInfo ->
                if (provider == 'aws') {
                    def awsManager = new AWSNodeManager(jenkins)
                    println awsManager.formatNodeInfo(nodeInfo)
                } else if (provider == 'kubernetes') {
                    def kubeManager = new KubernetesNodeManager(jenkins)
                    println kubeManager.formatNodeInfo(nodeInfo)
                } else if (provider == 'azure') {
                    def azureManager = new AzureNodeManager(jenkins)
                    println azureManager.formatNodeInfo(nodeInfo)
                }
                println "-" * 80  // Separator line for readability
            }
            
            println "Total ${provider} nodes: ${nodes.size()}"
            println ""
            
            totalNodes += nodes.size()
        }
    }
    
    if (totalNodes == 0) {
        println "No cloud nodes found in this Jenkins instance."
        println "You may need to install and configure cloud provider plugins:"
        println "- EC2 Plugin for AWS"
        println "- Kubernetes Plugin for Kubernetes"
        println "- Azure VM Agents Plugin for Azure"
    } else {
        println "Total cloud nodes across all providers: ${totalNodes}"
    }
}