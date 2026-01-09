/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Jenkins Pipeline shared library step for DSL-based pipeline generation.
 */

import com.github.thomasvincent.jenkinsscripts.dsl.PipelineDSL

/**
 * Generate Jenkins pipelines using Groovy DSL.
 *
 * Usage:
 *   // Generate a simple CI pipeline
 *   def pipelineCode = pipelineDSL.simpleCIPipeline([
 *       language: 'java',
 *       deploy: true,
 *       deployCommand: './deploy.sh'
 *   ])
 *
 *   // Use the full DSL builder
 *   def pipelineCode = pipelineDSL {
 *       agent 'any'
 *       stages {
 *           stage('Build') {
 *               steps {
 *                   sh './gradlew build'
 *               }
 *           }
 *       }
 *   }
 */

def call(@DelegatesTo(PipelineDSL.PipelineBuilder) Closure config) {
    return PipelineDSL.pipeline(config)
}

/**
 * Generate a simple CI pipeline from parameters.
 */
def simpleCIPipeline(Map params) {
    return PipelineDSL.simpleCIPipeline(params)
}

/**
 * Generate a pipeline for a Java/Gradle project.
 */
def javaPipeline(Map params = [:]) {
    return simpleCIPipeline(params + [language: 'java'])
}

/**
 * Generate a pipeline for a Maven project.
 */
def mavenPipeline(Map params = [:]) {
    return simpleCIPipeline(params + [language: 'maven'])
}

/**
 * Generate a pipeline for a Node.js project.
 */
def nodePipeline(Map params = [:]) {
    return simpleCIPipeline(params + [language: 'nodejs'])
}

/**
 * Generate a pipeline for a Python project.
 */
def pythonPipeline(Map params = [:]) {
    return simpleCIPipeline(params + [language: 'python'])
}

/**
 * Generate a pipeline for a Go project.
 */
def goPipeline(Map params = [:]) {
    return simpleCIPipeline(params + [language: 'go'])
}

/**
 * Generate a multi-branch pipeline configuration.
 */
def multiBranchPipeline(Map params) {
    def branches = params.branches ?: ['main', 'develop', 'feature/*']

    return PipelineDSL.pipeline {
        agent 'any'

        options {
            timeout(time: params.timeoutMinutes ?: 30, unit: 'MINUTES')
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout 'scm'
                }
            }

            stage('Build') {
                steps {
                    sh params.buildCommand ?: 'echo "Build"'
                }
            }

            stage('Test') {
                steps {
                    sh params.testCommand ?: 'echo "Test"'
                }
            }

            stage('Deploy to Dev') {
                when {
                    branch 'develop'
                }
                steps {
                    sh params.deployDevCommand ?: 'echo "Deploy to dev"'
                }
            }

            stage('Deploy to Prod') {
                when {
                    branch 'main'
                }
                steps {
                    sh params.deployProdCommand ?: 'echo "Deploy to prod"'
                }
            }
        }

        post {
            always {
                junit '**/test-results/*.xml'
            }
            failure {
                echo 'Build failed!'
            }
        }
    }
}
