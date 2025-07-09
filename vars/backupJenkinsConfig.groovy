/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import jenkins.model.Jenkins
import hudson.FilePath
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
 *     destination: '/backup/jenkins-config.tar.gz',
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
    def jenkins = Jenkins.get()
    def backup = new JenkinsConfigBackup(jenkins)
    def logger = new com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger(
        currentBuild.rawBuild.getListener(),
        'backupJenkinsConfig'
    )
    
    // Default options
    def includeJobs = args.includeJobs ?: true
    def includePlugins = args.includePlugins ?: true
    def includeNodes = args.includeNodes ?: true
    def compress = args.compress ?: true
    def archive = args.archive ?: false
    
    // Determine destination
    def destination = args.destination
    if (!destination) {
        def timestamp = new Date().format('yyyy-MM-dd_HH-mm-ss')
        def filename = compress ? "jenkins-backup-${timestamp}.tar.gz" : "jenkins-backup-${timestamp}"
        destination = "${env.WORKSPACE}/${filename}"
    }
    
    logger.info("Starting Jenkins configuration backup")
    logger.info("Options: includeJobs=${includeJobs}, includePlugins=${includePlugins}, " +
                "includeNodes=${includeNodes}, compress=${compress}")
    
    // Perform backup
    def backupPath = backup.performBackup(
        destination,
        includeJobs,
        includePlugins,
        includeNodes,
        compress
    )
    
    if (backupPath) {
        logger.info("Backup completed successfully: ${backupPath}")
        
        // Archive if requested
        if (archive) {
            def workspace = new FilePath(currentBuild.rawBuild.workspace)
            def backupFile = new File(backupPath)
            if (backupFile.exists()) {
                archiveArtifacts artifacts: backupFile.name, fingerprint: true
                logger.info("Backup archived as build artifact")
            }
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
    
    def jenkins = Jenkins.get()
    def backup = new JenkinsConfigBackup(jenkins)
    def logger = new com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger(
        currentBuild.rawBuild.getListener(),
        'backupJenkinsConfig'
    )
    
    logger.warning("Restoring Jenkins configuration from: ${args.backupFile}")
    logger.warning("This will overwrite current configuration!")
    
    def success = backup.restoreBackup(args.backupFile as String)
    
    if (success) {
        logger.info("Restore completed successfully")
        logger.warning("Jenkins restart may be required for all changes to take effect")
    } else {
        error "Restore failed - check logs for details"
    }
    
    return success
}