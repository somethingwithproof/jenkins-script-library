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

import com.github.thomasvincent.jenkinsscripts.credentials.CredentialRotationManager
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import jenkins.model.Jenkins
import groovy.json.JsonBuilder
import java.util.logging.Logger

/**
 * Jenkins CLI script for comprehensive credential management including rotation, compliance, and usage tracking.
 *
 * This script provides advanced credential management capabilities:
 * - Analyze credential health and compliance
 * - Rotate credentials based on age policies
 * - Track credential usage across jobs
 * - Identify unused credentials
 * - Generate compliance reports (SOX, HIPAA, PCI-DSS)
 *
 * Usage:
 *   jenkins-cli groovy = < ManageCredentials.groovy <command> [options]
 *
 * Commands:
 *   analyze      - Analyze all credentials and generate report
 *   rotate       - Rotate credentials based on policy
 *   unused       - Find unused credentials
 *   compliance   - Generate compliance report
 *   help         - Show this help message
 *
 * Examples:
 *   jenkins-cli groovy = < ManageCredentials.groovy analyze
 *   jenkins-cli groovy = < ManageCredentials.groovy rotate --dry-run
 *   jenkins-cli groovy = < ManageCredentials.groovy unused --days 60
 *   jenkins-cli groovy = < ManageCredentials.groovy compliance --format json
 *
 * @author Thomas Vincent
 * @since 1.4.0
 */

// Initialize logger
Logger logger = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.ManageCredentials')

// Parse command line arguments
def cli = args as List
if (cli.isEmpty() || cli[0] in ['-h', '--help', 'help']) {
    showHelp(logger)
    return
}

String command = cli[0]
def options = parseOptions(cli.drop(1))

// Main execution
ErrorHandler.withErrorHandling('manage credentials', logger) {
    def jenkins = Jenkins.get()
    def manager = new CredentialRotationManager(jenkins)
    
    switch (command.toLowerCase()) {
        case 'analyze':
            handleAnalyze(manager, options, logger)
            break
            
        case 'rotate':
            handleRotate(manager, options, logger)
            break
            
        case 'unused':
            handleUnused(manager, options, logger)
            break
            
        case 'compliance':
            handleCompliance(manager, options, logger)
            break
            
        default:
            logger.severe("Unknown command: ${command}")
            showHelp(logger)
            System.exit(1)
    }
}

/**
 * Shows help message.
 */
void showHelp(Logger logger) {
    logger.info """
Credential Management Tool
=========================

Comprehensive credential management for Jenkins including rotation, compliance, and usage tracking.

Usage: jenkins-cli groovy = < ManageCredentials.groovy <command> [options]

Commands:
  analyze      Analyze all credentials and generate comprehensive report
  rotate       Rotate credentials based on age policies
  unused       Find credentials that haven't been used recently
  compliance   Generate compliance report for various standards
  help         Show this help message

Options:
  --format <format>     Output format: text, json, summary (default: summary)
  --dry-run            Preview changes without making them (for rotate command)
  --days <number>      Number of days for unused credential detection (default: 90)
  --policy <json>      Custom rotation policy as JSON (for rotate command)
  --include-usage      Include detailed usage analysis (for analyze command)

Examples:
  Analyze all credentials with usage tracking:
    jenkins-cli groovy = < ManageCredentials.groovy analyze --include-usage
  
  Preview credential rotation:
    jenkins-cli groovy = < ManageCredentials.groovy rotate --dry-run
  
  Find credentials unused for 60 days:
    jenkins-cli groovy = < ManageCredentials.groovy unused --days 60
  
  Generate JSON compliance report:
    jenkins-cli groovy = < ManageCredentials.groovy compliance --format json
  
  Rotate with custom policy:
    jenkins-cli groovy = < ManageCredentials.groovy rotate --policy '{"PASSWORD":60,"API_KEY":120}'

Security Notes:
- This script requires admin privileges
- Rotation generates cryptographically secure credentials
- All operations are logged for audit purposes
- Compliance checks cover SOX, HIPAA, and PCI-DSS standards
"""
}

/**
 * Parses command line options.
 */
