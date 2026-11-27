// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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
    echo "[auditSecurity] Starting Jenkins security audit"

    // Confine Jenkins and auditor objects to @NonCPS so only serializable maps cross CPS steps
    Map auditResult = runSecurityAuditNonCps()
    Map findings = auditResult.findings as Map
    Map summary = auditResult.summary as Map

    findings.each { category, categoryFindings ->
        categoryFindings.each { finding ->
            switch (finding.severity) {
                case 'CRITICAL':
                    echo "[auditSecurity] CRITICAL: ${finding.description}"
                    break
                case 'HIGH':
                case 'WARNING':
                    echo "[auditSecurity] WARNING: ${finding.description}"
                    break
                default:
                    echo "[auditSecurity] INFO: ${finding.description}"
            }
        }
    }

    echo "[auditSecurity] Security audit complete: ${summary.critical} critical, " +
         "${summary.warning} warnings, ${summary.info} info"

    // Generate email report if requested (CPS step, must NOT be @NonCPS)
    if (args.emailReport) {
        sendSecurityReport(findings, args.emailReport as String)
    }

    // Archive report (default true; preserve explicit false)
    if (args.archive != false) {
        String report = generateHtmlReport(findings)
        writeFile file: 'security-audit-report.html', text: report
        archiveArtifacts artifacts: 'security-audit-report.html', fingerprint: true
    }

    // Fail build if critical issues found
    if (args.failOnCritical && (summary.critical as int) > 0) {
        error "Security audit failed: ${summary.critical} critical issues found"
    }

    return [
        findings: findings,
        summary: summary
    ]
}

@NonCPS
private Map runSecurityAuditNonCps() {
    def jenkins = Jenkins.get()
    def auditor = new JenkinsSecurityAuditor(jenkins)
    Map<String, List<Map<String, String>>> findings = auditor.performSecurityAudit()

    int criticalCount = 0
    int warningCount = 0
    int infoCount = 0

    findings.each { category, categoryFindings ->
        categoryFindings.each { finding ->
            switch (finding.severity) {
                case 'CRITICAL':
                    criticalCount++
                    break
                case 'HIGH':
                case 'WARNING':
                    warningCount++
                    break
                default:
                    infoCount++
            }
        }
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
        html.append("<h2>${JenkinsSecurityAuditor.escapeHtml(category?.toString())}</h2>")
        categoryFindings.each { finding ->
            String severity = finding.severity?.toString() ?: 'INFO'
            String cssClass = JenkinsSecurityAuditor.escapeHtml(severity.toLowerCase())
            String escapedSeverity = JenkinsSecurityAuditor.escapeHtml(severity)
            String escapedDesc = JenkinsSecurityAuditor.escapeHtml(finding.description?.toString())
            String escapedRec = finding.recommendation
                ? "<br><em>Recommendation:</em> ${JenkinsSecurityAuditor.escapeHtml(finding.recommendation?.toString())}"
                : ''
            html.append("""
    <div class="finding ${cssClass}">
        <strong class="${cssClass}">${escapedSeverity}:</strong> ${escapedDesc}
        ${escapedRec}
    </div>
""")
        }
    }

    html.append("</body></html>")
    return html.toString()
}

private void sendSecurityReport(Map findings, String recipient) {
    def subject = "Jenkins Security Audit Report - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = generateEmailBody(findings, env.BUILD_URL as String)

    emailext(
        to: recipient,
        subject: subject,
        body: body,
        mimeType: 'text/html'
    )
}

@NonCPS
private String generateEmailBody(Map findings, String buildUrl) {
    def criticalCount = findings.values().flatten().count { it.severity == 'CRITICAL' }
    def warningCount = findings.values().flatten().count { it.severity in ['WARNING', 'HIGH'] }
    String escapedBuildUrl = JenkinsSecurityAuditor.escapeHtml(buildUrl ?: '')

    return """
<h2>Jenkins Security Audit Results</h2>
<p>Build: ${escapedBuildUrl}</p>
<p>Summary: ${criticalCount} critical issues, ${warningCount} warnings</p>
<p>Please review the attached report for details.</p>
"""
}