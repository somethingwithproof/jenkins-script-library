/*
 * MIT License
 *
 * Copyright (c) 2023-2026 Thomas Vincent
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

import hudson.model.Job
import hudson.model.Result
import hudson.model.Run
import hudson.util.RunList
import jenkins.model.Jenkins
import spock.lang.Specification

class JobHealthAnalyzerSpec extends Specification {

    private Run mockRun(int number, Result result, long durationMs, long ageDays) {
        Run run = Mock(Run)
        Date timestamp = new Date(System.currentTimeMillis() - ageDays * 86_400_000L)
        run.getNumber() >> number
        run.getResult() >> result
        run.getDuration() >> durationMs
        run.getTime() >> timestamp
        return run
    }

    def "computes duration trend accurately for odd number of builds using integer division"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        Job job = Mock(Job)
        // Newest-first: 5 builds where recent 2 take 2000ms and older 3 take 1000ms
        List<Run> runs = [
            mockRun(5, Result.SUCCESS, 2000L, 1),
            mockRun(4, Result.SUCCESS, 2000L, 2),
            mockRun(3, Result.SUCCESS, 1000L, 3),
            mockRun(2, Result.SUCCESS, 1000L, 4),
            mockRun(1, Result.SUCCESS, 1000L, 5)
        ]
        job.getFullName() >> "odd-job"
        job.getBuilds() >> RunList.fromRuns(runs)
        jenkins.getItemByFullName("odd-job", Job.class) >> job

        JobHealthAnalyzer analyzer = new JobHealthAnalyzer(jenkins, 20, 30)

        when:
        JobHealthReport report = analyzer.analyzeJob("odd-job")

        then:
        report != null
        report.buildCount == 5
        report.successRate == 100.0d
        report.minDuration == 1000L
        report.maxDuration == 2000L
        Math.abs(report.durationTrend - 100.0d) < 0.001d
    }

    def "stops scanning builds early once buildCount or cutoffDate is reached"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        Job job = Mock(Job)
        Run recent1 = mockRun(5, Result.SUCCESS, 1000L, 1)
        Run recent2 = mockRun(4, Result.FAILURE, 2000L, 2)
        Run old1 = mockRun(3, Result.SUCCESS, 3000L, 45)
        Run unreachable = Mock(Run)
        job.getFullName() >> "cutoff-job"
        job.getBuilds() >> RunList.fromRuns([recent1, recent2, old1, unreachable])
        jenkins.getItemByFullName("cutoff-job", Job.class) >> job

        JobHealthAnalyzer analyzer = new JobHealthAnalyzer(jenkins, 10, 30)

        when:
        JobHealthReport report = analyzer.analyzeJob("cutoff-job")

        then:
        0 * unreachable.getTime()
        report.buildCount == 2
        report.successCount == 1
        report.failureCount == 1
        report.successRate == 50.0d
    }

    def "analyzeAllJobs filters by regex pattern using imported Pattern class"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        Job jobA = Mock(Job) {
            getFullName() >> "team-a/build"
            getBuilds() >> RunList.fromRuns([mockRun(1, Result.SUCCESS, 500L, 1)])
        }
        Job jobB = Mock(Job) {
            getFullName() >> "team-b/deploy"
            getBuilds() >> RunList.fromRuns([mockRun(1, Result.SUCCESS, 500L, 1)])
        }
        jenkins.getAllItems(Job.class) >> [jobA, jobB]
        jenkins.getItemByFullName("team-a/build", Job.class) >> jobA
        jenkins.getItemByFullName("team-b/deploy", Job.class) >> jobB

        JobHealthAnalyzer analyzer = new JobHealthAnalyzer(jenkins)

        when:
        List<JobHealthReport> reports = analyzer.analyzeAllJobs("team-a/.*")

        then:
        reports.size() == 1
        reports[0].jobName == "team-a/build"
    }
}
