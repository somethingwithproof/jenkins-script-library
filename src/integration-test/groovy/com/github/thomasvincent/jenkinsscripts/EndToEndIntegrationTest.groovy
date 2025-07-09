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

package com.github.thomasvincent.jenkinsscripts

import com.github.thomasvincent.jenkinsscripts.util.*
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Stepwise
import java.util.logging.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * End-to-end integration tests that simulate complete Jenkins workflows.
 * These tests validate that all components work together correctly in realistic scenarios.
 */
@Title("End-to-End Integration Test")
@Stepwise
class EndToEndIntegrationTest extends Specification {
    
    private static final Logger logger = Logger.getLogger(EndToEndIntegrationTest.class.name)
    private Path tempDir
    private Map<String, Object> testContext = [:]
    
    def setup() {
        tempDir = Files.createTempDirectory("jenkins-e2e-test")
        logger.info("Created temp directory: ${tempDir}")
    }
    
    def cleanup() {
        if (tempDir && Files.exists(tempDir)) {
            tempDir.toFile().deleteDir()
            logger.info("Cleaned up temp directory")
        }
    }
    
    def "E2E Test 1: Complete job configuration workflow"() {
        given: "a job configuration request with various parameters"
        def jobConfig = [
            name: "  Production Deploy Pipeline #1  ",
            description: "Deploy application to production environment",
            parameters: [
                branch: "main",
                environment: "production",
                version: "2.361.4",
                enableNotifications: "true",
                recipients: "team@company.com"
            ],
            schedule: "H 2 * * 1-5", // Weekdays at 2 AM
            timeout: "60"
        ]
        
        when: "we process the job configuration through our utilities"
        def processedConfig = ErrorHandler.withErrorHandling("job configuration", {
            // Step 1: Validate and sanitize job name
            def validatedName = ValidationUtils.requireNonEmpty(jobConfig.name, "jobName")
            def sanitizedName = StringUtils.sanitizeJobName(validatedName)
            
            // Step 2: Validate description
            def validatedDesc = ValidationUtils.requireNonEmpty(jobConfig.description, "description")
            
            // Step 3: Process parameters
            def processedParams = jobConfig.parameters.collectEntries { k, v ->
                def validated = ValidationUtils.requireNonEmpty(v, k)
                def formatted = StringUtils.formatParameter(k, validated)
                [k, [value: validated, display: formatted]]
            }
            
            // Step 4: Validate numeric parameters
            def timeout = StringUtils.safeParseInt(jobConfig.timeout, 30)
            timeout = ValidationUtils.requireInRange(timeout, 1, 180, "timeout")
            
            // Step 5: Parse boolean parameters
            def notifications = StringUtils.safeParseBoolean(
                processedParams.enableNotifications?.value, false
            )
            
            // Step 6: Validate version format
            def version = processedParams.version?.value
            if (version && !version.matches(/^\d+\.\d+\.\d+$/)) {
                throw new IllegalArgumentException("Invalid version format: ${version}")
            }
            
            return [
                originalName: jobConfig.name,
                name: sanitizedName,
                description: validatedDesc,
                parameters: processedParams,
                schedule: jobConfig.schedule,
                timeout: timeout,
                notifications: notifications,
                processed: true
            ]
        }, logger)
        
        then: "the configuration is properly processed"
        processedConfig.processed == true
        processedConfig.name == "Production_Deploy_Pipeline_#1"
        processedConfig.description == "Deploy application to production environment"
        processedConfig.timeout == 60
        processedConfig.notifications == true
        
        and: "parameters are validated and formatted"
        processedConfig.parameters.branch.value == "main"
        processedConfig.parameters.branch.display == "branch=main"
        processedConfig.parameters.version.value == "2.361.4"
        
        when: "we store in context for next test"
        testContext.jobConfig = processedConfig
        
        then: "context is updated"
        testContext.jobConfig != null
    }
    
