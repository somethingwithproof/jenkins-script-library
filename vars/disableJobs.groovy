// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.jobs.JobDisabler
import jenkins.model.Jenkins
import com.cloudbees.groovy.cps.NonCPS

/**
 * Pipeline step to disable Jenkins jobs.
 * 
 * Usage in Pipeline:
 * ```groovy
 * // Disable a single job
 * disableJobs(jobName: 'my-job')
 * 
 * // Disable jobs matching a pattern
 * disableJobs(pattern: 'test-.*', dryRun: true)
 * 
 * // Disable jobs in a folder
 * disableJobs(folderPath: 'my-folder', recursive: true)
 * ```
 */
def call(Map args = [:]) {
    // Validate arguments
    if (!args.jobName && !args.pattern && !args.folderPath) {
        error "Must specify either jobName, pattern, or folderPath"
    }

    if (args.jobName) {
        echo "[disableJobs] Disabling job: ${args.jobName}"
    } else if (args.pattern) {
        echo "[disableJobs] Disabling jobs matching pattern: ${args.pattern}"
    } else if (args.folderPath) {
        echo "[disableJobs] Disabling jobs in folder: ${args.folderPath}"
    }

    Map outcome = disableJobsNonCps(
        args.jobName as String,
        args.pattern as String,
        args.folderPath as String,
        args.dryRun == true,
        args.recursive == true
    )

    if (outcome.errorMessage) {
        error outcome.errorMessage as String
    }

    List results = outcome.results as List
    echo "[disableJobs] Disabled ${results.size()} jobs"
    return results
}

@NonCPS
private Map disableJobsNonCps(String jobName, String pattern, String folderPath, boolean dryRun, boolean recursive) {
    def jenkins = Jenkins.get()
    def disabler = new JobDisabler(jenkins)
    List<Map<String, Object>> results = []

    if (jobName) {
        boolean success = disabler.disableJob(jobName)
        results.add([job: jobName, success: success])
    } else if (pattern) {
        List<String> disabledJobs = disabler.disableJobsByPattern(pattern, dryRun)
        disabledJobs.each { String name ->
            results.add([job: name, success: true])
        }
    } else if (folderPath) {
        def folder = jenkins.getItemByFullName(folderPath)
        if (!folder) {
            return [results: [], errorMessage: "Folder not found: ${folderPath}"]
        }
        List<String> disabledJobs = disabler.disableJobsInFolder(folder, recursive)
        disabledJobs.each { String name ->
            results.add([job: name, success: true])
        }
    }

    return [results: results, errorMessage: null]
}

@NonCPS
def disableSingleJob(String jobName) {
    def jenkins = Jenkins.get()
    def job = jenkins.getItemByFullName(jobName)
    if (job && job.hasProperty('disabled')) {
        job.disabled = true
        job.save()
        return true
    }
    return false
}