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

package com.github.thomasvincent.jenkinsscripts.jobs

import jenkins.model.Jenkins
import hudson.model.Queue
import hudson.model.queue.QueueTaskFuture
import hudson.model.Job
import hudson.model.Run
import hudson.model.Computer
import hudson.model.Executor
import hudson.model.Label
import hudson.model.Node
import java.util.concurrent.TimeUnit

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages Jenkins job scheduling and throttling.
 * 
 * Provides mechanisms to optimize job scheduling, implement rate limiting, and balance workloads.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobScheduler {
    private static final Logger LOGGER = Logger.getLogger(JobScheduler.class.getName())
    
    private final Jenkins jenkins
    private Map<String, ThrottleConfig> jobThrottling = [:]
    private Map<String, ThrottleConfig> labelThrottling = [:]
    
    /**
     * Creates a JobScheduler instance.
     * 
     * ```groovy
     * def scheduler = new JobScheduler(Jenkins.get())
     * ```
     * 
     * @param jenkins Jenkins instance
     */
    JobScheduler(Jenkins jenkins) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
    }
    
    /**
     * Analyzes the job queue and executor load distribution.
     * 
     * ```groovy
     * def report = scheduler.analyzeQueue()
     * println "Queue size: ${report.queueSize}"
     * println "Blocked jobs: ${report.blockedJobs.size()}"
     * ```
     * 
     * @return QueueAnalysisReport with current state
     */
    QueueAnalysisReport analyzeQueue() {
        QueueAnalysisReport report = new QueueAnalysisReport()
        
        return ErrorHandler.withErrorHandling("analyzing job queue", {
            // Analyze queue items
            Queue.Item[] queueItems = jenkins.queue.items
            report.queueSize = queueItems.length
            
            // Categorize queue items
            queueItems.each { item ->
                String jobName = item.task.name
                
                if (item.blocked) {
                    report.blockedJobs.add(jobName)
                }
                
                if (item.stuck) {
                    report.stuckJobs.add(jobName)
                }
                
                // Track job labels
                Label label = item.assignedLabel
                if (label) {
                    String labelName = label.name
                    report.labelDemand[labelName] = (report.labelDemand[labelName] ?: 0) + 1
                }
            }
            
            // Analyze executor load
            for (Computer computer : jenkins.computers) {
                String nodeName = computer.name ?: "master"
                int totalExecutors = computer.numExecutors
                int busyExecutors = 0
                
                // Skip offline nodes
                if (computer.offline) {
                    report.offlineNodes.add(nodeName)
                    continue
                }
                
                // Count busy executors
                for (Executor executor : computer.executors) {
                    if (executor.busy) {
                        busyExecutors++
                        
                        // Record what's running
                        Queue.Executable executable = executor.currentExecutable
                        if (executable instanceof Run) {
                            Run run = (Run) executable
                            String jobName = run.parent.fullName
                            report.runningJobs.add(jobName)
                        }
                    }
                }
                
                // Calculate utilization
                if (totalExecutors > 0) {
                    double utilization = busyExecutors * 100.0 / totalExecutors
                    report.nodeUtilization[nodeName] = utilization
                    
                    // Track executor availability
                    report.availableExecutors[nodeName] = totalExecutors - busyExecutors
                }
                
                // Track labels for this node
                Set<String> labels = computer.assignedLabels.collect { it.name }
                labels.each { label ->
                    if (label && !label.startsWith("node-")) {
                        if (!report.labelCapacity.containsKey(label)) {
                            report.labelCapacity[label] = 0
                        }
                        report.labelCapacity[label] += totalExecutors
                    }
                }
            }
            
            // Calculate resource pressure
            report.labelDemand.each { label, demand ->
                int capacity = report.labelCapacity[label] ?: 0
                if (capacity > 0) {
                    report.labelPressure[label] = demand * 100.0 / capacity
                } else {
                    report.labelPressure[label] = -1  // No capacity
                }
            }
            
            return report
        }, LOGGER, new QueueAnalysisReport())
    }
    
    /**
     * Sets throttle config for a specific job.
     * 
     * ```groovy
     * // Limit a job to max 2 concurrent builds
     * scheduler.setJobThrottle("intensive-job", 2)
     * 
     * // Limit a job with rate limiting
     * scheduler.setJobThrottle("api-job", 5, 60)  // 5 builds per minute
     * ```
     * 
     * @param jobName Name of job to throttle
     * @param maxConcurrent Maximum concurrent builds allowed
     * @param periodSeconds Optional time period for rate limiting
     * @return true if successful, false otherwise
     */
    boolean setJobThrottle(String jobName, int maxConcurrent, int periodSeconds = 0) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        return ErrorHandler.withErrorHandling("setting throttle for job ${jobName}", {
            Job job = jenkins.getItemByFullName(jobName, Job.class)
            if (!job) {
                LOGGER.warning("Job not found: ${jobName}")
                return false
            }
            
            // Create throttle config
            ThrottleConfig config = new ThrottleConfig(maxConcurrent, periodSeconds)
            jobThrottling[jobName] = config
            
            LOGGER.info("Set throttle for job ${jobName}: max ${maxConcurrent} concurrent" + 
                       (periodSeconds > 0 ? ", max ${maxConcurrent} per ${periodSeconds}s" : ""))
            return true
        }, LOGGER, false)
    }
    
    /**
     * Removes throttle config for a job.
     * 
     * ```groovy
     * scheduler.removeJobThrottle("intensive-job")
     * ```
     * 
     * @param jobName Name of job to remove throttling from
     * @return true if successful, false otherwise
     */
    boolean removeJobThrottle(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        if (jobThrottling.containsKey(jobName)) {
            jobThrottling.remove(jobName)
            LOGGER.info("Removed throttle for job ${jobName}")
            return true
        } else {
            LOGGER.info("No throttle found for job ${jobName}")
            return false
        }
    }
    
    /**
     * Sets throttle config for jobs with a specific label.
     * 
     * ```groovy
     * // Limit jobs with 'database' label to 3 concurrent builds
     * scheduler.setLabelThrottle("database", 3)
     * ```
     * 
     * @param labelName Name of label to throttle
     * @param maxConcurrent Maximum concurrent builds allowed
     * @param periodSeconds Optional time period for rate limiting
     * @return true if successful, false otherwise
     */
    boolean setLabelThrottle(String labelName, int maxConcurrent, int periodSeconds = 0) {
        labelName = ValidationUtils.requireNonEmpty(labelName, "Label name")
        
        // Create throttle config
        ThrottleConfig config = new ThrottleConfig(maxConcurrent, periodSeconds)
        labelThrottling[labelName] = config
        
        LOGGER.info("Set throttle for label ${labelName}: max ${maxConcurrent} concurrent" + 
                   (periodSeconds > 0 ? ", max ${maxConcurrent} per ${periodSeconds}s" : ""))
        return true
    }
    
    /**
     * Removes throttle config for a label.
     * 
     * ```groovy
     * scheduler.removeLabelThrottle("database")
     * ```
     * 
     * @param labelName Name of label to remove throttling from
     * @return true if successful, false otherwise
     */
    boolean removeLabelThrottle(String labelName) {
        labelName = ValidationUtils.requireNonEmpty(labelName, "Label name")
        
        if (labelThrottling.containsKey(labelName)) {
            labelThrottling.remove(labelName)
            LOGGER.info("Removed throttle for label ${labelName}")
            return true
        } else {
            LOGGER.info("No throttle found for label ${labelName}")
            return false
        }
    }
    
    /**
     * Analyzes current resource usage and generates recommendations.
     * 
     * ```groovy
     * def recommendations = scheduler.generateRecommendations()
     * recommendations.each { println it }
     * ```
     * 
     * @return List of scheduling recommendations
     */
    List<String> generateRecommendations() {
        List<String> recommendations = []
        
        QueueAnalysisReport report = analyzeQueue()
        
        // Check for overloaded labels
        report.labelPressure.each { label, pressure ->
            if (pressure > 90) {
                recommendations.add("High demand for label '${label}': Consider adding more executors (${pressure.round(1)}% utilization)")
            }
        }
        
        // Check for underutilized nodes
        report.nodeUtilization.findAll { node, utilization -> utilization < 30 }.each { node, utilization ->
            recommendations.add("Low utilization for node '${node}': Consider consolidating workloads (${utilization.round(1)}% utilization)")
        }
        
        // Check for stuck jobs
        if (report.stuckJobs.size() > 0) {
            recommendations.add("${report.stuckJobs.size()} stuck jobs detected: ${report.stuckJobs.join(', ')}")
        }
        
        // Generate throttling recommendations
        getThrottleRecommendations(report).each { recommendations.add(it) }
        
        return recommendations
    }
    
    /**
     * Applies recommended throttle settings automatically.
     * 
     * ```groovy
     * def applied = scheduler.autoOptimizeThrottling()
     * println "Applied ${applied.size()} throttle settings"
     * ```
     * 
     * @return List of applied changes
     */
    List<String> autoOptimizeThrottling() {
        List<String> applied = []
        
        // Get recommendations and apply them
        QueueAnalysisReport report = analyzeQueue()
        Map<String, Integer> recommendations = calculateThrottleSettings(report)
        
        recommendations.each { jobName, limit ->
            // Apply the throttle
            boolean success = setJobThrottle(jobName, limit)
            if (success) {
                applied.add("Set throttle for ${jobName}: max ${limit} concurrent builds")
            }
        }
        
        return applied
    }
    
    /**
     * Gets the current job throttling configurations.
     * 
     * ```groovy
     * def throttles = scheduler.getJobThrottles()
     * throttles.each { job, config ->
     *     println "${job}: max ${config.maxConcurrent} concurrent"
     * }
     * ```
     * 
     * @return Map of job names to throttle configs
     */
    Map<String, ThrottleConfig> getJobThrottles() {
        return new HashMap<>(jobThrottling)
    }
    
    /**
     * Gets the current label throttling configurations.
     * 
     * ```groovy
     * def throttles = scheduler.getLabelThrottles()
     * throttles.each { label, config ->
     *     println "${label}: max ${config.maxConcurrent} concurrent"
     * }
     * ```
     * 
     * @return Map of label names to throttle configs
     */
    Map<String, ThrottleConfig> getLabelThrottles() {
        return new HashMap<>(labelThrottling)
    }
    
    /**
     * Generates throttle recommendations based on queue analysis.
     * 
     * @param report Queue analysis report
     * @return List of throttle recommendations
     */
    private List<String> getThrottleRecommendations(QueueAnalysisReport report) {
        List<String> recommendations = []
        Map<String, Integer> throttleSettings = calculateThrottleSettings(report)
        
        throttleSettings.each { jobName, limit ->
            ThrottleConfig existing = jobThrottling[jobName]
            if (!existing || existing.maxConcurrent != limit) {
                recommendations.add("Consider throttling job '${jobName}' to max ${limit} concurrent builds")
            }
        }
        
        return recommendations
    }
    
    /**
     * Calculates recommended throttle settings based on job history.
     * 
     * @param report Queue analysis report
     * @return Map of job names to recommended concurrent build limits
     */
    private Map<String, Integer> calculateThrottleSettings(QueueAnalysisReport report) {
        Map<String, Integer> throttleSettings = [:]

        // Count active builds directly from computers/executors in O(E) instead of scanning full build history
        Map<String, Integer> concurrentByJob = [:]
        Map<String, Job> jobsByName = [:]

        for (Computer computer : jenkins.computers) {
            if (computer == null || computer.offline) {
                continue
            }
            for (Executor executor : computer.executors) {
                if (executor.busy) {
                    Queue.Executable executable = executor.currentExecutable
                    if (executable instanceof Run) {
                        Run run = (Run) executable
                        Job parentJob = run.parent
                        if (parentJob != null) {
                            String jobName = parentJob.fullName
                            concurrentByJob[jobName] = (concurrentByJob[jobName] ?: 0) + 1
                            jobsByName[jobName] = parentJob
                        }
                    }
                }
            }
        }

        concurrentByJob.each { String jobName, int running ->
            if (running > 3) {
                throttleSettings[jobName] = calculateRecommendedLimit(jobsByName[jobName], running)
            }
        }

        return throttleSettings
    }
    
    /**
     * Calculates recommended concurrent builds limit for a job.
     * 
     * @param job Job to calculate for
     * @param currentConcurrent Current concurrent builds
     * @return Recommended limit
     */
    private int calculateRecommendedLimit(Job job, int currentConcurrent) {
        // In a real implementation, this would analyze:
        // - Historical resource usage
        // - Build duration
        // - Node utilization
        // - Impact on other jobs
        
        // For this simplified implementation, we'll use a basic heuristic
        if (currentConcurrent > 10) {
            return 8  // Heavily throttle extremely concurrent jobs
        } else if (currentConcurrent > 5) {
            return 5  // Moderately throttle highly concurrent jobs
        } else {
            return 3  // Lightly throttle concurrent jobs
        }
    }

    /**
     * Counts the number of currently running builds for a specific job.
     * @param jobFullName The full name of the job.
     * @return The number of currently building instances of the job, or 0 if job not found.
     */
    private int countRunningBuildsForJob(String jobFullName) {
        if (!jobFullName) {
            return 0
        }

        return ErrorHandler.withErrorHandling("counting running builds for job ${jobFullName}", {
            int running = 0
            for (Computer computer : jenkins.computers) {
                if (computer == null || computer.offline) {
                    continue
                }
                for (Executor executor : computer.executors) {
                    if (executor.busy) {
                        Queue.Executable executable = executor.currentExecutable
                        if (executable instanceof Run && ((Run) executable).parent?.fullName == jobFullName) {
                            running++
                        }
                    }
                }
            }
            return running
        }, LOGGER, 0)
    }

    /**
     * Counts the number of currently running builds on nodes assigned to a specific label.
     * @param labelName The name of the label.
     * @return The total number of builds currently running on agents with this label.
     */
    private int countRunningBuildsForLabel(String labelName) {
        if (!labelName) {
            return 0
        }

        Label label = jenkins.getLabel(labelName)
        if (label == null) {
            LOGGER.fine("Label not found while counting running builds: ${labelName}")
            return 0
        }

        return ErrorHandler.withErrorHandling("counting running builds for label ${labelName}", {
            int count = 0
            // Iterate through nodes associated with the label
            label.getNodes().each { Node node ->
                Computer computer = node.toComputer()
                if (computer != null && !computer.isOffline()) {
                    computer.getExecutors().each { Executor executor ->
                        if (executor.isBusy()) {
                            // This counts any busy executor on a node with the label.
                            // It doesn't check if the specific running job requires this specific label,
                            // which is a common simplification for label-based concurrency.
                            count++
                        }
                    }
                }
            }
            return count
        }, LOGGER, 0)
    }
}

