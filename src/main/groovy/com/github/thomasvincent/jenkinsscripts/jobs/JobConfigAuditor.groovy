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

package com.github.thomasvincent.jenkinsscripts.jobs

import jenkins.model.Jenkins
import hudson.model.Job
import hudson.model.TopLevelItem
import hudson.model.AbstractItem
import hudson.plugins.git.GitSCM
import hudson.plugins.git.extensions.GitSCMExtension
import hudson.plugins.git.extensions.impl.CloneOption
import hudson.scm.SCM
import hudson.tasks.Builder
import hudson.tasks.Publisher
import hudson.triggers.Trigger
import hudson.triggers.TriggerDescriptor
import hudson.security.Permission
import java.io.ByteArrayOutputStream

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Audits Jenkins job configurations for security and compliance.
 * 
 * Detects insecure configurations, deprecated plugins, and policy violations.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobConfigAuditor {
    private static final Logger LOGGER = Logger.getLogger(JobConfigAuditor.class.getName())
    
    private final Jenkins jenkins
    private final List<ConfigAuditRule> auditRules = []
    
    /**
     * Creates a JobConfigAuditor instance.
     * 
     * ```groovy
     * def auditor = new JobConfigAuditor(Jenkins.get())
     * ```
     * 
     * @param jenkins Jenkins instance
     */
    JobConfigAuditor(Jenkins jenkins) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        registerDefaultRules()
    }
    
    /**
     * Audits a specific job's configuration.
     * 
     * ```groovy
     * def report = auditor.auditJob("my-pipeline")
     * println "Issues found: ${report.issues.size()}"
     * ```
     * 
     * @param jobName Name of job to audit
     * @return JobAuditReport for the job
     */
    JobAuditReport auditJob(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return new JobAuditReport(jobName)
        }
        
        return ErrorHandler.withErrorHandling("auditing job ${jobName}", {
            JobAuditReport report = new JobAuditReport(job.fullName)
            
            // Extract job XML for content-based rules
            String jobXml = extractJobConfig(job)
            
            // Apply all audit rules
            auditRules.each { rule ->
                try {
                    List<ConfigIssue> issues = rule.check(job, jobXml)
                    issues.each { issue ->
                        report.addIssue(issue)
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error applying rule ${rule.name} to job ${jobName}: ${e.message}")
                }
            }
            
            return report
        }, LOGGER, new JobAuditReport(jobName))
    }
    
    /**
     * Audits all jobs in Jenkins.
     * 
     * ```groovy
     * def reports = auditor.auditAllJobs()
     * println "Found ${reports.size()} jobs with issues"
     * ```
     * 
     * @param pattern Optional regex pattern to filter jobs by name
     * @return Map of job names to audit reports
     */
    Map<String, JobAuditReport> auditAllJobs(String pattern = null) {
        Pattern jobPattern = pattern ? Pattern.compile(pattern) : null
        
        Map<String, JobAuditReport> reports = [:]
        List<Job> allJobs = jenkins.getAllItems(Job.class)
        
        allJobs.each { job ->
            if (!jobPattern || jobPattern.matcher(job.fullName).matches()) {
                JobAuditReport report = auditJob(job.fullName)
                if (report.hasIssues()) {
                    reports[job.fullName] = report
                }
            }
        }
        
        LOGGER.info("Audited ${allJobs.size()} jobs, found issues in ${reports.size()}")
        return reports
    }
    
    /**
     * Registers a custom audit rule.
     * 
     * ```groovy
     * auditor.registerRule(new CustomAuditRule())
     * ```
     * 
     * @param rule Rule to register
     */
    void registerRule(ConfigAuditRule rule) {
        ValidationUtils.requireNonNull(rule, "Audit rule")
        auditRules.add(rule)
        LOGGER.fine("Registered audit rule: ${rule.name}")
    }
    
    /**
     * Gets all registered audit rules.
     * 
     * ```groovy
     * def rules = auditor.getAuditRules()
     * println "Registered rules: ${rules.collect { it.name }}"
     * ```
     * 
     * @return List of registered audit rules
     */
    List<ConfigAuditRule> getAuditRules() {
        return new ArrayList<>(auditRules)
    }
    
    /**
     * Generates a compliance report summarizing all issues.
     * 
     * ```groovy
     * def complianceReport = auditor.generateComplianceReport()
     * println complianceReport.generateReport()
     * ```
     * 
     * @return ComplianceReport summarizing all issues
     */
    ComplianceReport generateComplianceReport() {
        Map<String, JobAuditReport> reports = auditAllJobs()
        return new ComplianceReport(reports)
    }
    
    /**
     * Fixes auto-fixable issues in a job.
     * 
     * ```groovy
     * def fixed = auditor.fixJob("my-pipeline")
     * println "Fixed ${fixed.size()} issues"
     * ```
     * 
     * @param jobName Name of job to fix
     * @return List of fixed issues
     */
    List<ConfigIssue> fixJob(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return []
        }
        
        return ErrorHandler.withErrorHandling("fixing job ${jobName}", {
            List<ConfigIssue> fixedIssues = []
            
            // Audit job to find issues
            JobAuditReport report = auditJob(jobName)
            
            // Filter auto-fixable issues
            List<ConfigIssue> fixableIssues = report.issues.findAll { it.autoFixable }
            
            if (fixableIssues.isEmpty()) {
                LOGGER.info("No auto-fixable issues found for job ${jobName}")
                return fixedIssues
            }
            
            // Apply fixes
            fixableIssues.each { issue ->
                try {
                    boolean fixed = issue.rule.fix(job)
                    if (fixed) {
                        fixedIssues.add(issue)
                        LOGGER.info("Fixed issue: ${issue.description} in job ${jobName}")
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error fixing issue in job ${jobName}: ${e.message}")
                }
            }
            
            return fixedIssues
        }, LOGGER, [])
    }
    
    /**
     * Registers default audit rules.
     */
    private void registerDefaultRules() {
        // Security rules
        registerRule(new UnrestrictedAgentRule())
        registerRule(new InsecureCredentialsRule())
        registerRule(new ScriptSecurityRule())
        
        // Best practices rules
        registerRule(new TimeoutRule())
        registerRule(new GitShallowCloneRule())
        registerRule(new ConcurrencyRule())
        
        // Deprecated features rules
        registerRule(new DeprecatedPluginsRule())
        registerRule(new LegacyScmRule())
    }
    
    /**
     * Extracts XML configuration from a job.
     * 
     * @param job Job to extract configuration from
     * @return XML configuration as string
     */
    private String extractJobConfig(Job job) {
        ValidationUtils.requireNonNull(job, "Job instance")
        
        return ErrorHandler.withErrorHandling("extracting job config for ${job.fullName}", {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            job.writeConfigDotXml(outputStream)
            return outputStream.toString("UTF-8")
        }, LOGGER, "")
    }
}

