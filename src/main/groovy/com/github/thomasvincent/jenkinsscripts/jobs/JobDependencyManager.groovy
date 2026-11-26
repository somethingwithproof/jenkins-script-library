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
import hudson.model.Job
import hudson.model.TopLevelItem
import hudson.model.AbstractProject
import hudson.model.Cause
import hudson.model.CauseAction
import hudson.triggers.Trigger
import hudson.triggers.TriggerDescriptor

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Manages and visualizes Jenkins job dependencies.
 * 
 * Detects upstream/downstream relationships, circular dependencies, and orphaned jobs.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobDependencyManager {
    private static final Logger LOGGER = Logger.getLogger(JobDependencyManager.class.getName())
    
    private final Jenkins jenkins
    
    /**
     * Creates a JobDependencyManager instance.
     * 
     * ```groovy
     * def manager = new JobDependencyManager(Jenkins.get())
     * ```
     * 
     * @param jenkins Jenkins instance
     */
    JobDependencyManager(Jenkins jenkins) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
    }
    
    /**
     * Gets dependency graph for a specific job.
     * 
     * ```groovy
     * def graph = manager.getJobDependencyGraph("deployment-pipeline")
     * println "Upstream jobs: ${graph.upstreamJobs}"
     * println "Downstream jobs: ${graph.downstreamJobs}"
     * ```
     * 
     * @param jobName Name of job to analyze
     * @return JobDependencyGraph or null if job not found
     */
    JobDependencyGraph getJobDependencyGraph(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return null
        }
        
        return ErrorHandler.withErrorHandling("analyzing dependencies for job ${jobName}", {
            // Create graph for this job
            JobDependencyGraph graph = new JobDependencyGraph(job.fullName)
            
            // Find upstream and downstream jobs
            findUpstreamJobs(job, graph)
            findDownstreamJobs(job, graph)
            
            return graph
        }, LOGGER, null)
    }
    
    /**
     * Creates a complete dependency graph for all jobs.
     * 
     * ```groovy
     * def completeGraph = manager.getCompleteDependencyGraph()
     * def orphans = completeGraph.findOrphanedJobs()
     * def cycles = completeGraph.findCircularDependencies()
     * ```
     * 
     * @return CompleteDependencyGraph for all jobs
     */
    CompleteDependencyGraph getCompleteDependencyGraph() {
        CompleteDependencyGraph completeGraph = new CompleteDependencyGraph()

        List<Job> allJobs = jenkins.getAllItems(Job.class)

        // First pass: register every job node
        allJobs.each { Job job ->
            completeGraph.addJobGraph(new JobDependencyGraph(job.fullName))
        }

        // Second pass: populate downstream edges and reversed upstream edges in O(V + E)
        allJobs.each { Job job ->
            if (job instanceof AbstractProject) {
                AbstractProject project = (AbstractProject) job
                hudson.tasks.BuildTrigger trigger = project.getPublishersList().get(hudson.tasks.BuildTrigger.class)
                String childProjectsValue = trigger?.getChildProjectsValue()
                if (childProjectsValue) {
                    JobDependencyGraph upstreamGraph = completeGraph.jobGraphs[job.fullName]
                    childProjectsValue.split(',').each { String rawChild ->
                        String childName = rawChild.trim()
                        if (childName) {
                            upstreamGraph.downstreamJobs.add(childName)
                            JobDependencyGraph downstreamGraph = completeGraph.jobGraphs[childName]
                            if (downstreamGraph != null) {
                                downstreamGraph.upstreamJobs.add(job.fullName)
                            }
                        }
                    }
                }
            }
        }

        LOGGER.info("Created complete dependency graph for ${allJobs.size()} jobs")
        return completeGraph
    }
    
    /**
     * Adds a dependency between two jobs.
     * 
     * ```groovy
     * // Make test-job trigger when build-job completes
     * manager.addJobDependency("build-job", "test-job")
     * ```
     * 
     * @param upstreamJobName Name of upstream job
     * @param downstreamJobName Name of downstream job
     * @return true if successful, false otherwise
     */
    boolean addJobDependency(String upstreamJobName, String downstreamJobName) {
        upstreamJobName = ValidationUtils.requireNonEmpty(upstreamJobName, "Upstream job name")
        downstreamJobName = ValidationUtils.requireNonEmpty(downstreamJobName, "Downstream job name")
        
        return ErrorHandler.withErrorHandling("adding dependency from ${upstreamJobName} to ${downstreamJobName}", {
            // Get jobs
            AbstractProject upstreamJob = jenkins.getItemByFullName(upstreamJobName, AbstractProject.class)
            AbstractProject downstreamJob = jenkins.getItemByFullName(downstreamJobName, AbstractProject.class)
            
            if (!upstreamJob || !downstreamJob) {
                LOGGER.warning("Jobs not found or not buildable projects")
                return false
            }
            
            // Check for existing dependency
            if (hasDownstreamDependency(upstreamJob, downstreamJob)) {
                LOGGER.info("Dependency already exists")
                return true
            }
            
            // Add or update BuildTrigger publisher
            hudson.tasks.BuildTrigger existingTrigger = upstreamJob.getPublishersList().get(hudson.tasks.BuildTrigger.class)
            String existingChildren = existingTrigger?.getChildProjectsValue()
            if (existingTrigger && existingChildren) {
                List<String> children = existingChildren.split(',')
                        .collect { it.trim() }
                        .findAll { it }
                children.add(downstreamJob.fullName)
                upstreamJob.getPublishersList().replace(
                    new hudson.tasks.BuildTrigger(children.join(','), existingTrigger.threshold)
                )
            } else {
                upstreamJob.getPublishersList().add(
                    new hudson.tasks.BuildTrigger(downstreamJob.fullName, hudson.model.Result.SUCCESS)
                )
            }
            
            upstreamJob.save()
            jenkins.rebuildDependencyGraph()
            
            LOGGER.info("Added dependency from ${upstreamJobName} to ${downstreamJobName}")
            return true
        }, LOGGER, false)
    }
    
    /**
     * Removes a dependency between two jobs.
     * 
     * ```groovy
     * manager.removeJobDependency("build-job", "test-job")
     * ```
     * 
     * @param upstreamJobName Name of upstream job
     * @param downstreamJobName Name of downstream job
     * @return true if successful, false otherwise
     */
    boolean removeJobDependency(String upstreamJobName, String downstreamJobName) {
        upstreamJobName = ValidationUtils.requireNonEmpty(upstreamJobName, "Upstream job name")
        downstreamJobName = ValidationUtils.requireNonEmpty(downstreamJobName, "Downstream job name")
        
        return ErrorHandler.withErrorHandling("removing dependency from ${upstreamJobName} to ${downstreamJobName}", {
            // Get jobs
            AbstractProject upstreamJob = jenkins.getItemByFullName(upstreamJobName, AbstractProject.class)
            AbstractProject downstreamJob = jenkins.getItemByFullName(downstreamJobName, AbstractProject.class)
            
            if (!upstreamJob || !downstreamJob) {
                LOGGER.warning("Jobs not found or not buildable projects")
                return false
            }
            
            // Check for existing dependency
            if (!hasDownstreamDependency(upstreamJob, downstreamJob)) {
                LOGGER.info("Dependency does not exist")
                return true
            }
            
            // Remove downstream project from BuildTrigger publisher
            boolean modified = false
            hudson.tasks.BuildTrigger triggerProperty = upstreamJob.getPublishersList().get(hudson.tasks.BuildTrigger.class)
            
            if (triggerProperty) {
                // Get the current configuration
                String rawChildren = triggerProperty.getChildProjectsValue() ?: ''
                def childProjects = rawChildren.split(',').collect { it.trim() }
                def newChildProjects = childProjects.findAll { it && it != downstreamJob.fullName }.join(',')
                
                // Update with new configuration
                if (newChildProjects) {
                    upstreamJob.getPublishersList().replace(
                        new hudson.tasks.BuildTrigger(newChildProjects, triggerProperty.threshold)
                    )
                } else {
                    upstreamJob.getPublishersList().remove(hudson.tasks.BuildTrigger.class)
                }
                
                modified = true
            }
            
            if (modified) {
                upstreamJob.save()
                jenkins.rebuildDependencyGraph()
                
                LOGGER.info("Removed dependency from ${upstreamJobName} to ${downstreamJobName}")
                return true
            } else {
                LOGGER.warning("No dependency found to remove")
                return false
            }
        }, LOGGER, false)
    }
    
    /**
     * Finds orphaned jobs (no upstream or downstream dependencies).
     * 
     * ```groovy
     * def orphans = manager.findOrphanedJobs()
     * println "Found ${orphans.size()} orphaned jobs"
     * ```
     * 
     * @param excludePattern Optional regex pattern to exclude certain jobs
     * @return List of orphaned job names
     */
    List<String> findOrphanedJobs(String excludePattern = null) {
        CompleteDependencyGraph graph = getCompleteDependencyGraph()
        return graph.findOrphanedJobs(excludePattern)
    }
    
    /**
     * Finds circular dependencies in the job graph.
     * 
     * ```groovy
     * def cycles = manager.findCircularDependencies()
     * cycles.each { cycle ->
     *     println "Circular dependency detected: ${cycle.join(' → ')}"
     * }
     * ```
     * 
     * @return List of circular dependency chains (each chain is a list of job names)
     */
    List<List<String>> findCircularDependencies() {
        CompleteDependencyGraph graph = getCompleteDependencyGraph()
        return graph.findCircularDependencies()
    }
    
    /**
     * Finds upstream jobs for a job.
     * 
     * @param job Job to analyze
     * @param graph Graph to update
     */
    private void findUpstreamJobs(Job job, JobDependencyGraph graph) {
        // For upstream jobs we need to check if this job is triggered by other jobs
        // This requires checking what triggers this job has
        
        // In a real implementation, we would check:
        // 1. Build triggers (e.g., upstream build trigger)
        // 2. Pipeline job upstream dependencies
        
        // Example with BuildTrigger (simplified)
        jenkins.getAllItems(AbstractProject.class).each { upstreamJob ->
            if (hasDownstreamDependency(upstreamJob, job)) {
                graph.upstreamJobs.add(upstreamJob.fullName)
            }
        }
    }
    
    /**
     * Finds downstream jobs for a job.
     * 
     * @param job Job to analyze
     * @param graph Graph to update
     */
    private void findDownstreamJobs(Job job, JobDependencyGraph graph) {
        // Check if job is AbstractProject to access downstream projects
        if (job instanceof AbstractProject) {
            AbstractProject project = (AbstractProject) job

            // Get downstream projects from build triggers
            hudson.tasks.BuildTrigger trigger = project.getPublishersList().get(hudson.tasks.BuildTrigger.class)
            String childProjectsValue = trigger?.getChildProjectsValue()
            if (childProjectsValue) {
                childProjectsValue.split(',').each { String childName ->
                    String trimmed = childName.trim()
                    if (trimmed) {
                        graph.downstreamJobs.add(trimmed)
                    }
                }
            }
        }
    }
    
    /**
     * Checks if upstream job has downstream job as a dependency.
     * 
     * @param upstreamJob Upstream job
     * @param downstreamJob Downstream job
     * @return true if dependency exists
     */
    private boolean hasDownstreamDependency(AbstractProject upstreamJob, Job downstreamJob) {
        hudson.tasks.BuildTrigger trigger = upstreamJob.getPublishersList().get(hudson.tasks.BuildTrigger.class)
        String childProjectsValue = trigger?.getChildProjectsValue()

        if (childProjectsValue) {
            return childProjectsValue.split(',').any { childName ->
                childName.trim() == downstreamJob.fullName
            }
        }

        return false
    }
}

