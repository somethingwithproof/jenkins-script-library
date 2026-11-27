// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config

import hudson.PluginManager
import hudson.PluginWrapper
import jenkins.model.Jenkins
import spock.lang.Specification

class PluginInventorySpec extends Specification {
    def 'compare pins, floating entries, extra and missing plugins in stable order'() {
        given:
        def git = Stub(PluginWrapper) {
            getShortName() >> 'git'
            getVersion() >> '1.0'
            isEnabled() >> true
            isPinned() >> true
            hasUpdate() >> true
        }
        def mailer = Stub(PluginWrapper) { getShortName() >> 'mailer'; getVersion() >> '2.0' }
        def extra = Stub(PluginWrapper) { getShortName() >> 'extra'; getVersion() >> '3.0' }
        def manager = Stub(PluginManager) { getPlugins() >> [mailer, git, extra] }
        def controller = Mock(Jenkins)
        controller.getPluginManager() >> manager

        when:
        def result = PluginInventory.inspect(controller, 'git:2.0\nmailer:latest\nmailer:latest # duplicate\nmissing')

        then:
        1 * controller.checkPermission(Jenkins.SYSTEM_READ)
        result.installed*.id == ['extra', 'git', 'mailer']
        result.installed[1] == [id: 'git', version: '1.0', enabled: true, pinned: true, updateAvailable: true]
        result.missing == ['missing']
        result.extra == ['extra']
        result.mismatched == [[id: 'git', expected: '2.0', installed: '1.0']]
        result.drift

        when:
        PluginInventory.inspect(controller, 'git:1.0\ngit:2.0')

        then:
        thrown(IllegalArgumentException)
    }
}
