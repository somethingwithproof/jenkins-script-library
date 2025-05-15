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

package com.github.thomasvincent.jenkinsscripts.scripts

import hudson.model.labels.LabelAtom
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.model.Node.Mode
import org.junit.Test
import org.junit.Ignore
import org.jvnet.hudson.test.WithoutJenkins

/**
 * Integration tests for ListCloudNodes script.
 */
class ListCloudNodesIntegrationTest extends BaseScriptIntegrationTest {

    @Override
    String getScriptPath() {
        return "src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ListCloudNodes.groovy"
    }
    
    /**
     * Test to verify script has valid syntax.
     */
    @Test
    @WithoutJenkins
    void testScriptSyntax() {
        ScriptSyntaxTest syntaxTest = new ScriptSyntaxTest()
        assert syntaxTest.checkScriptSyntax(getScriptPath())
    }
    
    /**
     * Test script with help option.
     */
    @Test
    void testHelpOption() {
        runScript("--help")
        
        assertOutputContains("Options:")
        assertOutputContains("groovy ListCloudNodes")
    }
    
    /**
     * Test script when no cloud nodes exist.
     */
    @Test
    void testNoCloudNodesExist() {
        runScript()
        
        assertOutputContains("No cloud nodes found in this Jenkins instance")
    }
    
    /**
     * Test script with mock nodes.
     */
    @Test
    void testWithMockNodes() {
        // Create mock slave node that appears cloud-like
        DumbSlave slave = new DumbSlave("cloud-node-12345", "/home/jenkins",
            new JNLPLauncher(true))
        slave.setNumExecutors(1)
        slave.setMode(Mode.NORMAL)
        slave.setLabelString("cloud aws")
        slave.setNodeDescription("Cloud node for testing")
        
        jenkinsRule.jenkins.addNode(slave)
        
        runScript()
        
        assertOutputContains("cloud-node-12345")
    }
    
    /**
     * Test JSON output format.
     */
    @Test
    void testJsonOutput() {
        // Create mock slave node that appears cloud-like
        DumbSlave slave = new DumbSlave("cloud-node-12345", "/home/jenkins",
            new JNLPLauncher(true))
        slave.setNumExecutors(1)
        slave.setMode(Mode.NORMAL)
        slave.setLabelString("cloud aws")
        slave.setNodeDescription("Cloud node for testing")
        
        jenkinsRule.jenkins.addNode(slave)
        
        runScript("--json")
        
        assertOutputContains("{")
        assertOutputContains("\"name\"")
        assertOutputContains("cloud-node-12345")
    }
    
    /**
     * Test with specific provider filter.
     */
    @Test
    void testWithProviderFilter() {
        // Create mock slave nodes for different providers
        DumbSlave awsSlave = new DumbSlave("aws-node-12345", "/home/jenkins",
            new JNLPLauncher(true))
        awsSlave.setLabelString("aws")
        
        DumbSlave kubeSlave = new DumbSlave("kubernetes-pod-12345", "/home/jenkins",
            new JNLPLauncher(true))
        kubeSlave.setLabelString("kubernetes")
        
        jenkinsRule.jenkins.addNode(awsSlave)
        jenkinsRule.jenkins.addNode(kubeSlave)
        
        // Test AWS filter
        runScript("--aws")
        
        assertOutputContains("aws-node-12345")
        assert !getOutput().contains("kubernetes-pod-12345")
        
        // Reset output capture
        resetStreams()
        
        // Test Kubernetes filter
        runScript("--kubernetes")
        
        assertOutputContains("kubernetes-pod-12345")
        assert !getOutput().contains("aws-node-12345")
    }
    
    /**
     * Test cloud statistics output.
     */
    @Test
    void testCloudStats() {
        // Create mock slave node that appears cloud-like
        DumbSlave slave = new DumbSlave("cloud-node-12345", "/home/jenkins",
            new JNLPLauncher(true))
        slave.setNumExecutors(1)
        slave.setMode(Mode.NORMAL)
        slave.setLabelString("cloud aws")
        slave.setNodeDescription("Cloud node for testing")
        
        jenkinsRule.jenkins.addNode(slave)
        
        runScript("--stats")
        
        assertOutputContains("Cloud Node Statistics")
        assertOutputContains("Total")
    }
}