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
import hudson.model.ParametersDefinitionProperty
import hudson.model.ParameterDefinition
import hudson.model.StringParameterDefinition
import hudson.model.TextParameterDefinition
import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import groovy.xml.XmlUtil
import groovy.xml.XmlParser

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Manages and standardizes Jenkins job parameters.
 * 
 * Provides methods to inventory, validate, standardize, and modify job parameters.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobParameterManager {
    private static final Logger LOGGER = Logger.getLogger(JobParameterManager.class.getName())
    
    private final Jenkins jenkins
    private Map<String, ParameterStandard> parameterStandards = [:]
    
    /**
     * Creates a JobParameterManager instance.
     * 
     * ```groovy
     * def manager = new JobParameterManager(Jenkins.get())
     * ```
     * 
     * @param jenkins Jenkins instance
     */
    JobParameterManager(Jenkins jenkins) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
    }
    
    /**
     * Inventories parameters across all jobs.
     * 
     * ```groovy
     * def inventory = manager.inventoryParameters()
     * println "Found ${inventory.size()} unique parameters"
     * ```
     * 
     * @param pattern Optional regex pattern to filter job names
     * @return Map of parameter names to parameter usage statistics
     */
    Map<String, ParameterUsage> inventoryParameters(String pattern = null) {
        Pattern jobPattern = pattern ? Pattern.compile(pattern) : null
        Map<String, ParameterUsage> inventory = [:]
        
        jenkins.getAllItems(Job.class).each { job ->
            // Skip jobs that don't match pattern
            if (jobPattern && !jobPattern.matcher(job.fullName).matches()) {
                return
            }
            
            // Get parameters for this job
            List<ParameterDefinition> params = getJobParameters(job)
            
            // Update inventory
            params.each { param ->
                String name = param.name
                
                if (!inventory.containsKey(name)) {
                    inventory[name] = new ParameterUsage(name, param.type.simpleName)
                }
                
                ParameterUsage usage = inventory[name]
                usage.addJob(job.fullName)
                
                // Record parameter type
                usage.addType(param.type.simpleName)
                
                // Record default value
                if (param.defaultValue != null) {
                    usage.addDefaultValue(param.defaultValue.toString())
                }
                
                // Record additional type-specific info
                if (param instanceof ChoiceParameterDefinition) {
                    usage.addChoices(param.choices)
                }
            }
        }
        
        LOGGER.info("Inventoried ${inventory.size()} parameters across Jenkins jobs")
        return inventory
    }
    
    /**
     * Defines a parameter standard.
     * 
     * ```groovy
     * manager.defineParameterStandard(
     *     "BRANCH_NAME",
     *     "StringParameterDefinition",
     *     "Branch to build",
     *     "main",
     *     true    // standardize existing parameters
     * )
     * ```
     * 
     * @param name Parameter name
     * @param type Parameter type class name
     * @param description Standard description
     * @param defaultValue Standard default value
     * @param standardize Whether to standardize existing parameters
     * @param additionalProps Additional properties for specific types
     */
    void defineParameterStandard(String name, String type, String description, Object defaultValue, 
                                 boolean standardize = false, Map additionalProps = [:]) {
        name = ValidationUtils.requireNonEmpty(name, "Parameter name")
        type = ValidationUtils.requireNonEmpty(type, "Parameter type")
        
        ParameterStandard standard = new ParameterStandard(
            name: name,
            type: type,
            description: description,
            defaultValue: defaultValue,
            additionalProps: additionalProps
        )
        
        parameterStandards[name] = standard
        LOGGER.info("Defined parameter standard for ${name} (${type})")
        
        if (standardize) {
            standardizeParameter(name)
        }
    }
    
    /**
     * Gets all defined parameter standards.
     * 
     * ```groovy
     * def standards = manager.getParameterStandards()
     * standards.each { name, standard ->
     *     println "${name}: ${standard.type}"
     * }
     * ```
     * 
     * @return Map of parameter names to standards
     */
    Map<String, ParameterStandard> getParameterStandards() {
        return new HashMap<>(parameterStandards)
    }
    
    /**
     * Adds a parameter to a job.
     * 
     * ```groovy
     * manager.addParameter(
     *     "my-pipeline",
     *     "DEPLOY_ENV",
     *     "ChoiceParameterDefinition",
     *     "Environment to deploy to",
     *     "dev",
     *     [choices: ["dev", "test", "prod"]]
     * )
     * ```
     * 
     * @param jobName Name of job to add parameter to
     * @param name Parameter name
     * @param type Parameter type class name
     * @param description Parameter description
     * @param defaultValue Default value
     * @param additionalProps Additional properties for specific types
     * @return true if successful, false otherwise
     */
    boolean addParameter(String jobName, String name, String type, String description, 
                         Object defaultValue, Map additionalProps = [:]) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        name = ValidationUtils.requireNonEmpty(name, "Parameter name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (!job) {
            LOGGER.warning("Job not found: ${jobName}")
            return false
        }
        
        return ErrorHandler.withErrorHandling("adding parameter ${name} to job ${jobName}", {
            // Get existing parameters
            ParametersDefinitionProperty paramProperty = job.getProperty(ParametersDefinitionProperty.class)
            List<ParameterDefinition> parameters = []
            
            if (paramProperty) {
                parameters.addAll(paramProperty.getParameterDefinitions())
                
                // Check if parameter already exists
                if (parameters.any { it.name == name }) {
                    LOGGER.warning("Parameter ${name} already exists in job ${jobName}")
                    return false
                }
            }
            
            // Create new parameter
            ParameterDefinition newParam = createParameter(name, type, description, defaultValue, additionalProps)
            if (!newParam) {
                LOGGER.warning("Failed to create parameter of type ${type}")
                return false
            }
            
            // Add parameter to list
            parameters.add(newParam)
            
            // Update job
            job.removeProperty(ParametersDefinitionProperty.class)
            job.addProperty(new ParametersDefinitionProperty(parameters))
            
            LOGGER.info("Added parameter ${name} to job ${jobName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Updates a parameter in a job.
     * 
     * ```groovy
     * manager.updateParameter(
     *     "my-pipeline",
     *     "DEPLOY_ENV",
     *     "Updated description",
     *     "test",  // new default
     *     [choices: ["dev", "test", "prod", "staging"]]  // updated choices
     * )
     * ```
     * 
     * @param jobName Name of job to update parameter in
     * @param name Parameter name to update
     * @param description New description (or null to keep current)
     * @param defaultValue New default value (or null to keep current)
     * @param additionalProps New additional properties (or empty to keep current)
     * @return true if successful, false otherwise
     */
    boolean updateParameter(String jobName, String name, String description = null, 
                            Object defaultValue = null, Map additionalProps = [:]) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        name = ValidationUtils.requireNonEmpty(name, "Parameter name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (!job) {
            LOGGER.warning("Job not found: ${jobName}")
            return false
        }
        
        return ErrorHandler.withErrorHandling("updating parameter ${name} in job ${jobName}", {
            // Get existing parameters
            ParametersDefinitionProperty paramProperty = job.getProperty(ParametersDefinitionProperty.class)
            if (!paramProperty) {
                LOGGER.warning("Job ${jobName} has no parameters")
                return false
            }
            
            List<ParameterDefinition> parameters = new ArrayList<>(paramProperty.getParameterDefinitions())
            
            // Find parameter to update
            int index = -1
            ParameterDefinition existingParam = null
            
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters[i].name == name) {
                    index = i
                    existingParam = parameters[i]
                    break
                }
            }
            
            if (index == -1) {
                LOGGER.warning("Parameter ${name} not found in job ${jobName}")
                return false
            }
            
            // Create updated parameter
            String type = existingParam.getClass().simpleName
            String newDescription = description ?: existingParam.description
            Object newDefault = defaultValue != null ? defaultValue : existingParam.defaultValue
            
            // Merge additional properties
            Map mergedProps = [:]
            
            if (existingParam instanceof ChoiceParameterDefinition) {
                mergedProps.choices = existingParam.choices
            }
            
            // Override with new properties
            mergedProps.putAll(additionalProps)
            
            ParameterDefinition updatedParam = createParameter(name, type, newDescription, newDefault, mergedProps)
            if (!updatedParam) {
                LOGGER.warning("Failed to create updated parameter")
                return false
            }
            
            // Replace parameter
            parameters.set(index, updatedParam)
            
            // Update job
            job.removeProperty(ParametersDefinitionProperty.class)
            job.addProperty(new ParametersDefinitionProperty(parameters))
            
            LOGGER.info("Updated parameter ${name} in job ${jobName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Removes a parameter from a job.
     * 
     * ```groovy
     * manager.removeParameter("my-pipeline", "OBSOLETE_PARAM")
     * ```
     * 
     * @param jobName Name of job to remove parameter from
     * @param name Parameter name to remove
     * @return true if successful, false otherwise
     */
    boolean removeParameter(String jobName, String name) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        name = ValidationUtils.requireNonEmpty(name, "Parameter name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (!job) {
            LOGGER.warning("Job not found: ${jobName}")
            return false
        }
        
        return ErrorHandler.withErrorHandling("removing parameter ${name} from job ${jobName}", {
            // Get existing parameters
            ParametersDefinitionProperty paramProperty = job.getProperty(ParametersDefinitionProperty.class)
            if (!paramProperty) {
                LOGGER.warning("Job ${jobName} has no parameters")
                return false
            }
            
            List<ParameterDefinition> parameters = new ArrayList<>(paramProperty.getParameterDefinitions())
            
            // Find parameter to remove
            int initialSize = parameters.size()
            parameters.removeAll { it.name == name }
            
            if (parameters.size() == initialSize) {
                LOGGER.warning("Parameter ${name} not found in job ${jobName}")
                return false
            }
            
            // Update job
            job.removeProperty(ParametersDefinitionProperty.class)
            if (!parameters.isEmpty()) {
                job.addProperty(new ParametersDefinitionProperty(parameters))
            }
            
            LOGGER.info("Removed parameter ${name} from job ${jobName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Standardizes a parameter across all jobs.
     * 
     * ```groovy
     * int updated = manager.standardizeParameter("BRANCH_NAME")
     * println "Updated parameter in ${updated} jobs"
     * ```
     * 
     * @param name Parameter name to standardize
     * @param pattern Optional regex pattern to filter job names
     * @return Number of jobs updated
     */
    int standardizeParameter(String name, String pattern = null) {
        name = ValidationUtils.requireNonEmpty(name, "Parameter name")
        
        ParameterStandard standard = parameterStandards[name]
        if (!standard) {
            LOGGER.warning("No standard defined for parameter ${name}")
            return 0
        }
        
        Pattern jobPattern = pattern ? Pattern.compile(pattern) : null
        int updatedCount = 0
        
        jenkins.getAllItems(Job.class).each { job ->
            // Skip jobs that don't match pattern
            if (jobPattern && !jobPattern.matcher(job.fullName).matches()) {
                return
            }
            
            // Check if job has this parameter
            ParametersDefinitionProperty paramProperty = job.getProperty(ParametersDefinitionProperty.class)
            if (!paramProperty) {
                return
            }
            
            List<ParameterDefinition> parameters = paramProperty.getParameterDefinitions()
            boolean hasParam = parameters.any { it.name == name }
            
            if (hasParam) {
                // Update parameter to match standard
                boolean updated = updateParameter(
                    job.fullName, 
                    name, 
                    standard.description, 
                    standard.defaultValue, 
                    standard.additionalProps
                )
                
                if (updated) {
                    updatedCount++
                }
            }
        }
        
        LOGGER.info("Standardized parameter ${name} in ${updatedCount} jobs")
        return updatedCount
    }
    
    /**
     * Renames a parameter across all jobs.
     * 
     * ```groovy
     * int updated = manager.renameParameter("old_param", "NEW_PARAM")
     * println "Renamed parameter in ${updated} jobs"
     * ```
     * 
     * @param oldName Old parameter name
     * @param newName New parameter name
     * @param pattern Optional regex pattern to filter job names
     * @return Number of jobs updated
     */
    int renameParameter(String oldName, String newName, String pattern = null) {
        oldName = ValidationUtils.requireNonEmpty(oldName, "Old parameter name")
        newName = ValidationUtils.requireNonEmpty(newName, "New parameter name")
        
        if (oldName == newName) {
            LOGGER.warning("Old and new parameter names are the same")
            return 0
        }
        
        Pattern jobPattern = pattern ? Pattern.compile(pattern) : null
        int updatedCount = 0
        
        jenkins.getAllItems(Job.class).each { job ->
            // Skip jobs that don't match pattern
            if (jobPattern && !jobPattern.matcher(job.fullName).matches()) {
                return
            }
            
            boolean updated = ErrorHandler.withErrorHandling("renaming parameter in job ${job.fullName}", {
                boolean jobUpdated = false
                
                // Get raw XML config
                String jobXml = getJobXml(job)
                
                // Use regex to rename parameters in XML
                String paramRx = "(<hudson.model.\\w+ParameterDefinition>\\s*<name>)" + 
                                Pattern.quote(oldName) + "(</name>)"
                String newXml = jobXml.replaceAll(paramRx, "\$1${newName}\$2")
                
                // Check if anything was changed
                if (newXml != jobXml) {
                    // Update job config
                    updateJobFromXml(job, newXml)
                    jobUpdated = true
                }
                
                return jobUpdated
            }, LOGGER, false)
            
            if (updated) {
                updatedCount++
            }
        }
        
        LOGGER.info("Renamed parameter ${oldName} to ${newName} in ${updatedCount} jobs")
        return updatedCount
    }
    
    /**
     * Gets the parameters for a job.
     * 
     * @param job Job to get parameters for
     * @return List of parameter definitions
     */
    private List<ParameterDefinition> getJobParameters(Job job) {
        List<ParameterDefinition> params = []
        
        ParametersDefinitionProperty paramProperty = job.getProperty(ParametersDefinitionProperty.class)
        if (paramProperty) {
            params.addAll(paramProperty.getParameterDefinitions())
        }
        
        return params
    }
    
    /**
     * Creates a parameter definition based on type.
     * 
     * @param name Parameter name
     * @param type Parameter type class name
     * @param description Parameter description
     * @param defaultValue Default value
     * @param additionalProps Additional properties
     * @return Parameter definition or null if type not supported
     */
    private ParameterDefinition createParameter(String name, String type, String description, 
                                              Object defaultValue, Map additionalProps) {
        switch (type) {
            case "StringParameterDefinition":
                return new StringParameterDefinition(name, defaultValue?.toString() ?: "", description)
                
            case "TextParameterDefinition":
                return new TextParameterDefinition(name, defaultValue?.toString() ?: "", description)
                
            case "BooleanParameterDefinition":
                boolean boolValue = defaultValue instanceof Boolean ? 
                    (Boolean)defaultValue : Boolean.valueOf(defaultValue?.toString() ?: "false")
                return new BooleanParameterDefinition(name, boolValue, description)
                
            case "ChoiceParameterDefinition":
                List<String> choices = additionalProps.choices instanceof List ? 
                    additionalProps.choices : [defaultValue?.toString() ?: ""]
                return new ChoiceParameterDefinition(name, choices as String[], description)
                
            default:
                LOGGER.warning("Unsupported parameter type: ${type}")
                return null
        }
    }
    
    /**
     * Gets XML configuration for a job.
     * 
     * @param job Job to get XML for
     * @return XML configuration as string
     */
    private String getJobXml(Job job) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        job.writeConfigDotXml(outputStream)
        return outputStream.toString("UTF-8")
    }
    
    /**
     * Updates a job from XML configuration.
     * 
     * @param job Job to update
     * @param xml XML configuration
     */
    private void updateJobFromXml(Job job, String xml) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"))
        job.updateByXml(inputStream)
    }
}

/**
 * Standard for a parameter.
 */
class ParameterStandard {
    String name
    String type
    String description
    Object defaultValue
    Map additionalProps = [:]
}

/**
 * Usage statistics for a parameter.
 */
class ParameterUsage {
    String name
    String primaryType
    Set<String> jobs = []
    Set<String> types = []
    Set<String> defaultValues = []
    Set<String> choices = []
    
    ParameterUsage(String name, String primaryType) {
        this.name = name
        this.primaryType = primaryType
        this.types.add(primaryType)
    }
    
    /**
     * Adds a job to the usage statistics.
     * 
     * @param jobName Name of job using this parameter
     */
    void addJob(String jobName) {
        jobs.add(jobName)
    }
    
    /**
     * Adds a type to the usage statistics.
     * 
     * @param type Type of the parameter
     */
    void addType(String type) {
        types.add(type)
    }
    
    /**
     * Adds a default value to the usage statistics.
     * 
     * @param value Default value
     */
    void addDefaultValue(String value) {
        defaultValues.add(value)
    }
    
    /**
     * Adds choices to the usage statistics.
     * 
     * @param newChoices Choices for the parameter
     */
    void addChoices(List<String> newChoices) {
        if (newChoices) {
            choices.addAll(newChoices)
        }
    }
    
    /**
     * Gets the number of jobs using this parameter.
     * 
     * @return Job count
     */
    int getJobCount() {
        return jobs.size()
    }
    
    /**
     * Checks if the parameter has multiple types.
     * 
     * @return true if multiple types exist
     */
    boolean hasMultipleTypes() {
        return types.size() > 1
    }
    
    /**
     * Checks if the parameter has multiple default values.
     * 
     * @return true if multiple default values exist
     */
    boolean hasMultipleDefaultValues() {
        return defaultValues.size() > 1
    }
}