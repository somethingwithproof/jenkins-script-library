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
import com.microsoft.azure.vmagent.AzureVMCloud
import com.microsoft.azure.vmagent.AzureVMAgent
import com.microsoft.azure.vmagent.AzureVMAgentTemplate

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages Azure VM-based Jenkins agent nodes.
 * 
 * <p>This class provides functionality for working with Azure VMs
 * that serve as Jenkins agent nodes, through the Azure VM Agents Plugin.</p>
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */
class AzureNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(AzureNodeManager.class.getName())
    
    /**
     * Cached reference to the Azure cloud configuration.
     * Set lazily on first access.
     */
    private List<AzureVMCloud> azureClouds = null
    
    /**
     * Creates a new AzureNodeManager.
     * 
     * <p>Initializes the manager with the current Jenkins instance.</p>
     * 
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    AzureNodeManager(Jenkins jenkins = null) {
        super(jenkins)
    }
    
    /**
     * Gets all Azure VM cloud configurations.
     * 
     * <p>Returns all AzureVMCloud instances configured in Jenkins.</p>
     * 
     * @return List of AzureVMCloud configurations, or empty list if none found
     */
    List<AzureVMCloud> getAzureClouds() {
        if (azureClouds == null) {
            azureClouds = ErrorHandler.withErrorHandling("retrieving Azure clouds", {
                return jenkins.clouds.findAll { it instanceof AzureVMCloud } as List<AzureVMCloud>
            }, LOGGER, [])
        }
        return azureClouds
    }
    
    /**
     * Checks if the Azure VM cloud plugin is installed and configured.
     * 
     * <p>Verifies whether there are any Azure VM clouds configured in Jenkins.</p>
     * 
     * @return true if Azure VM cloud is configured, false otherwise
     */
    boolean isAzureCloudConfigured() {
        return !getAzureClouds().isEmpty()
    }
    
    /**
     * Gets all Azure VM agent nodes.
     * 
     * <p>Returns all nodes that are managed by Azure VM clouds.</p>
     * 
     * @return List of Azure VM agent nodes
     */
    List<Node> getAzureNodes() {
        return ErrorHandler.withErrorHandling("retrieving Azure VM nodes", {
            // Find nodes that are instances of AzureVMAgent
            return jenkins.nodes.findAll { node ->
                node instanceof AzureVMAgent
            }
        }, LOGGER, [])
    }
    
    /**
     * Gets detailed information about all Azure VM agent nodes.
     * 
     * <p>Returns comprehensive information including Azure-specific details
     * for all Azure VM agent nodes.</p>
     * 
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getAzureNodesInfo() {
        return ErrorHandler.withErrorHandling("getting Azure VM nodes info", {
            def nodes = getAzureNodes()
            def result = []
            
            nodes.each { node ->
                def nodeInfo = extractAzureNodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Extracts detailed information about an Azure VM agent node.
     * 
     * <p>Retrieves basic node information and adds Azure-specific details.</p>
     * 
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractAzureNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")
        
        def info = extractNodeInfo(node)
        
        // Add Azure-specific information if available
        if (node instanceof AzureVMAgent) {
            def azureAgent = node as AzureVMAgent
            
            return ErrorHandler.withErrorHandling("extracting Azure VM node information", {
                info.azure = [
                    vmName: azureAgent.getNodeName(),
                    vmId: azureAgent.getNodeName(),
                    servicePrincipal: azureAgent.getServicePrincipalName(),
                    resourceGroupName: azureAgent.getResourceGroupName(),
                    templateName: azureAgent.getTemplateName(),
                    location: azureAgent.getLocation(),
                    vmSize: azureAgent.getVirtualMachineSize(),
                    osType: azureAgent.getOsType(),
                    agentLaunchMethod: azureAgent.getAgentLaunchMethod(),
                    retentionStrategy: azureAgent.getRetentionStrategy()?.getClass()?.getSimpleName()
                ]
                
                return info
            }, LOGGER, info)
        }
        
        return info
    }
    
    /**
     * Gets all available Azure VM templates.
     * 
     * <p>Returns all Azure VM templates configured across all Azure clouds.</p>
     * 
     * @return List of Azure VM templates
     */
    List<AzureVMAgentTemplate> getAzureTemplates() {
        return ErrorHandler.withErrorHandling("retrieving Azure VM templates", {
            def templates = []
            getAzureClouds().each { cloud ->
                templates.addAll(cloud.getVmTemplates())
            }
            return templates
        }, LOGGER, [])
    }
    
    /**
     * Gets template information for all Azure VM templates.
     * 
     * <p>Returns configuration details for all Azure VM templates
     * configured in Jenkins.</p>
     * 
     * @return List of maps containing template information
     */
    List<Map<String, Object>> getAzureTemplatesInfo() {
        return ErrorHandler.withErrorHandling("getting Azure VM templates info", {
            def templates = getAzureTemplates()
            def result = []
            
            templates.each { template ->
                def templateInfo = [
                    templateName: template.getTemplateName(),
                    labels: template.getLabels(),
                    location: template.getLocation(),
                    vmSize: template.getVirtualMachineSize(),
                    retentionStrategy: template.getRetentionStrategy()?.getClass()?.getSimpleName(),
                    osType: template.getOsType(),
                    imageTopLevelType: template.getImageTopLevelType(),
                    launchMethod: template.getAgentLaunchMethod(),
                    storageAccountType: template.getStorageAccountType(),
                    diskType: template.getDiskType(),
                    noOfParallelJobs: template.getNoOfParallelJobs(),
                    usageMode: template.getUsageMode()?.toString()
                ]
                
                result.add(templateInfo)
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Provision a new Azure VM.
     * 
     * <p>Provisions a new Azure VM based on a template.</p>
     * 
     * @param templateName The name of the template to use
     * @param cloudName The name of the Azure cloud to use (optional, if multiple are configured)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    boolean provisionNewVM(String templateName, String cloudName = null) {
        ValidationUtils.requireNonEmpty(templateName, "Template name")
        
        return ErrorHandler.withErrorHandling("provisioning new Azure VM", {
            def clouds = cloudName ?
                getAzureClouds().findAll { it.name == cloudName } :
                getAzureClouds()
            
            if (clouds.isEmpty()) {
                LOGGER.warning("No Azure clouds found${cloudName ? " with name '${cloudName}'" : ""}")
                return false
            }
            
            // Find template by name
            for (cloud in clouds) {
                def template = cloud.getVmTemplates().find { it.templateName == templateName }
                if (template) {
                    // Provision a new node using this template
                    template.provision()
                    LOGGER.info("Provisioning initiated for new Azure VM from template: ${templateName}")
                    return true
                }
            }
            
            LOGGER.warning("No Azure template found with name: ${templateName}")
            return false
        }, LOGGER, false)
    }
    
    /**
     * Cleans up resources for an Azure VM agent.
     * 
     * <p>Cleans up Azure resources for the specified VM agent.</p>
     * 
     * @param nodeName The name of the node to clean up
     * @return true if cleanup was initiated successfully, false otherwise
     */
    boolean cleanupVM(String nodeName) {
        ValidationUtils.requireNonEmpty(nodeName, "Node name")
        
        return ErrorHandler.withErrorHandling("cleaning up Azure VM", {
            // Find node by name
            def node = jenkins.getNode(nodeName)
            
            if (!node || !(node instanceof AzureVMAgent)) {
                LOGGER.warning("No Azure VM agent found with name: ${nodeName}")
                return false
            }
            
            // Clean up the VM
            def azureAgent = node as AzureVMAgent
            azureAgent.deprovision()
            
            LOGGER.info("Azure VM cleanup initiated for: ${nodeName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Formats Azure node information for display.
     * 
     * <p>Creates a human-readable representation of Azure VM node details.</p>
     * 
     * @param nodeInfo The node information map to format
     * @return Formatted string with Azure VM node details
     */
    @Override
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }
        
        StringBuilder builder = new StringBuilder()
        builder.append("Azure VM Node: ${nodeInfo.name}\n")
        
        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")
        
        // Azure-specific information
        if (nodeInfo.azure) {
            builder.append("\nAzure Details:\n")
            builder.append("  VM Name: ${nodeInfo.azure.vmName}\n")
            builder.append("  Resource Group: ${nodeInfo.azure.resourceGroupName}\n")
            builder.append("  Location: ${nodeInfo.azure.location}\n")
            builder.append("  VM Size: ${nodeInfo.azure.vmSize}\n")
            builder.append("  OS Type: ${nodeInfo.azure.osType}\n")
            builder.append("  Launch Method: ${nodeInfo.azure.agentLaunchMethod}\n")
            builder.append("  Template: ${nodeInfo.azure.templateName}\n")
            builder.append("  Retention Strategy: ${nodeInfo.azure.retentionStrategy}\n")
        }
        
        return builder.toString()
    }
}