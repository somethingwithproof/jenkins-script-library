// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.jobs

import hudson.model.AbstractProject
import hudson.model.Job
import hudson.model.Saveable
import hudson.tasks.BuildTrigger
import hudson.util.DescribableList
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Subject

@Subject(JobDependencyManager)
class JobDependencyManagerSpec extends Specification {

    def "CompleteDependencyGraph detects and canonically deduplicates cycles without mutating DFS path"() {
        given:
        def graph = new CompleteDependencyGraph()
        def a = new JobDependencyGraph('job-c')
        a.downstreamJobs.add('job-a')
        def b = new JobDependencyGraph('job-a')
        b.downstreamJobs.add('job-b')
        def c = new JobDependencyGraph('job-b')
        c.downstreamJobs.add('job-c')
        def self = new JobDependencyGraph('self-loop')
        self.downstreamJobs.add('self-loop')

        [a, b, c, self].each { graph.addJobGraph(it) }

        when:
        def cycles = graph.findCircularDependencies()

        then:
        cycles.size() == 2
        cycles.contains(['job-a', 'job-b', 'job-c'])
        cycles.contains(['self-loop'])
    }

    def "CompleteDependencyGraph reports no cycles in diamond DAG and filters orphans by pattern"() {
        given:
        def graph = new CompleteDependencyGraph()
        def root = new JobDependencyGraph('root')
        root.downstreamJobs.addAll(['left', 'right'])
        def left = new JobDependencyGraph('left')
        left.upstreamJobs.add('root')
        left.downstreamJobs.add('leaf')
        def right = new JobDependencyGraph('right')
        right.upstreamJobs.add('root')
        right.downstreamJobs.add('leaf')
        def leaf = new JobDependencyGraph('leaf')
        leaf.upstreamJobs.addAll(['left', 'right'])
        def orphanA = new JobDependencyGraph('orphan-1')
        def orphanB = new JobDependencyGraph('temp-orphan')

        [root, left, right, leaf, orphanA, orphanB].each { graph.addJobGraph(it) }

        expect:
        graph.findCircularDependencies().isEmpty()
        graph.findOrphanedJobs() == ['orphan-1', 'temp-orphan']
        graph.findOrphanedJobs('temp-.*') == ['orphan-1']
    }

    def "getCompleteDependencyGraph builds downstream and reversed upstream edges in one pass"() {
        given:
        def jenkins = Mock(Jenkins)
        def upstream = Mock(AbstractProject)
        def downstream = Mock(AbstractProject)
        def orphan = Mock(AbstractProject)
        def upPublishers = Mock(DescribableList)
        def emptyPublishers = Mock(DescribableList)
        def trigger = Mock(BuildTrigger)

        upstream.fullName >> 'build-app'
        downstream.fullName >> 'deploy-app'
        orphan.fullName >> 'standalone'

        upstream.getPublishersList() >> upPublishers
        downstream.getPublishersList() >> emptyPublishers
        orphan.getPublishersList() >> emptyPublishers
        upPublishers.get(BuildTrigger.class) >> trigger
        emptyPublishers.get(BuildTrigger.class) >> null
        trigger.getChildProjectsValue() >> ' deploy-app , '

        def manager = new JobDependencyManager(jenkins)

        when:
        def completeGraph = manager.getCompleteDependencyGraph()

        then:
        1 * jenkins.getAllItems(Job.class) >> [upstream, downstream, orphan]
        0 * jenkins.getAllItems(AbstractProject.class)
        completeGraph.jobGraphs['build-app'].downstreamJobs == ['deploy-app'] as Set
        completeGraph.jobGraphs['deploy-app'].upstreamJobs == ['build-app'] as Set
        completeGraph.findOrphanedJobs() == ['standalone']
    }

    def "addJobDependency registers BuildTrigger via publishersList instead of addProperty"() {
        given:
        def jenkins = Mock(Jenkins)
        def upstream = Mock(AbstractProject)
        def downstream = Mock(AbstractProject)
        def publishers = new DescribableList(Saveable.NOOP)

        jenkins.getItemByFullName('build', AbstractProject.class) >> upstream
        jenkins.getItemByFullName('test', AbstractProject.class) >> downstream
        upstream.fullName >> 'build'
        downstream.fullName >> 'test'
        upstream.getPublishersList() >> publishers

        def manager = new JobDependencyManager(jenkins)

        when:
        boolean added = manager.addJobDependency('build', 'test')
        BuildTrigger registered = publishers.get(BuildTrigger.class)

        then:
        added
        registered != null
        registered.getChildProjectsValue() == 'test'
        0 * upstream.addProperty(_)
        1 * upstream.save()
        1 * jenkins.rebuildDependencyGraph()
    }
}
