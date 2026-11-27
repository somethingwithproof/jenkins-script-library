// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config

import com.cloudbees.groovy.cps.NonCPS

import jenkins.model.Jenkins

/** Read-only inventory and comparison with a Jenkins plugins.txt manifest. */
class PluginInventory {

    private static final String ANY_VERSION = 'latest'

    @NonCPS
    static Map inspect(Jenkins controller, String manifest = null) {
        controller.checkPermission(Jenkins.SYSTEM_READ)
        def installed = controller.pluginManager.plugins.collect { plugin ->
            [id: plugin.shortName, version: plugin.version, enabled: plugin.enabled,
             pinned: plugin.pinned, updateAvailable: plugin.hasUpdate()]
        }.sort { plugin -> plugin.id }
        def expected = [:]
        manifest?.readLines()?.eachWithIndex { line, index ->
            String entry = line.split('#', 2)[0].trim()
            if (!entry) {
                return
            }
            def match = entry =~ /^([A-Za-z0-9_.-]+)(?::([^:\s]+))?$/
            if (!match.matches()) {
                throw new IllegalArgumentException("Invalid plugin entry at line ${index + 1}")
            }
            String id = match.group(1)
            String version = match.group(2) ?: ANY_VERSION
            if (expected.containsKey(id) && expected[id] != version) {
                throw new IllegalArgumentException("Conflicting versions for plugin ${id}")
            }
            expected[id] = version
        }
        def actual = installed.collectEntries { plugin -> [(plugin.id): plugin.version] }
        def missing = expected.keySet().findAll { id -> !actual.containsKey(id) }.sort()
        def extra = manifest == null ? [] : actual.keySet().findAll { id -> !expected.containsKey(id) }.sort()
        def mismatched = expected.keySet().sort().findResults { id ->
            actual.containsKey(id) && expected[id] != ANY_VERSION && expected[id] != actual[id] ?
                [id: id, expected: expected[id], installed: actual[id]] : null
        }
        return [installed: installed, missing: missing, extra: extra, mismatched: mismatched,
         drift: !(missing.empty && extra.empty && mismatched.empty)]
    }

}
