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
import hudson.model.Run
import hudson.model.Result
import java.util.Date
import java.text.SimpleDateFormat

import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Analyzes Jenkins job performance and health metrics.
 * 
 * Provides detailed metrics on job stability, duration trends, and failure analysis.
 * 
 * @author Thomas Vincent
 * @since 1.2
 */
class JobHealthAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(JobHealthAnalyzer.class.getName())
    private static final int DEFAULT_BUILD_COUNT = 20
    private static final int DEFAULT_DAYS_TO_ANALYZE = 30
    
    private final Jenkins jenkins
    private final int buildCount
    private final int daysToAnalyze
    
    /**
     * Creates a JobHealthAnalyzer with specified parameters.
     * 
     * ```groovy
     * def analyzer = new JobHealthAnalyzer(
     *     jenkins: Jenkins.get(),
     *     buildCount: 10,     // Only analyze last 10 builds
     *     daysToAnalyze: 14   // Only analyze builds from last 14 days
     * )
     * ```
     * 
     * @param jenkins Jenkins instance
     * @param buildCount Maximum number of builds to analyze per job
     * @param daysToAnalyze Maximum age of builds to analyze in days
     */
    JobHealthAnalyzer(Jenkins jenkins, int buildCount = DEFAULT_BUILD_COUNT, int daysToAnalyze = DEFAULT_DAYS_TO_ANALYZE) {
        this.jenkins = ValidationUtils.requireNonNull(jenkins, "Jenkins instance")
        this.buildCount = ValidationUtils.requirePositive(buildCount, "buildCount", DEFAULT_BUILD_COUNT)
        this.daysToAnalyze = ValidationUtils.requirePositive(daysToAnalyze, "daysToAnalyze", DEFAULT_DAYS_TO_ANALYZE)
    }
    
    /**
     * Analyzes health metrics for a specific job.
     * 
     * ```groovy
     * def report = analyzer.analyzeJob("deployment-pipeline")
     * println "Success rate: ${report.successRate}%"
     * println "Average duration: ${report.averageDuration} ms"
     * ```
     * 
     * @param jobName Name of job to analyze
     * @return JobHealthReport or null if job not found
     */
    JobHealthReport analyzeJob(String jobName) {
        jobName = ValidationUtils.requireNonEmpty(jobName, "Job name")
        
        Job job = jenkins.getItemByFullName(jobName, Job.class)
        if (job == null) {
            LOGGER.warning("Job not found: ${jobName}")
            return null
        }
        
        return ErrorHandler.withErrorHandling("analyzing job ${jobName}", {
            // Create report instance
            JobHealthReport report = new JobHealthReport(job.fullName)
            
            // Calculate date cutoff
            Calendar calendar = Calendar.instance
            calendar.add(Calendar.DAY_OF_YEAR, -daysToAnalyze)
            Date cutoffDate = calendar.time
            
            // Get builds to analyze
            List<Run> builds = getBuildsForAnalysis(job, cutoffDate)
            if (builds.isEmpty()) {
                LOGGER.info("No builds found for job ${jobName} within time period")
                return report
            }
            
            // Calculate metrics
            calculateBuildResults(builds, report)
            calculateDurationMetrics(builds, report)
            analyzeFailurePatterns(builds, report)
            
            return report
        }, LOGGER, null)
    }
    
    /**
     * Analyzes health metrics for all jobs.
     * 
     * ```groovy
     * def reports = analyzer.analyzeAllJobs()
     * reports.each { report ->
     *     println "${report.jobName}: ${report.successRate}% success"
     * }
     * ```
     * 
     * @param pattern Optional regex pattern to filter jobs by name
     * @return List of JobHealthReport objects
     */
    List<JobHealthReport> analyzeAllJobs(String pattern = null) {
        Pattern jobPattern = pattern ? Pattern.compile(pattern) : null
        
        List<JobHealthReport> reports = []
        List<Job> allJobs = jenkins.getAllItems(Job.class)
        
        allJobs.each { job ->
            if (!jobPattern || jobPattern.matcher(job.fullName).matches()) {
                JobHealthReport report = analyzeJob(job.fullName)
                if (report) {
                    reports.add(report)
                }
            }
        }
        
        LOGGER.info("Analyzed ${reports.size()} jobs${pattern ? " matching pattern '${pattern}'" : ""}")
        return reports
    }
    
    /**
     * Generates a report of unstable jobs.
     * 
     * ```groovy
     * def unstableJobs = analyzer.findUnstableJobs(75.0) // Jobs with < 75% success
     * unstableJobs.each { report ->
     *     println "${report.jobName}: ${report.successRate}%"
     * }
     * ```
     * 
     * @param successThreshold Minimum success rate to be considered stable
     * @return List of JobHealthReport objects for unstable jobs
     */
    List<JobHealthReport> findUnstableJobs(double successThreshold = 80.0) {
        List<JobHealthReport> allReports = analyzeAllJobs()
        List<JobHealthReport> unstableJobs = allReports.findAll { report ->
            report.successRate < successThreshold && report.buildCount > 0
        }
        
        // Sort by success rate ascending (worst first)
        unstableJobs.sort { a, b -> a.successRate <=> b.successRate }
        
        LOGGER.info("Found ${unstableJobs.size()} unstable jobs (below ${successThreshold}% success rate)")
        return unstableJobs
    }
    
    /**
     * Generates a report of jobs with increasing duration trends.
     * 
     * ```groovy
     * def slowingJobs = analyzer.findJobsWithIncreasingDuration()
     * slowingJobs.each { report ->
     *     println "${report.jobName}: ${report.durationTrend}% increase"
     * }
     * ```
     * 
     * @param thresholdPercent Minimum percentage increase to include
     * @return List of JobHealthReport objects for jobs with increasing duration
     */
    List<JobHealthReport> findJobsWithIncreasingDuration(double thresholdPercent = 10.0) {
        List<JobHealthReport> allReports = analyzeAllJobs()
        List<JobHealthReport> slowingJobs = allReports.findAll { report ->
            report.durationTrend > thresholdPercent && report.buildCount >= 5
        }
        
        // Sort by duration trend descending (worst first)
        slowingJobs.sort { a, b -> b.durationTrend <=> a.durationTrend }
        
        LOGGER.info("Found ${slowingJobs.size()} jobs with increasing duration (above ${thresholdPercent}% increase)")
        return slowingJobs
    }
    
    /**
     * Gets builds for analysis within the time period and build count.
     * 
     * @param job Job to analyze
     * @param cutoffDate Oldest date to include
     * @return List of builds to analyze
     */
    private List<Run> getBuildsForAnalysis(Job job, Date cutoffDate) {
        List<Run> builds = []
        int count = 0
        
        job.getBuilds().each { build ->
            if (count < buildCount && build.getTime().after(cutoffDate)) {
                builds.add(build)
                count++
            }
        }
        
        return builds
    }
    
    /**
     * Calculates build result metrics (success, failure, unstable, etc.).
     * 
     * @param builds Builds to analyze
     * @param report Report to update
     */
    private void calculateBuildResults(List<Run> builds, JobHealthReport report) {
        report.buildCount = builds.size()
        
        int successCount = 0
        int failureCount = 0
        int unstableCount = 0
        int abortedCount = 0
        
        builds.each { build ->
            Result result = build.getResult()
            
            if (result == Result.SUCCESS) {
                successCount++
            } else if (result == Result.FAILURE) {
                failureCount++
            } else if (result == Result.UNSTABLE) {
                unstableCount++
            } else if (result == Result.ABORTED) {
                abortedCount++
            }
        }
        
        report.successCount = successCount
        report.failureCount = failureCount
        report.unstableCount = unstableCount
        report.abortedCount = abortedCount
        
        // Calculate success rate
        report.successRate = (report.buildCount > 0) ? 
            (successCount * 100.0 / report.buildCount) : 0.0
    }
    
    /**
     * Calculates duration metrics (average, min, max, trend).
     * 
     * @param builds Builds to analyze
     * @param report Report to update
     */
    private void calculateDurationMetrics(List<Run> builds, JobHealthReport report) {
        if (builds.isEmpty()) {
            return
        }
        
        long totalDuration = 0
        long minDuration = Long.MAX_VALUE
        long maxDuration = 0
        
        builds.each { build ->
            long duration = build.getDuration()
            
            totalDuration += duration
            minDuration = Math.min(minDuration, duration)
            maxDuration = Math.max(maxDuration, duration)
        }
        
        report.averageDuration = totalDuration / builds.size()
        report.minDuration = minDuration
        report.maxDuration = maxDuration
        
        // Calculate duration trend
        if (builds.size() >= 5) {
            List<Run> firstHalf = builds.subList(builds.size() / 2, builds.size())
            List<Run> secondHalf = builds.subList(0, builds.size() / 2)
            
            long firstHalfTotal = 0
            firstHalf.each { build -> firstHalfTotal += build.getDuration() }
            
            long secondHalfTotal = 0
            secondHalf.each { build -> secondHalfTotal += build.getDuration() }
            
            double firstHalfAvg = firstHalfTotal / firstHalf.size()
            double secondHalfAvg = secondHalfTotal / secondHalf.size()
            
            if (firstHalfAvg > 0) {
                // Calculate percentage change (positive = increasing duration)
                report.durationTrend = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100.0
            }
        }
    }
    
    /**
     * Analyzes failure patterns in builds.
     * 
     * @param builds Builds to analyze
     * @param report Report to update
     */
    private void analyzeFailurePatterns(List<Run> builds, JobHealthReport report) {
        // Filter for failed builds only
        List<Run> failedBuilds = builds.findAll { build -> 
            build.getResult() == Result.FAILURE 
        }
        
        // Extract failure patterns (simplified)
        Map<String, Integer> failurePatterns = [:]
        
        failedBuilds.each { build ->
            // In a real implementation, this would parse the build logs
            // and identify common error patterns
            String failureReason = "Unknown failure"
            failurePatterns[failureReason] = (failurePatterns[failureReason] ?: 0) + 1
        }
        
        report.failurePatterns = failurePatterns
    }
}

