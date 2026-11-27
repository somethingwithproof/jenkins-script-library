// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.vars

import com.cloudbees.hudson.plugins.folder.Folder
import com.lesfurets.jenkins.unit.BasePipelineTest
import hudson.PluginManager
import hudson.PluginWrapper
import hudson.model.AbstractProject
import hudson.model.Computer
import hudson.model.Node
import hudson.model.Result
import hudson.model.Run
import hudson.model.Queue
import hudson.security.AuthorizationStrategy
import hudson.security.SecurityRealm
import hudson.slaves.Cloud
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.mockito.MockedStatic
import org.mockito.Mockito
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * JenkinsPipelineUnit test suite exercising every vars/*.groovy shared library step.
 */
class VarsPipelineUnitSpec extends Specification {

    static class PipelineUnitHarness extends BasePipelineTest {
        List<String> echoed = []
        List<String> shScripts = []
        Map<String, String> writtenFiles = [:]
        Map<String, String> existingFiles = [:]
        List<Map> archived = []
        List<Map> emails = []
        List<String> deletedDirs = []
        List<String> allocatedNodes = []
        Map<String, String> toolHomes = [:]
        Map<String, Closure> shHandlers = [:]
        Map<Class, Object> contextObjects = [:]
        boolean unix = true
        String tmpDir = '/tmp/pipeline-unit'

        @Override
        void setUp() throws Exception {
            scriptRoots = ['.']
            scriptExtension = 'groovy'
            super.setUp()

            binding.setVariable('env', [
                JOB_NAME    : 'platform/demo-service',
                BUILD_NUMBER: '42',
                BUILD_URL   : 'https://jenkins.example.com/job/demo-service/42/',
                PATH        : '/usr/local/bin:/usr/bin'
            ])
            binding.setVariable('currentBuild', [
                result       : 'SUCCESS',
                currentResult: 'SUCCESS',
                description  : ''
            ])

            helper.registerAllowedMethod('echo', [String]) { String msg ->
                echoed.add(msg)
            }
            helper.registerAllowedMethod('error', [String]) { String msg ->
                throw new IllegalStateException(msg)
            }
            helper.registerAllowedMethod('isUnix', []) { -> unix }
            helper.registerAllowedMethod('pwd', [Map]) { Map args -> tmpDir }
            helper.registerAllowedMethod('dir', [String, Closure]) { String path, Closure body ->
                body.call()
            }
            helper.registerAllowedMethod('deleteDir', []) { ->
                deletedDirs.add('deleted')
            }
            helper.registerAllowedMethod('fileExists', [String]) { String path ->
                existingFiles.containsKey(path) || writtenFiles.containsKey(path)
            }
            helper.registerAllowedMethod('readFile', [String]) { String path ->
                if (existingFiles.containsKey(path)) return existingFiles[path]
                if (writtenFiles.containsKey(path)) return writtenFiles[path]
                throw new FileNotFoundException(path)
            }
            helper.registerAllowedMethod('writeFile', [Map]) { Map args ->
                writtenFiles[args.file as String] = args.text as String
            }
            helper.registerAllowedMethod('archiveArtifacts', [Map]) { Map args ->
                archived.add(new LinkedHashMap(args))
            }
            helper.registerAllowedMethod('emailext', [Map]) { Map args ->
                emails.add(new LinkedHashMap(args))
            }
            helper.registerAllowedMethod('tool', [Map]) { Map args ->
                String name = args.name as String
                return toolHomes[name] ?: "/opt/tools/${name}"
            }
            helper.registerAllowedMethod('libraryResource', [String]) { String resourcePath ->
                File file = new File('resources', resourcePath)
                if (!file.exists()) {
                    throw new FileNotFoundException("Missing library resource: ${resourcePath}")
                }
                return file.getText('UTF-8')
            }
            helper.registerAllowedMethod('withEnv', [List, Closure]) { List envList, Closure body ->
                body.call()
            }
            helper.registerAllowedMethod('node', [String, Closure]) { String label, Closure body ->
                allocatedNodes.add(label)
                body.call()
            }
            helper.registerAllowedMethod('getContext', [Object]) { Object clazz ->
                contextObjects[clazz]
            }
            helper.registerAllowedMethod('getContext', [Class]) { Class clazz ->
                contextObjects[clazz]
            }
            helper.registerAllowedMethod('sh', [String]) { String cmd ->
                shScripts.add(cmd)
                return ''
            }
            helper.registerAllowedMethod('sh', [Map]) { Map args ->
                String cmd = args.script as String
                shScripts.add(cmd)
                for (Map.Entry<String, Closure> entry : shHandlers.entrySet()) {
                    if (cmd.contains(entry.key)) {
                        return entry.value.call(args)
                    }
                }
                if (args.returnStatus) return 0
                if (args.returnStdout) {
                    if (cmd == 'uname') return 'Linux\n'
                    if (cmd == 'uname -m') return 'x86_64\n'
                    return ''
                }
                return ''
            }
        }
    }

