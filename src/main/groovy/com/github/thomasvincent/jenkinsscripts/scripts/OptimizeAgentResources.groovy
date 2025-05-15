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

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import jenkins.model.Jenkins
import hudson.model.Queue
import hudson.model.Computer
import hudson.model.Label
import hudson.model.Node
import hudson.model.LoadStatistics
import jenkins.metrics.api.Metrics
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

import java.util.logging.Level
import java.util.logging.Logger
import java.text.SimpleDateFormat

/**
 * Analyzes and optimizes Jenkins agent resource utilization.
 * 
 * '''Usage:'''
 * ```groovy
 * # Analyze current resource utilization
 * ./OptimizeAgentResources.groovy --analyze
 * 
 * # Scale agents based on demand (auto-scaling)
 * ./OptimizeAgentResources.groovy --scale
 * 
 * # Recommend optimizations
 * ./OptimizeAgentResources.groovy --recommend
 * 
 * # Scale only specific cloud providers
 * ./OptimizeAgentResources.groovy --scale --provider aws
 * ./OptimizeAgentResources.groovy --scale --provider kubernetes
 * ./OptimizeAgentResources.groovy --scale --provider azure
 * 
 * # Set minimum and maximum agent counts
 * ./OptimizeAgentResources.groovy --scale --min 3 --max 10
 * 
 * # Output in JSON format
 * ./OptimizeAgentResources.groovy --analyze --json
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */

/**
 * Logger for this script.
 */
private static final Logger LOGGER = Logger.getLogger("OptimizeAgentResources.groovy")

/**
 * Creates a command-line argument parser for the script.
 * 
 * <p>Configures the available command-line options that can be used
 * when running this script.</p>
 */
def cli = new CliBuilder(usage: 'groovy OptimizeAgentResources [options]',
                        header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'json', 'Output in JSON format')
    a(longOpt: 'analyze', 'Analyze current resource utilization')
    s(longOpt: 'scale', 'Scale agents based on demand')
    r(longOpt: 'recommend', 'Recommend resource optimizations')
    p(longOpt: 'provider', args: 1, argName: 'name', 'Cloud provider to target (aws, kubernetes, azure, all)')
    m(longOpt: 'min', args: 1, argName: 'count', 'Minimum agent count per label')
    x(longOpt: 'max', args: 1, argName: 'count', 'Maximum agent count per label')
    t(longOpt: 'template', args: 1, argName: 'name', 'Template to use for scaling')
    l(longOpt: 'label', args: 1, argName: 'name', 'Label to target for scaling')
    d(longOpt: 'dry-run', 'Don\'t make changes, just show what would be done')
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
 * At least one action must be specified.
 */
if (!options.a && !options.s && !options.r) {
    println "Error: One of --analyze, --scale, or --recommend must be specified"
    cli.usage()
    return
}

/**
 * Get the current Jenkins instance.
 */
def jenkins = Jenkins.get()

/**
 * Parse parameters with defaults.
 */
def minAgents = options.min ? options.min as int : 0
def maxAgents = options.max ? options.max as int : 10
def targetProvider = options.provider ?: "all"
def targetLabel = options.label ?: ""
def dryRun = options.d ?: false
def templateName = options.template ?: ""

/**
 * Initialize cloud managers for the requested providers.
 */
def awsManager = targetProvider in ["all", "aws"] ? new AWSNodeManager(jenkins) : null
def kubeManager = targetProvider in ["all", "kubernetes"] ? new KubernetesNodeManager(jenkins) : null
def azureManager = targetProvider in ["all", "azure"] ? new AzureNodeManager(jenkins) : null

/**
 * Analyze current resource utilization.
 */
if (options.a) {
    def analysisData = analyzeResourceUtilization(jenkins)
    
    if (options.json) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(analysisData))
    } else {
        // Output in human-readable format
        println "=".multiply(80)
        println "JENKINS RESOURCE UTILIZATION ANALYSIS"
        println "=".multiply(80)
        println "\nSUMMARY:"
        println "  Total Executors: ${analysisData.executors.total}"
        println "  Busy Executors: ${analysisData.executors.busy} (${String.format("%.1f%%", analysisData.executors.utilizationPercent)})"
        println "  Idle Executors: ${analysisData.executors.idle}"
        println "  Offline Executors: ${analysisData.executors.offline}"
        println "  Queue Length: ${analysisData.queue.length}"
        println "  Average Wait Time: ${formatDuration(analysisData.queue.avgWaitTime)}"
        
        println "\nAGENT UTILIZATION:"
        analysisData.agents.each { agent ->
            println "  ${agent.name}:"
            println "    Status: ${agent.offline ? 'OFFLINE' : 'ONLINE'}"
            println "    Executors: ${agent.executors} (Busy: ${agent.busyExecutors})"
            println "    Utilization: ${String.format("%.1f%%", agent.utilizationPercent)}"
            if (agent.labels) {
                println "    Labels: ${agent.labels.join(', ')}"
            }
        }
        
        println "\nLABEL UTILIZATION:"
        analysisData.labels.each { label ->
            println "  ${label.name}:"
            println "    Agents: ${label.agentCount}"
            println "    Executors: ${label.executors} (Busy: ${label.busyExecutors})"
            println "    Utilization: ${String.format("%.1f%%", label.utilizationPercent)}"
            println "    Queued Items: ${label.queuedItems}"
        }
    }
}

