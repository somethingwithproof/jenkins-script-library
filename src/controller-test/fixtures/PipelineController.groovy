// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.Converter
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import org.jenkinsci.plugins.workflow.support.steps.input.InputAction

void beforeRestart() {
    def library = new LibraryConfiguration('jenkins-script-library', new SCMSourceRetriever(new GitSCMSource(libraryRepo)))
    library.defaultVersion = 'main'
    GlobalLibraries.get().libraries = [library]

    ['Jenkinsfile', 'templates/basic-pipeline.jenkinsfile', 'examples/advanced-pipeline.jenkinsfile'].each { name ->
        assert Converter.scriptToPipelineDef(new File(project, name).getText('UTF-8')) != null : name
    }

    // This is valid Groovy but invalid Declarative syntax.
    try {
        Converter.scriptToPipelineDef('pipeline { agent any; unsupportedDirective(); stages {} }')
        throw new AssertionError('The Declarative validator accepted an invalid directive')
    } catch (org.codehaus.groovy.control.MultipleCompilationErrorsException expected) {
        assert expected.message.contains('unsupportedDirective')
    }

    rule.jenkins.numExecutors = 0
    rule.createOnlineSlave()
    def environment = new hudson.slaves.EnvironmentVariablesNodeProperty()
    environment.envVars.put('BACKUP_ROOT', new File(rule.jenkins.rootDir, 'backups').canonicalPath)
    rule.jenkins.globalNodeProperties.add(environment)
    def job = rule.jenkins.createProject(WorkflowJob, 'operations')
    job.definition = new CpsFlowDefinition('''
        @Library('jenkins-script-library') _
        def report
        node {
            report = auditPlugins(expected: 'absent-fixture:latest')
            assert report.installed.any { it.id == 'workflow-job' }
            def zip = backupJenkinsConfig(destination: env.BACKUP_ROOT + '/named.zip',
                includeJobs: false, includeNodes: false, includePlugins: false, archive: true)
            assert zip == env.BACKUP_ROOT + '/named.zip'
            def tree = backupJenkinsConfig(destination: env.BACKUP_ROOT + '/named-tree', compress: false,
                includeJobs: false, includeNodes: false, includePlugins: false, archive: true)
            assert tree == env.BACKUP_ROOT + '/named-tree'
            for (def archive : [zip, tree]) {
                assert backupJenkinsConfig.restore(backupFile: archive, files: ['config.xml'], dryRun: true) == ['config.xml']
            }
            assert !fileExists('named.zip')
            assert !fileExists('named-tree/config.xml')
        }
        input id: 'Resume', message: 'Continue after controller restart'
        assert report.missing == ['absent-fixture']
        assert report.installed.any { it.id == 'workflow-job' }
        echo 'Inventory survived restart'
    ''', true)
    def run = job.scheduleBuild2(0).waitForStart()
    rule.waitForMessage('Proceed or Abort', run)
    def artifacts = run.artifactManager.root()
    assert artifacts.child('named.zip').length() > 0
    assert artifacts.child('named-tree/config.xml').length() > 0
}

void afterRestart() {
    def run = rule.jenkins.getItemByFullName('operations', WorkflowJob).getBuildByNumber(1)
    assert run.isBuilding()
    run.getAction(InputAction).getExecution('Resume').proceed(null)
    rule.assertBuildStatusSuccess(rule.waitForCompletion(run))
    rule.assertLogContains('Inventory survived restart', run)
}
