// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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
    
    // String constants to avoid duplication
    private static final String EMPTY_STRING = ""
    private static final String UNDERSCORE = "_"
    private static final String DOT = "."
    private static final String CAMEL_TO_KEBAB_PATTERN1 = '$1-$2'

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
            return EMPTY_STRING
        }
        
        // Replace characters that are not allowed in Jenkins job names
        return input
            .replaceAll('[\\\\/?%*:|"<>]', UNDERSCORE) // Replace illegal chars with underscore
            .replaceAll('\\s+', UNDERSCORE)            // Replace whitespace with underscore
            .replaceAll('__+', UNDERSCORE)             // Replace multiple underscores with a single one
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
        
        return ErrorHandler.withErrorHandling("parsing integer value from '" + str + "'", {
            return str.trim().toInteger()
        }, LOGGER, defaultValue)
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
        if (str == null || maxLength <= 0) {
            return EMPTY_STRING
        }

        if (str.length() <= maxLength) {
            return str
        }

        String effectiveEllipsis = ellipsis ?: EMPTY_STRING
        int truncateIndex = maxLength - effectiveEllipsis.length()
        if (truncateIndex <= 0) {
            return str.substring(0, maxLength)
        }

        return str.substring(0, truncateIndex) + effectiveEllipsis
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
            return EMPTY_STRING
        }
        
        String displayValue = sensitive ? "*****" : (paramValue ?: EMPTY_STRING)
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
            return EMPTY_STRING
        }
        
        // Special case for all-caps acronyms like "ABC"
        if (camelCase.matches("[A-Z]+")) {
            return camelCase.split(EMPTY_STRING).join("-").toLowerCase()
        }
        
        return camelCase
            .replaceAll(/([a-z0-9])([A-Z])/, CAMEL_TO_KEBAB_PATTERN1)
            .replaceAll(/([A-Z])([A-Z][a-z])/, CAMEL_TO_KEBAB_PATTERN1)
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
            return EMPTY_STRING
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
            return EMPTY_STRING
        }
        
        def chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        def random = new java.security.SecureRandom()
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

        if (version1.isEmpty() && version2.isEmpty()) {
            return 0
        }
        if (version1.isEmpty()) {
            return -1
        }
        if (version2.isEmpty()) {
            return 1
        }

        // Strip SemVer build metadata (+...) before comparing
        String clean1 = stripBuildMetadata(version1.trim())
        String clean2 = stripBuildMetadata(version2.trim())

        int dash1 = clean1.indexOf('-')
        int dash2 = clean2.indexOf('-')

        String core1 = dash1 >= 0 ? clean1.substring(0, dash1) : clean1
        String core2 = dash2 >= 0 ? clean2.substring(0, dash2) : clean2
        String qualifier1 = dash1 >= 0 ? clean1.substring(dash1 + 1) : null
        String qualifier2 = dash2 >= 0 ? clean2.substring(dash2 + 1) : null

        List<String> v1Parts = core1.tokenize(DOT)
        List<String> v2Parts = core2.tokenize(DOT)
        int maxLen = Math.max(v1Parts.size(), v2Parts.size())

        for (int i = 0; i < maxLen; i++) {
            String raw1 = i < v1Parts.size() ? v1Parts[i] : null
            String raw2 = i < v2Parts.size() ? v2Parts[i] : null

            int cmp = compareVersionSegment(raw1, raw2)
            if (cmp != 0) {
                return cmp
            }
        }

        // Numeric cores are equal: release > pre-release qualifier
        if (qualifier1 == null && qualifier2 == null) {
            return 0
        }
        if (qualifier1 == null) {
            return 1
        }
        if (qualifier2 == null) {
            return -1
        }

        return compareQualifiers(qualifier1, qualifier2)
    }

    private static String stripBuildMetadata(String version) {
        int plusIdx = version.indexOf('+')
        return plusIdx >= 0 ? version.substring(0, plusIdx) : version
    }

    private static int compareVersionSegment(String seg1, String seg2) {
        String s1 = seg1 ?: '0'
        String s2 = seg2 ?: '0'

        def m1 = (s1 =~ /^(\d+)(.*)$/)
        def m2 = (s2 =~ /^(\d+)(.*)$/)
        boolean num1 = m1.matches()
        boolean num2 = m2.matches()

        if (num1 && num2) {
            BigInteger n1 = new BigInteger(m1.group(1))
            BigInteger n2 = new BigInteger(m2.group(1))
            int numCmp = n1.compareTo(n2)
            if (numCmp != 0) {
                return numCmp
            }
            String rem1 = m1.group(2)
            String rem2 = m2.group(2)
            if (rem1 == rem2) {
                return 0
            }
            if (rem1.isEmpty()) {
                return 1
            }
            if (rem2.isEmpty()) {
                return -1
            }
            return rem1.compareToIgnoreCase(rem2) <=> 0
        }

        // Non-numeric token (e.g. JEP-229 hash token like v1234abcd) compared against missing segment
        if (seg1 == null && !num2) {
            return -1
        }
        if (seg2 == null && !num1) {
            return 1
        }
        if (num1 != num2) {
            return num1 ? 1 : -1
        }
        return s1.compareToIgnoreCase(s2) <=> 0
    }

    private static int compareQualifiers(String q1, String q2) {
        List<String> t1 = q1.tokenize('.-')
        List<String> t2 = q2.tokenize('.-')
        int maxLen = Math.max(t1.size(), t2.size())
        for (int i = 0; i < maxLen; i++) {
            if (i >= t1.size()) {
                return -1
            }
            if (i >= t2.size()) {
                return 1
            }
            String p1 = t1[i]
            String p2 = t2[i]
            if (p1 ==~ /\d+/ && p2 ==~ /\d+/) {
                int cmp = new BigInteger(p1).compareTo(new BigInteger(p2))
                if (cmp != 0) {
                    return cmp
                }
            } else {
                int cmp = p1.compareToIgnoreCase(p2) <=> 0
                if (cmp != 0) {
                    return cmp
                }
            }
        }
        return 0
    }
}