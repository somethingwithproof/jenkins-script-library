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
import spock.lang.Title

/**
 * A simple Spock specification to demonstrate idiomatic Groovy testing
 */
@Title("Simple Specification")
class SimpleSpec extends Specification {

    def "simple assertion should always pass"() {
        expect:
        true
    }

    def "string length should be greater than 0"() {
        given:
        String test = "Hello, World!"
        
        expect:
        test.length() > 0
    }
    
    def "demonstrate data-driven testing"() {
        expect:
        text.length() == expectedLength
        
        where:
        text            | expectedLength
        "Hello"         | 5
        "Spock"         | 5
        "Groovy"        | 6
        ""              | 0
    }
}