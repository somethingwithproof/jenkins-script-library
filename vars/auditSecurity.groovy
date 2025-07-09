/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.security.JenkinsSecurityAuditor
import jenkins.model.Jenkins
import com.cloudbees.groovy.cps.NonCPS

/**
 * Pipeline step to audit Jenkins security configuration.
 * 
 * Usage in Pipeline:
 * ```groovy
 * // Basic security audit
 * def issues = auditSecurity()
 * 
 * // Comprehensive audit with email report
 * auditSecurity(
 *     emailReport: 'security@company.com',
 *     includePluginVulnerabilities: true,
 *     checkWeakPasswords: true
 * )
 * 
 * // Fail build if critical issues found
 * def audit = auditSecurity(failOnCritical: true)
 * ```
 */
def call(Map args = [:]) {
    def jenkins = Jenkins.get()
    def auditor = new JenkinsSecurityAuditor(jenkins)
    def logger = new com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger(
        currentBuild.rawBuild.getListener(),
        'auditSecurity'
    )
    
    logger.info("Starting Jenkins security audit")
    
    // Perform audit
    def findings = auditor.performSecurityAudit()
    
    // Process findings
    def criticalCount = 0
    def warningCount = 0
    def infoCount = 0
    
    findings.each { category, categoryFindings ->
        categoryFindings.each { finding ->
            switch(finding.severity) {
                case 'CRITICAL':
                    criticalCount++
                    logger.error("CRITICAL: ${finding.description}")
                    break
                case 'WARNING':
                    warningCount++
                    logger.warning("WARNING: ${finding.description}")
                    break
                default:
                    infoCount++
                    logger.info("INFO: ${finding.description}")
            }
        }
    }
    
    logger.info("Security audit complete: ${criticalCount} critical, " +
                "${warningCount} warnings, ${infoCount} info")
    
    // Generate report if requested
    if (args.emailReport) {
        sendSecurityReport(findings, args.emailReport as String)
    }
    
    // Archive report
    if (args.archive ?: true) {
        def report = generateHtmlReport(findings)
        writeFile file: 'security-audit-report.html', text: report
        archiveArtifacts artifacts: 'security-audit-report.html', fingerprint: true
    }
    
    // Fail build if critical issues found
    if (args.failOnCritical && criticalCount > 0) {
        error "Security audit failed: ${criticalCount} critical issues found"
    }
    
    return [
        findings: findings,
        summary: [
            critical: criticalCount,
            warning: warningCount,
            info: infoCount
        ]
    ]
}

@NonCPS
private String generateHtmlReport(Map findings) {
    def html = new StringBuilder()
    html.append("""
<!DOCTYPE html>
<html>
<head>
    <title>Jenkins Security Audit Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .critical { color: #d9534f; font-weight: bold; }
        .warning { color: #f0ad4e; }
        .info { color: #5bc0de; }
        .finding { margin: 10px 0; padding: 10px; border-left: 4px solid #ccc; }
        .finding.critical { border-color: #d9534f; background-color: #f2dede; }
        .finding.warning { border-color: #f0ad4e; background-color: #fcf8e3; }
        .finding.info { border-color: #5bc0de; background-color: #d9edf7; }
    </style>
</head>
<body>
    <h1>Jenkins Security Audit Report</h1>
    <p>Generated: ${new Date()}</p>
""")
    
    findings.each { category, categoryFindings ->
        html.append("<h2>${category}</h2>")
        categoryFindings.each { finding ->
            def cssClass = finding.severity.toLowerCase()
            html.append("""
    <div class="finding ${cssClass}">
        <strong class="${cssClass}">${finding.severity}:</strong> ${finding.description}
        ${finding.recommendation ? "<br><em>Recommendation:</em> ${finding.recommendation}" : ""}
    </div>
""")
        }
    }
    
    html.append("</body></html>")
    return html.toString()
}

@NonCPS
private void sendSecurityReport(Map findings, String recipient) {
    def subject = "Jenkins Security Audit Report - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = generateEmailBody(findings)
    
    emailext(
        to: recipient,
        subject: subject,
        body: body,
        mimeType: 'text/html'
    )
}

@NonCPS
private String generateEmailBody(Map findings) {
    def criticalCount = findings.values().flatten().count { it.severity == 'CRITICAL' }
    def warningCount = findings.values().flatten().count { it.severity == 'WARNING' }
    
    return """
<h2>Jenkins Security Audit Results</h2>
<p>Build: ${env.BUILD_URL}</p>
<p>Summary: ${criticalCount} critical issues, ${warningCount} warnings</p>
<p>Please review the attached report for details.</p>
"""
}