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

package com.github.thomasvincent.jenkinsscripts.vars

import spock.lang.Specification
import spock.lang.Unroll

class VarsScriptsValidationSpec extends Specification {

    private static final File VARS_DIR = new File("vars")
    private static final File ADMIN_SCRIPTS_DIR = new File("admin-scripts")

    @Unroll
    def "vars script #scriptFile.name compiles cleanly without metaClass mutation or JenkinsLogger"() {
        given:
        String source = scriptFile.getText("UTF-8")
        GroovyShell shell = new GroovyShell()

        when:
        Script parsed = shell.parse(source, scriptFile.name)

        then:
        parsed != null
        !source.contains(".metaClass.")
        !source.contains("JenkinsLogger")

        where:
        scriptFile << (VARS_DIR.listFiles()?.findAll { it.name.endsWith(".groovy") }?.sort { it.name } ?: [])
    }

    @Unroll
    def "vars script #scriptFile.name has a matching non-empty Global Variables Reference .txt help file"() {
        given:
        String baseName = scriptFile.name.replaceFirst(/\.groovy$/, '')
        File helpFile = new File(VARS_DIR, "${baseName}.txt")

        expect:
        helpFile.exists()
        helpFile.isFile()
        !helpFile.getText("UTF-8").trim().isEmpty()

        where:
        scriptFile << (VARS_DIR.listFiles()?.findAll { it.name.endsWith(".groovy") }?.sort { it.name } ?: [])
    }

    @Unroll
    def "controller admin script #scriptFile.name compiles cleanly"() {
        given:
        String source = scriptFile.getText("UTF-8")
        GroovyShell shell = new GroovyShell()

        when:
        Script parsed = shell.parse(source, scriptFile.name)

        then:
        parsed != null

        where:
        scriptFile << (ADMIN_SCRIPTS_DIR.listFiles()?.findAll { it.name.endsWith(".groovy") }?.sort { it.name } ?: [])
    }

    def "auditSecurity sendSecurityReport is not annotated with @NonCPS"() {
        given:
        String source = new File(VARS_DIR, "auditSecurity.groovy").getText("UTF-8")

        expect:
        !(source =~ /@NonCPS\s+private\s+void\s+sendSecurityReport/)
    }

    def "selfHealingAgents runs single-pass without global static manager state or CPS echo callbacks"() {
        given:
        String source = new File(VARS_DIR, "selfHealingAgents.groovy").getText("UTF-8")

        expect:
        !(source =~ /onAgentRecovery\s*=\s*\{[^}]*\becho\b/)
        !(source =~ /onAgentFailure\s*=\s*\{[^}]*\becho\b/)
        !source.contains("MANAGER_REF")
        !source.contains("ScheduledExecutorService")
        source.contains("singlePass")
    }

    def "exportPrometheusMetrics keeps metrics state instance-scoped instead of global static fields"() {
        given:
        String source = new File(VARS_DIR, "exportPrometheusMetrics.groovy").getText("UTF-8")

        expect:
        !source.contains("static final PrometheusMetricsExporter")
        !source.contains("SCOPED_EXPORTERS")
    }

    def "vars scripts preserve explicit false boolean flags instead of using Elvis true fallback"() {
        given:
        List<String> files = [
            "auditSecurity.groovy",
            "backupJenkinsConfig.groovy",
            "helmDeploy.groovy",
            "optimizeCloudCosts.groovy",
            "rotateCredentials.groovy",
            "selfHealingAgents.groovy"
        ]

        expect:
        files.every { String name ->
            String src = new File(VARS_DIR, name).getText("UTF-8")
            !(src =~ /args\.[a-zA-Z0-9_]+\s*\?:\s*true/)
        }
    }
}
