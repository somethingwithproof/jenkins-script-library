package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Convention plugin for JaCoCo coverage configuration.
 * Sets up standard coverage thresholds and reporting formats.
 */
class JacocoConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('jacoco')
        
        project.extensions.configure(JacocoPluginExtension) { extension ->
            extension.toolVersion = '0.8.11'
        }
        
        project.tasks.withType(JacocoReport).configureEach { task ->
            task.reports {
                xml.required = true
                csv.required = true
                html.required = true
                html.outputLocation = project.layout.buildDirectory.dir('reports/jacoco/html')
            }
            
            // Set up threshold rules
            task.doLast {
                def coverageThreshold = 0.7 // 70%
                
                def jacocoReportFile = project.file("${project.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
                if (jacocoReportFile.exists()) {
                    def xml = new XmlSlurper().parse(jacocoReportFile)
                    def covered = xml.counter.find { it.@type == 'LINE' }.@covered.toInteger()
                    def missed = xml.counter.find { it.@type == 'LINE' }.@missed.toInteger()
                    def total = covered + missed
                    def coverageRatio = total > 0 ? covered / total : 0
                    
                    if (coverageRatio < coverageThreshold) {
                        project.logger.warn("Code coverage is below threshold: ${(coverageRatio * 100).round(2)}% < ${(coverageThreshold * 100).round(2)}%")
                    } else {
                        project.logger.lifecycle("Code coverage: ${(coverageRatio * 100).round(2)}%")
                    }
                }
            }
        }
    }
}

