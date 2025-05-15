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
 * Provides standardized validation methods for common checks throughout the library.
 * 
 * This utility class centralizes validation logic to ensure consistent error handling
 * and reduce code duplication. All validation methods follow similar patterns:
 * - They return the validated value if valid
 * - They throw appropriate exceptions with descriptive messages if invalid
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */
class ValidationUtils {
    
    /**
     * Validates that an object is not null.
     * 
     * @param object The object to validate
     * @param paramName The parameter name for the error message
     * @return The validated object
     * @throws IllegalArgumentException if the object is null
     */
    static <T> T requireNonNull(T object, String paramName) {
        if (object == null) {
            throw new IllegalArgumentException("${paramName} must not be null")
        }
        return object
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param string The string to validate
     * @param paramName The parameter name for the error message
     * @return The validated string
     * @throws IllegalArgumentException if the string is null or empty
     */
    static String requireNonEmpty(String string, String paramName) {
        if (string == null || string.trim().isEmpty()) {
            throw new IllegalArgumentException("${paramName} must not be null or empty")
        }
        return string.trim()
    }
    
    /**
     * Validates that a number is positive (greater than zero).
     * 
     * @param value The number to validate
     * @param paramName The parameter name for the error message
     * @param defaultValue Default value to return if the provided value is not positive
     * @return The validated value or default value
     */
    static int requirePositive(int value, String paramName, int defaultValue) {
        if (value <= 0) {
            return defaultValue
        }
        return value
    }
    
    /**
     * Validates that a file exists.
     * 
     * @param filePath The file path to validate
     * @param paramName The parameter name for the error message
     * @return The validated file path
     * @throws IllegalArgumentException if the file does not exist
     */
    static String requireFileExists(String filePath, String paramName) {
        if (filePath == null || !(new File(filePath).exists())) {
            throw new IllegalArgumentException("${paramName} file does not exist: ${filePath}")
        }
        return filePath
    }
    
    /**
     * Validates that a directory exists.
     * 
     * @param dirPath The directory path to validate
     * @param paramName The parameter name for the error message
     * @return The validated directory path
     * @throws IllegalArgumentException if the directory does not exist
     */
    static String requireDirectoryExists(String dirPath, String paramName) {
        File dir = new File(dirPath)
        if (dirPath == null || !dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("${paramName} directory does not exist: ${dirPath}")
        }
        return dirPath
    }
    
    /**
     * Validates that a value is within a range.
     * 
     * @param value The value to validate
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param paramName The parameter name for the error message
     * @return The validated value
     * @throws IllegalArgumentException if the value is out of range
     */
    static int requireInRange(int value, int min, int max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("${paramName} must be between ${min} and ${max}")
        }
        return value
    }
}