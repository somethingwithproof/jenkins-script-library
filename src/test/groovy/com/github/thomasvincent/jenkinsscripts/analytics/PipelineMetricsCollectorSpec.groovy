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

package com.github.thomasvincent.jenkinsscripts.analytics

import hudson.model.Job
import hudson.model.Result
import hudson.util.RunList
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.actions.LabelAction
import org.jenkinsci.plugins.workflow.actions.TimingAction
import org.jenkinsci.plugins.workflow.flow.FlowExecution
import org.jenkinsci.plugins.workflow.graph.BlockEndNode
import org.jenkinsci.plugins.workflow.graph.BlockStartNode
import org.jenkinsci.plugins.workflow.graph.FlowNode
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import spock.lang.Specification

class PipelineMetricsCollectorSpec extends Specification {

    private WorkflowRun mockWorkflowRun(int number, Result result, long durationMs, long startTimeMs = 1_000_000L, FlowExecution exec = null) {
        WorkflowRun run = Mock(WorkflowRun)
        run.getNumber() >> number
        run.getResult() >> result
        run.getDuration() >> durationMs
        run.getStartTimeInMillis() >> startTimeMs
        run.getTimeInMillis() >> (startTimeMs - 500L)
        run.getExecution() >> exec
        run.getExecutor() >> null
        return run
    }

    def "handles all IN_PROGRESS builds without division by zero or empty collection exceptions"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        WorkflowJob job = Mock(WorkflowJob)
        WorkflowRun inProgress = mockWorkflowRun(1, null, 0L)
        jenkins.getItemByFullName("running-pipe", WorkflowJob) >> job
        job.getBuilds() >> RunList.fromRuns([inProgress])

        PipelineMetricsCollector collector = new PipelineMetricsCollector(jenkins)

        when:
        Map<String, Object> metrics = collector.collectPipelineMetrics("running-pipe")

        then:
        metrics.summary.totalBuilds == 0
        metrics.summary.avgDuration == 0L
        metrics.summary.successRate == 0.0d
        metrics.anomalies.isEmpty()
        metrics.trends.message == 'Insufficient data for trend analysis'
    }

    def "filters out IN_PROGRESS builds and handles identical durations (zero stdDev) without NaN"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        WorkflowJob job = Mock(WorkflowJob)
        List<WorkflowRun> runs = [
            mockWorkflowRun(7, null, 0L),
            mockWorkflowRun(6, Result.SUCCESS, 1000L),
            mockWorkflowRun(5, Result.SUCCESS, 1000L),
            mockWorkflowRun(4, Result.SUCCESS, 1000L),
            mockWorkflowRun(3, Result.SUCCESS, 1000L),
            mockWorkflowRun(2, Result.SUCCESS, 1000L),
            mockWorkflowRun(1, Result.SUCCESS, 1000L)
        ]
        jenkins.getItemByFullName("steady-pipe", WorkflowJob) >> job
        job.getBuilds() >> RunList.fromRuns(runs)

        PipelineMetricsCollector collector = new PipelineMetricsCollector(jenkins)

        when:
        Map<String, Object> metrics = collector.collectPipelineMetrics("steady-pipe")

        then:
        metrics.summary.totalBuilds == 6
        metrics.summary.successRate == 100.0d
        metrics.summary.avgDuration == 1000L
        metrics.summary.stdDeviation == 0.0d
        metrics.anomalies.isEmpty()
        metrics.trends.durationChange == 0.0d
    }

    def "guards zero historicalAvg in trend calculation"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        WorkflowJob job = Mock(WorkflowJob)
        List<WorkflowRun> runs = [
            mockWorkflowRun(6, Result.SUCCESS, 100L),
            mockWorkflowRun(5, Result.SUCCESS, 100L),
            mockWorkflowRun(4, Result.SUCCESS, 0L),
            mockWorkflowRun(3, Result.SUCCESS, 0L),
            mockWorkflowRun(2, Result.SUCCESS, 0L),
            mockWorkflowRun(1, Result.SUCCESS, 0L)
        ]
        jenkins.getItemByFullName("zero-hist", WorkflowJob) >> job
        job.getBuilds() >> RunList.fromRuns(runs)

        PipelineMetricsCollector collector = new PipelineMetricsCollector(jenkins)

        when:
        Map<String, Object> metrics = collector.collectPipelineMetrics("zero-hist")

        then:
        metrics.trends.durationChange == 0.0d
    }

    def "computes actual stage duration from BlockStartNode to matching BlockEndNode"() {
        given:
        BlockStartNode startNode = Mock(BlockStartNode) {
            getId() >> "10"
            getDisplayName() >> "Build"
            getParents() >> []
            getAction(LabelAction) >> new LabelAction("Build")
            getAction(TimingAction) >> Stub(TimingAction) { getStartTime() >> 5_000L }
            getAction(_) >> null
        }
        BlockEndNode endNode = Mock(BlockEndNode) {
            getId() >> "15"
            getStartNode() >> startNode
            getDisplayName() >> "End Build"
            getParents() >> [startNode]
            getAction(TimingAction) >> Stub(TimingAction) { getStartTime() >> 12_500L }
            getAction(_) >> null
        }
        FlowExecution execution = Mock(FlowExecution) {
            getCurrentHeads() >> [endNode]
        }
        WorkflowRun run = mockWorkflowRun(1, Result.SUCCESS, 10_000L, 5_000L, execution)

        Jenkins jenkins = Mock(Jenkins)
        WorkflowJob job = Mock(WorkflowJob)
        jenkins.getItemByFullName("stage-pipe", WorkflowJob) >> job
        job.getBuilds() >> RunList.fromRuns([run])

        PipelineMetricsCollector collector = new PipelineMetricsCollector(jenkins)

        when:
        Map<String, Object> metrics = collector.collectPipelineMetrics("stage-pipe")

        then:
        metrics.stageMetrics.containsKey("Build")
        metrics.stageMetrics["Build"].avgDuration == 7_500.0d
        metrics.stageMetrics["Build"].minDuration == 7_500L
        metrics.stageMetrics["Build"].maxDuration == 7_500L
        metrics.stageMetrics["Build"].failureRate == 0.0d
    }
}
