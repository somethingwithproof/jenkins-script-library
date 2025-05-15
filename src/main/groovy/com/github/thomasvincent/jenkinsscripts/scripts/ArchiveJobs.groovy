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

import com.github.thomasvincent.jenkinsscripts.jobs.JobArchivalManager
import com.github.thomasvincent.jenkinsscripts.jobs.JobArchive
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Archives and manages inactive Jenkins jobs.
 * 
 * '''Usage:'''
 * ```groovy
 * # Archive a specific job
 * ./ArchiveJobs.groovy --job my-job --reason "Project completed" --archive-dir /var/jenkins_archives
 * 
 * # Find and archive inactive jobs
 * ./ArchiveJobs.groovy --inactive 90 --reason "Inactive for 90+ days" --archive-dir /var/jenkins_archives
 * 
 * # List archived jobs
 * ./ArchiveJobs.groovy --list --archive-dir /var/jenkins_archives
 * 
 * # Restore a job from archive
 * ./ArchiveJobs.groovy --restore /var/jenkins_archives/my-job_2023-10-01.zip
 * 
 * # Delete an archive
 * ./ArchiveJobs.groovy --delete /var/jenkins_archives/my-job_2023-10-01.zip
 * 
 * # Show help
 * ./ArchiveJobs.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy ArchiveJobs [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    ad(longOpt: 'archive-dir', args: 1, argName: 'path', required: true, 'Directory to store archives')
    
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Job to archive')
    r(longOpt: 'reason', args: 1, argName: 'text', 'Reason for archiving')
    d(longOpt: 'delete-after-archive', 'Delete job after archiving')
    
    m(longOpt: 'metadata', args: 2, valueSeparator: '=', argName: 'key=value', 'Metadata to store with archive (can be used multiple times)')
    
    i(longOpt: 'inactive', args: 1, argName: 'days', 'Find and archive jobs inactive for specified days')
    e(longOpt: 'exclude', args: 1, argName: 'pattern', 'Regex pattern to exclude jobs when using --inactive')
    
    l(longOpt: 'list', 'List archived jobs')
    f(longOpt: 'find', args: 1, argName: 'pattern', 'Find archives matching job name pattern')
    
    rst(longOpt: 'restore', args: 1, argName: 'archivePath', 'Restore a job from archive')
    n(longOpt: 'new-name', args: 1, argName: 'jobName', 'New name for restored job')
    
    del(longOpt: 'delete', args: 1, argName: 'archivePath', 'Delete an archive')
    
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for results')
    J(longOpt: 'json', 'Output in JSON format')
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

// Validate option combinations
if (options.j && !options.r) {
    println "Error: --job requires --reason to specify archival reason"
    cli.usage()
    return
}

if (options.i && !options.r) {
    println "Error: --inactive requires --reason to specify archival reason"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create archive manager
File archiveDir = new File(options.ad)
def manager = new JobArchivalManager(jenkins, archiveDir)

// Parse metadata
def metadata = [:]
if (options.ms) {
    for (int i = 0; i < options.ms.length; i += 2) {
        if (i + 1 < options.ms.length) {
            metadata[options.ms[i]] = options.ms[i + 1]
        }
    }
}

// Determine output format
boolean jsonFormat = options.J ?: false
String outputFile = options.o

// Execute requested operation
if (options.j) {
    // Archive a specific job
    def jobName = options.j
    def reason = options.r
    boolean deleteAfterArchive = options.d ?: false
    
    println "Archiving job: ${jobName}"
    println "Reason: ${reason}"
    println "Delete after archive: ${deleteAfterArchive}"
    if (!metadata.isEmpty()) {
        println "Metadata: ${metadata}"
    }
    
    try {
        JobArchive archive = manager.archiveJob(jobName, reason, metadata, deleteAfterArchive)
        println "Job successfully archived to: ${archive.archivePath}"
    } catch (Exception e) {
        println "Error archiving job: ${e.message}"
    }
} else if (options.i) {
    // Find and archive inactive jobs
    def daysThreshold = Integer.parseInt(options.i)
    def reason = options.r
    boolean deleteAfterArchive = options.d ?: false
    String excludePattern = options.e
    
    println "Finding jobs inactive for ${daysThreshold}+ days..."
    println "Reason: ${reason}"
    println "Delete after archive: ${deleteAfterArchive}"
    if (excludePattern) {
        println "Excluding jobs matching: ${excludePattern}"
    }
    
    try {
        // Find inactive jobs first
        Map<String, Date> inactiveJobs = manager.findInactiveJobs(daysThreshold)
        println "Found ${inactiveJobs.size()} inactive jobs"
        
        // Confirm with user before proceeding
        println "\nDo you want to archive these jobs? (y/n)"
        def reader = new BufferedReader(new InputStreamReader(System.in))
        def response = reader.readLine()
        
        if (response.toLowerCase() != 'y') {
            println "Archive operation cancelled"
            return
        }
        
        // Archive inactive jobs
        List<JobArchive> archives = manager.archiveInactiveJobs(daysThreshold, reason, deleteAfterArchive, excludePattern)
        
        println "\nArchived ${archives.size()} jobs:"
        archives.each { archive ->
            println "- ${archive.jobName} -> ${archive.archivePath}"
        }
        
        // Save report if requested
        if (outputFile) {
            if (jsonFormat) {
                def archivesMap = archives.collect { archive ->
                    [
                        jobName: archive.jobName,
                        archivePath: archive.archivePath,
                        timestamp: archive.timestamp.toString(),
                        reason: archive.reason,
                        metadata: archive.metadata
                    ]
                }
                
                String json = JsonOutput.prettyPrint(JsonOutput.toJson(archivesMap))
                new File(outputFile).text = json
            } else {
                StringBuilder sb = new StringBuilder()
                sb.append("Archived Jobs Report\n")
                sb.append("===================\n")
                sb.append("Generated: ${new Date()}\n")
                sb.append("Days threshold: ${daysThreshold}+\n")
                sb.append("Delete after archive: ${deleteAfterArchive}\n\n")
                sb.append("Total jobs archived: ${archives.size()}\n\n")
                
                archives.each { archive ->
                    sb.append("${archive.jobName}\n")
                    sb.append("  Archive: ${archive.archivePath}\n")
                    sb.append("  Timestamp: ${archive.timestamp}\n")
                    sb.append("  Reason: ${archive.reason}\n")
                    if (archive.metadata) {
                        sb.append("  Metadata:\n")
                        archive.metadata.each { key, value ->
                            sb.append("    ${key}: ${value}\n")
                        }
                    }
                    sb.append("\n")
                }
                
                new File(outputFile).text = sb.toString()
            }
            
            println "Report saved to ${outputFile}"
        }
    } catch (Exception e) {
        println "Error archiving inactive jobs: ${e.message}"
    }
} else if (options.l) {
    // List archived jobs
    println "Listing archived jobs in ${archiveDir}..."
    
    List<JobArchive> archives = manager.listArchives()
    
    if (archives.isEmpty()) {
        println "No archived jobs found"
        return
    }
    
    if (jsonFormat) {
        def archivesMap = archives.collect { archive ->
            [
                jobName: archive.jobName,
                archivePath: archive.archivePath,
                timestamp: archive.timestamp.toString(),
                reason: archive.reason,
                metadata: archive.metadata
            ]
        }
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(archivesMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Archive list saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Archived Jobs List\n")
        sb.append("=================\n")
        sb.append("Total archives: ${archives.size()}\n\n")
        
        archives.each { archive ->
            sb.append("${archive.jobName}\n")
            sb.append("  Archive: ${new File(archive.archivePath).name}\n")
            sb.append("  Timestamp: ${archive.timestamp}\n")
            sb.append("  Reason: ${archive.reason}\n")
            sb.append("\n")
        }
        
        String listText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = listText
            println "Archive list saved to ${outputFile}"
        } else {
            println listText
        }
    }
} else if (options.f) {
    // Find archives matching pattern
    def pattern = options.f
    println "Finding archives matching pattern: ${pattern}"
    
    List<JobArchive> archives = manager.findArchives(pattern)
    
    println "Found ${archives.size()} matching archives"
    
    if (!archives.isEmpty()) {
        archives.each { archive ->
            println "- ${archive.jobName} (${archive.timestamp})"
            println "  Archive: ${archive.archivePath}"
            println "  Reason: ${archive.reason}"
            println ""
        }
    }
} else if (options.rst) {
    // Restore a job from archive
    def archivePath = options.rst
    def newJobName = options.n
    
    println "Restoring job from archive: ${archivePath}"
    if (newJobName) {
        println "New job name: ${newJobName}"
    }
    
    boolean success = manager.restoreJob(archivePath, newJobName)
    
    if (success) {
        println "Job successfully restored"
    } else {
        println "Failed to restore job"
    }
} else if (options.del) {
    // Delete an archive
    def archivePath = options.del
    
    println "Deleting archive: ${archivePath}"
    
    boolean success = manager.deleteArchive(archivePath)
    
    if (success) {
        println "Archive successfully deleted"
    } else {
        println "Failed to delete archive"
    }
} else {
    // Show usage if no operation specified
    println "Jenkins Job Archival Manager"
    println "==========================="
    
    println "\nArchive directory: ${archiveDir}"
    
    try {
        List<JobArchive> archives = manager.listArchives()
        println "Current archives: ${archives.size()}"
        
        // Show recent archives
        if (!archives.isEmpty()) {
            println "\nRecent archives:"
            archives.take(5).each { archive ->
                println "- ${archive.jobName} (${archive.timestamp})"
            }
        }
    } catch (Exception e) {
        println "Error reading archives: ${e.message}"
    }
    
    try {
        Map<String, Date> inactiveJobs = manager.findInactiveJobs(90)
        println "\nInactive jobs (90+ days): ${inactiveJobs.size()}"
    } catch (Exception e) {
        println "Error finding inactive jobs: ${e.message}"
    }
    
    println "\nUse one of the following options:"
    println "- Archive a job: --job JOB_NAME --reason REASON --archive-dir DIR_PATH"
    println "- Find inactive jobs: --inactive DAYS --reason REASON --archive-dir DIR_PATH"
    println "- List archives: --list --archive-dir DIR_PATH"
    println "- Restore a job: --restore ARCHIVE_PATH --archive-dir DIR_PATH"
}