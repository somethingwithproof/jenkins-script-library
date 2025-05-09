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

package com.github.thomasvincent.jenkinsscripts.jobs

import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Test
import static org.junit.Assert.*

/**
 * Integration tests for JobDisabler using a real Jenkins instance.
 * 
 * <p>These tests verify that the JobDisabler class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Disabling a specific job</li>
 *   <li>Disabling all jobs</li>
 *   <li>Handling non-existent jobs</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JobDisablerIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    @Test
    void testDisableSpecificJobInRealJenkins() {
        // Create a job
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("test-disable-job")
        
        // Ensure job is initially enabled
        project.enable()
        assertTrue(project.isBuildable())
        
        // Create JobDisabler
        Jenkins jenkins = jenkinsRule.jenkins
        JobDisabler disabler = new JobDisabler(jenkins)
        
        // Disable job
        boolean result = disabler.disableJob("test-disable-job")
        
        // Verify result
        assertTrue(result)
        
        // Verify job is now disabled
        project = jenkins.getItem("test-disable-job")
        assertFalse(project.isBuildable())
    }
    
    @Test
    void testDisableAllJobsInRealJenkins() {
        // Create multiple jobs
        FreeStyleProject project1 = jenkinsRule.createFreeStyleProject("multi-test-job1")
        FreeStyleProject project2 = jenkinsRule.createFreeStyleProject("multi-test-job2")
        
        // Ensure jobs are initially enabled
        project1.enable()
        project2.enable()
        assertTrue(project1.isBuildable())
        assertTrue(project2.isBuildable())
        
        // Create JobDisabler
        Jenkins jenkins = jenkinsRule.jenkins
        JobDisabler disabler = new JobDisabler(jenkins)
        
        // Disable all jobs
        int disabledCount = disabler.disableAllJobs()
        
        // Verify result
        assertTrue(disabledCount >= 2)
        
        // Verify jobs are now disabled
        project1 = jenkins.getItem("multi-test-job1")
        project2 = jenkins.getItem("multi-test-job2")
        assertFalse(project1.isBuildable())
        assertFalse(project2.isBuildable())
    }
    
    @Test
    void testDisableNonExistentJobInRealJenkins() {
        // Create JobDisabler
        Jenkins jenkins = jenkinsRule.jenkins
        JobDisabler disabler = new JobDisabler(jenkins)
        
        // Try to disable non-existent job
        boolean result = disabler.disableJob("non-existent-job")
        
        // Verify result
        assertFalse(result)
    }
    
    @Test
    void testDisableAlreadyDisabledJobInRealJenkins() {
        // Create a job
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("already-disabled-job")
        
        // Disable the job initially
        project.disable()
        assertFalse(project.isBuildable())
        
        // Create JobDisabler
        Jenkins jenkins = jenkinsRule.jenkins
        JobDisabler disabler = new JobDisabler(jenkins)
        
        // Disable job again
        boolean result = disabler.disableJob("already-disabled-job")
        
        // Verify result
        assertTrue(result)
        
        // Verify job is still disabled
        project = jenkins.getItem("already-disabled-job")
        assertFalse(project.isBuildable())
    }
}