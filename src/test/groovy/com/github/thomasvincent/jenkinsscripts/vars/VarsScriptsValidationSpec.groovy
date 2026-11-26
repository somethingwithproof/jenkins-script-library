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

    def "auditSecurity sendSecurityReport is not annotated with @NonCPS"() {
        given:
        String source = new File(VARS_DIR, "auditSecurity.groovy").getText("UTF-8")

        expect:
        !(source =~ /@NonCPS\s+private\s+void\s+sendSecurityReport/)
    }

    def "selfHealingAgents does not capture CPS echo closures in background callbacks"() {
        given:
        String source = new File(VARS_DIR, "selfHealingAgents.groovy").getText("UTF-8")

        expect:
        !(source =~ /onAgentRecovery\s*=\s*\{[^}]*\becho\b/)
        !(source =~ /onAgentFailure\s*=\s*\{[^}]*\becho\b/)
    }

    def "vars scripts preserve explicit false boolean flags instead of using Elvis true fallback"() {
        given:
        List<String> files = [
            "auditSecurity.groovy",
            "backupJenkinsConfig.groovy",
            "rotateCredentials.groovy"
        ]

        expect:
        files.every { String name ->
            String src = new File(VARS_DIR, name).getText("UTF-8")
            !(src =~ /args\.[a-zA-Z0-9_]+\s*\?:\s*true/)
        }
    }
}
