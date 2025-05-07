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

package com.github.thomasvincent.jenkinsscripts.groovydoc

/**
 * Jenkins Script Library - Automation Tools for Jenkins
 * 
 * A collection of Groovy utilities and scripts for automating and managing Jenkins
 * operations. This library provides tools to handle common Jenkins admin tasks.
 * 
 * ## Main Features
 * 
 * ```groovy
 * // Backup Jenkins configuration
 * def backupTool = new JenkinsConfigBackup(Jenkins.get())
 * backupTool.createBackup("/backups")
 * 
 * // Audit Jenkins security
 * def auditor = new JenkinsSecurityAuditor(Jenkins.get())
 * def findings = auditor.runFullAudit()
 * 
 * // Manage Helm installations
 * def helm = new HelmHelper(this)
 * helm.use("3.8.0")
 * 
 * // Manage build history
 * def cleaner = new JobCleaner(Jenkins.get(), "project-job", true)
 * cleaner.clean()
 * ```
 * 
 * ## Package Structure
 * 
 * - **config**: Configuration management tools
 * - **helm**: Helm package manager utilities
 * - **jobs**: Jenkins job management
 * - **nodes**: Jenkins agent/node management
 * - **scripts**: Command-line script utilities
 * - **security**: Security auditing and management
 * - **util**: Shared utility classes
 * 
 * @author Thomas Vincent
 * @since 1.0.0
 */
class PackageSummary {
    
    /**
     * Returns the current version of the library.
     */
    static String getVersion() {
        return "1.0.0"
    }
    
    /**
     * Returns information about Groovy compatibility.
     */
    static Map<String, String> getCompatibilityInfo() {
        return [
            'groovyVersion': '4.0+',
            'javaVersion': '17+',
            'jenkinsVersion': '2.361.1+',
            'lastTested': 'May 2025'
        ]
    }
}