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

import java.util.logging.Level
import java.util.logging.Logger

/**
 * String utilities for Jenkins scripts.
 * 
 * Provides common string operations for Jenkins pipeline scripts including
 * sanitization, parsing, and formatting.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class StringUtils {
    private static final Logger LOGGER = Logger.getLogger(StringUtils.class.getName())

    /**
     * Sanitizes a string for use as a Jenkins job name.
     * 
     * Replaces invalid characters with underscores.
     * 
     * ```groovy
     * StringUtils.sanitizeJobName("My Project (v1.0)")  // "My_Project_v1.0"
     * StringUtils.sanitizeJobName("dev/feature:test")   // "dev_feature_test"
     * ```
     */
    static String sanitizeJobName(String input) {
        if (input == null) {
            return ""
        }
        
        // Replace characters that are not allowed in Jenkins job names
        return input
            .replaceAll('[\\\\/?%*:|"<>]', '_') // Replace illegal chars with underscore
            .replaceAll('\\s+', '_')            // Replace whitespace with underscore
            .replaceAll('__+', '_')             // Replace multiple underscores with a single one
            .trim()
    }
    
    /**
     * Safely parses a string to an integer.
     * 
     * Returns a default value if parsing fails.
     * 
     * ```groovy
     * StringUtils.safeParseInt("42", 0)     // 42
     * StringUtils.safeParseInt("invalid", 0) // 0
     * StringUtils.safeParseInt(null, 100)   // 100
     * ```
     */
    static int safeParseInt(String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue
        }
        
        return ErrorHandler.withErrorHandling("parsing integer value from '${str}'", {
            return str.trim().toInteger()
        }, LOGGER, defaultValue, Level.FINE)
    }
    
    /**
     * Safely parses a string to a boolean.
     * 
     * Accepts various truthy/falsy values and returns a default if parsing fails.
     * 
     * ```groovy
     * StringUtils.safeParseBoolean("true", false)  // true
     * StringUtils.safeParseBoolean("yes", false)   // true
     * StringUtils.safeParseBoolean("no", true)     // false
     * StringUtils.safeParseBoolean("invalid", true) // true
     * ```
     */
    static boolean safeParseBoolean(String str, boolean defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue
        }
        
        switch (str.trim().toLowerCase()) {
            case ['true', 'yes', 'y', '1', 'on']:
                return true
            case ['false', 'no', 'n', '0', 'off']:
                return false
            default:
                return defaultValue
        }
    }
    
    /**
     * Truncates a string to a maximum length.
     * 
     * Adds an ellipsis if truncated (defaults to "...").
     * 
     * ```groovy
     * StringUtils.truncate("Hello world", 7)           // "Hello..."
     * StringUtils.truncate("Hello", 10)                // "Hello"
     * StringUtils.truncate("Very long text", 8, "!")   // "Very lon!"
     * ```
     */
    static String truncate(String str, int maxLength, String ellipsis = "...") {
        if (str == null) {
            return ""
        }
        
        if (str.length() <= maxLength) {
            return str
        }
        
        int truncateIndex = maxLength - ellipsis.length()
        if (truncateIndex < 0) {
            truncateIndex = 0
        }
        
        return str.substring(0, truncateIndex) + ellipsis
    }
    
    /**
     * Formats a parameter for display in logs.
     * 
     * Masks sensitive parameters with asterisks.
     * 
     * ```groovy
     * StringUtils.formatParameter("user", "admin")            // "user=admin"
     * StringUtils.formatParameter("password", "secret", true) // "password=*****"
     * ```
     */
    static String formatParameter(String paramName, String paramValue, boolean sensitive = false) {
        if (paramName == null) {
            return ""
        }
        
        String displayValue = sensitive ? "*****" : (paramValue ?: "")
        return "${paramName}=${displayValue}"
    }
    
    /**
     * Converts camelCase to kebab-case.
     * 
     * Uses Groovy's regular expression support for elegant conversion.
     * 
     * ```groovy
     * StringUtils.camelToKebab("myVariableName")  // "my-variable-name"
     * StringUtils.camelToKebab("API2Config")      // "api2-config"
     * ```
     */
    static String camelToKebab(String camelCase) {
        if (camelCase == null) {
            return ""
        }
        
        // Special case for all-caps acronyms like "ABC"
        if (camelCase.matches("[A-Z]+")) {
            return camelCase.split("").join("-").toLowerCase()
        }
        
        return camelCase
            .replaceAll(/([a-z0-9])([A-Z])/, '$1-$2')
            .replaceAll(/([A-Z])([A-Z][a-z])/, '$1-$2')
            .toLowerCase()
    }
    
    /**
     * Converts kebab-case to camelCase.
     * 
     * Leverages Groovy's closure-based replacement for elegant transformation.
     * 
     * ```groovy
     * StringUtils.kebabToCamel("my-variable-name")  // "myVariableName"
     * StringUtils.kebabToCamel("api-config")        // "apiConfig"
     * ```
     */
    static String kebabToCamel(String kebabCase) {
        if (kebabCase == null) {
            return ""
        }
        
        return kebabCase.replaceAll(/-([a-z])/, { match, group -> group.toUpperCase() })
    }
    
    /**
     * Creates a random alphanumeric string.
     * 
     * Uses Groovy's range expressions and closures for concise implementation.
     * 
     * ```groovy
     * StringUtils.randomAlphanumeric(8)  // "Ab3dX7pQ" (random example)
     * StringUtils.randomAlphanumeric(0)  // ""
     * ```
     */
    static String randomAlphanumeric(int length) {
        if (length <= 0) {
            return ""
        }
        
        def chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        def random = new Random()
        def result = new StringBuilder()
        
        length.times { 
            result << chars[random.nextInt(chars.size())] 
        }
        
        return result.toString()
    }
    
    /**
     * Extracts a version number from a string.
     * 
     * Uses Groovy's powerful regular expression support with the =~ operator.
     * 
     * ```groovy
     * StringUtils.extractVersion("App version 1.2.3")  // "1.2.3"
     * StringUtils.extractVersion("v2.0-beta")          // "2.0-beta"
     * StringUtils.extractVersion("No version here")     // null
     * ```
     */
    static String extractVersion(String input) {
        if (input == null || input.isEmpty()) {
            return null
        }
        
        def matcher = input =~ /(\d+\.\d+(\.\d+)?(-[a-zA-Z0-9]+)?)/
        return matcher.find() ? matcher.group(1) : null
    }
    
    /**
     * Compares two version strings.
     * 
     * Uses Groovy's tokenize() method and spaceship operator for clean implementation.
     * 
     * ```groovy
     * StringUtils.compareVersions("1.2.3", "1.2.4")    // negative (first < second)
     * StringUtils.compareVersions("2.0", "1.9.9")      // positive (first > second)
     * StringUtils.compareVersions("1.0", "1.0.0")      // negative (first < second)
     * ```
     */
    static int compareVersions(String version1, String version2) {
        if (version1 == version2) {
            return 0
        }
        
        if (version1 == null) {
            return -1
        }
        
        if (version2 == null) {
            return 1
        }
        
        def v1Parts = version1.tokenize('.')
        def v2Parts = version2.tokenize('.')
        
        for (int i = 0; i < Math.min(v1Parts.size(), v2Parts.size()); i++) {
            def v1Part = parseVersionPart(v1Parts[i])
            def v2Part = parseVersionPart(v2Parts[i])
            
            if (v1Part != v2Part) {
                return v1Part <=> v2Part
            }
        }
        
        return v1Parts.size() <=> v2Parts.size()
    }
    
    /**
     * Helper method to parse version part strings.
     * 
     * Handles non-numeric suffixes in version components.
     */
    private static int parseVersionPart(String part) {
        def matcher = part =~ /^(\d+).*/
        if (matcher.matches()) {
            return matcher.group(1).toInteger()
        }
        return 0
    }
}