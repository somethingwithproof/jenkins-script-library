package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.owasp.dependencycheck.gradle.DependencyCheckExtension

/**
 * Convention plugin for security scanning configuration.
 * Sets up standard OWASP dependency check settings.
 */
class SecurityConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('org.owasp.dependencycheck')
        
        project.extensions.configure(DependencyCheckExtension) { extension ->
            extension.formats = ['HTML', 'XML', 'JSON']
            extension.failBuildOnCVSS = 7.0
            extension.analyzers {
                assemblyEnabled = false
                nugetconfEnabled = false
                nodeEnabled = false
                retirejs.enabled = true
                ossIndex.enabled = true
                experimentalEnabled = true
            }
            extension.scanConfigurations = [
                'runtimeClasspath',
                'testRuntimeClasspath'
            ]
        }
    }
}