    def "E2E Test 2: Simulate pipeline execution with metrics"() {
        given: "a simulated pipeline execution"
        def jobName = testContext.jobConfig?.name ?: "test-pipeline"
        def stages = [
            "Checkout": [duration: 5000, status: "SUCCESS"],
            "Build": [duration: 45000, status: "SUCCESS"],
            "Test": [duration: 120000, status: "SUCCESS"],
            "Deploy": [duration: 30000, status: "FAILED", error: "Connection timeout"]
        ]
        
        when: "we simulate pipeline execution and collect metrics"
        def executionResult = ErrorHandler.withErrorHandling("pipeline execution", {
            def startTime = System.currentTimeMillis()
            def stageResults = []
            def failed = false
            
            stages.each { stageName, stageData ->
                logger.info("Executing stage: ${stageName}")
                
                // Simulate stage execution
                Thread.sleep(10) // Simulate some work
                
                def stageResult = [
                    name: stageName,
                    startTime: System.currentTimeMillis(),
                    duration: stageData.duration,
                    status: stageData.status,
                    error: stageData.error
                ]
                
                if (stageData.status == "FAILED") {
                    failed = true
                    logger.warning("Stage ${stageName} failed: ${stageData.error}")
                }
                
                stageResults << stageResult
                
                if (failed) {
                    throw new RuntimeException("Pipeline failed at stage: ${stageName}")
                }
            }
            
            return [
                success: true,
                stages: stageResults,
                totalDuration: System.currentTimeMillis() - startTime
            ]
        }, logger, [success: false, error: "Pipeline execution failed"])
        
        then: "the pipeline execution is tracked"
        executionResult.success == false
        executionResult.error == "Pipeline execution failed"
        
        when: "we store metrics for analysis"
        testContext.executionMetrics = [
            jobName: jobName,
            stages: stages,
            result: executionResult
        ]
        
        then: "metrics are stored"
        testContext.executionMetrics != null
    }
    
    def "E2E Test 3: Security and compliance validation workflow"() {
        given: "a set of credentials to validate"
        def credentials = [
            [id: "github-token", type: "SECRET", value: "ghp_xxxxxxxxxxxx", age: 45],
            [id: "aws-access-key", type: "ACCESS_KEY", value: "AKIA_EXAMPLE", age: 120],
            [id: "db-password", type: "PASSWORD", value: "P@ssw0rd123!", age: 200],
            [id: "ssh-key", type: "SSH_KEY", value: "ssh-rsa AAAAB3...", age: 365]
        ]
        
        when: "we perform security validation"
        def securityReport = ErrorHandler.withErrorHandling("security validation", {
            def report = [
                timestamp: new Date().toString(),
                credentials: [],
                violations: [],
                recommendations: []
            ]
            
            credentials.each { cred ->
                // Validate credential ID
                def validId = ValidationUtils.requireNonEmpty(cred.id, "credentialId")
                
                // Check credential age
                def maxAge = getMaxAgeForType(cred.type)
                def needsRotation = cred.age > maxAge
                
                // Check credential strength
                def strength = assessCredentialStrength(cred)
                
                def credReport = [
                    id: validId,
                    type: cred.type,
                    age: cred.age,
                    needsRotation: needsRotation,
                    strength: strength
                ]
                
                report.credentials << credReport
                
                // Add violations
                if (needsRotation) {
                    report.violations << "Credential '${validId}' exceeds ${maxAge} day rotation policy (age: ${cred.age} days)"
                }
                
                if (strength == "WEAK") {
                    report.violations << "Credential '${validId}' has weak security strength"
                }
            }
            
            // Generate recommendations
            def rotationNeeded = report.credentials.count { it.needsRotation }
            if (rotationNeeded > 0) {
                report.recommendations << [
                    type: "ROTATION",
                    priority: "HIGH",
                    message: "${rotationNeeded} credentials need rotation",
                    credentialIds: report.credentials.findAll { it.needsRotation }.collect { it.id }
                ]
            }
            
            def weakCreds = report.credentials.count { it.strength == "WEAK" }
            if (weakCreds > 0) {
                report.recommendations << [
                    type: "SECURITY",
                    priority: "HIGH",
                    message: "${weakCreds} credentials have weak security strength"
                ]
            }
            
            report.compliant = report.violations.isEmpty()
            return report
        }, logger)
        
        then: "security issues are identified"
        securityReport.compliant == false
        securityReport.violations.size() >= 2
        securityReport.recommendations.size() >= 1
        
        and: "specific credentials are flagged"
        def dbCred = securityReport.credentials.find { it.id == "db-password" }
        dbCred.needsRotation == true
        
        and: "store report for next test"
        testContext.securityReport = securityReport
    }
    
