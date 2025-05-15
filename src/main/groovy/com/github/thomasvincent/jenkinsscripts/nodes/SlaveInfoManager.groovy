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

package com.github.thomasvincent.jenkinsscripts.nodes

import hudson.model.Slave
import hudson.model.Computer
import hudson.plugins.ec2.EC2Computer
import jenkins.model.Jenkins

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages information about Jenkins slave nodes.
 * 
 * Collects, formats, and displays detailed information about Jenkins slaves,
 * with special handling for EC2 instances.
 * 
 * ```groovy
 * def manager = new SlaveInfoManager(Jenkins.get())
 * def slaveInfo = manager.getSlaveInfo('my-slave-node')
 * println manager.formatSlaveInfo(slaveInfo)
 * 
 * // List all slaves
 * manager.listAllSlaves().each { slave ->
 *     println manager.formatSlaveInfo(slave)
 * }
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class SlaveInfoManager {

    private static final Logger LOGGER = Logger.getLogger(SlaveInfoManager.class.getName())
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial({ 
        new SimpleDateFormat("yyyy-MM-dd HH:mm") 
    })
    
    private final Jenkins jenkins
    
    /**
     * Constructs a SlaveInfoManager instance.
     * 
     * Uses current Jenkins instance if parameter is null.
     * 
     * ```groovy
     * // With explicit Jenkins instance
     * def manager = new SlaveInfoManager(Jenkins.get())
     * 
     * // Using default instance
     * def manager = new SlaveInfoManager(null)
     * ```
     */
    SlaveInfoManager(Jenkins jenkins) {
        this.jenkins = jenkins ?: Jenkins.get()
    }
    
    /**
     * Lists information about all slave nodes.
     * 
     * Collects detailed info about all nodes in the Jenkins instance.
     * 
     * ```groovy
     * def slaves = manager.listAllSlaves()
     * println "Found ${slaves.size()} nodes"
     * 
     * // Filter EC2 slaves only
     * def ec2Slaves = slaves.findAll { it.containsKey('ec2') }
     * ```
     * 
     * @return List of maps with slave node information
     */
    List<Map<String, Object>> listAllSlaves() {
        List<Map<String, Object>> result = []
        
        jenkins.nodes.each { node ->
            ErrorHandler.withErrorHandling("collecting information for node ${node.nodeName}", {
                Map<String, Object> info = collectSlaveInfo(node)
                if (info) {
                    result.add(info)
                }
                return null // No specific return value needed
            }, LOGGER, Level.WARNING)
        }
        
        return result
    }
    
    /**
     * Gets information about a specific slave by name.
     * 
     * Returns null if the node doesn't exist.
     * 
     * ```groovy
     * def info = manager.getSlaveInfo('jenkins-worker-1')
     * if (info) {
     *     println "Node ${info.name} has ${info.numExecutors} executors"
     *     if (info.offline) {
     *         println "Node is offline. Reason: ${info.offlineCause ?: 'Unknown'}"
     *     }
     * }
     * ```
     */
    Map<String, Object> getSlaveInfo(String slaveName) {
        try {
            slaveName = ValidationUtils.requireNonEmpty(slaveName, "Slave name")
        } catch (IllegalArgumentException e) {
            ErrorHandler.handleError("validating slave name", e, LOGGER)
            return null
        }
        
        def node = jenkins.getNode(slaveName)
        if (!node) {
            LOGGER.warning("Slave node not found: ${slaveName}")
            return null
        }
        
        return collectSlaveInfo(node)
    }
    
    /**
     * Collects detailed information about a Jenkins slave node.
     * 
     * Gathers basic node info and adds type-specific details (standard or EC2).
     * Returns null if the computer is unavailable.
     *
     * Uses Groovy's concise map syntax for collecting properties.
     */
    private Map<String, Object> collectSlaveInfo(Slave node) {
        ValidationUtils.requireNonNull(node, "Slave node")
        
        Computer computer = node.computer
        if (computer == null) {
            LOGGER.warning("Computer is null for node ${node.nodeName}")
            return null
        }
        
        Map<String, Object> info = [
            name: node.nodeName,
            displayName: node.displayName,
            description: node.nodeDescription,
            remoteFS: node.remoteFS,
            numExecutors: node.numExecutors,
            mode: node.mode.toString(),
            offline: computer.offline,
            temporarilyOffline: computer.temporarilyOffline
        ]
        
        if (computer instanceof EC2Computer) {
            addEC2Info(info, (EC2Computer) computer)
        } else {
            addStandardComputerInfo(info, computer)
        }
        
        return info
    }
    
    /**
     * Adds EC2-specific information to the slave info map.
     * 
     * Collects EC2 details like instance ID, type, IPs, AMI, and tags.
     * Uses Groovy's collectEntries for elegant tag map creation.
     *
     * ```groovy
     * // Example of EC2 data available in the returned map
     * assert info.ec2.instanceId == 'i-01234567890abcdef'
     * assert info.ec2.tags.Name == 'jenkins-worker'
     * ```
     */
    private void addEC2Info(Map<String, Object> info, EC2Computer ec2Computer) {
        ValidationUtils.requireNonNull(info, "Info map")
        ValidationUtils.requireNonNull(ec2Computer, "EC2 computer")
        
        ErrorHandler.withErrorHandling("collecting EC2 information for ${ec2Computer.name}", {
            def instance = ec2Computer.describeInstance()
            if (instance) {
                info.ec2 = [
                    instanceId: instance.getInstanceId(),
                    instanceType: instance.getInstanceType(),
                    privateIp: instance.getPrivateIpAddress(),
                    publicIp: instance.getPublicIpAddress(),
                    amiId: instance.getImageId(),
                    launchTime: instance.getLaunchTime() ? DATE_FORMATTER.get().format(instance.getLaunchTime()) : null,
                    state: instance.getState()?.getName(),
                    tags: instance.getTags()?.collectEntries { [(it.key): it.value] }
                ]
            }
            return null // No specific return value needed
        }, LOGGER, Level.WARNING)
        
        if (!info.containsKey('ec2')) {
            info.ec2Error = "Failed to retrieve EC2 instance information"
        }
    }
    
    /**
     * Adds standard computer information to the slave info map.
     * 
     * Adds basic node details like hostname and connection time.
     * Uses Groovy's safe navigation operator for clean null handling.
     *
     * ```groovy
     * // Connection time is formatted with DATE_FORMATTER if available
     * println "Node ${info.name} connected at ${info.connectionTime ?: 'unknown'}"
     * ```
     */
    private void addStandardComputerInfo(Map<String, Object> info, Computer computer) {
        info.hostName = computer.hostName
        info.connectionTime = computer.connectTime > 0 ? DATE_FORMATTER.get().format(new Date(computer.connectTime)) : null
        info.offlineCause = computer.offlineCause?.getShortDescription()
    }
    
    /**
     * Formats the slave information for display.
     * 
     * Creates human-readable text from the slave info map.
     * Uses Groovy's StringBuilder for efficient string building.
     *
     * ```groovy
     * // Get and format info for all offline nodes
     * manager.listAllSlaves().findAll { it.offline }.each { node ->
     *     println manager.formatSlaveInfo(node)
     * }
     * 
     * // Custom formatting with provided info
     * def info = manager.getSlaveInfo('worker-1')
     * if (info?.ec2) {
     *     println "EC2 Instance: ${info.ec2.instanceId} (${info.ec2.state})"
     * }
     * ```
     */
    String formatSlaveInfo(Map<String, Object> slaveInfo) {
        // No need to use ValidationUtils here - null check is appropriate since we return a default message
        if (!slaveInfo) {
            return "No information available"
        }
        
        StringBuilder builder = new StringBuilder()
        builder.append("Node: ${slaveInfo.name}\n")
        
        slaveInfo.each { propName, propValue ->
            if (propName != 'name' && propName != 'ec2') {
                builder.append("  ${propName}: ${propValue}\n")
            }
        }
        
        if (slaveInfo.containsKey('ec2')) {
            builder.append("  EC2 Details:\n")
            slaveInfo.ec2.each { propName, propValue ->
                builder.append("    ${propName}: ${propValue}\n")
            }
        }
        
        return builder.toString()
    }
}