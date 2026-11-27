// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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
import hudson.plugins.ec2.EC2Cloud
import hudson.plugins.ec2.EC2Computer
import hudson.plugins.ec2.SlaveTemplate

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages AWS EC2-based Jenkins agent nodes.
 *
 * <p>This class provides functionality for working with AWS EC2 instances
 * that serve as Jenkins agent nodes, through the EC2 Plugin.</p>
 *
 * @author Thomas Vincent
 * @since 1.1.0
 */
class AWSNodeManager extends CloudNodesManager {
    private static final Logger LOGGER = Logger.getLogger(AWSNodeManager.class.getName())
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    /**
     * Cached reference to the EC2 cloud configuration.
     * Set lazily on first access.
     */
    private List<EC2Cloud> ec2Clouds = null

    /**
     * Creates a new AWSNodeManager.
     *
     * <p>Initializes the manager with the current Jenkins instance.</p>
     *
     * @param jenkins The Jenkins instance to use (defaults to Jenkins.get() if null)
     */
    AWSNodeManager(Jenkins jenkins = null) {
        super(jenkins)
    }

    /**
     * Gets all EC2 cloud configurations.
     *
     * <p>Returns all EC2Cloud instances configured in Jenkins.</p>
     *
     * @return List of EC2Cloud configurations, or empty list if none found
     */
    List<EC2Cloud> getEC2Clouds() {
        if (ec2Clouds == null) {
            ec2Clouds = ErrorHandler.withErrorHandling("retrieving EC2 clouds", {
                return jenkins.clouds.findAll { it instanceof EC2Cloud } as List<EC2Cloud>
            }, LOGGER, [])
        }
        return ec2Clouds
    }

    /**
     * Checks if the EC2 cloud plugin is installed and configured.
     *
     * <p>Verifies whether there are any EC2 clouds configured in Jenkins.</p>
     *
     * @return true if EC2 cloud is configured, false otherwise
     */
    boolean isEC2CloudConfigured() {
        return !getEC2Clouds().isEmpty()
    }

    /**
     * Gets all EC2 agent nodes.
     *
     * <p>Returns all nodes that are managed by EC2 clouds.</p>
     *
     * @return List of EC2 agent nodes
     */
    List<Node> getEC2Nodes() {
        return ErrorHandler.withErrorHandling("retrieving EC2 nodes", {
            // Find nodes with computers that are instances of EC2Computer
            return jenkins.nodes.findAll { node ->
                node.computer instanceof EC2Computer
            }
        }, LOGGER, [])
    }

