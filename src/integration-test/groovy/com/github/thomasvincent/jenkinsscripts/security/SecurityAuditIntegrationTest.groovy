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

import com.github.thomasvincent.jenkinsscripts.DockerIntegrationTest

/**
 * Integration tests for security auditing scripts.
 * 
 * Tests security auditing, vulnerability scanning, and compliance checking.
 */
class SecurityAuditIntegrationTest extends DockerIntegrationTest {
    
    def setup() {
        // Create test users with different permissions
        def createUsersScript = """
import jenkins.model.*
import hudson.security.*
import hudson.model.User

def jenkins = Jenkins.instance

// Create test users
def users = [
    [id: 'test-admin', fullName: 'Test Admin', password: 'admin123'],
    [id: 'test-developer', fullName: 'Test Developer', password: 'dev123'],
    [id: 'test-viewer', fullName: 'Test Viewer', password: 'view123'],
    [id: 'test-inactive', fullName: 'Inactive User', password: 'inactive123']
]

users.each { userInfo ->
    def user = User.getById(userInfo.id, true)
    user.setFullName(userInfo.fullName)
    user.save()
    
    // Set user properties for testing
    if (userInfo.id == 'test-inactive') {
        // Simulate inactive user by setting a property
        user.addProperty(new hudson.security.HudsonPrivateSecurityRealm.Details(
            "Inactive since 2020-01-01"
        ))
    }
}

// Create API tokens for some users
def adminUser = User.getById('test-admin', false)
if (adminUser) {
    // Note: In real Jenkins, API tokens are created differently
    // This is a simplified version for testing
    adminUser.addProperty(new jenkins.security.ApiTokenProperty())
}

println "Test users created successfully"
"""
        httpClient.executeScript(createUsersScript)
        
        // Create jobs with different security configurations
        createTestJob('test-public-job')
        createTestJob('test-restricted-job')
        
        // Set up job-specific permissions
        def setupJobPermissionsScript = """
import jenkins.model.*
import hudson.security.*
import hudson.model.*

def jenkins = Jenkins.instance
def restrictedJob = jenkins.getItem('test-restricted-job')

if (restrictedJob) {
    // Add job-specific security settings
    restrictedJob.addProperty(new hudson.security.AuthorizationMatrixProperty([
        (Permission.fromId('hudson.model.Item.Read')): ['test-admin', 'test-developer'],
        (Permission.fromId('hudson.model.Item.Build')): ['test-admin'],
        (Permission.fromId('hudson.model.Item.Configure')): ['test-admin']
    ]))
    restrictedJob.save()
    println "Security permissions set for test-restricted-job"
}
"""
        httpClient.executeScript(setupJobPermissionsScript)
    }
    
    def "test AuditJenkinsSecurity script performs comprehensive audit"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/AuditJenkinsSecurity.groovy')
        
