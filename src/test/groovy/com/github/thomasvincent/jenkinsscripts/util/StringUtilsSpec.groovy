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
import spock.lang.Unroll

/**
 * Test for {@link StringUtils}.
 */
class StringUtilsSpec extends Specification {

    @Subject
    StringUtils utils = new StringUtils()
    
    @Unroll
    def "sanitizeJobName should sanitize '#input' to '#expected'"() {
        expect:
        StringUtils.sanitizeJobName(input) == expected
        
        where:
        input                  | expected
        "my-project"           | "my-project"
        "my project"           | "my_project"
        "project/with/slashes" | "project_with_slashes"
        "project:with:colons"  | "project_with_colons"
        "project with spaces"  | "project_with_spaces"
        "project__with__underscores" | "project_with_underscores"
        null                   | ""
        ""                     | ""
    }
    
    @Unroll
    def "safeParseInt should parse '#input' to #expected"() {
        expect:
        StringUtils.safeParseInt(input, 0) == expected
        
        where:
        input   | expected
        "123"   | 123
        "-123"  | -123
        "  42 " | 42
        "abc"   | 0
        ""      | 0
        null    | 0
    }
    
    @Unroll
    def "safeParseBoolean should parse '#input' to #expected"() {
        expect:
        StringUtils.safeParseBoolean(input, false) == expected
        
        where:
        input    | expected
        "true"   | true
        "True"   | true
        "TRUE"   | true
        "yes"    | true
        "y"      | true
        "1"      | true
        "on"     | true
        "false"  | false
        "False"  | false
        "FALSE"  | false
        "no"     | false
        "n"      | false
        "0"      | false
        "off"    | false
        "other"  | false
        ""       | false
        null     | false
    }
    
    @Unroll
    def "truncate should truncate '#input' to '#expected' with maxLength=#maxLength"() {
        expect:
        StringUtils.truncate(input, maxLength, "...") == expected
        
        where:
        input               | maxLength | expected
        "short"             | 10        | "short"
        "this is too long"  | 10        | "this is..."
        "exactly right"     | 13        | "exactly right"
        ""                  | 5         | ""
        null                | 5         | ""
        "tiny"              | 2         | "..."  // edge case, maxLength < ellipsis.length()
    }
    
    @Unroll
    def "formatParameter should format parameter #paramName with value #paramValue"() {
        expect:
        StringUtils.formatParameter(paramName, paramValue, sensitive) == expected
        
        where:
        paramName | paramValue | sensitive | expected
        "name"    | "value"    | false     | "name=value"
        "name"    | "value"    | true      | "name=*****"
        "name"    | ""         | false     | "name="
        "name"    | null       | false     | "name="
        null      | "value"    | false     | ""
    }
    
    @Unroll
    def "camelToKebab should convert '#input' to '#expected'"() {
        expect:
        StringUtils.camelToKebab(input) == expected
        
        where:
        input           | expected
        "camelCase"     | "camel-case"
        "ThisIsATest"   | "this-is-a-test"
        "ABC"           | "a-b-c"
        "abcDef123Ghi"  | "abc-def123-ghi"
        ""              | ""
        null            | ""
    }
    
    @Unroll
    def "kebabToCamel should convert '#input' to '#expected'"() {
        expect:
        StringUtils.kebabToCamel(input) == expected
        
        where:
        input           | expected
        "kebab-case"    | "kebabCase"
        "this-is-a-test"| "thisIsATest"
        "simple"        | "simple"
        ""              | ""
        null            | ""
    }
    
    def "randomAlphanumeric should generate a string of the correct length"() {
        when:
        def result = StringUtils.randomAlphanumeric(10)
        
        then:
        result.length() == 10
        result.matches("[a-zA-Z0-9]+")
        
        when:
        def result2 = StringUtils.randomAlphanumeric(0)
        
        then:
        result2 == ""
        
        when:
        def result3 = StringUtils.randomAlphanumeric(-1)
        
        then:
        result3 == ""
    }
    
    @Unroll
    def "extractVersion should extract version from '#input'"() {
        expect:
        StringUtils.extractVersion(input) == expected
        
        where:
        input                          | expected
        "version 1.2.3"                | "1.2.3"
        "v1.2.3-beta"                  | "1.2.3-beta"
        "something 1.2"                | "1.2"
        "no version here"              | null
        "multiple 1.2.3 and 4.5.6"     | "1.2.3"
        ""                             | null
        null                           | null
    }
    
    @Unroll
    def "compareVersions should compare '#version1' and '#version2'"() {
        expect:
        StringUtils.compareVersions(version1, version2) == expected
        
        where:
        version1 | version2 | expected
        "1.0.0"  | "1.0.0"  | 0
        "1.0.0"  | "1.0.1"  | -1
        "1.0.1"  | "1.0.0"  | 1
        "1.1.0"  | "1.0.0"  | 1
        "1.0.0"  | "1.1.0"  | -1
        "1.0"    | "1.0.0"  | -1
        "1.0.0"  | "1.0"    | 1
        "1.0.0"  | null     | 1
        null     | "1.0.0"  | -1
        null     | null     | 0
    }
}