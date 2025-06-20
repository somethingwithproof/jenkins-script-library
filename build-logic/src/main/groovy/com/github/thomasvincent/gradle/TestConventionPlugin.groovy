package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Convention plugin for test configuration.
 * Sets up standard test execution settings, including retry for flaky tests.
 */
class TestConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('org.gradle.test-retry')
        
        project.tasks.withType(Test).configureEach { task ->
            // Enable JUnit Platform for all test tasks
            task.useJUnit()
            
            // Configure test logging
            task.testLogging {
                events "passed", "skipped", "failed", "standardOut", "standardError"
                exceptionFormat = 'full'
                showExceptions = true
                showCauses = true
                showStackTraces = true
            }
            
            // Configure test retry for flaky tests
            task.retry {
                maxRetries = 3
                maxFailures = 10
                failOnPassedAfterRetry = false
            }
            
            // Configure parallel test execution
            task.maxParallelForks = Math.max(Runtime.runtime.availableProcessors() / 2, 1).toInteger()
            
            // Configure memory settings
            task.minHeapSize = "256m"
            task.maxHeapSize = "1g"
            
            // Configure timeouts
            task.timeout = project.providers.provider { 
                return 300000 // 5 minutes in milliseconds
            }
        }
    }
}

