package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Subject
import spock.lang.Unroll
import hudson.model.Job
import hudson.model.Run
import hudson.model.Result
import hudson.util.RunList

/**
 * Spock specification for BuildIterator utility class.
 */
@Title("BuildIterator Specification")
@Subject(BuildIterator)
class BuildIteratorSpec extends Specification {

    def "forJob creates BuildIterator instance"() {
        given: "a mock job"
        def job = mockJob([])

        when: "creating BuildIterator"
        def iterator = BuildIterator.forJob(job)

        then: "returns BuildIterator instance"
        iterator != null
        iterator instanceof BuildIterator
    }

    def "forJob throws on null job"() {
        when: "creating BuildIterator with null job"
        BuildIterator.forJob(null)

        then: "throws IllegalArgumentException"
        thrown(IllegalArgumentException)
    }

    def "limit restricts number of builds returned"() {
        given: "a job with multiple builds"
        def builds = (1..10).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "iterating with limit"
        def result = BuildIterator.forJob(job)
            .limit(3)
            .collect()

        then: "returns only limited number"
        result.size() == 3
    }

    def "skip ignores first N builds"() {
        given: "a job with multiple builds"
        def builds = (1..10).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "iterating with skip"
        def result = BuildIterator.forJob(job)
            .skip(3)
            .limit(3)
            .collect()

        then: "skips first 3 builds"
        result.size() == 3
        result[0].number == 4
    }

    def "filter with closure filters builds"() {
        given: "a job with mixed results"
        def builds = [
            mockBuild(1, Result.SUCCESS),
            mockBuild(2, Result.FAILURE),
            mockBuild(3, Result.SUCCESS),
            mockBuild(4, Result.FAILURE),
            mockBuild(5, Result.SUCCESS)
        ]
        def job = mockJob(builds)

        when: "filtering for failures"
        def result = BuildIterator.forJob(job)
            .filter { it.result == Result.FAILURE }
            .collect()

        then: "returns only failures"
        result.size() == 2
        result.every { it.result == Result.FAILURE }
    }

    def "successful() filters for successful builds"() {
        given: "a job with mixed results"
        def builds = [
            mockBuild(1, Result.SUCCESS),
            mockBuild(2, Result.FAILURE),
            mockBuild(3, Result.SUCCESS)
        ]
        def job = mockJob(builds)

        when: "filtering for successful builds"
        def result = BuildIterator.forJob(job)
            .successful()
            .collect()

        then: "returns only successful builds"
        result.size() == 2
        result.every { it.result == Result.SUCCESS }
    }

    def "failed() filters for failed builds"() {
        given: "a job with mixed results"
        def builds = [
            mockBuild(1, Result.SUCCESS),
            mockBuild(2, Result.FAILURE),
            mockBuild(3, Result.UNSTABLE),
            mockBuild(4, Result.SUCCESS)
        ]
        def job = mockJob(builds)

        when: "filtering for failed builds"
        def result = BuildIterator.forJob(job)
            .failed()
            .collect()

        then: "returns failures and unstable"
        result.size() == 2
        result.every { it.result in [Result.FAILURE, Result.UNSTABLE] }
    }

    def "count returns number of matching builds"() {
        given: "a job with builds"
        def builds = (1..5).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "counting builds"
        def count = BuildIterator.forJob(job).count()

        then: "returns correct count"
        count == 5
    }

    def "first returns first matching build"() {
        given: "a job with builds"
        def builds = (1..5).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "getting first build"
        def first = BuildIterator.forJob(job).first()

        then: "returns first build"
        first.number == 1
    }

    def "first returns null for empty results"() {
        given: "a job with no builds"
        def job = mockJob([])

        when: "getting first build"
        def first = BuildIterator.forJob(job).first()

        then: "returns null"
        first == null
    }

    def "any returns true when builds exist"() {
        given: "a job with builds"
        def builds = [mockBuild(1, Result.SUCCESS)]
        def job = mockJob(builds)

        when: "checking any"
        def result = BuildIterator.forJob(job).any()

        then: "returns true"
        result == true
    }

    def "any returns false when no builds match"() {
        given: "a job with no matching builds"
        def builds = [mockBuild(1, Result.SUCCESS)]
        def job = mockJob(builds)

        when: "checking any with non-matching filter"
        def result = BuildIterator.forJob(job)
            .filter { it.result == Result.FAILURE }
            .any()

        then: "returns false"
        result == false
    }

