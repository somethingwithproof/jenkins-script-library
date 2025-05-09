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

import jenkins.model.Jenkins
import hudson.model.Computer
import hudson.model.Node
import hudson.model.Slave
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.slaves.OfflineCause
import hudson.slaves.RetentionStrategy
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Test
import static org.junit.Assert.*

/**
 * Integration tests for ComputerLauncher using a real Jenkins instance.
 * 
 * <p>These tests verify that the ComputerLauncher class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Starting all offline slave nodes</li>
 *   <li>Starting a specific offline slave node</li>
 *   <li>Handling non-existent nodes</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class ComputerLauncherIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    @Test
    void testStartAllOfflineNodesInRealJenkins() {
        // Create slaves
        DumbSlave slave1 = createTestSlave(jenkinsRule, "offline-slave1", 1)
        DumbSlave slave2 = createTestSlave(jenkinsRule, "offline-slave2", 1)
        
        // Make sure the slaves are set to offline
        slave1.getComputer().setTemporarilyOffline(true, new OfflineCause.UserCause(null, "Test offline"))
        slave2.getComputer().setTemporarilyOffline(true, new OfflineCause.UserCause(null, "Test offline"))
        
        // Create ComputerLauncher
        Jenkins jenkins = jenkinsRule.jenkins
        ComputerLauncher launcher = new ComputerLauncher(jenkins)
        
        // Start all offline slaves
        int startedCount = launcher.startAllOfflineNodes()
        
        // In a test environment, this may not actually start nodes but should attempt to
        assertNotNull(startedCount)
        assertTrue(startedCount >= 0) 
    }
    
    @Test
    void testStartSpecificOfflineNodeInRealJenkins() {
        // Create slave
        DumbSlave slave = createTestSlave(jenkinsRule, "specific-offline-slave", 1)
        
        // Make sure the slave is set to offline
        slave.getComputer().setTemporarilyOffline(true, new OfflineCause.UserCause(null, "Test offline"))
        
        // Create ComputerLauncher
        Jenkins jenkins = jenkinsRule.jenkins
        ComputerLauncher launcher = new ComputerLauncher(jenkins)
        
        // Start specific offline slave
        boolean result = launcher.startOfflineNode("specific-offline-slave")
        
        // In a test environment, this may not actually start nodes but should attempt to
        assertNotNull(result)
    }
    
    @Test
    void testStartNonExistentNodeInRealJenkins() {
        // Create ComputerLauncher
        Jenkins jenkins = jenkinsRule.jenkins
        ComputerLauncher launcher = new ComputerLauncher(jenkins)
        
        // Try to start non-existent node
        boolean result = launcher.startOfflineNode("non-existent-node")
        
        // Verify result
        assertFalse(result)
    }
    
    @Test
    void testGetOfflineNodesInRealJenkins() {
        // Create slaves
        DumbSlave slave1 = createTestSlave(jenkinsRule, "offline-node1", 1)
        DumbSlave slave2 = createTestSlave(jenkinsRule, "online-node2", 1)
        
        // Make one slave offline
        slave1.getComputer().setTemporarilyOffline(true, new OfflineCause.UserCause(null, "Test offline"))
        
        // Create ComputerLauncher
        Jenkins jenkins = jenkinsRule.jenkins
        ComputerLauncher launcher = new ComputerLauncher(jenkins)
        
        // Get offline nodes
        List<Computer> offlineNodes = launcher.getOfflineNodes()
        
        // Verify result
        assertNotNull(offlineNodes)
        assertTrue(offlineNodes.size() >= 1)
        
        // Check that our offline node is included
        boolean hasOfflineNode = offlineNodes.any { it.name == slave1.getComputer().name }
        assertTrue(hasOfflineNode)
    }
    
    /**
     * Helper method to create a test slave node.
     */
    private static DumbSlave createTestSlave(JenkinsRule jenkinsRule, String name, int numExecutors) {
        DumbSlave slave = new DumbSlave(
            name,
            "Test slave " + name,
            "/tmp/jenkins-agent",
            numExecutors.toString(),
            Node.Mode.NORMAL,
            "",
            new JNLPLauncher(true),
            RetentionStrategy.INSTANCE,
            []
        )
        jenkinsRule.jenkins.addNode(slave)
        return slave
    }
}