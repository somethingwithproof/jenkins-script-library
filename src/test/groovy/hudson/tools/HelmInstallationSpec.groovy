// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

package hudson.tools

import hudson.EnvVars
import spock.lang.Specification
import spock.lang.Subject

@Subject(HelmInstallation)
class HelmInstallationSpec extends Specification {

    def "DescriptorImpl inherits ToolDescriptor getInstallations and setInstallations without infinite recursion"() {
        given:
        def getMethod = HelmInstallation.DescriptorImpl.getMethod('getInstallations')
        def setMethod = HelmInstallation.DescriptorImpl.getMethod('setInstallations', ToolInstallation[].class)

        expect:
        getMethod.declaringClass == ToolDescriptor
        setMethod.declaringClass == ToolDescriptor
    }

    def "HelmInstallation expands home directory for environment"() {
        given:
        def installation = new HelmInstallation('helm-3', '${TOOLS_DIR}/helm', [])
        def env = new EnvVars(['TOOLS_DIR': '/opt/tools'])

        when:
        def expanded = installation.forEnvironment(env)

        then:
        expanded.name == 'helm-3'
        expanded.home == '/opt/tools/helm'
    }
}
