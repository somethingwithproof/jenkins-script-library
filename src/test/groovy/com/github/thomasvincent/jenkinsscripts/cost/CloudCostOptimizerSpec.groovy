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

package com.github.thomasvincent.jenkinsscripts.cost

import com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory
import hudson.model.Computer
import hudson.model.Executor
import hudson.model.Node
import jenkins.model.Jenkins
import spock.lang.Specification

class CloudCostOptimizerSpec extends Specification {

    private CloudInventory inventory = Stub(CloudInventory) {
        owner(_ as Node) >> { Node node -> [provider: 'AWS', cloudName: 'compute', agentId: node.nodeName] }
    }

    private Computer mockCloudComputer(String name, String labels, boolean idle, long idleMinutesAgo, boolean busyExecutor = false) {
        Node node = Mock(Node) {
            getLabelString() >> labels
            getNodeName() >> name
        }
        Executor exec = Mock(Executor) {
            isBusy() >> busyExecutor
            getCurrentExecutable() >> null
        }
        long now = System.currentTimeMillis()
        Computer computer = Mock(Computer) {
            getName() >> name
            getNode() >> node
            isIdle() >> idle
            getConnectTime() >> (now - 120L * 60_000L)
            getIdleStartMilliseconds() >> (idle ? (now - idleMinutesAgo * 60_000L) : 0L)
            getExecutors() >> [exec]
        }
        return computer
    }

    def "monitorBudget computes total cost once per agent and guards zero budget"() {
        given:
        Computer agent1 = mockCloudComputer(
            "aws-1", "aws cloud instance-type=m5.xlarge team=core project=web", false, 0, true)
        Jenkins jenkins = Mock(Jenkins) {
            getComputers() >> ([agent1] as Computer[])
        }
        CloudCostOptimizer optimizer = new CloudCostOptimizer(jenkins, inventory)
        // m5.xlarge hourly cost = 0.192 -> monthly = 0.192 * 24 * 30 = 138.24
        double expectedMonthly = 0.192d * 24.0d * 30.0d

        when:
        Map<String, Object> result = optimizer.monitorBudget([
            'total': 200.0d,
            'team:core': 200.0d,
            'project:web': 0.0d,
            'team:empty': 0.0d
        ])

        then:
        Math.abs(result.budgetStatus['total'].spent - expectedMonthly) < 0.001d
        Math.abs(result.budgetStatus['team:core'].spent - expectedMonthly) < 0.001d
        result.budgetStatus['project:web'].percentUsed == 100.0d
        result.budgetStatus['team:empty'].percentUsed == 0.0d
    }

    def "optimizeResources excludes terminated idle agents from spot and rightsizing actions"() {
        given:
        Computer idleAgent = mockCloudComputer(
            "aws-idle", "aws cloud instance-type=m5.xlarge", true, 60, false)
        Computer activeAgent = mockCloudComputer(
            "aws-active", "aws cloud instance-type=m5.xlarge", false, 0, true)
        Jenkins jenkins = Mock(Jenkins) {
            getComputers() >> ([idleAgent, activeAgent] as Computer[])
            getComputer("aws-idle") >> idleAgent
            getComputer("aws-active") >> activeAgent
        }
        CloudCostOptimizer optimizer = new CloudCostOptimizer(jenkins, inventory)
        // Populate usageData via analyzeCosts
        optimizer.analyzeCosts()

        when:
        Map<String, Object> opt = optimizer.optimizeResources(true)
        List<Map<String, Object>> actions = opt.actions as List<Map<String, Object>>

        then:
        actions.any { it.type == 'TERMINATE_IDLE' && it.agent == 'aws-idle' }
        !actions.any { it.type == 'CONVERT_TO_SPOT' && it.agent == 'aws-idle' }
        !actions.any { it.type == 'RIGHTSIZE_INSTANCE' && it.agent == 'aws-idle' }
        actions.any { it.type == 'CONVERT_TO_SPOT' && it.agent == 'aws-active' }
    }

    def "an apply request reports unsupported actions without claiming realized savings"() {
        given:
        Computer active = mockCloudComputer('worker', 'instance-type=m5.xlarge', false, 0, true)
        Jenkins jenkins = Mock(Jenkins) {
            getComputers() >> ([active] as Computer[])
            getComputer('worker') >> active
        }
        def optimizer = new CloudCostOptimizer(jenkins, inventory)

        when:
        def preview = optimizer.optimizeResources(true)
        def result = optimizer.optimizeResources(false)

        then:
        preview.counts.planned > 0
        preview.actions.every { it.status == 'planned' }
        result.counts.unsupported == preview.counts.planned
        result.actions.every { it.status == 'unsupported' }
        result.counts.applied == 0
        result.estimatedMonthlySavings > 0
    }

    def "analyzeCosts prunes removed computers from usageData"() {
        given:
        Computer ephemeral = mockCloudComputer("aws-ephemeral", "aws cloud instance-type=m5.xlarge", true, 45, false)
        Computer[] current = [ephemeral] as Computer[]
        Jenkins jenkins = Mock(Jenkins) {
            getComputers() >> { current }
            getComputer("aws-ephemeral") >> { current.length > 0 ? ephemeral : null }
        }
        CloudCostOptimizer optimizer = new CloudCostOptimizer(jenkins, inventory)

        when:
        optimizer.analyzeCosts()
        current = [] as Computer[]
        optimizer.analyzeCosts()
        Map<String, Object> opt = optimizer.optimizeResources(true)

        then:
        (opt.actions as List).isEmpty()
    }
}