/**
 * Configuration for throttling jobs.
 */
class ThrottleConfig {
    final int maxConcurrent
    final int periodSeconds
    final Deque<Long> executionTimes = new ArrayDeque<>()

    ThrottleConfig(int maxConcurrent, int periodSeconds) {
        this.maxConcurrent = maxConcurrent
        this.periodSeconds = periodSeconds
    }

    /**
     * Atomically checks concurrency/rate limits and records an execution if allowed.
     *
     * @param currentRunningExecutions Number of instances currently running for the associated job/label.
     * @return true if acquired (and recorded when rate-limited), false if throttled.
     */
    synchronized boolean tryAcquire(int currentRunningExecutions) {
        if (!isAllowedToStart(currentRunningExecutions)) {
            return false
        }
        recordExecution()
        return true
    }

    /**
     * Checks if a new build is allowed to start under this throttle config.
     * Considers both immediate concurrency and rate limiting over a period.
     *
     * @param currentRunningExecutions Number of instances currently running for the associated job/label.
     * @return true if allowed to start, false if throttled.
     */
    synchronized boolean isAllowedToStart(int currentRunningExecutions) {
        if (currentRunningExecutions >= maxConcurrent) {
            return false
        }

        if (periodSeconds > 0) {
            long cutoffTime = System.currentTimeMillis() - (periodSeconds * 1000L)
            evictExpired(cutoffTime)
            if (executionTimes.size() >= maxConcurrent) {
                return false
            }
        }

        return true
    }

