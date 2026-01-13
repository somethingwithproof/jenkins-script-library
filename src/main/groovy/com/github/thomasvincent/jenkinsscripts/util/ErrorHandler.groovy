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

import hudson.model.TaskListener
import java.io.PrintStream
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Provides standardized error handling for Jenkins scripts.
 *
 * <p>This utility centralizes error handling logic to ensure:</p>
 * <ul>
 *   <li>Consistent error reporting</li>
 *   <li>Proper logging with appropriate log levels</li>
 *   <li>Helpful user-facing error messages</li>
 *   <li>Support for Pipeline build console output via TaskListener</li>
 * </ul>
 *
 * <h3>Usage in Pipeline context:</h3>
 * <pre>
 * // Create handler with TaskListener for build console output
 * def handler = new ErrorHandler(listener)
 * handler.withErrorHandling("fetching data", {
 *     // your code here
 * })
 * </pre>
 *
 * <h3>Usage in Script Console / CLI:</h3>
 * <pre>
 * // Use static methods with Logger
 * ErrorHandler.withErrorHandling("operation", { ... }, LOGGER, defaultValue)
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.1.0
 */
class ErrorHandler {

    /** Optional TaskListener for Pipeline build console output */
    private final TaskListener taskListener

    /** Optional PrintStream for direct output (alternative to TaskListener) */
    private final PrintStream outputStream

    /** Logger for system-level logging */
    private final Logger logger

    /**
     * Creates an ErrorHandler with default system logging only.
     */
    ErrorHandler() {
        this.taskListener = null
        this.outputStream = null
        this.logger = Logger.getLogger(ErrorHandler.class.getName())
    }

    /**
     * Creates an ErrorHandler with TaskListener for Pipeline build console output.
     *
     * <p>When a TaskListener is provided, errors will be logged to both the
     * system logger AND the build console, making them visible to users in
     * the Jenkins UI.</p>
     *
     * @param listener The TaskListener from the build context
     */
    ErrorHandler(TaskListener listener) {
        this.taskListener = listener
        this.outputStream = listener?.getLogger()
        this.logger = Logger.getLogger(ErrorHandler.class.getName())
    }

    /**
     * Creates an ErrorHandler with a PrintStream for custom output.
     *
     * @param output The PrintStream to write errors to
     */
    ErrorHandler(PrintStream output) {
        this.taskListener = null
        this.outputStream = output
        this.logger = Logger.getLogger(ErrorHandler.class.getName())
    }

    /**
     * Creates an ErrorHandler with both TaskListener and custom Logger.
     *
     * @param listener The TaskListener from the build context
     * @param logger Custom logger for system logging
     */
    ErrorHandler(TaskListener listener, Logger logger) {
        this.taskListener = listener
        this.outputStream = listener?.getLogger()
        this.logger = logger ?: Logger.getLogger(ErrorHandler.class.getName())
    }

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
    static <T> T handleErrorWithDefault(
            String operation, Exception e, Logger logger, T defaultValue, Level level = Level.WARNING) {
        String errorMessage = formatErrorMessage(operation, e)
        logger.log(level, errorMessage, e)
        return defaultValue
    }

    /**
     * Wraps an operation with standard error handling (static version).
     *
     * @param operation Description of the operation
     * @param action The closure to execute
     * @param logger The logger to use
     * @param defaultValue The default value to return on error (if null, rethrows the exception)
     * @return The result of the action or defaultValue on error
     */
    @SuppressWarnings('CatchException')
    static <T> T withErrorHandling(String operation, Closure<T> action, Logger logger, T defaultValue = null) {
        try {
            return action()
        } catch (Exception e) { // NOSONAR - Generic error handler needs to catch all exceptions
            if (defaultValue == null) {
                handleError(operation, e, logger)
                throw e
            } else {
                return handleErrorWithDefault(operation, e, logger, defaultValue)
            }
        }
    }

