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

import com.github.thomasvincent.jenkinsscripts.security.JenkinsSecurityAuditor
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Audits Jenkins security settings and optionally applies fixes.
 * 
 * '''Usage:'''
 * ```groovy
 * # Run basic security audit with text output to console
 * ./AuditJenkinsSecurity.groovy
 * 
 * # Generate HTML security report
 * ./AuditJenkinsSecurity.groovy --format html --output report.html
 * 
 * # Generate JSON security report
 * ./AuditJenkinsSecurity.groovy --format json --output report.json
 * 
 * # Audit and automatically fix critical/high issues
 * ./AuditJenkinsSecurity.groovy --fix
 * 
 * # Audit specific security categories
 * ./AuditJenkinsSecurity.groovy --categories authentication,csrf,agents
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy AuditJenkinsSecurity.groovy [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for report')
    f(longOpt: 'format', args: 1, argName: 'format', 'Report format (text, html, json)')
    x(longOpt: 'fix', 'Attempt to fix critical and high severity issues')
    c(longOpt: 'categories', args: 1, argName: 'categories', 'Comma-separated list of categories to audit')
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

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create auditor
def auditor = new JenkinsSecurityAuditor(jenkins)

// Run the audit
println "Running Jenkins security audit..."
def findings = auditor.runFullAudit()

// Apply fixes if requested
if (options.x) {
    applyFixes(findings, jenkins)
}

// Determine output format
def format = options.f ?: 'text'

// Generate and output report
switch (format.toLowerCase()) {
    case 'html':
        def report = auditor.generateHtmlReport()
        if (options.o) {
            new File(options.o).text = report
            println "HTML report saved to ${options.o}"
        } else {
            println report
        }
        break
        
    case 'json':
        def json = generateJsonReport(findings)
        if (options.o) {
            new File(options.o).text = json
            println "JSON report saved to ${options.o}"
        } else {
            println json
        }
        break
        
    default:
        def report = generateTextReport(findings)
        if (options.o) {
            new File(options.o).text = report
            println "Text report saved to ${options.o}"
        } else {
            println report
        }
        break
}

/**
 * Applies fixes for critical and high severity issues.
 * 
 * @param findings the list of security findings
 * @param jenkins the Jenkins instance
 */
void applyFixes(List findings, def jenkins) {
    def fixableIssues = findings.findAll { 
        (it.severity == JenkinsSecurityAuditor.FindingSeverity.CRITICAL || 
         it.severity == JenkinsSecurityAuditor.FindingSeverity.HIGH) &&
        canFix(it) 
    }
    
    if (fixableIssues.isEmpty()) {
        println "No automatically fixable issues found."
        return
    }
    
    println "Attempting to fix ${fixableIssues.size()} issues:"
    
    fixableIssues.each { finding ->
        println "- Fixing: ${finding.title}"
        applyFixForFinding(finding, jenkins)
    }
    
    jenkins.save()
    println "Fixes applied. Jenkins configuration saved."
}

/**
 * Determines if a finding can be automatically fixed.
 * 
 * @param finding the security finding
 * @return true if the finding can be automatically fixed, false otherwise
 */
boolean canFix(def finding) {
    switch (finding.title) {
        case "CSRF Protection Disabled":
        case "Insecure Agent Protocol":
        case "Anonymous Admin Access":
            return true
        default:
            return false
    }
}

/**
 * Applies a fix for a specific finding.
 * 
 * @param finding the security finding
 * @param jenkins the Jenkins instance
 */
void applyFixForFinding(def finding, def jenkins) {
    switch (finding.title) {
        case "CSRF Protection Disabled":
            // Enable CSRF protection
            jenkins.setCrumbIssuer(new hudson.security.csrf.DefaultCrumbIssuer(true))
            break
            
        case "Insecure Agent Protocol":
            // Disable insecure agent protocols
            def protocols = new HashSet(jenkins.getAgentProtocols())
            protocols.remove("JNLP-connect")
            protocols.remove("JNLP2-connect")
            jenkins.setAgentProtocols(protocols)
            break
            
        case "Anonymous Admin Access":
            // This requires more complex authorization changes
            println "WARNING: Anonymous admin access detected. Manual intervention required to fix."
            break
    }
}

/**
 * Generates a plain text report of security findings.
 * 
 * @param findings the list of security findings
 * @return a string containing the report
 */
String generateTextReport(List findings) {
    def reportBuilder = new StringBuilder()
    
    reportBuilder.append("Jenkins Security Audit Report\n")
    reportBuilder.append("============================\n")
    reportBuilder.append("Generated on: ${new Date().format('yyyy-MM-dd HH:mm:ss')}\n\n")
    
    // Summary
    reportBuilder.append("Summary:\n")
    reportBuilder.append("Found ${findings.size()} security issues:\n")
    JenkinsSecurityAuditor.FindingSeverity.values().each { severity ->
        def count = findings.count { it.severity == severity }
        reportBuilder.append("- ${severity}: ${count}\n")
    }
    reportBuilder.append("\n")
    
    // Group findings by severity
    JenkinsSecurityAuditor.FindingSeverity.values().each { severity ->
        def severityFindings = findings.findAll { it.severity == severity }
        
        if (!severityFindings.isEmpty()) {
            reportBuilder.append("${severity} Findings:\n")
            reportBuilder.append("${'-'.multiply(severity.toString().length() + 10)}\n")
            
            severityFindings.each { finding ->
                reportBuilder.append("* ${finding.title}\n")
                reportBuilder.append("  Category: ${finding.category}\n")
                reportBuilder.append("  ${finding.description}\n\n")
            }
        }
    }
    
    return reportBuilder.toString()
}

/**
 * Generates a JSON report of security findings.
 * 
 * @param findings the list of security findings
 * @return a JSON string containing the report
 */
String generateJsonReport(List findings) {
    def builder = new groovy.json.JsonBuilder()
    
    builder {
        report {
            timestamp new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
            summary {
                total findings.size()
                byCategory(findings.groupBy { it.category }.collectEntries { k, v -> [(k.toString()): v.size()] })
                bySeverity(findings.groupBy { it.severity }.collectEntries { k, v -> [(k.toString()): v.size()] })
            }
            findings findings.collect { finding ->
                [
                    title: finding.title,
                    category: finding.category.toString(),
                    severity: finding.severity.toString(),
                    description: finding.description,
                    timestamp: finding.timestamp.format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
                ]
            }
        }
    }
    
    return builder.toPrettyString()
}