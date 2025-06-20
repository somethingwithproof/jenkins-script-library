package com.github.thomasvincent.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

/**
 * Convention plugin for publishing configuration.
 * Sets up standard publishing settings for Maven repositories.
 */
class PublishingConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('maven-publish')
        project.plugins.apply('io.github.gradle-nexus.publish-plugin')
        
        project.afterEvaluate {
            project.extensions.configure(PublishingExtension) { extension ->
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
                
                extension.repositories {
                    extension.repositories.maven { repo ->
                        repo.name = 'GitHubPackages'
                        repo.url = 'https://maven.pkg.github.com/thomasvincent/jenkins-script-library'
                        repo.credentials {
                            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
                            password = project.findProperty('gpr.key') ?: System.getenv('GITHUB_TOKEN')
                        }
                    }
                }
            }
        }
    }
}