    /**
     * Instance method for error handling with TaskListener support.
     *
     * <p>Logs errors to both the system logger AND the build console when
     * a TaskListener was provided in the constructor.</p>
     *
     * @param operation Description of the operation
     * @param action The closure to execute
     * @param defaultValue The default value to return on error (if null, rethrows)
     * @return The result of the action or defaultValue on error
     */
    @SuppressWarnings('CatchException')
    <T> T execute(String operation, Closure<T> action, T defaultValue = null) {
        try {
            return action()
        } catch (Exception e) {
            String errorMessage = formatErrorMessage(operation, e)

            // Log to system logger
            Level level = (defaultValue == null) ? Level.SEVERE : Level.WARNING
            logger.log(level, errorMessage, e)

            // Log to build console if available
            logToConsole(errorMessage, level)

            if (defaultValue == null) {
                throw e
            }
            return defaultValue
        }
    }

    /**
     * Logs an informational message to both system logger and build console.
     *
     * @param message The message to log
     */
    void info(String message) {
        logger.info(message)
        logToConsole(message, Level.INFO)
    }

    /**
     * Logs a warning message to both system logger and build console.
     *
     * @param message The message to log
     */
    void warn(String message) {
        logger.warning(message)
        logToConsole("[WARNING] ${message}", Level.WARNING)
    }

    /**
     * Logs an error message to both system logger and build console.
     *
     * @param message The message to log
     */
    void error(String message) {
        logger.severe(message)
        logToConsole("[ERROR] ${message}", Level.SEVERE)
    }

    /**
     * Logs an error with exception to both system logger and build console.
     *
     * @param message The message to log
     * @param e The exception that occurred
     */
    void error(String message, Exception e) {
        String fullMessage = formatErrorMessage(message, e)
        logger.log(Level.SEVERE, fullMessage, e)
        logToConsole("[ERROR] ${fullMessage}", Level.SEVERE)
    }

    /**
     * Logs a message to the build console if a TaskListener or PrintStream is available.
     *
     * @param message The message to log
     * @param level The log level (for potential formatting)
     */
    private void logToConsole(String message, Level level) {
        if (outputStream != null) {
            outputStream.println(message)
        }
    }

    /**
     * Formats an error message for consistent presentation.
     *
     * @param operation Description of the operation that failed
     * @param e The exception that occurred
     * @return Formatted error message
     */
    static String formatErrorMessage(String operation, Exception e) {
        StringBuilder errorMessage = new StringBuilder()
        if (operation) {
            errorMessage.append("Error during ${operation}")
        } else {
            errorMessage.append("Error")
        }
        if (e?.message) {
            errorMessage.append(": ${e.message}")
        }

        // Add root cause if available
        Throwable cause = e?.cause
        if (cause && cause != e) {
            errorMessage.append(" (Caused by: ${cause.class.simpleName}: ${cause.message ?: 'No message'})")
        }

        return errorMessage.toString()
    }

    /**
     * Creates a user-friendly error summary suitable for display in Jenkins UI.
     *
     * @param operation Description of the operation
     * @param e The exception
     * @param includeStackTrace Whether to include stack trace
     * @return Formatted HTML string for Jenkins UI
     */
    static String formatErrorForUI(String operation, Exception e, boolean includeStackTrace = false) {
        StringBuilder html = new StringBuilder()
        html.append("<div class='error'>")
        html.append("<strong>Error:</strong> ${escapeHtml(formatErrorMessage(operation, e))}")

        if (includeStackTrace && e != null) {
            html.append("<details><summary>Stack trace</summary>")
            html.append("<pre>")
            StringWriter sw = new StringWriter()
            e.printStackTrace(new PrintWriter(sw))
            html.append(escapeHtml(sw.toString()))
            html.append("</pre></details>")
        }

        html.append("</div>")
        return html.toString()
    }

    /**
     * Escapes HTML special characters.
     *
     * @param text Text to escape
     * @return HTML-safe text
     */
    private static String escapeHtml(String text) {
        if (text == null) return ""
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
