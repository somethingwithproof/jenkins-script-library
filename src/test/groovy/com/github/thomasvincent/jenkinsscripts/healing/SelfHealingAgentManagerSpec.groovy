// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.healing

import hudson.model.Computer
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Subject

@Subject(SelfHealingAgentManager)
class SelfHealingAgentManagerSpec extends Specification {

    def "transitions once into FAILED or REPLACING per outage episode and resets upon recovery"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(Computer)
        boolean offline = true
        agent.name >> 'ephemeral-1'
        agent.isOffline() >> { offline }
        agent.isOnline() >> { !offline }
        agent.offlineCause >> null
        agent.getMonitorData() >> [:]
        jenkins.toComputer() >> null
        jenkins.computers >> { [agent] as Computer[] }

        int failureCallbacks = 0
        int replaceCallbacks = 0
        int recoverySuccessCallbacks = 0

        def manager = new SelfHealingAgentManager([
            maxRetryAttempts: 2,
            enableAutoReconnect: false,
            enableAutoRestart: false,
            enableAutoReplace: true
        ], jenkins)
        manager.onRecoveryFailure { name, reason -> failureCallbacks++ }
        manager.onAgentReplaced { name -> replaceCallbacks++ }
        manager.onRecoverySuccess { name -> recoverySuccessCallbacks++ }

        when: "5 consecutive offline checks occur beyond maxRetryAttempts=2"
        5.times { manager.performHealthCheck() }

        then: "failure and replacement fire exactly once for the outage episode"
        manager.statistics.totalAttempts == 2
        manager.statistics.failed == 1
        failureCallbacks == 1
        replaceCallbacks == 1
        manager.statistics.agents['ephemeral-1'].status == 'REPLACING'

        when: "agent comes back online"
        offline = false
        manager.performHealthCheck()

        then: "state transitions back to HEALTHY and recovery is counted"
        manager.statistics.agents['ephemeral-1'].status == 'HEALTHY'
        manager.statistics.successful == 1
        recoverySuccessCallbacks == 1

        cleanup:
        manager.stop()
    }

    def "prunes removed ephemeral computers during health checks"() {
        given:
        def jenkins = Mock(Jenkins)
        def agentA = Mock(Computer)
        def agentB = Mock(Computer)
        agentA.name >> 'agent-a'
        agentA.isOffline() >> false
        agentA.isOnline() >> true
        agentA.getMonitorData() >> [:]
        agentB.name >> 'agent-b'
        agentB.isOffline() >> false
        agentB.isOnline() >> true
        agentB.getMonitorData() >> [:]

        def activeComputers = [agentA, agentB] as Computer[]
        jenkins.toComputer() >> null
        jenkins.computers >> { activeComputers }

        def manager = new SelfHealingAgentManager([:], jenkins)

        when: "both agents are checked"
        manager.performHealthCheck()

        then:
        manager.agentStatuses.collect { it.agentName }.sort() == ['agent-a', 'agent-b']

        when: "agent-b is removed from Jenkins"
        activeComputers = [agentA] as Computer[]
        manager.performHealthCheck()

        then: "agent-b state is pruned"
        manager.agentStatuses.collect { it.agentName } == ['agent-a']

        cleanup:
        manager.stop()
    }

    def "lazily creates daemon scheduler on start and deduplicates in-flight recovery tasks"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(Computer)
        agent.name >> 'slow-agent'
        agent.isOffline() >> true
        agent.isOnline() >> false
        agent.offlineCause >> null
        jenkins.toComputer() >> null
        jenkins.computers >> ([agent] as Computer[])

        def manager = new SelfHealingAgentManager([
            maxRetryAttempts: 5,
            retryDelayMs: 60000L,
            enableAutoReconnect: true,
            enableAutoRestart: false
        ], jenkins)

        expect: "not running before start()"
        !manager.isRunning()

        when: "two rapid health checks run while first reconnect verification is in flight"
        manager.performHealthCheck()
        manager.performHealthCheck()

        then: "only one reconnect attempt is dispatched while delayed task is pending"
        1 * agent.connect(true)
        manager.statistics.totalAttempts == 1

        when: "start and stop lifecycle runs"
        manager.start()
        boolean runningAfterStart = manager.isRunning()
        manager.stop()

        then:
        runningAfterStart
        !manager.isRunning()
    }
}
