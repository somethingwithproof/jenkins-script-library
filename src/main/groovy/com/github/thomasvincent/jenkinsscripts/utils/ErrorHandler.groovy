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

package com.github.thomasvincent.jenkinsscripts.utils

import groovy.transform.CompileStatic
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Provides standardized error handling across the Jenkins script library.
 * 
 * This utility class ensures consistent error handling patterns and proper
 * logging throughout the codebase.
 * 
 * @author Thomas Vincent
 * @since 1.4.0
 */
@CompileStatic
class ErrorHandler {
    
    private final Logger logger
    
    ErrorHandler(Logger logger) {
        this.logger = logger
    }
    
    /**
     * Executes a closure with standardized error handling.
     * 
     * @param context Description of the operation for error messages
     * @param closure The code to execute
     * @return The result of the closure execution
     * @throws RuntimeException if the closure throws an exception
     */
    def <T> T withErrorHandling(String context, Closure<T> closure) {
        try {
            return closure.call()
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "${context}: Security violation", e)
            throw new SecurityException("${context}: ${e.message}", e)
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "${context}: Invalid argument", e)
            throw new IllegalArgumentException("${context}: ${e.message}", e)
        } catch (Exception e) {
            logger.log(Level.SEVERE, "${context}: Unexpected error", e)
            throw new RuntimeException("${context}: ${e.message}", e)
        }
    }
    
    /**
     * Executes a closure with error recovery.
     * 
     * @param context Description of the operation
     * @param defaultValue Value to return on error
     * @param closure The code to execute
     * @return The result of the closure or default value on error
     */
    def <T> T withErrorRecovery(String context, T defaultValue, Closure<T> closure) {
        try {
            return closure.call()
        } catch (Exception e) {
            logger.log(Level.WARNING, "${context}: Error occurred, using default value", e)
            return defaultValue
        }
    }
    
    /**
     * Logs an error with context and optionally rethrows.
     * 
     * @param context Description of where the error occurred
     * @param error The error that occurred
     * @param rethrow Whether to rethrow the error
     */
    void handleError(String context, Throwable error, boolean rethrow = true) {
        logger.log(Level.SEVERE, "${context}: ${error.message}", error)
        
        if (rethrow) {
            if (error instanceof RuntimeException) {
                throw error
            } else {
                throw new RuntimeException("${context}: ${error.message}", error)
            }
        }
    }
    
    /**
     * Validates a required parameter.
     * 
     * @param paramName Parameter name for error messages
     * @param value The value to validate
     * @throws IllegalArgumentException if value is null
     */
    void requireNotNull(String paramName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("${paramName} must not be null")
        }
    }
    
    /**
     * Validates a required string parameter.
     * 
     * @param paramName Parameter name for error messages
     * @param value The string to validate
     * @throws IllegalArgumentException if value is null or empty
     */
    void requireNotEmpty(String paramName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("${paramName} must not be null or empty")
        }
    }
}