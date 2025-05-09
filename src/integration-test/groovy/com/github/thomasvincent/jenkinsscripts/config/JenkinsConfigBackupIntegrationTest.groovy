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

package com.github.thomasvincent.jenkinsscripts.config

import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
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
 * Integration tests for JenkinsConfigBackup using a real Jenkins instance.
 * 
 * <p>These tests verify that the JenkinsConfigBackup class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Backing up Jenkins configuration</li>
 *   <li>Backing up job configuration</li>
 *   <li>Backing up plugin configuration</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JenkinsConfigBackupIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    private Path tempDir
    
    @Before
    void setup() {
        // Create a temporary directory for backups
        tempDir = Files.createTempDirectory("jenkins-backup-test")
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
    void testBackupJenkinsConfigInRealJenkins() {
        // Create a job to include in the backup
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("config-backup-test-job")
        
        // Create JenkinsConfigBackup
        Jenkins jenkins = jenkinsRule.jenkins
        JenkinsConfigBackup configBackup = new JenkinsConfigBackup(jenkins)
        
        // Execute backup
        String backupPath = tempDir.toString()
        boolean result = configBackup.backupJenkinsConfig(backupPath)
        
        // Verify result
        assertTrue(result)
        
        // Verify backup files were created
        Path backupFilePath = Paths.get(backupPath, "jenkins-backup.tar.gz")
        assertTrue(Files.exists(backupFilePath))
        assertTrue(Files.size(backupFilePath) > 0)
    }
    
    @Test
    void testBackupJobConfigInRealJenkins() {
        // Create a job to backup
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("job-config-backup-test")
        
        // Create JenkinsConfigBackup
        Jenkins jenkins = jenkinsRule.jenkins
        JenkinsConfigBackup configBackup = new JenkinsConfigBackup(jenkins)
        
        // Execute job config backup
        String backupPath = tempDir.toString()
        boolean result = configBackup.backupJobConfigs(backupPath)
        
        // Verify result
        assertTrue(result)
        
        // Verify backup files were created
        Path jobsBackupDir = Paths.get(backupPath, "jobs")
        assertTrue(Files.exists(jobsBackupDir))
        
        // Check for job config file
        Path jobConfigPath = Paths.get(jobsBackupDir.toString(), "job-config-backup-test", "config.xml")
        assertTrue(Files.exists(jobConfigPath))
        assertTrue(Files.size(jobConfigPath) > 0)
    }
    
    @Test
    void testBackupPluginConfigInRealJenkins() {
        // Create JenkinsConfigBackup
        Jenkins jenkins = jenkinsRule.jenkins
        JenkinsConfigBackup configBackup = new JenkinsConfigBackup(jenkins)
        
        // Execute plugin config backup
        String backupPath = tempDir.toString()
        boolean result = configBackup.backupPluginConfigs(backupPath)
        
        // Verify result
        assertTrue(result)
        
        // Verify backup directory was created
        Path pluginsBackupDir = Paths.get(backupPath, "plugins")
        assertTrue(Files.exists(pluginsBackupDir))
    }
}