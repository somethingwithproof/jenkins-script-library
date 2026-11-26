// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import jenkins.model.Jenkins
import com.cloudbees.groovy.cps.NonCPS

/**
 * Pipeline step to backup Jenkins configuration.
 * 
 * Usage in Pipeline:
 * ```groovy
 * // Backup to workspace
 * def backupFile = backupJenkinsConfig()
 * 
 * // Backup to specific location
 * backupJenkinsConfig(
 *     destination: '/backup/jenkins-config.zip',
 *     includeJobs: true,
 *     includePlugins: true,
 *     compress: true
 * )
 * 
 * // Archive the backup
 * backupJenkinsConfig(archive: true)
 * ```
 */
def call(Map args = [:]) {
    // Default options (preserve explicit false)
    boolean includeJobs = args.includeJobs != false
    boolean includePlugins = args.includePlugins != false
    boolean includeNodes = args.includeNodes != false
    boolean compress = args.compress != false
    boolean archive = args.archive == true

    // Determine destination
    String destination = args.destination as String
    if (!destination) {
        String timestamp = new Date().format('yyyy-MM-dd_HH-mm-ss')
        String filename = compress ? "jenkins-backup-${timestamp}.zip" : "jenkins-backup-${timestamp}"
        destination = "${env.WORKSPACE}/${filename}"
    }

    echo "[backupJenkinsConfig] Starting Jenkins configuration backup"
    echo "[backupJenkinsConfig] Options: includeJobs=${includeJobs}, includePlugins=${includePlugins}, " +
         "includeNodes=${includeNodes}, compress=${compress}"

    Map backupResult = performBackupNonCps(destination, includeJobs, includePlugins, includeNodes, compress)
    String backupPath = backupResult.path as String

    if (backupPath) {
        echo "[backupJenkinsConfig] Backup completed successfully: ${backupPath}"

        if (archive && backupResult.exists && backupResult.fileName) {
            archiveArtifacts artifacts: backupResult.fileName as String, fingerprint: true
            echo "[backupJenkinsConfig] Backup archived as build artifact"
        }

        return backupPath
    } else {
        error "Backup failed - check logs for details"
    }
}

/**
 * Restore Jenkins configuration from backup.
 */
def restore(Map args = [:]) {
    if (!args.backupFile) {
        error "backupFile parameter is required for restore"
    }

    echo "[backupJenkinsConfig] WARNING: Restoring Jenkins configuration from: ${args.backupFile}"
    echo "[backupJenkinsConfig] WARNING: This will overwrite current configuration!"

    boolean success = performRestoreNonCps(args.backupFile as String)

    if (success) {
        echo "[backupJenkinsConfig] Restore completed successfully"
        echo "[backupJenkinsConfig] WARNING: Jenkins restart may be required for all changes to take effect"
    } else {
        error "Restore failed - check logs for details"
    }

    return success
}

@NonCPS
private Map performBackupNonCps(String destination, boolean includeJobs, boolean includePlugins,
                                boolean includeNodes, boolean compress) {
    def jenkins = Jenkins.get()
    def backup = new JenkinsConfigBackup(jenkins)
    String backupPath = backup.performBackup(destination, includeJobs, includePlugins, includeNodes, compress)
    if (!backupPath) {
        return [path: null, exists: false, fileName: null]
    }
    File backupFile = new File(backupPath)
    return [
        path: backupPath,
        exists: backupFile.exists(),
        fileName: backupFile.name
    ]
}

@NonCPS
private boolean performRestoreNonCps(String backupFilePath) {
    def jenkins = Jenkins.get()
    def backup = new JenkinsConfigBackup(jenkins)
    return backup.restoreBackup(backupFilePath)
}