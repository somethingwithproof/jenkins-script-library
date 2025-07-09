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

import com.github.thomasvincent.jenkinsscripts.DockerIntegrationTest
import spock.lang.Requires
import spock.lang.Unroll

/**
 * Integration tests for cloud node management scripts.
 * 
 * Tests the interaction with various cloud providers (AWS, Azure, Kubernetes)
 * using mock services running in Docker.
 */
class CloudNodeManagerIntegrationTest extends DockerIntegrationTest {
    
    def setup() {
        // Create some test nodes for cloud simulation
        createTestNode('test-aws-node-1', [
            labels: 'aws ec2 linux',
            description: 'AWS EC2 test node'
        ])
        
        createTestNode('test-azure-node-1', [
            labels: 'azure windows',
            description: 'Azure VM test node'
        ])
        
        createTestNode('test-k8s-node-1', [
            labels: 'kubernetes docker linux',
            description: 'Kubernetes pod test node'
        ])
    }
    
    def "test ListCloudNodes script lists all cloud nodes"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ListCloudNodes.groovy')
        
        then:
        result != null
        result.contains('test-aws-node-1')
        result.contains('test-azure-node-1')
        result.contains('test-k8s-node-1')
        result.contains('Cloud Node Report')
    }
    
    @Unroll
    def "test #cloudType node manager identifies #nodeType nodes"() {
        given:
        def scriptPath = "src/main/groovy/com/github/thomasvincent/jenkinsscripts/cloud/${managerClass}.groovy"
        
        when:
        def script = """
import com.github.thomasvincent.jenkinsscripts.cloud.${managerClass}

def manager = new ${managerClass}()
def nodes = manager.getNodes()

println "Found ${nodes.size()} ${nodeType} nodes"
nodes.each { node ->
    println "- \${node.displayName}: \${node.nodeDescription}"
}
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains("${nodeType} nodes")
        
        where:
        cloudType   | managerClass          | nodeType
        'AWS'       | 'AWSNodeManager'      | 'AWS'
        'Azure'     | 'AzureNodeManager'    | 'Azure'
        'K8s'       | 'KubernetesNodeManager' | 'Kubernetes'
    }
    
    def "test ManageEC2Agents script with mock LocalStack"() {
        given:
        // Configure AWS credentials for LocalStack
        def setupScript = """
System.setProperty('aws.accessKeyId', 'test')
System.setProperty('aws.secretKey', 'test')
System.setProperty('aws.region', 'us-east-1')
System.setProperty('aws.endpoint', 'http://localstack:4566')
"""
        httpClient.executeScript(setupScript)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageEC2Agents.groovy', [
            action: 'list'
        ])
        
        then:
        result != null
        // Should handle LocalStack connection gracefully
        result.contains('EC2') || result.contains('AWS')
    }
    
    def "test ManageAzureVMAgents script"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageAzureVMAgents.groovy', [
            action: 'list'
        ])
        
        then:
        result != null
        result.contains('Azure') || result.contains('azure')
    }
    
    def "test ManageKubernetesAgents script"() {
        given:
        // Configure Kubernetes connection
        def setupScript = """
System.setProperty('kubernetes.master', 'https://kind:6443')
System.setProperty('kubernetes.skipTlsVerify', 'true')
"""
        httpClient.executeScript(setupScript)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageKubernetesAgents.groovy', [
            action: 'list',
            namespace: 'default'
        ])
        
        then:
        result != null
        result.contains('Kubernetes') || result.contains('k8s')
    }
    
    def "test OptimizeAgentResources script analyzes node utilization"() {
        given:
        // Create some build history by triggering test jobs
        ['test-job-1', 'test-job-2', 'test-job-3'].each { jobName ->
            createTestJob(jobName)
            // Trigger a build
            httpClient.post("/job/${jobName}/build", "")
        }
        
        // Wait for builds to register
        Thread.sleep(2000)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/OptimizeAgentResources.groovy')
        
        then:
        result != null
        result.contains('Resource Optimization Report') || result.contains('Agent Resource')
        result.contains('test-aws-node-1') || result.contains('test-azure-node-1') || result.contains('test-k8s-node-1')
    }
    
    def "test cloud node lifecycle management"() {
        given:
        def testNodeName = 'test-cloud-lifecycle-node'
        
        when: "Create a cloud node"
        createTestNode(testNodeName, [
            labels: 'cloud dynamic test',
            description: 'Node for lifecycle testing'
        ])
        
        and: "List nodes to verify creation"
        def listResult = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ListCloudNodes.groovy')
        
        then: "Node appears in the list"
        listResult.contains(testNodeName)
        
        when: "Mark node for deletion"
        def deleteScript = """
import jenkins.model.Jenkins
import hudson.model.Node

def jenkins = Jenkins.instance
def node = jenkins.getNode('${testNodeName}')
if (node) {
    node.setTemporaryOfflineCause(new hudson.slaves.OfflineCause.UserCause(User.current(), "Marked for deletion"))
    println "Node ${testNodeName} marked for deletion"
} else {
    println "Node ${testNodeName} not found"
}
"""
        def deleteResult = httpClient.executeScript(deleteScript)
        
        then: "Deletion is acknowledged"
        deleteResult.status == 200
        deleteResult.content.contains('marked for deletion')
        
        cleanup:
        deleteTestNode(testNodeName)
    }
}