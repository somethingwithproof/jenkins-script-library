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
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave
import org.csanchez.jenkins.plugins.kubernetes.PodTemplate

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages Kubernetes-based Jenkins agent nodes.
 * 
 * <p>This class provides functionality for working with Kubernetes pods
 * that serve as Jenkins agent nodes, through the Kubernetes Plugin.</p>
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */
class KubernetesNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(KubernetesNodeManager.class.getName())
    
    /**
     * Cached reference to the Kubernetes cloud configuration.
     * Set lazily on first access.
     */
    private List<KubernetesCloud> kubeClouds = null
    
    /**
     * Creates a new KubernetesNodeManager.
     * 
     * <p>Initializes the manager with the current Jenkins instance.</p>
     * 
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    KubernetesNodeManager(Jenkins jenkins = null) {
        super(jenkins)
    }
    
    /**
     * Gets all Kubernetes cloud configurations.
     * 
     * <p>Returns all KubernetesCloud instances configured in Jenkins.</p>
     * 
     * @return List of KubernetesCloud configurations, or empty list if none found
     */
    List<KubernetesCloud> getKubernetesClouds() {
        if (kubeClouds == null) {
            kubeClouds = ErrorHandler.withErrorHandling("retrieving Kubernetes clouds", {
                return jenkins.clouds.findAll { it instanceof KubernetesCloud } as List<KubernetesCloud>
            }, LOGGER, [])
        }
        return kubeClouds
    }
    
    /**
     * Checks if the Kubernetes cloud plugin is installed and configured.
     * 
     * <p>Verifies whether there are any Kubernetes clouds configured in Jenkins.</p>
     * 
     * @return true if Kubernetes cloud is configured, false otherwise
     */
    boolean isKubernetesCloudConfigured() {
        return !getKubernetesClouds().isEmpty()
    }
    
    /**
     * Gets all Kubernetes agent nodes.
     * 
     * <p>Returns all nodes that are managed by Kubernetes clouds.</p>
     * 
     * @return List of Kubernetes agent nodes
     */
    List<Node> getKubernetesNodes() {
        return ErrorHandler.withErrorHandling("retrieving Kubernetes nodes", {
            // Find nodes that are instances of KubernetesSlave
            return jenkins.nodes.findAll { node ->
                node instanceof KubernetesSlave
            }
        }, LOGGER, [])
    }
    
    /**
     * Gets detailed information about all Kubernetes agent nodes.
     * 
     * <p>Returns comprehensive information including Kubernetes-specific details
     * for all Kubernetes agent nodes.</p>
     * 
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getKubernetesNodesInfo() {
        return ErrorHandler.withErrorHandling("getting Kubernetes nodes info", {
            def nodes = getKubernetesNodes()
            def result = []
            
            nodes.each { node ->
                def nodeInfo = extractKubernetesNodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Extracts detailed information about a Kubernetes agent node.
     * 
     * <p>Retrieves basic node information and adds Kubernetes-specific details.</p>
     * 
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractKubernetesNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")
        
        def info = extractNodeInfo(node)
        
        // Add Kubernetes-specific information if available
        if (node instanceof KubernetesSlave) {
            def kubeSlave = node as KubernetesSlave
            
            return ErrorHandler.withErrorHandling("extracting Kubernetes node information", {
                info.kubernetes = [
                    podName: kubeSlave.getPodName(),
                    namespace: kubeSlave.getNamespace(),
                    cloudName: kubeSlave.getCloudName(),
                    template: kubeSlave.getTemplateLabel(),
                    containers: extractContainerInfo(kubeSlave)
                ]
                
                return info
            }, LOGGER, info)
        }
        
        return info
    }
    
    /**
     * Extracts container information from a Kubernetes slave.
     * 
     * <p>Attempts to retrieve container specifications from the pod template.</p>
     * 
     * @param kubeSlave The KubernetesSlave to extract container info from
     * @return List of container information maps
     */
    private List<Map<String, Object>> extractContainerInfo(KubernetesSlave kubeSlave) {
        return ErrorHandler.withErrorHandling("extracting container information", {
            def result = []
            def templateLabel = kubeSlave.getTemplateLabel()
            def cloudName = kubeSlave.getCloudName()
            
            // Find the template used for this pod
            def cloud = getKubernetesClouds().find { it.name == cloudName }
            if (cloud) {
                def template = cloud.getTemplates().find { it.label == templateLabel }
                if (template) {
                    template.getContainers().each { container ->
                        result.add([
                            name: container.name,
                            image: container.image,
                            resourceLimitCpu: container.resourceLimitCpu,
                            resourceLimitMemory: container.resourceLimitMemory,
                            resourceRequestCpu: container.resourceRequestCpu,
                            resourceRequestMemory: container.resourceRequestMemory,
                            workingDir: container.workingDir,
                            ports: container.ports?.collect { it.toString() }
                        ])
                    }
                }
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Gets all available Kubernetes pod templates.
     * 
     * <p>Returns all pod templates configured across all Kubernetes clouds.</p>
     * 
     * @return List of pod templates
     */
    List<PodTemplate> getPodTemplates() {
        return ErrorHandler.withErrorHandling("retrieving pod templates", {
            def templates = []
            getKubernetesClouds().each { cloud ->
                templates.addAll(cloud.getTemplates())
            }
            return templates
        }, LOGGER, [])
    }
    
    /**
     * Gets template information for all pod templates.
     * 
     * <p>Returns configuration details for all pod templates
     * configured in Jenkins.</p>
     * 
     * @return List of maps containing template information
     */
    List<Map<String, Object>> getPodTemplatesInfo() {
        return ErrorHandler.withErrorHandling("getting pod templates info", {
            def templates = getPodTemplates()
            def result = []
            
            templates.each { template ->
                def templateInfo = [
                    name: template.name,
                    label: template.label,
                    namespace: template.namespace,
                    nodeUsageMode: template.nodeUsageMode?.toString(),
                    serviceAccount: template.serviceAccount,
                    idleMinutes: template.idleMinutes,
                    slaveConnectTimeout: template.slaveConnectTimeout,
                    containers: template.containers?.collect { container ->
                        [
                            name: container.name,
                            image: container.image,
                            workingDir: container.workingDir,
                            command: container.command,
                            args: container.args
                        ]
                    }
                ]
                
                result.add(templateInfo)
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Provision a new Kubernetes pod.
     * 
     * <p>Provisions a new Kubernetes pod based on a template.</p>
     * 
     * @param templateLabel The label of the template to use
     * @param cloudName The name of the Kubernetes cloud to use (optional, if multiple are configured)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    boolean provisionNewPod(String templateLabel, String cloudName = null) {
        ValidationUtils.requireNonEmpty(templateLabel, "Template label")
        
        return ErrorHandler.withErrorHandling("provisioning new Kubernetes pod", {
            def clouds = cloudName ?
                getKubernetesClouds().findAll { it.name == cloudName } :
                getKubernetesClouds()
            
            if (clouds.isEmpty()) {
                LOGGER.warning("No Kubernetes clouds found${cloudName ? " with name '${cloudName}'" : ""}")
                return false
            }
            
            // Find template by label
            for (cloud in clouds) {
                def template = cloud.getTemplates().find { it.label == templateLabel }
                if (template) {
                    // Provision a new node using this template
                    cloud.provision(new Node.Mode(), templateLabel)
                    LOGGER.info("Provisioning initiated for new Kubernetes pod from template: ${templateLabel}")
                    return true
                }
            }
            
            LOGGER.warning("No Kubernetes template found with label: ${templateLabel}")
            return false
        }, LOGGER, false)
    }
    
    /**
     * Terminates a Kubernetes pod.
     * 
     * <p>Terminates the specified Kubernetes pod and removes it from Jenkins.</p>
     * 
     * @param podName The pod name to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    boolean terminatePod(String podName) {
        ValidationUtils.requireNonEmpty(podName, "Pod name")
        
        return ErrorHandler.withErrorHandling("terminating Kubernetes pod", {
            // Find node by pod name
            def nodes = getKubernetesNodesInfo()
            def nodeInfo = nodes.find { it.kubernetes?.podName == podName }
            
            if (!nodeInfo) {
                LOGGER.warning("No Kubernetes pod found with name: ${podName}")
                return false
            }
            
            // Get the node
            def node = jenkins.getNode(nodeInfo.name as String)
            if (!node || !(node instanceof KubernetesSlave)) {
                LOGGER.warning("Node not found or not a Kubernetes node: ${nodeInfo.name}")
                return false
            }
            
            // Terminate the pod
            node.terminate()
            
            LOGGER.info("Kubernetes pod termination initiated for: ${podName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Formats Kubernetes node information for display.
     * 
     * <p>Creates a human-readable representation of Kubernetes node details.</p>
     * 
     * @param nodeInfo The node information map to format
     * @return Formatted string with Kubernetes node details
     */
    @Override
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }
        
        StringBuilder builder = new StringBuilder()
        builder.append("Kubernetes Node: ${nodeInfo.name}\n")
        
        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")
        
        // Kubernetes-specific information
        if (nodeInfo.kubernetes) {
            builder.append("\nKubernetes Details:\n")
            builder.append("  Pod Name: ${nodeInfo.kubernetes.podName}\n")
            builder.append("  Namespace: ${nodeInfo.kubernetes.namespace}\n")
            builder.append("  Cloud: ${nodeInfo.kubernetes.cloudName}\n")
            builder.append("  Template: ${nodeInfo.kubernetes.template}\n")
            
            if (nodeInfo.kubernetes.containers) {
                builder.append("\n  Containers:\n")
                nodeInfo.kubernetes.containers.each { container ->
                    builder.append("    Name: ${container.name}\n")
                    builder.append("    Image: ${container.image}\n")
                    if (container.resourceLimitCpu) {
                        builder.append("    CPU Limit: ${container.resourceLimitCpu}\n")
                    }
                    if (container.resourceLimitMemory) {
                        builder.append("    Memory Limit: ${container.resourceLimitMemory}\n")
                    }
                    if (container.workingDir) {
                        builder.append("    Working Dir: ${container.workingDir}\n")
                    }
                    if (container.ports) {
                        builder.append("    Ports: ${container.ports.join(', ')}\n")
                    }
                    builder.append("\n")
                }
            }
        }
        
        return builder.toString()
    }
}