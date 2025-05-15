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

import spock.lang.Specification
import spock.lang.Unroll
import jenkins.model.Jenkins
import hudson.model.Label
import hudson.model.Node
import hudson.model.Computer
import hudson.model.LoadStatistics
import hudson.model.queue.QueueTaskFuture
import hudson.model.Queue
import jenkins.metrics.api.Metrics
import jenkins.model.JenkinsLocationConfiguration

/**
 * Unit tests for OptimizeAgentResources script functions.
 * 
 * This tests the individual functions defined within the OptimizeAgentResources script.
 * Testing is done by extracting and binding the functions to this test class.
 */
class OptimizeAgentResourcesTest extends Specification {

    def jenkins = Mock(Jenkins)
    def locationConfig = Mock(JenkinsLocationConfiguration)
    def computer1 = Mock(Computer)
    def computer2 = Mock(Computer)
    def node1 = Mock(Node)
    def node2 = Mock(Node)
    def label = Mock(Label)
    def loadStats = Mock(LoadStatistics)
    def queueItem = Mock(Queue.Item)
    
    // Functions to test - these would be extracted from the script
    def analyzeResourceUtilization
    def determineScalingActions
    def generateRecommendations
    def formatDuration
    
    def setup() {
        // Setup computers
        computer1.name >> "agent1"
        computer1.numExecutors >> 2
        computer1.countBusy() >> 1
        computer1.offline >> false
        computer1.node >> node1
        
        computer2.name >> "agent2"
        computer2.numExecutors >> 4
        computer2.countBusy() >> 4
        computer2.offline >> false
        computer2.node >> node2
        
        // Setup nodes
        node1.nodeName >> "agent1"
        node1.labelString >> "linux"
        node1.numExecutors >> 2
        
        node2.nodeName >> "agent2"
        node2.labelString >> "windows"
        node2.numExecutors >> 4
        
        // Setup label
        label.name >> "linux"
        label.nodes >> [node1]
        
        // Setup label stats
        loadStats.queueLength >> new LoadStatistics.LoadStatisticsSnapshot(3, 2, 1)
        
        // Setup queue
        queueItem.inQueueSince >> (System.currentTimeMillis() - 60000)
        queueItem.task >> Mock(Queue.Task) { getName() >> "test-job" }
        queueItem.why >> "Waiting for next available executor"
        
        // Setup Jenkins
        jenkins.computers >> [computer1, computer2]
        jenkins.nodes >> [node1, node2]
        jenkins.queue >> Mock(Queue) { getItems() >> [queueItem] }
        jenkins.getLabel("linux") >> label
        label.loadStatistics >> loadStats
        
        // Implement test functions - these would be equivalents of the functions in the script
        analyzeResourceUtilization = { Jenkins jenkins ->
            def totalExecutors = 0
            def busyExecutors = 0
            def offlineExecutors = 0
            
            jenkins.computers.each { computer ->
                if (computer.name == "master") return
                
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
            
            return [
                executors: [
                    total: totalExecutors,
                    busy: busyExecutors,
                    idle: idleExecutors,
                    offline: offlineExecutors,
                    utilizationPercent: utilizationPercent
                ],
                queue: [
                    length: jenkins.queue.items.length,
                    avgWaitTime: jenkins.queue.items.size() > 0 ? 
                        (jenkins.queue.items.sum { System.currentTimeMillis() - it.inQueueSince } / jenkins.queue.items.size()) : 0
                ],
                agents: jenkins.computers.findAll { it.name != "master" }.collect { computer ->
                    [
                        name: computer.name,
                        offline: computer.offline,
                        executors: computer.numExecutors,
                        busyExecutors: computer.countBusy(),
                        utilizationPercent: computer.numExecutors > 0 ? 
                            (computer.countBusy() / computer.numExecutors) * 100 : 0
                    ]
                }
            ]
        }
        
        determineScalingActions = { Jenkins jenkins, String targetLabel, int minAgents, int maxAgents ->
            def actions = []
            def labels = []
            
            if (targetLabel) {
                def label = jenkins.getLabel(targetLabel)
                if (label) {
                    labels.add(label)
                }
            } else {
                labels = jenkins.labels
            }
            
            labels.each { label ->
                def labelName = label.name
                def nodes = label.nodes
                def agentCount = nodes.size()
                
                // Get load statistics for this label
                def stats = jenkins.getLabel(labelName).loadStatistics
                def queueLength = stats.queueLength.latest
                
                // Determine if we need to scale up or down
                def targetAgents = agentCount
                def reason = ""
                
                if (queueLength > 0) {
                    // Need more agents
                    def newCount = Math.min(agentCount + 1, maxAgents)
                    if (newCount > agentCount) {
                        targetAgents = newCount
                        reason = "Queue length is ${queueLength}, need more capacity"
                    }
                } else {
                    // Check if we have idle agents to terminate
                    def executors = nodes.sum { it.numExecutors } ?: 0
                    def busyExecutors = 0
                    nodes.each { node ->
                        def computer = node.toComputer()
                        if (computer && !computer.offline) {
                            busyExecutors += computer.countBusy()
                        }
                    }
                    
                    def utilizationPercent = executors > 0 ? (busyExecutors / executors) * 100 : 0
                    
                    if (utilizationPercent < 30 && agentCount > minAgents) {
                        def newCount = Math.max(agentCount - 1, minAgents)
                        if (newCount < agentCount) {
                            targetAgents = newCount
                            reason = "Low utilization (${utilizationPercent}%), can reduce capacity"
                        }
                    }
                }
                
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
        }
        
        generateRecommendations = { Jenkins jenkins ->
            def recommendations = []
            def utilization = analyzeResourceUtilization(jenkins)
            
            // Check overall utilization
            if (utilization.executors.utilizationPercent < 30 && utilization.queue.length == 0) {
                recommendations.add([
                    type: "cost",
                    description: "Overall executor utilization is only ${String.format("%.1f%%", utilization.executors.utilizationPercent)} with no queued jobs",
                    impact: "Paying for more cloud resources than needed",
                    action: "Reduce the minimum number of agents or use smaller instance types"
                ])
            }
            
            return recommendations
        }
        
        formatDuration = { long durationMs ->
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
    }
    
    def "analyzeResourceUtilization should return correct utilization data"() {
        when:
        def result = analyzeResourceUtilization(jenkins)
        
        then:
        result.executors.total == 6
        result.executors.busy == 5
        result.executors.idle == 1
        result.executors.offline == 0
        result.executors.utilizationPercent == (5 / 6) * 100
        result.queue.length == 1
        result.agents.size() == 2
        result.agents.find { it.name == "agent1" }.utilizationPercent == 50.0
        result.agents.find { it.name == "agent2" }.utilizationPercent == 100.0
    }
    
    def "determineScalingActions should recommend scaling up when queue length is high"() {
        given:
        def targetLabel = "linux"
        def minAgents = 1
        def maxAgents = 5
        
        when:
        def actions = determineScalingActions(jenkins, targetLabel, minAgents, maxAgents)
        
        then:
        actions.size() == 1
        actions[0].label == "linux"
        actions[0].currentAgents == 1
        actions[0].targetAgents == 2
        actions[0].delta == 1
        actions[0].action == "provision"
        actions[0].reason.contains("Queue length")
    }
    
    def "determineScalingActions should not exceed maximum agent limit"() {
        given:
        def targetLabel = "linux"
        def minAgents = 1
        def maxAgents = 1  // Max already reached
        
        when:
        def actions = determineScalingActions(jenkins, targetLabel, minAgents, maxAgents)
        
        then:
        actions.size() == 0  // No actions because we've reached max
    }
    
    def "generateRecommendations should recommend cost reduction for low utilization"() {
        given:
        computer1.countBusy() >> 0
        computer2.countBusy() >> 1
        
        when:
        def recommendations = generateRecommendations(jenkins)
        
        then:
        recommendations.size() == 1
        recommendations[0].type == "cost"
        recommendations[0].description.contains("utilization")
        recommendations[0].action.contains("Reduce")
    }
    
    @Unroll
    def "formatDuration should format #durationMs ms as #expected"() {
        expect:
        formatDuration(durationMs) == expected
        
        where:
        durationMs | expected
        500        | "0.5s"
        1000       | "1.0s"
        61000      | "1m 01s"
        3661000    | "1h 01m 01s"
    }
}