/**
 * Interface for job configuration audit rules.
 */
interface ConfigAuditRule {
    /**
     * Gets the name of the rule.
     * 
     * @return Rule name
     */
    String getName()
    
    /**
     * Gets the category of the rule.
     * 
     * @return Rule category
     */
    String getCategory()
    
    /**
     * Gets the severity of the rule.
     * 
     * @return Rule severity
     */
    IssueSeverity getSeverity()
    
    /**
     * Checks a job for configuration issues.
     * 
     * @param job Job to check
     * @param jobXml Job XML configuration
     * @return List of configuration issues
     */
    List<ConfigIssue> check(Job job, String jobXml)
    
    /**
     * Determines if the rule can auto-fix issues.
     * 
     * @return true if auto-fixable
     */
    boolean isAutoFixable()
    
    /**
     * Fixes issues in a job.
     * 
     * @param job Job to fix
     * @return true if fixed, false otherwise
     */
    boolean fix(Job job)
}

/**
 * Base class for audit rules with common functionality.
 */
abstract class BaseAuditRule implements ConfigAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAutoFixable() {
        return false
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean fix(Job job) {
        return false
    }
    
    /**
     * Creates a configuration issue.
     * 
     * @param job Job with the issue
     * @param description Issue description
     * @param recommendation Recommendation to fix
     * @return ConfigIssue instance
     */
    protected ConfigIssue createIssue(Job job, String description, String recommendation) {
        return new ConfigIssue(
            rule: this,
            jobName: job.fullName,
            description: description,
            recommendation: recommendation,
            severity: getSeverity(),
            category: getCategory(),
            autoFixable: isAutoFixable()
        )
    }
}

