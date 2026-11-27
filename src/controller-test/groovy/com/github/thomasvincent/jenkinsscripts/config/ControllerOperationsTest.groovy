// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config

import com.github.thomasvincent.jenkinsscripts.jobs.JobMigrator
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.jvnet.hudson.test.JenkinsRule
import groovy.json.JsonSlurper
import hudson.AbortException
import static org.junit.Assert.*

class ControllerOperationsTest {
    @Rule public JenkinsRule controller = new JenkinsRule()
    @Rule public TemporaryFolder backups = new TemporaryFolder()

    private Object script(String file, List<String> arguments, StringWriter output) {
        def binding = new Binding(args: arguments as String[], out: new PrintWriter(output, true))
        new GroovyShell(getClass().classLoader, binding).evaluate(new File(file))
    }

    @Test void runPublicInventoryAndMigrationEntryPoints() {
        String scripts = 'src/com/github/thomasvincent/jenkinsscripts/scripts'
        def output = new StringWriter()
        script("${scripts}/AuditPlugins.groovy", ['--format', 'json'], output)
        assertFalse(new JsonSlurper().parseText(output.toString()).drift)
        File expected = backups.newFile('plugins.txt')
        expected.text = 'absent-plugin:latest'
        output = new StringWriter()
        assertThrows(AbortException) {
            script("${scripts}/AuditPlugins.groovy", ['--expected', expected.path, '--format', 'json'], output)
        }
        assertEquals(['absent-plugin'], new JsonSlurper().parseText(output.toString()).missing)
        // Resolve the production class through Jenkins' shared-library src/ layout.
        def sourceRoot = new File('src').toURI().toURL()
        new GroovyClassLoader(getClass().classLoader).withCloseable { loader ->
            loader.addURL(sourceRoot)
            def source = loader.getResourceLoader().loadGroovySource(PluginInventory.name)
            assertEquals(new File('src/' + PluginInventory.name.replace('.', '/') + '.groovy').canonicalFile,
                new File(source.toURI()).canonicalFile)
            loader.parseClass(new File(source.toURI()))
            def step = new GroovyShell(loader).parse(new File('vars/auditPlugins.groovy'))
            assertEquals(['absent-plugin'], step.call([expected: expected.text]).missing)
        }
        assertThrows(AbortException) {
            script("${scripts}/MigrateJobs.groovy", ['--url', 'https://unused.invalid/', '--job', 'example'], new StringWriter())
        }
    }

    @Test void inspectAndRecoverControllerConfiguration() {
        def jenkins = controller.jenkins
        assertNull(jenkins.pluginManager.getPlugin('google-compute-engine'))
        assertTrue(new com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory().inspect(jenkins).clouds.empty)
        def report = PluginInventory.inspect(jenkins, 'example-uninstalled:latest')
        assertTrue(report.drift)
        assertEquals(['example-uninstalled'], report.missing)
        assertFalse(PluginInventory.inspect(jenkins).drift)
        assertThrows(IllegalArgumentException) { new JobMigrator(jenkins, jenkins) }

        File config = new File(jenkins.rootDir, 'ops-example.xml')
        File other = new File(jenkins.rootDir, 'ops-other.xml')
        def tool = new JenkinsConfigBackup(jenkins).withConfigFiles([config.name, other.name])
        [true, false].each { zip ->
            config.text = '<saved/>'
            other.text = '<other/>'
            String archive = tool.withCompression(zip).createBackup(backups.newFolder().path)
            config.text = '<edited/>'
            other.text = '<unchanged/>'
            def preview = tool.restore(archive, [files: [config.name], dryRun: true])
            assertEquals([config.name], preview.entries)
            assertEquals('<edited/>', config.text)
            def listed = new StringWriter()
            script('src/com/github/thomasvincent/jenkinsscripts/scripts/BackupJenkinsConfig.groovy',
                ['--restore', archive, '--list-contents', '--files', config.name], listed)
            assertEquals(config.name, listed.toString().trim())
            def applied = tool.restore(archive, [files: [config.name]])
            assertEquals('<saved/>', config.text)
            assertEquals('<unchanged/>', other.text)
            assertEquals('<edited/>', new File(applied.recoveryDirectory, config.name + '.pre-restore').text)
            assertNull(tool.restore(archive, [files: [config.name]]).recoveryDirectory)
        }
    }
}
