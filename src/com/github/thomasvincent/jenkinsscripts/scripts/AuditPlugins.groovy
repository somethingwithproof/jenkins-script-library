#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.scripts

import com.github.thomasvincent.jenkinsscripts.config.PluginInventory
import groovy.cli.commons.CliBuilder
import groovy.json.JsonOutput
import hudson.AbortException
import jenkins.model.Jenkins
import java.nio.file.Files
import java.nio.file.Paths

def cli = new CliBuilder(usage: 'AuditPlugins.groovy [--expected plugins.txt] [--format text|json]')
cli.h(longOpt: 'help', 'Show usage')
cli.e(longOpt: 'expected', args: 1, 'Manifest path on the controller')
cli.f(longOpt: 'format', args: 1, 'Output format (text or json)')
def options = cli.parse(args)
if (!options) {
    throw new AbortException('Invalid arguments')
}
if (options.h) {
    cli.usage()
    return
}
String format = options.f ?: 'text'
if (!(format in ['text', 'json'])) {
    throw new AbortException('Expected text or json')
}
def controller = Jenkins.get()
String manifest = null
if (options.e) {
    controller.checkPermission(Jenkins.ADMINISTER)
    manifest = Files.readString(Paths.get(options.e as String))
}
def report = PluginInventory.inspect(controller, manifest)
if (format == 'json') {
    println JsonOutput.toJson(report)
} else {
    println 'PLUGIN\tVERSION\tENABLED\tPINNED\tUPDATE AVAILABLE'
    report.installed.each { plugin -> println([plugin.id, plugin.version, plugin.enabled, plugin.pinned, plugin.updateAvailable].join('\t')) }
    report.missing.each { id -> println "Missing: ${id}" }
    report.extra.each { id -> println "Extra: ${id}" }
    report.mismatched.each { plugin -> println "Version mismatch: ${plugin.id} (expected ${plugin.expected}, installed ${plugin.installed})" }
}
if (report.drift) {
    throw new AbortException('Plugin manifest differs from the installed inventory')
}