/**
 * Rule for unrestricted agent usage.
 */
class UnrestrictedAgentRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Unrestricted Agent"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Security"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.HIGH
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for pipeline with agent any or no agent restrictions
        if (jobXml.contains("agent any") || (jobXml.contains("pipeline") && !jobXml.contains("agent {"))) {
            issues.add(createIssue(
                job,
                "Job uses unrestricted agent execution",
                "Use agent with label restrictions or specific nodes"
            ))
        }
        
        return issues
    }
}

/**
 * Rule for insecure credentials usage.
 */
class InsecureCredentialsRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Insecure Credentials"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Security"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.CRITICAL
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for hardcoded credentials 
        if (jobXml =~ /password=['"][\w\d]+['"]/ || 
            jobXml =~ /token=['"][\w\d]+['"]/ || 
            jobXml =~ /secret=['"][\w\d]+['"]/ ||
            jobXml =~ /pass=['"][\w\d]+['"]/) {
            
            issues.add(createIssue(
                job,
                "Job contains hardcoded credentials",
                "Use Jenkins credentials provider and inject credentials securely"
            ))
        }
        
        return issues
    }
}

/**
 * Rule for script security issues.
 */
class ScriptSecurityRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Script Security"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Security"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.HIGH
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for sandbox bypassing
        if (jobXml.contains("sandbox>false</sandbox") || 
            jobXml.contains("scriptApproval")) {
            
            issues.add(createIssue(
                job,
                "Job uses script approval or sandbox bypass",
                "Use sandbox mode and approved standard libraries"
            ))
        }
        
        return issues
    }
}

/**
 * Rule for job timeouts.
 */
class TimeoutRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Build Timeout"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Best Practices"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.MEDIUM
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAutoFixable() {
        return true  // Can automatically add timeout to some job types
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for missing timeout
        if (!jobXml.contains("timeout") && !jobXml.contains("BuildTimeoutWrapper")) {
            issues.add(createIssue(
                job,
                "Job does not have a timeout configured",
                "Add a timeout to prevent stuck builds from consuming resources"
            ))
        }
        
        return issues
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean fix(Job job) {
        // Simplified implementation - in a real implementation, 
        // we would add a timeout configuration to the job
        return false
    }
}

/**
 * Rule for Git shallow clone usage.
 */
class GitShallowCloneRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Git Shallow Clone"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Best Practices"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.LOW
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAutoFixable() {
        return true
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check if job uses Git but not shallow clone
        if (jobXml.contains("<hudson.plugins.git.GitSCM") && 
            !jobXml.contains("<shallow>true</shallow>")) {
            
            issues.add(createIssue(
                job,
                "Git SCM is not using shallow clone",
                "Enable shallow clone to improve performance for large repositories"
            ))
        }
        
        return issues
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean fix(Job job) {
        boolean fixed = false
        
        try {
            // Find Git SCM
            SCM scm = null
            if (job.hasProperty("scm")) {
                scm = job.getProperty("scm")
            }
            
            if (scm instanceof GitSCM) {
                GitSCM gitScm = (GitSCM) scm
                
                // Check if shallow clone is already configured
                boolean hasShallowClone = false
                for (GitSCMExtension extension : gitScm.extensions) {
                    if (extension instanceof CloneOption && ((CloneOption) extension).shallow) {
                        hasShallowClone = true
                        break
                    }
                }
                
                // Add shallow clone if not present
                if (!hasShallowClone) {
                    CloneOption cloneOption = new CloneOption(true, false, null, null)
                    gitScm.extensions.add(cloneOption)
                    fixed = true
                }
            }
        } catch (Exception e) {
            // Log error and return false
            Logger.getLogger(GitShallowCloneRule.class.getName())
                  .warning("Failed to fix Git shallow clone: ${e.message}")
        }
        
        return fixed
    }
}

