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

import com.github.thomasvincent.jenkinsscripts.jobs.JobParameterManager
import com.github.thomasvincent.jenkinsscripts.jobs.ParameterUsage
import com.github.thomasvincent.jenkinsscripts.jobs.ParameterStandard
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Manages and standardizes Jenkins job parameters.
 * 
 * '''Usage:'''
 * ```groovy
 * # Inventory all parameters
 * ./ManageJobParameters.groovy --inventory
 * 
 * # Add a parameter to a job
 * ./ManageJobParameters.groovy --job my-job --add-param NAME --type StringParameterDefinition --desc "Description" --default "Default Value"
 * 
 * # Add a choice parameter
 * ./ManageJobParameters.groovy --job my-job --add-param ENV --type ChoiceParameterDefinition --desc "Environment" --default "dev" --choices "dev,test,prod"
 * 
 * # Update a parameter
 * ./ManageJobParameters.groovy --job my-job --update-param NAME --desc "New description" --default "New default"
 * 
 * # Remove a parameter
 * ./ManageJobParameters.groovy --job my-job --remove-param NAME
 * 
 * # Define a parameter standard
 * ./ManageJobParameters.groovy --define-standard NAME --type StringParameterDefinition --desc "Standard description" --default "Default value"
 * 
 * # Apply a standard to all matching parameters
 * ./ManageJobParameters.groovy --standardize NAME
 * 
 * # Rename a parameter across all jobs
 * ./ManageJobParameters.groovy --rename old_name --to NEW_NAME
 * 
 * # Show help
 * ./ManageJobParameters.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.2
 */

// Define command line options
def cli = new CliBuilder(usage: 'groovy ManageJobParameters [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    i(longOpt: 'inventory', 'Inventory all job parameters')
    j(longOpt: 'job', args: 1, argName: 'jobName', 'Job to modify')
    p(longOpt: 'pattern', args: 1, argName: 'pattern', 'Pattern to match job names')
    
    ap(longOpt: 'add-param', args: 1, argName: 'name', 'Add a parameter')
    up(longOpt: 'update-param', args: 1, argName: 'name', 'Update a parameter')
    rp(longOpt: 'remove-param', args: 1, argName: 'name', 'Remove a parameter')
    
    t(longOpt: 'type', args: 1, argName: 'type', 'Parameter type (e.g., StringParameterDefinition)')
    d(longOpt: 'desc', args: 1, argName: 'description', 'Parameter description')
    D(longOpt: 'default', args: 1, argName: 'value', 'Parameter default value')
    c(longOpt: 'choices', args: 1, argName: 'values', 'Comma-separated choices for choice parameters')
    
    ds(longOpt: 'define-standard', args: 1, argName: 'name', 'Define a parameter standard')
    s(longOpt: 'standardize', args: 1, argName: 'name', 'Apply standard to parameter')
    
    r(longOpt: 'rename', args: 1, argName: 'oldName', 'Rename a parameter')
    to(longOpt: 'to', args: 1, argName: 'newName', 'New name for parameter (used with --rename)')
    
    sf(longOpt: 'standards-file', args: 1, argName: 'file', 'Load parameter standards from JSON file')
    o(longOpt: 'output', args: 1, argName: 'file', 'Output file for inventory or standards')
    J(longOpt: 'json', 'Output in JSON format')
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

// Validate option combinations
if ((options.ap || options.up) && !options.j) {
    println "Error: --add-param and --update-param require --job to specify which job to modify"
    cli.usage()
    return
}

if (options.r && !options.to) {
    println "Error: --rename requires --to to specify the new parameter name"
    cli.usage()
    return
}

// Get Jenkins instance
def jenkins = Jenkins.get()

// Create parameter manager
def manager = new JobParameterManager(jenkins)

// Load standards from file if specified
if (options.sf) {
    try {
        File standardsFile = new File(options.sf)
        if (standardsFile.exists()) {
            def standards = new JsonSlurper().parse(standardsFile)
            
            standards.each { name, standard ->
                manager.defineParameterStandard(
                    name,
                    standard.type,
                    standard.description,
                    standard.defaultValue,
                    false,
                    standard.additionalProps ?: [:]
                )
            }
            
            println "Loaded ${standards.size()} parameter standards from ${options.sf}"
        } else {
            println "Standards file not found: ${options.sf}"
        }
    } catch (Exception e) {
        println "Error loading standards file: ${e.message}"
    }
}

// Determine output format
boolean jsonFormat = options.J ?: false
String outputFile = options.o

// Execute requested operation
if (options.i) {
    // Inventory parameters
    println "Inventorying parameters across Jenkins jobs..."
    
    Map<String, ParameterUsage> inventory = manager.inventoryParameters(options.p)
    
    if (jsonFormat) {
        def inventoryMap = inventory.collectEntries { name, usage ->
            [(name): [
                type: usage.primaryType,
                jobCount: usage.jobCount,
                multipleTypes: usage.hasMultipleTypes(),
                types: usage.types.toList(),
                defaultValues: usage.defaultValues.toList(),
                choices: usage.choices.toList()
            ]]
        }
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(inventoryMap))
        
        if (outputFile) {
            new File(outputFile).text = json
            println "Inventory saved to ${outputFile}"
        } else {
            println json
        }
    } else {
        StringBuilder sb = new StringBuilder()
        sb.append("Parameter Inventory\n")
        sb.append("==================\n")
        sb.append("Total Parameters: ${inventory.size()}\n\n")
        
        // Group by usage count
        def paramsByUsage = inventory.values().groupBy { it.jobCount }
        def usageCounts = paramsByUsage.keySet().sort { -it }
        
        sb.append("Parameters by Usage:\n")
        usageCounts.each { count ->
            def params = paramsByUsage[count]
            sb.append("- Used in ${count} jobs: ${params.size()} parameters\n")
        }
        
        sb.append("\nParameters with Multiple Types:\n")
        inventory.values().findAll { it.hasMultipleTypes() }.each { usage ->
            sb.append("- ${usage.name}: ${usage.types.join(', ')}\n")
        }
        
        sb.append("\nParameters with Multiple Default Values:\n")
        inventory.values().findAll { it.hasMultipleDefaultValues() }.each { usage ->
            sb.append("- ${usage.name}: ${usage.defaultValues.size()} different values\n")
        }
        
        sb.append("\nMost Common Parameters (top 20):\n")
        inventory.values().sort { -it.jobCount }.take(20).each { usage ->
            sb.append(String.format("- %-30s: %d jobs, type: %s\n", 
                                   usage.name, usage.jobCount, usage.primaryType))
        }
        
        String reportText = sb.toString()
        
        if (outputFile) {
            new File(outputFile).text = reportText
            println "Inventory saved to ${outputFile}"
        } else {
            println reportText
        }
    }
} else if (options.ds) {
    // Define parameter standard
    def paramName = options.ds
    def type = options.t
    def description = options.d
    def defaultValue = options.D
    
    if (!type) {
        println "Error: --type is required with --define-standard"
        cli.usage()
        return
    }
    
    Map additionalProps = [:]
    if (options.c && type == "ChoiceParameterDefinition") {
        additionalProps.choices = options.c.split(",")
    }
    
    println "Defining parameter standard for ${paramName}..."
    
    manager.defineParameterStandard(
        paramName,
        type,
        description ?: "",
        defaultValue,
        false,  // Don't standardize automatically
        additionalProps
    )
    
    println "Parameter standard defined successfully"
    
    // Save all standards to file if requested
    if (outputFile) {
        def standards = manager.getParameterStandards()
        def standardsMap = standards.collectEntries { name, standard ->
            [(name): [
                type: standard.type,
                description: standard.description,
                defaultValue: standard.defaultValue,
                additionalProps: standard.additionalProps
            ]]
        }
        
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(standardsMap))
        new File(outputFile).text = json
        println "All parameter standards saved to ${outputFile}"
    }
} else if (options.s) {
    // Standardize parameter
    def paramName = options.s
    println "Standardizing parameter ${paramName} across jobs..."
    
    int count = manager.standardizeParameter(paramName, options.p)
    
    println "Standardized parameter in ${count} jobs"
} else if (options.r) {
    // Rename parameter
    def oldName = options.r
    def newName = options.to
    println "Renaming parameter ${oldName} to ${newName} across jobs..."
    
    int count = manager.renameParameter(oldName, newName, options.p)
    
    println "Renamed parameter in ${count} jobs"
} else if (options.ap && options.j) {
    // Add parameter to job
    def jobName = options.j
    def paramName = options.ap
    def type = options.t
    def description = options.d ?: ""
    def defaultValue = options.D
    
    if (!type) {
        println "Error: --type is required with --add-param"
        cli.usage()
        return
    }
    
    println "Adding parameter ${paramName} to job ${jobName}..."
    
    Map additionalProps = [:]
    if (options.c && type == "ChoiceParameterDefinition") {
        additionalProps.choices = options.c.split(",")
    }
    
    boolean success = manager.addParameter(
        jobName,
        paramName,
        type,
        description,
        defaultValue,
        additionalProps
    )
    
    if (success) {
        println "Parameter added successfully"
    } else {
        println "Failed to add parameter"
    }
} else if (options.up && options.j) {
    // Update parameter in job
    def jobName = options.j
    def paramName = options.up
    def description = options.d
    def defaultValue = options.D
    
    println "Updating parameter ${paramName} in job ${jobName}..."
    
    Map additionalProps = [:]
    if (options.c) {
        additionalProps.choices = options.c.split(",")
    }
    
    boolean success = manager.updateParameter(
        jobName,
        paramName,
        description,
        defaultValue,
        additionalProps
    )
    
    if (success) {
        println "Parameter updated successfully"
    } else {
        println "Failed to update parameter"
    }
} else if (options.rp && options.j) {
    // Remove parameter from job
    def jobName = options.j
    def paramName = options.rp
    
    println "Removing parameter ${paramName} from job ${jobName}..."
    
    boolean success = manager.removeParameter(jobName, paramName)
    
    if (success) {
        println "Parameter removed successfully"
    } else {
        println "Failed to remove parameter"
    }
} else {
    // Show usage if no operation specified
    println "Jenkins Job Parameters Manager"
    println "============================"
    
    // Show defined standards
    def standards = manager.getParameterStandards()
    
    if (!standards.isEmpty()) {
        println "\nDefined Parameter Standards:"
        standards.each { name, standard ->
            println "- ${name} (${standard.type}): '${standard.description}' Default: '${standard.defaultValue}'"
        }
    }
    
    println "\nUse one of the following options:"
    println "- Inventory parameters: --inventory"
    println "- Add parameter: --job JOB_NAME --add-param PARAM_NAME --type TYPE --desc DESCRIPTION --default VALUE"
    println "- Update parameter: --job JOB_NAME --update-param PARAM_NAME [--desc DESCRIPTION] [--default VALUE]"
    println "- Remove parameter: --job JOB_NAME --remove-param PARAM_NAME"
    println "- Define standard: --define-standard PARAM_NAME --type TYPE --desc DESCRIPTION --default VALUE"
    println "- Standardize parameter: --standardize PARAM_NAME"
    println "- Rename parameter: --rename OLD_NAME --to NEW_NAME"
}