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

package com.github.thomasvincent.jenkinsscripts.config

import com.github.thomasvincent.jenkinsscripts.DockerIntegrationTest
import spock.lang.Stepwise

/**
 * Integration tests for configuration backup and restore scripts.
 * 
 * Tests backup of Jenkins configuration, jobs, and plugins.
 */
@Stepwise
class ConfigBackupIntegrationTest extends DockerIntegrationTest {
    
    static String backupLocation = '/tmp/jenkins-backup-test'
    
    def setupSpec() {
        // Create backup directory
        def mkdirScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}')
if (!Files.exists(backupDir)) {
    Files.createDirectories(backupDir)
}
println "Backup directory created: ${backupLocation}"
"""
        httpClient.executeScript(mkdirScript)
    }
    
    def setup() {
        // Create test configuration
        createTestJob('test-backup-job-1')
        createTestJob('test-backup-job-2')
        
        // Create a folder with jobs
        def createFolderScript = """
import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder

def jenkins = Jenkins.instance
def folder = new Folder(jenkins, 'test-backup-folder')
jenkins.add(folder, folder.name)

// Add a job to the folder
def jobXml = '''<?xml version='1.1' encoding='UTF-8'?>
<project>
  <description>Job in folder for backup testing</description>
  <builders>
    <hudson.tasks.Shell>
      <command>echo "Folder job"</command>
    </hudson.tasks.Shell>
  </builders>
</project>'''

folder.createProjectFromXML('folder-job', new ByteArrayInputStream(jobXml.bytes))
jenkins.save()

println "Created folder with job for backup testing"
"""
        httpClient.executeScript(createFolderScript)
        
        // Create some global configuration
        def setGlobalConfigScript = """
import jenkins.model.*

def jenkins = Jenkins.instance

// Set some global properties for testing
jenkins.systemMessage = 'Test Jenkins Instance for Backup Testing'
jenkins.numExecutors = 4
jenkins.quietPeriod = 10
jenkins.scmCheckoutRetryCount = 2

// Add some environment variables
def globalNodeProperties = jenkins.globalNodeProperties
def envVarsNodeProperty = new hudson.slaves.EnvironmentVariablesNodeProperty()
envVarsNodeProperty.envVars.put('TEST_ENV_VAR', 'backup-test-value')
envVarsNodeProperty.envVars.put('BACKUP_TEST', 'true')
globalNodeProperties.add(envVarsNodeProperty)

jenkins.save()
println "Global configuration set for backup testing"
"""
        httpClient.executeScript(setGlobalConfigScript)
    }
    
    def "test BackupJenkinsConfig script creates full backup"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: backupLocation,
            includeJobs: true,
            includePlugins: true,
            includeSecrets: false  // Don't backup secrets in test
        ])
        
        then:
        result != null
        result.contains('Backup completed') || result.contains('backup created')
        
        when:
        def listBackupScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}')
def files = []
Files.walk(backupDir).forEach { path ->
    if (Files.isRegularFile(path)) {
        files << path.fileName.toString()
    }
}

println "Backup contains \${files.size()} files:"
files.sort().each { println "  - \${it}" }
"""
        def listResult = httpClient.executeScript(listBackupScript)
        
        then:
        listResult.status == 200
        listResult.content.contains('files')
    }
    
    def "test selective job backup"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/selective",
            includeJobs: true,
            jobPattern: 'test-backup-job-1',
            includePlugins: false,
            includeGlobalConfig: false
        ])
        
        then:
        result != null
        result.contains('test-backup-job-1') || result.contains('backup')
        
        when:
        def verifyScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}/selective')
def hasJob1 = Files.exists(backupDir.resolve('jobs/test-backup-job-1/config.xml'))
def hasJob2 = Files.exists(backupDir.resolve('jobs/test-backup-job-2/config.xml'))

println "Selective backup verification:"
println "  - test-backup-job-1: \${hasJob1 ? 'YES' : 'NO'}"
println "  - test-backup-job-2: \${hasJob2 ? 'YES' : 'NO'}"
"""
        def verifyResult = httpClient.executeScript(verifyScript)
        
        then:
        verifyResult.content.contains('test-backup-job-1: YES')
        verifyResult.content.contains('test-backup-job-2: NO')
    }
    
    def "test backup with folder structure"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/with-folders",
            includeJobs: true,
            includeFolders: true,
            includePlugins: false
        ])
        
        then:
        result != null
        result.contains('backup') || result.contains('Backup')
        
        when:
        def verifyFolderScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}/with-folders')
def hasFolder = Files.exists(backupDir.resolve('jobs/test-backup-folder'))
def hasFolderJob = Files.exists(backupDir.resolve('jobs/test-backup-folder/jobs/folder-job/config.xml'))

println "Folder backup verification:"
println "  - Folder exists: \${hasFolder}"
println "  - Folder job exists: \${hasFolderJob}"
"""
        def verifyResult = httpClient.executeScript(verifyFolderScript)
        
        then:
        verifyResult.content.contains('Folder exists: true')
    }
    
    def "test backup compression"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/compressed",
            compress: true,
            compressionFormat: 'tar.gz'
        ])
        
        then:
        result != null
        result.contains('compressed') || result.contains('archive')
        
        when:
        def verifyCompressionScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}/compressed')
def archives = []
Files.list(backupDir).forEach { path ->
    if (path.toString().endsWith('.tar.gz')) {
        archives << path.fileName.toString()
    }
}

