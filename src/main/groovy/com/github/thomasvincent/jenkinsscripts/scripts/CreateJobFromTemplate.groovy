#!/usr/bin/env groovy

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

import com.github.thomasvincent.jenkinsscripts.jobs.JobTemplate
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonSlurper

/**
 * Creates or updates Jenkins jobs from templates.
 * 
 * '''Usage:'''
 * ```groovy
 * # Create from existing job as template
 * ./CreateJobFromTemplate.groovy --template-job my-template-job --job new-job --param PROJECT_NAME=frontend --param GIT_URL=https://github.com/org/frontend.git
 * 
 * # Create from template file
 * ./CreateJobFromTemplate.groovy --template-file /path/to/template.xml --job new-job
 * 
 * # Create multiple jobs with parameters from JSON file
 * ./CreateJobFromTemplate.groovy --template-job my-template-job --jobs-file jobs.json
 * 
 * # Show help
 * ./CreateJobFromTemplate.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy CreateJobFromTemplate [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    tj(longOpt: 'template-job', args: 1, argName: 'jobName', 'Name of existing job to use as template')
    tf(longOpt: 'template-file', args: 1, argName: 'filePath', 'Path to template XML file')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Name of job to create/update')
    jf(longOpt: 'jobs-file', args: 1, argName: 'filePath', 'Path to JSON file with multiple jobs configuration')
    p(longOpt: 'param', args: 2, valueSeparator: '=', argName: 'name=value', 'Parameter to replace in template (can be used multiple times)')
    o(longOpt: 'overwrite', 'Allow overwriting existing jobs')
    s(longOpt: 'save-template', args: 1, argName: 'filePath', 'Save template to file')
}

// Parse the command line
def options = cli.parse(args)
if (!options) {
    return
}

// Show help and exit if requested
if (options.h) {
    cli.usage()
    return
}

// Validate required options
if (!options.tj && !options.tf) {
    println "Error: Either template job (--template-job) or template file (--template-file) is required"
    cli.usage()
    return
}

if (!options.j && !options.jf) {
    println "Error: Either job name (--job) or jobs file (--jobs-file) is required"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Parse parameters
def parameters = [:]
if (options.ps) {
    for (int i = 0; i < options.ps.length; i += 2) {
        if (i + 1 < options.ps.length) {
            parameters[options.ps[i]] = options.ps[i + 1]
        }
    }
}
println "Parameters: ${parameters.size() > 0 ? parameters : 'none'}"

// Create template
JobTemplate template

if (options.tj) {
    // Use existing job as template
    def templateJobName = options.tj
    println "Using job as template: ${templateJobName}"
    template = new JobTemplate(jenkins, null, templateJobName, parameters)
} else {
    // Use template file
    def templateFile = options.tf
    println "Using template file: ${templateFile}"
    
    try {
        String templateXml = new File(templateFile).text
        template = new JobTemplate(jenkins, templateXml, null, parameters)
    } catch (Exception e) {
        println "Error loading template file: ${e.message}"
        return
    }
}

// Save template if requested
if (options.s) {
    def templateFilePath = options.s
    println "Saving template to: ${templateFilePath}"
    
    if (template.saveTemplateToFile(templateFilePath)) {
        println "Template saved successfully"
    } else {
        println "Failed to save template"
    }
}

// Allow overwriting existing jobs?
def allowOverwrite = options.o ?: false
if (allowOverwrite) {
    println "Overwriting existing jobs is enabled"
}

// Apply template to jobs
if (options.j) {
    // Single job
    def jobName = options.j
    println "Creating/updating job: ${jobName}"
    
    if (template.applyTemplate(jobName, allowOverwrite)) {
        println "Job created/updated successfully"
    } else {
        println "Failed to create/update job"
    }
} else if (options.jf) {
    // Multiple jobs from file
    def jobsFile = options.jf
    println "Creating/updating jobs from file: ${jobsFile}"
    
    try {
        def jobsConfig = new JsonSlurper().parse(new File(jobsFile))
        
        if (!jobsConfig.jobs) {
            println "Error: JSON file must contain a 'jobs' array"
            return
        }
        
        def jobNames = []
        def parametersList = []
        
        jobsConfig.jobs.each { job ->
            if (!job.name) {
                println "Warning: Skipping job without name"
                return
            }
            
            jobNames.add(job.name)
            parametersList.add(job.parameters ?: [:])
        }
        
        def createdJobs = template.applyTemplateToJobs(jobNames, parametersList, allowOverwrite)
        
        println "Jobs creation summary:"
        println "- Total jobs requested: ${jobNames.size()}"
        println "- Successfully created/updated: ${createdJobs.size()}"
        
        if (createdJobs.size() > 0) {
            println "- Created/updated jobs:"
            createdJobs.each { jobName ->
                println "  * ${jobName}"
            }
        }
        
        if (createdJobs.size() < jobNames.size()) {
            println "- Failed jobs:"
            jobNames.findAll { !createdJobs.contains(it) }.each { jobName ->
                println "  * ${jobName}"
            }
        }
    } catch (Exception e) {
        println "Error processing jobs file: ${e.message}"
    }
}