    /**
     * Gets detailed information about all EC2 agent nodes.
     *
     * <p>Returns comprehensive information including EC2-specific details
     * for all EC2 agent nodes.</p>
     *
     * @return List of maps containing detailed node information
     */
    List<Map<String, Object>> getEC2NodesInfo() {
        return ErrorHandler.withErrorHandling("getting EC2 nodes info", {
            def nodes = getEC2Nodes()
            def result = []

            nodes.each { node ->
                def nodeInfo = extractEC2NodeInfo(node)
                if (nodeInfo) {
                    result.add(nodeInfo)
                }
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Extracts detailed information about an EC2 agent node.
     *
     * <p>Retrieves basic node information and adds EC2-specific details.</p>
     *
     * @param node The node to extract information from
     * @return Map containing detailed node information
     */
    Map<String, Object> extractEC2NodeInfo(Node node) {
        ValidationUtils.requireNonNull(node, "Node instance")

        def info = extractNodeInfo(node)

        // Add EC2-specific information if available
        if (node.computer instanceof EC2Computer) {
            def ec2Computer = node.computer as EC2Computer

            return ErrorHandler.withErrorHandling("extracting EC2 node information", {
                def instance = ec2Computer.describeInstance()
                if (instance) {
                    info.ec2 = [
                        instanceId: instance.getInstanceId(),
                        instanceType: instance.getInstanceType(),
                        privateIp: instance.getPrivateIpAddress(),
                        publicIp: instance.getPublicIpAddress(),
                        amiId: instance.getImageId(),
                        launchTime: formatLaunchTime(instance.getLaunchTime()),
                        state: instance.getState()?.getName(),
                        region: findInstanceRegion(ec2Computer),
                        tags: instance.getTags()?.collectEntries { [(it.key): it.value] }
                    ]
                }
                return info
            }, LOGGER, info)
        }

        return info
    }

    /**
     * Finds the AWS region for an EC2 instance.
     *
     * <p>Attempts to determine the AWS region for the instance
     * associated with the given EC2Computer.</p>
     *
     * @param ec2Computer The EC2Computer to find the region for
     * @return The AWS region, or null if not found
     */
    private String findInstanceRegion(EC2Computer ec2Computer) {
        return ErrorHandler.withErrorHandling("finding EC2 instance region", {
            // Try to get region from EC2 computer or parent cloud
            def parent = ec2Computer.getCloud()
            return parent?.getRegion() ?: "unknown"
        }, LOGGER, "unknown")
    }

    /**
     * Gets all available EC2 templates.
     *
     * <p>Returns all EC2 templates configured across all EC2 clouds.</p>
     *
     * @return List of EC2 templates
     */
    List<SlaveTemplate> getEC2Templates() {
        return ErrorHandler.withErrorHandling("retrieving EC2 templates", {
            def templates = []
            getEC2Clouds().each { cloud ->
                templates.addAll(cloud.getTemplates())
            }
            return templates
        }, LOGGER, [])
    }

    /**
     * Gets template information for all EC2 templates.
     *
     * <p>Returns configuration details for all EC2 templates
     * configured in Jenkins.</p>
     *
     * @return List of maps containing template information
     */
    List<Map<String, Object>> getEC2TemplatesInfo() {
        return ErrorHandler.withErrorHandling("getting EC2 templates info", {
            def templates = getEC2Templates()
            def result = []

            templates.each { template ->
                def templateInfo = [
                    description: template.description,
                    ami: template.ami,
                    instanceType: template.type.name,
                    labels: template.labelString,
                    numExecutors: template.numExecutors,
                    remoteFS: template.remoteFS,
                    securityGroups: template.securityGroupString,
                    userData: template.userData ? "(user data script provided)" : "(none)",
                    spotInstance: template.spotConfig != null
                ]

                // Add spot details if applicable
                if (template.spotConfig) {
                    templateInfo.spotDetails = [
                        maxBidPrice: template.spotConfig.spotMaxBidPrice,
                        fallbackToOnDemand: template.spotConfig.fallbackToOndemand
                    ]
                }

                result.add(templateInfo)
            }

            return result
        }, LOGGER, [])
    }

    /**
     * Provision a new EC2 instance.
     *
     * <p>Provisions a new EC2 instance based on a template.</p>
     *
     * @param templateDescription The description of the template to use
     * @param cloudName The name of the EC2 cloud to use (optional, if multiple are configured)
     * @return true if provisioning was initiated successfully, false otherwise
     */
    boolean provisionNewInstance(String templateDescription, String cloudName = null) {
        ValidationUtils.requireNonEmpty(templateDescription, "Template description")

        return ErrorHandler.withErrorHandling("provisioning new EC2 instance", {
            def clouds = cloudName ?
                getEC2Clouds().findAll { it.name == cloudName } :
                getEC2Clouds()

            if (clouds.isEmpty()) {
                LOGGER.warning("No EC2 clouds found${cloudName ? " with name '${cloudName}'" : ""}")
                return false
            }

            // Find template by description
            for (cloud in clouds) {
                def template = cloud.getTemplates().find { it.description == templateDescription }
                if (template) {
                    // Provision a new node using this template
                    cloud.provision(template, 1)
                    LOGGER.info("Provisioning initiated for new EC2 instance from template: ${templateDescription}")
                    return true
                }
            }

            LOGGER.warning("No EC2 template found with description: ${templateDescription}")
            return false
        }, LOGGER, false)
    }

    private static String formatLaunchTime(Object launchTime) {
        if (launchTime == null) {
            return null
        }
        if (launchTime instanceof Instant) {
            return DATE_FORMATTER.format((Instant) launchTime)
        }
        if (launchTime instanceof Date) {
            return DATE_FORMATTER.format(((Date) launchTime).toInstant())
        }
        return launchTime.toString()
    }

    /**
     * Terminates an EC2 instance.
     *
     * <p>Terminates the specified EC2 instance and removes it from Jenkins.</p>
     *
     * @param instanceId The EC2 instance ID to terminate
     * @return true if termination was initiated successfully, false otherwise
     */
    boolean terminateInstance(String instanceId) {
        ValidationUtils.requireNonEmpty(instanceId, "Instance ID")

        return ErrorHandler.withErrorHandling("terminating EC2 instance", {
            // Locate matching EC2 computer directly in O(N) without describing unrelated instances twice
            EC2Computer targetComputer = null
            for (Node node : getEC2Nodes()) {
                if (node.computer instanceof EC2Computer) {
                    EC2Computer candidate = (EC2Computer) node.computer
                    def instance = candidate.describeInstance()
                    if (instance?.getInstanceId() == instanceId) {
                        targetComputer = candidate
                        break
                    }
                }
            }

            if (targetComputer == null) {
                LOGGER.warning("No EC2 instance found with ID: ${instanceId}")
                return false
            }

            targetComputer.disconnect(null)
            targetComputer.getCloud().doTerminate(targetComputer)

            LOGGER.info("EC2 instance termination initiated for: ${instanceId}")
            return true
        }, LOGGER, false)
    }

    /**
     * Formats EC2 node information for display.
     *
     * <p>Creates a human-readable representation of EC2 node details.</p>
     *
     * @param nodeInfo The node information map to format
     * @return Formatted string with EC2 node details
     */
    @Override
    String formatNodeInfo(Map<String, Object> nodeInfo) {
        if (!nodeInfo) {
            return "No information available"
        }

        StringBuilder builder = new StringBuilder()
        builder.append("EC2 Node: ${nodeInfo.name}\n")

        // Basic node information
        builder.append("Status: ${nodeInfo.offline ? 'OFFLINE' : 'ONLINE'}\n")
        if (nodeInfo.offline && nodeInfo.offlineCause) {
            builder.append("Offline Cause: ${nodeInfo.offlineCause}\n")
        }
        builder.append("Executors: ${nodeInfo.numExecutors}\n")
        builder.append("Labels: ${nodeInfo.labels}\n")

        // EC2-specific information
        if (nodeInfo.ec2) {
            builder.append("\nEC2 Details:\n")
            builder.append("  Instance ID: ${nodeInfo.ec2.instanceId}\n")
            builder.append("  Type: ${nodeInfo.ec2.instanceType}\n")
            builder.append("  Region: ${nodeInfo.ec2.region}\n")
            builder.append("  State: ${nodeInfo.ec2.state}\n")
            builder.append("  Private IP: ${nodeInfo.ec2.privateIp}\n")
            builder.append("  Public IP: ${nodeInfo.ec2.publicIp}\n")
            builder.append("  Launch Time: ${nodeInfo.ec2.launchTime}\n")

            if (nodeInfo.ec2.tags) {
                builder.append("  Tags:\n")
                nodeInfo.ec2.tags.each { key, value ->
                    builder.append("    ${key}: ${value}\n")
                }
            }
        }

        return builder.toString()
    }
}
