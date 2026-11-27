#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/**
 * Controller admin script: run a Jenkins security configuration audit.
 *
 * Run from Manage Jenkins -> Script Console or via:
 *   java -jar jenkins-cli.jar -s <url> groovy = < admin-scripts/audit-security.groovy
 */

import com.github.thomasvincent.jenkinsscripts.security.JenkinsSecurityAuditor
import jenkins.model.Jenkins
import java.util.logging.Level
import java.util.logging.Logger

Logger log = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.admin.auditSecurity')

try {
    Jenkins jenkins = Jenkins.get()
    def auditor = new JenkinsSecurityAuditor(jenkins)
    Map<String, List<Map<String, String>>> findings = auditor.performSecurityAudit()

    int critical = 0
    int warning = 0
    int info = 0

    findings.each { String category, List<Map<String, String>> items ->
        println "== ${category} (${items.size()}) =="
        items.each { Map<String, String> item ->
            String severity = item.severity ?: 'INFO'
            switch (severity) {
                case 'CRITICAL':
                    critical++
                    break
                case 'HIGH':
                case 'WARNING':
                    warning++
                    break
                default:
                    info++
            }
            println "  [${severity}] ${item.description}"
            if (item.recommendation) {
                println "    Recommendation: ${item.recommendation}"
            }
        }
    }

    String summary = "Security audit complete: ${critical} critical, ${warning} warnings, ${info} info"
    log.info(summary)
    println summary
} catch (Exception e) {
    log.log(Level.SEVERE, 'Security audit failed', e)
    throw e
}
