package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.plugins.quality.CodeNarcExtension

/**
 * Convention plugin for CodeNarc configuration.
 * Applies standard rulesets and configurations for Groovy code quality.
 */
class CodeNarcConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('codenarc')
        
        project.extensions.configure(CodeNarcExtension) { extension ->
            extension.toolVersion = '3.3.0'
            extension.reportFormat = 'html'
            extension.ignoreFailures = false
        }
        
        project.tasks.withType(CodeNarc).configureEach { task ->
            task.reports.each { report ->
                report.enabled = true
            }
            
            // Set default ruleset
            task.configFile = project.rootProject.file('config/codenarc/codenarc.groovy')
            if (!task.configFile.exists()) {
                task.configFile = project.file("${project.rootDir}/config/codenarc/codenarc.groovy")
                if (!task.configFile.exists()) {
                    // Use a default config from the convention plugin
                    task.configFile = project.resources.text.fromString(getDefaultCodeNarcConfig())
                }
            }
            
            // Configure source files
            if (task.name == 'codenarcMain') {
                configureMainSourceSet(task, project)
            } else if (task.name == 'codenarcTest') {
                configureTestSourceSet(task, project)
            }
        }
    }
    
    private void configureMainSourceSet(CodeNarc task, Project project) {
        FileTree sourceFiles = project.fileTree(dir: 'src/main/groovy', includes: ['**/*.groovy'])
        task.source = sourceFiles
    }
    
    private void configureTestSourceSet(CodeNarc task, Project project) {
        FileTree sourceFiles = project.fileTree(dir: 'src/test/groovy', includes: ['**/*.groovy'])
        task.source = sourceFiles
    }
    
    private String getDefaultCodeNarcConfig() {
        return '''
        ruleset {
            // Basic rules
            BracesForClass
            BracesForMethod
            BracesForIfElse
            BracesForForLoop
            BracesForTryCatchFinally
            
            // Naming rules
            ClassNameSameAsFilename
            MethodName
            PropertyName
            VariableName
            
            // Size and complexity rules
            CyclomaticComplexity {
                maxMethodComplexity = 10
            }
            
            // Unnecessary code
            UnnecessaryGString
            UnnecessaryPublicModifier
            UnnecessaryReturn
            
            // Potential bugs
            EmptyCatchBlock
            EmptyMethod
            EmptyTryBlock
            ReturnNullFromCatchBlock
        }
        '''
    }
}

