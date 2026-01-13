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
import hudson.model.AbstractProject
import hudson.model.Job
import hudson.model.Run
import hudson.model.TopLevelItem
import hudson.model.TaskListener
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob
import org.kohsuke.stapler.DataBoundConstructor

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import com.github.thomasvincent.jenkinsscripts.util.JenkinsEnvironment

import java.io.PrintStream
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Cleans Jenkins jobs by removing old builds and resetting build numbers.
 *
 * <p>Handles build cleanup for Jenkins jobs with validation and safe error handling.
 * Supports both classic (AbstractProject) and modern (WorkflowJob/Pipeline) job types.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Memory-efficient lazy iteration for large build histories</li>
 *   <li>Support for Pipeline (WorkflowJob) and classic (AbstractProject) jobs</li>
 *   <li>Dry run mode to preview changes without deletion</li>
 *   <li>Batch processing to prevent memory issues</li>
 *   <li>Verbose logging for troubleshooting</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * // Basic cleaning without build reset
 * def basicCleaner = new JobCleaner(Jenkins.instance, 'deploy-job')
 * basicCleaner.clean()
 *
 * // Clean with custom limits (first 50 builds, reset to build #1)
 * def advancedCleaner = new JobCleaner(
 *     Jenkins.instance,
 *     'test-job',
 *     true,       // Reset build number
 *     100,        // Batch size for memory efficiency
 *     50          // Delete up to 50 builds
 * )
 * advancedCleaner.setDryRun(true)  // Preview mode
 * advancedCleaner.clean()
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class JobCleaner {
    private static final Logger LOGGER = Logger.getLogger(JobCleaner.class.getName())
    private static final int DEFAULT_BATCH_SIZE = 25
    private static final int DEFAULT_BUILD_TOTAL = 100
    private static final int MAX_BATCH_SIZE = 1000

    private final Jenkins jenkins
    private final String jobName
    private final boolean resetBuildNumber
    private final int batchSize
    private final int buildTotal

    // Optional features
    private boolean dryRun = false
    private boolean verbose = false
    private TaskListener taskListener = null
    private PrintStream outputStream = null

    /**
     * Creates a JobCleaner with specified parameters.
     *
     * <pre>
     * def cleaner = new JobCleaner(Jenkins.instance, 'my-job', true)
     * cleaner.clean()  // Cleans job and resets build number to 1
     * </pre>
     *
     * @param jenkins Jenkins instance
     * @param jobName Job to clean (supports full path for folder-based jobs)
     * @param resetBuildNumber Reset build number to 1 if true
     * @param batchSize Builds to process per batch for memory efficiency (default: 25, max: 1000)
     * @param buildTotal Max builds to delete (default: 100)
     */
    @DataBoundConstructor
    JobCleaner(Jenkins jenkins, String jobName, boolean resetBuildNumber = false,
               int batchSize = DEFAULT_BATCH_SIZE,
               int buildTotal = DEFAULT_BUILD_TOTAL) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        this.jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        this.resetBuildNumber = resetBuildNumber
        this.batchSize = ValidationUtils.requireInRange(
            batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE,
            1, MAX_BATCH_SIZE, "batchSize"
        )
        this.buildTotal = ValidationUtils.requirePositive(buildTotal, "buildTotal", DEFAULT_BUILD_TOTAL)
    }

    /**
     * Enables or disables dry run mode.
     *
     * <p>In dry run mode, the cleaner will report what would be deleted
     * without actually deleting any builds.</p>
     *
     * @param dryRun true to enable dry run mode
     * @return this instance for method chaining
     */
    JobCleaner setDryRun(boolean dryRun) {
        this.dryRun = dryRun
        return this
    }

    /**
     * Enables or disables verbose output.
     *
     * @param verbose true to enable verbose output
     * @return this instance for method chaining
     */
    JobCleaner setVerbose(boolean verbose) {
        this.verbose = verbose
        return this
    }

    /**
     * Sets a TaskListener for Pipeline context output.
     *
     * <p>When set, output will be directed to the build console log
     * instead of the system logger.</p>
     *
     * @param listener The TaskListener from the build context
     * @return this instance for method chaining
     */
    JobCleaner setTaskListener(TaskListener listener) {
        this.taskListener = listener
        if (listener != null) {
            this.outputStream = listener.getLogger()
        }
        return this
    }

    /**
     * Cleans the specified job.
     *
     * <p>Finds the job and initiates cleaning based on job type.
     * Supports both classic AbstractProject jobs and modern Pipeline (WorkflowJob) jobs.</p>
     *
     * @return true if cleaning succeeded, false otherwise
     */
    boolean clean() {
        // Use idiomatic permission check
        try {
            JenkinsEnvironment.checkAdminPermission()
        } catch (Exception e) {
            log(Level.WARNING, "Insufficient permissions to clean jobs: ${e.message}")
            return false
        }

        TopLevelItem item = jenkins.getItemByFullName(jobName, TopLevelItem.class)
        if (item == null) {
            log(Level.WARNING, "Item not found: ${jobName}")
            return false
        }

        // Support both classic and modern job types
        if (item instanceof WorkflowJob) {
            return cleanWorkflowJob((WorkflowJob) item)
        } else if (item instanceof AbstractProject) {
            return cleanAbstractProject((AbstractProject) item)
        } else if (item instanceof Job) {
            // Generic Job support for other job types
            return cleanGenericJob((Job) item)
        } else {
            log(Level.WARNING, "Unsupported job type for '${jobName}': ${item.class.simpleName}")
            return false
        }
    }

    /**
     * Cleans a Pipeline (WorkflowJob) by removing builds.
     *
     * @param job Pipeline job to clean
     * @return true if successful, false otherwise
     */
    private boolean cleanWorkflowJob(WorkflowJob job) {
        return ErrorHandler.withErrorHandling("cleaning Pipeline job ${job.fullName}", {
            log(Level.INFO, "Cleaning Pipeline job: ${job.fullName}")
            deleteBuildsBatched(job)

            if (resetBuildNumber) {
                return resetJobBuildNumber(job)
            }

            return true
        }, LOGGER, false)
    }

    /**
     * Cleans a classic (AbstractProject) job by removing builds.
     *
     * @param project Classic project to clean
     * @return true if successful, false otherwise
     */
    private boolean cleanAbstractProject(AbstractProject<?, ?> project) {
        return ErrorHandler.withErrorHandling("cleaning classic project ${project.fullName}", {
            log(Level.INFO, "Cleaning classic project: ${project.fullName}")
            deleteBuildsBatched(project)

            if (resetBuildNumber) {
                return resetProjectBuildNumber(project)
            }

            return true
        }, LOGGER, false)
    }

    /**
     * Cleans a generic Job type.
     *
     * @param job Job to clean
     * @return true if successful, false otherwise
     */
    private boolean cleanGenericJob(Job<?, ?> job) {
        return ErrorHandler.withErrorHandling("cleaning job ${job.fullName}", {
            log(Level.INFO, "Cleaning job: ${job.fullName}")
            deleteBuildsBatched(job)
            return true
        }, LOGGER, false)
    }

    /**
     * Deletes builds using memory-efficient batched iteration.
     *
     * <p>Uses lazy iteration over the build list to avoid loading
     * the entire build history into memory at once. This is critical
     * for jobs with thousands of builds.</p>
     *
     * @param job Job to delete builds from
     */
    private void deleteBuildsBatched(Job<?, ?> job) {
        int deletedCount = 0
        int batchCount = 0
        List<Run> batchToDelete = []

        // Use getBuilds() which returns a lazy RunList
        def builds = job.getBuilds()

        // Iterate using an iterator for memory efficiency
        def iterator = builds.iterator()

        while (iterator.hasNext() && deletedCount < buildTotal) {
            Run build = iterator.next()
            batchToDelete.add(build)

            // Process batch when full or at the end
            if (batchToDelete.size() >= batchSize) {
                deletedCount += processBatch(batchToDelete, job.fullName, ++batchCount)
                batchToDelete.clear()

                // Allow GC to reclaim memory between batches
                if (verbose) {
                    log(Level.FINE, "Processed batch ${batchCount}, ${deletedCount} builds deleted so far")
                }
            }

            if (deletedCount >= buildTotal) {
                break
            }
        }

        // Process remaining builds
        if (!batchToDelete.isEmpty() && deletedCount < buildTotal) {
            deletedCount += processBatch(batchToDelete, job.fullName, ++batchCount)
        }

        log(Level.INFO, "${dryRun ? '[DRY RUN] Would delete' : 'Deleted'} ${deletedCount} builds from job ${job.fullName}")
    }

    /**
     * Processes a batch of builds for deletion.
     *
     * @param batch List of builds to delete
     * @param jobName Name of the job (for logging)
     * @param batchNumber Batch number (for logging)
     * @return Number of builds successfully deleted
     */
    private int processBatch(List<Run> batch, String jobName, int batchNumber) {
        int deleted = 0

        for (Run build : batch) {
            if (dryRun) {
                if (verbose) {
                    log(Level.INFO, "[DRY RUN] Would delete build #${build.number}")
                }
                deleted++
            } else {
                boolean success = ErrorHandler.withErrorHandling(
                    "deleting build #${build.number} for job ${jobName}",
                    {
                        build.delete()
                        return true
                    },
                    LOGGER,
                    false
                )

                if (success) {
                    deleted++
                    if (verbose) {
                        log(Level.FINE, "Deleted build #${build.number}")
                    }
                }
            }
        }

        return deleted
    }

    /**
     * Resets Pipeline job build number to 1.
     *
     * @param job Pipeline job to reset
     * @return true if successful, false otherwise
     */
    private boolean resetJobBuildNumber(WorkflowJob job) {
        if (dryRun) {
            log(Level.INFO, "[DRY RUN] Would reset build number for job ${job.fullName}")
            return true
        }

        return ErrorHandler.withErrorHandling("resetting build number for Pipeline job ${job.fullName}", {
            job.updateNextBuildNumber(1)
            job.save()
            log(Level.INFO, "Reset build number for Pipeline job ${job.fullName}")
            return true
        }, LOGGER, false)
    }

    /**
     * Resets classic project build number to 1.
     *
     * @param project Classic project to reset
     * @return true if successful, false otherwise
     */
    private boolean resetProjectBuildNumber(AbstractProject<?, ?> project) {
        if (dryRun) {
            log(Level.INFO, "[DRY RUN] Would reset build number for project ${project.fullName}")
            return true
        }

        return ErrorHandler.withErrorHandling("resetting build number for project ${project.fullName}", {
            project.updateNextBuildNumber(1)
            project.save()
            log(Level.INFO, "Reset build number for project ${project.fullName}")
            return true
        }, LOGGER, false)
    }

    /**
     * Logs a message to both the system logger and optionally to the build console.
     *
     * @param level Log level
     * @param message Message to log
     */
    private void log(Level level, String message) {
        LOGGER.log(level, message)

        // Also log to build console if TaskListener is set
        if (outputStream != null) {
            String prefix = level.intValue() >= Level.WARNING.intValue() ? "[WARN] " : ""
            outputStream.println("${prefix}${message}")
        }
    }

    /**
     * Gets statistics about the job's current build history.
     *
     * <p>Useful for understanding the scope of a cleaning operation
     * before executing it.</p>
     *
     * @return Map containing build statistics, or empty map if job not found
     */
    Map<String, Object> getJobStats() {
        TopLevelItem item = jenkins.getItemByFullName(jobName, TopLevelItem.class)
        if (item == null || !(item instanceof Job)) {
            return [:]
        }

        Job job = (Job) item
        def builds = job.getBuilds()

        // Count builds without loading all into memory
        int totalBuilds = 0
        Run firstBuild = null
        Run lastBuild = job.getLastBuild()

        def iterator = builds.iterator()
        while (iterator.hasNext()) {
            Run build = iterator.next()
            if (firstBuild == null) {
                firstBuild = build
            }
            totalBuilds++
        }

        return [
            jobName: job.fullName,
            jobType: item.class.simpleName,
            totalBuilds: totalBuilds,
            firstBuildNumber: firstBuild?.number,
            lastBuildNumber: lastBuild?.number,
            nextBuildNumber: job.getNextBuildNumber(),
            buildsToClean: Math.min(totalBuilds, buildTotal)
        ]
    }
}
