// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.config.PluginInventory
import jenkins.model.Jenkins

/** Accept manifest text, e.g. auditPlugins(expected: readFile('plugins.txt')). */
def call(Map args = [:]) {
    return inventory(args.expected as String)
}

@NonCPS
private Map inventory(String expected) {
    return PluginInventory.inspect(Jenkins.get(), expected)
}
