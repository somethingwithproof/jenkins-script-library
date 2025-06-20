package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.provider.Provider
import org.gradle.api.credentials.PasswordCredentials

/**
 * Convention plugin for publishing configuration.
 * Sets up standard publishing settings for Maven repositories.
 * 
 * SECURITY NOTE: This plugin does NOT contain hardcoded credentials.
 * All credentials are resolved at runtime from Gradle properties or environment variables.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class PublishingConventionPlugin implements Plugin<Project> {
    
    // Constants for repository URLs
    private static final String GITHUB_PACKAGES_URL = 'https://maven.pkg.github.com/thomasvincent/jenkins-script-library'
    
    /**
     * Apply the publishing configuration to the project.
     * 
     * @param project The project to configure
     */
    void apply(Project project) {
        project.plugins.apply('maven-publish')
        project.plugins.apply('io.github.gradle-nexus.publish-plugin')
        
        project.afterEvaluate {
            project.extensions.configure(PublishingExtension) { extension ->
                configurePublications(project, extension)
                configureRepositories(project, extension)
            }
        }
    }
    
    /**
     * Configure Maven publications with project metadata.
     * 
     * @param project The project being configured
     * @param extension The publishing extension
     */
    private void configurePublications(Project project, PublishingExtension extension) {
        extension.publications { publications ->
            publications.create('maven', MavenPublication) { publication ->
                from project.components.java
                
                publication.pom {
                    name = project.name
                    description = project.description ?: project.name
                    url = 'https://github.com/thomasvincent/jenkins-script-library'
                    
                    licenses {
                        license {
                            name = 'Apache License 2.0'
                            url = 'https://www.apache.org/licenses/LICENSE-2.0'
                        }
                    }
                    
                    developers {
                        developer {
                            id = 'thomasvincent'
                            name = 'Thomas Vincent'
                        }
                    }
                    
                    scm {
                        url = 'https://github.com/thomasvincent/jenkins-script-library'
                        connection = 'scm:git:git://github.com/thomasvincent/jenkins-script-library.git'
                        developerConnection = 'scm:git:ssh://github.com/thomasvincent/jenkins-script-library.git'
                    }
                }
            }
        }
    }
    
    /**
     * Configure the Maven repositories for publishing.
     * 
     * @param project The project being configured
     * @param extension The publishing extension
     */
    private void configureRepositories(Project project, PublishingExtension extension) {
        extension.repositories {
            extension.repositories.maven { repo ->
                repo.name = 'GitHubPackages'
                repo.url = GITHUB_PACKAGES_URL
                
                // Configure credentials securely from environment or properties
                configureGitHubPackagesCredentials(project, repo)
            }
        }
    }
    
    /**
     * Configure credentials for GitHub Packages repository.
     * SECURITY: No hardcoded credentials are used in this method.
     * Credentials are resolved from Gradle properties or environment variables.
     * 
     * @param project The project being configured
     * @param repo The Maven repository
     */
    private void configureGitHubPackagesCredentials(Project project, def repo) {
        repo.credentials { credentials ->
            // First try to resolve from Gradle properties, then fall back to environment variables
            credentials.username = resolveCredentialValue(project, 'gpr.user', 'GITHUB_ACTOR')
            
            // SECURITY: No hardcoded token/password values - only resolved at runtime
            // from properties or environment variables
            credentials.password = resolveCredentialValue(project, 'gpr.key', 'GITHUB_TOKEN') 
        }
    }
    
    /**
     * Safely resolve a credential value from project properties or environment.
     * SECURITY: This method does not contain any hardcoded secrets.
     * 
     * @param project The project
     * @param propertyName The name of the property to look for
     * @param envVarName The name of the environment variable to use as fallback
     * @return The resolved credential value or null
     */
    private String resolveCredentialValue(Project project, String propertyName, String envVarName) {
        // Try project property first
        if (project.hasProperty(propertyName)) {
            return project.findProperty(propertyName)?.toString()
        }
        
        // Fall back to environment variable
        String envValue = System.getenv(envVarName)
        return envValue
    }
}

