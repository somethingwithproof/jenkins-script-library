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

package com.github.thomasvincent.jenkinsscripts.jobs

import jenkins.model.Jenkins
import hudson.model.Job
import hudson.model.AbstractItem
// File classes (File, streams) are part of java.io, which is automatically imported in Groovy
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.text.SimpleDateFormat
import java.text.ParseException

/**
 * Manages archival and restoration of Jenkins jobs.
 * 
 * Provides functionality to archive inactive jobs with metadata and restore them when needed.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobArchivalManager {
    private static final Logger LOGGER = Logger.getLogger(JobArchivalManager.class.getName())
    private static final ThreadLocal<SimpleDateFormat> FILENAME_DATE_FORMAT = ThreadLocal.withInitial({ 
        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") 
    })
    private static final ThreadLocal<SimpleDateFormat> METADATA_TIMESTAMP_FORMAT = ThreadLocal.withInitial({ 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") 
    })

    private final Jenkins jenkins
    private final File archiveDir
    
    /**
     * Creates a JobArchivalManager with specified archive directory.
     * 
     * ```groovy
     * def manager = new JobArchivalManager(
     *     jenkins: Jenkins.get(),
     *     archiveDir: new File("/var/jenkins_archives")
     * )
     * ```
     * 
     * @param jenkins Jenkins instance
     * @param archiveDir Directory to store archives
     */
    JobArchivalManager(Jenkins jenkins, File archiveDir) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        this.archiveDir = ValidationUtils.requireNonNull(archiveDir, "Archive directory")
        
        // Ensure archive directory exists
        if (!archiveDir.exists()) {
            archiveDir.mkdirs()
        }
        
        if (!archiveDir.isDirectory()) {
            throw new IllegalArgumentException("Archive path is not a directory: ${archiveDir}")
        }
    }
    
    /**
     * Archives a job with metadata.
     * 
     * ```groovy
     * def archive = manager.archiveJob(
     *     "legacy-job", 
     *     "Project completed", 
     *     ["owner": "john.doe", "project": "LEGACY"]
     * )
     * println "Job archived to ${archive.archivePath}"
     * ```
     * 
     * @param jobName Name of job to archive
     * @param reason Reason for archiving
     * @param metadata Additional metadata to store
     * @param deleteAfterArchive Whether to delete the job after archiving
     * @return JobArchive containing archive information
     */
    JobArchive archiveJob(String jobName, String reason, Map<String, String> metadata = [:], boolean deleteAfterArchive = false) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            throw new IllegalArgumentException("Job not found: ${jobName}")
        }
        
        return ErrorHandler.withErrorHandling("archiving job ${jobName}", {
            // Create archive file name
            String fileTimestamp = FILENAME_DATE_FORMAT.get().format(new Date())
            String safeJobName = jobName.replaceAll("[^a-zA-Z0-9.-]", "_")
            String archiveFileName = "${safeJobName}_${fileTimestamp}.zip"
            File archiveFile = new File(archiveDir, archiveFileName)
            
            // Extract job config and create metadata
            String jobXml = extractJobConfig(job)
            Map<String, Object> archiveMetadata = createArchiveMetadata(job, reason, metadata)
            
            // Create archive
            createArchiveZip(archiveFile, jobXml, archiveMetadata)
            
            // Create archive record
            JobArchive archive = new JobArchive(
                jobName: jobName,
                archivePath: archiveFile.absolutePath,
                timestamp: new Date(),
                reason: reason,
                metadata: metadata
            )
            
            // Delete job if requested
            if (deleteAfterArchive) {
                LOGGER.info("Deleting job ${jobName} after archiving")
                job.delete()
            }
            
            LOGGER.info("Job ${jobName} archived to ${archiveFile}")
            return archive
        }, LOGGER)
    }
    
    /**
     * Restores a job from archive.
     * 
     * ```groovy
     * boolean restored = manager.restoreJob("/var/jenkins_archives/legacy-job_2023-10-01.zip")
     * ```
     * 
     * @param archivePath Path to archive file
     * @param newJobName Optional new name for the job
     * @return true if restoration succeeded, false otherwise
     */
    boolean restoreJob(String archivePath, String newJobName = null) {
        archivePath = ValidationUtils.requireNonEmpty(archivePath, "Archive path")
        ValidationUtils.requireFileExists(archivePath, "Archive file")
        
        return ErrorHandler.withErrorHandling("restoring job from ${archivePath}", {
            File archiveFile = new File(archivePath)
            
            // Extract archive
            Map<String, Object> result = extractArchiveContents(archiveFile)
            String jobXml = result.jobXml
            Map<String, Object> metadata = result.metadata
            
            // Determine job name
            String originalJobName = metadata.jobName?.toString()
            String jobName = newJobName ?: originalJobName
            
            if (!jobName) {
                LOGGER.warning("Could not determine job name from archive")
                return false
            }
            
            // Check if job already exists
            if (jenkins.getItemByFullName(jobName) != null) {
                LOGGER.warning("Job ${jobName} already exists")
                return false
            }
            
            // Determine parent folder and job simple name
            String parentPath = getParentPath(jobName)
            String jobSimpleName = getSimpleName(jobName)
            
            // Create job
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
            
            if (parentPath) {
                // Find or create parent folder
                def parent = getOrCreateFolder(parentPath)
                parent.createProjectFromXML(jobSimpleName, inputStream)
            } else {
                jenkins.createProjectFromXML(jobSimpleName, inputStream)
            }
            
            LOGGER.info("Job restored from archive to ${jobName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Lists all archived jobs.
     * 
     * ```groovy
     * def archives = manager.listArchives()
     * archives.each { archive ->
     *     println "${archive.jobName} - ${archive.timestamp}"
     * }
     * ```
     * 
     * @return List of JobArchive objects
     */
    List<JobArchive> listArchives() {
        List<JobArchive> archives = []
        
        archiveDir.listFiles().findAll { it.name.endsWith(".zip") }.each { file ->
            try {
                Map<String, Object> result = extractArchiveContents(file)
                Map<String, Object> metadata = result.metadata
                
                JobArchive archive = new JobArchive(
                    jobName: metadata.jobName?.toString(),
                    archivePath: file.absolutePath,
                    timestamp: parseDate(metadata.timestamp?.toString()),
                    reason: metadata.reason?.toString(),
                    metadata: metadata.additionalMetadata as Map<String, String>
                )
                
                archives.add(archive)
            } catch (Exception e) {
                LOGGER.warning("Error reading archive ${file}: ${e.message}")
            }
        }
        
        // Sort by timestamp, newest first
        archives.sort { a, b -> b.timestamp <=> a.timestamp }
        
        return archives
    }
    
    /**
     * Finds archived jobs by criteria.
     * 
     * ```groovy
     * def archives = manager.findArchives(
     *     namePattern: "legacy-.*",
     *     metadata: ["project": "LEGACY"]
     * )
     * ```
     * 
     * @param namePattern Pattern to match job names
     * @param reason Reason text to match
     * @param metadata Metadata key-value pairs to match
     * @return List of matching JobArchive objects
     */
    List<JobArchive> findArchives(String namePattern = null, String reason = null, Map<String, String> metadata = null) {
        List<JobArchive> allArchives = listArchives()
        
        // Apply filters
        if (namePattern) {
            def pattern = ~namePattern
            allArchives = allArchives.findAll { archive -> 
                archive.jobName && archive.jobName =~ pattern
            }
        }
        
        if (reason) {
            allArchives = allArchives.findAll { archive -> 
                archive.reason && archive.reason.contains(reason)
            }
        }
        
        if (metadata) {
            allArchives = allArchives.findAll { archive -> 
                metadata.every { key, value -> 
                    archive.metadata?.get(key) == value
                }
            }
        }
        
        return allArchives
    }
    
    /**
     * Gets inactive jobs based on last build date.
     * 
     * ```groovy
     * def inactiveJobs = manager.findInactiveJobs(90)  // inactive for 90+ days
     * println "Found ${inactiveJobs.size()} inactive jobs"
     * ```
     * 
     * @param daysThreshold Days of inactivity to consider a job inactive
     * @return Map of job names to last build dates
     */
    Map<String, Date> findInactiveJobs(int daysThreshold) {
        Map<String, Date> inactiveJobs = [:]
        Calendar threshold = Calendar.instance
        threshold.add(Calendar.DAY_OF_YEAR, -daysThreshold)
        Date thresholdDate = threshold.time
        
        jenkins.getAllItems(Job.class).each { job ->
            def lastBuild = job.getLastBuild()
            Date lastBuildDate = lastBuild?.getTime()
            
            // If no builds or last build is older than threshold
            if (!lastBuildDate || lastBuildDate.before(thresholdDate)) {
                inactiveJobs[job.fullName] = lastBuildDate
            }
        }
        
        LOGGER.info("Found ${inactiveJobs.size()} jobs inactive for ${daysThreshold}+ days")
        return inactiveJobs
    }
    
    /**
     * Archives inactive jobs.
     * 
     * ```groovy
     * def archived = manager.archiveInactiveJobs(
     *     90,                          // 90+ days inactive
     *     "Automatic archival - inactive", 
     *     true                         // delete after archiving
     * )
     * ```
     * 
     * @param daysThreshold Days of inactivity to consider a job inactive
     * @param reason Reason for archiving
     * @param deleteAfterArchive Whether to delete jobs after archiving
     * @param excludePattern Optional regex pattern to exclude job names
     * @return List of JobArchive objects for archived jobs
     */
    List<JobArchive> archiveInactiveJobs(int daysThreshold, String reason, boolean deleteAfterArchive = false, String excludePattern = null) {
        // Find inactive jobs
        Map<String, Date> inactiveJobs = findInactiveJobs(daysThreshold)
        List<JobArchive> archivedJobs = []
        
        // Apply exclude pattern if specified
        if (excludePattern) {
            def pattern = ~excludePattern
            inactiveJobs = inactiveJobs.findAll { jobName, date -> 
                !(jobName =~ pattern)
            }
        }
        
        // Archive each inactive job
        inactiveJobs.each { jobName, lastBuildDate ->
            try {
                Map<String, String> metadata = [
                    lastBuildDate: lastBuildDate ? lastBuildDate.toString() : "Never built",
                    daysInactive: "${daysThreshold}+"
                ]
                
                JobArchive archive = archiveJob(jobName, reason, metadata, deleteAfterArchive)
                archivedJobs.add(archive)
            } catch (Exception e) {
                LOGGER.warning("Error archiving job ${jobName}: ${e.message}")
            }
        }
        
        LOGGER.info("Archived ${archivedJobs.size()} inactive jobs")
        return archivedJobs
    }
    
    /**
     * Deletes an archive.
     * 
     * ```groovy
     * boolean deleted = manager.deleteArchive("/var/jenkins_archives/old-job.zip")
     * ```
     * 
     * @param archivePath Path to archive file
     * @return true if deletion succeeded, false otherwise
     */
    boolean deleteArchive(String archivePath) {
        archivePath = ValidationUtils.requireNonEmpty(archivePath, "Archive path")
        File archiveFile = new File(archivePath)
        
        if (!archiveFile.exists()) {
            LOGGER.warning("Archive file not found: ${archivePath}")
            return false
        }
        
        boolean deleted = archiveFile.delete()
        if (deleted) {
            LOGGER.info("Deleted archive file: ${archivePath}")
        } else {
            LOGGER.warning("Failed to delete archive file: ${archivePath}")
        }
        
        return deleted
    }
    
    /**
     * Gets the XML configuration for a job.
     * 
     * @param job Job to extract configuration from
     * @return XML configuration as string
     */
    private String extractJobConfig(Job job) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        job.writeConfigDotXml(outputStream)
        return outputStream.toString("UTF-8")
    }
    
    /**
     * Creates metadata for archiving a job.
     * 
     * @param job Job to create metadata for
     * @param reason Reason for archiving
     * @param metadata Additional metadata
     * @return Complete metadata map
     */
    private Map<String, Object> createArchiveMetadata(Job job, String reason, Map<String, String> metadata) {
        Map<String, Object> archiveMetadata = [
            jobName: job.fullName,
            timestamp: METADATA_TIMESTAMP_FORMAT.get().format(new Date()), // Use standardized format
            reason: reason,
            jenkinsVersion: jenkins.version,
            lastBuild: job.getLastBuild()?.getNumber() ?: "None",
            additionalMetadata: metadata ?: [:]
        ]

        return archiveMetadata
    }
    
    /**
     * Creates a ZIP archive for a job.
     * 
     * @param archiveFile Output ZIP file
     * @param jobXml Job XML configuration
     * @param metadata Job metadata
     */
    private void createArchiveZip(File archiveFile, String jobXml, Map<String, Object> metadata) {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(archiveFile))
        
        try {
            // Add job.xml
            zipOut.putNextEntry(new ZipEntry("job.xml"))
            zipOut.write(jobXml.getBytes("UTF-8"))
            zipOut.closeEntry()
            
            // Add metadata.json
            String metadataJson = new JsonBuilder(metadata).toPrettyString()
            zipOut.putNextEntry(new ZipEntry("metadata.json"))
            zipOut.write(metadataJson.getBytes("UTF-8"))
            zipOut.closeEntry()
        } finally {
            zipOut.close()
        }
    }
    
    /**
     * Extracts contents from a job archive.
     * 
     * @param archiveFile ZIP archive file
     * @return Map containing jobXml and metadata
     */
    private Map<String, Object> extractArchiveContents(File archiveFile) {
        String jobXml = null
        Map<String, Object> metadata = null
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(archiveFile))
        
        try {
            ZipEntry entry
            while ((entry = zipIn.nextEntry) != null) {
                if (entry.name == "job.xml") {
                    // Read job XML
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()
                    byte[] buffer = new byte[1024]
                    int bytesRead
                    while ((bytesRead = zipIn.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    jobXml = baos.toString("UTF-8")
                } else if (entry.name == "metadata.json") {
                    // Read metadata JSON
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()
                    byte[] buffer = new byte[1024]
                    int bytesRead
                    while ((bytesRead = zipIn.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    String metadataJson = baos.toString("UTF-8")
                    metadata = new JsonSlurper().parseText(metadataJson)
                }
            }
        } finally {
            zipIn.close()
        }
        
        if (!jobXml || !metadata) {
            throw new IllegalArgumentException("Invalid archive file: missing job.xml or metadata.json")
        }
        
        return [jobXml: jobXml, metadata: metadata]
    }
    
    /**
     * Gets or creates a folder path.
     * 
     * @param path Folder path (e.g., "folder1/folder2")
     * @return Folder item
     */
    private Object getOrCreateFolder(String path) {
        String[] parts = path.split("/")
        def parent = jenkins
        
        StringBuilder currentPath = new StringBuilder()
        
        for (String part : parts) {
            if (currentPath.length() > 0) {
                currentPath.append("/")
            }
            currentPath.append(part)
            
            AbstractItem item = jenkins.getItemByFullName(currentPath.toString(), AbstractItem.class)
            
            if (item != null) {
                parent = item
            } else {
                // Create folder
                ByteArrayInputStream folderXml = new ByteArrayInputStream(
                    "<com.cloudbees.hudson.plugins.folder.Folder/>".getBytes("UTF-8"))
                
                parent = parent.createProject(jenkins.getDescriptor("com.cloudbees.hudson.plugins.folder.Folder"), 
                                             part, true, folderXml)
                LOGGER.info("Created folder: ${currentPath}")
            }
        }
        
        return parent
    }
    
    /**
     * Gets the parent path from a full job name.
     * 
     * @param fullName Full job name
     * @return Parent path or empty string
     */
    private String getParentPath(String fullName) {
        int lastSlash = fullName.lastIndexOf('/')
        return (lastSlash != -1) ? fullName.substring(0, lastSlash) : ""
    }
    
    /**
     * Gets the simple name from a full job name.
     * 
     * @param fullName Full job name
     * @return Simple name
     */
    private String getSimpleName(String fullName) {
        int lastSlash = fullName.lastIndexOf('/')
        return (lastSlash != -1) ? fullName.substring(lastSlash + 1) : fullName
    }
    
    /**
     * Parses a date string.
     * 
     * @param dateStr Date string
     * @return Parsed date or current date if parsing fails
     */
    private Date parseDate(String dateStr) {
        if (!dateStr) {
            LOGGER.warning("Timestamp string is null or empty in archive metadata, using current date as fallback.")
            return new Date() // Or handle as an error / null
        }

        try {
            // Use the defined SimpleDateFormat to parse
            return METADATA_TIMESTAMP_FORMAT.get().parse(dateStr)
        } catch (ParseException e) { // Be specific with exception for known format
            LOGGER.log(Level.WARNING, "Error parsing date string '${dateStr}' with standard format: ${e.getMessage()}. Attempting fallback.", e)
            // Attempt fallback for old format (Date.toString()) - this is best-effort
            try {
                return new Date(dateStr) // Deprecated, but for existing data
            } catch (IllegalArgumentException iae) {
                LOGGER.log(Level.WARNING, "Fallback parsing of date string '${dateStr}' also failed: ${iae.getMessage()}. Using current date.", iae)
                return new Date()
            }
        } catch (Exception e) { // Catch other unexpected exceptions during parsing
            LOGGER.log(Level.WARNING, "Unexpected error parsing date string '${dateStr}': ${e.getMessage()}. Using current date as fallback.", e)
            return new Date()
        }
    }
}

/**
 * Represents a job archive.
 */
class JobArchive {
    String jobName
    String archivePath
    Date timestamp
    String reason
    Map<String, String> metadata
}