    def "inBatches processes builds in batches"() {
        given: "a job with 10 builds"
        def builds = (1..10).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)
        def batchSizes = []

        when: "processing in batches of 3"
        BuildIterator.forJob(job).inBatches(3) { batch ->
            batchSizes << batch.size()
        }

        then: "processes in expected batch sizes"
        batchSizes == [3, 3, 3, 1]
    }

    def "map transforms builds"() {
        given: "a job with builds"
        def builds = (1..3).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "mapping to build numbers"
        def numbers = BuildIterator.forJob(job).map { it.number }

        then: "returns transformed values"
        numbers == [1, 2, 3]
    }

    def "forEach applies action to each build"() {
        given: "a job with builds"
        def builds = (1..3).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)
        def processed = []

        when: "applying forEach"
        BuildIterator.forJob(job).forEach { processed << it.number }

        then: "action applied to all builds"
        processed == [1, 2, 3]
    }

    def "stopWhen stops iteration"() {
        given: "a job with builds"
        def builds = (1..10).collect { mockBuild(it, Result.SUCCESS) }
        def job = mockJob(builds)

        when: "stopping at build 5"
        def result = BuildIterator.forJob(job)
            .stopWhen { it.number == 5 }
            .collect()

        then: "stops before the matching build"
        result.size() == 4
        result.every { it.number < 5 }
    }

    def "multiple filters are AND-ed together"() {
        given: "a job with varied builds"
        def builds = (1..10).collect { num ->
            mockBuild(num, num % 2 == 0 ? Result.SUCCESS : Result.FAILURE)
        }
        def job = mockJob(builds)

        when: "applying multiple filters"
        def result = BuildIterator.forJob(job)
            .filter { it.result == Result.SUCCESS }
            .filter { it.number > 5 }
            .collect()

        then: "returns only builds matching all filters"
        result.size() == 2
        result.every { it.result == Result.SUCCESS && it.number > 5 }
    }

    def "getStats returns correct statistics"() {
        given: "a job with varied builds"
        def builds = [
            mockBuild(1, Result.SUCCESS, 1000),
            mockBuild(2, Result.FAILURE, 2000),
            mockBuild(3, Result.SUCCESS, 1500),
            mockBuild(4, Result.UNSTABLE, 500),
            mockBuild(5, Result.ABORTED, 100)
        ]
        def job = mockJob(builds)

        when: "getting stats"
        def stats = BuildIterator.forJob(job).getStats()

        then: "returns correct statistics"
        stats.total == 5
        stats.success == 2
        stats.failure == 1
        stats.unstable == 1
        stats.aborted == 1
        stats.totalDurationMs == 5100
        stats.oldestBuildNumber == 1
        stats.newestBuildNumber == 5
    }

    def "olderThanDays filters old builds"() {
        given: "a job with builds of different ages"
        def now = System.currentTimeMillis()
        def builds = [
            mockBuildWithTime(1, Result.SUCCESS, now - (1 * 24 * 60 * 60 * 1000)),  // 1 day old
            mockBuildWithTime(2, Result.SUCCESS, now - (5 * 24 * 60 * 60 * 1000)),  // 5 days old
            mockBuildWithTime(3, Result.SUCCESS, now - (10 * 24 * 60 * 60 * 1000)), // 10 days old
        ]
        def job = mockJob(builds)

        when: "filtering builds older than 3 days"
        def result = BuildIterator.forJob(job)
            .olderThanDays(3)
            .collect()

        then: "returns only old builds"
        result.size() == 2
        result*.number == [2, 3]
    }

    // Helper methods to create mock objects

    private Job mockJob(List<Run> builds) {
        def runList = Mock(RunList)
        runList.iterator() >> builds.iterator()

        def job = Mock(Job)
        job.getBuilds() >> runList

        return job
    }

    private Run mockBuild(int number, Result result, long duration = 1000) {
        def build = Mock(Run)
        build.getNumber() >> number
        build.getResult() >> result
        build.getDuration() >> duration
        build.getTimeInMillis() >> System.currentTimeMillis()
        return build
    }

    private Run mockBuildWithTime(int number, Result result, long timeInMillis) {
        def build = Mock(Run)
        build.getNumber() >> number
        build.getResult() >> result
        build.getDuration() >> 1000
        build.getTimeInMillis() >> timeInMillis
        return build
    }
}