Map parseOptions(List<String> args) {
    def options = [
        format: 'summary',
        dryRun: false,
        days: 90,
        includeUsage: false,
        policy: [:]
    ]
    
    def iterator = args.iterator()
    while (iterator.hasNext()) {
        def arg = iterator.next()
        switch (arg) {
            case '--format':
                if (iterator.hasNext()) {
                    options.format = iterator.next()
                }
                break
            case '--dry-run':
                options.dryRun = true
                break
            case '--days':
                if (iterator.hasNext()) {
                    options.days = Integer.parseInt(iterator.next())
                }
                break
            case '--include-usage':
                options.includeUsage = true
                break
            case '--policy':
                if (iterator.hasNext()) {
                    options.policy = parseJsonPolicy(iterator.next())
                }
                break
        }
    }
    
    return options
}

/**
 * Parses JSON policy string.
 */
Map parseJsonPolicy(String json) {
    try {
        return new groovy.json.JsonSlurper().parseText(json) as Map
    } catch (Exception e) {
        return [:]
    }
}

/**
 * Handles analyze command.
 */
void handleAnalyze(CredentialRotationManager manager, Map options, Logger logger) {
    logger.info("Analyzing credentials...")
    
    def report = manager.analyzeCredentials(options.includeUsage)
    
    switch (options.format) {
        case 'json':
            outputJson(report, logger)
            break
        case 'text':
            outputDetailedAnalysis(report, logger)
            break
        case 'summary':
        default:
            outputAnalysisSummary(report, logger)
            break
    }
}

/**
 * Handles rotate command.
 */
void handleRotate(CredentialRotationManager manager, Map options, Logger logger) {
    logger.info("Processing credential rotation...")
    
    def results = manager.rotateCredentials(options.dryRun, options.policy as Map)
    
    switch (options.format) {
        case 'json':
            outputJson(results, logger)
            break
        default:
            outputRotationResults(results, logger)
            break
    }
}

/**
 * Handles unused command.
 */
void handleUnused(CredentialRotationManager manager, Map options, Logger logger) {
    logger.info("Finding unused credentials...")
    
    def unused = manager.findUnusedCredentials(options.days as Integer)
    
    switch (options.format) {
        case 'json':
            outputJson([credentials: unused, days: options.days], logger)
            break
        default:
            outputUnusedCredentials(unused, options.days as Integer, logger)
            break
    }
}

/**
 * Handles compliance command.
 */
void handleCompliance(CredentialRotationManager manager, Map options, Logger logger) {
    logger.info("Generating compliance report...")
    
    def report = manager.analyzeCredentials(true)
    
    switch (options.format) {
        case 'json':
            outputJson(report.compliance, logger)
            break
        default:
            outputComplianceReport(report, logger)
            break
    }
}

/**
 * Outputs JSON format.
 */
void outputJson(Object data, Logger logger) {
    def json = new JsonBuilder(data)
    logger.info(json.toPrettyString())
}

/**
 * Outputs analysis summary.
 */
