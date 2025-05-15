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

package com.github.thomasvincent.jenkinsscripts.nodes

import hudson.model.Computer
import hudson.model.Slave
import hudson.plugins.ec2.EC2Computer
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Subject

/**
 * Unit tests for SlaveInfoManager class.
 * 
 * <p>This specification verifies the behavior of the SlaveInfoManager class, which is
 * responsible for collecting and formatting information about Jenkins slave nodes.</p>
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class SlaveInfoManagerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def node1 = Mock(Slave)
    def node2 = Mock(Slave)
    def computer1 = Mock(Computer)
    def computer2 = Mock(EC2Computer)
    
    @Subject
    SlaveInfoManager manager
    
    def setup() {
        // Set up node mocks
        node1.nodeName >> "node1"
        node1.displayName >> "Node 1"
        node1.nodeDescription >> "Test node 1"
        node1.remoteFS >> "/home/jenkins"
        node1.numExecutors >> 2
        node1.mode >> hudson.model.Node.Mode.NORMAL
        node1.computer >> computer1
        
        node2.nodeName >> "node2"
        node2.displayName >> "Node 2"
        node2.nodeDescription >> "EC2 node"
        node2.remoteFS >> "/home/ec2-user"
        node2.numExecutors >> 1
        node2.mode >> hudson.model.Node.Mode.EXCLUSIVE
        node2.computer >> computer2
        
        // Set up computer mocks
        computer1.offline >> false
        computer1.temporarilyOffline >> false
        computer1.hostName >> "node1.example.com"
        computer1.connectTime >> System.currentTimeMillis()
        
        computer2.offline >> true
        computer2.temporarilyOffline >> true
        computer2.name >> "node2"
        
        // Set up Jenkins mock
        jenkins.getNode("node1") >> node1
        jenkins.getNode("node2") >> node2
        jenkins.getNode("non-existent") >> null
        jenkins.nodes >> [node1, node2]
        
        // Create manager
        manager = new SlaveInfoManager(jenkins)
    }
    
    def "should list all slaves"() {
        when:
        def slaves = manager.listAllSlaves()
        
        then:
        slaves.size() == 2
        slaves[0].name == "node1"
        slaves[1].name == "node2"
    }
    
    def "should get info for a specific slave"() {
        when:
        def info = manager.getSlaveInfo("node1")
        
        then:
        info != null
        info.name == "node1"
        info.displayName == "Node 1"
        info.numExecutors == 2
        info.offline == false
    }
    
    def "should return null for non-existent slave"() {
        when:
        def info = manager.getSlaveInfo("non-existent")
        
        then:
        info == null
    }
    
    def "should return null for null slave name"() {
        when:
        def info = manager.getSlaveInfo(null)
        
        then:
        info == null
    }
    
    def "should return null for empty slave name"() {
        when:
        def info = manager.getSlaveInfo("")
        
        then:
        info == null
    }
    
    def "should return null for blank slave name"() {
        when:
        def info = manager.getSlaveInfo("   ")
        
        then:
        info == null
    }
    
    def "should format slave info"() {
        given:
        def info = [
            name: "test-node",
            displayName: "Test Node",
            offline: true,
            numExecutors: 2
        ]
        
        when:
        def formatted = manager.formatSlaveInfo(info)
        
        then:
        formatted.contains("Node: test-node")
        formatted.contains("displayName: Test Node")
        formatted.contains("offline: true")
        formatted.contains("numExecutors: 2")
    }
    
    def "should handle null when formatting slave info"() {
        when:
        def formatted = manager.formatSlaveInfo(null)
        
        then:
        formatted == "No information available"
    }
    
    def "should format EC2 info when available"() {
        given:
        def info = [
            name: "ec2-node",
            displayName: "EC2 Node",
            offline: true,
            numExecutors: 2,
            ec2: [
                instanceId: "i-1234567890abcdef0",
                instanceType: "t2.micro",
                privateIp: "10.0.0.1",
                state: "running"
            ]
        ]
        
        when:
        def formatted = manager.formatSlaveInfo(info)
        
        then:
        formatted.contains("Node: ec2-node")
        formatted.contains("EC2 Details:")
        formatted.contains("instanceId: i-1234567890abcdef0")
        formatted.contains("instanceType: t2.micro")
        formatted.contains("privateIp: 10.0.0.1")
        formatted.contains("state: running")
    }
}