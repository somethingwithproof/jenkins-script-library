/*
 * MIT License
 *
 * Copyright (c) 2024 Thomas Vincent
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

package com.github.thomasvincent.jenkinsscripts.helm

import hudson.model.Node
import hudson.model.TaskListener
import hudson.tools.InstallSourceProperty
import hudson.tools.HelmInstallation
import hudson.tools.ZipExtractionInstaller
import jenkins.model.Jenkins
import com.github.thomasvincent.jenkinsscripts.util.PipelineUtils

import java.io.Serializable

/**
 * Helm tool installer for Jenkins pipelines.
 * 
 * Manages Helm installation, OS/architecture detection, and PATH configuration.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class HelmHelper implements Serializable {

    private static final long serialVersionUID = 1L

    /** Reference to the pipeline script context */
    private transient def pipeline
    
    /** Utility class for common pipeline operations */
    private transient PipelineUtils utils

    /**
     * Creates a new Helm installer with pipeline context.
     * 
     * ```groovy
     * def helm = new HelmHelper(this)
     * ```
     */
    HelmHelper(def pipeline) {
        this.pipeline = pipeline
        this.utils = new PipelineUtils(pipeline)
    }

    /**
     * Installs and activates a Helm version in the PATH.
     * 
     * Automatically detects OS/architecture and downloads if needed.
     * 
     * ```groovy
     * helm.use('3.10.0')  // Install and use Helm 3.10.0
     * helm.use()          // Default to version 2.12.3
     * 
     * // Use in a pipeline
     * node {
     *     stage('Setup') {
     *         def helm = new HelmHelper(this)
     *         helm.use('3.11.2')
     *     }
     *     stage('Deploy') {
     *         sh 'helm upgrade --install my-release ./chart'
     *     }
     * }
     * ```
     */
    void use(String version = '2.12.3') {
        String normalizedVersion = normalizeVersion(version)
        String os = utils.currentOS()
        String arch = normalizeArchitecture(utils.currentArchitecture())
        String extension = determineFileExtension()
        String helmInstallPath = buildInstallPath(normalizedVersion)
        
        def installer = createInstaller(normalizedVersion, os, arch, extension, helmInstallPath)
        def helmTool = createToolInstallation(normalizedVersion, helmInstallPath, installer)
        
        configureEnvironment(helmTool, normalizedVersion)
    }
    
    /**
     * Ensures version string starts with 'v' for Helm URLs.
     * 
     * ```groovy
     * normalizeVersion('3.10.0')  // Returns 'v3.10.0'
     * normalizeVersion('v3.10.0') // Returns 'v3.10.0' (unchanged)
     * ```
     */
    private String normalizeVersion(String version) {
        return version.startsWith("v") ? version : "v$version"
    }
    
    /**
     * Converts architecture strings to Helm's format.
     * 
     * Uses Groovy's replaceAll method to transform architecture identifiers.
     * 
     * ```groovy
     * normalizeArchitecture('i386')   // Returns '386'
     * normalizeArchitecture('amd64')  // Returns 'amd64' (unchanged)
     * ```
     */
    private String normalizeArchitecture(String architecture) {
        return architecture.replaceAll("i", "")
    }
    
    /**
     * Returns the correct package extension based on OS.
     * 
     * Uses Groovy's ternary operator for concise platform checking.
     * 
     * ```groovy
     * determineFileExtension() // Returns 'tar.gz' on Unix, 'zip' on Windows
     * ```
     */
    private String determineFileExtension() {
        return pipeline.isUnix() ? 'tar.gz' : 'zip'
    }
    
    /**
     * Constructs Helm's installation path in Jenkins tools dir.
     * 
     * Uses GString interpolation for path construction.
     * 
     * ```groovy
     * buildInstallPath('v3.10.0') // Returns '/jenkins/tools/helm/v3.10.0'
     * ```
     */
    private String buildInstallPath(String version) {
        return "${Jenkins.instance.rootPath}/tools/helm/$version"
    }
    
    /**
     * Creates Helm download and extraction installer.
     * 
     * Uses Groovy list syntax to create installer configuration.
     * 
     * ```groovy
     * def installer = createInstaller(
     *     'v3.10.0', 'darwin', 'amd64', 'tar.gz', '/jenkins/tools/helm/v3.10.0'
     * )
     * // Creates installer for https://get.helm.sh/helm-v3.10.0-darwin-amd64.tar.gz
     * ```
     */
    private List<ZipExtractionInstaller> createInstaller(String version, String os, String arch, String extension, String installPath) {
        return [
            new ZipExtractionInstaller(
                null, 
                "https://get.helm.sh/helm-$version-$os-$arch.$extension", 
                "$installPath/$os-$arch"
            )
        ]
    }
    
    /**
     * Registers Helm with Jenkins' tool system.
     * 
     * Configures tool metadata for installation tracking.
     * 
     * ```groovy
     * def helmTool = createToolInstallation(
     *     'v3.10.0',
     *     '/jenkins/tools/helm/v3.10.0',
     *     installersList
     * )
     * ```
     */
    private HelmInstallation createToolInstallation(String version, String installPath, List<ZipExtractionInstaller> installers) {
        return new HelmInstallation("helm-$version", installPath, [new InstallSourceProperty(installers)])
    }
    
    /**
     * Adds Helm to PATH and activates it for the pipeline.
     * 
     * Uses try/catch for robust error handling and clear error messages.
     * 
     * ```groovy
     * configureEnvironment(helmTool, 'v3.10.0')
     * // Updates PATH and prints "Using Helm v3.10.0 installed at /path/to/helm"
     * ```
     */
    private void configureEnvironment(HelmInstallation helmTool, String version) {
        try {
            Node currentNode = pipeline.getContext(Node.class)
            TaskListener listener = pipeline.getContext(TaskListener.class)
            
            String helmHome = helmTool.forNode(currentNode, listener).home
            pipeline.env.PATH = "$helmHome:${pipeline.env.PATH}"
            pipeline.echo "Using Helm $version installed at $helmHome"
        } catch (Exception e) {
            pipeline.error("Failed to set up Helm $version: ${e.message}")
        }
    }
}