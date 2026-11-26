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

package com.github.thomasvincent.jenkinsscripts.nodes

import hudson.model.Computer
import jenkins.model.Jenkins
import java.util.concurrent.Future
import java.util.logging.Logger

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

/**
 * Utility for starting Jenkins agent computers.
 * 
 * Starts offline Jenkins computers with error handling and logging.
 * Safely manages individual agent startup or bulk operations.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class ComputerLauncher {

    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(ComputerLauncher.class.getName())
    
    /** The Jenkins instance to work with */
    private final Jenkins jenkins
    
    /**
     * Creates a ComputerLauncher instance.
     * 
     * Uses provided Jenkins instance or defaults to Jenkins.get().
     * 
     * ```groovy
     * def launcher = new ComputerLauncher(Jenkins.get())
     * def defaultLauncher = new ComputerLauncher(null) // Uses Jenkins.get()
     * ```
     */
    ComputerLauncher(Jenkins jenkins) {
        this.jenkins = jenkins ?: Jenkins.get()
        // No need to validate jenkins here as we use a default if null
    }
    
    /**
     * Starts a specific computer by name.
     * 
     * Validates existence and launches specified agent.
     * 
     * ```groovy
     * launcher.startComputer("worker-node-1")   // true if successful/already online
     * launcher.startComputer("nonexistent")     // false - computer not found
     * launcher.startComputer("master")         // false - can't start master
     * ```
     */
    boolean startComputer(String computerName) {
        try {
            computerName = ValidationUtils.requireNonEmpty(computerName, "Computer name")
        } catch (IllegalArgumentException e) {
            return ErrorHandler.handleErrorWithDefault("validating computer name", e, LOGGER, false)
        }
        
        Computer computer = jenkins.getComputer(computerName)
        if (computer == null) {
            LOGGER.warning("Computer with name ${computerName} not found.")
            return false
        }
        
        if (computer instanceof Jenkins.MasterComputer) {
            LOGGER.info("Cannot start master computer.")
            return false
        }
        
        return startComputerInternal(computer)
    }
    
    /**
     * Starts all offline computers except the master.
     * 
     * Iterates through agents and launches those that are offline.
     * Uses Groovy's each iteration for clean implementation.
     * 
     * ```groovy
     * def started = launcher.startAllOfflineComputers()
     * println "Started ${started} offline agents"  // Shows count of started agents
     * ```
     */
    int startAllOfflineComputers(long timeoutSeconds = 60L) {
        Map<Computer, Future<?>> pending = new LinkedHashMap<>()

        // Initiate all connection attempts concurrently
        for (Computer computer : jenkins.computers) {
            if (!(computer instanceof Jenkins.MasterComputer) && computer.offline) {
                try {
                    LOGGER.info("Attempting to start computer ${computer.name}...")
                    Future<?> future = computer.connect(false)
                    if (future != null) {
                        pending[computer] = future
                    }
                } catch (Exception e) {
                    ErrorHandler.handleError("starting computer ${computer.name}", e, LOGGER)
                }
            }
        }

        // Await all initiated connections against a single shared deadline
        int startedCount = 0
        long deadlineNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(timeoutSeconds)

        pending.each { Computer computer, Future<?> future ->
            long remainingNanos = Math.max(0L, deadlineNanos - System.nanoTime())
            try {
                future.get(remainingNanos, java.util.concurrent.TimeUnit.NANOSECONDS)
                LOGGER.info("Computer ${computer.name} started successfully.")
                startedCount++
            } catch (java.util.concurrent.TimeoutException e) {
                LOGGER.warning("Starting computer ${computer.name} exceeded the shared ${timeoutSeconds}s deadline. " +
                              "The connection attempt will continue in the background.")
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt()
                LOGGER.warning("Interrupted while waiting for computer ${computer.name} to start.")
            } catch (Exception e) {
                ErrorHandler.handleError("starting computer ${computer.name}", e, LOGGER)
            }
        }

        LOGGER.info("Started ${startedCount} offline computers")
        return startedCount
    }
    
    /**
     * Starts a specific computer internally.
     * 
     * Internal helper that manages connect calls with timeout handling.
     * Uses Groovy's exception handling capabilities.
     */
    private boolean startComputerInternal(Computer computer) {
        ValidationUtils.requireNonNull(computer, "Computer instance")
        
        if (!computer.offline) {
            LOGGER.info("Computer ${computer.name} is already online.")
            return true
        }
        
        return ErrorHandler.withErrorHandling("starting computer ${computer.name}", {
            LOGGER.info("Attempting to start computer ${computer.name}...")
            Future<?> connectionResult = computer.connect(false)
            
            // Wait for connection to complete, with a reasonable timeout
            try {
                connectionResult.get(60, java.util.concurrent.TimeUnit.SECONDS)
                LOGGER.info("Computer ${computer.name} started successfully.")
                return true
            } catch (java.util.concurrent.TimeoutException e) {
                LOGGER.warning("Starting computer ${computer.name} is taking longer than expected. " +
                              "The connection attempt will continue in the background.")
                return false
            }
        }, LOGGER, false)
    }
}