/**
 * Scale agents based on demand.
 */
if (options.s) {
    def scalingActions = determineScalingActions(jenkins, targetLabel, minAgents, maxAgents)
    
    if (options.json) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(scalingActions))
    } else {
        // Output in human-readable format
        println "=".multiply(80)
        println "JENKINS AGENT SCALING ACTIONS"
        println "=".multiply(80)
        
        if (scalingActions.isEmpty()) {
            println "\nNo scaling actions needed at this time."
        } else {
            scalingActions.each { action ->
                println "\nLabel: ${action.label}"
                println "  Current Agents: ${action.currentAgents}"
                println "  Target Agents: ${action.targetAgents}"
                println "  Action: ${action.action.toUpperCase()} ${Math.abs(action.delta)} agent(s)"
                println "  Reason: ${action.reason}"
                
                if (!dryRun && action.action == "provision") {
                    performProvisioning(action.label, action.delta, templateName, awsManager, kubeManager, azureManager)
                } else if (!dryRun && action.action == "terminate") {
                    performTermination(action.label, -action.delta, awsManager, kubeManager, azureManager)
                }
            }
            
            if (dryRun) {
                println "\nDRY RUN: No changes were made"
            }
        }
    }
}

/**
 * Recommend optimizations.
 */
if (options.r) {
    def recommendations = generateRecommendations(jenkins)
    
    if (options.json) {
        // Output in JSON format
        println JsonOutput.prettyPrint(JsonOutput.toJson(recommendations))
    } else {
        // Output in human-readable format
        println "=".multiply(80)
        println "JENKINS RESOURCE OPTIMIZATION RECOMMENDATIONS"
        println "=".multiply(80)
        
        if (recommendations.isEmpty()) {
            println "\nNo optimization recommendations at this time."
        } else {
            recommendations.each { rec ->
                println "\n${rec.type.toUpperCase()} RECOMMENDATION:"
                println "  ${rec.description}"
                println "  Impact: ${rec.impact}"
                println "  Suggested Action: ${rec.action}"
            }
        }
    }
}

/**
 * Analyzes current Jenkins resource utilization.
 * 
 * @param jenkins The Jenkins instance
 * @return Map with analysis data
 */
def analyzeResourceUtilization(Jenkins jenkins) {
    return ErrorHandler.withErrorHandling("analyzing resource utilization", {
        def result = [:]
        
        // Analyze executors
        def totalExecutors = 0
        def busyExecutors = 0
        def offlineExecutors = 0
        jenkins.computers.each { computer ->
            if (computer.name == "master") return // Skip master
            
            def executorCount = computer.numExecutors
            totalExecutors += executorCount
            
            if (computer.offline) {
                offlineExecutors += executorCount
            } else {
                busyExecutors += computer.countBusy()
            }
        }
        
        def idleExecutors = totalExecutors - busyExecutors - offlineExecutors
        def utilizationPercent = totalExecutors > 0 ? (busyExecutors / (totalExecutors - offlineExecutors)) * 100 : 0
        
        result.executors = [
            total: totalExecutors,
            busy: busyExecutors,
            idle: idleExecutors,
            offline: offlineExecutors,
            utilizationPercent: utilizationPercent
        ]
        
        // Analyze queue
        def queueItems = jenkins.queue.items
        def queueLength = queueItems.length
        def totalWaitTime = 0
        queueItems.each { item ->
            totalWaitTime += System.currentTimeMillis() - item.inQueueSince
        }
        def avgWaitTime = queueLength > 0 ? totalWaitTime / queueLength : 0
        
        result.queue = [
            length: queueLength,
            items: queueItems.collect { [
                id: it.id,
                name: it.task.name,
                inQueueSince: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(it.inQueueSince)),
                waitTime: System.currentTimeMillis() - it.inQueueSince,
                why: it.why ?: "Unknown"
            ]},
            avgWaitTime: avgWaitTime
        ]
        
        // Analyze individual agents
        result.agents = jenkins.computers.findAll { it.name != "master" }.collect { computer ->
            def nodeLabel = computer.node?.labelString ?: ""
            def labelList = nodeLabel.split(/\s+/).findAll { it }
            
            [
                name: computer.name,
                offline: computer.offline,
                executors: computer.numExecutors,
                busyExecutors: computer.countBusy(),
                utilizationPercent: computer.numExecutors > 0 ? (computer.countBusy() / computer.numExecutors) * 100 : 0,
                labels: labelList
            ]
        }
        
        // Analyze labels
        def labels = [:]
        jenkins.labels.each { label ->
            def name = label.name
            if (!name) return // Skip empty labels
            
            def nodes = label.nodes
            def executors = nodes.sum { it.numExecutors } ?: 0
            def busyExecutors = 0
            nodes.each { node ->
                def computer = node.toComputer()
                if (computer && !computer.offline) {
                    busyExecutors += computer.countBusy()
                }
            }
            
            def utilizationPercent = executors > 0 ? (busyExecutors / executors) * 100 : 0
            
            // Count queued items for this label
            def queuedItems = jenkins.queue.items.count { item ->
                item.assignedLabel == label
            }
            
            labels[name] = [
                name: name,
                agentCount: nodes.size(),
                executors: executors,
                busyExecutors: busyExecutors,
                utilizationPercent: utilizationPercent,
                queuedItems: queuedItems
            ]
        }
        
        result.labels = labels.values() as List
        
        return result
    }, LOGGER, [:])
}

