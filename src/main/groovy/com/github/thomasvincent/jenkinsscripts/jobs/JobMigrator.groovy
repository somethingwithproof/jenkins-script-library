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
import hudson.model.TopLevelItem
import hudson.model.AbstractItem
import jenkins.model.ModifiableTopLevelItemGroup
import java.io.File
import java.io.InputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayInputStream
import groovy.xml.XmlUtil

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Migrates Jenkins jobs between Jenkins instances.
 * 
 * Supports copying job configurations with optional modifications during migration.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobMigrator {
    private static final Logger LOGGER = Logger.getLogger(JobMigrator.class.getName())
    
    private final Jenkins sourceJenkins
    private final Jenkins targetJenkins
    private final Map<String, String> propertyReplacements
    
    /**
     * Creates a JobMigrator for copying jobs between Jenkins instances.
     * 
     * ```groovy
     * def migrator = new JobMigrator(
     *     sourceJenkins: Jenkins.get(),
     *     targetJenkins: remoteJenkins,
     *     [
     *         'env.PROD_SERVER': 'env.STAGING_SERVER',
     *         'credentialsId-prod': 'credentialsId-staging'
     *     ]
     * )
     * ```
     * 
     * @param sourceJenkins Source Jenkins instance
     * @param targetJenkins Target Jenkins instance
     * @param propertyReplacements Map of strings to replace in configurations (optional)
     */
    JobMigrator(Jenkins sourceJenkins, Jenkins targetJenkins, Map<String, String> propertyReplacements = [:]) {
        this.sourceJenkins = ValidationUtils.requireNonNull(sourceJenkins, "Source Jenkins instance")
        this.targetJenkins = ValidationUtils.requireNonNull(targetJenkins, "Target Jenkins instance")
        this.propertyReplacements = propertyReplacements ?: [:]
    }
    
    /**
     * Migrates a job from source to target Jenkins instance.
     * 
     * ```groovy
     * // Basic migration keeping the same name
     * migrator.migrateJob("deployment-job")
     * 
     * // Migration with a new name on the target
     * migrator.migrateJob("prod-deploy", "staging-deploy")
     * 
     * // Migration with properties customization
     * migrator.migrateJob("prod-deploy", "staging-deploy", true)
     * ```
     * 
     * @param sourceJobName Source job name
     * @param targetJobName Target job name (defaults to source name)
     * @param applyReplacements Whether to apply property replacements
     * @return true if migration succeeded, false otherwise
     */
    boolean migrateJob(String sourceJobName, String targetJobName = null, boolean applyReplacements = false) {
        sourceJobName = ValidationUtils.requireNonEmpty(sourceJobName, "Source job name")
        targetJobName = targetJobName ?: sourceJobName
        
        Job sourceJob = sourceJenkins.getItemByFullName(sourceJobName, Job.class)
        if (sourceJob == null) {
            LOGGER.warning("Source job not found: ${sourceJobName}")
            return false
        }
        
        return ErrorHandler.withErrorHandling("migrating job ${sourceJobName} to ${targetJobName}", {
            // Get job XML configuration
            String jobXml = extractJobConfig(sourceJob)
            
            // Apply replacements if requested
            if (applyReplacements && !propertyReplacements.isEmpty()) {
                jobXml = applyPropertyReplacements(jobXml)
            }
            
            // Create or update job on target Jenkins
            return createOrUpdateJob(targetJobName, jobXml)
        }, LOGGER, false)
    }
    
    /**
     * Migrates multiple jobs matching a pattern.
     * 
     * ```groovy
     * // Migrate all jobs with a prefix
     * def migrated = migrator.migrateJobs("prod-.*", "staging-", true)
     * println "Migrated ${migrated.size()} jobs"
     * ```
     * 
     * @param pattern Regular expression pattern to match source job names
     * @param targetPrefix Prefix to add to job names on target (or null to keep names)
     * @param applyReplacements Whether to apply property replacements
     * @return Map of migrated jobs (source name to target name)
     */
    Map<String, String> migrateJobs(String pattern, String targetPrefix = null, boolean applyReplacements = false) {
        pattern = ValidationUtils.requireNonEmpty(pattern, "Job pattern")
        Pattern jobPattern = Pattern.compile(pattern)
        
        Map<String, String> migratedJobs = [:]
        List<Job> allJobs = sourceJenkins.getAllItems(Job.class)
        
        allJobs.each { job ->
            String jobName = job.fullName
            if (jobPattern.matcher(jobName).matches()) {
                String targetName = targetPrefix ? "${targetPrefix}${jobName}" : jobName
                boolean success = migrateJob(jobName, targetName, applyReplacements)
                if (success) {
                    migratedJobs[jobName] = targetName
                }
            }
        }
        
        LOGGER.info("Migrated ${migratedJobs.size()} jobs matching pattern '${pattern}'")
        return migratedJobs
    }
    
    /**
     * Extracts XML configuration from a job.
     * 
     * @param job Job to extract configuration from
     * @return XML configuration as string
     */
    private String extractJobConfig(Job job) {
        ValidationUtils.requireNonNull(job, "Job instance")
        
        return ErrorHandler.withErrorHandling("extracting job config for ${job.fullName}", {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            job.writeConfigDotXml(outputStream)
            return outputStream.toString("UTF-8")
        }, LOGGER)
    }
    
    /**
     * Applies property replacements to job XML.
     * 
     * @param jobXml Job XML configuration
     * @return Modified job XML
     */
    private String applyPropertyReplacements(String jobXml) {
        ValidationUtils.requireNonNull(jobXml, "Job XML configuration")
        
        String modifiedXml = jobXml
        propertyReplacements.each { oldValue, newValue ->
            modifiedXml = modifiedXml.replace(oldValue, newValue)
        }
        
        return modifiedXml
    }
    
    /**
     * Creates or updates a job on the target Jenkins.
     * 
     * @param jobName Target job name
     * @param jobXml Job XML configuration
     * @return true if successful, false otherwise
     */
    private boolean createOrUpdateJob(String jobName, String jobXml) {
        ValidationUtils.requireNonEmpty(jobName, "Job name")
        ValidationUtils.requireNonNull(jobXml, "Job XML configuration")
        
        return ErrorHandler.withErrorHandling("creating or updating job ${jobName}", {
            // Check if job already exists
            Job existingJob = targetJenkins.getItemByFullName(jobName, Job.class)
            
            if (existingJob) {
                // Update existing job
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                existingJob.updateByXml(inputStream)
                LOGGER.info("Updated existing job: ${jobName}")
                return true
            } else {
                // Handle possible parent folders
                String parentPath = getParentPath(jobName)
                ModifiableTopLevelItemGroup parent
                
                if (parentPath) {
                    // Ensure parent folders exist
                    parent = getOrCreateFolder(parentPath)
                } else {
                    parent = targetJenkins
                }
                
                // Create new job
                String jobSimpleName = getSimpleName(jobName)
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
                parent.createProjectFromXML(jobSimpleName, inputStream)
                LOGGER.info("Created new job: ${jobName}")
                return true
            }
        }, LOGGER, false)
    }
    
    /**
     * Gets or creates a folder path.
     * 
     * @param path Folder path (e.g., "folder1/folder2")
     * @return Folder item
     */
    private ModifiableTopLevelItemGroup getOrCreateFolder(String path) {
        String[] parts = path.split("/")
        ModifiableTopLevelItemGroup parent = targetJenkins
        
        StringBuilder currentPath = new StringBuilder()
        
        for (String part : parts) {
            if (currentPath.length() > 0) {
                currentPath.append("/")
            }
            currentPath.append(part)
            
            AbstractItem item = targetJenkins.getItemByFullName(currentPath.toString(), AbstractItem.class)
            
            if (item instanceof ModifiableTopLevelItemGroup) {
                parent = (ModifiableTopLevelItemGroup) item
            } else {
                // Create folder
                ByteArrayInputStream folderXml = new ByteArrayInputStream(
                    "<com.cloudbees.hudson.plugins.folder.Folder/>".getBytes("UTF-8"))
                    
                parent = parent.createProjectFromXML(part, folderXml) as ModifiableTopLevelItemGroup
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
}