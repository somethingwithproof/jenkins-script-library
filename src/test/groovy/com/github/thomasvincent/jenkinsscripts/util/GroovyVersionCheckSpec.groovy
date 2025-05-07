/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
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
import spock.lang.Subject

/**
 * Test for {@link GroovyVersionCheck}.
 */
class GroovyVersionCheckSpec extends Specification {

    @Subject
    GroovyVersionCheck checker = new GroovyVersionCheck()
    
    def "should correctly identify compatible Groovy version"() {
        when:
        def result = GroovyVersionCheck.isCompatibleGroovyVersion()
        
        then:
        result == true
    }
    
    def "should correctly test Groovy 4.0 features"() {
        when:
        def result = GroovyVersionCheck.testGroovy40Features()
        
        then:
        result == true
    }
    
    def "should return correct environment details"() {
        when:
        def details = GroovyVersionCheck.getEnvironmentDetails()
        
        then:
        details != null
        details.groovyVersion != null
        details.javaVersion != null
        details.groovyVersion.startsWith("4.")
    }
}