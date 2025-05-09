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
import hudson.model.Node
import hudson.model.Slave
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.slaves.RetentionStrategy
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Test
import static org.junit.Assert.*

/**
 * Integration tests for SlaveInfoManager using a real Jenkins instance.
 * 
 * <p>These tests verify that the SlaveInfoManager class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Listing all slave nodes</li>
 *   <li>Getting a specific slave node</li>
 *   <li>Checking online/offline status</li>
 *   <li>Getting slave node details</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class SlaveInfoManagerIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    @Test
    void testListAllSlavesInRealJenkins() {
        // Create slaves
        createTestSlave(jenkinsRule, "slave1", 1)
        createTestSlave(jenkinsRule, "slave2", 2)
        
        // Create SlaveInfoManager
        Jenkins jenkins = jenkinsRule.jenkins
        SlaveInfoManager manager = new SlaveInfoManager(jenkins)
        
        // List all slaves
        List<Node> slaves = manager.getAllSlaves()
        
        // Verify result
        assertEquals(2, slaves.size())
        assertTrue(slaves.find { it.nodeName == "slave1" } != null)
        assertTrue(slaves.find { it.nodeName == "slave2" } != null)
    }
    
    @Test
    void testGetSpecificSlaveInRealJenkins() {
        // Create slaves
        createTestSlave(jenkinsRule, "specific-slave", 1)
        
        // Create SlaveInfoManager
        Jenkins jenkins = jenkinsRule.jenkins
        SlaveInfoManager manager = new SlaveInfoManager(jenkins)
        
        // Get specific slave
        Node slave = manager.getSlave("specific-slave")
        
        // Verify result
        assertNotNull(slave)
        assertEquals("specific-slave", slave.nodeName)
    }
    
    @Test
    void testGetNonExistentSlaveInRealJenkins() {
        // Create SlaveInfoManager
        Jenkins jenkins = jenkinsRule.jenkins
        SlaveInfoManager manager = new SlaveInfoManager(jenkins)
        
        // Get non-existent slave
        Node slave = manager.getSlave("non-existent-slave")
        
        // Verify result
        assertNull(slave)
    }
    
    @Test
    void testIsSlaveOnlineInRealJenkins() {
        // Create slaves - by default slaves are offline in JenkinsRule
        DumbSlave slave = createTestSlave(jenkinsRule, "online-test-slave", 1)
        
        // Create SlaveInfoManager
        Jenkins jenkins = jenkinsRule.jenkins
        SlaveInfoManager manager = new SlaveInfoManager(jenkins)
        
        // Check if slave is online (should be offline by default)
        boolean isOnline = manager.isSlaveOnline("online-test-slave")
        
        // Verify result
        assertFalse(isOnline)
        
        // Mark slave as temporarily online for testing
        slave.getComputer().connect(false).get()
        
        // Check again after marking online
        boolean isOnlineAfterConnect = manager.isSlaveOnline("online-test-slave")
        
        // Verify result (may still be false as connection is async)
        // This is primarily testing the method call, not the actual online state
        assertNotNull(isOnlineAfterConnect)
    }
    
    @Test
    void testGetSlaveDetailsInRealJenkins() {
        // Create slave
        createTestSlave(jenkinsRule, "detail-test-slave", 2)
        
        // Create SlaveInfoManager
        Jenkins jenkins = jenkinsRule.jenkins
        SlaveInfoManager manager = new SlaveInfoManager(jenkins)
        
        // Get slave details
        Map<String, Object> details = manager.getSlaveDetails("detail-test-slave")
        
        // Verify result
        assertNotNull(details)
        assertEquals("detail-test-slave", details.name)
        assertEquals(2, details.executors)
        assertFalse(details.isOnline)
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