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

import org.junit.Test
import org.junit.Ignore
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.WithoutJenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.Result

/**
 * Integration tests for AnalyzePipelinePerformance script.
 */
class AnalyzePipelinePerformanceIntegrationTest extends BaseScriptIntegrationTest {

    @Override
    String getScriptPath() {
        return "src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/AnalyzePipelinePerformance.groovy"
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
        assertOutputContains("groovy AnalyzePipelinePerformance")
    }
    
    /**
     * Test handling of non-existent job.
     */
    @Test
    void testNonExistentJob() {
        runScript("--job", "non-existent-job")
        
        assertOutputContains("Warning: Job 'non-existent-job' not found")
    }
    
    /**
     * Test analyzing a pipeline job.
     */
    @Test
    void testAnalyzePipelineJob() {
        // Create a test pipeline job
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline")
        job.setDefinition(new CpsFlowDefinition('''
            pipeline {
                agent any
                stages {
                    stage('Checkout') {
                        steps {
                            echo 'Checking out code...'
                            sleep 1
                        }
                    }
                    stage('Build') {
                        steps {
                            echo 'Building...'
                            sleep 2
                        }
                    }
                    stage('Test') {
                        steps {
                            echo 'Testing...'
                            sleep 3
                        }
                    }
                }
            }
        ''', true))
        
        // Run the pipeline job
        jenkinsRule.buildAndAssertSuccess(job)
        
        // Analyze the job
        runScript("--job", "test-pipeline")
        
        // Verify output contains pipeline stages
        assertOutputContains("test-pipeline")
        assertOutputContains("Checkout")
        assertOutputContains("Build")
        assertOutputContains("Test")
    }
    
    /**
     * Test JSON output format.
     */
    @Test
    void testJsonOutput() {
        // Create a test pipeline job
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline")
        job.setDefinition(new CpsFlowDefinition('''
            pipeline {
                agent any
                stages {
                    stage('Stage1') {
                        steps {
                            echo 'Running stage 1'
                            sleep 1
                        }
                    }
                }
            }
        ''', true))
        
        // Run the pipeline job
        jenkinsRule.buildAndAssertSuccess(job)
        
        // Analyze the job with JSON output
        runScript("--job", "test-pipeline", "--json")
        
        // Verify JSON output format
        assertOutputContains("{")
        assertOutputContains("\"name\"")
        assertOutputContains("test-pipeline")
        assertOutputContains("\"stages\"")
    }
    
    /**
     * Test threshold filtering.
     */
    @Test
    void testThresholdFiltering() {
        // Create a test pipeline job with stages of different durations
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline")
        job.setDefinition(new CpsFlowDefinition('''
            pipeline {
                agent any
                stages {
                    stage('Fast') {
                        steps {
                            echo 'Fast stage'
                            sleep 1
                        }
                    }
                    stage('Slow') {
                        steps {
                            echo 'Slow stage'
                            sleep 5
                        }
                    }
                }
            }
        ''', true))
        
        // Run the pipeline job
        jenkinsRule.buildAndAssertSuccess(job)
        
        // Analyze with threshold that should only show the slow stage
        runScript("--job", "test-pipeline", "--threshold", "3")
        
        // Verify only the slow stage is shown
        assertOutputContains("Slow")
        assert !getOutput().contains("Fast")
    }
    
    /**
     * Test pattern-based job selection.
     */
    @Test
    void testPatternBasedJobSelection() {
        // Create multiple test pipeline jobs
        WorkflowJob job1 = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline-1")
        job1.setDefinition(new CpsFlowDefinition("node { echo 'Job 1'; sleep 1 }", true))
        
        WorkflowJob job2 = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline-2")
        job2.setDefinition(new CpsFlowDefinition("node { echo 'Job 2'; sleep 1 }", true))
        
        WorkflowJob job3 = jenkinsRule.jenkins.createProject(WorkflowJob.class, "other-pipeline")
        job3.setDefinition(new CpsFlowDefinition("node { echo 'Job 3'; sleep 1 }", true))
        
        // Run all jobs
        jenkinsRule.buildAndAssertSuccess(job1)
        jenkinsRule.buildAndAssertSuccess(job2)
        jenkinsRule.buildAndAssertSuccess(job3)
        
        // Analyze with pattern to match only test-pipeline-* jobs
        runScript("--pattern", "test-pipeline-*")
        
        // Verify matching jobs are analyzed
        assertOutputContains("test-pipeline-1")
        assertOutputContains("test-pipeline-2")
        assert !getOutput().contains("other-pipeline")
    }
    
    /**
     * Test trend analysis.
     */
    @Test
    void testTrendAnalysis() {
        // Create a test pipeline job
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "test-pipeline")
        job.setDefinition(new CpsFlowDefinition('''
            pipeline {
                agent any
                stages {
                    stage('Test') {
                        steps {
                            echo 'Testing...'
                            sleep 2
                        }
                    }
                }
            }
        ''', true))
        
        // Run the job multiple times with increasing sleep durations to simulate performance regression
        jenkinsRule.buildAndAssertSuccess(job)
        
        // Change pipeline definition to be slower
        job.setDefinition(new CpsFlowDefinition('''
            pipeline {
                agent any
                stages {
                    stage('Test') {
                        steps {
                            echo 'Testing...'
                            sleep 3
                        }
                    }
                }
            }
        ''', true))
        
        jenkinsRule.buildAndAssertSuccess(job)
        
        // Analyze with trends
        runScript("--job", "test-pipeline", "--trends")
        
        // Verify trend information is included in output
        assertOutputContains("TRENDS")
        assertOutputContains("Test")
    }
}