/**
 * Represents dependency graph for a single job.
 */
class JobDependencyGraph {
    String jobName
    Set<String> upstreamJobs = []
    Set<String> downstreamJobs = []
    
    JobDependencyGraph(String jobName) {
        this.jobName = jobName
    }
    
    boolean isOrphaned() {
        return upstreamJobs.isEmpty() && downstreamJobs.isEmpty()
    }
}

/**
 * Represents a complete dependency graph for all jobs.
 */
class CompleteDependencyGraph {
    Map<String, JobDependencyGraph> jobGraphs = [:]
    
    void addJobGraph(JobDependencyGraph graph) {
        jobGraphs[graph.jobName] = graph
    }
    
    /**
     * Finds orphaned jobs (no upstream or downstream dependencies).
     * 
     * @param excludePattern Optional regex pattern to exclude certain jobs
     * @return List of orphaned job names
     */
    List<String> findOrphanedJobs(String excludePattern = null) {
        Pattern pattern = excludePattern ? Pattern.compile(excludePattern) : null
        
        return jobGraphs.keySet().findAll { jobName ->
            JobDependencyGraph graph = jobGraphs[jobName]
            boolean orphaned = graph.isOrphaned()
            
            // Exclude jobs matching pattern
            if (pattern && pattern.matcher(jobName).matches()) {
                return false
            }
            
            return orphaned
        }.sort()
    }
    
