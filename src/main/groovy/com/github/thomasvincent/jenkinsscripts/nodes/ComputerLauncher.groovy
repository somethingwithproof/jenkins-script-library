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
import java.util.logging.Level
import java.util.logging.Logger

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
        if (computerName == null) {
            LOGGER.warning("No computer name provided.")
            return false
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
        
        return startComputer(computer)
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
    int startAllOfflineComputers() {
        int startedCount = 0
        
        jenkins.computers.each { computer ->
            if (!(computer instanceof Jenkins.MasterComputer) && computer.offline) {
                if (startComputer(computer)) {
                    startedCount++
                }
            }
        }
        
        LOGGER.info("Started ${startedCount} offline computers")
        return startedCount
    }
    
    /**
     * Starts a specific computer.
     * 
     * Internal helper that manages connect calls with timeout handling.
     * Uses Groovy's exception handling capabilities.
     */
    private boolean startComputer(Computer computer) {
        if (!computer.offline) {
            LOGGER.info("Computer ${computer.name} is already online.")
            return true
        }
        
        try {
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start computer ${computer.name}", e)
            return false
        }
    }
}