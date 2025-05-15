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

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

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
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        this.jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        this.resetBuildNumber = resetBuildNumber
        this.cleanedJobsLimit = ValidationUtils.requirePositive(cleanedJobsLimit, "cleanedJobsLimit", DEFAULT_CLEANED_JOBS_LIMIT)
        this.buildTotal = ValidationUtils.requirePositive(buildTotal, "buildTotal", DEFAULT_BUILD_TOTAL)
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
        return ErrorHandler.withErrorHandling("cleaning project ${project.name}", {
            deleteBuilds(project)
            
            if (resetBuildNumber) {
                return resetBuildNumber(project)
            }
            
            return true
        }, LOGGER, false)
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
                boolean deleted = ErrorHandler.withErrorHandling("deleting build ${build.number} for job ${project.name}", {
                    build.delete()
                    return true
                }, LOGGER, false)
                
                if (deleted) {
                    deletedCount++
                    LOGGER.fine("Deleted build ${build.number} for job ${project.name}")
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
        return ErrorHandler.withErrorHandling("resetting build number for job ${project.name}", {
            project.updateNextBuildNumber(1)
            project.save()
            LOGGER.info("Reset build number for job ${project.name}")
            return true
        }, LOGGER, false)
    }

}