    def "E2E Test 4: Resource optimization workflow"() {
        given: "a set of cloud resources to analyze"
        def resources = [
            [name: "jenkins-agent-1", provider: "AWS", type: "m5.large", 
             cpuUsage: 15, memoryUsage: 20, idleMinutes: 45, hourlyCost: 0.096],
            [name: "jenkins-agent-2", provider: "AWS", type: "m5.xlarge", 
             cpuUsage: 65, memoryUsage: 70, idleMinutes: 5, hourlyCost: 0.192],
            [name: "jenkins-agent-3", provider: "Azure", type: "Standard_D2s_v3", 
             cpuUsage: 5, memoryUsage: 10, idleMinutes: 120, hourlyCost: 0.096],
            [name: "jenkins-agent-k8s", provider: "Kubernetes", type: "2cpu-4gb", 
             cpuUsage: 80, memoryUsage: 85, idleMinutes: 0, hourlyCost: 0.050]
        ]
        
        when: "we analyze resources for optimization"
        def optimizationReport = ErrorHandler.withErrorHandling("resource optimization", {
            def report = [
                timestamp: new Date().toString(),
                resources: [],
                recommendations: [],
                potentialSavings: 0.0
            ]
            
            resources.each { resource ->
                // Validate resource data
                def name = ValidationUtils.requireNonEmpty(resource.name, "resourceName")
                def cpuUsage = ValidationUtils.requireInRange(resource.cpuUsage, 0, 100, "cpuUsage")
                def memoryUsage = ValidationUtils.requireInRange(resource.memoryUsage, 0, 100, "memoryUsage")
                
                def analysis = [
                    name: name,
                    provider: resource.provider,
                    type: resource.type,
                    cpuUsage: cpuUsage,
                    memoryUsage: memoryUsage,
                    idleMinutes: resource.idleMinutes,
                    hourlyCost: resource.hourlyCost,
                    recommendations: []
                ]
                
                // Check for idle resources
                if (resource.idleMinutes > 30) {
                    analysis.recommendations << "TERMINATE_IDLE"
                    report.potentialSavings += resource.hourlyCost
                }
                
                // Check for underutilized resources
                def avgUsage = (cpuUsage + memoryUsage) / 2
                if (avgUsage < 30 && resource.idleMinutes < 30) {
                    analysis.recommendations << "DOWNSIZE"
                    report.potentialSavings += resource.hourlyCost * 0.5 // Assume 50% savings
                }
                
                // Check for spot instance opportunities
                if (resource.provider == "AWS" && !resource.name.contains("prod")) {
                    analysis.recommendations << "CONVERT_TO_SPOT"
                    report.potentialSavings += resource.hourlyCost * 0.7 // 70% savings on spot
                }
                
                report.resources << analysis
            }
            
            // Generate summary recommendations
            def idleResources = report.resources.count { "TERMINATE_IDLE" in it.recommendations }
            if (idleResources > 0) {
                report.recommendations << [
                    type: "IDLE_RESOURCES",
                    count: idleResources,
                    message: "Terminate ${idleResources} idle resources",
                    savingsPerHour: report.resources
                        .findAll { "TERMINATE_IDLE" in it.recommendations }
                        .sum { it.hourlyCost }
                ]
            }
            
            report.monthlySavings = report.potentialSavings * 24 * 30
            return report
        }, logger)
        
        then: "optimization opportunities are identified"
        optimizationReport.potentialSavings > 0
        optimizationReport.resources.size() == 4
        
        and: "specific resources are flagged for optimization"
        def agent3 = optimizationReport.resources.find { it.name == "jenkins-agent-3" }
        agent3.recommendations.contains("TERMINATE_IDLE")
        agent3.recommendations.contains("DOWNSIZE")
        
        and: "monthly savings are calculated"
        optimizationReport.monthlySavings > 100
    }
    