/**
 * Determines what scaling actions should be taken.
 * 
 * @param jenkins The Jenkins instance
 * @param targetLabel Label to target (or empty for all)
 * @param minAgents Minimum agent count per label
 * @param maxAgents Maximum agent count per label
 * @return List of scaling actions
 */
def determineScalingActions(Jenkins jenkins, String targetLabel, int minAgents, int maxAgents) {
    return ErrorHandler.withErrorHandling("determining scaling actions", {
        def actions = []
        def labels = []
        
        if (targetLabel) {
            def label = jenkins.getLabel(targetLabel)
            if (label) {
                labels.add(label)
            } else {
                println "Warning: Label '${targetLabel}' not found"
                return []
            }
        } else {
            // Use all labels that have at least one agent
            labels = jenkins.labels.findAll { it.nodes.size() > 0 }
        }
        
        labels.each { label ->
            def labelName = label.name
            if (!labelName) return // Skip empty labels
            
            def nodes = label.nodes
            def agentCount = nodes.size()
            
            // Get load statistics for this label
            def stats = jenkins.getLabel(labelName).loadStatistics
            def queueLength = stats.queueLength.latest
            
            // Determine target number of agents
            def targetAgents = agentCount
            def reason = ""
            
            if (queueLength > 0) {
                // We have jobs waiting - need more agents
                def newCount = Math.min(agentCount + Math.ceil(queueLength / 2) as int, maxAgents)
                if (newCount > agentCount) {
                    targetAgents = newCount
                    reason = "Queue length is ${queueLength}, need more capacity"
                }
            } else {
                // Check if we have idle agents that can be terminated
                def executors = nodes.sum { it.numExecutors } ?: 0
                def busyExecutors = 0
                nodes.each { node ->
                    def computer = node.toComputer()
                    if (computer && !computer.offline) {
                        busyExecutors += computer.countBusy()
                    }
                }
                
                def idleExecutors = executors - busyExecutors
                def utilizationPercent = executors > 0 ? (busyExecutors / executors) * 100 : 0
                
                if (utilizationPercent < 30 && agentCount > minAgents) {
                    // Low utilization - can terminate some agents
                    def idleAgents = Math.floor(idleExecutors / (executors / agentCount)) as int
                    def newCount = Math.max(agentCount - idleAgents, minAgents)
                    if (newCount < agentCount) {
                        targetAgents = newCount
                        reason = "Low utilization (${String.format("%.1f%%", utilizationPercent)}), can reduce capacity"
                    }
                }
            }
            
            // Only add action if there's something to do
            if (targetAgents != agentCount) {
                actions.add([
                    label: labelName,
                    currentAgents: agentCount,
                    targetAgents: targetAgents,
                    delta: targetAgents - agentCount,
                    action: targetAgents > agentCount ? "provision" : "terminate",
                    reason: reason
                ])
            }
        }
        
        return actions
    }, LOGGER, [])
}

/**
 * Generates recommendations for resource optimization.
 * 
 * @param jenkins The Jenkins instance
 * @return List of recommendation maps
 */
