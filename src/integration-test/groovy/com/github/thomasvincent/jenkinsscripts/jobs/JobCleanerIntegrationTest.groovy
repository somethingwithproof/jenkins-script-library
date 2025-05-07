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
 * Integration tests for JobCleaner using a real Jenkins instance.
 * 
 * <p>These tests verify that the JobCleaner class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Cleaning builds from an existing job</li>
 *   <li>Resetting build numbers</li>
 *   <li>Handling non-existent jobs</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JobCleanerIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()

    @Test
    void testCleanBuildsInRealJenkins() {
        // Create a job
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("test-project")
        
        // Create some builds
        for (int i = 0; i < 5; i++) {
            project.scheduleBuild2(0).get()
        }
        
        // Verify we have 5 builds
        assertEquals(5, project.getBuilds().size())
        
        // Clean builds
        Jenkins jenkins = jenkinsRule.jenkins
        JobCleaner cleaner = new JobCleaner(jenkins, "test-project", false, 25, 3)
        boolean result = cleaner.clean()
        
        // Verify result
        assertTrue(result)
        
        // Verify we have removed 3 builds
        assertEquals(2, project.getBuilds().size())
    }
    
    @Test
    void testResetBuildNumberInRealJenkins() {
        // Create a job
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("reset-test-project")
        
        // Create some builds
        for (int i = 0; i < 3; i++) {
            project.scheduleBuild2(0).get()
        }
        
        // Verify current next build number
        assertEquals(4, project.getNextBuildNumber())
        
        // Clean builds and reset build number
        Jenkins jenkins = jenkinsRule.jenkins
        JobCleaner cleaner = new JobCleaner(jenkins, "reset-test-project", true, 25, 3)
        boolean result = cleaner.clean()
        
        // Verify result
        assertTrue(result)
        
        // Verify build number has been reset
        assertEquals(1, project.getNextBuildNumber())
    }
    
    @Test
    void testNonExistentJobInRealJenkins() {
        // Clean non-existent job
        Jenkins jenkins = jenkinsRule.jenkins
        JobCleaner cleaner = new JobCleaner(jenkins, "non-existent-job", false, 25, 3)
        boolean result = cleaner.clean()
        
        // Verify result
        assertFalse(result)
    }
}