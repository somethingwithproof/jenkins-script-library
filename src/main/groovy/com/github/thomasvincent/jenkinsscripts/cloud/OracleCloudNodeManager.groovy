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
import com.oracle.cloud.compute.jenkins.OracleCloudComputer
import com.oracle.cloud.compute.jenkins.OracleCloud
import com.oracle.cloud.compute.jenkins.template.OCITemplate

import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages Oracle Cloud Infrastructure (OCI) based Jenkins agent nodes.
 * 
 * <p>This class provides functionality for working with OCI instances
 * that serve as Jenkins agent nodes, through the Oracle Cloud Infrastructure Compute plugin.</p>
 * 
 * @author Thomas Vincent
 * @since 1.3.0
 */
class OracleCloudNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(OracleCloudNodeManager.class.getName())
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial({ 
        new SimpleDateFormat("yyyy-MM-dd HH:mm") 
    })
    
    /**
     * Cached reference to the Oracle Cloud configuration.
     * Set lazily on first access.
     */
    private List<OracleCloud> oracleClouds = null
    
    /**
     * Creates a new OracleCloudNodeManager.
     * 
     * <p>Initializes the manager with the current Jenkins instance.</p>
     * 
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    OracleCloudNodeManager(Jenkins jenkins = null) {
        super(jenkins)
    }
    
    /**
     * Gets all Oracle Cloud configurations.
     * 
     * <p>Returns all OracleCloud instances configured in Jenkins.</p>
     * 
     * @return List of OracleCloud configurations, or empty list if none found
     */
    List<OracleCloud> getOracleClouds() {
        if (this.oracleClouds == null) {
            // Use the helper from the superclass
            this.oracleClouds = getCachedCloudsByType(OracleCloud.class, "Oracle Cloud")
        }
        return this.oracleClouds
    }
    
    /**
     * Checks if the Oracle Cloud plugin is installed and configured.
     * 
     * <p>Verifies whether there are any Oracle Cloud configurations in Jenkins.</p>
     * 
     * @return true if Oracle Cloud is configured, false otherwise
     */
    @Override
    boolean isProviderConfigured() {
        return super.isCloudConfigured(OracleCloud.class)
    }
    
    /**
     * Gets all Oracle Cloud agent nodes.
     * 
     * <p>Returns all nodes that are managed by Oracle Cloud.</p>
     * 
     * @return List of Oracle Cloud agent nodes
     */
    @Override
    List<Node> getManagedNodes() {
        return getOracleCloudNodes()
    }

    /**
     * Gets all Oracle Cloud agent nodes (specific implementation).
     *
     * @return List of Oracle Cloud agent nodes
     */
    List<Node> getOracleCloudNodes() {
        return ErrorHandler.withErrorHandling("retrieving Oracle Cloud nodes", {
            // Find nodes with computers that are instances of OracleCloudComputer
            return jenkins.nodes.findAll { node ->
                node.computer instanceof OracleCloudComputer
            }
        }, LOGGER, [])
    }
    
    /**
     * Gets detailed information about all Oracle Cloud agent nodes.
     * 
     * <p>Returns comprehensive information including OCI-specific details
     * for all Oracle Cloud agent nodes.</p>
     * 
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getOracleCloudNodesInfo() {
        return ErrorHandler.withErrorHandling("getting Oracle Cloud nodes info", {
            def nodes = getOracleCloudNodes()
            def result = []
            
            nodes.each { node ->
                def nodeInfo = extractOracleCloudNodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * {@inheritDoc}
     * Extracts Oracle Cloud-specific information for a node.
     */
    @Override
    public Map<String, Object> extractProviderSpecificNodeInfo(Node node) {
        return extractOracleCloudNodeInfo(node)
    }
    
    /**
     * Extracts detailed information about an Oracle Cloud agent node.
     * 
     * <p>Retrieves basic node information and adds OCI-specific details.</p>
     * 
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractOracleCloudNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")
        
        def info = extractNodeInfo(node)
        
        // Add Oracle Cloud-specific information if available
        if (node.computer instanceof OracleCloudComputer) {
            def ociComputer = node.computer as OracleCloudComputer
            
            return ErrorHandler.withErrorHandling("extracting Oracle Cloud node information", {
                info.oci = [
                    instanceId: ociComputer.getInstanceId(),
                    instanceName: ociComputer.getNodeName(),
                    compartmentId: ociComputer.getCompartmentId(),
                    availabilityDomain: ociComputer.getAvailabilityDomain(),
                    vcnId: ociComputer.getVcnId(),
                    subnetId: ociComputer.getSubnetId(),
                    shape: ociComputer.getShape(),
                    templateId: ociComputer.getTemplateId(),
                    ocpus: ociComputer.getOcpus(),
                    memoryInGBs: ociComputer.getMemoryInGBs(),
                    region: ociComputer.getRegion(),
                    privateIp: ociComputer.getPrivateIp(),
                    publicIp: ociComputer.getPublicIp(),
                    instanceState: ociComputer.getInstanceState(),
                    launchTime: ociComputer.getLaunchTime() ? DATE_FORMATTER.get().format(ociComputer.getLaunchTime()) : null
                ]
                return info
            }, LOGGER, info)
        }
        
        return info
    }
    
    /**
     * Gets all available Oracle Cloud templates.
     * 
     * <p>Returns all OCITemplates configured across all Oracle Cloud configurations.</p>
     * 
     * @return List of Oracle Cloud templates
     */
    @Override
    List<OCITemplate> getResourceTemplates() {
        return getOracleCloudTemplates()
    }

    /**
     * Gets all available Oracle Cloud templates (specific implementation).
     *
     * @return List of Oracle Cloud templates
     */
    List<OCITemplate> getOracleCloudTemplates() {
        return ErrorHandler.withErrorHandling("retrieving Oracle Cloud templates", {
            def templates = []
            getOracleClouds().each { cloud ->
                templates.addAll(cloud.getTemplates())
            }
            return templates
        }, LOGGER, [])
    }
    
    /**
     * Gets template information for all Oracle Cloud templates.
     * 
     * <p>Returns configuration details for all Oracle Cloud templates
     * configured in Jenkins.</p>
     * 
     * @return List of maps containing template information
     */
    @Override
    List<Map<String, Object>> getResourceTemplatesInfo() {
        return getOracleCloudTemplatesInfo()
    }

    /**
     * Gets template information for all Oracle Cloud templates (specific implementation).
     *
     * @return List of maps containing template information
     */
    List<Map<String, Object>> getOracleCloudTemplatesInfo() {
        return ErrorHandler.withErrorHandling("getting Oracle Cloud templates info", {
            def templates = getOracleCloudTemplates()
            def result = []
            
            templates.each { template ->
                def templateInfo = [
                    description: template.getDescription(),
                    templateId: template.getTemplateId(),
                    compartmentId: template.getCompartmentId(),
                    availabilityDomain: template.getAvailabilityDomain(),
                    vcnCompartmentId: template.getVcnCompartmentId(),
                    vcnId: template.getVcnId(),
                    subnetCompartmentId: template.getSubnetCompartmentId(),
                    subnetId: template.getSubnetId(),
                    imageCompartmentId: template.getImageCompartmentId(),
                    imageId: template.getImageId(),
                    shape: template.getShape(),
                    ocpus: template.getOcpus(),
                    memoryInGBs: template.getMemoryInGBs(),
                    sshPublicKey: template.getSshPublicKey() ? "(SSH key provided)" : "(None)",
                    labels: template.getLabelString(),
                    mode: template.getMode()?.toString(),
                    numExecutors: template.getNumExecutors(),
                    remoteFS: template.getRemoteFS(),
                    assignPublicIP: template.getAssignPublicIP(),
                    usePublicIP: template.getUsePublicIP(),
                    timeout: template.getTimeout()
                ]
                
                result.add(templateInfo)
            }
            
            return result
        }, LOGGER, [])
    }
    
    /**
     * Provision a new Oracle Cloud instance.
     * 
     * <p>Provisions a new Oracle Cloud instance based on a template.</p>
     * 
     * @param templateId The id of the template to use
     * @param cloudName The name of the Oracle Cloud configuration to use (optional, if multiple are configured)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    @Override
    boolean provisionNewResource(String templateIdentifier, String cloudInstanceName = null) {
        return provisionNewInstance(templateIdentifier, cloudInstanceName)
    }

    /**
     * Provision a new Oracle Cloud instance (specific implementation).
     *
     * @param templateId The id of the template to use
     * @param cloudName The name of the Oracle Cloud configuration to use (optional, if multiple are configured)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    boolean provisionNewInstance(String templateId, String cloudName = null) {
        ValidationUtils.requireNonEmpty(templateId, "Template ID")
        
        return ErrorHandler.withErrorHandling("provisioning new Oracle Cloud instance", {
            def clouds = cloudName ?
                getOracleClouds().findAll { it.name == cloudName } :
                getOracleClouds()
            
            if (clouds.isEmpty()) {
                LOGGER.warning("No Oracle Cloud configurations found${cloudName ? " with name '${cloudName}'" : ""}")
                return false
            }
            
            // Find template by ID
            for (cloud in clouds) {
                def template = cloud.getTemplates().find { it.getTemplateId() == templateId }
                if (template) {
                    // Provision a new node using this template
                    cloud.provision(template, 1)
                    LOGGER.info("Provisioning initiated for new Oracle Cloud instance from template: ${templateId}")
                    return true
                }
            }
            
            LOGGER.warning("No Oracle Cloud template found with ID: ${templateId}")
            return false
        }, LOGGER, false)
    }
    
    /**
     * Terminates an Oracle Cloud instance.
     * 
     * <p>Terminates the specified Oracle Cloud instance and removes it from Jenkins.</p>
     * 
     * @param instanceId The instance ID to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    @Override
    boolean terminateResource(String resourceIdentifier, String nodeNameHint = null) {
        return terminateInstance(resourceIdentifier)
    }

    /**
     * Terminates an Oracle Cloud instance (specific implementation).
     *
     * @param instanceId The instance ID to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    boolean terminateInstance(String instanceId) {
        ValidationUtils.requireNonEmpty(instanceId, "Instance ID")
        
        return ErrorHandler.withErrorHandling("terminating Oracle Cloud instance", {
            // Find node by instance ID
            def nodes = getOracleCloudNodesInfo()
            def nodeInfo = nodes.find { it.oci?.instanceId == instanceId }
            
            if (!nodeInfo) {
                LOGGER.warning("No Oracle Cloud instance found with ID: ${instanceId}")
                return false
            }
            
            // Get the node and its computer
            def node = jenkins.getNode(nodeInfo.name as String)
            if (!node || !(node.computer instanceof OracleCloudComputer)) {
                LOGGER.warning("Node not found or not an Oracle Cloud node: ${nodeInfo.name}")
                return false
            }
            
            // Terminate the instance
            def computer = node.computer as OracleCloudComputer
            computer.terminate()
            
            LOGGER.info("Oracle Cloud instance termination initiated for: ${instanceId}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Formats Oracle Cloud node information for display.
     * 
     * <p>Creates a human-readable representation of Oracle Cloud node details.</p>
     * 
     * @param nodeInfo The node information map to format
     * @return Formatted string with Oracle Cloud node details
     */
    @Override
    String formatProviderNodeInfo(Map<String, Object> nodeInfo) {
        if (nodeInfo == null || nodeInfo.isEmpty()) {
            return "No information available"
        }
        
        StringBuilder builder = new StringBuilder()
        builder.append("Oracle Cloud Node: ${nodeInfo.name}\n")
        
        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")
        
        // OCI-specific information
        if (nodeInfo.oci) {
            builder.append("\nOracle Cloud Details:\n")
            builder.append("  Instance ID: ${nodeInfo.oci.instanceId}\n")
            builder.append("  Region: ${nodeInfo.oci.region}\n")
            builder.append("  Availability Domain: ${nodeInfo.oci.availabilityDomain}\n")
            builder.append("  Compartment ID: ${nodeInfo.oci.compartmentId}\n")
            builder.append("  Shape: ${nodeInfo.oci.shape}\n")
            if (nodeInfo.oci.ocpus) {
                builder.append("  OCPUs: ${nodeInfo.oci.ocpus}\n")
            }
            if (nodeInfo.oci.memoryInGBs) {
                builder.append("  Memory (GB): ${nodeInfo.oci.memoryInGBs}\n")
            }
            builder.append("  State: ${nodeInfo.oci.instanceState}\n")
            builder.append("  Private IP: ${nodeInfo.oci.privateIp}\n")
            if (nodeInfo.oci.publicIp) {
                builder.append("  Public IP: ${nodeInfo.oci.publicIp}\n")
            }
            if (nodeInfo.oci.launchTime) {
                builder.append("  Launch Time: ${nodeInfo.oci.launchTime}\n")
            }
            builder.append("  Template ID: ${nodeInfo.oci.templateId}\n")
        }
        
        return builder.toString()
    }

    /**
     * Gets Oracle Cloud nodes if the specified cloud class matches OracleCloud.
     *
     * <p>Overrides the base implementation to provide specific node retrieval
     * for Oracle Cloud.</p>
     *
     * @param cloudClassToCheck The specific cloud class type to check against.
     * @return List of Oracle Cloud nodes if cloudClassToCheck is OracleCloud, otherwise an empty list.
     */
    @Override
    protected List<Node> getCloudNodes(Class cloudClassToCheck) {
        if (OracleCloud.class.isAssignableFrom(cloudClassToCheck)) {
            // Delegate to the method that specifically gets Oracle Cloud nodes
            return getOracleCloudNodes()
        }
        // This manager specifically handles Oracle Cloud, so return empty for other types
        return []
    }
}