    /**
     * Finds circular dependencies in the job graph.
     * 
     * @return List of circular dependency chains
     */
    List<List<String>> findCircularDependencies() {
        List<List<String>> cycles = []

        // For each job, explore reachable cycles
        jobGraphs.keySet().each { String startJob ->
            findCycles(startJob, [], [:], new HashSet<String>(), cycles)
        }

        // Remove duplicates (same cycle can be detected from different starting points)
        return removeDuplicateCycles(cycles)
    }

    /**
     * Recursively finds cycles starting from a job.
     *
     * @param currentJob Current job being checked
     * @param path Current DFS stack
     * @param pathIndex Map from job name to its index in path for O(1) cycle detection
     * @param visited Set of already visited jobs for O(1) lookup
     * @param cycles List to store found cycles
     */
    private void findCycles(String currentJob, List<String> path, Map<String, Integer> pathIndex,
                            Set<String> visited, List<List<String>> cycles) {
        if (!jobGraphs.containsKey(currentJob)) {
            return
        }

        Integer index = pathIndex.get(currentJob)
        if (index != null) {
            // Copy slice without mutating path or appending a closing duplicate
            cycles.add(new ArrayList<>(path.subList(index, path.size())))
            return
        }

        if (!visited.add(currentJob)) {
            return
        }

        pathIndex.put(currentJob, path.size())
        path.add(currentJob)

        JobDependencyGraph graph = jobGraphs[currentJob]
        graph.downstreamJobs.each { String downstream ->
            findCycles(downstream, path, pathIndex, visited, cycles)
        }

        path.remove(path.size() - 1)
        pathIndex.remove(currentJob)
    }

