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

import hudson.model.Job
import hudson.model.Run
import hudson.model.Result

import java.util.logging.Logger
import java.util.function.Predicate

/**
 * Memory-efficient iterator for Jenkins build history.
 *
 * <p>Provides lazy iteration over job builds without loading the entire
 * build history into memory. This is critical for "Mega-Jenkins" instances
 * with jobs that have thousands of builds.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Lazy loading - only loads builds as needed</li>
 *   <li>Filtering support - filter by result, date range, etc.</li>
 *   <li>Batch processing - process builds in configurable batches</li>
 *   <li>Early termination - stop iteration based on conditions</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * def job = Jenkins.instance.getItemByFullName("my-job")
 *
 * // Iterate over builds lazily
 * BuildIterator.forJob(job)
 *     .limit(100)
 *     .filter { it.result == Result.FAILURE }
 *     .forEach { build ->
 *         println "Failed build: ${build.number}"
 *     }
 *
 * // Process in batches
 * BuildIterator.forJob(job)
 *     .limit(1000)
 *     .inBatches(50) { batch ->
 *         batch.each { build -> build.delete() }
 *     }
 *
 * // Collect with limit
 * def recentBuilds = BuildIterator.forJob(job)
 *     .limit(10)
 *     .filter { it.result == Result.SUCCESS }
 *     .collect()
 * </pre>
 *
 * @author Thomas Vincent
 * @since 1.2.0
 */
class BuildIterator implements Iterable<Run> {
    private static final Logger LOGGER = Logger.getLogger(BuildIterator.class.getName())

    private final Job<?, ?> job
    private int limit = Integer.MAX_VALUE
    private int skip = 0
    private List<Predicate<Run>> filters = []
    private Predicate<Run> stopCondition = null

    /**
     * Creates a BuildIterator for the specified job.
     *
     * @param job The job to iterate builds for
     */
    private BuildIterator(Job<?, ?> job) {
        this.job = ValidationUtils.requireNonNull(job, "Job")
    }

    /**
     * Creates a new BuildIterator for a job.
     *
     * @param job The job to iterate builds for
     * @return A new BuildIterator instance
     */
    static BuildIterator forJob(Job<?, ?> job) {
        return new BuildIterator(job)
    }

    /**
     * Limits the maximum number of builds to iterate.
     *
     * @param maxBuilds Maximum number of builds
     * @return this instance for method chaining
     */
    BuildIterator limit(int maxBuilds) {
        this.limit = maxBuilds > 0 ? maxBuilds : Integer.MAX_VALUE
        return this
    }

    /**
     * Skips the first N builds.
     *
     * @param count Number of builds to skip
     * @return this instance for method chaining
     */
    BuildIterator skip(int count) {
        this.skip = count > 0 ? count : 0
        return this
    }

    /**
     * Adds a filter predicate.
     *
     * <p>Only builds matching all filters will be included.</p>
     *
     * @param predicate The filter predicate
     * @return this instance for method chaining
     */
    BuildIterator filter(Predicate<Run> predicate) {
        if (predicate != null) {
            filters.add(predicate)
        }
        return this
    }

    /**
     * Adds a filter using a Groovy closure.
     *
     * @param closure The filter closure
     * @return this instance for method chaining
     */
    BuildIterator filter(Closure<Boolean> closure) {
        if (closure != null) {
            filters.add({ Run run -> closure.call(run) } as Predicate<Run>)
        }
        return this
    }

    /**
     * Filters to only include builds with a specific result.
     *
     * @param result The build result to filter by
     * @return this instance for method chaining
     */
    BuildIterator withResult(Result result) {
        return filter { Run run -> run.result == result }
    }

    /**
     * Filters to only include successful builds.
     *
     * @return this instance for method chaining
     */
    BuildIterator successful() {
        return withResult(Result.SUCCESS)
    }

    /**
     * Filters to only include failed builds.
     *
     * @return this instance for method chaining
     */
    BuildIterator failed() {
        return filter { Run run ->
            run.result == Result.FAILURE || run.result == Result.UNSTABLE
        }
    }

    /**
     * Filters to builds within a date range.
     *
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @return this instance for method chaining
     */
    BuildIterator inDateRange(Date startDate, Date endDate) {
        return filter { Run run ->
            def buildTime = new Date(run.getTimeInMillis())
            return (startDate == null || buildTime >= startDate) &&
                   (endDate == null || buildTime <= endDate)
        }
    }

