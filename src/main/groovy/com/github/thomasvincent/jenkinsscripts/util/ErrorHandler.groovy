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
 * Provides standardized error handling for Jenkins scripts.
 * 
 * This utility centralizes error handling logic to ensure:
 * - Consistent error reporting
 * - Proper logging with appropriate log levels
 * - Helpful user-facing error messages
 * 
 * @author Thomas Vincent
 * @since 1.1.0
 */
class ErrorHandler {
    
    /**
     * Handles an exception consistently, logging it with the appropriate level.
     * 
     * @param operation Description of the operation that failed 
     * @param e The exception to handle
     * @param logger The logger to use
     * @param level Optional log level (defaults to SEVERE)
     */
    static void handleError(String operation, Exception e, Logger logger, Level level = Level.SEVERE) {
        String errorMessage = formatErrorMessage(operation, e)
        logger.log(level, errorMessage, e)
    }
    
    /**
     * Handles an exception and returns a default value.
     * 
     * Use this method in cases where the operation should continue despite the error,
     * but with a fallback value.
     * 
     * @param operation Description of the operation that failed
     * @param e The exception to handle
     * @param logger The logger to use
     * @param defaultValue The default value to return
     * @param level Optional log level (defaults to WARNING)
     * @return The default value
     */
    static <T> T handleErrorWithDefault(String operation, Exception e, Logger logger, T defaultValue, Level level = Level.WARNING) {
        String errorMessage = formatErrorMessage(operation, e)
        logger.log(level, errorMessage, e)
        return defaultValue
    }
    
    /**
     * Wraps an operation with standard error handling.
     * 
     * @param operation Description of the operation
     * @param action The closure to execute
     * @param logger The logger to use
     * @param defaultValue The default value to return on error (if null, rethrows the exception)
     * @return The result of the action or defaultValue on error
     */
    static <T> T withErrorHandling(String operation, Closure<T> action, Logger logger, T defaultValue = null) {
        try {
            return action()
        } catch (Exception e) {
            if (defaultValue == null) {
                handleError(operation, e, logger)
                throw e
            } else {
                return handleErrorWithDefault(operation, e, logger, defaultValue)
            }
        }
    }
    
    /**
     * Formats an error message for consistent presentation.
     * 
     * @param operation Description of the operation that failed
     * @param e The exception that occurred
     * @return Formatted error message
     */
    private static String formatErrorMessage(String operation, Exception e) {
        StringBuilder errorMessage = new StringBuilder()
        errorMessage.append("Error during ${operation}: ${e.message}")
        
        // Add root cause if available
        Throwable cause = e.cause
        if (cause && cause != e) {
            errorMessage.append(" (Caused by: ${cause.class.simpleName}: ${cause.message})")
        }
        
        return errorMessage.toString()
    }
}