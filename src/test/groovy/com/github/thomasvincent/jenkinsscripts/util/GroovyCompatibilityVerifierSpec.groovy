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

package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Unroll

class GroovyCompatibilityVerifierSpec extends Specification {

    @Unroll
    def "compareVersions compares '#v1' vs '#v2' -> sign #expectedSign"() {
        when:
        int cmp = GroovyCompatibilityVerifier.compareVersions(v1, v2)

        then:
        Integer.signum(cmp) == expectedSign

        where:
        v1                  | v2                  | expectedSign
        '2.590'             | '2.590'             | 0
        '2.590'             | '2.492'             | 1
        '2.9'               | '2.10'              | -1
        '1.0.0-alpha'       | '1.0.0'             | -1
        '1.0.0-alpha.1'     | '1.0.0-beta.1'      | -1
        '1.0.0-rc1'         | '1.0.0'             | -1
        '500.v1234abcd'     | '499.v9999ffff'     | 1
        '500.v1234abcd'     | '501.v0000aaaa'     | -1
        '1.2.3+build.1'     | '1.2.3+build.99'    | 0
        '3.0.25'            | '3.0.9'             | 1
    }

    def "scripts compile and preserve expected security/performance fixes"() {
        given:
        String scanSrc = new File("src/com/github/thomasvincent/jenkinsscripts/scripts/SecurityVulnerabilityScan.groovy").getText("UTF-8")
        String perfSrc = new File("src/com/github/thomasvincent/jenkinsscripts/scripts/AnalyzePipelinePerformance.groovy").getText("UTF-8")

        expect:
        scanSrc.contains('import java.lang.management.ManagementFactory')
        scanSrc.contains('!it.startsWith("JNLP4")')
        !scanSrc.contains('cred.class.name.contains("StringCredentialsImpl")')
        perfSrc.contains("J(longOpt: 'json'")
        perfSrc.contains("j(longOpt: 'job'")
    }
}