    /**
     * Filters to builds older than a specified age.
     *
     * @param days Minimum age in days
     * @return this instance for method chaining
     */
    BuildIterator olderThanDays(int days) {
        def cutoff = new Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000))
        return filter { Run run ->
            new Date(run.getTimeInMillis()) < cutoff
        }
    }

    /**
     * Filters to builds newer than a specified age.
     *
     * @param days Maximum age in days
     * @return this instance for method chaining
     */
    BuildIterator newerThanDays(int days) {
        def cutoff = new Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000))
        return filter { Run run ->
            new Date(run.getTimeInMillis()) >= cutoff
        }
    }

    /**
     * Sets a stop condition - iteration will stop when this condition is met.
     *
     * @param condition The stop condition predicate
     * @return this instance for method chaining
     */
    BuildIterator stopWhen(Predicate<Run> condition) {
        this.stopCondition = condition
        return this
    }

    /**
     * Sets a stop condition using a Groovy closure.
     *
     * @param closure The stop condition closure
     * @return this instance for method chaining
     */
    BuildIterator stopWhen(Closure<Boolean> closure) {
        if (closure != null) {
            this.stopCondition = { Run run -> closure.call(run) } as Predicate<Run>
        }
        return this
    }

    /**
     * Returns an iterator over the builds.
     *
     * @return Iterator of builds
     */
    @Override
    Iterator<Run> iterator() {
        return new LazyBuildIterator()
    }

    /**
     * Iterates over builds, applying the given action to each.
     *
     * @param action The action to apply to each build
     */
    void forEach(Closure action) {
        iterator().each { action.call(it) }
    }

    /**
     * Collects all matching builds into a list.
     *
     * <p>Note: This loads all matching builds into memory.
     * Use with appropriate limits for large build histories.</p>
     *
     * @return List of matching builds
     */
    List<Run> collect() {
        def result = []
        iterator().each { result.add(it) }
        return result
    }

    /**
     * Counts matching builds without loading them all into memory.
     *
     * @return Number of matching builds
     */
    int count() {
        int count = 0
        iterator().each { count++ }
        return count
    }

    /**
     * Finds the first matching build.
     *
     * @return The first matching build, or null if none found
     */
    Run first() {
        def iter = iterator()
        return iter.hasNext() ? iter.next() : null
    }

    /**
     * Checks if any builds match the criteria.
     *
     * @return true if at least one build matches
     */
    boolean any() {
        return iterator().hasNext()
    }

    /**
     * Processes builds in batches.
     *
     * <p>Memory-efficient way to process large numbers of builds.</p>
     *
     * @param batchSize Number of builds per batch
     * @param batchProcessor Closure to process each batch
     */
    void inBatches(int batchSize, Closure batchProcessor) {
        def batch = []
        def iter = iterator()

        while (iter.hasNext()) {
            batch.add(iter.next())

            if (batch.size() >= batchSize) {
                batchProcessor.call(batch)
                batch.clear()
            }
        }

        // Process remaining builds
        if (!batch.isEmpty()) {
            batchProcessor.call(batch)
        }
    }

    /**
     * Maps builds to a different type.
     *
     * @param mapper The mapping function
     * @return List of mapped values
     */
    def <T> List<T> map(Closure<T> mapper) {
        def result = []
        iterator().each { result.add(mapper.call(it)) }
        return result
    }

    /**
     * Gets build statistics.
     *
     * @return Map of build statistics
     */
    Map<String, Object> getStats() {
        def stats = [
            total: 0,
            success: 0,
            failure: 0,
            unstable: 0,
            aborted: 0,
            other: 0,
            oldestBuildNumber: null,
            newestBuildNumber: null,
            oldestBuildTime: null,
            newestBuildTime: null,
            totalDurationMs: 0L
        ]

        iterator().each { Run run ->
            stats.total++
            stats.totalDurationMs += run.duration

            switch (run.result) {
                case Result.SUCCESS:
                    stats.success++
                    break
                case Result.FAILURE:
                    stats.failure++
                    break
                case Result.UNSTABLE:
                    stats.unstable++
                    break
                case Result.ABORTED:
                    stats.aborted++
                    break
                default:
                    stats.other++
            }

            if (stats.oldestBuildNumber == null || run.number < stats.oldestBuildNumber) {
                stats.oldestBuildNumber = run.number
                stats.oldestBuildTime = new Date(run.getTimeInMillis())
            }
            if (stats.newestBuildNumber == null || run.number > stats.newestBuildNumber) {
                stats.newestBuildNumber = run.number
                stats.newestBuildTime = new Date(run.getTimeInMillis())
            }
        }

        return stats
    }

    /**
     * Internal lazy iterator implementation.
     */
    private class LazyBuildIterator implements Iterator<Run> {
        private Iterator<Run> baseIterator
        private Run nextBuild = null
        private boolean hasNextCalled = false
        private int skipped = 0
        private int returned = 0
        private boolean stopped = false

        LazyBuildIterator() {
            // Use the job's RunList which is already lazy
            this.baseIterator = job.getBuilds().iterator()
        }

        @Override
        boolean hasNext() {
            if (stopped) return false
            if (returned >= limit) return false

            if (hasNextCalled && nextBuild != null) {
                return true
            }

            nextBuild = findNextMatching()
            hasNextCalled = true
            return nextBuild != null
        }

        @Override
        Run next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more builds")
            }
            hasNextCalled = false
            returned++
            Run result = nextBuild
            nextBuild = null
            return result
        }

        private Run findNextMatching() {
            while (baseIterator.hasNext() && !stopped) {
                Run run = baseIterator.next()

                // Check stop condition
                if (stopCondition != null && stopCondition.test(run)) {
                    stopped = true
                    return null
                }

                // Skip if needed
                if (skipped < skip) {
                    skipped++
                    continue
                }

                // Apply filters
                boolean matches = true
                for (Predicate<Run> filter : filters) {
                    if (!filter.test(run)) {
                        matches = false
                        break
                    }
                }

                if (matches) {
                    return run
                }
            }

            return null
        }
    }
}
