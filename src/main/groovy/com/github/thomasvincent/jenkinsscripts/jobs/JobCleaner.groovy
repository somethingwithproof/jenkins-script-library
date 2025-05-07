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
import hudson.model.AbstractProject
import hudson.model.Job
import hudson.model.TopLevelItem
import org.kohsuke.stapler.DataBoundConstructor

import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Cleans Jenkins jobs by removing old builds and resetting build numbers.
 * 
 * Handles build cleanup for Jenkins jobs with validation and safe error handling.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JobCleaner {
    private static final Logger LOGGER = Logger.getLogger(JobCleaner.class.getName())
    private static final int DEFAULT_CLEANED_JOBS_LIMIT = 25
    private static final int DEFAULT_BUILD_TOTAL = 100

    private final Jenkins jenkins
    private final String jobName
    private final boolean resetBuildNumber
    private final int cleanedJobsLimit
    private final int buildTotal

    /**
     * Creates a JobCleaner with specified parameters.
     * 
     * ```groovy
     * def cleaner = new JobCleaner(Jenkins.instance, 'my-job', true)
     * cleaner.clean()  // Cleans job and resets build number to 1
     * ```
     * 
     * @param jenkins Jenkins instance
     * @param jobName Job to clean
     * @param resetBuildNumber Reset build number to 1 if true
     * @param cleanedJobsLimit Max jobs to clean (default: 25)
     * @param buildTotal Max builds to delete (default: 100)
     */
    @DataBoundConstructor
    JobCleaner(Jenkins jenkins, String jobName, boolean resetBuildNumber = false, 
               int cleanedJobsLimit = DEFAULT_CLEANED_JOBS_LIMIT, 
               int buildTotal = DEFAULT_BUILD_TOTAL) {
        this.jenkins = validateNotNull(jenkins, "Jenkins instance cannot be null")
        this.jobName = validateNotNullOrEmpty(jobName, "Job name cannot be null or empty").trim()
        this.resetBuildNumber = resetBuildNumber
        this.cleanedJobsLimit = validatePositive(cleanedJobsLimit, "cleanedJobsLimit", DEFAULT_CLEANED_JOBS_LIMIT)
        this.buildTotal = validatePositive(buildTotal, "buildTotal", DEFAULT_BUILD_TOTAL)
    }

    /**
     * Cleans the specified job.
     * 
     * Finds the job and initiates cleaning based on job type.
     * 
     * ```groovy
     * // Basic cleaning without build reset
     * def basicCleaner = new JobCleaner(Jenkins.instance, 'deploy-job')
     * basicCleaner.clean()
     * 
     * // Clean with custom limits (first 50 builds, reset to build #1)
     * def advancedCleaner = new JobCleaner(
     *     Jenkins.instance, 
     *     'test-job', 
     *     true,       // Reset build number
     *     10,         // Max 10 jobs in folder
     *     50          // Delete up to 50 builds
     * )
     * advancedCleaner.clean()
     * ```
     * 
     * @return true if cleaning succeeded, false otherwise
     */
    boolean clean() {
        TopLevelItem item = jenkins.getItemByFullName(jobName, TopLevelItem.class)
        if (item == null) {
            LOGGER.warning("Item not found: ${jobName}")
            return false
        }

        if (item instanceof AbstractProject) {
            return cleanProject((AbstractProject) item)
        } else {
            LOGGER.warning("Unsupported job type: ${jobName}")
            return false
        }
    }

    /**
     * Cleans a project by removing builds and optionally resetting build numbers.
     * 
     * @param project Project to clean
     * @return true if successful, false otherwise
     */
    private boolean cleanProject(AbstractProject<?, ?> project) {
        try {
            deleteBuilds(project)
            
            if (resetBuildNumber) {
                return resetBuildNumber(project)
            }
            
            return true
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cleaning project ${project.name}", e)
            return false
        }
    }
    
    /**
     * Deletes builds from a project up to the specified limit.
     * 
     * Uses Groovy's each() for iterating through builds.
     * 
     * @param project Project to delete builds from
     */
    private void deleteBuilds(AbstractProject<?, ?> project) {
        int deletedCount = 0
        
        project.getBuilds().each { build ->
            if (deletedCount < buildTotal) {
                try {
                    build.delete()
                    deletedCount++
                    LOGGER.fine("Deleted build ${build.number} for job ${project.name}")
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to delete build ${build.number} for job ${project.name}", e)
                }
            }
        }
        
        LOGGER.info("Deleted ${deletedCount} builds from job ${project.name}")
    }

    /**
     * Resets project build number to 1.
     * 
     * @param project Project to reset
     * @return true if successful, false otherwise
     */
    private boolean resetBuildNumber(AbstractProject<?, ?> project) {
        try {
            project.updateNextBuildNumber(1)
            project.save()
            LOGGER.info("Reset build number for job ${project.name}")
            return true
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to reset build number for job ${project.name}", e)
            return false
        }
    }

    /**
     * Validates non-null value.
     * 
     * ```groovy
     * validateNotNull(obj, "Object must not be null")
     * ```
     * 
     * @param arg Value to validate
     * @param message Error message
     * @return Validated value
     */
    private static <T> T validateNotNull(T arg, String message) {
        if (arg == null) {
            throw new IllegalArgumentException(message)
        }
        return arg
    }

    /**
     * Validates non-empty string.
     * 
     * ```groovy
     * validateNotNullOrEmpty(name, "Name is required")
     * ```
     * 
     * @param arg String to validate
     * @param message Error message
     * @return Validated string
     */
    private static String validateNotNullOrEmpty(String arg, String message) {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException(message)
        }
        return arg
    }
    
    /**
     * Validates positive integer.
     * 
     * Uses default if value is non-positive.
     * 
     * ```groovy
     * validatePositive(count, "count", 10)  // Returns count if positive, 10 otherwise
     * ```
     * 
     * @param value Value to validate
     * @param name Parameter name for logging
     * @param defaultValue Fallback value
     * @return Validated or default value
     */
    private static int validatePositive(int value, String name, int defaultValue) {
        if (value <= 0) {
            LOGGER.warning("${name} must be positive. Using default value ${defaultValue}")
            return defaultValue
        }
        return value
    }
}