void outputAnalysisSummary(Map report, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("CREDENTIAL ANALYSIS SUMMARY")
    logger.info("="*80)
    
    def summary = report.summary as Map
    logger.info("\nOVERVIEW:")
    logger.info("  Total Credentials: ${summary.totalCredentials}")
    logger.info("  Needing Rotation: ${summary.needingRotation}")
    logger.info("  Unused Credentials: ${summary.unusedCredentials}")
    logger.info("  Weak Credentials: ${summary.weakCredentials}")
    logger.info("  Average Age: ${summary.averageAge.round()} days")
    
    def compliance = report.compliance as Map
    logger.info("\nCOMPLIANCE STATUS: ${compliance.overall}")
    if (compliance.overall != 'COMPLIANT') {
        logger.info("  SOX: ${compliance.sox.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
        logger.info("  HIPAA: ${compliance.hipaa.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
        logger.info("  PCI-DSS: ${compliance.pciDss.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
    }
    
    def recommendations = report.recommendations as List
    if (recommendations) {
        logger.info("\nTOP RECOMMENDATIONS:")
        recommendations.take(3).each { rec ->
            logger.info("\n  [${rec.priority}] ${rec.type}")
            logger.info("  ${rec.message}")
        }
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs detailed analysis.
 */
void outputDetailedAnalysis(Map report, Logger logger) {
    outputAnalysisSummary(report, logger)
    
    logger.info("\nCREDENTIAL DETAILS:")
    logger.info("="*80)
    
    def credentials = report.credentials as List
    credentials.each { cred ->
        logger.info("\nCredential: ${cred.id}")
        logger.info("  Type: ${cred.type}")
        logger.info("  Description: ${cred.description}")
        logger.info("  Age: ${cred.daysSinceRotation} days")
        logger.info("  Strength: ${cred.strength}")
        logger.info("  Scope: ${cred.scope}")
        
        if (cred.usage) {
            logger.info("  Usage:")
            logger.info("    Total Uses: ${cred.usage.totalUses}")
            logger.info("    Last Used: ${cred.usage.lastUsed ?: 'Never'}")
            logger.info("    Used In: ${cred.usage.jobs.size()} jobs")
        }
    }
}

/**
 * Outputs rotation results.
 */
void outputRotationResults(Map results, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("CREDENTIAL ROTATION RESULTS")
    logger.info("="*80)
    
    logger.info("\nMode: ${results.dryRun ? 'DRY RUN (Preview Only)' : 'LIVE EXECUTION'}")
    logger.info("Timestamp: ${results.timestamp}")
    
    def rotated = results.rotated as List
    if (rotated) {
        logger.info("\nROTATED (${rotated.size()}):")
        rotated.each { cred ->
            logger.info("  - ${cred.id} (${cred.type})")
            logger.info("    Reason: ${cred.reason ?: 'Policy-based rotation'}")
        }
    }
    
    def failed = results.failed as List
    if (failed) {
        logger.info("\nFAILED (${failed.size()}):")
        failed.each { cred ->
            logger.info("  - ${cred.id}: ${cred.reason}")
        }
    }
    
    logger.info("\nSUMMARY:")
    logger.info("  Rotated: ${rotated.size()}")
    logger.info("  Failed: ${failed.size()}")
    logger.info("  Skipped: ${results.skipped.size()}")
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs unused credentials.
 */
void outputUnusedCredentials(List unused, int days, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("UNUSED CREDENTIALS REPORT")
    logger.info("="*80)
    
    logger.info("\nCredentials unused for ${days} days or more:")
    
    if (unused.isEmpty()) {
        logger.info("\nNo unused credentials found.")
    } else {
        logger.info("\nFound ${unused.size()} unused credential(s):")
        
        unused.each { cred ->
            logger.info("\n  ${cred.id} (${cred.type})")
            logger.info("    Description: ${cred.description}")
            logger.info("    Last Used: ${cred.lastUsed ?: 'Never'}")
            logger.info("    Previously Used In: ${cred.usedInJobs.size()} job(s)")
        }
        
        logger.info("\nRECOMMENDATION:")
        logger.info("Review these credentials and consider removing them if no longer needed.")
    }
    
    logger.info("\n" + "="*80)
}

/**
 * Outputs compliance report.
 */
void outputComplianceReport(Map report, Logger logger) {
    logger.info("\n" + "="*80)
    logger.info("COMPLIANCE REPORT")
    logger.info("="*80)
    
    def compliance = report.compliance as Map
    
    logger.info("\nOVERALL STATUS: ${compliance.overall}")
    
    logger.info("\nSTANDARD COMPLIANCE:")
    
    // SOX
    logger.info("\nSOX (Sarbanes-Oxley):")
    logger.info("  Status: ${compliance.sox.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
    if (!compliance.sox.compliant) {
        logger.info("  Violations:")
        compliance.sox.violations.each { violation ->
            logger.info("    - ${violation}")
        }
    }
    
    // HIPAA
    logger.info("\nHIPAA (Health Insurance Portability and Accountability Act):")
    logger.info("  Status: ${compliance.hipaa.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
    if (!compliance.hipaa.compliant) {
        logger.info("  Violations:")
        compliance.hipaa.violations.each { violation ->
            logger.info("    - ${violation}")
        }
    }
    
    // PCI-DSS
    logger.info("\nPCI-DSS (Payment Card Industry Data Security Standard):")
    logger.info("  Status: ${compliance.pciDss.compliant ? 'COMPLIANT' : 'NON-COMPLIANT'}")
    if (!compliance.pciDss.compliant) {
        logger.info("  Violations:")
        compliance.pciDss.violations.each { violation ->
            logger.info("    - ${violation}")
        }
    }
    
    if (compliance.overall != 'COMPLIANT') {
        logger.info("\nRECOMMENDED ACTIONS:")
        logger.info("1. Rotate all credentials exceeding age requirements")
        logger.info("2. Remove or document unused credentials")
        logger.info("3. Strengthen weak credentials")
        logger.info("4. Implement automated rotation policies")
    }
    
    logger.info("\n" + "="*80)
}

logger.info("Credential management operation complete.")