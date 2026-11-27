// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package com.github.thomasvincent.jenkinsscripts.helm

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(HelmHelper)
class HelmHelperSpec extends Specification {

    static class StubPipeline implements Serializable {
        private static final long serialVersionUID = 1L
        boolean unix = true
        String osOutput = 'Linux'
        String archOutput = 'x86_64'
        String tmpDir = '/tmp/agent-work'
        Map<String, String> env = [PATH: '/usr/bin']
        List<String> shCommands = []
        List<String> psCommands = []
        List<String> echoes = []

        boolean isUnix() { unix }

        String pwd(Map args = [:]) { tmpDir }

        String sh(Map args) {
            String script = args.script as String
            if (args.returnStdout) {
                if (script == 'uname') return osOutput
                if (script == 'uname -m') return archOutput
            }
            shCommands.add(script)
            return ''
        }

        String powershell(Map args) {
            String script = args.script as String
            if (args.returnStdout) {
                return '9'
            }
            psCommands.add(script)
            return ''
        }

        void echo(String msg) {
            echoes.add(msg)
        }

        void error(String msg) {
            throw new IllegalStateException(msg)
        }
    }

    @Unroll
    def "normalizeVersion accepts '#input' and returns '#expected'"() {
        given:
        def helper = new HelmHelper(new StubPipeline())

        expect:
        helper.normalizeVersion(input) == expected

        where:
        input           | expected
        '3.17.0'        | 'v3.17.0'
        'v3.17.0'       | 'v3.17.0'
        '3.14.2-rc.1'   | 'v3.14.2-rc.1'
    }

    @Unroll
    def "normalizeVersion rejects unsafe version '#badVersion'"() {
        given:
        def helper = new HelmHelper(new StubPipeline())

        when:
        helper.normalizeVersion(badVersion)

        then:
        thrown(IllegalArgumentException)

        where:
        badVersion << ['', '   ', '3.17.0; rm -rf /', '$(whoami)', 'latest']
    }

    def "use installs Helm into agent tmp directory and updates PATH on Unix"() {
        given:
        def pipeline = new StubPipeline(unix: true, osOutput: 'Linux', archOutput: 'x86_64', tmpDir: '/var/tmp/build')
        def helper = new HelmHelper(pipeline)

        when:
        String binDir = helper.use('3.17.0')

        then:
        binDir == '/var/tmp/build/helm/v3.17.0/linux-amd64'
        pipeline.env.PATH == '/var/tmp/build/helm/v3.17.0/linux-amd64:/usr/bin'
        pipeline.shCommands.size() == 1
        pipeline.shCommands[0].contains('https://get.helm.sh/helm-v3.17.0-linux-amd64.tar.gz')
        pipeline.echoes.any { it.contains('Using Helm v3.17.0') }
    }

    def "buildDeployCommand applies defaults and preserves explicit false flags"() {
        given:
        def helper = new HelmHelper(new StubPipeline())

        when:
        String defaultCmd = helper.buildDeployCommand(release: 'demo-app', chart: './charts/demo')
        String explicitFalseCmd = helper.buildDeployCommand(
            release: 'demo-app',
            chart: './charts/demo',
            namespace: 'prod',
            atomic: false,
            wait: false,
            createNamespace: true,
            dryRun: true,
            timeout: '10m',
            valuesFiles: ['values.yaml', 'values-prod.yaml'],
            setValues: ['image.tag': "1.0.0'beta"]
        )

        then:
        defaultCmd.contains("'helm' upgrade --install 'demo-app' './charts/demo'")
        defaultCmd.contains('--atomic')
        defaultCmd.contains('--wait')
        !defaultCmd.contains('--dry-run')
        !defaultCmd.contains('--create-namespace')

        explicitFalseCmd.contains("--namespace 'prod'")
        explicitFalseCmd.contains("--timeout '10m'")
        explicitFalseCmd.contains('--create-namespace')
        explicitFalseCmd.contains('--dry-run')
        !explicitFalseCmd.contains('--atomic')
        !explicitFalseCmd.contains('--wait')
        explicitFalseCmd.contains("-f 'values.yaml' -f 'values-prod.yaml'")
        explicitFalseCmd.contains("--set 'image.tag=1.0.0'\"'\"'beta'")
    }

    @Unroll
    def "buildDeployCommand rejects invalid inputs (#label)"() {
        given:
        def helper = new HelmHelper(new StubPipeline())

        when:
        helper.buildDeployCommand(params)

        then:
        thrown(IllegalArgumentException)

        where:
        label               | params
        'missing release'   | [chart: './chart']
        'unsafe release'    | [release: 'bad;cmd', chart: './chart']
        'missing chart'     | [release: 'demo']
        'unsafe namespace'  | [release: 'demo', chart: './chart', namespace: 'BAD_NS']
        'unsafe timeout'    | [release: 'demo', chart: './chart', timeout: '5m;id']
    }

    def "HelmHelper survives Java serialization for Jenkins CPS safety"() {
        given:
        def helper = new HelmHelper(new StubPipeline())
        def baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable { it.writeObject(helper) }

        when:
        HelmHelper restored = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).withCloseable {
            it.readObject() as HelmHelper
        }
        Map result = restored.deploy(release: 'web', chart: './chart', dryRun: true)

        then:
        result.status == 'dry-run'
        result.release == 'web'
    }
}