    PipelineUnitHarness unit

    @TempDir
    Path tempDir

    def setup() {
        unit = new PipelineUnitHarness()
        unit.setUp()
    }

    private static Object roundTripSerialize(Object value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable { it.writeObject(value) }
        return new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).withCloseable { it.readObject() }
    }

    // -------------------------------------------------------------------------
    // Build-scoped steps (no Jenkins.get() controller state required)
    // -------------------------------------------------------------------------

    def "helmDeploy deploys with defaults, honors explicit false flags, supports lint and toolName, and returns serializable map"() {
        given:
        def step = unit.loadScript('vars/helmDeploy.groovy')

        when: "deployed with defaults"
        Map defaultResult = step.call(release: 'orders-api', chart: './charts/orders')

        then:
        defaultResult.status == 'deployed'
        defaultResult.atomic == true
        defaultResult.wait == true
        defaultResult.linted == false
        defaultResult.command.contains('--atomic')
        defaultResult.command.contains('--wait')
        roundTripSerialize(defaultResult) == defaultResult

        when: "deployed with explicit false flags, lint, dryRun, and toolName"
        unit.toolHomes['helm-3'] = '/opt/helm-3'
        Map customResult = step.call(
            release: 'orders-api',
            chart: './charts/orders',
            namespace: 'staging',
            atomic: false,
            wait: false,
            createNamespace: true,
            dryRun: true,
            lint: true,
            toolName: 'helm-3',
            valuesFiles: ['values-staging.yaml'],
            setValues: ['replicas': 2]
        )

        then:
        customResult.status == 'dry-run'
        customResult.atomic == false
        customResult.wait == false
        customResult.linted == true
        !customResult.command.contains('--atomic')
        !customResult.command.contains('--wait')
        customResult.command.contains('--dry-run')
        customResult.command.contains('--create-namespace')
        customResult.command.contains("'/opt/helm-3/helm'")
        unit.shScripts.any { it.contains("'/opt/helm-3/helm' lint './charts/orders'") }
        roundTripSerialize(customResult) == customResult

        when: "missing required parameters"
        step.call(release: 'orders-api')

        then:
        thrown(IllegalStateException)
    }

    def "helmDeploy setup and lint helper methods execute cleanly"() {
        given:
        def step = unit.loadScript('vars/helmDeploy.groovy')

        when:
        String binDir = step.setup('3.17.0')
        Map lintResult = step.lint(chart: './charts/orders')

        then:
        binDir.endsWith('/helm/v3.17.0/linux-amd64')
        lintResult.status == 'linted'
        roundTripSerialize(lintResult) == lintResult
    }

    def "generatePipeline renders from workspace file or inline text and writes destination"() {
        given:
        def step = unit.loadScript('vars/generatePipeline.groovy')
        unit.existingFiles['templates/basic.jenkinsfile'] = 'pipeline { agent any /* ${project} */ }'

        when:
        String fromFile = step.call(
            template: 'templates/basic.jenkinsfile',
            parameters: [project: 'billing'],
            destination: 'Jenkinsfile'
        )
        String inline = step.call(
            template: 'echo "${greeting}"',
            parameters: [greeting: 'hello']
        )

        then:
        fromFile.contains('billing')
        unit.writtenFiles['Jenkinsfile'] == fromFile
        inline == 'echo "hello"'

        when:
        step.call([:])

        then:
        thrown(IllegalStateException)
    }

    def "pipelineDSL generates Declarative Pipelines via closure and language presets"() {
        given:
        def step = unit.loadScript('vars/pipelineDSL.groovy')

        when:
        String custom = step.call {
            agent 'any'
            stages {
                stage('Build') {
                    steps {
                        sh './gradlew check'
                    }
                }
            }
        }
        String javaDsl = step.javaPipeline([deploy: false])
        String multi = step.multiBranchPipeline([buildCommand: './gradlew build', testCommand: './gradlew test'])

        then:
        custom.contains('pipeline {')
        custom.contains('./gradlew check')
        javaDsl.contains('pipeline {')
        multi.contains('Deploy to Prod')
    }

    def "exportPrometheusMetrics isolates metrics per script instance without global static state"() {
        given:
        def buildOne = unit.loadScript('vars/exportPrometheusMetrics.groovy')
        def buildTwo = unit.loadScript('vars/exportPrometheusMetrics.groovy')

        when:
        buildOne.recordBuild('service-a', 'SUCCESS', 1500L)
        buildOne.recordStage('service-a', 'Build', 500L, true)
        Map summaryOne = buildOne.summary()
        Map summaryTwo = buildTwo.summary()
        String defaultExport = buildOne.call()
        String customExport = buildOne.call(namespace: 'custom', subsystem: 'ci')

        then:
        summaryOne.builds.total == 1L
        summaryOne.builds.successful == 1L
        summaryTwo.builds.total == 0L
        defaultExport.contains('jenkins_pipeline_builds_total 1')
        customExport.contains('custom_ci_builds_total 0')
        // Custom namespace export must not overwrite buildOne's metrics
        buildOne.summary().builds.total == 1L
        roundTripSerialize(summaryOne) == summaryOne

        when:
        buildOne.reset()

        then:
        buildOne.summary().builds.total == 0L
    }

    def "provisionSpot previews by default, passes --apply when requested, and validates apply type"() {
        given:
        def step = unit.loadScript('vars/provisionSpot.groovy')
        unit.shHandlers['provision.py'] = { Map args ->
            String cmd = args.script as String
            if (cmd.contains('--apply')) {
                return '{"provider":"aws","target":"us-east-1/req-1","status":"submitted","resourceId":"i-123456"}'
            }
            return '{"provider":"aws","target":"us-east-1/req-1","status":"planned"}'
        }

        when:
        Map planned = step.call(provider: 'aws', region: 'us-east-1', requestId: 'req-1')
        Map applied = step.call(provider: 'aws', region: 'us-east-1', requestId: 'req-1', apply: true)

        then:
        planned == [provider: 'aws', target: 'us-east-1/req-1', status: 'planned']
        applied == [provider: 'aws', target: 'us-east-1/req-1', status: 'submitted', resourceId: 'i-123456']
        unit.writtenFiles.containsKey('provision.py')
        unit.writtenFiles.containsKey('spot_pools.json')
        unit.deletedDirs.size() == 2
        roundTripSerialize(applied) == applied

        when:
        step.call(provider: 'aws', apply: 'yes')

        then:
        thrown(IllegalStateException)
    }

    // -------------------------------------------------------------------------
    // Controller-backed steps (Jenkins.get() mocked cleanly per test)
    // -------------------------------------------------------------------------

    def "withSpotAgent validates provider and runs body on verified candidate agent"() {
        given:
        def step = unit.loadScript('vars/withSpotAgent.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        Node agent = Mockito.mock(Node)
        Computer computer = Mockito.mock(Computer)
        hudson.model.labels.LabelAtom spotAtom = new hudson.model.labels.LabelAtom('spot')
        Mockito.when(agent.nodeName).thenReturn('spot-agent-1')
        Mockito.when(agent.numExecutors).thenReturn(2)
        Mockito.when(agent.toComputer()).thenReturn(computer)
        Mockito.when(agent.assignedLabels).thenReturn([spotAtom] as Set)
        Mockito.when(computer.name).thenReturn('spot-agent-1')
        Mockito.when(computer.online).thenReturn(true)
        Mockito.when(computer.acceptingTasks).thenReturn(true)
        Mockito.when(jenkins.nodes).thenReturn([agent])
        Mockito.when(jenkins.getLabelAtom('spot')).thenReturn(spotAtom)
        unit.contextObjects[Computer] = computer
        unit.shHandlers['verify_spot.py'] = { Map args -> 0 }

        when: "invalid provider"
        step.call(provider: 'invalid') { 'never' }

        then:
        thrown(IllegalStateException)

        when: "kubernetes without pool"
        step.call(provider: 'kubernetes') { 'never' }

        then:
        thrown(IllegalStateException)

        when: "verified aws spot agent runs closure"
        Object bodyResult
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            mocked.when { Jenkins.getInstanceOrNull() }.thenReturn(jenkins)
            bodyResult = step.call(provider: 'aws', label: 'spot') { 'ok-from-spot' }
        } finally {
            mocked.close()
        }

        then:
        bodyResult == 'ok-from-spot'
        unit.allocatedNodes == ['spot-agent-1']
    }

    def "auditPlugins inspects installed plugins and detects drift"() {
        given:
        def step = unit.loadScript('vars/auditPlugins.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        PluginManager pm = Mockito.mock(PluginManager)
        PluginWrapper gitPlugin = Mockito.mock(PluginWrapper)
        Mockito.when(gitPlugin.shortName).thenReturn('git')
        Mockito.when(gitPlugin.version).thenReturn('5.11.0')
        Mockito.when(gitPlugin.isActive()).thenReturn(true)
        Mockito.when(gitPlugin.isEnabled()).thenReturn(true)
        Mockito.when(pm.plugins).thenReturn([gitPlugin])
        Mockito.when(jenkins.pluginManager).thenReturn(pm)

        when:
        Map report
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            report = step.call(expected: 'git:5.11.0\nmissing-plugin:1.0\n')
        } finally {
            mocked.close()
        }

        then:
        report.drift == true
        report.missing == ['missing-plugin']
        roundTripSerialize(report) == report
    }

    def "auditSecurity respects archive:false, archives HTML by default, sends emailReport, and fails on critical"() {
        given:
        def step = unit.loadScript('vars/auditSecurity.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        Mockito.when(jenkins.isUseSecurity()).thenReturn(false)
        Mockito.when(jenkins.getSecurityRealm()).thenReturn(SecurityRealm.NO_AUTHENTICATION)
        Mockito.when(jenkins.getAuthorizationStrategy()).thenReturn(AuthorizationStrategy.UNSECURED)
        Mockito.when(jenkins.getCrumbIssuer()).thenReturn(null)
        Mockito.when(jenkins.getAgentProtocols()).thenReturn(['JNLP4-connect'] as Set)
        Mockito.when(jenkins.getNodes()).thenReturn([])
        Mockito.when(jenkins.getPluginManager()).thenReturn(Mockito.mock(PluginManager))

        when: "archive is explicitly false and failOnCritical is false"
        Map noArchiveResult
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        MockedStatic<hudson.model.User> mockedUsers = Mockito.mockStatic(hudson.model.User)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            mockedUsers.when { hudson.model.User.getAll() }.thenReturn([])
            noArchiveResult = step.call(archive: false, emailReport: 'sec@example.com')
        } finally {
            mockedUsers.close()
            mocked.close()
        }

        then:
        unit.archived.isEmpty()
        !unit.writtenFiles.containsKey('security-audit-report.html')
        unit.emails.size() == 1
        unit.emails[0].to == 'sec@example.com'
        noArchiveResult.summary.critical > 0
        roundTripSerialize(noArchiveResult) == noArchiveResult

        when: "default archive:true runs and failOnCritical:true triggers error"
        MockedStatic<Jenkins> mocked2 = Mockito.mockStatic(Jenkins)
        MockedStatic<hudson.model.User> mockedUsers2 = Mockito.mockStatic(hudson.model.User)
        try {
            mocked2.when { Jenkins.get() }.thenReturn(jenkins)
            mockedUsers2.when { hudson.model.User.getAll() }.thenReturn([])
            step.call(failOnCritical: true)
        } finally {
            mockedUsers2.close()
            mocked2.close()
        }

        then:
        thrown(IllegalStateException)
        unit.writtenFiles.containsKey('security-audit-report.html')
        unit.archived.size() == 1
    }

    def "backupJenkinsConfig creates backups, preserves explicit false flags, and previews selective restore"() {
        given:
        def step = unit.loadScript('vars/backupJenkinsConfig.groovy')
        File jenkinsHome = tempDir.resolve('jenkins_home').toFile()
        jenkinsHome.mkdirs()
        new File(jenkinsHome, 'config.xml').text = '<hudson/>'
        File backupDir = tempDir.resolve('backups').toFile()

        Jenkins jenkins = Mockito.mock(Jenkins)
        Mockito.when(jenkins.rootDir).thenReturn(jenkinsHome)
        Mockito.when(jenkins.getRootDir()).thenReturn(jenkinsHome)
        Mockito.when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true)

        when: "both destination and destinationDir are provided"
        step.call(destination: '/a.zip', destinationDir: '/b')

        then:
        thrown(IllegalStateException)

        when: "restore called without backupFile"
        step.restore([:])

        then:
        thrown(IllegalStateException)

        when: "uncompressed backup and dry-run restore preview execute"
        String backupPath
        List previewEntries
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            backupPath = step.call(
                destinationDir: backupDir.absolutePath,
                includeJobs: false,
                includePlugins: false,
                includeNodes: false,
                compress: false,
                archive: false
            )
            previewEntries = step.restore(backupFile: backupPath, files: ['config.xml'], dryRun: true)
        } finally {
            mocked.close()
        }

        then:
        new File(backupPath).isDirectory()
        previewEntries == ['config.xml']
        roundTripSerialize(previewEntries) == previewEntries
    }

    private static void setCloudsField(Jenkins jenkins, hudson.model.Hudson.CloudList cloudList) {
        def field = Jenkins.class.getDeclaredField('clouds')
        field.accessible = true
        field.set(jenkins, cloudList)
    }

    def "cloudInventory returns plain serializable clouds and agents map"() {
        given:
        def step = unit.loadScript('vars/cloudInventory.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        hudson.model.Hudson.CloudList cloudList = Mockito.mock(hudson.model.Hudson.CloudList)
        Mockito.when(cloudList.iterator()).thenAnswer { [].iterator() }
        setCloudsField(jenkins, cloudList)
        Mockito.when(jenkins.nodes).thenReturn([])

        when:
        Map inventory
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            inventory = step.call()
        } finally {
            mocked.close()
        }

        then:
        inventory.clouds == []
        inventory.agents == []
        roundTripSerialize(inventory) == inventory
    }

    def "disableJobs validates arguments, disables matching jobs, and reports missing folder"() {
        given:
        def step = unit.loadScript('vars/disableJobs.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        AbstractProject job = Mockito.mock(AbstractProject)
        Mockito.when(job.name).thenReturn('deprecated-demo')
        Mockito.when(job.fullName).thenReturn('deprecated-demo')
        Mockito.when(job.isDisabled()).thenReturn(false)
        Mockito.when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true)
        Mockito.when(jenkins.getItemByFullName('deprecated-demo')).thenReturn(job)
        Mockito.when(jenkins.getAllItems(hudson.model.Job)).thenReturn([job])
        Mockito.when(jenkins.getAllItems()).thenReturn([job])
        Mockito.when(jenkins.getItemByFullName('missing-folder')).thenReturn(null)

        when: "no target supplied"
        step.call([:])

        then:
        thrown(IllegalStateException)

        when: "pattern dryRun executes"
        List results
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            results = step.call(pattern: 'deprecated-.*', dryRun: true)
        } finally {
            mocked.close()
        }

        then:
        results == [[job: 'deprecated-demo', success: true]]
        roundTripSerialize(results) == results

        when: "missing folderPath triggers error"
        MockedStatic<Jenkins> mocked2 = Mockito.mockStatic(Jenkins)
        try {
            mocked2.when { Jenkins.get() }.thenReturn(jenkins)
            step.call(folderPath: 'missing-folder')
        } finally {
            mocked2.close()
        }

        then:
        thrown(IllegalStateException)
    }

    def "analyzePipelineMetrics analyzes builds, respects includeRecommendations:false, and validates jobName"() {
        given:
        def step = unit.loadScript('vars/analyzePipelineMetrics.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        WorkflowJob job = Mockito.mock(WorkflowJob)
        WorkflowRun build = Mockito.mock(WorkflowRun)
        Mockito.when(build.number).thenReturn(1)
        Mockito.when(build.result).thenReturn(Result.SUCCESS)
        Mockito.when(build.duration).thenReturn(2000L)
        Mockito.when(build.timeInMillis).thenReturn(System.currentTimeMillis() - 5000L)
        Mockito.when(build.isBuilding()).thenReturn(false)
        Mockito.when(build.getCauses()).thenReturn([])
        Mockito.when(job.fullName).thenReturn('platform/demo-service')
        Mockito.when(job.builds).thenReturn(hudson.util.RunList.fromRuns([build]))
        Mockito.when(jenkins.getItemByFullName('platform/demo-service', WorkflowJob)).thenReturn(job)

        when:
        Map metrics
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            metrics = step.call(historyLimit: 10, includeRecommendations: false)
        } finally {
            mocked.close()
        }

        then:
        metrics.summary.totalBuilds == 1
        metrics.summary.successRate == 100.0
        !unit.echoed.any { it.contains('Top recommendations') }
        roundTripSerialize(metrics) == metrics

        when: "no jobName and empty env.JOB_NAME"
        unit.binding.getVariable('env').JOB_NAME = ''
        step.call([:])

        then:
        thrown(IllegalStateException)
    }

    def "optimizeCloudCosts handles analyze, optimize, allocate, budget, report, and unknown action"() {
        given:
        def step = unit.loadScript('vars/optimizeCloudCosts.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        hudson.model.Hudson.CloudList cloudList = Mockito.mock(hudson.model.Hudson.CloudList)
        Mockito.when(cloudList.iterator()).thenAnswer { [].iterator() }
        Mockito.when(jenkins.nodes).thenReturn([])
        Mockito.when(jenkins.computers).thenReturn([] as Computer[])
        setCloudsField(jenkins, cloudList)
        Mockito.when(jenkins.getAllItems()).thenReturn([])

        when:
        Map analysis
        Map plan
        Map applied
        Map allocations
        Map budget
        String report
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            analysis = step.analyze()
            plan = step.optimize(dryRun: true)
            applied = step.optimize(dryRun: false)
            allocations = step.getAllocations()
            budget = step.checkBudgets(['total': 1000.0])
            report = step.generateReport()
        } finally {
            mocked.close()
        }

        then:
        analysis.totalCost != null
        plan.actions != null
        applied.counts.applied == 0
        unit.binding.getVariable('currentBuild').description.contains('Cloud actions:')
        allocations != null
        budget.alerts != null
        report.contains('Jenkins Cloud Cost Report')
        roundTripSerialize(analysis) == analysis
        roundTripSerialize(plan) == plan

        when:
        step.call(action: 'unknown')

        then:
        thrown(IllegalStateException)
    }

    def "rotateCredentials handles analyze, rotate, findUnused, checkCompliance, and unknown action"() {
        given:
        def step = unit.loadScript('vars/rotateCredentials.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        hudson.ExtensionList extList = Mockito.mock(hudson.ExtensionList)
        Mockito.when(extList.iterator()).thenAnswer { [].iterator() }
        Mockito.when(jenkins.getRootDir()).thenReturn(tempDir.toFile())
        Mockito.when(jenkins.getAllItems()).thenReturn([])
        Mockito.when(jenkins.getExtensionList(com.cloudbees.plugins.credentials.CredentialsProvider)).thenReturn(extList)

        when:
        Map analysis
        Map rotation
        List unused
        Map compliance
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.get() }.thenReturn(jenkins)
            analysis = step.analyze(includeUsage: false)
            rotation = step.rotate(dryRun: true, policy: [PASSWORD: 60])
            unused = step.findUnused(days: 30)
            compliance = step.checkCompliance()
        } finally {
            mocked.close()
        }

        then:
        analysis.summary.totalCredentials == 0
        rotation.rotated == []
        unused == []
        compliance.compliant == true
        roundTripSerialize(analysis) == analysis
        roundTripSerialize(rotation) == rotation
        roundTripSerialize(compliance) == compliance

        when:
        step.call(action: 'bogus')

        then:
        thrown(IllegalStateException)
    }

    def "selfHealingAgents runs single-pass health check without background threads and returns serializable results"() {
        given:
        def step = unit.loadScript('vars/selfHealingAgents.groovy')
        Jenkins jenkins = Mockito.mock(Jenkins)
        Computer offlineAgent = Mock(Computer)
        offlineAgent.name >> 'linux-agent-1'
        offlineAgent.isOffline() >> true
        offlineAgent.isOnline() >> false
        offlineAgent.offlineCause >> null
        Mockito.when(jenkins.toComputer()).thenReturn(null)
        Mockito.when(jenkins.computers).thenReturn([offlineAgent] as Computer[])

        when:
        Map healResult
        Map statsResult
        List statuses
        MockedStatic<Jenkins> mocked = Mockito.mockStatic(Jenkins)
        try {
            mocked.when { Jenkins.getInstanceOrNull() }.thenReturn(jenkins)
            healResult = step.call(enableAutoReconnect: true, enableAutoRestart: false)
            statsResult = step.statistics()
            statuses = step.agentStatuses()
        } finally {
            mocked.close()
        }

        then:
        1 * offlineAgent.connect(true)
        healResult.totalAttempts == 1
        healResult.agentStatuses.size() == 1
        statsResult.totalAttempts == 0
        statuses.size() == 1
        statuses[0].agentName == 'linux-agent-1'
        roundTripSerialize(healResult) == healResult
        roundTripSerialize(statuses) == statuses
    }
}
