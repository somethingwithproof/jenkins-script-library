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

import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Manages Kubernetes agent nodes in Jenkins.
 * 
 * '''Usage:'''
 * ```groovy
 * # List all Kubernetes pods
 * ./ManageKubernetesAgents.groovy --list
 * 
 * # List all pod templates
 * ./ManageKubernetesAgents.groovy --templates
 * 
 * # Provision a new pod using a template
 * ./ManageKubernetesAgents.groovy --provision "jenkins-agent"
 * 
 * # Terminate a pod by name
 * ./ManageKubernetesAgents.groovy --terminate jenkins-agent-xyz123
 * 
 * # Output in JSON format
 * ./ManageKubernetesAgents.groovy --list --json
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
def cli = new CliBuilder(usage: 'groovy ManageKubernetesAgents [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    l(longOpt: 'list', 'List all Kubernetes pods')
    t(longOpt: 'templates', 'List all pod templates')
    p(longOpt: 'provision', args: 1, argName: 'template', 'Provision a new pod using the specified template')
    c(longOpt: 'cloud', args: 1, argName: 'name', 'Specify Kubernetes cloud name (for provision)')
    x(longOpt: 'terminate', args: 1, argName: 'podName', 'Terminate the specified pod')
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
 * Create a KubernetesNodeManager to work with Kubernetes pods.
 * 
 * <p>The KubernetesNodeManager handles operations related to Kubernetes agents.</p>
 */
def kubeManager = new KubernetesNodeManager(jenkins)

/**
 * Check if Kubernetes cloud plugin is configured.
 * 
 * <p>Verify the environment before attempting operations.</p>
 */
if (!kubeManager.isKubernetesCloudConfigured()) {
    println "Error: Kubernetes cloud is not configured in this Jenkins instance."
    println "Please make sure the Kubernetes Plugin is installed and configured correctly."
    return
}

/**
 * Handle the requested operation.
 * 
 * <p>Perform the operation specified by the command-line arguments.</p>
 */
if (options.l) {
    /**
     * List all Kubernetes pods.
     * 
     * <p>Retrieves and displays information about all Kubernetes agent nodes.</p>
     */
    def nodes = kubeManager.getKubernetesNodesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(nodes))
    } else {
        // Output in human-readable format
        if (nodes.isEmpty()) {
            println "No Kubernetes nodes found"
        } else {
            nodes.each { nodeInfo ->
                println kubeManager.formatNodeInfo(nodeInfo)
                println "-" * 80  // Separator line for readability
            }
            println "Total: ${nodes.size()} Kubernetes pods"
        }
    }
} else if (options.t) {
    /**
     * List all pod templates.
     * 
     * <p>Retrieves and displays information about all pod templates.</p>
     */
    def templates = kubeManager.getPodTemplatesInfo()
    
    if (options.j) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(templates))
    } else {
        // Output in human-readable format
        if (templates.isEmpty()) {
            println "No pod templates found"
        } else {
            println "KUBERNETES POD TEMPLATES:"
            println "========================="
            
            templates.each { template ->
                println "Template: ${template.name}"
                println "  Label: ${template.label}"
                println "  Namespace: ${template.namespace ?: 'default'}"
                println "  Node Usage: ${template.nodeUsageMode ?: 'Normal'}"
                println "  Service Account: ${template.serviceAccount ?: 'default'}"
                println "  Idle Minutes: ${template.idleMinutes ?: 'Not set'}"
                println "  Connect Timeout: ${template.slaveConnectTimeout ?: 'Not set'}"
                
                if (template.containers) {
                    println "  Containers:"
                    template.containers.each { container ->
                        println "    - Name: ${container.name}"
                        println "      Image: ${container.image}"
                        if (container.workingDir) {
                            println "      Working Dir: ${container.workingDir}"
                        }
                        if (container.command) {
                            println "      Command: ${container.command}"
                        }
                        if (container.args) {
                            println "      Args: ${container.args}"
                        }
                    }
                }
                
                println ""
            }
            
            println "Total: ${templates.size()} pod templates"
        }
    }
} else if (options.p) {
    /**
     * Provision a new Kubernetes pod.
     * 
     * <p>Creates a new pod using the specified template.</p>
     */
    def templateLabel = options.p
    def cloudName = options.c
    
    def result = kubeManager.provisionNewPod(templateLabel, cloudName)
    
    if (result) {
        println "Successfully initiated provisioning of new Kubernetes pod"
        println "Template: ${templateLabel}"
        if (cloudName) {
            println "Cloud: ${cloudName}"
        }
        println ""
        println "Note: The pod may take a few moments to appear in Jenkins"
    } else {
        println "Failed to provision new Kubernetes pod"
        println "Please check the template label and credentials"
    }
} else if (options.x) {
    /**
     * Terminate a Kubernetes pod.
     * 
     * <p>Terminates the specified pod.</p>
     */
    def podName = options.x
    
    def result = kubeManager.terminatePod(podName)
    
    if (result) {
        println "Successfully initiated termination of Kubernetes pod: ${podName}"
        println "Note: It may take a few moments for the pod to be fully terminated"
    } else {
        println "Failed to terminate Kubernetes pod: ${podName}"
        println "Please check the pod name and make sure the pod is still active"
    }
} else {
    /**
     * If no operation is specified, display usage information.
     * 
     * <p>Shows the available options if no command is provided.</p>
     */
    cli.usage()
}