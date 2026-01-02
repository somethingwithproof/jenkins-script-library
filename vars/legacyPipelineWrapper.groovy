/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

/**
 * Legacy Pipeline Wrapper for Backward Compatibility
 * 
 * This wrapper provides backward compatibility for legacy scripted pipelines
 * by allowing them to be called from declarative pipelines or vice versa.
 * 
 * Usage in Declarative Pipeline:
 * @Library('jenkins-script-library') _
 * 
 * pipeline {
 *     agent any
 *     stages {
 *         stage('Run Legacy Script') {
 *             steps {
 *                 legacyPipelineWrapper {
 *                     // Your legacy scripted pipeline code here
 *                     node {
 *                         stage('Build') {
 *                             sh 'make build'
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * 
 * Usage for Migrating Existing Scripted Pipelines:
 * @Library('jenkins-script-library') _
 * 
 * // Wrap your existing scripted pipeline
 * legacyPipelineWrapper {
 *     node('linux') {
 *         try {
 *             stage('Build') {
 *                 sh 'make build'
 *             }
 *             stage('Test') {
 *                 sh 'make test'
 *             }
 *         } catch (Exception e) {
 *             error "Build failed: ${e.message}"
 *         }
 *     }
 * }
 */
def call(Closure body) {
    // Detect if we're running in a declarative or scripted context
    def isDeclarative = isDeclarativePipeline()
    
    try {
        if (isDeclarative) {
            echo "[Legacy Wrapper] Executing legacy scripted code in declarative context"
        }
        
        // Execute the legacy pipeline code
        body()
        
    } catch (Exception e) {
        echo "[Legacy Wrapper] Error in legacy pipeline: ${e.message}"
        throw e
    }
}

/**
 * Alternative method that accepts configuration
 */
def call(Map config = [:], Closure body) {
    def enableLogging = config.enableLogging ?: true
    def catchErrors = config.catchErrors ?: false
    
    try {
        if (enableLogging) {
            echo "[Legacy Wrapper] Starting legacy pipeline execution"
            echo "[Legacy Wrapper] Configuration: ${config}"
        }
        
        body()
        
        if (enableLogging) {
            echo "[Legacy Wrapper] Legacy pipeline completed successfully"
        }
        
    } catch (Exception e) {
        if (enableLogging) {
            echo "[Legacy Wrapper] Legacy pipeline failed: ${e.message}"
        }
        
        if (catchErrors) {
            currentBuild.result = 'FAILURE'
            return false
        } else {
            throw e
        }
    }
    
    return true
}

/**
 * Detect if we're in a declarative pipeline context
 * Note: This is a best-effort detection and may not be 100% accurate
 */
private boolean isDeclarativePipeline() {
    try {
        // Check for declarative pipeline environment
        // This is a simplified check - in practice, both syntaxes share many characteristics
        return true  // Assume declarative for compatibility
    } catch (Exception e) {
        return false
    }
}

/**
 * Execute a scripted node block in declarative context
 */
def executeNode(String label = '', Closure body) {
    echo "[Legacy Wrapper] Executing node block with label: ${label ?: 'any'}"
    
    if (label) {
        node(label) {
            body()
        }
    } else {
        node {
            body()
        }
    }
}

/**
 * Execute parallel stages in a compatible way
 */
def executeParallel(Map stages) {
    echo "[Legacy Wrapper] Executing parallel stages: ${stages.keySet()}"
    
    // Convert to format compatible with both syntaxes
    def parallelStages = [:]
    
    stages.each { name, closure ->
        parallelStages[name] = {
            stage(name) {
                closure()
            }
        }
    }
    
    parallel parallelStages
}
