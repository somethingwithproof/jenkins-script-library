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

package com.github.thomasvincent.jenkinsscripts.credentials

import com.cloudbees.plugins.credentials.common.StandardCredentials
import hudson.model.Item
import jenkins.model.Jenkins
import spock.lang.Specification

class CredentialRotationManagerSpec extends Specification {

    static class TestableCredentialRotationManager extends CredentialRotationManager {
        List<StandardCredentials> stubCredentials = []
        int trackUsageCalls = 0

        TestableCredentialRotationManager(Jenkins jenkins, List<StandardCredentials> creds) {
            super(jenkins)
            this.stubCredentials = creds
        }

        @Override
        protected List<StandardCredentials> getAllCredentials() {
            return stubCredentials ?: []
        }

        @Override
        Map<String, Object> trackCredentialUsage() {
            trackUsageCalls++
            return [:]
        }
    }

    def "handles empty credential store without division by zero in generateSummary"() {
        given:
        Jenkins jenkins = Mock(Jenkins) {
            getAllItems() >> []
        }
        TestableCredentialRotationManager manager = new TestableCredentialRotationManager(jenkins, [])

        when:
        Map<String, Object> analysis = manager.analyzeCredentials(true)

        then:
        analysis.summary.totalCredentials == 0
        analysis.summary.averageAge == 0.0d
        analysis.compliance.overall == 'COMPLIANT'
        !analysis.recommendations.any { it.type == 'COMPLIANCE' }
    }

    def "computes trackCredentialUsage only once across multiple credentials and flags NON_COMPLIANT properly"() {
        given:
        Jenkins jenkins = Mock(Jenkins) {
            getAllItems() >> ([] as List<Item>)
        }
        List<StandardCredentials> creds = (1..5).collect { i ->
            Mock(StandardCredentials) {
                getId() >> "cred-${i}"
                getDescription() >> "Test credential ${i}"
            }
        }
        TestableCredentialRotationManager manager = new TestableCredentialRotationManager(jenkins, creds)

        when:
        Map<String, Object> analysis = manager.analyzeCredentials(true)

        then:
        manager.trackUsageCalls == 1
        analysis.summary.totalCredentials == 5
        analysis.compliance.overall == 'NON_COMPLIANT'
        analysis.recommendations.any { it.type == 'COMPLIANCE' }
    }
}
