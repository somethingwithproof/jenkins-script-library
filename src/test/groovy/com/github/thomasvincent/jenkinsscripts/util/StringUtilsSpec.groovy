package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("StringUtils Specification")
class StringUtilsSpec extends Specification {

    def "class initializes successfully"() {
        expect:
        StringUtils.class != null
    }

    @Unroll
    def "sanitizeJobName converts '#input' to '#expected'"() {
        expect:
        StringUtils.sanitizeJobName(input) == expected
        
        where:
        input       | expected
        "test job"  | "test_job"
        "test@job"  | "test@job"
        "test#job"  | "test#job"
        "test/job"  | "test_job"
        "test  job" | "test_job"
        null        | ""
    }

    @Unroll
    def "safeParseInt converts '#input' to #expected with default #defaultValue"() {
        expect:
        StringUtils.safeParseInt(input, defaultValue) == expected
        
        where:
        input  | defaultValue | expected
        "123"  | 0            | 123
        "abc"  | 0            | 0
        ""     | 99           | 99
        null   | 99           | 99
        "-123" | 0            | -123
    }

    @Unroll
    def "safeParseBoolean converts '#input' to #expected"() {
        expect:
        StringUtils.safeParseBoolean(input, defaultValue) == expected
        
        where:
        input     | defaultValue | expected
        "true"    | false        | true
        "TRUE"    | false        | true
        "yes"     | false        | true
        "YES"     | false        | true
        "on"      | false        | true
        "ON"      | false        | true
        "1"       | false        | true
        "false"   | true         | false
        "FALSE"   | true         | false
        "no"      | true         | false
        "NO"      | true         | false
        "off"     | true         | false
        "OFF"     | true         | false
        "0"       | true         | false
        ""        | true         | true
        null      | true         | true
        "invalid" | false        | false
    }

    @Unroll
    def "truncate handles '#text' with maxLength #maxLength"() {
        expect:
        StringUtils.truncate(text, maxLength, suffix) == expected
        
        where:
        text         | maxLength | suffix | expected
        "hello world"| 8         | "..."  | "hello..."
        "hello"      | 10        | "..."  | "hello"
        null         | 10        | "..."  | ""
        "test"       | 4         | "..."  | "test"
        "testing"    | 4         | "..."  | "t..."
        ""           | 10        | "..."  | ""
    }

    @Unroll
    def "camelToKebab converts '#input' to '#expected'"() {
        expect:
        StringUtils.camelToKebab(input) == expected
        
        where:
        input              | expected
        "myVariableName"   | "my-variable-name"
        "HTTPSConnection"  | "https-connection"
        "IOError"          | "io-error"
        "simpleTest"       | "simple-test"
        ""                 | ""
        null               | ""
    }

    @Unroll
    def "kebabToCamel converts '#input' to '#expected'"() {
        expect:
        StringUtils.kebabToCamel(input) == expected
        
        where:
        input              | expected
        "my-variable-name" | "myVariableName"
        "https-connection" | "httpsConnection"
        "simple-test"      | "simpleTest"
        ""                 | ""
        null               | ""
    }

    def "randomAlphanumeric generates correct length strings"() {
        given:
        def lengths = [5, 10, 20]
        
        expect:
        lengths.each { length ->
            def result = StringUtils.randomAlphanumeric(length)
            assert result.length() == length
            assert result ==~ /[A-Za-z0-9]+/
        }
    }

    def "randomAlphanumeric handles edge cases"() {
        expect:
        StringUtils.randomAlphanumeric(0) == ""
        StringUtils.randomAlphanumeric(-1) == ""
    }

    @Unroll
    def "extractVersion extracts '#expected' from '#input'"() {
        expect:
        StringUtils.extractVersion(input) == expected
        
        where:
        input                    | expected
        "app-1.2.3"              | "1.2.3"
        "version: 2.0.0"         | "2.0.0"
        "release-1.0.0-SNAPSHOT" | "1.0.0-SNAPSHOT"
        "no version here"        | null
        null                     | null
    }

    @Unroll
    def "compareVersions compares '#v1' and '#v2' correctly"() {
        expect:
        StringUtils.compareVersions(v1, v2) == expected
        
        where:
        v1      | v2      | expected
        "1.0.0" | "1.0.0" | 0
        "1.0.1" | "1.0.0" | 1
        "1.0.0" | "1.0.1" | -1
        "2.0.0" | "1.9.9" | 1
        "1.10.0"| "1.9.0" | 1
        ""      | "1.0.0" | -1
        "1.0.0" | ""      | 1
    }

    @Unroll
    def "formatParameter formats '#key' and '#value' as '#expected'"() {
        expect:
        StringUtils.formatParameter(key, value) == expected
        
        where:
        key    | value      | expected
        "test" | "value"    | "test=value"
        "flag" | null       | "flag="
        "opt"  | ""         | "opt="
        null   | "value"    | ""
        ""     | "value"    | "=value"
    }
}