def generateRecommendations(Jenkins jenkins) {
    return ErrorHandler.withErrorHandling("generating recommendations", {
        def recommendations = []
        def utilizationData = analyzeResourceUtilization(jenkins)
        
        // Check for labels with high queue but low agent count
        utilizationData.labels.each { label ->
            if (label.queuedItems > 3 && label.agentCount < 3) {
                recommendations.add([
                    type: "capacity",
                    description: "Label '${label.name}' has ${label.queuedItems} queued items but only ${label.agentCount} agents",
                    impact: "Jobs are waiting unnecessarily in the queue",
                    action: "Increase max agents for this label or add more static agents"
                ])
            }
        }
        
        // Check for underutilized agents
        def underutilizedAgents = utilizationData.agents.findAll { it.utilizationPercent < 20 && !it.offline }
        if (underutilizedAgents.size() > 3) {
            recommendations.add([
                type: "efficiency",
                description: "Found ${underutilizedAgents.size()} agents with utilization under 20%",
                impact: "Wasting cloud resources and increasing costs",
                action: "Reduce minimum agent counts or consolidate jobs onto fewer agents"
            ])
        }
        
        // Check for offline agents
        def offlineAgents = utilizationData.agents.findAll { it.offline }
        if (offlineAgents.size() > 3) {
            recommendations.add([
                type: "reliability",
                description: "Found ${offlineAgents.size()} offline agents",
                impact: "Reduced capacity and potential job failures",
                action: "Investigate agent connectivity issues or terminate and replace failing agents"
            ])
        }
        
        // Check overall utilization
        if (utilizationData.executors.utilizationPercent < 30 && utilizationData.queue.length == 0) {
            recommendations.add([
                type: "cost",
                description: "Overall executor utilization is only ${String.format("%.1f%%", utilizationData.executors.utilizationPercent)} with no queued jobs",
                impact: "Paying for more cloud resources than needed",
                action: "Reduce the minimum number of agents or use smaller instance types"
            ])
        }
        
        // Check for imbalanced labels
        def highUtilizationLabels = utilizationData.labels.findAll { it.utilizationPercent > 80 }
        def lowUtilizationLabels = utilizationData.labels.findAll { it.utilizationPercent < 20 && it.agentCount > 2 }
        
        if (highUtilizationLabels && lowUtilizationLabels) {
            recommendations.add([
                type: "balance",
                description: "Resource imbalance across labels: ${highUtilizationLabels.size()} overutilized and ${lowUtilizationLabels.size()} underutilized",
                impact: "Some jobs wait while resources sit idle",
                action: "Redistribute jobs or adjust agent provisioning across labels"
            ])
        }
        
        return recommendations
    }, LOGGER, [])
}

/**
 * Provisions new agents based on label and template.
 * 
 * @param label Label to provision for
 * @param count Number of agents to provision
 * @param templateName Template to use, or empty to auto-select
 * @param awsManager AWSNodeManager instance or null
 * @param kubeManager KubernetesNodeManager instance or null
 * @param azureManager AzureNodeManager instance or null
 */
def performProvisioning(String label, int count, String templateName, 
                       AWSNodeManager awsManager, 
                       KubernetesNodeManager kubeManager, 
                       AzureNodeManager azureManager) {
    return ErrorHandler.withErrorHandling("provisioning new agents", {
        def success = false
        
        // Try to provision with AWS EC2 if configured
        if (awsManager && awsManager.isEC2CloudConfigured()) {
            def templates = awsManager.getEC2TemplatesInfo()
            def template = null
            
            if (templateName) {
                template = templates.find { it.description == templateName }
            } else {
                template = templates.find { it.labels?.contains(label) }
            }
            
            if (template) {
                println "  Provisioning ${count} EC2 instance(s) with template: ${template.description}"
                for (int i = 0; i < count; i++) {
                    awsManager.provisionNewInstance(template.description)
                }
                success = true
            }
        }
        
        // Try to provision with Kubernetes if configured and AWS didn't succeed
        if (!success && kubeManager && kubeManager.isKubernetesCloudConfigured()) {
            def templates = kubeManager.getPodTemplatesInfo()
            def template = null
            
            if (templateName) {
                template = templates.find { it.name == templateName }
            } else {
                template = templates.find { it.label?.contains(label) }
            }
            
            if (template) {
                println "  Provisioning ${count} Kubernetes pod(s) with template: ${template.name}"
                for (int i = 0; i < count; i++) {
                    kubeManager.provisionNewPod(template.label)
                }
                success = true
            }
        }
        
        // Try to provision with Azure if configured and others didn't succeed
        if (!success && azureManager && azureManager.isAzureCloudConfigured()) {
            def templates = azureManager.getAzureTemplatesInfo()
            def template = null
            
            if (templateName) {
                template = templates.find { it.templateName == templateName }
            } else {
                template = templates.find { it.labels?.contains(label) }
            }
            
            if (template) {
                println "  Provisioning ${count} Azure VM(s) with template: ${template.templateName}"
                for (int i = 0; i < count; i++) {
                    azureManager.provisionNewVM(template.templateName)
                }
                success = true
            }
        }
        
        if (!success) {
            println "  Failed to provision agents: No suitable template found for label '${label}'"
        }
        
        return success
    }, LOGGER, false)
}

