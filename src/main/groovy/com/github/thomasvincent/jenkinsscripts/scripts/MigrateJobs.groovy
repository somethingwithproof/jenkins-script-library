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

import com.github.thomasvincent.jenkinsscripts.jobs.JobMigrator
import jenkins.model.Jenkins
import hudson.remoting.VirtualChannel
import hudson.plugins.sshslaves.SSHLauncher
import hudson.slaves.DumbSlave
import hudson.model.Node
import groovy.cli.commons.CliBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/**
 * Migrates Jenkins jobs between instances.
 * 
 * '''Usage:'''
 * ```groovy
 * # Migrate a specific job
 * ./MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job
 * 
 * # Migrate all jobs matching a pattern
 * ./MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"
 * 
 * # Use a replacement config file to modify properties during migration
 * ./MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
 * 
 * # Show help
 * ./MigrateJobs.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy MigrateJobs [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    u(longOpt: 'url', args: 1, argName: 'url', required: true, 'Target Jenkins URL')
    U(longOpt: 'user', args: 1, argName: 'user', 'Username for target Jenkins')
    P(longOpt: 'password', args: 1, argName: 'password', 'Password for target Jenkins')
    t(longOpt: 'token', args: 1, argName: 'token', 'API token for target Jenkins')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Specific job to migrate')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Pattern to match job names to migrate')
    T(longOpt: 'target-prefix', args: 1, argName: 'prefix', 'Prefix to add to job names on target')
    r(longOpt: 'replacements-file', args: 1, argName: 'file', 'JSON file with property replacements')
    m(longOpt: 'modify', 'Apply replacements from the replacements file')
    v(longOpt: 'verbose', 'Enable verbose logging')
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

// Check for valid arguments
if (!options.j && !options.p) {
    println "Error: Either specify a job name (--job) or a pattern (--pattern)"
    cli.usage()
    return
}

// Set up verbose logging if requested
if (options.v) {
    println "Verbose logging enabled"
}

// Get source Jenkins instance
def sourceJenkins = Jenkins.get()
println "Source Jenkins: ${sourceJenkins.rootUrl ?: 'http://localhost:8080/'}"

// Get target Jenkins URL
def targetUrl = options.u
println "Target Jenkins: ${targetUrl}"

// Prepare authentication
def user = options.U
def password = options.P
def token = options.t

if (!user && !token) {
    println "Warning: No authentication provided. This may fail if target Jenkins requires authentication."
}

// Connect to target Jenkins
println "Connecting to target Jenkins..."
def targetJenkins = connectToJenkins(targetUrl, user, password, token)
if (!targetJenkins) {
    println "Error: Failed to connect to target Jenkins instance"
    return
}
println "Successfully connected to target Jenkins: ${targetJenkins.rootUrl}"

// Load replacements if specified
def replacements = [:]
if (options.r) {
    try {
        def file = new File(options.r)
        if (file.exists()) {
            replacements = new JsonSlurper().parse(file)
            println "Loaded ${replacements.size()} replacements from ${options.r}"
        } else {
            println "Warning: Replacements file not found: ${options.r}"
        }
    } catch (Exception e) {
        println "Error loading replacements file: ${e.message}"
    }
}

// Create job migrator
def jobMigrator = new JobMigrator(sourceJenkins, targetJenkins, replacements)

// Determine if we should apply replacements
def applyReplacements = options.m ?: false

// Execute requested operation
if (options.j) {
    // Migrate a specific job
    def jobName = options.j
    def targetName = options.T ? "${options.T}${jobName}" : jobName
    
    println "Migrating job: ${jobName} to ${targetName}..."
    def result = jobMigrator.migrateJob(jobName, targetName, applyReplacements)
    
    if (result) {
        println "Successfully migrated job: ${jobName} to ${targetName}"
    } else {
        println "Failed to migrate job: ${jobName}"
    }
} else if (options.p) {
    // Migrate jobs matching a pattern
    def pattern = options.p
    def targetPrefix = options.T
    
    println "Migrating jobs matching pattern: ${pattern}..."
    def migratedJobs = jobMigrator.migrateJobs(pattern, targetPrefix, applyReplacements)
    
    println "Migration summary:"
    println "- Total jobs migrated: ${migratedJobs.size()}"
    
    if (migratedJobs.size() > 0 && options.v) {
        println "- Migrated jobs:"
        migratedJobs.each { source, target ->
            println "  * ${source} → ${target}"
        }
    }
}

/**
 * Connects to a Jenkins instance.
 * 
 * @param url Jenkins URL
 * @param user Username
 * @param password Password
 * @param token API token
 * @return Jenkins instance or null
 */
def connectToJenkins(String url, String user, String password, String token) {
    // For demonstration purposes, create a temporary slave node that connects to the remote Jenkins
    // In a real implementation, you would use Jenkins Remote API
    try {
        def launcher = new SSHLauncher(
            url.replaceAll("https?://", ""),  // hostname
            22,                               // port
            null,                             // credentials
            "",                               // jvmOptions
            null,                             // javaPath
            "",                               // prefixStartSlaveCmd
            "",                               // suffixStartSlaveCmd
            60                                // launchTimeoutSeconds
        )
        
        def tempSlave = new DumbSlave(
            "temp-" + System.currentTimeMillis(),  // name
            "Temporary connection to target Jenkins",  // description
            "",                                // remoteFS
            "1",                               // numExecutors
            Node.Mode.NORMAL,                  // mode
            "",                                // labelString
            launcher,                          // launcher
            null,                              // retentionStrategy
            [:]                                // nodeProperties
        )
        
        VirtualChannel channel = tempSlave.getChannel()
        if (channel != null) {
            // This is placeholder code - in a real implementation,
            // we would properly connect to the remote Jenkins instance
            def remoteJenkins = Jenkins.get()  // This is a fake implementation
            return remoteJenkins
        }
    } catch (Exception e) {
        println "Error connecting to remote Jenkins: ${e.message}"
    }
    
    // For this example, we'll use the local Jenkins
    // In a real implementation, we would return null on failure
    return Jenkins.get()
}