        then:
        result != null
        result.contains('Security Audit Report') || result.contains('Jenkins Security Audit')
        result.contains('test-admin') || result.contains('Users')
        result.contains('Permissions') || result.contains('Authorization')
    }
    
    def "test SecurityVulnerabilityScan script"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/SecurityVulnerabilityScan.groovy')
        
        then:
        result != null
        result.contains('Vulnerability Scan') || result.contains('Security Scan')
        result.contains('Plugin') || result.contains('Version')
    }
    
    def "test AuditJobConfigurations for security issues"() {
        given:
        // Create a job with potential security issues
        def insecureJobConfig = '''<?xml version='1.1' encoding='UTF-8'?>
<project>
  <description>Job with security issues for testing</description>
  <builders>
    <hudson.tasks.Shell>
      <command>
# Potential security issues
echo $PASSWORD
curl http://example.com/api?key=secret123
chmod 777 /tmp/test
      </command>
    </hudson.tasks.Shell>
  </builders>
  <properties>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.PasswordParameterDefinition>
          <name>PASSWORD</name>
          <description>Sensitive parameter</description>
          <defaultValue>default123</defaultValue>
        </hudson.model.PasswordParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
</project>'''
        createTestJob('test-insecure-job', insecureJobConfig)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/AuditJobConfigurations.groovy', [
            includeSecurityChecks: true
        ])
        
        then:
        result != null
        result.contains('test-insecure-job') || result.contains('security')
        result.contains('Configuration Audit') || result.contains('Job Audit')
        
        cleanup:
        deleteTestJob('test-insecure-job')
    }
    
    def "test user permission analysis"() {
        when:
        def script = """
import com.github.thomasvincent.jenkinsscripts.security.JenkinsSecurityAuditor

def auditor = new JenkinsSecurityAuditor()
def report = auditor.auditUserPermissions()

println "=== User Permission Analysis ==="
report.each { username, permissions ->
    println "\\nUser: \${username}"
    println "Permissions: \${permissions.size()}"
    permissions.each { perm ->
        println "  - \${perm}"
    }
}
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains('User Permission Analysis')
        result.content.contains('test-admin')
        result.content.contains('Permissions:')
    }
    
    def "test inactive user detection"() {
        when:
        def script = """
import jenkins.model.*
import hudson.model.*
import java.time.*

def jenkins = Jenkins.instance
def cutoffDate = LocalDate.now().minusDays(30)

println "=== Inactive User Report ==="
println "Users inactive for more than 30 days:"

User.getAll().each { user ->
    // Check last login or activity
    def lastActivity = user.getProperty(hudson.security.LastGrantedAuthoritiesProperty.class)
    if (!lastActivity || user.fullName.contains('Inactive')) {
        println "- \${user.id} (\${user.fullName})"
    }
}
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains('Inactive User Report')
        result.content.contains('test-inactive')
    }
    
    def "test API token audit"() {
        when:
        def script = """
import jenkins.model.*
import hudson.model.*
import jenkins.security.*

println "=== API Token Audit ==="

User.getAll().each { user ->
    def tokenProperty = user.getProperty(ApiTokenProperty.class)
    if (tokenProperty && tokenProperty.hasLegacyToken()) {
        println "User \${user.id} has legacy API token - should be rotated"
    }
}

println "\\nAPI token audit completed"
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains('API Token Audit')
    }
    
    def "test plugin security analysis"() {
        when:
        def script = """
import jenkins.model.*
import hudson.PluginWrapper

def jenkins = Jenkins.instance
def pluginManager = jenkins.pluginManager

println "=== Plugin Security Analysis ==="
println "\\nInstalled plugins with potential security concerns:"

pluginManager.plugins.each { plugin ->
    def warnings = []
    
    // Check for old plugins
    if (plugin.version =~ /^[01]\\./) {
        warnings << "Very old version"
    }
    
    // Check for plugins with known security issues (mock check)
    def securityPlugins = ['script-security', 'matrix-auth', 'credentials']
    if (plugin.shortName in securityPlugins) {
        warnings << "Security-critical plugin"
    }
    
    if (warnings) {
        println "\\n- \${plugin.displayName} (\${plugin.version})"
        warnings.each { println "  ! \${it}" }
    }
}

println "\\nPlugin security analysis completed"
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains('Plugin Security Analysis')
        result.content.contains('plugin')
    }
    
    def "test security configuration recommendations"() {
        when:
        def script = """
import jenkins.model.*
import hudson.security.*

def jenkins = Jenkins.instance

println "=== Security Configuration Recommendations ==="

def recommendations = []

// Check CSRF protection
if (!jenkins.crumbIssuer) {
    recommendations << "Enable CSRF protection"
}

// Check security realm
if (jenkins.securityRealm instanceof hudson.security.SecurityRealm.None) {
    recommendations << "Configure authentication (security realm)"
}

// Check authorization strategy
if (jenkins.authorizationStrategy instanceof AuthorizationStrategy.Unsecured) {
    recommendations << "Configure authorization strategy"
}

// Check agent protocols
def protocols = jenkins.agentProtocols
if (protocols.contains("JNLP-connect") || protocols.contains("CLI-connect")) {
    recommendations << "Disable deprecated agent protocols"
}

// Check for script approval
def scriptApproval = org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval.get()
if (scriptApproval.pendingScripts.size() > 0) {
    recommendations << "Review \${scriptApproval.pendingScripts.size()} pending script approvals"
}

if (recommendations) {
    println "\\nRecommendations:"
    recommendations.each { println "- \${it}" }
} else {
    println "\\nNo critical security recommendations at this time."
}
"""
        def result = httpClient.executeScript(script)
        
        then:
        result.status == 200
        result.content.contains('Security Configuration Recommendations')
    }
    
    def cleanup() {
        // Clean up test users
        def cleanupScript = """
import hudson.model.User

['test-admin', 'test-developer', 'test-viewer', 'test-inactive'].each { userId ->
    try {
        def user = User.getById(userId, false)
        if (user) {
            user.delete()
        }
    } catch (Exception e) {
        // Ignore errors during cleanup
    }
}
"""
        httpClient.executeScript(cleanupScript)
        
        // Call parent cleanup
        super.cleanup()
    }
}