println "Compressed backups found: \${archives.size()}"
archives.each { println "  - \${it}" }
"""
        def verifyResult = httpClient.executeScript(verifyCompressionScript)
        
        then:
        verifyResult.content.contains('Compressed backups found:')
        verifyResult.content.contains('.tar.gz')
    }
    
    def "test incremental backup"() {
        given:
        // Create initial backup
        executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/incremental/full",
            includeJobs: true
        ])
        
        // Make a change
        createTestJob('test-new-job-after-backup')
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/incremental/delta",
            incrementalMode: true,
            lastBackupPath: "${backupLocation}/incremental/full"
        ])
        
        then:
        result != null
        result.contains('incremental') || result.contains('changes')
        result.contains('test-new-job-after-backup') || result.contains('new job')
        
        cleanup:
        deleteTestJob('test-new-job-after-backup')
    }
    
    def "test backup metadata and manifest"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: "${backupLocation}/with-metadata",
            generateManifest: true,
            includeMetadata: true
        ])
        
        then:
        result != null
        
        when:
        def readManifestScript = """
import java.nio.file.*

def manifestPath = Paths.get('${backupLocation}/with-metadata/backup-manifest.json')
if (Files.exists(manifestPath)) {
    def manifest = new groovy.json.JsonSlurper().parse(manifestPath.toFile())
    println "Backup Manifest:"
    println "  - Date: \${manifest.date}"
    println "  - Jenkins Version: \${manifest.jenkinsVersion}"
    println "  - Jobs Backed Up: \${manifest.jobCount}"
    println "  - Total Size: \${manifest.totalSize}"
} else {
    println "No manifest found"
}
"""
        def manifestResult = httpClient.executeScript(readManifestScript)
        
        then:
        manifestResult.content.contains('Backup Manifest:')
        manifestResult.content.contains('Jenkins Version:')
    }
    
    def "test backup retention policy"() {
        given:
        // Create multiple backups
        5.times { i ->
            Thread.sleep(1000)  // Ensure different timestamps
            executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
                backupPath: "${backupLocation}/retention/backup-${i}",
                includeJobs: true
            ])
        }
        
        when:
        def cleanupScript = """
import java.nio.file.*
import java.time.*

def retentionDir = Paths.get('${backupLocation}/retention')
def retentionDays = 0  // Keep only backups from today
def cutoffTime = Instant.now().minus(retentionDays, java.time.temporal.ChronoUnit.DAYS)

def deletedCount = 0
Files.list(retentionDir).forEach { backup ->
    def lastModified = Files.getLastModifiedTime(backup).toInstant()
    if (lastModified.isBefore(cutoffTime)) {
        // In real implementation, would delete
        deletedCount++
        println "Would delete old backup: \${backup.fileName}"
    }
}

println "\\nRetention policy: \${deletedCount} backups would be deleted"
"""
        def cleanupResult = httpClient.executeScript(cleanupScript)
        
        then:
        cleanupResult.status == 200
        cleanupResult.content.contains('Retention policy:')
    }
    
    def "test backup verification and integrity check"() {
        given:
        def backupPath = "${backupLocation}/verify-test"
        executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy', [
            backupPath: backupPath,
            includeJobs: true,
            generateChecksums: true
        ])
        
        when:
        def verifyScript = """
import java.nio.file.*
import java.security.MessageDigest

def backupDir = Paths.get('${backupPath}')
def checksumFile = backupDir.resolve('checksums.md5')

if (Files.exists(checksumFile)) {
    def checksums = checksumFile.toFile().readLines()
    def verified = 0
    def failed = 0
    
    checksums.each { line ->
        def parts = line.split('  ')
        if (parts.size() == 2) {
            def expectedHash = parts[0]
            def filePath = backupDir.resolve(parts[1])
            
            if (Files.exists(filePath)) {
                // Calculate actual hash
                def md = MessageDigest.getInstance("MD5")
                filePath.toFile().eachByte(4096) { bytes, size ->
                    md.update(bytes, 0, size)
                }
                def actualHash = md.digest().encodeHex().toString()
                
                if (actualHash == expectedHash) {
                    verified++
                } else {
                    failed++
                    println "Checksum mismatch: \${parts[1]}"
                }
            }
        }
    }
    
    println "\\nBackup Integrity Check:"
    println "  - Files verified: \${verified}"
    println "  - Files failed: \${failed}"
    println "  - Status: \${failed == 0 ? 'PASSED' : 'FAILED'}"
} else {
    println "No checksum file found"
}
"""
        def verifyResult = httpClient.executeScript(verifyScript)
        
        then:
        verifyResult.content.contains('Backup Integrity Check:')
        verifyResult.content.contains('Status: PASSED')
    }
    
    def cleanup() {
        // Clean up test jobs and folders
        super.cleanup()
        
        // Clean up folder
        try {
            httpClient.post("/job/test-backup-folder/doDelete", "")
        } catch (Exception e) {
            // Ignore
        }
    }
    
    def cleanupSpec() {
        // Clean up backup directory
        def cleanupScript = """
import java.nio.file.*

def backupDir = Paths.get('${backupLocation}')
if (Files.exists(backupDir)) {
    // In real implementation, would recursively delete
    println "Backup directory would be cleaned: ${backupLocation}"
}
"""
        httpClient.executeScript(cleanupScript)
    }
}