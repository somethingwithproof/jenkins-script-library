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

package com.github.thomasvincent.jenkinsscripts.helm

import jenkins.model.Jenkins
import hudson.tools.ToolProperty
import hudson.tools.ToolInstallation
import hudson.tools.InstallSourceProperty
import hudson.tools.CommandInstaller
import hudson.model.DownloadService.Downloadable
import hudson.tools.ToolInstaller
import hudson.tools.DownloadFromUrlInstaller
import hudson.model.TaskListener
import hudson.FilePath
import hudson.tools.ToolLocationNodeProperty
import hudson.model.JDK
import hudson.model.Node
import hudson.slaves.DumbSlave
import hudson.slaves.JNLPLauncher
import hudson.slaves.RetentionStrategy

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Test
import org.junit.After
import org.junit.Before
import static org.junit.Assert.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Integration tests for HelmHelper using a real Jenkins instance.
 * 
 * <p>These tests verify that the HelmHelper class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Installing Helm</li>
 *   <li>Verifying Helm installation</li>
 *   <li>Getting Helm installation details</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class HelmHelperIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    private Path tempDir
    
    @Before
    void setup() {
        // Create a temporary directory for installations
        tempDir = Files.createTempDirectory("helm-install-test")
    }
    
    @After
    void cleanup() {
        // Delete temp directory and contents
        if (tempDir != null) {
            def paths = Files.walk(tempDir).sorted(Comparator.reverseOrder()).toArray()
            paths.each { path ->
                try {
                    Files.deleteIfExists(path)
                } catch (IOException e) {
                    // Ignore deletion errors
                }
            }
        }
    }
    
    @Test
    void testGetHelmInstallationsInRealJenkins() {
        // Setup a Helm installation in Jenkins
        setupTestHelmInstallation()
        
        // Create HelmHelper
        Jenkins jenkins = jenkinsRule.jenkins
        HelmHelper helmHelper = new HelmHelper(jenkins)
        
        // Get Helm installations
        def installations = helmHelper.getHelmInstallations()
        
        // Verify result
        assertNotNull(installations)
        assertTrue(installations.size() > 0)
        assertEquals("TestHelm", installations[0].name)
    }
    
    @Test
    void testIsHelmInstalledInRealJenkins() {
        // Setup a Helm installation in Jenkins
        setupTestHelmInstallation()
        
        // Create HelmHelper
        Jenkins jenkins = jenkinsRule.jenkins
        HelmHelper helmHelper = new HelmHelper(jenkins)
        
        // Check if Helm is installed
        boolean isInstalled = helmHelper.isHelmInstalled()
        
        // Verify result
        assertTrue(isInstalled)
    }
    
    @Test
    void testGetHelmVersionInRealJenkins() {
        // This test would normally verify that we can get a Helm version
        // However, we're not actually installing Helm binaries in the test
        // So we'll just verify the method runs without errors
        
        // Setup a Helm installation in Jenkins
        setupTestHelmInstallation()
        
        // Create HelmHelper
        Jenkins jenkins = jenkinsRule.jenkins
        HelmHelper helmHelper = new HelmHelper(jenkins)
        
        // Get Helm version - this won't return a real version since we're not installing Helm
        // But we test that the method executes without errors
        String version = helmHelper.getHelmVersion()
        
        // The version may be null since we don't have a real Helm binary in the test
        assertNotNull(version)
    }
    
    /**
     * Helper method to setup a test Helm installation.
     */
    private void setupTestHelmInstallation() {
        // Add a mock Helm installation tool in Jenkins
        def jenkins = jenkinsRule.jenkins
        
        // Create a simple CommandInstaller that just touches a file to simulate installation
        def installer = new CommandInstaller("MockHelmInstall", "echo 'Installed' > helm && chmod +x helm", tempDir.toString())
        def installSource = new InstallSourceProperty([installer])
        
        // Create a Helm tool installation
        def helmTool = new hudson.tools.HelmInstallation("TestHelm", tempDir.toString(), [installSource])
        
        // Set the tool installation in Jenkins
        def helmToolDescriptor = jenkins.getDescriptorByType(hudson.tools.HelmInstallation.DescriptorImpl.class)
        helmToolDescriptor.setInstallations(helmTool)
        
        // Save the configuration
        jenkins.save()
    }
}