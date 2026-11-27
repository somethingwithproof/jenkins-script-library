// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.healing

import hudson.model.Computer
import hudson.model.Slave
import hudson.slaves.OfflineCause
import hudson.slaves.SlaveComputer
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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
        1 * agent.connect(false)
        manager.statistics.totalAttempts == 1

        when: "start and stop lifecycle runs"
        manager.start()
        boolean runningAfterStart = manager.isRunning()
        manager.stop()

        then:
        runningAfterStart
        !manager.isRunning()
    }

    def "singlePass mode attempts reconnect synchronously without starting background scheduler"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(Computer)
        agent.name >> 'single-pass-agent'
        agent.isOffline() >> true
        agent.isOnline() >> false
        agent.offlineCause >> null
        jenkins.toComputer() >> null
        jenkins.computers >> ([agent] as Computer[])

        def manager = new SelfHealingAgentManager([
            maxRetryAttempts   : 1,
            enableAutoReconnect: true,
            enableAutoRestart  : false,
            singlePass         : true
        ], jenkins)

        when:
        manager.performHealthCheck()

        then:
        1 * agent.connect(false)
        !manager.isRunning()
        manager.statistics.totalAttempts == 1
        manager.statistics.agents['single-pass-agent'].status == 'RECOVERING'

        cleanup:
        manager.stop()
    }

    def "readOnly mode records OFFLINE status without recovery attempts or callbacks"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(Computer)
        boolean offline = false
        agent.name >> 'ro-agent'
        agent.isOffline() >> { offline }
        agent.isOnline() >> { !offline }
        agent.offlineCause >> null
        agent.getMonitorData() >> [:]
        jenkins.toComputer() >> null
        jenkins.computers >> ([agent] as Computer[])

        int callbacks = 0
        def manager = new SelfHealingAgentManager([
            readOnly           : true,
            singlePass         : true,
            enableAutoReconnect: true,
            enableAutoRestart  : true
        ], jenkins)
        manager.onRecoveryStart { name, attempt -> callbacks++ }
        manager.onRecoveryFailure { name, reason -> callbacks++ }

        when: "agent is observed online then offline"
        manager.performHealthCheck()
        String statusWhenOnline = manager.statistics.agents['ro-agent'].status
        offline = true
        manager.performHealthCheck()

        then: "status updates from HEALTHY to OFFLINE without calling connect"
        0 * agent.connect(_)
        statusWhenOnline == 'HEALTHY'
        manager.statistics.agents['ro-agent'].status == 'OFFLINE'
        manager.statistics.totalAttempts == 0
        callbacks == 0

        cleanup:
        manager.stop()
    }

    def "singlePass mode does not restart while reconnect future is still pending"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(SlaveComputer)
        def pendingFuture = new CompletableFuture<Void>()
        agent.name >> 'pending-agent'
        agent.isOffline() >> true
        agent.isOnline() >> false
        agent.offlineCause >> null
        agent.node >> Mock(Slave)
        jenkins.toComputer() >> null
        jenkins.computers >> ([agent] as Computer[])

        def manager = new SelfHealingAgentManager([
            maxRetryAttempts   : 1,
            enableAutoReconnect: true,
            enableAutoRestart  : true,
            singlePass         : true
        ], jenkins)

        when:
        manager.performHealthCheck()

        then:
        1 * agent.connect(false) >> pendingFuture
        0 * agent.disconnect(_)
        manager.hasPendingRecovery('pending-agent')
        manager.statistics.agents['pending-agent'].status == 'RECOVERING'

        when: "stop is called while launch is in flight"
        manager.stop()

        then: "external connect future is not cancelled"
        !pendingFuture.isCancelled()
    }

    def "singlePass restart uses non-UserCause disconnect and schedules deferred reconnect when disconnect times out"() {
        given:
        def jenkins = Mock(Jenkins)
        def agent = Mock(SlaveComputer)
        def slowDisconnect = new CompletableFuture<Void>()
        OfflineCause recordedCause = null
        agent.name >> 'restart-agent'
        agent.isOffline() >> true
        agent.isOnline() >> false
        agent.offlineCause >> { recordedCause }
        agent.node >> Mock(Slave)
        jenkins.toComputer() >> null
        jenkins.computers >> ([agent] as Computer[])

        def manager = new SelfHealingAgentManager([
            maxRetryAttempts   : 1,
            retryDelayMs       : 50L,
            enableAutoReconnect: false,
            enableAutoRestart  : true,
            singlePass         : true
        ], jenkins)

        when: "disconnect takes longer than the bounded wait"
        manager.performHealthCheck()

        then: "disconnect used a non-UserCause and deferred reconnect is tracked without premature connect"
        1 * agent.disconnect(_ as SelfHealingAgentManager.SelfHealingRestartCause) >> { OfflineCause c ->
            recordedCause = c
            return slowDisconnect
        }
        !(recordedCause instanceof OfflineCause.UserCause)
        0 * agent.connect(_)
        manager.hasPendingRecovery('restart-agent')

        when: "disconnect finally completes"
        slowDisconnect.complete(null)

        then: "deferred reconnect executes and clears pending recovery"
        1 * agent.connect(false)
        !manager.hasPendingRecovery('restart-agent')

        cleanup:
        manager.stop()
    }
}
