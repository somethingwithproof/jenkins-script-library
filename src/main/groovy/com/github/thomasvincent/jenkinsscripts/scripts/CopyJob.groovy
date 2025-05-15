package com.github.thomasvincent.jenkinsscripts.scripts

import groovy.util.CliBuilder
import jenkins.model.Jenkins
import hudson.model.AbstractItem
import hudson.model.Job
import hudson.model.ItemGroup
import com.cloudbees.hudson.plugins.folder.Folder
import java.io.ByteArrayInputStream

// #!/usr/bin/env groovy

def cli = new CliBuilder(usage: 'groovy CopyJob.groovy -s <sourceJob> -n <newJobName> [options]',
                         header: 'Copies an existing Jenkins job.\nOptions:')
cli.s(longOpt: 'source', args: 1, argName: 'fullName', required: true, 'Full name of the source job (e.g., "Folder/JobToCopy")')
cli.n(longOpt: 'newName', args: 1, argName: 'name', required: true, 'Name for the new job (simple name, not full path)')
cli.t(longOpt: 'targetFolder', args: 1, argName: 'path', 'Full path of the target folder for the new job. Defaults to source job\'s folder or root.')
cli.r(longOpt: 'replace', args: 1, argName: 'old:new', valueSeparator: ':', 'Configuration replacement string "oldString:newString". Can be used multiple times.')
cli.o(longOpt: 'overwrite', 'Allow overwriting if the target job already exists')
cli.d(longOpt: 'dryRun', 'Simulate without actual changes')
cli.h(longOpt: 'help', 'Show usage information')

def options = cli.parse(args)
if (!options || options.h) {
    cli.usage()
    return
}
if (!options.s || !options.n) {
    println "Error: Source job full name (-s) and new job name (-n) are required."
    cli.usage()
    return
}

def jenkins = Jenkins.get()
if (jenkins == null) {
    println "Error: Could not get Jenkins instance. Ensure this script is run in a Jenkins context."
    return
}

String sourceJobFullName = options.s
String newJobName = options.n
boolean dryRun = options.d
boolean overwrite = options.o
List<String> replacementsList = options.rs ?: [] // options.rs gives a list of lists for : separated values
Map<String, String> replacements = [:]
replacementsList.each { List pair ->
    if (pair.size() == 2) {
        replacements[pair[0]] = pair[1]
    } else {
        println "Warning: Invalid replacement format '${pair.join(':')}'. Expected 'oldString:newString'. Skipping."
    }
}

println "--- Job Copy Operation ---"
if (dryRun) println "[DRY RUN MODE ENABLED]"

// 1. Get Source Item
AbstractItem sourceItem = jenkins.getItemByFullName(sourceJobFullName)
if (sourceItem == null) {
    println "Error: Source job '${sourceJobFullName}' not found."
    return
}
if (!(sourceItem instanceof Job)) { // Or check if it's an AbstractItem with a config file if more general
    println "Error: Source item '${sourceJobFullName}' is not a Job and cannot be copied with this script."
    // Jenkins.copy might work for other AbstractItem, but config.xml based replacement needs item with getConfigFile()
    return
}
println "Source job: '${sourceItem.fullName}' of type ${sourceItem.getClass().simpleName}"

// 2. Determine Target Parent ItemGroup
ItemGroup targetParent
String targetJobFullName
if (options.t) {
    AbstractItem folderItem = jenkins.getItemByFullName(options.t)
    if (folderItem == null) {
        println "Error: Target folder '${options.t}' not found."
        return
    }
    if (!(folderItem instanceof Folder)) {
        println "Error: Target item '${options.t}' is not a Folder."
        return
    }
    targetParent = (Folder) folderItem
    targetJobFullName = "${targetParent.fullName}/${newJobName}"
    println "Target folder specified: '${targetParent.fullName}'"
} else {
    // Default to source job's parent
    targetParent = sourceItem.getParent()
    if (targetParent == jenkins) { // Source job is at root
        targetJobFullName = newJobName
        println "Target folder not specified, defaulting to Jenkins root (same as source)."
    } else { // Source job is in a folder
        targetJobFullName = "${targetParent.fullName}/${newJobName}"
        println "Target folder not specified, defaulting to source job's folder: '${targetParent.fullName}'."
    }
}
println "New job full name will be: '${targetJobFullName}'"

