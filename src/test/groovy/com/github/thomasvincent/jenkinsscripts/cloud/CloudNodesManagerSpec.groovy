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

import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import hudson.model.Computer
import hudson.model.Node
import spock.lang.Specification
import spock.lang.Subject
import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

class CloudNodesManagerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def locationConfig = Mock(JenkinsLocationConfiguration)
    def mockNode = Mock(Node)
    def mockComputer = Mock(Computer)
    def mockCloud = Mock(Object)

    @Subject
    def cloudManager

    def setup() {
        // Setup JenkinsLocationConfiguration
        JenkinsLocationConfiguration.metaClass.static.get = { return locationConfig }
        locationConfig.getUrl() >> "https://jenkins.example.com/"

        // Setup mock node and computer
        mockNode.getNodeName() >> "cloud-node-1"
        mockNode.getDisplayName() >> "Cloud Node 1"
        mockNode.getNodeDescription() >> "Test cloud node"
        mockNode.getNumExecutors() >> 2
        mockNode.getLabelString() >> "cloud linux"
        mockNode.getRemoteFS() >> "/home/jenkins"
        mockNode.getComputer() >> mockComputer

        mockComputer.isOffline() >> false
        mockComputer.isTemporarilyOffline() >> false
        mockComputer.getConnectTime() >> System.currentTimeMillis()
        mockComputer.countBusy() >> 1

        // Create the manager
        cloudManager = new CloudNodesManager(jenkins)
    }

    def "constructor should use provided Jenkins instance"() {
        expect:
        cloudManager.jenkins == jenkins
    }

    def "constructor should default to Jenkins.get() if null is provided"() {
        given:
        Jenkins.metaClass.static.get = { return jenkins }

        when:
        def manager = new CloudNodesManager(null)

        then:
        manager.jenkins == jenkins
    }

    def "getConfiguredClouds should return empty list on error"() {
        given:
        jenkins.clouds >> { throw new Exception("Test exception") }

        when:
        def result = cloudManager.getConfiguredClouds()

        then:
        result == []
    }

    def "getConfiguredClouds should return clouds when available"() {
        given:
        jenkins.clouds >> [mockCloud]

        when:
        def result = cloudManager.getConfiguredClouds()

        then:
        result == [mockCloud]
    }

    def "isCloudConfigured should handle exceptions"() {
        given:
        jenkins.clouds >> { throw new Exception("Test exception") }

        when:
        def result = cloudManager.isCloudConfigured(Object)

        then:
        result == false
    }

    def "isCloudConfigured should return true when cloud type is configured"() {
        given:
        jenkins.clouds >> [mockCloud]
        mockCloud.getClass() >> Object

        when:
        def result = cloudManager.isCloudConfigured(Object)

        then:
        result == true
    }

    def "isCloudConfigured should return false when cloud type is not configured"() {
        given:
        jenkins.clouds >> [mockCloud]
        mockCloud.getClass() >> String

        when:
        def result = cloudManager.isCloudConfigured(Integer)

        then:
        result == false
    }

    def "getCloudNodes should handle exceptions"() {
        given:
        jenkins.nodes >> { throw new Exception("Test exception") }

        when:
        def result = cloudManager.getCloudNodes(Object)

        then:
        result == []
    }

    def "getCloudNodes should return matching nodes"() {
        given:
        def cloudNode1 = Mock(Node)
        cloudNode1.getName() >> "cloud-test-abc123"
        
        def cloudNode2 = Mock(Node)
        cloudNode2.getName() >> "regular-node"
        cloudNode2.getLabelString() >> "cloud linux"
        
        jenkins.nodes >> [cloudNode1, cloudNode2]

        when:
        def result = cloudManager.getCloudNodes(Object)

        then:
        result.size() == 2
        result.contains(cloudNode1)
        result.contains(cloudNode2)
    }

    def "getAllCloudNodes should handle exceptions"() {
        given:
        jenkins.clouds >> { throw new Exception("Test exception") }

        when:
        def result = cloudManager.getAllCloudNodes()

        then:
        result == [:]
    }

    def "getAllCloudNodes should return nodes by cloud type"() {
        given:
        def mockCloud1 = Mock(Object)
        mockCloud1.getClass() >> String
        
        jenkins.clouds >> [mockCloud1]
        
        cloudManager.metaClass.getCloudNodes = { Class cloudClass ->
            return cloudClass == String ? [mockNode] : []
        }

        when:
        def result = cloudManager.getAllCloudNodes()

        then:
        result.size() == 1
        result["String"] == [mockNode]
    }

    def "getCloudNodeStats should handle exceptions"() {
        given:
        cloudManager.metaClass.getAllCloudNodes = { throw new Exception("Test exception") }

        when:
        def result = cloudManager.getCloudNodeStats()

        then:
        result == [:]
    }

    def "getCloudNodeStats should return correct statistics"() {
        given:
        def onlineNode = Mock(Node)
        onlineNode.getComputer() >> mockComputer
        
        def offlineNode = Mock(Node)
        def offlineComputer = Mock(Computer)
        offlineComputer.isOffline() >> true
        offlineNode.getComputer() >> offlineComputer
        
        cloudManager.metaClass.getAllCloudNodes = {
            return ["TestCloud": [onlineNode, offlineNode]]
        }

        when:
        def result = cloudManager.getCloudNodeStats()

        then:
        result.size() == 1
        result["TestCloud"].total == 2
        result["TestCloud"].online == 1
        result["TestCloud"].offline == 1
    }

    def "extractNodeInfo should validate input"() {
        when:
        cloudManager.extractNodeInfo(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "extractNodeInfo should handle exceptions"() {
        given:
        def mockNodeBad = Mock(Node)
        mockNodeBad.getNodeName() >> { throw new Exception("Test exception") }

        when:
        def result = cloudManager.extractNodeInfo(mockNodeBad)

        then:
        result == [:]
    }

    def "extractNodeInfo should return correct node information"() {
        when:
        def result = cloudManager.extractNodeInfo(mockNode)

        then:
        result.name == "cloud-node-1"
        result.displayName == "Cloud Node 1"
        result.description == "Test cloud node"
        result.numExecutors == 2
        result.labels == "cloud linux"
        result.remoteFS == "/home/jenkins"
        result.offline == false
        result.temporarilyOffline == false
    }

    def "formatNodeInfo should handle null input"() {
        when:
        def result = cloudManager.formatNodeInfo(null)

        then:
        result == "No information available"
    }

    def "formatNodeInfo should format node information correctly"() {
        given:
        def nodeInfo = [
            name: "test-node",
            displayName: "Test Node",
            description: "Test description",
            numExecutors: 2,
            labels: "test linux",
            offline: false,
            nestedInfo: [
                key1: "value1",
                key2: "value2"
            ]
        ]

        when:
        def result = cloudManager.formatNodeInfo(nodeInfo)

        then:
        result.contains("Node: test-node")
        result.contains("displayName: Test Node")
        result.contains("description: Test description")
        result.contains("numExecutors: 2")
        result.contains("labels: test linux")
        result.contains("offline: false")
        result.contains("nestedInfo:")
        result.contains("key1: value1")
        result.contains("key2: value2")
    }
}