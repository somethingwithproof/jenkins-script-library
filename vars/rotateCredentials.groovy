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

import com.github.thomasvincent.jenkinsscripts.credentials.CredentialRotationManager
import com.github.thomasvincent.jenkinsscripts.util.JenkinsLogger
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
    def action = args.action ?: 'analyze'
    def jenkins = Jenkins.get()
    def logger = new JenkinsLogger(currentBuild.rawBuild.getListener(), 'rotateCredentials')
    
    try {
        def manager = new CredentialRotationManager(jenkins)
        
        switch (action.toLowerCase()) {
            case 'analyze':
                logger.info("Analyzing credential health and compliance...")
                def analysis = manager.analyzeCredentials(args.includeUsage ?: true)
                
                // Log summary
                logger.info("Credential analysis summary:")
                logger.info("  Total credentials: ${analysis.summary.totalCredentials}")
                logger.info("  Needing rotation: ${analysis.summary.needingRotation}")
                logger.info("  Compliance status: ${analysis.compliance.overall}")
                
                // Add Pipeline-friendly methods
                analysis.metaClass.isCompliant = { ->
                    analysis.compliance.overall == 'COMPLIANT'
                }
                
                analysis.metaClass.getViolations = { ->
                    def violations = []
                    ['sox', 'hipaa', 'pciDss'].each { standard ->
                        if (!analysis.compliance[standard].compliant) {
                            violations.addAll(analysis.compliance[standard].violations)
                        }
                    }
                    return violations
                }
                
                return analysis
                
            case 'rotate':
                def dryRun = args.dryRun != false  // Default to dry run for safety
                def policy = args.policy ?: [:]
                
                logger.info("${dryRun ? 'Preview' : 'Executing'} credential rotation...")
                def results = manager.rotateCredentials(dryRun, policy)
                
                logger.info("Rotation results:")
                logger.info("  Rotated: ${results.rotated.size()}")
                logger.info("  Failed: ${results.failed.size()}")
                logger.info("  Skipped: ${results.skipped.size()}")
                
                if (!dryRun && results.failed) {
                    logger.warning("Failed to rotate ${results.failed.size()} credentials")
                }
                
                return results
                
            case 'unused':
                def days = args.days ?: 90
                logger.info("Finding credentials unused for ${days} days...")
                
                def unused = manager.findUnusedCredentials(days)
                logger.info("Found ${unused.size()} unused credentials")
                
                return unused
                
            default:
                error "Unknown credential action: ${action}. Valid actions: analyze, rotate, unused"
        }
        
    } catch (Exception e) {
        logger.error("Credential management failed: ${e.message}")
        throw e
    }
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
    def analysis = analyze()
    return [
        compliant: analysis.compliance.overall == 'COMPLIANT',
        violations: analysis.getViolations(),
        summary: analysis.compliance
    ]
}