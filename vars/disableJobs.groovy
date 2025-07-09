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
    
    def jenkins = Jenkins.get()
    def disabler = new JobDisabler(jenkins)
    def logger = new com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger(
        currentBuild.rawBuild.getListener(), 
        'disableJobs'
    )
    
    def results = []
    
    if (args.jobName) {
        logger.info("Disabling job: ${args.jobName}")
        def success = disabler.disableJob(args.jobName as String)
        results.add([job: args.jobName, success: success])
    } else if (args.pattern) {
        logger.info("Disabling jobs matching pattern: ${args.pattern}")
        def dryRun = args.dryRun ?: false
        def disabledJobs = disabler.disableJobsByPattern(args.pattern as String, dryRun)
        disabledJobs.each { jobName ->
            results.add([job: jobName, success: true])
        }
    } else if (args.folderPath) {
        logger.info("Disabling jobs in folder: ${args.folderPath}")
        def recursive = args.recursive ?: false
        def folder = jenkins.getItemByFullName(args.folderPath as String)
        if (!folder) {
            error "Folder not found: ${args.folderPath}"
        }
        def disabledJobs = disabler.disableJobsInFolder(folder, recursive)
        disabledJobs.each { jobName ->
            results.add([job: jobName, success: true])
        }
    }
    
    logger.info("Disabled ${results.size()} jobs")
    return results
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