    /**
     * Removes duplicate cycles from the list by rotating each cycle to start at its lexicographically smallest job.
     *
     * @param cycles List of cycles to deduplicate
     * @return Deduplicated list of cycles
     */
    private List<List<String>> removeDuplicateCycles(List<List<String>> cycles) {
        Map<String, List<String>> canonicalCycles = new LinkedHashMap<>()

        cycles.each { List<String> cycle ->
            if (!cycle) {
                return
            }
            int size = cycle.size()
            int minIndex = 0
            for (int i = 1; i < size; i++) {
                if (cycle[i] < cycle[minIndex]) {
                    minIndex = i
                }
            }

            List<String> normalizedCycle = new ArrayList<>(size)
            for (int i = 0; i < size; i++) {
                normalizedCycle.add(cycle[(minIndex + i) % size])
            }

            String key = normalizedCycle.join("\u0000")
            canonicalCycles[key] = normalizedCycle
        }

        return new ArrayList<>(canonicalCycles.values())
    }
    
    /**
     * Generates a DOT format representation of the graph.
     * 
     * @return String in DOT format
     */
    String toDotFormat() {
        StringBuilder dot = new StringBuilder()
        dot.append("digraph JobDependencies {\n")
        
        // Add job nodes
        jobGraphs.keySet().each { jobName ->
            String escapedName = jobName.replaceAll('"', '\\\\"')
            dot.append("  \"${escapedName}\";\n")
        }
        
        // Add dependencies
        jobGraphs.each { jobName, graph ->
            String escapedName = jobName.replaceAll('"', '\\\\"')
            
            graph.downstreamJobs.each { downstream ->
                String escapedDownstream = downstream.replaceAll('"', '\\\\"')
                dot.append("  \"${escapedName}\" -> \"${escapedDownstream}\";\n")
            }
        }
        
        dot.append("}\n")
        return dot.toString()
    }
}