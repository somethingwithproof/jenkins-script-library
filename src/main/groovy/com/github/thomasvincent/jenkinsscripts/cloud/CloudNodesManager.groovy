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

package com.github.thomasvincent.jenkinsscripts.cloud

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import jenkins.model.Jenkins
import hudson.model.Node
import jenkins.model.JenkinsLocationConfiguration

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages cloud-based Jenkins agent nodes.
 * 
 * <p>This class provides common functionality for working with cloud provider
 * agents in Jenkins, serving as a base for cloud-specific implementations.</p>
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */
class CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(CloudNodesManager.class.getName())
    
    /** The Jenkins instance to work with */
    protected final Jenkins jenkins
    
    /** The Jenkins root URL, used for callbacks from cloud agents */
    protected final String jenkinsUrl
    
    /**
     * Creates a new CloudNodesManager.
     * 
     * <p>Initializes the manager with the current Jenkins instance
     * and configuration.</p>
     * 
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    CloudNodesManager(Jenkins jenkins = null) {
        this.jenkins = jenkins ?: Jenkins.get()
        
        // Get Jenkins URL for later use in agent configurations
        def locationConfig = JenkinsLocationConfiguration.get()
        this.jenkinsUrl = locationConfig?.getUrl()?.trim() ?: ""
        
        if (jenkinsUrl.isEmpty()) {
            LOGGER.warning("Jenkins URL is not configured. Cloud agents may not be able to connect back to Jenkins.")
        }
    }
    
    /**
     * Retrieves all cloud plugins configured in Jenkins.
     * 
     * <p>Returns a list of cloud implementations from the Jenkins configuration.
     * This includes EC2, Kubernetes, Azure, etc.</p>
     * 
     * @return List of configured cloud providers
     */
    protected List getConfiguredClouds() {
        return ErrorHandler.withErrorHandling("retrieving configured clouds", {
            return jenkins.clouds ?: []
        }, LOGGER, [])
    }
    
    /**
     * Checks if a specific cloud plugin is installed and configured.
     * 
     * <p>Verifies if a cloud plugin of the given class type is available
     * in the current Jenkins instance.</p>
     * 
     * @param cloudClass The cloud class to check for
     * @return true if the cloud type is configured, false otherwise
     */
    protected boolean isCloudConfigured(Class cloudClass) {
        return ErrorHandler.withErrorHandling("checking for configured cloud type", {
            return getConfiguredClouds().any { cloudClass.isInstance(it) }
        }, LOGGER, false)
    }
    
    /**
     * Gets cloud nodes filtered by a specific cloud type.
     * 
     * <p>Returns agents that are managed by a specific cloud provider type.</p>
     * 
     * @param cloudClass The cloud provider class to filter by
     * @return List of nodes managed by the specified cloud type
     */
    protected List<Node> getCloudNodes(Class cloudClass) {
        return ErrorHandler.withErrorHandling("retrieving cloud nodes", {
            // This basic implementation returns nodes that appear to be cloud-managed
            // Specific cloud implementations should override with more precise logic
            return jenkins.nodes.findAll { node ->
                // Basic cloud node detection
                node.name.contains("cloud") || 
                node.name.matches(/.*-[a-z0-9]{8}/) ||  // Many cloud agents have random suffixes
                node.labelString.contains("cloud") ||
                (node.descriptor?.displayName?.toLowerCase()?.contains("cloud") ?: false)
            }
        }, LOGGER, [])
    }
    
    /**
     * Gets basic information about all cloud nodes.
     * 
     * <p>Returns information about all detected cloud-based nodes
     * across all cloud providers.</p>
     * 
     * @return Map of cloud provider types to their managed nodes
     */
    Map<String, List<Node>> getAllCloudNodes() {
        return ErrorHandler.withErrorHandling("getting all cloud nodes", {
            def result = [:]
            
            // Get all clouds and group their nodes
            getConfiguredClouds().each { cloud ->
                def cloudType = cloud.getClass().simpleName
                def cloudNodes = getCloudNodes(cloud.getClass())
                
                if (cloudNodes) {
                    result[cloudType] = cloudNodes
                }
            }
            
            return result
        }, LOGGER, [:])
    }
    
    /**
     * Gets node statistics by cloud provider.
     * 
     * <p>Returns counts of online/offline nodes grouped by cloud provider.</p>
     * 
     * @return Map of cloud provider types to node statistics
     */
    Map<String, Map<String, Integer>> getCloudNodeStats() {
        return ErrorHandler.withErrorHandling("getting cloud node statistics", {
            def result = [:]
            def allCloudNodes = getAllCloudNodes()
            
            allCloudNodes.each { cloudType, nodes ->
                def stats = [
                    total: nodes.size(),
                    online: nodes.count { !it.computer.offline },
                    offline: nodes.count { it.computer.offline }
                ]
                result[cloudType] = stats
            }
            
            return result
        }, LOGGER, [:])
    }
    
    /**
     * Extracts detailed node information.
     * 
     * <p>Retrieves basic information common to all node types.
     * Cloud-specific implementations should extend this with more details.</p>
     * 
     * @param node The node to extract information from
     * @return Map containing basic node information
     */
    protected Map<String, Object> extractNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")
        
        return ErrorHandler.withErrorHandling("extracting node information", {
            def computer = node.computer
            
            return [
                name: node.nodeName,
                displayName: node.displayName,
                description: node.nodeDescription,
                numExecutors: node.numExecutors,
                labels: node.labelString,
                remoteFS: node.remoteFS,
                offline: computer?.offline ?: true,
                temporarilyOffline: computer?.temporarilyOffline ?: false,
                connectTime: computer?.connectTime ?: 0,
                offlineCause: computer?.offlineCause?.toString()
            ]
        }, LOGGER, [:])
    }
    
    /**
     * Formats cloud node information for display.
     * 
     * <p>Creates a human-readable representation of cloud node details.</p>
     * 
     * @param nodeInfo The node information map to format
     * @return Formatted string with node details
     */
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }
        
        StringBuilder builder = new StringBuilder()
        builder.append("Node: ${nodeInfo.name}\n")
        
        // Sort keys for consistent output
        def sortedKeys = nodeInfo.keySet().sort()
        
        sortedKeys.each { key ->
            if (key != 'name') {
                def value = nodeInfo[key]
                if (value instanceof Map) {
                    builder.append("${key}:\n")
                    value.each { k, v ->
                        builder.append("  ${k}: ${v}\n")
                    }
                } else {
                    builder.append("${key}: ${value}\n")
                }
            }
        }
        
        return builder.toString()
    }
}