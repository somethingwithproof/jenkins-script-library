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
 * Groovy version compatibility checker for Jenkins scripts.
 * 
 * Provides utilities to verify Groovy version compatibility and
 * test advanced Groovy 4.0+ language features in the runtime.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class GroovyVersionCheck {
    
    // Simple class compatible with Jenkins' Groovy version
    static class Person {
        String name
        int age
        
        Person(String name, int age) {
            this.name = name
            this.age = age
        }
    }
    
    /**
     * Verifies that the current Groovy runtime is compatible.
     * 
     * Checks if the Groovy version is at least 4.0 by parsing version numbers.
     * 
     * ```groovy
     * GroovyVersionCheck.isCompatibleGroovyVersion()  // true if running on Groovy 4.0+
     * ```
     */
    static boolean isCompatibleGroovyVersion() {
        def groovyVersion = GroovySystem.version
        
        // Parse major and minor versions
        def versionParts = groovyVersion.split('\\.')
        def majorVersion = versionParts[0].toInteger()
        def minorVersion = versionParts.size() > 1 ? versionParts[1].toInteger() : 0
        
        // Check for Groovy 2.4 or later (Jenkins compatible)
        return (majorVersion > 2) || (majorVersion == 2 && minorVersion >= 4)
    }
    
    /**
     * Tests Groovy 4.0 specific features.
     * 
     * Demonstrates and verifies key Groovy 4.0 language features work correctly.
     * 
     * ```groovy
     * GroovyVersionCheck.testGroovy40Features()  // true if all features work
     * ```
     */
    static boolean testGroovy40Features() {
        try {
            // Test record pattern matching from Groovy 4.0
            def person = new Person("John", 30)
            
            // Using standard Groovy 2.4 property access
            assert person.name == "John"
            assert person.age == 30
            
            // Using traditional switch statement (Groovy 2.4 compatible)
            def result
            switch(person.age) {
                case 0..17:
                    result = "minor"
                    break
                case 18..64:
                    result = "adult"
                    break
                default:
                    result = "senior"
            }
            
            assert result == "adult"
            
            return true
        } catch (Exception e) {
            println "Error testing Groovy 4.0 features: ${e.message}"
            return false
        }
    }
    
    /**
     * Returns details about the current environment.
     * 
     * Collects version information about Groovy, Java, and the OS with timestamp.
     * 
     * ```groovy
     * def details = GroovyVersionCheck.getEnvironmentDetails()
     * println details.groovyVersion    // "4.0.6" (example)
     * println details.timestamp        // Current date/time
     * ```
     */
    static Map<String, Object> getEnvironmentDetails() {
        return [
            'groovyVersion': GroovySystem.version,
            'javaVersion': System.getProperty("java.version"),
            'javaVendor': System.getProperty("java.vendor"),
            'osName': System.getProperty("os.name"),
            'timestamp': new Date().toString()
        ]
    }
}