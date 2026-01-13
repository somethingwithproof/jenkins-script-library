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

import jenkins.model.Jenkins
import hudson.model.TaskListener
import hudson.security.Permission
import hudson.security.ACL

import java.util.logging.Logger

/**
 * Provides utilities for detecting and working with different Jenkins execution environments.
 *
 * <p>This class helps scripts work seamlessly across different contexts:</p>
 * <ul>
 *   <li>Jenkins Script Console (web UI)</li>
 *   <li>Jenkins CLI (command line)</li>
 *   <li>Pipeline scripts (Jenkinsfile)</li>
 *   <li>Shared Library steps (vars/)</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Get Jenkins instance regardless of environment
 * def jenkins = JenkinsEnvironment.getJenkinsInstance()
 *
 * // Check execution context
 * if (JenkinsEnvironment.isScriptConsole()) {
 *     println "Running in Script Console"
 * }
 *
 * // Use idiomatic permission checking
 * JenkinsEnvironment.checkAdminPermission()
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.2.0
 */
class JenkinsEnvironment {
    private static final Logger LOGGER = Logger.getLogger(JenkinsEnvironment.class.getName())

    /**
     * Execution context types.
     */
    enum ExecutionContext {
        SCRIPT_CONSOLE,
        CLI,
        PIPELINE,
        SHARED_LIBRARY,
        UNKNOWN
    }

