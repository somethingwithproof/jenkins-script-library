package com.github.thomasvincent.jenkinsscripts

import hudson.model.Node
import hudson.model.TaskListener
import hudson.tools.InstallSourceProperty
import hudson.tools.ToolInstallation
import hudson.tools.ZipExtractionInstaller
import jenkins.model.Jenkins
import java.io.Serializable

/**
 * Helper class to manage Helm installations within a Jenkins pipeline. This class ensures
 * that a specified version of Helm is available in the Jenkins environment and properly set
 * up in the PATH variable for use in builds.
 * 
 * @author Thomas Vincent
 * @date Created on 2024-04-15
 * @license MIT License
 * 
 * This class handles the dynamic installation of Helm tools, ensuring the specified version is available
 * and properly configured in the executing environment's PATH. It is designed to support both Unix and Windows environments,
 * adapting the installation process to match the operating system and architecture specifics.
 */

/**
 * Helper class to manage Helm installations within a Jenkins pipeline. This class ensures
 * that a specified version of Helm is available in the Jenkins environment and properly set
 * up in the PATH variable for use in builds.
 */
class HelmHelper implements Serializable {

    private transient def pipeline
    private transient PipelineUtils utils

    /**
     * Initializes a new instance of the HelmHelper class.
     * 
     * @param pipeline the current pipeline script context, providing access to pipeline-specific methods
     */
    HelmHelper(def pipeline) {
        this.pipeline = pipeline
        this.utils = new PipelineUtils(pipeline)
    }

    /**
     * Ensures the specified version of Helm is installed and available in the PATH.
     * Installs Helm if it is not already installed.
     * 
     * @param version the desired version of Helm, defaults to '2.12.3'
     */
    void use(String version = '2.12.3') {
        String normalizedVersion = version.startsWith("v") ? version : "v$version"
        String os = utils.currentOS()
        String arch = utils.currentArchitecture().replaceAll("i", "")
        String extension = pipeline.isUnix() ? 'tar.gz' : 'zip'
        String helmInstallPath = "${Jenkins.instance.rootPath}/tools/helm/$normalizedVersion"

        def installers = [
            new ZipExtractionInstaller(null, "https://get.helm.sh/helm-$normalizedVersion-$os-$arch.$extension", "$helmInstallPath/$os-$arch")
        ]

        ToolInstallation helmTool = new ToolInstallation("helm-$normalizedVersion", helmInstallPath, [new InstallSourceProperty(installers)])
        Node currentNode = pipeline.getContext(Node.class)
        TaskListener listener = pipeline.getContext(TaskListener.class)
        
        try {
            String helmHome = helmTool.forNode(currentNode, listener).home
            pipeline.env.PATH = "$helmHome:${pipeline.env.PATH}"
            pipeline.echo "Using Helm $normalizedVersion installed at $helmHome"
        } catch (Exception e) {
            pipeline.error("Failed to set up Helm $normalizedVersion: ${e.message}")
        }
    }
}
