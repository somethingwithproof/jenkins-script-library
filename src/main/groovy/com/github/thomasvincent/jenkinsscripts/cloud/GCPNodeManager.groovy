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
import com.github.thomasvincent.jenkinsscripts.util.JenkinsEnvironment

import jenkins.model.Jenkins
import hudson.model.Node

import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages Google Cloud Platform (GCP) Compute Engine-based Jenkins agent nodes.
 *
 * <p>This class provides functionality for working with GCP Compute Engine instances
 * that serve as Jenkins agent nodes, through the Google Compute Engine Plugin.</p>
 *
 * <h3>Prerequisites:</h3>
 * <ul>
 *   <li>Google Compute Engine Plugin installed</li>
 *   <li>GCP credentials configured in Jenkins</li>
 *   <li>Compute Engine API enabled in GCP project</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * def gcpManager = new GCPNodeManager()
 *
 * // Check if GCP is configured
 * if (gcpManager.isGCPCloudConfigured()) {
 *     // List all GCP nodes
 *     gcpManager.getGCPNodesInfo().each { nodeInfo ->
 *         println gcpManager.formatNodeInfo(nodeInfo)
 *     }
 *
 *     // Provision a new instance
 *     gcpManager.provisionNewInstance('linux-agent-template', 'my-gcp-project')
 * }
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.2.0
 */
class GCPNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(GCPNodeManager.class.getName())
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    /** Plugin class name for GCP Compute Engine */
    private static final String GCP_CLOUD_CLASS = "com.google.jenkins.plugins.computeengine.ComputeEngineCloud"
    private static final String GCP_COMPUTER_CLASS = "com.google.jenkins.plugins.computeengine.ComputeEngineComputer"

    /** Cached reference to GCP cloud configurations */
    private List gcpClouds = null

    /** Cached GCP cloud class (loaded dynamically) */
    private Class gcpCloudClass = null

    /**
     * Creates a new GCPNodeManager.
     *
     * <p>Initializes the manager with the current Jenkins instance.</p>
     *
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    GCPNodeManager(Jenkins jenkins = null) {
        super(jenkins)
        loadGCPClasses()
    }

    /**
     * Attempts to load GCP plugin classes.
     *
     * <p>This is done lazily to support environments where the plugin
     * may not be installed.</p>
     */
    private void loadGCPClasses() {
        gcpCloudClass = JenkinsEnvironment.loadOptionalClass(
            GCP_CLOUD_CLASS,
            "google-compute-engine"
        )
    }

    /**
     * Gets all GCP cloud configurations.
     *
     * <p>Returns all ComputeEngineCloud instances configured in Jenkins.</p>
     *
     * @return List of GCP cloud configurations, or empty list if none found
     */
    List getGCPClouds() {
        if (gcpClouds == null) {
            gcpClouds = ErrorHandler.withErrorHandling("retrieving GCP clouds", {
                if (gcpCloudClass == null) {
                    LOGGER.info("GCP Compute Engine plugin is not installed")
                    return []
                }
                return jenkins.clouds.findAll { gcpCloudClass.isInstance(it) }
            }, LOGGER, [])
        }
        return gcpClouds
    }

    /**
     * Checks if the GCP Compute Engine plugin is installed and configured.
     *
     * <p>Verifies whether there are any GCP clouds configured in Jenkins.</p>
     *
     * @return true if GCP cloud is configured, false otherwise
     */
    boolean isGCPCloudConfigured() {
        return !getGCPClouds().isEmpty()
    }

    /**
     * Gets all GCP Compute Engine agent nodes.
     *
     * <p>Returns all nodes that are managed by GCP clouds.</p>
     *
     * @return List of GCP agent nodes
     */
    List<Node> getGCPNodes() {
        return ErrorHandler.withErrorHandling("retrieving GCP nodes", {
            def computerClass = JenkinsEnvironment.loadOptionalClass(GCP_COMPUTER_CLASS, null)
            if (computerClass == null) {
                return []
            }

            return jenkins.nodes.findAll { node ->
                computerClass.isInstance(node.computer)
            }
        }, LOGGER, [])
    }

    /**
     * Gets detailed information about all GCP agent nodes.
     *
     * <p>Returns comprehensive information including GCP-specific details
     * for all GCP agent nodes.</p>
     *
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getGCPNodesInfo() {
        return ErrorHandler.withErrorHandling("getting GCP nodes info", {
            def nodes = getGCPNodes()
            def result = []

            nodes.each { node ->
                def nodeInfo = extractGCPNodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Extracts detailed information about a GCP agent node.
     *
     * <p>Retrieves basic node information and adds GCP-specific details.</p>
     *
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractGCPNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")

        def info = extractNodeInfo(node)

        // Add GCP-specific information if available
        return ErrorHandler.withErrorHandling("extracting GCP node information", {
            def computer = node.computer

            // Use reflection to safely access GCP-specific methods
            if (computer != null) {
                try {
                    // Try to get instance details via reflection
                    def instance = computer.metaClass.respondsTo(computer, "getInstance") ?
                        computer.getInstance() : null

                    if (instance) {
                        info.gcp = [
                            instanceName: getPropertySafe(instance, "name"),
                            zone: extractZone(getPropertySafe(instance, "zone")),
                            machineType: extractMachineType(getPropertySafe(instance, "machineType")),
                            status: getPropertySafe(instance, "status"),
                            networkIp: extractNetworkIp(instance),
                            externalIp: extractExternalIp(instance),
                            creationTimestamp: getPropertySafe(instance, "creationTimestamp"),
                            preemptible: isPreemptible(instance),
                            labels: getPropertySafe(instance, "labels") ?: [:]
                        ]
                    }
                } catch (Exception e) {
                    LOGGER.fine("Could not extract GCP details: ${e.message}")
                }
            }

            return info
        }, LOGGER, info)
    }

    /**
     * Safely gets a property value from an object.
     *
     * @param obj The object to get the property from
     * @param propertyName The property name
     * @return The property value or null if not accessible
     */
    private static Object getPropertySafe(Object obj, String propertyName) {
        try {
            return obj?."${propertyName}"
        } catch (Exception e) {
            return null
        }
    }

    /**
     * Extracts the zone name from a full zone URL.
     *
     * @param zoneUrl The full zone URL
     * @return The zone name
     */
    private static String extractZone(String zoneUrl) {
        if (!zoneUrl) return "unknown"
        // Zone URL format: https://www.googleapis.com/compute/v1/projects/.../zones/us-central1-a
        def parts = zoneUrl.split('/')
        return parts.length > 0 ? parts[-1] : zoneUrl
    }

    /**
     * Extracts the machine type from a full machine type URL.
     *
     * @param machineTypeUrl The full machine type URL
     * @return The machine type name
     */
    private static String extractMachineType(String machineTypeUrl) {
        if (!machineTypeUrl) return "unknown"
        // Machine type URL format: .../machineTypes/n1-standard-1
        def parts = machineTypeUrl.split('/')
        return parts.length > 0 ? parts[-1] : machineTypeUrl
    }

    /**
     * Extracts the internal IP from a GCP instance.
     *
     * @param instance The GCP instance object
     * @return The internal IP address
     */
    private static String extractNetworkIp(Object instance) {
        try {
            def networkInterfaces = instance?.networkInterfaces
            if (networkInterfaces && networkInterfaces.size() > 0) {
                return networkInterfaces[0]?.networkIP
            }
        } catch (Exception e) {
            // Ignore
        }
        return null
    }

    /**
     * Extracts the external IP from a GCP instance.
     *
     * @param instance The GCP instance object
     * @return The external IP address
     */
    private static String extractExternalIp(Object instance) {
        try {
            def networkInterfaces = instance?.networkInterfaces
            if (networkInterfaces && networkInterfaces.size() > 0) {
                def accessConfigs = networkInterfaces[0]?.accessConfigs
                if (accessConfigs && accessConfigs.size() > 0) {
                    return accessConfigs[0]?.natIP
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null
    }

    /**
     * Checks if a GCP instance is preemptible.
     *
     * @param instance The GCP instance object
     * @return true if preemptible
     */
    private static boolean isPreemptible(Object instance) {
        try {
            return instance?.scheduling?.preemptible == true
        } catch (Exception e) {
            return false
        }
    }

    /**
     * Gets all available GCP instance templates.
     *
     * <p>Returns all instance configurations defined across all GCP clouds.</p>
     *
     * @return List of instance template configurations
     */
    List<Map<String, Object>> getGCPTemplatesInfo() {
        return ErrorHandler.withErrorHandling("retrieving GCP templates", {
            def result = []

            getGCPClouds().each { cloud ->
                try {
                    def configurations = cloud.metaClass.respondsTo(cloud, "getConfigurations") ?
                        cloud.getConfigurations() : []

                    configurations.each { config ->
                        def templateInfo = [
                            cloudName: cloud.name,
                            namePrefix: getPropertySafe(config, "namePrefix"),
                            description: getPropertySafe(config, "description"),
                            labels: getPropertySafe(config, "labelString"),
                            numExecutors: getPropertySafe(config, "numExecutors") ?: 1,
                            zone: getPropertySafe(config, "zone"),
                            machineType: getPropertySafe(config, "machineType"),
                            preemptible: getPropertySafe(config, "preemptible") ?: false,
                            bootDiskType: getPropertySafe(config, "bootDiskType"),
                            bootDiskSizeGb: getPropertySafe(config, "bootDiskSizeGb"),
                            bootDiskSourceImageName: getPropertySafe(config, "bootDiskSourceImageName"),
                            networkInterface: getPropertySafe(config, "network"),
                            serviceAccount: getPropertySafe(config, "serviceAccountEmail")
                        ]
                        result.add(templateInfo)
                    }
                } catch (Exception e) {
                    LOGGER.fine("Could not extract templates from cloud ${cloud.name}: ${e.message}")
                }
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Provisions a new GCP Compute Engine instance.
     *
     * <p>Provisions a new instance based on a configuration template.</p>
     *
     * @param configNamePrefix The name prefix of the configuration to use
     * @param cloudName The name of the GCP cloud to use (optional)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    boolean provisionNewInstance(String configNamePrefix, String cloudName = null) {
        ValidationUtils.requireNonEmpty(configNamePrefix, "Configuration name prefix")

        return ErrorHandler.withErrorHandling("provisioning new GCP instance", {
            def clouds = cloudName ?
                getGCPClouds().findAll { it.name == cloudName } :
                getGCPClouds()

            if (clouds.isEmpty()) {
                LOGGER.warning("No GCP clouds found${cloudName ? " with name '${cloudName}'" : ""}")
                return false
            }

            // Find configuration by name prefix
            for (cloud in clouds) {
                try {
                    def configurations = cloud.metaClass.respondsTo(cloud, "getConfigurations") ?
                        cloud.getConfigurations() : []

                    def config = configurations.find {
                        getPropertySafe(it, "namePrefix") == configNamePrefix
                    }

                    if (config) {
                        // Provision using the cloud's provision method
                        if (cloud.metaClass.respondsTo(cloud, "provision", config.class, int)) {
                            cloud.provision(config, 1)
                            LOGGER.info("Provisioning initiated for new GCP instance from config: ${configNamePrefix}")
                            return true
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warning("Failed to provision from cloud ${cloud.name}: ${e.message}")
                }
            }

            LOGGER.warning("No GCP configuration found with name prefix: ${configNamePrefix}")
            return false
        }, LOGGER, false)
    }

    /**
     * Terminates a GCP Compute Engine instance.
     *
     * <p>Terminates the specified instance and removes it from Jenkins.</p>
     *
     * @param nodeName The Jenkins node name to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    boolean terminateInstance(String nodeName) {
        ValidationUtils.requireNonEmpty(nodeName, "Node name")

        return ErrorHandler.withErrorHandling("terminating GCP instance", {
            def node = jenkins.getNode(nodeName)
            if (node == null) {
                LOGGER.warning("Node not found: ${nodeName}")
                return false
            }

            def computer = node.computer
            if (computer == null) {
                LOGGER.warning("Computer not available for node: ${nodeName}")
                return false
            }

            // Disconnect and terminate
            computer.disconnect(null)

            // Try to call terminate if available
            if (computer.metaClass.respondsTo(computer, "terminate")) {
                computer.terminate()
                LOGGER.info("GCP instance termination initiated for: ${nodeName}")
                return true
            } else {
                // Fall back to deleting the node
                jenkins.removeNode(node)
                LOGGER.info("Node removed from Jenkins: ${nodeName}")
                return true
            }
        }, LOGGER, false)
    }

    /**
     * Gets cost optimization recommendations for GCP nodes.
     *
     * <p>Analyzes current GCP usage and provides recommendations.</p>
     *
     * @return List of recommendations
     */
    List<Map<String, Object>> getCostOptimizationRecommendations() {
        return ErrorHandler.withErrorHandling("generating GCP cost recommendations", {
            def recommendations = []
            def nodesInfo = getGCPNodesInfo()

            // Check for idle non-preemptible instances
            nodesInfo.each { info ->
                if (info.offline && info.gcp?.preemptible == false) {
                    recommendations.add([
                        type: "IDLE_NON_PREEMPTIBLE",
                        severity: "MEDIUM",
                        node: info.name,
                        message: "Consider terminating idle non-preemptible instance or converting to preemptible",
                        estimatedSavings: "Up to 80% on compute costs"
                    ])
                }

                // Check for oversized instances
                def machineType = info.gcp?.machineType
                if (machineType?.contains("n1-highmem") || machineType?.contains("n1-highcpu")) {
                    recommendations.add([
                        type: "POTENTIALLY_OVERSIZED",
                        severity: "LOW",
                        node: info.name,
                        machineType: machineType,
                        message: "Review if high-memory/high-CPU instance type is necessary",
                        estimatedSavings: "Varies based on actual usage"
                    ])
                }
            }

            return recommendations
        }, LOGGER, [])
    }

    /**
     * Formats GCP node information for display.
     *
     * <p>Creates a human-readable representation of GCP node details.</p>
     *
     * @param nodeInfo The node information map to format
     * @return Formatted string with GCP node details
     */
    @Override
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }

        StringBuilder builder = new StringBuilder()
        builder.append("GCP Node: ${nodeInfo.name}\n")

        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")

        // GCP-specific information
        if (nodeInfo.gcp) {
            builder.append("\nGCP Details:\n")
            builder.append("  Instance Name: ${nodeInfo.gcp.instanceName}\n")
            builder.append("  Zone: ${nodeInfo.gcp.zone}\n")
            builder.append("  Machine Type: ${nodeInfo.gcp.machineType}\n")
            builder.append("  Status: ${nodeInfo.gcp.status}\n")
            builder.append("  Internal IP: ${nodeInfo.gcp.networkIp}\n")
            builder.append("  External IP: ${nodeInfo.gcp.externalIp ?: 'None'}\n")
            builder.append("  Preemptible: ${nodeInfo.gcp.preemptible}\n")
            builder.append("  Created: ${nodeInfo.gcp.creationTimestamp}\n")

            if (nodeInfo.gcp.labels) {
                builder.append("  Labels:\n")
                nodeInfo.gcp.labels.each { key, value ->
                    builder.append("    ${key}: ${value}\n")
                }
            }
        }

        return builder.toString()
    }
}
