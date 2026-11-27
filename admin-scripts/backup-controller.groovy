#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/**
 * Controller admin script: create a Jenkins configuration backup.
 *
 * Run from Manage Jenkins -> Script Console or via:
 *   java -jar jenkins-cli.jar -s <url> groovy = < admin-scripts/backup-controller.groovy
 *
 * Optional environment or binding variables:
 *   BACKUP_DIR      Target parent directory (default: JENKINS_HOME/backups)
 *   INCLUDE_JOBS    true/false (default: true)
 *   INCLUDE_PLUGINS true/false (default: true)
 *   INCLUDE_NODES   true/false (default: true)
 *   COMPRESS        true/false (default: true)
 */

import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import jenkins.model.Jenkins
import java.util.logging.Level
import java.util.logging.Logger

Logger log = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.admin.backupController')

def resolveFlag = { String name, boolean defaultValue ->
    String raw = binding.hasVariable(name) ? binding.getVariable(name)?.toString() : System.getenv(name)
    if (raw == null || raw.trim().isEmpty()) {
        return defaultValue
    }
    return raw.trim().equalsIgnoreCase('true')
}

try {
    Jenkins jenkins = Jenkins.get()
    String destinationDir = (binding.hasVariable('BACKUP_DIR') ? binding.getVariable('BACKUP_DIR')?.toString() : System.getenv('BACKUP_DIR'))
        ?: new File(jenkins.rootDir, 'backups').path
    boolean includeJobs = resolveFlag('INCLUDE_JOBS', true)
    boolean includePlugins = resolveFlag('INCLUDE_PLUGINS', true)
    boolean includeNodes = resolveFlag('INCLUDE_NODES', true)
    boolean compress = resolveFlag('COMPRESS', true)

    log.info("Starting controller configuration backup to ${destinationDir}")
    def backup = new JenkinsConfigBackup(jenkins)
    String archivePath = backup.performBackup(destinationDir, includeJobs, includePlugins, includeNodes, compress, true)
    log.info("Backup created at ${archivePath}")
    println "Backup created: ${archivePath}"
} catch (Exception e) {
    log.log(Level.SEVERE, 'Controller backup failed', e)
    throw e
}
