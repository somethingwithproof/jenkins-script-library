#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/**
 * Controller admin script: preview or disable stale jobs and prune build history.
 *
 * Run from Manage Jenkins -> Script Console or via:
 *   java -jar jenkins-cli.jar -s <url> groovy = < admin-scripts/cleanup-jobs.groovy
 *
 * Optional environment or binding variables:
 *   JOB_PATTERN   Regex pattern of jobs to disable (default: 'deprecated-.*')
 *   CLEAN_JOB     Optional single job name whose build history should be trimmed
 *   BUILD_LIMIT   Max builds to delete when CLEAN_JOB is set (default: 50)
 *   DRY_RUN       true/false (default: true; previews without mutating jobs)
 */

import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import com.github.thomasvincent.jenkinsscripts.jobs.JobDisabler
import jenkins.model.Jenkins
import java.util.logging.Level
import java.util.logging.Logger

Logger log = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.admin.cleanupJobs')

def resolveVar = { String name, String defaultValue ->
    String raw = binding.hasVariable(name) ? binding.getVariable(name)?.toString() : System.getenv(name)
    return (raw == null || raw.trim().isEmpty()) ? defaultValue : raw.trim()
}

try {
    Jenkins jenkins = Jenkins.get()
    String pattern = resolveVar('JOB_PATTERN', 'deprecated-.*')
    String cleanJob = resolveVar('CLEAN_JOB', '')
    int buildLimit = Integer.parseInt(resolveVar('BUILD_LIMIT', '50'))
    boolean dryRun = resolveVar('DRY_RUN', 'true').equalsIgnoreCase('true')

    log.info("Running job cleanup (pattern=${pattern}, dryRun=${dryRun})")
    def disabler = new JobDisabler(jenkins)
    List<String> matched = disabler.disableJobsByPattern(pattern, dryRun)
    println "${dryRun ? 'Matched (dry-run)' : 'Disabled'} ${matched.size()} jobs matching '${pattern}': ${matched}"

    if (cleanJob && !dryRun) {
        def cleaner = new JobCleaner(jenkins, cleanJob, false, 25, buildLimit)
        cleaner.clean()
        log.info("Cleaned build history for ${cleanJob} (limit=${buildLimit})")
        println "Cleaned build history for ${cleanJob}"
    } else if (cleanJob) {
        println "Dry-run enabled; skipping build history cleanup for '${cleanJob}'"
    }
} catch (Exception e) {
    log.log(Level.SEVERE, 'Job cleanup failed', e)
    throw e
}
