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

package com.github.thomasvincent.jenkinsscripts.security

import jenkins.model.Jenkins
import hudson.security.Permission
import hudson.model.User
import hudson.security.ACL
import hudson.security.AuthorizationStrategy
import hudson.plugins.warnings.PluginsUtils

import java.util.logging.Logger

/**
 * Audits Jenkins security settings and identifies vulnerabilities.
 * 
 * Performs security checks on authentication, authorization, user permissions,
 * plugin vulnerabilities, and other security configurations.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JenkinsSecurityAuditor {

    private static final Logger LOGGER = Logger.getLogger(JenkinsSecurityAuditor.class.getName())
    
    private final Jenkins jenkins
    private List<SecurityFinding> findings = []
    
    // Security finding categories
    enum FindingCategory {
        AUTHENTICATION,
        AUTHORIZATION,
        CSRF,
        PLUGIN,
        SCRIPT_SECURITY,
        CONFIGURATION,
        NETWORK,
        CREDENTIALS
    }
    
    // Security finding severity
    enum FindingSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }
    
    /**
     * Creates a security auditor for the given Jenkins instance.
     * 
     * ```groovy
     * def auditor = new JenkinsSecurityAuditor(Jenkins.get())
     * ```
     */
    JenkinsSecurityAuditor(Jenkins jenkins) {
        this.jenkins = jenkins
    }
    
    /**
     * Runs full security audit on all Jenkins security aspects.
     * 
     * ```groovy
     * def findings = auditor.runFullAudit()
     * findings.each { finding ->
     *     println "${finding.severity}: ${finding.title}"
     * }
     * ```
     */
    List<SecurityFinding> runFullAudit() {
        findings.clear()
        
        // Check authentication settings
        checkAuthenticationSettings()
        
        // Check authorization settings
        checkAuthorizationSettings()
        
        // Check user permissions
        checkUserPermissions()
        
        // Check CSRF protection
        checkCsrfProtection()
        
        // Check plugin security
        checkPluginSecurity()
        
        // Check script security
        checkScriptSecurity()
        
        // Check agent security
        checkAgentSecurity()
        
        // Check credential security
        checkCredentialSecurity()
        
        return findings
    }
    
    /**
     * Checks authentication settings for security issues.
     * 
     * ```groovy
     * auditor.checkAuthenticationSettings()
     * ```
     */
    void checkAuthenticationSettings() {
        def securityRealm = jenkins.getSecurityRealm()
        
        // Check if security is enabled
        if (securityRealm.getClass().getName().contains("HudsonPrivateSecurityRealm")) {
            // Jenkins' own user database is being used, which is fine
            if (User.getAll().size() < 2) {
                addFinding(
                    FindingCategory.AUTHENTICATION,
                    FindingSeverity.MEDIUM,
                    "Limited User Accounts",
                    "Only a single user account detected. Consider creating additional accounts for accountability."
                )
            }
        } else if (securityRealm.getClass().getName().contains("None")) {
            addFinding(
                FindingCategory.AUTHENTICATION,
                FindingSeverity.CRITICAL,
                "Security Disabled",
                "Jenkins security is disabled. Anyone can access and modify Jenkins without authentication."
            )
        }
        
        // Check if insecure protocols are allowed
        if (jenkins.isUseSecurity() && !jenkins.isSlaveAgentPortEnforced()) {
            addFinding(
                FindingCategory.AUTHENTICATION,
                FindingSeverity.HIGH,
                "JNLP Port Not Fixed",
                "JNLP port for agent connections is not fixed. This could allow unauthorized agents to connect."
            )
        }
    }
    
    /**
     * Checks authorization settings for security issues.
     * 
     * ```groovy
     * auditor.checkAuthorizationSettings()
     * ```
     */
    void checkAuthorizationSettings() {
        def authStrategy = jenkins.getAuthorizationStrategy()
        
        // Check for unsecure authorization strategies
        if (authStrategy instanceof AuthorizationStrategy.Unsecured) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.CRITICAL,
                "Unsecured Authorization",
                "Jenkins is using the Unsecured authorization strategy. Anyone can do anything."
            )
        } else if (authStrategy.getClass().getName().contains("FullControlOnceLoggedInAuthorizationStrategy")) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.HIGH,
                "Logged-in Users Have Full Access",
                "Any logged-in user has full administrator access."
            )
        } else if (authStrategy.getClass().getName().contains("LegacyAuthorizationStrategy")) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.HIGH,
                "Legacy Authorization Strategy",
                "Using legacy authorization strategy which grants unnecessary permissions."
            )
        }
    }
    
    /**
     * Checks user permissions for security issues.
     * 
     * ```groovy
     * auditor.checkUserPermissions()
     * def adminCount = findings.count { it.title.contains("Admin") }
     * ```
     */
    void checkUserPermissions() {
        // Look for anonymous permissions
        def strategy = jenkins.getAuthorizationStrategy()
        if (strategy.getACL(jenkins).hasPermission(ACL.ANONYMOUS, Permission.ADMINISTER)) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.CRITICAL,
                "Anonymous Admin Access",
                "Anonymous users have administrator permissions."
            )
        } else if (strategy.getACL(jenkins).hasPermission(ACL.ANONYMOUS, Permission.READ)) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.MEDIUM,
                "Anonymous Read Access",
                "Anonymous users have read access to Jenkins. Consider restricting to authenticated users only."
            )
        }
        
        // Check admin accounts
        int adminCount = 0
        User.getAll().each { user ->
            if (strategy.getACL(jenkins).hasPermission(user.impersonate(), Permission.ADMINISTER)) {
                adminCount++
            }
        }
        
        if (adminCount == 0) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.HIGH,
                "No Admin Users",
                "No administrative users found. Jenkins administration may be inaccessible."
            )
        } else if (adminCount > 5) {
            addFinding(
                FindingCategory.AUTHORIZATION,
                FindingSeverity.LOW,
                "Excessive Admin Users",
                "Found ${adminCount} admin users. Consider reducing the number of administrative accounts."
            )
        }
    }
    
    /**
     * Checks CSRF protection settings.
     * 
     * ```groovy
     * auditor.checkCsrfProtection()
     * ```
     */
    void checkCsrfProtection() {
        if (!jenkins.getCrumbIssuer()) {
            addFinding(
                FindingCategory.CSRF,
                FindingSeverity.HIGH,
                "CSRF Protection Disabled",
                "Cross-Site Request Forgery (CSRF) protection is disabled."
            )
        }
    }
    
    /**
     * Checks plugin security for outdated or vulnerable plugins.
     * 
     * ```groovy
     * auditor.checkPluginSecurity()
     * def securityIssues = findings.findAll { it.category == FindingCategory.PLUGIN }
     * ```
     */
    void checkPluginSecurity() {
        // Check for outdated plugins (would typically use the UpdateCenter)
        try {
            def updateCenter = jenkins.getUpdateCenter()
            def plugins = jenkins.getPluginManager().getPlugins()
            
            plugins.each { plugin ->
                def pluginInfo = updateCenter.getPlugin(plugin.shortName)
                if (pluginInfo && pluginInfo.hasUpdates()) {
                    def severity = FindingSeverity.MEDIUM
                    
                    // Check if this is a security update
                    if (pluginInfo.hasSecurityUpdates()) {
                        severity = FindingSeverity.HIGH
                    }
                    
                    addFinding(
                        FindingCategory.PLUGIN,
                        severity,
                        "Outdated Plugin: ${plugin.displayName}",
                        "Plugin ${plugin.displayName} is outdated (${plugin.version} vs ${pluginInfo.version})" + 
                        (pluginInfo.hasSecurityUpdates() ? " and has security vulnerabilities." : ".")
                    )
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to check plugin updates: ${e.message}")
        }
    }
    
    /**
     * Checks Groovy script security settings.
     * 
     * ```groovy
     * auditor.checkScriptSecurity()
     * def dangerousApprovals = findings.findAll { 
     *     it.category == FindingCategory.SCRIPT_SECURITY && 
     *     it.severity == FindingSeverity.HIGH 
     * }
     * ```
     */
    void checkScriptSecurity() {
        // Check if Script Security Plugin is installed
        if (!jenkins.getPluginManager().getPlugin("script-security")) {
            addFinding(
                FindingCategory.SCRIPT_SECURITY,
                FindingSeverity.HIGH,
                "Script Security Plugin Missing",
                "Script Security Plugin is not installed, which is crucial for securing Groovy scripts."
            )
        }
        
        // Check if Groovy Script approval is in place (requires Script Security Plugin)
        try {
            def scriptApproval = Class.forName("org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval").getInstance()
            def approvedSignatures = scriptApproval.getApprovedSignatures()
            
            // Check for potentially dangerous approvals
            def dangerousPatterns = [
                "java.lang.Runtime.exec",
                "java.lang.ProcessBuilder",
                "jenkins.model.Jenkins.getInstance",
                "hudson.model.Hudson.getInstance",
                "java.io.File.delete",
                "java.net.URLClassLoader"
            ]
            
            dangerousPatterns.each { pattern ->
                if (approvedSignatures.any { it.contains(pattern) }) {
                    addFinding(
                        FindingCategory.SCRIPT_SECURITY,
                        FindingSeverity.HIGH,
                        "Dangerous Script Approval",
                        "Potentially dangerous script signature approved: ${pattern}"
                    )
                }
            }
            
            // Warn about excessive approvals
            if (approvedSignatures.size() > 50) {
                addFinding(
                    FindingCategory.SCRIPT_SECURITY,
                    FindingSeverity.MEDIUM,
                    "Excessive Script Approvals",
                    "${approvedSignatures.size()} script signatures approved. Consider reviewing the list."
                )
            }
        } catch (Exception e) {
            // Script Security plugin not installed or not accessible
            LOGGER.fine("Could not check script approvals: ${e.message}")
        }
    }
    
    /**
     * Checks agent security configurations.
     * 
     * ```groovy
     * auditor.checkAgentSecurity()
     * def insecureProtocols = findings.findAll { 
     *     it.title == "Insecure Agent Protocol" 
     * }
     * ```
     */
    void checkAgentSecurity() {
        // Check agent protocols
        def agentProtocols = jenkins.getAgentProtocols()
        def insecureProtocols = ["JNLP-connect", "JNLP2-connect"]
        
        insecureProtocols.each { protocol ->
            if (agentProtocols.contains(protocol)) {
                addFinding(
                    FindingCategory.NETWORK,
                    FindingSeverity.HIGH,
                    "Insecure Agent Protocol",
                    "Insecure agent protocol enabled: ${protocol}. Consider using JNLP4 instead."
                )
            }
        }
        
        // Check if agent port is open without security
        if (jenkins.getSlaveAgentPort() > 0 && !jenkins.isSlaveAgentPortEnforced()) {
            addFinding(
                FindingCategory.NETWORK,
                FindingSeverity.MEDIUM,
                "Agent Port Configuration",
                "Agent port (${jenkins.getSlaveAgentPort()}) is open but not enforced. " +
                "Consider using a specific port or disabling it if not needed."
            )
        }
    }
    
    /**
     * Checks credential storage and security.
     * 
     * ```groovy
     * auditor.checkCredentialSecurity()
     * ```
     */
    void checkCredentialSecurity() {
        // Check for Credentials plugin
        if (!jenkins.getPluginManager().getPlugin("credentials")) {
            addFinding(
                FindingCategory.CREDENTIALS,
                FindingSeverity.MEDIUM,
                "Credentials Plugin Missing",
                "Credentials Plugin is not installed, which is recommended for securely storing secrets."
            )
            return
        }
        
        // Check for credentials being directly exposed in job configurations
        // This requires parsing job XML files which is beyond this example
    }
    
    /**
     * Adds a security finding to the audit results.
     * 
     * ```groovy
     * addFinding(
     *     FindingCategory.NETWORK, 
     *     FindingSeverity.HIGH,
     *     "Open Firewall", 
     *     "Jenkins ports exposed to internet"
     * )
     * ```
     */
    private void addFinding(FindingCategory category, FindingSeverity severity, String title, String description) {
        findings.add(new SecurityFinding(category, severity, title, description))
    }
    
    /**
     * Generates an HTML security report from findings.
     * 
     * Uses Groovy's multiline strings and string interpolation.
     * 
     * ```groovy
     * def reportHtml = auditor.generateHtmlReport()
     * new File("/var/www/security-report.html").text = reportHtml
     * ```
     */
    String generateHtmlReport() {
        def reportBuilder = new StringBuilder()
        
        // Group findings by severity using Groovy collection methods
        def criticalFindings = findings.findAll { it.severity == FindingSeverity.CRITICAL }
        def highFindings = findings.findAll { it.severity == FindingSeverity.HIGH }
        def mediumFindings = findings.findAll { it.severity == FindingSeverity.MEDIUM }
        def lowFindings = findings.findAll { it.severity == FindingSeverity.LOW }
        def infoFindings = findings.findAll { it.severity == FindingSeverity.INFO }
        
        // Start HTML
        reportBuilder.append("""
            <html>
            <head>
                <title>Jenkins Security Audit Report</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #333; }
                    h2 { color: #666; margin-top: 30px; }
                    .summary { margin: 20px 0; }
                    .finding { border: 1px solid #ddd; padding: 10px; margin-bottom: 10px; border-radius: 5px; }
                    .critical { border-left: 5px solid #d9534f; }
                    .high { border-left: 5px solid #f0ad4e; }
                    .medium { border-left: 5px solid #5bc0de; }
                    .low { border-left: 5px solid #5cb85c; }
                    .info { border-left: 5px solid #5bc0de; }
                    .finding-title { font-weight: bold; margin-bottom: 5px; }
                    .finding-meta { color: #666; font-size: 0.9em; margin-bottom: 5px; }
                    .finding-desc { margin-top: 5px; }
                </style>
            </head>
            <body>
                <h1>Jenkins Security Audit Report</h1>
                <p>Generated on ${new Date().format("yyyy-MM-dd HH:mm:ss")}</p>
                
                <div class="summary">
                    <h2>Summary</h2>
                    <p>Found ${findings.size()} security issues:</p>
                    <ul>
                        <li>Critical: ${criticalFindings.size()}</li>
                        <li>High: ${highFindings.size()}</li>
                        <li>Medium: ${mediumFindings.size()}</li>
                        <li>Low: ${lowFindings.size()}</li>
                        <li>Info: ${infoFindings.size()}</li>
                    </ul>
                </div>
        """)
        
        // Add critical findings
        if (!criticalFindings.isEmpty()) {
            reportBuilder.append("<h2>Critical Findings</h2>")
            criticalFindings.each { finding ->
                appendFindingHtml(reportBuilder, finding, "critical")
            }
        }
        
        // Add high findings
        if (!highFindings.isEmpty()) {
            reportBuilder.append("<h2>High Findings</h2>")
            highFindings.each { finding ->
                appendFindingHtml(reportBuilder, finding, "high")
            }
        }
        
        // Add medium findings
        if (!mediumFindings.isEmpty()) {
            reportBuilder.append("<h2>Medium Findings</h2>")
            mediumFindings.each { finding ->
                appendFindingHtml(reportBuilder, finding, "medium")
            }
        }
        
        // Add low findings
        if (!lowFindings.isEmpty()) {
            reportBuilder.append("<h2>Low Findings</h2>")
            lowFindings.each { finding ->
                appendFindingHtml(reportBuilder, finding, "low")
            }
        }
        
        // Add info findings
        if (!infoFindings.isEmpty()) {
            reportBuilder.append("<h2>Informational Findings</h2>")
            infoFindings.each { finding ->
                appendFindingHtml(reportBuilder, finding, "info")
            }
        }
        
        // End HTML
        reportBuilder.append("</body></html>")
        
        return reportBuilder.toString()
    }
    
    /**
     * Appends HTML for a security finding to the report.
     */
    private void appendFindingHtml(StringBuilder builder, SecurityFinding finding, String severityClass) {
        builder.append("""
            <div class="finding ${severityClass}">
                <div class="finding-title">${finding.title}</div>
                <div class="finding-meta">Category: ${finding.category}</div>
                <div class="finding-desc">${finding.description}</div>
            </div>
        """)
    }
    
    /**
     * Represents a security finding with category, severity and details.
     */
    class SecurityFinding {
        FindingCategory category
        FindingSeverity severity
        String title
        String description
        Date timestamp = new Date()
        
        SecurityFinding(FindingCategory category, FindingSeverity severity, String title, String description) {
            this.category = category
            this.severity = severity
            this.title = title
            this.description = description
        }
    }
}