    def "E2E Test 5: Complete workflow with file operations"() {
        given: "a complete workflow scenario"
        def workflowConfig = [
            jobName: "backup-and-cleanup",
            backupDir: tempDir.toString(),
            retentionDays: 7,
            patterns: ["*.log", "*.tmp", "*.bak"]
        ]
        
        and: "create some test files"
        def testFiles = []
        5.times { i ->
            def file = tempDir.resolve("test-${i}.log").toFile()
            file.text = "Test log content ${i}\n" * 100
            testFiles << file
        }
        
        3.times { i ->
            def file = tempDir.resolve("temp-${i}.tmp").toFile()
            file.text = "Temporary data ${i}"
            testFiles << file
        }
        
        when: "we execute the complete workflow"
        def workflowResult = ErrorHandler.withErrorHandling("backup workflow", {
            // Step 1: Validate configuration
            def jobName = ValidationUtils.requireNonEmpty(workflowConfig.jobName, "jobName")
            def backupDir = ValidationUtils.requireDirectoryExists(workflowConfig.backupDir, "backupDir")
            def retention = ValidationUtils.requireInRange(workflowConfig.retentionDays, 1, 365, "retentionDays")
            
            // Step 2: Create backup subdirectory
            def timestamp = new Date().format("yyyyMMdd-HHmmss")
            def backupSubDir = Paths.get(backupDir, "backup-${timestamp}")
            Files.createDirectories(backupSubDir)
            
            // Step 3: Process files
            def processedFiles = []
            def errors = []
            
            workflowConfig.patterns.each { pattern ->
                try {
                    def matchingFiles = new File(backupDir).listFiles({ File file ->
                        file.name.matches(pattern.replace("*", ".*"))
                    } as FileFilter)
                    
                    matchingFiles?.each { file ->
                        try {
                            // Analyze file
                            def fileInfo = [
                                name: file.name,
                                size: file.length(),
                                pattern: pattern,
                                action: determineAction(file)
                            ]
                            
                            // Perform action
                            if (fileInfo.action == "BACKUP") {
                                def backupFile = backupSubDir.resolve(file.name).toFile()
                                file.renameTo(backupFile)
                                fileInfo.backed_up = true
                            } else if (fileInfo.action == "DELETE") {
                                file.delete()
                                fileInfo.deleted = true
                            }
                            
                            processedFiles << fileInfo
                        } catch (Exception e) {
                            errors << "Failed to process ${file.name}: ${e.message}"
                        }
                    }
                } catch (Exception e) {
                    errors << "Failed to process pattern ${pattern}: ${e.message}"
                }
            }
            
            // Step 4: Generate report
            return [
                success: errors.isEmpty(),
                jobName: jobName,
                timestamp: timestamp,
                backupLocation: backupSubDir.toString(),
                processedFiles: processedFiles,
                fileCount: processedFiles.size(),
                backedUp: processedFiles.count { it.backed_up },
                deleted: processedFiles.count { it.deleted },
                errors: errors
            ]
        }, logger)
        
        then: "the workflow completes successfully"
        workflowResult.success == true
        workflowResult.fileCount == 8
        workflowResult.backedUp > 0
        
        and: "files are processed according to rules"
        workflowResult.processedFiles.find { it.name.endsWith(".log") }?.backed_up == true
        
        and: "backup directory is created"
        Files.exists(Paths.get(workflowResult.backupLocation))
    }
    
    // Helper methods
    
    private int getMaxAgeForType(String type) {
        switch (type) {
            case "PASSWORD": return 90
            case "SECRET": return 180
            case "ACCESS_KEY": return 180
            case "SSH_KEY": return 365
            default: return 180
        }
    }
    
    private String assessCredentialStrength(Map cred) {
        if (cred.type == "SSH_KEY") return "STRONG"
        if (cred.value.length() < 12) return "WEAK"
        if (!cred.value.matches(/.*[A-Z].*/) || !cred.value.matches(/.*[0-9].*/)) return "WEAK"
        return "MEDIUM"
    }
    
    private String determineAction(File file) {
        if (file.name.endsWith(".log")) return "BACKUP"
        if (file.name.endsWith(".tmp")) return "DELETE"
        if (file.name.endsWith(".bak") && file.lastModified() < System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) {
            return "DELETE"
        }
        return "BACKUP"
    }
}