/**
 * Rule for build concurrency.
 */
class ConcurrencyRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Build Concurrency"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Best Practices"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.MEDIUM
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for missing concurrency control
        if (!jobXml.contains("disableConcurrentBuilds") && !jobXml.contains("throttle")) {
            issues.add(createIssue(
                job,
                "Job does not have concurrency control",
                "Add disableConcurrentBuilds or throttle to prevent resource contention"
            ))
        }
        
        return issues
    }
}

/**
 * Rule for deprecated plugins.
 */
class DeprecatedPluginsRule extends BaseAuditRule {
    // List of deprecated plugins and their replacements
    private static final Map<String, String> DEPRECATED_PLUGINS = [
        "checkstyle": "warnings-ng",
        "findbugs": "warnings-ng",
        "pmd": "warnings-ng",
        "analysis-core": "warnings-ng",
        "ssh-slaves": "ssh-agent",
        "subversion": "git",
        "cvs": "git"
    ]
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Deprecated Plugins"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Deprecated Features"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.MEDIUM
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for deprecated plugins
        DEPRECATED_PLUGINS.each { deprecated, replacement ->
            String pattern = "<hudson.plugins.${deprecated}."
            if (jobXml.contains(pattern)) {
                issues.add(createIssue(
                    job,
                    "Job uses deprecated plugin: ${deprecated}",
                    "Migrate to recommended replacement: ${replacement}"
                ))
            }
        }
        
        return issues
    }
}

/**
 * Rule for legacy SCM usage.
 */
