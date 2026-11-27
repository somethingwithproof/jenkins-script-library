// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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
import hudson.model.Item
import hudson.model.ItemGroup
import hudson.model.Job

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Disables Jenkins jobs with proper security checks.
 *
 * Provides methods to disable individual or multiple Jenkins jobs securely.
 * Supports disabling by name, pattern matching, or all buildable jobs.
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JobDisabler implements Serializable {

    private static final long serialVersionUID = 1L
    private static final Logger LOGGER = Logger.getLogger(JobDisabler.class.getName())

    private transient Jenkins jenkins
    private List<String> jobNames = []
    private Pattern pattern = null

    /**
     * Creates a JobDisabler instance.
     *
     * Uses provided Jenkins instance or resolves Jenkins.get() lazily.
     *
     * ```groovy
     * def disabler = new JobDisabler(Jenkins.get())
     * def customDisabler = new JobDisabler(customJenkins)
     * ```
     *
     * @param jenkins Jenkins instance to use (optional; resolved lazily if null)
     */
    JobDisabler(Jenkins jenkins = null) {
        this.jenkins = jenkins
    }

    private Jenkins getJenkins() {
        return jenkins ?: Jenkins.get()
    }

    /**
     * Specifies job names to disable.
     *
     * @param jobNames List of job names to disable
     * @return This JobDisabler instance for method chaining
     */
    JobDisabler withJobNames(List<String> jobNames) {
        this.jobNames = ValidationUtils.requireNonNull(jobNames, "Job names list")
        return this
    }

    /**
     * Specifies a pattern for job names to disable.
     *
     * @param patternString Regex pattern string for matching job names
     * @return This JobDisabler instance for method chaining
     */
    JobDisabler withPattern(String patternString) {
        ValidationUtils.requireNonEmpty(patternString, "Pattern string")
        this.pattern = Pattern.compile(patternString)
        return this
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

        return disableJobs()
    }

    /**
     * Disables all jobs that match the configured criteria.
     *
     * ```groovy
     * def disabler = new JobDisabler(Jenkins.get())
     *     .withPattern("test-.*")
     * int count = disabler.disableJobs()
     * println "Disabled ${count} matching jobs"
     * ```
     *
     * @return Number of jobs successfully disabled
     */
    int disableJobs() {
        if (!hasAdminPermission()) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
            return 0
        }

        List<Job> jobsToDisable = findJobsToDisable()
        int disabledCount = 0

        jobsToDisable.each { job ->
            if (disableJobInternal(job)) {
                disabledCount++
            }
        }

        LOGGER.info("Disabled ${disabledCount} of ${jobsToDisable.size()} jobs")
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

        try {
            jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        } catch (IllegalArgumentException e) {
            return ErrorHandler.handleErrorWithDefault("validating job name", e, LOGGER, false)
        }

        Job job = getJenkins().getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return false
        }

        return disableJobInternal(job)
    }

    /**
     * Disables jobs matching a regex pattern (or previews them in dry-run mode).
     *
     * @param patternString Regex pattern matching job name or full name
     * @param dryRun When true, returns matching job full names without disabling them
     * @return List of disabled (or matching, in dry-run) job full names
     */
    List<String> disableJobsByPattern(String patternString, boolean dryRun = false) {
        if (!hasAdminPermission()) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
            return []
        }

        ValidationUtils.requireNonEmpty(patternString, "Pattern string")
        Pattern regex = Pattern.compile(patternString)
        List<Job> matching = getJenkins().getAllItems(Job.class).findAll { Job job ->
            regex.matcher(job.getName()).matches() || regex.matcher(job.getFullName()).matches()
        }

        if (dryRun) {
            return matching.collect { it.fullName }
        }

        List<String> disabled = []
        matching.each { Job job ->
            if (disableJobInternal(job)) {
                disabled.add(job.fullName)
            }
        }
        return disabled
    }

    /**
     * Disables jobs contained in a folder/item group.
     *
     * @param folder Folder or ItemGroup containing jobs
     * @param recursive Whether to descend into nested folders
     * @return List of disabled job full names
     */
    List<String> disableJobsInFolder(Object folder, boolean recursive = false) {
        if (!hasAdminPermission()) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.")
            return []
        }
        if (!(folder instanceof ItemGroup)) {
            LOGGER.warning("Provided folder is not an ItemGroup")
            return []
        }

        List<Job> targetJobs = []
        collectJobsInGroup((ItemGroup<?>) folder, recursive, targetJobs)

        List<String> disabled = []
        targetJobs.each { Job job ->
            if (disableJobInternal(job)) {
                disabled.add(job.fullName)
            }
        }
        return disabled
    }

    private void collectJobsInGroup(ItemGroup<?> group, boolean recursive, List<Job> target) {
        group.getItems()?.each { Object item ->
            if (item instanceof Job) {
                target.add((Job) item)
            } else if (recursive && item instanceof ItemGroup) {
                collectJobsInGroup((ItemGroup<?>) item, true, target)
            }
        }
    }

    /**
     * Checks admin permissions for current user.
     *
     * @return true if user has admin rights, false otherwise
     */
    private boolean hasAdminPermission() {
        return getJenkins().hasPermission(Jenkins.ADMINISTER)
    }

    /**
     * Finds jobs to disable based on the configured criteria.
     *
     * @return List of Jenkins jobs that match the criteria
     */
    List<Job> findJobsToDisable() {
        List<Job> allJobs = getJenkins().getAllItems(Job.class)
        List<Job> matchingJobs = []

        // Filter jobs based on criteria
        if (!jobNames.isEmpty()) {
            // Filter by job names
            Set<String> jobNameSet = new HashSet<>(jobNames)
            matchingJobs = allJobs.findAll { job ->
                jobNameSet.contains(job.getName()) || jobNameSet.contains(job.getFullName())
            }
        } else if (pattern != null) {
            // Filter by pattern
            matchingJobs = allJobs.findAll { job ->
                pattern.matcher(job.getName()).matches() || pattern.matcher(job.getFullName()).matches()
            }
        } else {
            // Default: all buildable jobs
            matchingJobs = findBuildableJobs()
        }

        return matchingJobs
    }

    /**
     * Finds all buildable jobs in Jenkins.
     *
     * Uses Groovy's findAll() for filtering jobs.
     *
     * @return List of buildable jobs
     */
    private List<Job> findBuildableJobs() {
        return getJenkins().getAllItems(Job.class).findAll { it.isBuildable() }
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
        ValidationUtils.requireNonNull(job, "Job instance")

        return ErrorHandler.withErrorHandling("disabling job ${job.fullName}", {
            job.setBuildable(false)
            job.save()
            LOGGER.info("Successfully disabled job: ${job.fullName}")
            return true
        }, LOGGER, false)
    }
}
