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
import hudson.security.AuthorizationStrategy
import hudson.security.SecurityRealm
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.model.User
import hudson.model.Item
import jenkins.security.ApiTokenProperty
import org.acegisecurity.userdetails.UserDetails

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.junit.Test
import static org.junit.Assert.*

/**
 * Integration tests for JenkinsSecurityAuditor using a real Jenkins instance.
 * 
 * <p>These tests verify that the JenkinsSecurityAuditor class works correctly with an actual
 * Jenkins instance rather than mocks. The tests use JenkinsRule to create a temporary
 * Jenkins instance for testing purposes.</p>
 * 
 * <p>The tests verify:
 * <ul>
 *   <li>Auditing user accounts</li>
 *   <li>Auditing global security settings</li>
 *   <li>Auditing API tokens</li>
 * </ul>
 * </p>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JenkinsSecurityAuditorIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    @Test
    void testAuditSecurityConfigInRealJenkins() {
        // Create JenkinsSecurityAuditor
        Jenkins jenkins = jenkinsRule.jenkins
        JenkinsSecurityAuditor auditor = new JenkinsSecurityAuditor(jenkins)
        
        // Get security config
        Map<String, Object> securityConfig = auditor.auditSecurityConfig()
        
        // Verify result - should have basic security information
        assertNotNull(securityConfig)
        assertNotNull(securityConfig.securityEnabled)
        assertNotNull(securityConfig.authorizationStrategy)
        assertNotNull(securityConfig.securityRealm)
    }
    
    @Test
    void testAuditUsersInRealJenkins() {
        // Setup security realm to allow user creation
        Jenkins jenkins = jenkinsRule.jenkins
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(false, false, null)
        jenkins.setSecurityRealm(realm)
        
        // Create a test user
        User user = realm.createAccount("test-user", "password123")
        
        // Create JenkinsSecurityAuditor
        JenkinsSecurityAuditor auditor = new JenkinsSecurityAuditor(jenkins)
        
        // Audit users
        List<Map<String, Object>> users = auditor.auditUsers()
        
        // Verify result - should find at least our test user
        assertNotNull(users)
        assertTrue(users.size() >= 1)
        
        // Check that our test user is included
        boolean hasTestUser = users.any { it.id == "test-user" }
        assertTrue(hasTestUser)
    }
    
    @Test
    void testAuditApiTokensInRealJenkins() {
        // Setup security realm to allow user creation
        Jenkins jenkins = jenkinsRule.jenkins
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(false, false, null)
        jenkins.setSecurityRealm(realm)
        
        // Create a test user
        User user = realm.createAccount("token-test-user", "password123")
        
        // Create JenkinsSecurityAuditor
        JenkinsSecurityAuditor auditor = new JenkinsSecurityAuditor(jenkins)
        
        // Audit API tokens
        Map<String, Object> tokenAudit = auditor.auditApiTokens()
        
        // Verify result
        assertNotNull(tokenAudit)
        assertNotNull(tokenAudit.usersWithTokens)
        assertNotNull(tokenAudit.totalTokenCount)
    }
    
    @Test
    void testAuditPermissionsInRealJenkins() {
        // Setup security realm and authorization strategy
        Jenkins jenkins = jenkinsRule.jenkins
        HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(false, false, null)
        jenkins.setSecurityRealm(realm)
        
        GlobalMatrixAuthorizationStrategy strategy = new GlobalMatrixAuthorizationStrategy()
        strategy.add(Jenkins.ADMINISTER, "admin")
        strategy.add(Item.READ, "user")
        jenkins.setAuthorizationStrategy(strategy)
        
        // Create test users
        User adminUser = realm.createAccount("admin", "password123")
        User regularUser = realm.createAccount("user", "password123")
        
        // Create JenkinsSecurityAuditor
        JenkinsSecurityAuditor auditor = new JenkinsSecurityAuditor(jenkins)
        
        // Audit permissions
        Map<String, Object> permissionAudit = auditor.auditPermissions()
        
        // Verify result
        assertNotNull(permissionAudit)
        assertNotNull(permissionAudit.adminUsers)
        assertTrue(permissionAudit.adminUsers.contains("admin"))
    }
}