/**
 * Terminates excess agents for a label.
 * 
 * @param label Label to terminate agents for
 * @param count Number of agents to terminate
 * @param awsManager AWSNodeManager instance or null
 * @param kubeManager KubernetesNodeManager instance or null
 * @param azureManager AzureNodeManager instance or null
 */
def performTermination(String label, int count, 
                       AWSNodeManager awsManager, 
                       KubernetesNodeManager kubeManager, 
                       AzureNodeManager azureManager) {
    return ErrorHandler.withErrorHandling("terminating excess agents", {
        def jenkins = Jenkins.get()
        def labelObj = jenkins.getLabel(label)
        if (!labelObj) {
            println "  Failed to terminate agents: Label '${label}' not found"
            return false
        }
        
        // Get nodes with this label, prioritizing idle ones
        def labelNodes = labelObj.nodes.collect { node ->
            def computer = node.toComputer()
            def busyExecutors = computer?.countBusy() ?: 0
            def isIdle = computer && !computer.offline && busyExecutors == 0
            
            [
                node: node,
                name: node.nodeName,
                isIdle: isIdle,
                computer: computer
            ]
        }.sort { !it.isIdle } // Sort so idle nodes come first
        
        // Limit to the number we need to terminate
        def nodesToTerminate = labelNodes.take(count)
        def terminatedCount = 0
        
        nodesToTerminate.each { nodeInfo ->
            def success = false
            def nodeName = nodeInfo.name
            
            // Attempt to terminate based on node type
            if (awsManager) {
                def ec2Nodes = awsManager.getEC2NodesInfo()
                def ec2Node = ec2Nodes.find { it.name == nodeName }
                if (ec2Node && ec2Node.ec2?.instanceId) {
                    println "  Terminating EC2 instance: ${ec2Node.ec2.instanceId} (${nodeName})"
                    success = awsManager.terminateInstance(ec2Node.ec2.instanceId)
                }
            }
            
            if (!success && kubeManager) {
                def k8sNodes = kubeManager.getKubernetesNodesInfo()
                def k8sNode = k8sNodes.find { it.name == nodeName }
                if (k8sNode && k8sNode.kubernetes?.podName) {
                    println "  Terminating Kubernetes pod: ${k8sNode.kubernetes.podName} (${nodeName})"
                    success = kubeManager.terminatePod(k8sNode.kubernetes.podName)
                }
            }
            
            if (!success && azureManager) {
                def azureNodes = azureManager.getAzureNodesInfo()
                def azureNode = azureNodes.find { it.name == nodeName }
                if (azureNode) {
                    println "  Cleaning up Azure VM: ${nodeName}"
                    success = azureManager.cleanupVM(nodeName)
                }
            }
            
            // If cloud-specific termination failed, try generic disconnection
            if (!success && nodeInfo.computer) {
                println "  Disconnecting node: ${nodeName}"
                nodeInfo.computer.disconnect(new hudson.slaves.OfflineCause.UserCause(
                    null, "Automatically terminated by OptimizeAgentResources script"))
                success = true
            }
            
            if (success) {
                terminatedCount++
            }
        }
        
        println "  Terminated ${terminatedCount} of ${count} requested agents"
        return terminatedCount > 0
    }, LOGGER, false)
}

/**
 * Formats a duration in milliseconds to a human-readable string.
 * 
 * @param durationMs Duration in milliseconds
 * @return Formatted duration string
 */
def formatDuration(long durationMs) {
    def seconds = durationMs / 1000
    def minutes = (int)(seconds / 60)
    def hours = (int)(minutes / 60)
    
    minutes = minutes % 60
    seconds = seconds % 60
    
    if (hours > 0) {
        return String.format("%dh %02dm %02ds", hours, minutes, (int)seconds)
    } else if (minutes > 0) {
        return String.format("%dm %02ds", minutes, (int)seconds)
    } else {
        return String.format("%.1fs", seconds)
    }
}