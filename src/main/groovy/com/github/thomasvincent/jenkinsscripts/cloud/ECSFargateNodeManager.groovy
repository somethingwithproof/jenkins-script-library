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
 * Manages AWS ECS and Fargate-based Jenkins agent nodes.
 *
 * <p>This class provides functionality for working with AWS ECS (Elastic Container Service)
 * and Fargate tasks that serve as Jenkins agent nodes, through the Amazon ECS Plugin.</p>
 *
 * <h3>Prerequisites:</h3>
 * <ul>
 *   <li>Amazon ECS Plugin installed</li>
 *   <li>AWS credentials configured in Jenkins</li>
 *   <li>ECS cluster and task definitions configured</li>
 * </ul>
 *
 * <h3>Supported Launch Types:</h3>
 * <ul>
 *   <li>FARGATE - Serverless container execution</li>
 *   <li>EC2 - Container execution on EC2 instances</li>
 *   <li>EXTERNAL - On-premises container execution</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * def ecsManager = new ECSFargateNodeManager()
 *
 * // Check if ECS is configured
 * if (ecsManager.isECSCloudConfigured()) {
 *     // List all ECS nodes
 *     ecsManager.getECSNodesInfo().each { nodeInfo ->
 *         println ecsManager.formatNodeInfo(nodeInfo)
 *     }
 *
 *     // List available task templates
 *     ecsManager.getECSTemplatesInfo().each { template ->
 *         println "Template: ${template.label} - ${template.taskDefinition}"
 *     }
 * }
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.2.0
 */
class ECSFargateNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(ECSFargateNodeManager.class.getName())
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    /** Plugin class names for ECS */
    private static final String ECS_CLOUD_CLASS = "com.cloudbees.jenkins.plugins.amazonecs.ECSCloud"
    private static final String ECS_COMPUTER_CLASS = "com.cloudbees.jenkins.plugins.amazonecs.ECSComputer"
    private static final String ECS_SLAVE_CLASS = "com.cloudbees.jenkins.plugins.amazonecs.ECSSlave"

    /** AWS Fargate launch type */
    static final String LAUNCH_TYPE_FARGATE = "FARGATE"

    /** AWS EC2 launch type */
    static final String LAUNCH_TYPE_EC2 = "EC2"

    /** Cached reference to ECS cloud configurations */
    private List ecsClouds = null

    /** Cached ECS cloud class (loaded dynamically) */
    private Class ecsCloudClass = null

    /**
     * Creates a new ECSFargateNodeManager.
     *
     * <p>Initializes the manager with the current Jenkins instance.</p>
     *
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    ECSFargateNodeManager(Jenkins jenkins = null) {
        super(jenkins)
        loadECSClasses()
    }

    /**
     * Attempts to load ECS plugin classes.
     *
     * <p>This is done lazily to support environments where the plugin
     * may not be installed.</p>
     */
    private void loadECSClasses() {
        ecsCloudClass = JenkinsEnvironment.loadOptionalClass(
            ECS_CLOUD_CLASS,
            "amazon-ecs"
        )
    }

    /**
     * Gets all ECS cloud configurations.
     *
     * <p>Returns all ECSCloud instances configured in Jenkins.</p>
     *
     * @return List of ECS cloud configurations, or empty list if none found
     */
    List getECSClouds() {
        if (ecsClouds == null) {
            ecsClouds = ErrorHandler.withErrorHandling("retrieving ECS clouds", {
                if (ecsCloudClass == null) {
                    LOGGER.info("Amazon ECS plugin is not installed")
                    return []
                }
                return jenkins.clouds.findAll { ecsCloudClass.isInstance(it) }
            }, LOGGER, [])
        }
        return ecsClouds
    }

    /**
     * Checks if the ECS plugin is installed and configured.
     *
     * <p>Verifies whether there are any ECS clouds configured in Jenkins.</p>
     *
     * @return true if ECS cloud is configured, false otherwise
     */
    boolean isECSCloudConfigured() {
        return !getECSClouds().isEmpty()
    }

    /**
     * Gets all ECS agent nodes.
     *
     * <p>Returns all nodes that are managed by ECS clouds.</p>
     *
     * @return List of ECS agent nodes
     */
    List<Node> getECSNodes() {
        return ErrorHandler.withErrorHandling("retrieving ECS nodes", {
            def slaveClass = JenkinsEnvironment.loadOptionalClass(ECS_SLAVE_CLASS, null)
            if (slaveClass == null) {
                return []
            }

            return jenkins.nodes.findAll { node ->
                slaveClass.isInstance(node)
            }
        }, LOGGER, [])
    }

    /**
     * Gets all Fargate-specific agent nodes.
     *
     * <p>Returns only nodes running on AWS Fargate.</p>
     *
     * @return List of Fargate agent nodes
     */
    List<Node> getFargateNodes() {
        return getECSNodes().findAll { node ->
            def launchType = getNodeLaunchType(node)
            return launchType == LAUNCH_TYPE_FARGATE
        }
    }

    /**
     * Gets the launch type for an ECS node.
     *
     * @param node The ECS node
     * @return The launch type (FARGATE, EC2, or UNKNOWN)
     */
    private String getNodeLaunchType(Node node) {
        try {
            return node.metaClass.respondsTo(node, "getLaunchType") ?
                node.getLaunchType() : "UNKNOWN"
        } catch (Exception e) {
            return "UNKNOWN"
        }
    }

    /**
     * Gets detailed information about all ECS agent nodes.
     *
     * <p>Returns comprehensive information including ECS-specific details
     * for all ECS agent nodes.</p>
     *
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getECSNodesInfo() {
        return ErrorHandler.withErrorHandling("getting ECS nodes info", {
            def nodes = getECSNodes()
            def result = []

            nodes.each { node ->
                def nodeInfo = extractECSNodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Extracts detailed information about an ECS agent node.
     *
     * <p>Retrieves basic node information and adds ECS-specific details.</p>
     *
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractECSNodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")

        def info = extractNodeInfo(node)

        // Add ECS-specific information if available
        return ErrorHandler.withErrorHandling("extracting ECS node information", {
            info.ecs = [
                launchType: getNodeLaunchType(node),
                cluster: getPropertySafe(node, "clusterArn") ?:
                         extractClusterName(getPropertySafe(node, "cloud")?.name),
                taskArn: getPropertySafe(node, "taskArn"),
                taskDefinition: getPropertySafe(node, "taskDefinitionArn"),
                containerInstanceArn: getPropertySafe(node, "containerInstanceArn"),
                region: extractRegion(node),
                cpu: getPropertySafe(node, "cpu"),
                memory: getPropertySafe(node, "memory"),
                memoryReservation: getPropertySafe(node, "memoryReservation"),
                assignPublicIp: getPropertySafe(node, "assignPublicIp") ?: false,
                subnets: getPropertySafe(node, "subnets"),
                securityGroups: getPropertySafe(node, "securityGroups"),
                executionRole: getPropertySafe(node, "executionRoleArn"),
                taskRole: getPropertySafe(node, "taskRoleArn")
            ]

            // Add container details if available
            def containerDetails = extractContainerDetails(node)
            if (containerDetails) {
                info.ecs.container = containerDetails
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
     * Extracts the cluster name from cloud configuration.
     *
     * @param cloudName The cloud name
     * @return The cluster name or null
     */
    private String extractClusterName(String cloudName) {
        if (!cloudName) return null

        def cloud = getECSClouds().find { it.name == cloudName }
        return cloud ? getPropertySafe(cloud, "cluster") : null
    }

    /**
     * Extracts the AWS region from a node.
     *
     * @param node The ECS node
     * @return The AWS region
     */
    private String extractRegion(Node node) {
        // Try to get from node directly
        def region = getPropertySafe(node, "regionName")
        if (region) return region

        // Try to get from cloud configuration
        def cloudName = getPropertySafe(node, "cloud")?.name
        if (cloudName) {
            def cloud = getECSClouds().find { it.name == cloudName }
            region = getPropertySafe(cloud, "regionName")
            if (region) return region
        }

        // Try to extract from ARN
        def taskArn = getPropertySafe(node, "taskArn")
        if (taskArn && taskArn.contains(":")) {
            def parts = taskArn.split(":")
            if (parts.length > 3) {
                return parts[3]
            }
        }

        return "unknown"
    }

    /**
     * Extracts container details from an ECS node.
     *
     * @param node The ECS node
     * @return Map of container details
     */
    private Map<String, Object> extractContainerDetails(Node node) {
        try {
            return [
                image: getPropertySafe(node, "image"),
                containerName: getPropertySafe(node, "containerName") ?: "jenkins-agent",
                privileged: getPropertySafe(node, "privileged") ?: false,
                logDriver: getPropertySafe(node, "logDriver"),
                entrypoint: getPropertySafe(node, "entrypoint"),
                jvmArgs: getPropertySafe(node, "jvmArgs")
            ]
        } catch (Exception e) {
            return null
        }
    }

    /**
     * Gets all available ECS task templates.
     *
     * <p>Returns all task template configurations defined across all ECS clouds.</p>
     *
     * @return List of task template configurations
     */
    List<Map<String, Object>> getECSTemplatesInfo() {
        return ErrorHandler.withErrorHandling("retrieving ECS templates", {
            def result = []

            getECSClouds().each { cloud ->
                try {
                    def templates = cloud.metaClass.respondsTo(cloud, "getTemplates") ?
                        cloud.getTemplates() : []

                    templates.each { template ->
                        def templateInfo = [
                            cloudName: cloud.name,
                            label: getPropertySafe(template, "label"),
                            templateName: getPropertySafe(template, "templateName"),
                            image: getPropertySafe(template, "image"),
                            launchType: getPropertySafe(template, "launchType") ?: LAUNCH_TYPE_EC2,
                            cpu: getPropertySafe(template, "cpu"),
                            memory: getPropertySafe(template, "memory"),
                            memoryReservation: getPropertySafe(template, "memoryReservation"),
                            networkMode: getPropertySafe(template, "networkMode"),
                            assignPublicIp: getPropertySafe(template, "assignPublicIp") ?: false,
                            privileged: getPropertySafe(template, "privileged") ?: false,
                            remoteFSRoot: getPropertySafe(template, "remoteFSRoot") ?: "/home/jenkins",
                            platformVersion: getPropertySafe(template, "platformVersion"),
                            subnets: getPropertySafe(template, "subnets"),
                            securityGroups: getPropertySafe(template, "securityGroups"),
                            executionRoleArn: getPropertySafe(template, "executionRoleArn"),
                            taskRoleArn: getPropertySafe(template, "taskRoleArn"),
                            taskDefinitionOverride: getPropertySafe(template, "taskDefinitionOverride"),
                            logDriver: getPropertySafe(template, "logDriver"),
                            logDriverOptions: getPropertySafe(template, "logDriverOptions"),
                            isFargate: (getPropertySafe(template, "launchType") == LAUNCH_TYPE_FARGATE)
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
     * Gets Fargate-specific templates only.
     *
     * @return List of Fargate task templates
     */
    List<Map<String, Object>> getFargateTemplatesInfo() {
        return getECSTemplatesInfo().findAll { it.isFargate }
    }

    /**
     * Gets ECS cluster information.
     *
     * <p>Returns information about configured ECS clusters.</p>
     *
     * @return List of cluster configurations
     */
    List<Map<String, Object>> getClustersInfo() {
        return ErrorHandler.withErrorHandling("retrieving ECS clusters info", {
            def result = []

            getECSClouds().each { cloud ->
                def clusterInfo = [
                    cloudName: cloud.name,
                    cluster: getPropertySafe(cloud, "cluster"),
                    region: getPropertySafe(cloud, "regionName"),
                    jenkinsUrl: getPropertySafe(cloud, "jenkinsUrl"),
                    tunnel: getPropertySafe(cloud, "tunnel"),
                    slaveTimoutInSeconds: getPropertySafe(cloud, "slaveTimeoutInSeconds"),
                    retentionTimeout: getPropertySafe(cloud, "retentionTimeout"),
                    templateCount: (getPropertySafe(cloud, "templates")?.size() ?: 0)
                ]
                result.add(clusterInfo)
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Terminates an ECS task.
     *
     * <p>Stops the specified ECS task and removes the node from Jenkins.</p>
     *
     * @param nodeName The Jenkins node name to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    boolean terminateTask(String nodeName) {
        ValidationUtils.requireNonEmpty(nodeName, "Node name")

        return ErrorHandler.withErrorHandling("terminating ECS task", {
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

            // Try to call terminate if available (ECSSlave has this)
            if (node.metaClass.respondsTo(node, "terminate")) {
                node.terminate()
                LOGGER.info("ECS task termination initiated for: ${nodeName}")
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
     * Gets cost and usage statistics for ECS/Fargate.
     *
     * <p>Provides insights into ECS resource usage.</p>
     *
     * @return Map of usage statistics
     */
    Map<String, Object> getUsageStats() {
        return ErrorHandler.withErrorHandling("calculating ECS usage stats", {
            def nodes = getECSNodes()
            def fargateNodes = getFargateNodes()
            def ec2Nodes = nodes - fargateNodes

            // Calculate total resources
            def totalCpu = 0
            def totalMemory = 0
            nodes.each { node ->
                totalCpu += (getPropertySafe(node, "cpu") as Integer) ?: 0
                totalMemory += (getPropertySafe(node, "memory") as Integer) ?: 0
            }

            return [
                totalNodes: nodes.size(),
                fargateNodes: fargateNodes.size(),
                ec2LaunchNodes: ec2Nodes.size(),
                onlineNodes: nodes.count { !it.computer?.offline },
                offlineNodes: nodes.count { it.computer?.offline },
                totalCpuUnits: totalCpu,
                totalMemoryMB: totalMemory,
                clusters: getClustersInfo().size(),
                templates: getECSTemplatesInfo().size(),
                fargateTemplates: getFargateTemplatesInfo().size()
            ]
        }, LOGGER, [:])
    }

    /**
     * Gets cost optimization recommendations for ECS/Fargate.
     *
     * <p>Analyzes current usage and provides cost-saving suggestions.</p>
     *
     * @return List of recommendations
     */
    List<Map<String, Object>> getCostOptimizationRecommendations() {
        return ErrorHandler.withErrorHandling("generating ECS cost recommendations", {
            def recommendations = []
            def templates = getECSTemplatesInfo()
            def nodes = getECSNodesInfo()

            // Check for potentially oversized Fargate tasks
            templates.findAll { it.isFargate }.each { template ->
                def cpu = template.cpu as Integer
                def memory = template.memory as Integer

                // Fargate has specific CPU/memory combinations
                // Recommend smaller sizes if they seem oversized
                if (cpu >= 4096 || memory >= 16384) {
                    recommendations.add([
                        type: "OVERSIZED_FARGATE_TASK",
                        severity: "MEDIUM",
                        template: template.label,
                        cpu: cpu,
                        memory: memory,
                        message: "Consider if large Fargate task configuration is necessary",
                        suggestion: "Review task requirements - smaller tasks cost less"
                    ])
                }
            }

            // Check for idle nodes
            nodes.each { info ->
                if (info.offline) {
                    recommendations.add([
                        type: "IDLE_ECS_TASK",
                        severity: "LOW",
                        node: info.name,
                        launchType: info.ecs?.launchType,
                        message: "Idle ECS task consuming resources",
                        suggestion: "Consider reducing retention timeout or terminating"
                    ])
                }
            }

            // Check for Spot/EC2 opportunities
            def fargateTemplates = templates.findAll { it.isFargate }
            if (fargateTemplates.size() > 3) {
                recommendations.add([
                    type: "CONSIDER_FARGATE_SPOT",
                    severity: "LOW",
                    message: "Multiple Fargate templates detected",
                    suggestion: "Consider using Fargate Spot for fault-tolerant workloads (up to 70% savings)"
                ])
            }

            return recommendations
        }, LOGGER, [])
    }

    /**
     * Formats ECS node information for display.
     *
     * <p>Creates a human-readable representation of ECS node details.</p>
     *
     * @param nodeInfo The node information map to format
     * @return Formatted string with ECS node details
     */
    @Override
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }

        StringBuilder builder = new StringBuilder()
        builder.append("ECS Node: ${nodeInfo.name}\n")

        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")

        // ECS-specific information
        if (nodeInfo.ecs) {
            builder.append("\nECS Details:\n")
            builder.append("  Launch Type: ${nodeInfo.ecs.launchType}\n")
            builder.append("  Cluster: ${nodeInfo.ecs.cluster}\n")
            builder.append("  Region: ${nodeInfo.ecs.region}\n")
            builder.append("  Task ARN: ${nodeInfo.ecs.taskArn}\n")

            if (nodeInfo.ecs.cpu || nodeInfo.ecs.memory) {
                builder.append("  Resources: ${nodeInfo.ecs.cpu} CPU units, ${nodeInfo.ecs.memory} MB memory\n")
            }

            if (nodeInfo.ecs.assignPublicIp) {
                builder.append("  Public IP: Assigned\n")
            }

            if (nodeInfo.ecs.container) {
                builder.append("\n  Container:\n")
                builder.append("    Image: ${nodeInfo.ecs.container.image}\n")
                builder.append("    Name: ${nodeInfo.ecs.container.containerName}\n")
                if (nodeInfo.ecs.container.privileged) {
                    builder.append("    Privileged: true\n")
                }
            }
        }

        return builder.toString()
    }
}