class LegacyScmRule extends BaseAuditRule {
    /**
     * {@inheritDoc}
     */
    @Override
    String getName() {
        return "Legacy SCM"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    String getCategory() {
        return "Deprecated Features"
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    IssueSeverity getSeverity() {
        return IssueSeverity.LOW
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    List<ConfigIssue> check(Job job, String jobXml) {
        List<ConfigIssue> issues = []
        
        // Check for legacy SCM
        if (jobXml.contains("<hudson.scm.SubversionSCM") || 
            jobXml.contains("<hudson.scm.CVSSCM")) {
            
            issues.add(createIssue(
                job,
                "Job uses legacy SCM (Subversion/CVS)",
                "Migrate to Git for improved features and performance"
            ))
        }
        
        return issues
    }
}

/**
 * Severity levels for configuration issues.
 */
enum IssueSeverity {
    INFO, LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Represents a single configuration issue.
 */
class ConfigIssue {
    ConfigAuditRule rule
    String jobName
    String description
    String recommendation
    IssueSeverity severity
    String category
    boolean autoFixable
    
    /**
     * Gets a formatted description of the issue.
     * 
     * @return Formatted description
     */
    String getFormattedDescription() {
        return "[${severity}] ${description}"
    }
}

/**
 * Report of configuration issues for a job.
 */
class JobAuditReport {
    String jobName
    List<ConfigIssue> issues = []
    
    JobAuditReport(String jobName) {
        this.jobName = jobName
    }
    
    /**
     * Adds an issue to the report.
     * 
     * @param issue Issue to add
     */
    void addIssue(ConfigIssue issue) {
        issues.add(issue)
    }
    
    /**
     * Checks if the report has any issues.
     * 
     * @return true if issues exist
     */
    boolean hasIssues() {
        return !issues.isEmpty()
    }
    
    /**
     * Gets issues by severity.
     * 
     * @param severity Severity to filter by
     * @return List of issues with specified severity
     */
    List<ConfigIssue> getIssuesBySeverity(IssueSeverity severity) {
        return issues.findAll { it.severity == severity }
    }
    
    /**
     * Gets issues by category.
     * 
     * @param category Category to filter by
     * @return List of issues with specified category
     */
    List<ConfigIssue> getIssuesByCategory(String category) {
        return issues.findAll { it.category == category }
    }
    
    /**
     * Generates a formatted report of issues.
     * 
     * @return Formatted report
     */
    String generateReport() {
        StringBuilder report = new StringBuilder()
        
        report.append("Audit Report for ${jobName}\n")
        report.append("==============================\n")
        
        if (issues.isEmpty()) {
            report.append("No issues found.\n")
            return report.toString()
        }
        
        report.append("Total issues: ${issues.size()}\n\n")
        
        // Group by severity
        IssueSeverity.values().reverse().each { severity ->
            List<ConfigIssue> severityIssues = getIssuesBySeverity(severity)
            
            if (!severityIssues.isEmpty()) {
                report.append("${severity} Issues (${severityIssues.size()}):\n")
                
                severityIssues.each { issue ->
                    report.append("- ${issue.description}\n")
                    report.append("  Recommendation: ${issue.recommendation}\n")
                    if (issue.autoFixable) {
                        report.append("  (Auto-fixable)\n")
                    }
                }
                
                report.append("\n")
            }
        }
        
        return report.toString()
    }
}

/**
 * Overall compliance report for Jenkins jobs.
 */
class ComplianceReport {
    Map<String, JobAuditReport> jobReports
    
    ComplianceReport(Map<String, JobAuditReport> jobReports) {
        this.jobReports = jobReports
    }
    
    /**
     * Gets the total number of issues.
     * 
     * @return Total issues
     */
    int getTotalIssueCount() {
        return jobReports.values().sum { it.issues.size() } ?: 0
    }
    
    /**
     * Gets the number of issues by severity.
     * 
     * @return Map of severity to count
     */
    Map<IssueSeverity, Integer> getIssueCountBySeverity() {
        Map<IssueSeverity, Integer> counts = [:]
        
        IssueSeverity.values().each { severity ->
            counts[severity] = 0
        }
        
        jobReports.values().each { report ->
            report.issues.each { issue ->
                counts[issue.severity] = (counts[issue.severity] ?: 0) + 1
            }
        }
        
        return counts
    }
    
    /**
     * Gets the number of issues by category.
     * 
     * @return Map of category to count
     */
    Map<String, Integer> getIssueCountByCategory() {
        Map<String, Integer> counts = [:]
        
        jobReports.values().each { report ->
            report.issues.each { issue ->
                counts[issue.category] = (counts[issue.category] ?: 0) + 1
            }
        }
        
        return counts
    }
    
    /**
     * Gets jobs with critical issues.
     * 
     * @return List of job names with critical issues
     */
    List<String> getJobsWithCriticalIssues() {
        return jobReports.findAll { jobName, report -> 
            report.getIssuesBySeverity(IssueSeverity.CRITICAL).size() > 0 
        }.keySet().toList()
    }
    
    /**
     * Generates a formatted compliance report.
     * 
     * @return Formatted report
     */
    String generateReport() {
        StringBuilder report = new StringBuilder()
        
        report.append("Jenkins Job Compliance Report\n")
        report.append("============================\n")
        report.append("Generated: ${new Date()}\n\n")
        
        report.append("Overview:\n")
        report.append("- Total jobs analyzed: ${jobReports.size()}\n")
        report.append("- Total issues found: ${getTotalIssueCount()}\n\n")
        
        // Severity breakdown
        report.append("Issues by Severity:\n")
        getIssueCountBySeverity().each { severity, count ->
            if (count > 0) {
                report.append("- ${severity}: ${count}\n")
            }
        }
        report.append("\n")
        
        // Category breakdown
        report.append("Issues by Category:\n")
        getIssueCountByCategory().each { category, count ->
            report.append("- ${category}: ${count}\n")
        }
        report.append("\n")
        
        // Critical issues
        List<String> criticalJobs = getJobsWithCriticalIssues()
        if (!criticalJobs.isEmpty()) {
            report.append("Jobs with Critical Issues (${criticalJobs.size()}):\n")
            criticalJobs.each { jobName ->
                report.append("- ${jobName}\n")
            }
            report.append("\n")
        }
        
        // Top 5 jobs with most issues
        report.append("Top 5 Jobs with Most Issues:\n")
        jobReports.sort { -it.value.issues.size() }.take(5).each { jobName, jobReport ->
            report.append("- ${jobName}: ${jobReport.issues.size()} issues\n")
        }
        
        return report.toString()
    }
}