/**
 * Contains health metrics for a Jenkins job.
 */
class JobHealthReport {
    String jobName
    int buildCount = 0
    
    // Build result metrics
    int successCount = 0
    int failureCount = 0
    int unstableCount = 0
    int abortedCount = 0
    double successRate = 0.0
    
    // Duration metrics
    long averageDuration = 0
    long minDuration = 0
    long maxDuration = 0
    double durationTrend = 0.0  // Percentage change (positive = getting slower)
    
    // Failure analysis
    Map<String, Integer> failurePatterns = [:]
    
    JobHealthReport(String jobName) {
        this.jobName = jobName
    }
    
    /**
     * Generates a formatted report string.
     * 
     * @return String representation of the report
     */
    String generateReport() {
        StringBuilder report = new StringBuilder()
        
        report.append("Health Report for ${jobName}\n")
        report.append("==============================\n")
        report.append("Build Statistics:\n")
        report.append("- Total builds analyzed: ${buildCount}\n")
        report.append("- Success rate: ${String.format("%.1f", successRate)}%\n")
        report.append("- Successful builds: ${successCount}\n")
        report.append("- Failed builds: ${failureCount}\n")
        report.append("- Unstable builds: ${unstableCount}\n")
        report.append("- Aborted builds: ${abortedCount}\n")
        
        report.append("\nDuration Statistics:\n")
        report.append("- Average duration: ${formatDuration(averageDuration)}\n")
        report.append("- Minimum duration: ${formatDuration(minDuration)}\n")
        report.append("- Maximum duration: ${formatDuration(maxDuration)}\n")
        
        String trendDirection = durationTrend > 0 ? "increasing" : "decreasing"
        report.append("- Duration trend: ${String.format("%.1f", Math.abs(durationTrend))}% ${trendDirection}\n")
        
        if (failureCount > 0) {
            report.append("\nFailure Patterns:\n")
            failurePatterns.each { pattern, count ->
                double percentage = count * 100.0 / failureCount
                report.append("- ${pattern}: ${count} occurrences (${String.format("%.1f", percentage)}%)\n")
            }
        }
        
        report.append("\nRecommendations:\n")
        if (successRate < 80.0) {
            report.append("- Job has low success rate. Consider investigating frequent failures.\n")
        }
        
        if (durationTrend > 10.0) {
            report.append("- Job duration is increasing. Consider optimizing performance.\n")
        }
        
        if (abortedCount > buildCount * 0.2) {
            report.append("- High rate of aborted builds. Consider checking timeout settings.\n")
        }
        
        return report.toString()
    }
    
    /**
     * Formats duration in a human-readable format.
     * 
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000
        long minutes = seconds / 60
        long hours = minutes / 60
        
        seconds %= 60
        minutes %= 60
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String.format("%d:%02d", minutes, seconds)
        }
    }
}