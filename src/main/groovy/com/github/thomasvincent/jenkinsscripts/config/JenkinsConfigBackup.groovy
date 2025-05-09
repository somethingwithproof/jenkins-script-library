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

import jenkins.model.Jenkins
import hudson.security.Permission
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Jenkins configuration backup utility.
 * 
 * Backs up key Jenkins configuration files with support for compression
 * and backup rotation. Uses fluent API for easy configuration.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JenkinsConfigBackup {
    
    private static final Logger LOGGER = Logger.getLogger(JenkinsConfigBackup.class.getName())
    private static final List<String> DEFAULT_CONFIG_FILES = [
        "config.xml",                  // Main jenkins configuration
        "credentials.xml",             // Credentials
        "hudson.plugins.git.GitTool.xml", // Git configuration 
        "jenkins.model.JenkinsLocationConfiguration.xml", // Location config
        "hudson.tasks.Mailer.xml",     // Email notification config
        "users/",                      // User configurations
        "secrets/",                    // Secret files
        "jobs/",                       // Job configurations
        "nodes/",                      // Agent node configurations
        "plugins/"                     // Plugin configs
    ]
    
    private final Jenkins jenkins
    private List<String> configFiles
    private boolean compress = true
    
    /**
     * Creates a backup utility for the specified Jenkins instance.
     * 
     * ```groovy
     * def backup = new JenkinsConfigBackup(Jenkins.get())
     * ```
     */
    JenkinsConfigBackup(Jenkins jenkins) {
        this.jenkins = jenkins
        this.configFiles = DEFAULT_CONFIG_FILES
    }
    
    /**
     * Sets files to backup (relative to JENKINS_HOME).
     * 
     * ```groovy
     * backup.withConfigFiles(['config.xml', 'jobs/', 'users/'])
     * ```
     */
    JenkinsConfigBackup withConfigFiles(List<String> configFiles) {
        this.configFiles = configFiles
        return this
    }
    
    /**
     * Enables or disables ZIP compression.
     * 
     * ```groovy
     * backup.withCompression(false) // Store as uncompressed files
     * ```
     */
    JenkinsConfigBackup withCompression(boolean compress) {
        this.compress = compress
        return this
    }
    
    /**
     * Creates a Jenkins configuration backup.
     * 
     * Requires admin privileges and creates a timestamped backup.
     * 
     * ```groovy
     * String backupPath = backup.createBackup("/var/backups/jenkins")
     * println "Backup created at: ${backupPath}"
     * ```
     */
    String createBackup(String backupDir) throws IOException, SecurityException {
        // Check permissions
        if (!jenkins.hasPermission(Permission.ADMINISTER)) {
            throw new SecurityException("Administrative privileges required to backup Jenkins configuration")
        }
        
        // Ensure the backup directory exists
        Files.createDirectories(Paths.get(backupDir))
        
        // Create a timestamped directory for this backup
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        String backupName = "jenkins_backup_${timestamp}"
        Path backupPath = Paths.get(backupDir, backupName)
        
        if (compress) {
            return createCompressedBackup(backupPath)
        } else {
            return createUncompressedBackup(backupPath)
        }
    }
    
    /**
     * Creates an uncompressed backup.
     */
    private String createUncompressedBackup(Path backupPath) throws IOException {
        Files.createDirectories(backupPath)
        
        String jenkinsHome = jenkins.rootDir.absolutePath
        int filesCopied = 0
        
        for (String configFile : configFiles) {
            Path source = Paths.get(jenkinsHome, configFile)
            if (!Files.exists(source)) {
                LOGGER.warning("Config file or directory not found: ${source}")
                continue
            }
            
            Path destination = Paths.get(backupPath.toString(), configFile)
            
            if (Files.isDirectory(source)) {
                Files.createDirectories(destination)
                Files.walk(source).forEach { sourcePath ->
                    Path relativePath = source.relativize(sourcePath)
                    Path targetPath = Paths.get(destination.toString(), relativePath.toString())
                    
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath)
                    } else {
                        Files.createDirectories(targetPath.parent)
                        Files.copy(sourcePath, targetPath)
                        filesCopied++
                    }
                }
            } else {
                Files.createDirectories(destination.parent)
                Files.copy(source, destination)
                filesCopied++
            }
        }
        
        LOGGER.info("Backed up ${filesCopied} files to ${backupPath}")
        return backupPath.toString()
    }
    
    /**
     * Creates a compressed backup as a ZIP file.
     */
    private String createCompressedBackup(Path backupPath) throws IOException {
        String zipFilePath = backupPath.toString() + ".zip"
        String jenkinsHome = jenkins.rootDir.absolutePath
        int filesCopied = 0
        
        ZipOutputStream zipOut = null
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))
            for (String configFile : configFiles) {
                Path source = Paths.get(jenkinsHome, configFile)
                if (!Files.exists(source)) {
                    LOGGER.warning("Config file or directory not found: ${source}")
                    continue
                }
                
                if (Files.isDirectory(source)) {
                    Files.walk(source).forEach { sourcePath ->
                        if (!Files.isDirectory(sourcePath)) {
                            Path relativePath = Paths.get(jenkinsHome).relativize(sourcePath)
                            try {
                                ZipEntry zipEntry = new ZipEntry(relativePath.toString())
                                zipOut.putNextEntry(zipEntry)
                                Files.copy(sourcePath, zipOut)
                                zipOut.closeEntry()
                                filesCopied++
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Failed to add file to zip: ${sourcePath}", e)
                            }
                        }
                    }
                } else {
                    Path relativePath = Paths.get(jenkinsHome).relativize(source)
                    try {
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString())
                        zipOut.putNextEntry(zipEntry)
                        Files.copy(source, zipOut)
                        zipOut.closeEntry()
                        filesCopied++
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to add file to zip: ${source}", e)
                    }
                }
            }
        } finally {
            if (zipOut != null) {
                zipOut.close()
            }
        }
        
        LOGGER.info("Backed up ${filesCopied} files to ${zipFilePath}")
        return zipFilePath
    }
    
    /**
     * Lists all backups in the specified directory.
     * 
     * Returns a sorted list with newest backups first.
     * 
     * ```groovy
     * def backups = backup.listAvailableBackups("/var/backups/jenkins")
     * backups.each { b ->
     *     println "${b.name} (${b.size} bytes, created ${b.date})"
     * }
     * ```
     */
    List<Map<String, Object>> listAvailableBackups(String backupDir) {
        List<Map<String, Object>> backups = []
        Path backupPath = Paths.get(backupDir)
        
        if (!Files.exists(backupPath) || !Files.isDirectory(backupPath)) {
            return backups
        }
        
        Files.list(backupPath).forEach { path ->
            String fileName = path.fileName.toString()
            
            if ((fileName.startsWith("jenkins_backup_") && Files.isDirectory(path)) ||
                (fileName.startsWith("jenkins_backup_") && fileName.endsWith(".zip"))) {
                
                Map<String, Object> backupInfo = [
                    path: path.toString(),
                    name: fileName,
                    date: Files.getLastModifiedTime(path).toInstant(),
                    size: Files.isDirectory(path) ? 
                        Files.walk(path).filter({ p -> !Files.isDirectory(p) })
                             .mapToLong({ p -> Files.size(p) }).sum() : 
                        Files.size(path),
                    compressed: fileName.endsWith(".zip")
                ]
                
                backups.add(backupInfo)
            }
        }
        
        return backups.sort { b1, b2 -> b2.date <=> b1.date }
    }
    
    /**
     * Deletes old backups, keeping only the most recent ones.
     * 
     * Uses Groovy's drop() to elegantly handle collection slicing.
     * 
     * ```groovy
     * int deleted = backup.purgeOldBackups("/var/backups/jenkins", 5)
     * println "${deleted} old backups were removed"
     * ```
     */
    int purgeOldBackups(String backupDir, int keepCount) {
        List<Map<String, Object>> backups = listAvailableBackups(backupDir)
        
        if (backups.size() <= keepCount) {
            return 0
        }
        
        int deletedCount = 0
        backups.drop(keepCount).each { backup ->
            try {
                Path path = Paths.get(backup.path)
                
                if (Files.isDirectory(path)) {
                    Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach({ p -> Files.delete(p) })
                } else {
                    Files.delete(path)
                }
                
                deletedCount++
                LOGGER.info("Deleted old backup: ${backup.name}")
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to delete backup: ${backup.path}", e)
            }
        }
        
        return deletedCount
    }
}