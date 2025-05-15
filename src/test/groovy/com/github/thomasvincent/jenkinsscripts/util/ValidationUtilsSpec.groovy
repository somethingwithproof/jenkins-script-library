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
 * Tests for ValidationUtils class to verify validation methods work as expected.
 */
@Subject(ValidationUtils)
class ValidationUtilsSpec extends Specification {

    def "requireNonNull should return the input object when not null"() {
        given:
        def testObject = "test object"
        
        when:
        def result = ValidationUtils.requireNonNull(testObject, "Test Param")
        
        then:
        result == testObject
    }
    
    def "requireNonNull should throw IllegalArgumentException when input is null"() {
        when:
        ValidationUtils.requireNonNull(null, "Test Param")
        
        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Test Param must not be null"
    }
    
    @Unroll
    def "requireNonEmpty should validate string '#input' correctly"() {
        when:
        def result = ValidationUtils.requireNonEmpty(input, "Test String")
        
        then:
        result == expected
        
        where:
        input      | expected
        "test"     | "test"
        " test "   | "test"
        "  value"  | "value"
    }
    
    @Unroll
    def "requireNonEmpty should throw exception for invalid input '#input'"() {
        when:
        ValidationUtils.requireNonEmpty(input, "Test String")
        
        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Test String must not be null or empty"
        
        where:
        input << [null, "", "   "]
    }
    
    @Unroll
    def "requirePositive should handle value=#value correctly with default=#defaultValue"() {
        when:
        def result = ValidationUtils.requirePositive(value, "Test Value", defaultValue)
        
        then:
        result == expected
        
        where:
        value | defaultValue | expected
        5     | 10           | 5
        0     | 10           | 10
        -5    | 10           | 10
    }
    
    @Unroll
    def "requireInRange should validate value=#value in range #min-#max"() {
        when:
        def result = ValidationUtils.requireInRange(value, min, max, "Test Range")
        
        then:
        result == value
        
        where:
        value | min | max
        5     | 1   | 10
        1     | 1   | 10
        10    | 1   | 10
    }
    
    @Unroll
    def "requireInRange should throw exception when value=#value is outside range #min-#max"() {
        when:
        ValidationUtils.requireInRange(value, min, max, "Test Range")
        
        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Test Range must be between ${min} and ${max}"
        
        where:
        value | min | max
        0     | 1   | 10
        11    | 1   | 10
        -5    | 1   | 10
    }
    
    // File and directory tests would require file system access
    // In a real test environment, you might use temporary directories
    // or mock the file system
}