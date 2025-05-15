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
import java.io.File
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import groovy.xml.XmlUtil
import groovy.xml.XmlParser
import groovy.xml.XmlSlurper
import groovy.xml.SlurperConfiguration

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Creates and manages Jenkins job templates.
 * 
 * Provides methods to create standardized job templates based on organizational best practices.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobTemplate {
    private static final Logger LOGGER = Logger.getLogger(JobTemplate.class.getName())
    
    private final Jenkins jenkins
    private final Map<String, String> parameters
    private String templateXml
    
    /**
     * Creates a JobTemplate with specified parameters.
     * 
     * ```groovy
     * def template = new JobTemplate(
     *     jenkins: Jenkins.get(),
     *     templateXml: existingJobTemplateXml,
     *     parameters: [
     *         'PROJECT_NAME': 'frontend-app',
     *         'GIT_URL': 'https://github.com/myorg/frontend-app.git',
     *         'CREDENTIALS_ID': 'github-creds'
     *     ]
     * )
     * ```
     * 
     * @param jenkins Jenkins instance
     * @param templateXml XML template string or null to extract from templateJobName
     * @param templateJobName Name of job to use as template
     * @param parameters Map of parameters to use when applying template
     */
    JobTemplate(Jenkins jenkins, String templateXml = null, String templateJobName = null, Map<String, String> parameters = [:]) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        this.parameters = parameters ?: [:]
        
        if (templateXml) {
            this.templateXml = templateXml
        } else if (templateJobName) {
            this.templateXml = extractTemplateFromJob(templateJobName)
        } else {
            throw new IllegalArgumentException("Either templateXml or templateJobName must be provided")
        }
    }
    
    /**
     * Applies the template to create or update a job.
     * 
     * ```groovy
     * // Create a new job from template
     * template.applyTemplate("new-project-pipeline")
     * 
     * // Update existing job with template
     * template.applyTemplate("existing-project", true)
     * ```
     * 
     * @param jobName Name of job to create or update
     * @param allowOverwrite Whether to overwrite existing job
     * @return true if successful, false otherwise
     */
    boolean applyTemplate(String jobName, boolean allowOverwrite = false) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        return ErrorHandler.withErrorHandling("applying template to job ${jobName}", {
            // Check if job already exists
            Job existingJob = jenkins.getItemByFullName(jobName, Job.class)
            if (existingJob && !allowOverwrite) {
                LOGGER.warning("Job ${jobName} already exists and allowOverwrite is false")
                return false
            }
            
            // Apply parameters to template
            String jobXml = applyParameters(templateXml)
            
            // Create or update job
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jobXml.getBytes("UTF-8"))
            
            if (existingJob) {
                existingJob.updateByXml(inputStream)
                LOGGER.info("Updated existing job ${jobName} with template")
            } else {
                // Handle possible parent folders
                String parentPath = getParentPath(jobName)
                def parent
                
                if (parentPath) {
                    parent = jenkins.getItemByFullName(parentPath)
                    if (!parent || !parent.hasPermission(com.cloudbees.hudson.plugins.folder.Folder.CREATE)) {
                        LOGGER.warning("Parent folder ${parentPath} not found or insufficient permissions")
                        return false
                    }
                } else {
                    parent = jenkins
                }
                
                String jobSimpleName = getSimpleName(jobName)
                parent.createProjectFromXML(jobSimpleName, inputStream)
                LOGGER.info("Created new job ${jobName} from template")
            }
            
            return true
        }, LOGGER, false)
    }
    
    /**
     * Creates or updates multiple jobs from the template.
     * 
     * ```groovy
     * // Create jobs with parameters
     * def parametersList = [
     *     ['PROJECT_NAME': 'frontend', 'GIT_BRANCH': 'main'],
     *     ['PROJECT_NAME': 'backend', 'GIT_BRANCH': 'develop']
     * ]
     * 
     * def jobs = template.applyTemplateToJobs(
     *     ['frontend-pipeline', 'backend-pipeline'],
     *     parametersList,
     *     true
     * )
     * ```
     * 
     * @param jobNames List of job names to create or update
     * @param parametersList List of parameter maps, one per job
     * @param allowOverwrite Whether to overwrite existing jobs
     * @return List of successfully created/updated job names
     */
    List<String> applyTemplateToJobs(List<String> jobNames, List<Map<String, String>> parametersList = null, boolean allowOverwrite = false) {
        if (!jobNames) {
            LOGGER.warning("No job names provided")
            return []
        }
        
        if (parametersList && jobNames.size() != parametersList.size()) {
            LOGGER.warning("Job names count (${jobNames.size()}) doesn't match parameters list count (${parametersList.size()})")
            return []
        }
        
        List<String> successfulJobs = []
        
        jobNames.eachWithIndex { jobName, index ->
            // Use job-specific parameters if provided
            Map<String, String> jobParameters = parametersList ? parametersList[index] : parameters
            
            // Save original parameters
            Map<String, String> originalParameters = parameters.clone()
            
            // Apply job-specific parameters
            if (jobParameters) {
                parameters.clear()
                parameters.putAll(jobParameters)
            }
            
            boolean success = applyTemplate(jobName, allowOverwrite)
            if (success) {
                successfulJobs.add(jobName)
            }
            
            // Restore original parameters
            parameters.clear()
            parameters.putAll(originalParameters)
        }
        
        LOGGER.info("Applied template to ${successfulJobs.size()} of ${jobNames.size()} jobs")
        return successfulJobs
    }
    
    /**
     * Saves the template to a file.
     * 
     * ```groovy
     * // Save template for external use
     * template.saveTemplateToFile("/path/to/templates/pipeline-template.xml")
     * ```
     * 
     * @param filePath Path to save template to
     * @return true if successful, false otherwise
     */
    boolean saveTemplateToFile(String filePath) {
        filePath = ValidationUtils.requireNonEmpty(filePath, "File path")
        
        return ErrorHandler.withErrorHandling("saving template to file ${filePath}", {
            File file = new File(filePath)
            file.parentFile?.mkdirs()
            file.text = templateXml
            LOGGER.info("Template saved to ${filePath}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Loads the template from a file.
     * 
     * ```groovy
     * // Load template from file
     * template.loadTemplateFromFile("/path/to/templates/pipeline-template.xml")
     * ```
     * 
     * @param filePath Path to load template from
     * @return true if successful, false otherwise
     */
    boolean loadTemplateFromFile(String filePath) {
        filePath = ValidationUtils.requireNonEmpty(filePath, "File path")
        
        return ErrorHandler.withErrorHandling("loading template from file ${filePath}", {
            File file = new File(filePath)
            if (!file.exists()) {
                LOGGER.warning("Template file not found: ${filePath}")
                return false
            }
            
            templateXml = file.text
            LOGGER.info("Template loaded from ${filePath}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Extracts a template from an existing job.
     * 
     * @param jobName Name of job to extract template from
     * @return XML template
     */
    private String extractTemplateFromJob(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        return ErrorHandler.withErrorHandling("extracting template from job ${jobName}", {
            Job job = jenkins.getItemByFullName(jobName, Job.class)
            if (!job) {
                throw new IllegalArgumentException("Job not found: ${jobName}")
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            job.writeConfigDotXml(outputStream)
            return outputStream.toString("UTF-8")
        }, LOGGER)
    }
    
    /**
     * Applies parameters to the template.
     * 
     * @param template XML template
     * @return Processed XML
     */
    private String applyParameters(String template) {
        if (!parameters || parameters.isEmpty()) {
            return template
        }
        
        String processed = template
        parameters.each { key, value ->
            processed = processed.replace('${' + key + '}', value)
        }
        
        return processed
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