    /**
     * Records a new execution under this throttle when rate limiting is enabled.
     * Skips timestamp recording when periodSeconds <= 0 to avoid unbounded growth.
     */
    synchronized void recordExecution() {
        if (periodSeconds <= 0) {
            return
        }
        long currentTime = System.currentTimeMillis()
        evictExpired(currentTime - (periodSeconds * 1000L))
        executionTimes.addLast(currentTime)
    }

    private void evictExpired(long cutoffTime) {
        while (!executionTimes.isEmpty() && executionTimes.peekFirst() < cutoffTime) {
            executionTimes.pollFirst()
        }
    }
}

/**
 * Report containing queue and executor analysis.
 */
class QueueAnalysisReport {
    int queueSize = 0
    List<String> blockedJobs = []
    List<String> stuckJobs = []
    List<String> runningJobs = []
    List<String> offlineNodes = []
    
    Map<String, Double> nodeUtilization = [:]
    Map<String, Integer> availableExecutors = [:]
    
    Map<String, Integer> labelDemand = [:]
    Map<String, Integer> labelCapacity = [:]
    Map<String, Double> labelPressure = [:]
    
    /**
     * Generates a formatted report string.
     * 
     * @return String representation of the report
     */
    String generateReport() {
        StringBuilder report = new StringBuilder()
        
        report.append("Jenkins Queue Analysis Report\n")
        report.append("============================\n")
        report.append("Generated: ${new Date()}\n\n")
        
        report.append("Queue Status:\n")
        report.append("- Queue size: ${queueSize}\n")
        report.append("- Blocked jobs: ${blockedJobs.size()}\n")
        report.append("- Stuck jobs: ${stuckJobs.size()}\n\n")
        
        report.append("Node Status:\n")
        if (offlineNodes.isEmpty()) {
            report.append("- All nodes online\n")
        } else {
            report.append("- Offline nodes: ${offlineNodes.join(', ')}\n")
        }
        
        report.append("\nExecutor Utilization:\n")
        nodeUtilization.sort { -it.value }.each { node, utilization ->
            report.append(String.format("- %-20s: %5.1f%% (%d available executors)\n", 
                                       node, utilization, availableExecutors[node] ?: 0))
        }
        
        report.append("\nLabel Resource Pressure:\n")
        labelPressure.sort { -it.value }.each { label, pressure ->
            if (pressure >= 0) {
                report.append(String.format("- %-20s: %5.1f%% (Demand: %d, Capacity: %d)\n", 
                                           label, pressure, labelDemand[label] ?: 0, labelCapacity[label] ?: 0))
            } else {
                report.append(String.format("- %-20s: No capacity (Demand: %d)\n", 
                                           label, labelDemand[label] ?: 0))
            }
        }
        
        return report.toString()
    }
}