// 3. Check if Target Job Exists
AbstractItem existingTargetItem = targetParent.getItem(newJobName)
if (existingTargetItem != null) {
    println "Warning: Job '${targetJobFullName}' already exists."
    if (overwrite) {
        println "Overwrite flag is set. Deleting existing job '${targetJobFullName}'."
        if (dryRun) {
            println "[DRY RUN] Would delete existing job '${existingTargetItem.fullName}'."
        } else {
            try {
                existingTargetItem.delete()
                println "Existing job '${existingTargetItem.fullName}' deleted."
            } catch (Exception e) {
                println "Error: Failed to delete existing job '${existingTargetItem.fullName}': ${e.getMessage()}"
                return
            }
        }
    } else {
        println "Error: Target job '${targetJobFullName}' already exists and --overwrite not specified. Aborting."
        return
    }
}

// 4. Perform Copy or CreateFromXML
println "Preparing to copy job..."
Job newJob = null

if (dryRun) {
    println "[DRY RUN] Would copy '${sourceItem.fullName}' to '${targetJobFullName}'."
    if (!replacements.isEmpty()) {
        println "[DRY RUN] Would apply ${replacements.size()} string replacements to config.xml:"
        replacements.each { oldStr, newStr ->
            println "  - '${oldStr}' -> '${newStr}'"
        }
    }
    println "[DRY RUN] Job copy operation complete (simulation)."
} else {
    try {
        if (replacements.isEmpty()) {
            println "Performing direct copy (no replacements)."
            if (targetParent instanceof Folder) {
                newJob = ((Folder) targetParent).copy(sourceItem, newJobName)
            } else { // targetParent is Jenkins root
                newJob = jenkins.copy(sourceItem, newJobName)
            }
            println "Direct copy successful."
        } else {
            println "Performing copy with config.xml replacements."
            if (sourceItem.getConfigFile() == null || !sourceItem.getConfigFile().exists()) {
                 println "Error: Source job '${sourceItem.fullName}' does not have a readable config.xml. Cannot apply replacements."
                 return
            }
            String configXml = sourceItem.getConfigFile().asString()
            println "Original config.xml (first 100 chars): ${configXml.take(100).replaceAll('\n', ' ')}..."
            
            int replacementsApplied = 0
            replacements.each { oldStr, newStr ->
                if (configXml.contains(oldStr)) {
                    configXml = configXml.replace(oldStr, newStr)
                    println "Applied replacement: '${oldStr}' -> '${newStr}'"
                    replacementsApplied++
                } else {
                    println "Warning: String '${oldStr}' not found in config.xml, replacement skipped."
                }
            }
            println "Total replacements applied: ${replacementsApplied}."
            println "Modified config.xml (first 100 chars): ${configXml.take(100).replaceAll('\n', ' ')}..."

            InputStream xmlStream = new ByteArrayInputStream(configXml.getBytes("UTF-8"))
            if (targetParent instanceof Folder) {
                newJob = ((Folder) targetParent).createProjectFromXML(newJobName, xmlStream)
            } else { // targetParent is Jenkins root
                newJob = jenkins.createProjectFromXML(newJobName, xmlStream)
            }
            xmlStream.close()
            println "Copy via modified config.xml successful."
        }

        if (newJob != null) {
            println "Successfully created job '${newJob.fullName}'."
            // Optionally, re-fetch to be absolutely sure, though newJob should be the created item.
            AbstractItem fetchedNewJob = targetParent.getItem(newJobName)
            if (fetchedNewJob != null && fetchedNewJob.fullName == newJob.fullName) {
                 println "Verified new job exists at '${fetchedNewJob.fullName}'."
            } else {
                 println "Warning: New job verification failed or job name mismatch. Expected '${targetJobFullName}', created '${newJob?.fullName}'."
            }
        } else {
            println "Error: Job creation/copy returned null. Unknown error during operation."
        }

    } catch (Exception e) {
        println "Error during job copy/creation: ${e.getClass().simpleName} - ${e.getMessage()}"
        e.printStackTrace(System.out) // For more details if needed
    }
}

println "--- End of Operation ---"
return // End of script

