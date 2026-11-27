// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.cloud

import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Node
import hudson.slaves.Cloud
import jenkins.model.Jenkins

/** Local plugin metadata only. Provider plugins are optional on a controller. */
class CloudInventory {
    private static final Map CLOUD_TYPES = [
        'hudson.plugins.ec2.EC2Cloud': 'AWS',
        'com.microsoft.azure.vmagent.AzureVMCloud': 'Azure',
        'org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud': 'Kubernetes',
        'com.oracle.cloud.baremetal.jenkins.BaremetalCloud': 'Oracle'
    ]
    private static final Map NODE_TYPES = [
        'hudson.plugins.ec2.EC2AbstractSlave': 'AWS',
        'com.microsoft.azure.vmagent.AzureVMAgent': 'Azure',
        'org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave': 'Kubernetes',
        'com.oracle.cloud.baremetal.jenkins.BaremetalCloudAgent': 'Oracle'
    ]

    @NonCPS
    private static String provider(Object item, Map types) {
        for (Class type = item?.getClass(); type != null; type = type.superclass) {
            if (types.containsKey(type.name)) {
                return types[type.name]
            }
        }
        return null
    }

    @NonCPS
    Map owner(Node node) {
        String id = provider(node, NODE_TYPES)
        if (!id) return null
        Cloud cloud
        try {
            cloud = node.getCloud()
        } catch (ClassCastException staleOwner) {
            // A different plugin may have replaced the cloud at this name.
            return null
        }
        if (provider(cloud, CLOUD_TYPES) != id) return null
        return [provider: id, cloudName: cloud.name, agentId: node.nodeName]
    }

    @NonCPS
    Map inspect(Jenkins controller) {
        controller.checkPermission(Jenkins.SYSTEM_READ)
        def clouds = []
        for (Cloud cloud : controller.clouds) {
            String id = provider(cloud, CLOUD_TYPES)
            if (id) clouds.add([provider: id, cloudName: cloud.name])
        }
        def agents = []
        for (Node node : controller.nodes) {
            Map identity = owner(node)
            if (identity) agents.add(identity + [online: node.toComputer()?.online == true])
        }
        return [clouds: clouds.sort { it.cloudName }, agents: agents.sort { it.agentId }]
    }
}
