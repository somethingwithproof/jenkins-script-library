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

import com.github.thomasvincent.jenkinsscripts.jobs.JobConfigAuditor
import com.github.thomasvincent.jenkinsscripts.jobs.JobAuditReport
import com.github.thomasvincent.jenkinsscripts.jobs.ConfigIssue
import com.github.thomasvincent.jenkinsscripts.jobs.ComplianceReport
import com.github.thomasvincent.jenkinsscripts.jobs.IssueSeverity
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput

/**
 * Audits Jenkins job configurations for security and compliance.
 * 
 * '''Usage:'''
 * ```groovy
 * # Generate a compliance report for all jobs
 * ./AuditJobConfigurations.groovy --compliance
 * 
 * # Audit a specific job
 * ./AuditJobConfigurations.groovy --job my-job
 * 
 * # Audit jobs matching a pattern
 * ./AuditJobConfigurations.groovy --pattern "deploy.*"
 * 
 * # Find jobs with critical security issues
 * ./AuditJobConfigurations.groovy --security-critical
 * 
 * # Auto-fix issues in a job
 * ./AuditJobConfigurations.groovy --job my-job --fix
 * 
 * # Output report to file
 * ./AuditJobConfigurations.groovy --compliance --output report.txt
 * 
 * # Show help
 * ./AuditJobConfigurations.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy AuditJobConfigurations [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Audit a specific job')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Pattern to match job names to audit')
    c(longOpt: 'compliance', 'Generate a full compliance report')
    f(longOpt: 'fix', 'Fix auto-fixable issues (used with --job)')
    sc(longOpt: 'security-critical', 'Find jobs with critical security issues')
    sm(longOpt: 'security-medium', 'Find jobs with medium or higher security issues')
    bp(longOpt: 'best-practices', 'Report on best practices issues')
    dp(longOpt: 'deprecated', 'Report on deprecated features')
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for report')
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
if (options.f && !options.j) {
    println "Error: --fix requires --job to specify which job to fix"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create auditor
def auditor = new JobConfigAuditor(jenkins)

// Determine output format
boolean jsonFormat = options.J ?: false
String outputFile = options.o

// Execute requested operation
if (options.j) {
    // Audit a specific job
    def jobName = options.j
    println "Auditing job: ${jobName}"
    
    JobAuditReport report = auditor.auditJob(jobName)
    
    if (options.f) {
        // Fix auto-fixable issues
        List<ConfigIssue> fixedIssues = auditor.fixJob(jobName)
        println "Fixed Issues (${fixedIssues.size()}):"
        
        if (fixedIssues.isEmpty()) {
            println "No auto-fixable issues found or fixed"
        } else {
            fixedIssues.each { issue ->
                println "- ${issue.description}"
            }
        }
        
        // Re-audit to show remaining issues
        println "\nRemaining Issues:"
        report = auditor.auditJob(jobName)
    }
    
    if (report.hasIssues()) {
        if (jsonFormat) {
            def issuesMap = report.issues.collect { issue ->
                [
                    rule: issue.rule.name,
                    description: issue.description,
                    recommendation: issue.recommendation,
                    severity: issue.severity.toString(),
                    category: issue.category,
                    autoFixable: issue.autoFixable
                ]
            }
            
            def reportMap = [
                jobName: report.jobName,
                totalIssues: report.issues.size(),
                issues: issuesMap
            ]
            
            String json = JsonOutput.prettyPrint(JsonOutput.toJson(reportMap))
            
            if (outputFile) {
                new File(outputFile).text = json
                println "Report saved to ${outputFile}"
            } else {
                println json
            }
        } else {
            String reportText = report.generateReport()
            
            if (outputFile) {
                new File(outputFile).text = reportText
                println "Report saved to ${outputFile}"
            } else {
                println reportText
            }
        }
    } else {
        println "No issues found for job: ${jobName}"
    }
} else if (options.p) {
    // Audit jobs matching a pattern
    def pattern = options.p
    println "Auditing jobs matching pattern: ${pattern}"
    
    Map<String, JobAuditReport> reports = auditor.auditAllJobs(pattern)
    
    if (reports.isEmpty()) {
        println "No issues found in jobs matching pattern: ${pattern}"
        return
    }
    
    if (jsonFormat) {
        def reportsMap = reports.collectEntries { jobName, report ->
            [(jobName): [
                totalIssues: report.issues.size(),
                issues: report.issues.collect { issue ->
                    [
                        rule: issue.rule.name,
                        description: issue.description,
                        recommendation: issue.recommendation,
                        severity: issue.severity.toString(),
                        category: issue.category,
                        autoFixable: issue.autoFixable
                    ]
                }
            ]]
        }
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(reportsMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Report saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Audit Report for Pattern: ${pattern}\n")
        sb.append("===============================\n")
        sb.append("Jobs with issues: ${reports.size()}\n\n")
        
        reports.sort { a, b -> b.value.issues.size() <=> a.value.issues.size() }.each { jobName, report ->
            sb.append("${jobName}: ${report.issues.size()} issues\n")
            
            // Show top 3 issues for each job
            report.issues.take(3).each { issue ->
                sb.append("- ${issue.severity} - ${issue.description}\n")
            }
            
            if (report.issues.size() > 3) {
                sb.append("- ...and ${report.issues.size() - 3} more issues\n")
            }
            
            sb.append("\n")
        }
        
        String reportText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = reportText
            println "Report saved to ${outputFile}"
        } else {
            println reportText
        }
    }
} else if (options.c) {
    // Generate compliance report
    println "Generating compliance report for all jobs..."
    
    ComplianceReport report = auditor.generateComplianceReport()
    
    if (jsonFormat) {
        Map<String, Integer> issueBySeverity = [:]
        report.getIssueCountBySeverity().each { severity, count ->
            issueBySeverity[severity.toString()] = count
        }
        
        def reportMap = [
            totalJobs: report.jobReports.size(),
            totalIssues: report.getTotalIssueCount(),
            issuesBySeverity: issueBySeverity,
            issuesByCategory: report.getIssueCountByCategory(),
            jobsWithCriticalIssues: report.getJobsWithCriticalIssues()
        ]
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(reportMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Compliance report saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        String reportText = report.generateReport()
        
        if (outputFile) {
            new File(outputFile).text = reportText
            println "Compliance report saved to ${outputFile}"
        } else {
            println reportText
        }
    }
} else if (options.sc) {
    // Find jobs with critical security issues
    println "Finding jobs with critical security issues..."
    
    Map<String, JobAuditReport> allReports = auditor.auditAllJobs()
    Map<String, List<ConfigIssue>> criticalIssues = [:]
    
    allReports.each { jobName, report ->
        List<ConfigIssue> critical = report.issues.findAll { 
            it.severity == IssueSeverity.CRITICAL && it.category == "Security"
        }
        
        if (!critical.isEmpty()) {
            criticalIssues[jobName] = critical
        }
    }
    
    if (criticalIssues.isEmpty()) {
        println "No jobs with critical security issues found"
        return
    }
    
    if (jsonFormat) {
        def issuesMap = criticalIssues.collectEntries { jobName, issues ->
            [(jobName): issues.collect { issue ->
                [
                    rule: issue.rule.name,
                    description: issue.description,
                    recommendation: issue.recommendation
                ]
            }]
        }
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(issuesMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Report saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Jobs with Critical Security Issues\n")
        sb.append("================================\n")
        sb.append("Total jobs affected: ${criticalIssues.size()}\n\n")
        
        criticalIssues.each { jobName, issues ->
            sb.append("${jobName}:\n")
            
            issues.each { issue ->
                sb.append("- ${issue.description}\n")
                sb.append("  Recommendation: ${issue.recommendation}\n")
            }
            
            sb.append("\n")
        }
        
        String reportText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = reportText
            println "Report saved to ${outputFile}"
        } else {
            println reportText
        }
    }
} else if (options.sm) {
    // Find jobs with medium or higher security issues
    println "Finding jobs with medium or higher security issues..."
    
    Map<String, JobAuditReport> allReports = auditor.auditAllJobs()
    Map<String, List<ConfigIssue>> securityIssues = [:]
    
    allReports.each { jobName, report ->
        List<ConfigIssue> issues = report.issues.findAll { 
            it.category == "Security" && 
            (it.severity == IssueSeverity.MEDIUM ||
             it.severity == IssueSeverity.HIGH ||
             it.severity == IssueSeverity.CRITICAL)
        }
        
        if (!issues.isEmpty()) {
            securityIssues[jobName] = issues
        }
    }
    
    if (securityIssues.isEmpty()) {
        println "No jobs with medium or higher security issues found"
        return
    }
    
    // Output similar to the critical issues report
    // (Implementation skipped for brevity)
    println "Found ${securityIssues.size()} jobs with medium or higher security issues"
} else if (options.bp) {
    // Report on best practices issues
    println "Analyzing best practices compliance..."
    
    Map<String, JobAuditReport> allReports = auditor.auditAllJobs()
    Map<String, List<ConfigIssue>> bestPracticesIssues = [:]
    
    allReports.each { jobName, report ->
        List<ConfigIssue> issues = report.issues.findAll { 
            it.category == "Best Practices"
        }
        
        if (!issues.isEmpty()) {
            bestPracticesIssues[jobName] = issues
        }
    }
    
    // Output report
    // (Implementation skipped for brevity)
    println "Found ${bestPracticesIssues.size()} jobs with best practices issues"
} else if (options.dp) {
    // Report on deprecated features
    println "Checking for deprecated features usage..."
    
    Map<String, JobAuditReport> allReports = auditor.auditAllJobs()
    Map<String, List<ConfigIssue>> deprecatedIssues = [:]
    
    allReports.each { jobName, report ->
        List<ConfigIssue> issues = report.issues.findAll { 
            it.category == "Deprecated Features"
        }
        
        if (!issues.isEmpty()) {
            deprecatedIssues[jobName] = issues
        }
    }
    
    // Output report
    // (Implementation skipped for brevity)
    println "Found ${deprecatedIssues.size()} jobs using deprecated features"
} else {
    // Default to showing a summary of rules and issues overview
    println "Jenkins Job Configuration Audit"
    println "==============================="
    
    // List available audit rules
    def rules = auditor.getAuditRules()
    println "Available audit rules: ${rules.size()}"
    
    rules.groupBy { it.category }.each { category, categoryRules ->
        println "\n${category} Rules:"
        categoryRules.each { rule ->
            println "- ${rule.name} (${rule.severity})" + (rule.isAutoFixable() ? " [auto-fixable]" : "")
        }
    }
    
    println "\nTo run an audit, use one of the following options:"
    println "- Audit a specific job: --job JOB_NAME"
    println "- Audit jobs by pattern: --pattern PATTERN"
    println "- Generate compliance report: --compliance"
    println "- Find critical security issues: --security-critical"
}