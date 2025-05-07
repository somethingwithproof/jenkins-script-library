#!/usr/bin/env groovy

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

package com.github.thomasvincent.jenkinsscripts.scripts

import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Creates backups of Jenkins configuration files.
 * 
 * '''Usage:'''
 * ```groovy
 * # Create a basic backup
 * ./BackupJenkinsConfig.groovy --destination /path/to/backups
 * 
 * # Create an uncompressed backup
 * ./BackupJenkinsConfig.groovy --destination /path/to/backups --no-compress
 * 
 * # Backup specific files/directories
 * ./BackupJenkinsConfig.groovy --destination /path/to/backups --files config.xml,jobs/
 * 
 * # Create backup and retain only 5 most recent backups
 * ./BackupJenkinsConfig.groovy --destination /path/to/backups --keep 5
 * 
 * # List available backups
 * ./BackupJenkinsConfig.groovy --destination /path/to/backups --list
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy BackupJenkinsConfig.groovy [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    d(longOpt: 'destination', args: 1, argName: 'directory', required: true, 'Destination directory for backups')
    c(longOpt: 'compress', 'Compress backup as ZIP file (default)')
    nc(longOpt: 'no-compress', 'Do not compress backup')
    f(longOpt: 'files', args: 1, argName: 'files', 'Comma-separated list of files/directories to backup (relative to JENKINS_HOME)')
    k(longOpt: 'keep', args: 1, argName: 'count', type: Integer, 'Number of recent backups to keep (deletes older backups)')
    l(longOpt: 'list', 'List available backups and exit')
}

// Parse the command line
def options = cli.parse(args)
if (!options) {
    return
}

// Show help and exit if requested
if (options.h) {
    cli.usage()
    return
}

// Get destination directory
def backupDir = options.d

// List backups if requested
if (options.l) {
    listBackups(backupDir)
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create backup tool
def backupTool = new JenkinsConfigBackup(jenkins)

// Configure compression
if (options.nc) {
    backupTool.withCompression(false)
}

// Configure files to backup
if (options.f) {
    def files = options.f.split(',').collect { it.trim() }
    backupTool.withConfigFiles(files)
}

try {
    // Create the backup
    def backupPath = backupTool.createBackup(backupDir)
    println "Backup created successfully at: ${backupPath}"
    
    // Purge old backups if requested
    if (options.k) {
        def keepCount = options.k
        def deletedCount = backupTool.purgeOldBackups(backupDir, keepCount)
        if (deletedCount > 0) {
            println "Deleted ${deletedCount} old backup(s), keeping ${keepCount} most recent"
        }
    }
} catch (Exception e) {
    println "Error creating backup: ${e.message}"
    e.printStackTrace()
    System.exit(1)
}

/**
 * Lists available backups in the specified directory.
 * 
 * @param backupDir the directory containing backups
 */
void listBackups(String backupDir) {
    def jenkins = Jenkins.get()
    def backupTool = new JenkinsConfigBackup(jenkins)
    
    println "Available backups in ${backupDir}:"
    println "-".multiply(80)
    println String.format("%-30s %-20s %-15s %s", "Backup Name", "Date", "Size", "Type")
    println "-".multiply(80)
    
    def backups = backupTool.listAvailableBackups(backupDir)
    
    if (backups.isEmpty()) {
        println "No backups found"
        return
    }
    
    backups.each { backup ->
        def date = backup.date.atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        def sizeStr = formatFileSize(backup.size)
        def type = backup.compressed ? "Compressed (ZIP)" : "Directory"
        
        println String.format("%-30s %-20s %-15s %s", 
            backup.name,
            date,
            sizeStr,
            type
        )
    }
}

/**
 * Formats a file size in bytes to a human-readable string.
 * 
 * @param bytes the size in bytes
 * @return a human-readable size string
 */
String formatFileSize(long bytes) {
    def units = ['B', 'KB', 'MB', 'GB', 'TB']
    def unitIndex = 0
    def size = bytes
    
    while (size > 1024 && unitIndex < units.size() - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}