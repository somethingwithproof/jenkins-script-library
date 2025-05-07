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

package com.github.thomasvincent.jenkinsscripts.util

/**
 * Groovy compatibility verification for Jenkins scripts.
 * 
 * Provides utilities to verify Groovy and Java version compatibility
 * with required runtime environments for Jenkins Script Library.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class GroovyCompatibilityVerifier {

    static final String MINIMUM_GROOVY_VERSION = "4.0.0"
    static final String MINIMUM_JAVA_VERSION = "17.0.0"
    
    /**
     * Main method to run the verifier.
     * 
     * Performs version checks and feature tests then prints results.
     * 
     * ```groovy
     * groovy GroovyCompatibilityVerifier.groovy
     * ```
     */
    static void main(String[] args) {
        println "Jenkins Script Library Compatibility Verifier"
        println "=============================================="
        println ""
        
        def details = getEnvironmentDetails()
        
        println "Current environment:"
        println "  Groovy version: ${details.groovyVersion}"
        println "  Java version:   ${details.javaVersion}"
        println "  Java vendor:    ${details.javaVendor}"
        println "  OS:             ${details.osName}"
        println ""
        
        // Check Groovy version compatibility
        if (isCompatibleGroovyVersion()) {
            println "✅ Groovy version is compatible (${MINIMUM_GROOVY_VERSION}+)"
        } else {
            println "❌ Groovy version is not compatible! Please use Groovy ${MINIMUM_GROOVY_VERSION} or later."
        }
        
        // Check Java version compatibility
        if (isCompatibleJavaVersion()) {
            println "✅ Java version is compatible (${MINIMUM_JAVA_VERSION}+)"
        } else {
            println "❌ Java version is not compatible! Please use Java ${MINIMUM_JAVA_VERSION} or later."
        }
        
        // Test Groovy 4.0 features
        println ""
        println "Testing Groovy 4.0 features..."
        if (testGroovy40Features()) {
            println "✅ All Groovy 4.0 features are working correctly"
        } else {
            println "❌ Some Groovy 4.0 features are not working"
        }
        
        println ""
        println "Overall result:"
        if (isCompatibleGroovyVersion() && isCompatibleJavaVersion() && testGroovy40Features()) {
            println "✅ Your environment is fully compatible with Jenkins Script Library"
        } else {
            println "❌ Your environment is not fully compatible with Jenkins Script Library"
        }
    }
    
    /**
     * Tests if the current Groovy version is compatible.
     * 
     * Compares current Groovy version against the minimum required version.
     * 
     * ```groovy
     * GroovyCompatibilityVerifier.isCompatibleGroovyVersion()  // true if Groovy 4.0.0+
     * ```
     */
    static boolean isCompatibleGroovyVersion() {
        def currentVersion = GroovySystem.version
        return compareVersions(currentVersion, MINIMUM_GROOVY_VERSION) >= 0
    }
    
    /**
     * Tests if the current Java version is compatible.
     * 
     * Compares current Java version against the minimum required version.
     * 
     * ```groovy
     * GroovyCompatibilityVerifier.isCompatibleJavaVersion()  // true if Java 17.0.0+
     * ```
     */
    static boolean isCompatibleJavaVersion() {
        def currentVersion = System.getProperty("java.version")
        return compareVersions(currentVersion, MINIMUM_JAVA_VERSION) >= 0
    }
    
    /**
     * Compares two version strings.
     * 
     * Uses Groovy's tokenize method to compare version segments numerically.
     * 
     * ```groovy
     * GroovyCompatibilityVerifier.compareVersions("4.0.0", "3.0.15")  // positive (first > second)
     * GroovyCompatibilityVerifier.compareVersions("1.8.0", "11.0.2")  // negative (first < second)
     * ```
     */
    static int compareVersions(String version1, String version2) {
        def v1Parts = version1.tokenize('.')
        def v2Parts = version2.tokenize('.')
        
        for (int i = 0; i < Math.min(v1Parts.size(), v2Parts.size()); i++) {
            def v1Part = v1Parts[i].replaceAll("\\D", "").toInteger()
            def v2Part = v2Parts[i].replaceAll("\\D", "").toInteger()
            
            if (v1Part != v2Part) {
                return v1Part <=> v2Part
            }
        }
        
        return v1Parts.size() <=> v2Parts.size()
    }
    
    /**
     * Internal record class for testing Groovy 4.0 features.
     * 
     * Used to verify record support in the Groovy runtime.
     */
    record Person(String name, int age) {}
    
    /**
     * Tests Groovy 4.0 specific features.
     * 
     * Verifies that critical Groovy 4.0 features work in the current runtime.
     * 
     * ```groovy
     * GroovyCompatibilityVerifier.testGroovy40Features()  // true if all features work
     * ```
     */
    static boolean testGroovy40Features() {
        try {
            // Create a record
            def person = new GroovyCompatibilityVerifier.Person("Test", 42)
            
            // Test record accessors
            if (person.name() != "Test" || person.age() != 42) {
                return false
            }
            
            // Test switch expressions
            def result = switch(person.age()) {
                case 0..17 -> "child"
                case 18..64 -> "adult" 
                default -> "senior"
            }
            
            if (result != "adult") {
                return false
            }
            
            // Test pattern matching with instanceof
            def value = "test"
            def typeResult = ""
            
            if (value instanceof String) {
                typeResult = "string of length ${value.length()}"
            } else if (value instanceof Number) {
                typeResult = "number with value ${value}"
            } else {
                typeResult = "unknown type"
            }
            
            if (typeResult != "string of length 4") {
                return false
            }
            
            return true
        } catch (Exception e) {
            println "Error testing Groovy 4.0 features: ${e.message}"
            return false
        }
    }
    
    /**
     * Returns details about the current environment.
     * 
     * Collects version information about Groovy, Java, and the operating system.
     * 
     * ```groovy
     * def details = GroovyCompatibilityVerifier.getEnvironmentDetails()
     * println details.groovyVersion  // "4.0.6" (example)
     * ```
     */
    static Map<String, String> getEnvironmentDetails() {
        return [
            'groovyVersion': GroovySystem.version,
            'javaVersion': System.getProperty("java.version"),
            'javaVendor': System.getProperty("java.vendor"),
            'osName': System.getProperty("os.name"),
        ]
    }
}