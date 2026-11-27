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

package com.github.thomasvincent.jenkinsscripts.config

import com.cloudbees.groovy.cps.NonCPS

import jenkins.model.Jenkins
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.StandardCopyOption
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
    @NonCPS
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
    @NonCPS
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
    @NonCPS
    String createBackup(String backupDir) throws IOException, SecurityException {
        return writeBackup(Paths.get(backupDir), null)
    }

    @NonCPS
    private String writeBackup(Path parent, String requestedName) {
        if (!jenkins.hasPermission(Jenkins.ADMINISTER)) {
            throw new SecurityException("Administrative privileges required to backup Jenkins configuration")
        }
        parent = parent.toAbsolutePath().normalize()
        Files.createDirectories(parent)
        parent = parent.toRealPath()
        // A backup inside a selected configuration tree could recursively include itself.
        for (String item : configFiles) {
            Path source = jenkins.rootDir.toPath().resolve(item)
            if (Files.isDirectory(source) && parent.startsWith(source.toRealPath())) {
                throw new IOException("Backup destination is inside selected directory: ${item}")
            }
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        String name = requestedName ?: "jenkins_backup_${timestamp}_${UUID.randomUUID()}${compress ? '.zip' : ''}"
        Path target = parent.resolve(name)
        Path staging = Files.createTempDirectory(parent, '.jenkins-backup-')
        try {
            Path output = compress ? Files.createTempFile(staging, 'backup-', '.zip') :
                Files.createTempDirectory(staging, 'backup-')
            String completed = compress ? createCompressedBackup(output) : createUncompressedBackup(output)
            // Publish only a complete backup. An existing destination belongs to the caller.
            Files.move(Paths.get(completed), target)
            return target.toString()
        } finally {
            hudson.Util.deleteRecursive(staging.toFile())
        }
    }

    /**
     * Performs a backup with explicit inclusion flags to a destination path or directory.
     */
    @NonCPS
    String performBackup(String destination, boolean includeJobs = true, boolean includePlugins = true,
                         boolean includeNodes = true, boolean compressBackup = true, boolean destinationIsDirectory = false) throws IOException, SecurityException {
        List<String> selectedFiles = [
            "config.xml",
            "credentials.xml",
            "hudson.plugins.git.GitTool.xml",
            "jenkins.model.JenkinsLocationConfiguration.xml",
            "hudson.tasks.Mailer.xml",
            "users/",
            "secrets/"
        ]
        if (includeJobs) {
            selectedFiles.add("jobs/")
        }
        if (includeNodes) {
            selectedFiles.add("nodes/")
        }
        if (includePlugins) {
            selectedFiles.add("plugins/")
        }

        this.configFiles = selectedFiles
        this.compress = compressBackup

        Path destPath = Paths.get(destination).toAbsolutePath().normalize()
        if (destinationIsDirectory || destination.endsWith(File.separator) ||
                (compressBackup && Files.isDirectory(destPath))) {
            return createBackup(destPath.toString())
        }
        return writeBackup(destPath.parent, destPath.fileName.toString())
    }

    /**
     * Restores Jenkins configuration from a backup ZIP file or directory.
     */
    @NonCPS
    boolean restoreBackup(String backupFilePath) throws IOException, SecurityException {
        jenkins.checkPermission(Jenkins.ADMINISTER)
        if (!Files.exists(Paths.get(backupFilePath))) return false
        restore(backupFilePath)
        return true
    }

    @NonCPS
    Map restore(String archivePath, Map options = [:]) {
        jenkins.checkPermission(Jenkins.ADMINISTER)
        BackupRestore.restore(jenkins.rootDir.toPath(), Paths.get(archivePath),
            options.files ?: [], options.dryRun == true || options.listContents == true)
    }

    /**
     * Checks whether a path under jobs/ points to build logs, artifacts, or workspaces that should be excluded.
     */
    @NonCPS
    private static boolean shouldExcludePath(String configFile, Path sourceRoot, Path candidate) {
        if (!configFile.startsWith("jobs")) {
            return false
        }
        if (Files.isSymbolicLink(candidate)) {
            return true
        }
        Path relative = sourceRoot.relativize(candidate)
        for (int i = 0; i < relative.getNameCount(); i++) {
            String segment = relative.getName(i).toString()
            if (segment == "builds" || segment == "workspace" || segment == "modules"
                    || segment.startsWith("lastSuccessful") || segment.startsWith("lastStable")
                    || segment.startsWith("lastFailed") || segment.startsWith("lastUnstable")
                    || segment.startsWith("lastCompleted")) {
                return true
            }
        }
        return false
    }

    /**
     * Creates an uncompressed backup.
     */
    @NonCPS
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
                BackupFiles.withStream(Files.walk(source)) { stream ->
                    stream.forEach { Path sourcePath ->
                        if (shouldExcludePath(configFile, source, sourcePath)) {
                            return
                        }
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
    @NonCPS
    private String createCompressedBackup(Path backupPath) throws IOException {
        String zipFilePath = backupPath.toString()
        String jenkinsHome = jenkins.rootDir.absolutePath
        Path jenkinsHomePath = Paths.get(jenkinsHome)
        int filesCopied = 0

        new ZipOutputStream(new FileOutputStream(zipFilePath)).withCloseable { ZipOutputStream zipOut ->
            for (String configFile : configFiles) {
                Path source = Paths.get(jenkinsHome, configFile)
                if (!Files.exists(source)) {
                    LOGGER.warning("Config file or directory not found: ${source}")
                    continue
                }

                if (Files.isDirectory(source)) {
                    BackupFiles.withStream(Files.walk(source)) { stream ->
                        stream.forEach { Path sourcePath ->
                            if (!Files.isDirectory(sourcePath) && !shouldExcludePath(configFile, source, sourcePath)) {
                                Path relativePath = jenkinsHomePath.relativize(sourcePath)
                                addFileToZip(zipOut, sourcePath, relativePath.toString())
                                filesCopied++
                            }
                        }
                    }
                } else {
                    Path relativePath = jenkinsHomePath.relativize(source)
                    addFileToZip(zipOut, source, relativePath.toString())
                    filesCopied++
                }
            }
        }

        LOGGER.info("Backed up ${filesCopied} files to ${zipFilePath}")
        return zipFilePath
    }

    @NonCPS
    private void addFileToZip(ZipOutputStream zipOut, Path sourcePath, String entryName) {
        zipOut.putNextEntry(new ZipEntry(entryName))
        try {
            Files.copy(sourcePath, zipOut)
        } finally {
            zipOut.closeEntry()
        }
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
    @NonCPS
    List<Map<String, Object>> listAvailableBackups(String backupDir) {
        List<Map<String, Object>> backups = []
        Path backupPath = Paths.get(backupDir)

        if (!Files.exists(backupPath) || !Files.isDirectory(backupPath)) {
            return backups
        }

        BackupFiles.withStream(Files.list(backupPath)) { stream ->
            stream.forEach { Path path ->
                String fileName = path.fileName.toString()

                if ((fileName.startsWith("jenkins_backup_") && Files.isDirectory(path)) ||
                    (fileName.startsWith("jenkins_backup_") && fileName.endsWith(".zip"))) {

                    long size
                    if (Files.isDirectory(path)) {
                        size = BackupFiles.withStream(Files.walk(path)) { walkStream ->
                            walkStream.filter({ Path p -> !Files.isDirectory(p) })
                                    .mapToLong({ Path p -> Files.size(p) })
                                    .sum()
                        }
                    } else {
                        size = Files.size(path)
                    }

                    Map<String, Object> backupInfo = [
                        path: path.toString(),
                        name: fileName,
                        date: Files.getLastModifiedTime(path).toInstant(),
                        size: size,
                        compressed: fileName.endsWith(".zip")
                    ]

                    backups.add(backupInfo)
                }
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
    @NonCPS
    int purgeOldBackups(String backupDir, int keepCount) {
        List<Map<String, Object>> backups = listAvailableBackups(backupDir)

        if (backups.size() <= keepCount) {
            return 0
        }

        int deletedCount = 0
        backups.drop(keepCount).each { backup ->
            try {
                Path path = Paths.get(backup.path as String)

                if (Files.isDirectory(path)) {
                    BackupFiles.withStream(Files.walk(path)) { walkStream ->
                        walkStream.sorted(Comparator.reverseOrder())
                                .forEach({ Path p -> Files.delete(p) })
                    }
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
