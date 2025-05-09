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
import hudson.model.Job
import hudson.security.Permission

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Disables Jenkins jobs with proper security checks.
 * 
 * Provides methods to disable individual or multiple Jenkins jobs securely.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JobDisabler implements Serializable {

    private static final long serialVersionUID = 1L
    private static final Logger LOGGER = Logger.getLogger(JobDisabler.class.getName())
    
    private final Jenkins jenkins
    
    /**
     * Creates a JobDisabler instance.
     * 
     * Uses provided Jenkins instance or defaults to Jenkins.get().
     * 
     * ```groovy
     * def disabler = new JobDisabler(Jenkins.get())
     * def customDisabler = new JobDisabler(customJenkins)
     * ```
     * 
     * @param jenkins Jenkins instance to use
     */
    JobDisabler(Jenkins jenkins) {
        this.jenkins = jenkins ?: Jenkins.get()
    }
    
    /**
     * Disables all buildable jobs.
     * 
     * Requires admin permissions and performs bulk job disabling.
     * 
     * ```groovy
     * def disabler = new JobDisabler(Jenkins.get())
     * int count = disabler.disableAllJobs()
     * println "Disabled ${count} jobs"
     * ```
     * 
     * @return Number of jobs successfully disabled
     */
    int disableAllJobs() {
        if (!hasAdminPermission()) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
            return 0
        }
        
        List<Job> buildableJobs = findBuildableJobs()
        int disabledCount = 0
        
        buildableJobs.each { job ->
            if (disableJobInternal(job)) {
                disabledCount++
            }
        }
        
        LOGGER.info("Disabled ${disabledCount} of ${buildableJobs.size()} buildable jobs")
        return disabledCount
    }
    
    /**
     * Disables a specific job by name.
     * 
     * Requires admin permissions and validates job existence.
     * 
     * ```groovy
     * def disabler = new JobDisabler(Jenkins.get())
     * 
     * // Disable a pipeline job
     * disabler.disableJob("deployment-pipeline")
     * 
     * // Disable job in a folder
     * disabler.disableJob("project/backend-tests")
     * ```
     * 
     * @param jobName Job's full name to disable
     * @return true if job was disabled, false otherwise
     */
    boolean disableJob(String jobName) {
        if (!hasAdminPermission()) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
            return false
        }
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return false
        }
        
        return disableJobInternal(job)
    }
    
    /**
     * Checks admin permissions for current user.
     * 
     * @return true if user has admin rights, false otherwise
     */
    private boolean hasAdminPermission() {
        return jenkins.hasPermission(Permission.ADMINISTER)
    }
    
    /**
     * Finds all buildable jobs in Jenkins.
     * 
     * Uses Groovy's findAll() for filtering jobs.
     * 
     * @return List of buildable jobs
     */
    private List<Job> findBuildableJobs() {
        return jenkins.getAllItems(Job.class).findAll { it.isBuildable() }
    }
    
    /**
     * Disables a job internally.
     * 
     * Sets job buildable property to false with error handling.
     * 
     * @param job Job to disable
     * @return true if successful, false otherwise
     */
    private boolean disableJobInternal(Job job) {
        try {
            job.setBuildable(false)
            job.save()
            LOGGER.info("Successfully disabled job: ${job.fullName}")
            return true
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to disable job ${job.fullName}", e)
            return false
        }
    }
}