// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.credentials.CredentialRotationManager
import jenkins.model.Jenkins

/**
 * Pipeline step for managing credential rotation and compliance.
 * 
 * This step provides Jenkins Pipeline integration for credential management,
 * including rotation, compliance checking, and usage analysis.
 * 
 * Usage in Pipeline:
 * 
 * @Library('jenkins-script-library') _
 * 
 * pipeline {
 *     agent any
 *     stages {
 *         stage('Credential Management') {
 *             steps {
 *                 script {
 *                     // Check credential compliance
 *                     def analysis = rotateCredentials.analyze()
 *                     if (analysis.compliance.overall != 'COMPLIANT') {
 *                         error "Credentials are not compliant!"
 *                     }
 *                     
 *                     // Rotate old credentials (dry run)
 *                     def rotation = rotateCredentials(
 *                         action: 'rotate',
 *                         dryRun: true,
 *                         policy: [PASSWORD: 60, API_KEY: 90]
 *                     )
 *                     
 *                     // Find unused credentials
 *                     def unused = rotateCredentials.findUnused(days: 30)
 *                     if (unused) {
 *                         echo "Found ${unused.size()} unused credentials"
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 */
def call(Map args = [:]) {
    String action = (args.action ?: 'analyze') as String

    try {
        switch (action.toLowerCase()) {
            case 'analyze':
                echo "[rotateCredentials] Analyzing credential health and compliance..."
                boolean includeUsage = args.includeUsage != false
                Map analysis = analyzeCredentialsNonCps(includeUsage)

                echo "[rotateCredentials] Credential analysis summary:"
                echo "  Total credentials: ${analysis.summary.totalCredentials}"
                echo "  Needing rotation: ${analysis.summary.needingRotation}"
                echo "  Compliance status: ${analysis.compliance.overall}"

                return analysis

            case 'rotate':
                boolean dryRun = args.dryRun != false
                Map policy = (args.policy ?: [:]) as Map

                echo "[rotateCredentials] ${dryRun ? 'Preview' : 'Executing'} credential rotation..."
                Map results = rotateCredentialsNonCps(dryRun, policy)

                echo "[rotateCredentials] Rotation results:"
                echo "  Rotated: ${results.rotated.size()}"
                echo "  Failed: ${results.failed.size()}"
                echo "  Skipped: ${results.skipped.size()}"

                if (!dryRun && results.failed) {
                    echo "[rotateCredentials] WARNING: Failed to rotate ${results.failed.size()} credentials"
                }

                return results

            case 'unused':
                int days = (args.days ?: 90) as int
                echo "[rotateCredentials] Finding credentials unused for ${days} days..."

                List unused = findUnusedCredentialsNonCps(days)
                echo "[rotateCredentials] Found ${unused.size()} unused credentials"

                return unused

            default:
                error "Unknown credential action: ${action}. Valid actions: analyze, rotate, unused"
        }

    } catch (Exception e) {
        echo "[rotateCredentials] ERROR: Credential management failed: ${e.message}"
        throw e
    }
}

@NonCPS
private Map analyzeCredentialsNonCps(boolean includeUsage) {
    def manager = new CredentialRotationManager(Jenkins.get())
    return manager.analyzeCredentials(includeUsage)
}

@NonCPS
private Map rotateCredentialsNonCps(boolean dryRun, Map policy) {
    def manager = new CredentialRotationManager(Jenkins.get())
    return manager.rotateCredentials(dryRun, policy)
}

@NonCPS
private List findUnusedCredentialsNonCps(int days) {
    def manager = new CredentialRotationManager(Jenkins.get())
    return manager.findUnusedCredentials(days)
}

/**
 * Analyze credentials (default action).
 */
def analyze(Map args = [:]) {
    return call(args + [action: 'analyze'])
}

/**
 * Find unused credentials.
 */
def findUnused(Map args = [:]) {
    return call(args + [action: 'unused'])
}

/**
 * Rotate credentials.
 */
def rotate(Map args = [:]) {
    return call(args + [action: 'rotate'])
}

/**
 * Check compliance status.
 */
def checkCompliance() {
    Map analysis = analyze()
    List violations = extractComplianceViolations(analysis)
    return [
        compliant: analysis.compliance.overall == 'COMPLIANT',
        violations: violations,
        summary: analysis.compliance
    ]
}

@NonCPS
private List extractComplianceViolations(Map analysis) {
    List violations = []
    ['sox', 'hipaa', 'pciDss'].each { standard ->
        def standardReport = analysis.compliance?.get(standard)
        if (standardReport && !standardReport.compliant && standardReport.violations) {
            violations.addAll(standardReport.violations)
        }
    }
    return violations
}