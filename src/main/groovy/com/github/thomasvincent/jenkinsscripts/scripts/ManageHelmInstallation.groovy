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

import com.github.thomasvincent.jenkinsscripts.helm.HelmHelper
import jenkins.model.Jenkins
import groovy.cli.commons.CliBuilder

/**
 * Installs and configures Helm versions for use in Jenkins.
 * 
 * '''Usage:'''
 * ```groovy
 * # Install default Helm version (2.12.3)
 * ./ManageHelmInstallation.groovy
 * 
 * # Install specific Helm version
 * ./ManageHelmInstallation.groovy --version 3.9.0
 * 
 * # Show help
 * ./ManageHelmInstallation.groovy --help
 * ```
 * 
 * @author Thomas Vincent
 * @since 1.0
 */

/**
 * Initialize the command line argument parser with appropriate options.
 */
def cli = new CliBuilder(usage: 'groovy ManageHelmInstallation [options]',
                         header: 'Options:')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    v(longOpt: 'version', args: 1, argName: 'version', 'Helm version to install (default: 2.12.3)')
    l(longOpt: 'list', 'List all installed Helm versions')
}

/**
 * Parse the command line arguments.
 */
def options = cli.parse(args)
if (!options) {
    return
}

/**
 * Show help if requested.
 */
if (options.h) {
    cli.usage()
    return
}

/**
 * Get the Jenkins instance for interacting with Jenkins API.
 */
def jenkins = Jenkins.get()

/**
 * List installed Helm versions if requested.
 */
if (options.l) {
    println "Installed Helm versions:"
    def helmHome = "${jenkins.rootPath}/tools/helm"
    def helmDir = new File(helmHome)
    
    if (helmDir.exists() && helmDir.isDirectory()) {
        def versions = helmDir.listFiles().findAll { it.isDirectory() }
                           .collect { it.name }
                           .sort()
        
        if (versions.isEmpty()) {
            println "  No Helm versions found"
        } else {
            versions.each { version ->
                println "  ${version}"
            }
        }
    } else {
        println "  No Helm versions found"
    }
    return
}

/**
 * Get the Helm version to install.
 */
def helmVersion = options.v ?: '2.12.3'
println "Installing Helm version: ${helmVersion}"

/**
 * Create a script execution environment for the HelmHelper.
 */
def binding = new Binding()
def script = new GroovyShell(binding).parse('class Script { def getContext(Class c) { return null } }')

/**
 * Use the HelmHelper to ensure the specified Helm version is installed.
 */
def helmHelper = new HelmHelper(script)

try {
    helmHelper.use(helmVersion)
    println "Successfully installed Helm ${helmVersion}"
} catch (Exception e) {
    println "Failed to install Helm ${helmVersion}: ${e.message}"
    e.printStackTrace()
    System.exit(1)
}