    /**
     * Gets the Jenkins instance in an environment-agnostic way.
     *
     * <p>Works in Script Console, CLI, and Pipeline contexts.</p>
     *
     * @return The Jenkins instance
     * @throws IllegalStateException if Jenkins is not available
     */
    static Jenkins getJenkinsInstance() {
        Jenkins jenkins = Jenkins.getInstanceOrNull()
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins instance is not available. " +
                "This script must be run within a Jenkins environment.")
        }
        return jenkins
    }

    /**
     * Detects the current execution context.
     *
     * @return The detected execution context
     */
    static ExecutionContext detectContext() {
        // Check for Script Console indicators
        if (isScriptConsole()) {
            return ExecutionContext.SCRIPT_CONSOLE
        }

        // Check for Pipeline context
        if (isPipelineContext()) {
            return ExecutionContext.PIPELINE
        }

        // Check for CLI context
        if (isCliContext()) {
            return ExecutionContext.CLI
        }

        return ExecutionContext.UNKNOWN
    }

    /**
     * Checks if running in Jenkins Script Console.
     *
     * <p>The Script Console typically has pre-injected bindings like 'out'.</p>
     *
     * @return true if running in Script Console
     */
    static boolean isScriptConsole() {
        try {
            // Script Console typically runs with a specific thread context
            def currentThread = Thread.currentThread()
            def threadName = currentThread.name

            // Script Console threads are often named "Handling ..." or have specific patterns
            if (threadName?.contains("Handling") || threadName?.contains("script")) {
                return true
            }

            // Check for Groovy script console class in call stack
            def stackTrace = currentThread.stackTrace
            return stackTrace.any { it.className.contains("groovy.ui.Console") ||
                                    it.className.contains("ScriptConsole") ||
                                    it.className.contains("GroovyShellDecoratorImpl") }
        } catch (Exception e) {
            LOGGER.fine("Could not detect Script Console context: ${e.message}")
            return false
        }
    }

    /**
     * Checks if running in a Pipeline context.
     *
     * @return true if running in Pipeline
     */
    static boolean isPipelineContext() {
        try {
            // Check for CPS-transformed classes in stack
            def stackTrace = Thread.currentThread().stackTrace
            return stackTrace.any { it.className.contains("WorkflowScript") ||
                                    it.className.contains("CpsThread") ||
                                    it.className.contains("CpsScript") }
        } catch (Exception e) {
            LOGGER.fine("Could not detect Pipeline context: ${e.message}")
            return false
        }
    }

    /**
     * Checks if running in CLI context.
     *
     * @return true if running from CLI
     */
    static boolean isCliContext() {
        try {
            def stackTrace = Thread.currentThread().stackTrace
            return stackTrace.any { it.className.contains("CLICommand") ||
                                    it.className.contains("CliProtocol") }
        } catch (Exception e) {
            LOGGER.fine("Could not detect CLI context: ${e.message}")
            return false
        }
    }

    /**
     * Checks for admin permission using Jenkins idiomatic pattern.
     *
     * <p>Uses {@code checkPermission} which automatically throws
     * {@code AccessDeniedException} if the user lacks rights.</p>
     *
     * @throws hudson.security.AccessDeniedException if user lacks admin permission
     */
    static void checkAdminPermission() {
        getJenkinsInstance().checkPermission(Jenkins.ADMINISTER)
    }

    /**
     * Checks for a specific permission using Jenkins idiomatic pattern.
     *
     * @param permission The permission to check
     * @throws hudson.security.AccessDeniedException if user lacks the permission
     */
    static void checkPermission(Permission permission) {
        ValidationUtils.requireNonNull(permission, "Permission")
        getJenkinsInstance().checkPermission(permission)
    }

    /**
     * Checks if the current user has admin permission.
     *
     * <p>Unlike {@link #checkAdminPermission()}, this returns a boolean
     * instead of throwing an exception.</p>
     *
     * @return true if user has admin permission
     */
    static boolean hasAdminPermission() {
        try {
            return getJenkinsInstance().hasPermission(Jenkins.ADMINISTER)
        } catch (Exception e) {
            LOGGER.fine("Could not check admin permission: ${e.message}")
            return false
        }
    }

    /**
     * Gets a PrintStream for output appropriate to the current context.
     *
     * <p>In Script Console, returns System.out. In Pipeline, should use
     * TaskListener from the build context.</p>
     *
     * @param listener Optional TaskListener for Pipeline context
     * @return PrintStream for output
     */
    static PrintStream getOutputStream(TaskListener listener = null) {
        if (listener != null) {
            return listener.getLogger()
        }
        return System.out
    }

    /**
     * Checks if a Jenkins plugin is installed and active.
     *
     * @param pluginShortName The plugin short name (e.g., "git", "ec2")
     * @return true if the plugin is installed and active
     */
    static boolean isPluginInstalled(String pluginShortName) {
        ValidationUtils.requireNonEmpty(pluginShortName, "Plugin short name")
        try {
            def jenkins = getJenkinsInstance()
            def plugin = jenkins.getPlugin(pluginShortName)
            return plugin != null && plugin.wrapper?.isActive()
        } catch (Exception e) {
            LOGGER.fine("Could not check plugin '${pluginShortName}': ${e.message}")
            return false
        }
    }

    /**
     * Safely loads a class that may depend on an optional plugin.
     *
     * <p>Use this when your code needs to work with classes from plugins
     * that may or may not be installed.</p>
     *
     * @param className The fully qualified class name
     * @param requiredPlugin The plugin that provides this class (for error messages)
     * @return The loaded Class, or null if not available
     */
    static Class<?> loadOptionalClass(String className, String requiredPlugin = null) {
        try {
            return Class.forName(className)
        } catch (ClassNotFoundException e) {
            if (requiredPlugin) {
                LOGGER.info("Class '${className}' not available. " +
                    "Install the '${requiredPlugin}' plugin to enable this feature.")
            }
            return null
        }
    }

    /**
     * Gets the Jenkins version.
     *
     * @return The Jenkins version string
     */
    static String getJenkinsVersion() {
        return Jenkins.VERSION
    }

    /**
     * Checks if the Jenkins version is at least the specified version.
     *
     * @param requiredVersion The minimum required version (e.g., "2.361.4")
     * @return true if current version meets or exceeds the requirement
     */
    static boolean isJenkinsVersionAtLeast(String requiredVersion) {
        ValidationUtils.requireNonEmpty(requiredVersion, "Required version")
        return StringUtils.compareVersions(Jenkins.VERSION, requiredVersion) >= 0
    }

    /**
     * Runs a closure with SYSTEM privileges.
     *
     * <p>Use sparingly and only when absolutely necessary for operations
     * that require elevated permissions.</p>
     *
     * @param action The closure to execute with SYSTEM privileges
     * @return The result of the closure
     */
    static <T> T runAsSystem(Closure<T> action) {
        return ACL.impersonate(ACL.SYSTEM, action)
    }
}
