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
import hudson.model.Node
import hudson.model.Computer
import hudson.plugins.ec2.EC2Cloud
import hudson.plugins.ec2.EC2Computer
import hudson.plugins.ec2.SlaveTemplate
import spock.lang.Specification
import spock.lang.Subject

class AWSNodeManagerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def locationConfig = Mock(JenkinsLocationConfiguration)
    def mockEC2Cloud = Mock(EC2Cloud)
    def mockEC2Computer = Mock(EC2Computer)
    def mockEC2Instance = Mock(Object)
    def mockNode = Mock(Node)
    def mockTemplate = Mock(SlaveTemplate)
    def mockState = Mock(Object)

    @Subject
    def awsManager

    def setup() {
        // Setup JenkinsLocationConfiguration
        JenkinsLocationConfiguration.metaClass.static.get = { return locationConfig }
        locationConfig.getUrl() >> "https://jenkins.example.com/"

        // Setup mockEC2Instance
        mockEC2Instance.getInstanceId() >> "i-1234567890abcdef0"
        mockEC2Instance.getInstanceType() >> "t2.micro"
        mockEC2Instance.getPrivateIpAddress() >> "10.0.0.1"
        mockEC2Instance.getPublicIpAddress() >> "54.123.45.67"
        mockEC2Instance.getImageId() >> "ami-12345"
        mockEC2Instance.getLaunchTime() >> new Date()
        mockEC2Instance.getState() >> mockState
        mockEC2Instance.getTags() >> [
            [key: "Name", value: "jenkins-agent"]
        ]
        
        mockState.getName() >> "running"

        // Setup mockEC2Computer
        mockEC2Computer.describeInstance() >> mockEC2Instance
        mockEC2Computer.getName() >> "ec2-instance-1"
        mockEC2Computer.isOffline() >> false
        mockEC2Computer.getCloud() >> mockEC2Cloud
        
        // Setup mockEC2Cloud
        mockEC2Cloud.getRegion() >> "us-west-2"
        mockEC2Cloud.getTemplates() >> [mockTemplate]
        mockEC2Cloud.getName() >> "aws-cloud"
        
        // Setup mockTemplate
        mockTemplate.getDescription() >> "EC2 Template"
        mockTemplate.getAmi() >> "ami-12345"
        mockTemplate.getType() >> Mock(Object) { getName() >> "t2.micro" }
        mockTemplate.getLabelString() >> "ec2 linux"
        mockTemplate.getNumExecutors() >> 1
        mockTemplate.getRemoteFS() >> "/home/ec2-user"
        mockTemplate.getSecurityGroupString() >> "sg-12345"
        mockTemplate.getSpotConfig() >> null
        
        // Setup mockNode
        mockNode.getNodeName() >> "ec2-instance-1"
        mockNode.getComputer() >> mockEC2Computer

        // Create manager
        awsManager = new AWSNodeManager(jenkins)
        awsManager.ec2Clouds = [mockEC2Cloud]  // Set directly for testing
    }

    def "getEC2Clouds should cache results"() {
        given:
        def mgr = new AWSNodeManager(jenkins)
        jenkins.clouds >> [mockEC2Cloud]
        
        when:
        def result1 = mgr.getEC2Clouds()
        def result2 = mgr.getEC2Clouds()
        
        then:
        result1 == [mockEC2Cloud]
        result2 == [mockEC2Cloud]
        1 * jenkins.clouds // Called only once due to caching
    }
    
    def "getEC2Clouds should handle exceptions"() {
        given:
        def mgr = new AWSNodeManager(jenkins)
        jenkins.clouds >> { throw new Exception("Test exception") }
        
        when:
        def result = mgr.getEC2Clouds()
        
        then:
        result == []
    }
    
    def "isEC2CloudConfigured should return true when clouds exist"() {
        when:
        def result = awsManager.isEC2CloudConfigured()
        
        then:
        result == true
    }
    
    def "isEC2CloudConfigured should return false when no clouds exist"() {
        given:
        awsManager.ec2Clouds = []
        
        when:
        def result = awsManager.isEC2CloudConfigured()
        
        then:
        result == false
    }
    
    def "getEC2Nodes should return nodes with EC2Computer"() {
        given:
        def normalNode = Mock(Node)
        normalNode.getComputer() >> Mock(Computer)
        jenkins.nodes >> [mockNode, normalNode]
        
        when:
        def result = awsManager.getEC2Nodes()
        
        then:
        result == [mockNode]
    }
    
    def "getEC2Nodes should handle exceptions"() {
        given:
        jenkins.nodes >> { throw new Exception("Test exception") }
        
        when:
        def result = awsManager.getEC2Nodes()
        
        then:
        result == []
    }
    
    def "getEC2NodesInfo should return node information"() {
        given:
        awsManager.metaClass.getEC2Nodes = { [mockNode] }
        
        when:
        def result = awsManager.getEC2NodesInfo()
        
        then:
        result.size() == 1
        result[0].name == "ec2-instance-1"
        result[0].ec2.instanceId == "i-1234567890abcdef0"
        result[0].ec2.instanceType == "t2.micro"
        result[0].ec2.region == "us-west-2"
    }
    
    def "getEC2NodesInfo should handle exceptions"() {
        given:
        awsManager.metaClass.getEC2Nodes = { throw new Exception("Test exception") }
        
        when:
        def result = awsManager.getEC2NodesInfo()
        
        then:
        result == []
    }
    
    def "extractEC2NodeInfo should validate input"() {
        when:
        awsManager.extractEC2NodeInfo(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "extractEC2NodeInfo should add EC2-specific information"() {
        when:
        def result = awsManager.extractEC2NodeInfo(mockNode)
        
        then:
        result.ec2.instanceId == "i-1234567890abcdef0"
        result.ec2.instanceType == "t2.micro"
        result.ec2.privateIp == "10.0.0.1"
        result.ec2.publicIp == "54.123.45.67"
        result.ec2.region == "us-west-2"
    }
    
    def "extractEC2NodeInfo should handle exceptions"() {
        given:
        def badNode = Mock(Node)
        badNode.getComputer() >> mockEC2Computer
        mockEC2Computer.describeInstance() >> { throw new Exception("Test exception") }
        
        when:
        def result = awsManager.extractEC2NodeInfo(badNode)
        
        then:
        result.ec2Error == "Failed to retrieve EC2 instance information"
    }
    
    def "getEC2Templates should return templates from all clouds"() {
        given:
        def cloud2 = Mock(EC2Cloud)
        def template2 = Mock(SlaveTemplate)
        cloud2.getTemplates() >> [template2]
        awsManager.ec2Clouds = [mockEC2Cloud, cloud2]
        
        when:
        def result = awsManager.getEC2Templates()
        
        then:
        result == [mockTemplate, template2]
    }
    
    def "getEC2Templates should handle exceptions"() {
        given:
        mockEC2Cloud.getTemplates() >> { throw new Exception("Test exception") }
        
        when:
        def result = awsManager.getEC2Templates()
        
        then:
        result == []
    }
    
    def "getEC2TemplatesInfo should return template information"() {
        when:
        def result = awsManager.getEC2TemplatesInfo()
        
        then:
        result.size() == 1
        result[0].description == "EC2 Template"
        result[0].ami == "ami-12345"
        result[0].instanceType == "t2.micro"
        result[0].labels == "ec2 linux"
    }
    
    def "getEC2TemplatesInfo should handle spot instances"() {
        given:
        def spotConfig = Mock(Object)
        spotConfig.getSpotMaxBidPrice() >> "0.05"
        spotConfig.getFallbackToOndemand() >> true
        mockTemplate.getSpotConfig() >> spotConfig
        
        when:
        def result = awsManager.getEC2TemplatesInfo()
        
        then:
        result[0].spotInstance == true
        result[0].spotDetails.maxBidPrice == "0.05"
        result[0].spotDetails.fallbackToOnDemand == true
    }
    
    def "provisionNewInstance should validate input"() {
        when:
        awsManager.provisionNewInstance(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "provisionNewInstance should return false when no templates match"() {
        when:
        def result = awsManager.provisionNewInstance("Non-existent Template")
        
        then:
        result == false
    }
    
    def "provisionNewInstance should return false when no clouds exist"() {
        given:
        awsManager.ec2Clouds = []
        
        when:
        def result = awsManager.provisionNewInstance("EC2 Template")
        
        then:
        result == false
    }
    
    def "provisionNewInstance should provision with matching template"() {
        when:
        def result = awsManager.provisionNewInstance("EC2 Template")
        
        then:
        result == true
        1 * mockEC2Cloud.provision(mockTemplate, 1)
    }
    
    def "provisionNewInstance should filter by cloud name if provided"() {
        given:
        def cloud2 = Mock(EC2Cloud)
        cloud2.getName() >> "other-cloud"
        awsManager.ec2Clouds = [mockEC2Cloud, cloud2]
        
        when:
        def result = awsManager.provisionNewInstance("EC2 Template", "aws-cloud")
        
        then:
        result == true
        1 * mockEC2Cloud.provision(mockTemplate, 1)
        0 * cloud2.provision(_, _)
    }
    
    def "terminateInstance should validate input"() {
        when:
        awsManager.terminateInstance(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "terminateInstance should return false when instance not found"() {
        given:
        awsManager.metaClass.getEC2NodesInfo = { [] }
        
        when:
        def result = awsManager.terminateInstance("i-nonexistent")
        
        then:
        result == false
    }
    
    def "terminateInstance should terminate matching instance"() {
        given:
        awsManager.metaClass.getEC2NodesInfo = { [
            [name: "ec2-instance-1", ec2: [instanceId: "i-1234567890abcdef0"]]
        ]}
        jenkins.getNode("ec2-instance-1") >> mockNode
        
        when:
        def result = awsManager.terminateInstance("i-1234567890abcdef0")
        
        then:
        result == true
        1 * mockEC2Computer.disconnect(_)
        1 * mockEC2Cloud.doTerminate(mockEC2Computer)
    }
    
    def "formatNodeInfo should handle EC2-specific information"() {
        given:
        def nodeInfo = [
            name: "ec2-instance-1",
            numExecutors: 2,
            labels: "ec2 linux",
            offline: false,
            ec2: [
                instanceId: "i-1234567890abcdef0",
                instanceType: "t2.micro",
                region: "us-west-2",
                state: "running",
                privateIp: "10.0.0.1",
                publicIp: "54.123.45.67",
                launchTime: "2023-05-04 12:34",
                tags: [Name: "jenkins-agent"]
            ]
        ]
        
        when:
        def result = awsManager.formatNodeInfo(nodeInfo)
        
        then:
        result.contains("EC2 Node: ec2-instance-1")
        result.contains("Status: ONLINE")
        result.contains("Executors: 2")
        result.contains("Labels: ec2 linux")
        result.contains("EC2 Details:")
        result.contains("Instance ID: i-1234567890abcdef0")
        result.contains("Type: t2.micro")
        result.contains("Region: us-west-2")
        result.contains("State: running")
        result.contains("Private IP: 10.0.0.1")
        result.contains("Public IP: 54.123.45.67")
        result.contains("Launch Time: 2023-05-04 12:34")
        result.contains("Tags:")
        result.contains("Name: jenkins-agent")
    }
}