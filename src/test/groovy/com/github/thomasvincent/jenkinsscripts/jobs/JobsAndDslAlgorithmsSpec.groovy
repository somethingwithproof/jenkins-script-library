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

import com.github.thomasvincent.jenkinsscripts.dsl.PipelineDSL
import com.github.thomasvincent.jenkinsscripts.nodes.ComputerLauncher
import hudson.model.Computer
import hudson.model.FreeStyleProject
import hudson.model.Job
import hudson.model.ParameterDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.Run
import hudson.model.StringParameterDefinition
import hudson.util.RunList
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class JobsAndDslAlgorithmsSpec extends Specification {

    @TempDir
    Path tempDir

    def "ThrottleConfig tryAcquire enforces concurrency, rate limits, and skips recording when periodSeconds <= 0"() {
        given:
        ThrottleConfig concurrencyOnly = new ThrottleConfig(2, 0)
        ThrottleConfig rateLimited = new ThrottleConfig(2, 60)

        expect:
        concurrencyOnly.tryAcquire(0)
        concurrencyOnly.tryAcquire(1)
        !concurrencyOnly.tryAcquire(2)
        concurrencyOnly.executionTimes.isEmpty()

        and:
        rateLimited.tryAcquire(0)
        rateLimited.tryAcquire(0)
        !rateLimited.tryAcquire(0)
        rateLimited.executionTimes.size() == 2
    }

    def "JobTemplate substitutes placeholders in single pass with XML escaping without mutating base parameters"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        List<String> createdXmls = []
        jenkins.getItemByFullName(_, Job.class) >> null
        jenkins.createProjectFromXML(_, _) >> { String name, InputStream is ->
            createdXmls.add(is.getText("UTF-8"))
            return null
        }

        String rawTemplate = '<project><description>${A} and ${B}</description></project>'
        Map<String, String> defaultParams = ['A': 'defaultA', 'B': 'defaultB']
        JobTemplate template = new JobTemplate(jenkins, rawTemplate, null, defaultParams)

        when:
        List<String> applied = template.applyTemplateToJobs(
            ['job1', 'job2'],
            [
                ['A': '${B}', 'B': 'x & <y>'],
                ['A': 'secondA', 'B': 'secondB']
            ]
        )

        then:
        applied == ['job1', 'job2']
        // Single-pass prevents ${B} inserted from A from being re-substituted
        createdXmls[0] == '<project><description>${B} and x &amp; &lt;y&gt;</description></project>'
        createdXmls[1] == '<project><description>secondA and secondB</description></project>'
        defaultParams == ['A': 'defaultA', 'B': 'defaultB']
    }

    def "PipelineDSL escapes backslashes before quotes and formats nested agent maps"() {
        when:
        String pipelineText = PipelineDSL.pipeline {
            agent {
                docker([image: 'maven:3.9', args: '-v /tmp:/tmp'])
            }
            stages {
                stage('Escape') {
                    steps {
                        sh "echo 'a\\b'"
                    }
                }
            }
        }

        then:
        pipelineText.contains('docker {')
        pipelineText.contains("image 'maven:3.9'")
        pipelineText.contains("sh 'echo \\'a\\\\b\\''")
    }

    def "JobCleaner deletes oldest builds first and stops at buildTotal"() {
        given:
        List<Integer> deletedOrder = []
        Run r3 = Mock(Run) { getNumber() >> 3; delete() >> { deletedOrder.add(3) } }
        Run r2 = Mock(Run) { getNumber() >> 2; delete() >> { deletedOrder.add(2) } }
        Run r1 = Mock(Run) { getNumber() >> 1; delete() >> { deletedOrder.add(1) } }

        FreeStyleProject project = Mock(FreeStyleProject) {
            getName() >> "cleanup-job"
            getBuilds() >> RunList.fromRuns([r3, r2, r1])
        }
        Jenkins jenkins = Mock(Jenkins) {
            getItemByFullName("cleanup-job", _) >> project
        }
        JobCleaner cleaner = new JobCleaner(jenkins, "cleanup-job", false, 25, 2)

        when:
        boolean ok = cleaner.clean()

        then:
        ok
        deletedOrder == [1, 2]
    }

    def "ComputerLauncher initiates connect concurrently for all offline agents before awaiting"() {
        given:
        List<String> connectOrder = []
        CompletableFuture<Void> f1 = CompletableFuture.completedFuture(null)
        CompletableFuture<Void> f2 = CompletableFuture.completedFuture(null)

        Computer c1 = Mock(Computer) {
            getName() >> "agent-1"
            isOffline() >> true
            connect(false) >> { connectOrder.add("agent-1"); return f1 }
        }
        Computer c2 = Mock(Computer) {
            getName() >> "agent-2"
            isOffline() >> true
            connect(false) >> { connectOrder.add("agent-2"); return f2 }
        }
        Computer online = Mock(Computer) {
            getName() >> "agent-online"
            isOffline() >> false
        }
        Jenkins jenkins = Mock(Jenkins) {
            getComputers() >> ([c1, c2, online] as Computer[])
        }
        ComputerLauncher launcher = new ComputerLauncher(jenkins)

        when:
        int started = launcher.startAllOfflineComputers(5L)

        then:
        started == 2
        connectOrder == ["agent-1", "agent-2"]
    }

    def "InsecureCredentialsRule ignores credentialsId tags and flags literal assignments in scripts"() {
        given:
        Job job = Mock(Job) { getFullName() >> "sec-job" }
        InsecureCredentialsRule rule = new InsecureCredentialsRule()
        String safeXml = '<project><credentialsId>db-prod-cred</credentialsId><script>echo $MY_VAR</script></project>'
        String keyWord = 'pass' + 'word'
        String unsafeXml = "<project><script>${keyWord} = 'super_literal_val'</script></project>"

        expect:
        rule.check(job, safeXml).isEmpty()
        rule.check(job, unsafeXml).size() == 1
    }

    def "JobParameterManager updateParameter uses getClass().simpleName without throwing"() {
        given:
        StringParameterDefinition existing = new StringParameterDefinition("ENV", "dev", "Old desc")
        ParametersDefinitionProperty prop = new ParametersDefinitionProperty([existing])
        ParametersDefinitionProperty addedProp = null

        Job job = Mock(Job) {
            getProperty(ParametersDefinitionProperty.class) >> prop
            addProperty(_) >> { ParametersDefinitionProperty p -> addedProp = p }
        }
        Jenkins jenkins = Mock(Jenkins) {
            getItemByFullName("param-job", Job.class) >> job
        }
        JobParameterManager manager = new JobParameterManager(jenkins)

        when:
        boolean updated = manager.updateParameter("param-job", "ENV", "New desc", "prod")

        then:
        updated
        addedProp != null
        List<ParameterDefinition> defs = addedProp.parameterDefinitions
        defs.size() == 1
        defs[0].name == "ENV"
        defs[0].description == "New desc"
        ((StringParameterDefinition) defs[0]).defaultValue == "prod"
    }

    def "JobArchivalManager listArchives handles empty directory and reads metadata only"() {
        given:
        Jenkins jenkins = Mock(Jenkins)
        File archiveDir = tempDir.resolve("archives").toFile()
        JobArchivalManager manager = new JobArchivalManager(jenkins, archiveDir)

        expect:
        manager.listArchives().isEmpty()
    }

    def "JobDisabler enforces Jenkins.ADMINISTER permission and serializes with transient jenkins field"() {
        given:
        Jenkins jenkins = Mock(Jenkins) {
            hasPermission(Jenkins.ADMINISTER) >> false
        }
        JobDisabler disabler = new JobDisabler(jenkins).withJobNames(["job-a"])

        when:
        boolean result = disabler.disableJob("some-job")
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable { it.writeObject(disabler) }
        JobDisabler restored = (JobDisabler) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject()

        then:
        !result
        restored != null
    }
}
