// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import hudson.tasks.ArtifactArchiver
import java.nio.file.Files
import jenkins.model.Jenkins
import com.cloudbees.groovy.cps.NonCPS

/**
 * Pipeline step to backup Jenkins configuration.
 *
 * Usage in Pipeline:
 * ```groovy
 * // Backup to the controller backup directory
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

    if (args.destination && args.destinationDir) {
        error 'Specify either destination or destinationDir'
    }
    String path = performBackupNonCps((args.destinationDir ?: args.destination) as String,
        includeJobs, includePlugins, includeNodes, compress, args.destinationDir != null)
    if (archive) {
        TaskListener listener = getContext(TaskListener)
        archiveBackup(currentBuild.rawBuild, listener, path)
    }
    echo "[backupJenkinsConfig] Created ${path}"
    return path
}

/**
 * Restore Jenkins configuration from backup.
 */
def restore(Map args = [:]) {
    if (!args.backupFile) {
        error "backupFile parameter is required for restore"
    }

    boolean preview = args.dryRun == true || args.listContents == true
    def report = performRestoreNonCps(args.backupFile as String, args.files ?: [], preview)
    if (preview) return report.entries
    if (report.recoveryDirectory) echo "[backupJenkinsConfig] Originals: ${report.recoveryDirectory}"
    echo '[backupJenkinsConfig] Restore complete. Reload configuration explicitly to apply changes.'
    return true
}

@NonCPS
private String performBackupNonCps(String destination, boolean includeJobs, boolean includePlugins,
                                   boolean includeNodes, boolean compress, boolean destinationIsDirectory) {
    def jenkins = Jenkins.get()
    String path = destination ?: new File(jenkins.rootDir, 'backups').path
    return new JenkinsConfigBackup(jenkins).performBackup(path, includeJobs, includePlugins,
        includeNodes, compress, destination == null || destinationIsDirectory)
}

@NonCPS
private void archiveBackup(Run run, TaskListener listener, String path) {
    // Archive directly from private controller staging, including when the build runs on an agent.
    File source = new File(path)
    FilePath staging = new FilePath(Files.createTempDirectory('jenkins-backup-artifact-').toFile())
    try {
        FilePath target = staging.child(source.name)
        if (source.isDirectory()) {
            new FilePath(source).copyRecursiveTo(target)
        } else {
            new FilePath(source).copyTo(target)
        }
        def archiver = new ArtifactArchiver('**')
        archiver.setFingerprint(true)
        archiver.setDefaultExcludes(false)
        archiver.setFollowSymlinks(false)
        archiver.perform(run, staging, new Launcher.LocalLauncher(listener), listener)
    } finally {
        staging.deleteRecursive()
    }
}

@NonCPS
private Map performRestoreNonCps(String backupFilePath, List files, boolean preview) {
    def jenkins = Jenkins.get()
    def backup = new JenkinsConfigBackup(jenkins)
    return backup.restore(backupFilePath, [files: files, dryRun: preview])
}
