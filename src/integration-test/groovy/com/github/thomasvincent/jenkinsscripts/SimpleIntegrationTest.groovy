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

package com.github.thomasvincent.jenkinsscripts

import com.github.thomasvincent.jenkinsscripts.util.StringUtils
import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import spock.lang.Specification
import spock.lang.Title
import java.util.logging.Logger

/**
 * Simple integration test to verify the test framework is working.
 * 
 * Real integration tests would require a Jenkins runtime environment.
 * This test demonstrates that the integration test framework is properly configured.
 */
@Title("Simple Integration Test")
class SimpleIntegrationTest extends Specification {
    
    private static final Logger logger = Logger.getLogger(SimpleIntegrationTest.class.name)
    
    def setup() {
        logger.info("Setting up integration test")
    }
    
    def cleanup() {
        logger.info("Cleaning up integration test")
    }
    
    def "test utility classes work together"() {
        given: "input data that needs validation and processing"
        def input = "  test-job-name-123  "
        
        when: "we validate and process the input"
        def validated = ValidationUtils.requireNonEmpty(input, "jobName")
        def sanitized = StringUtils.sanitizeJobName(validated)
        
        then: "the result is properly validated and sanitized"
        validated == "test-job-name-123"
        sanitized == "test-job-name-123"
    }
    
    def "test error handling integration"() {
        given: "an operation that might fail"
        def operation = {
            if (it) {
                throw new RuntimeException("Test error")
            }
            return "success"
        }
        
        when: "we use error handler with success case"
        def successResult = ErrorHandler.withErrorHandling("test operation", {
            operation(false)
        }, logger)
        
        then: "it returns the success value"
        successResult == "success"
        
        when: "we use error handler with failure case and default value"
        def failureResult = ErrorHandler.withErrorHandling("test operation", {
            operation(true)
        }, logger, "default")
        
        then: "it returns the default value"
        failureResult == "default"
    }
    
    def "test version comparison across utilities"() {
        given: "version strings to compare"
        def version1 = "2.361.4"
        def version2 = "2.400.1"
        
        when: "we compare versions"
        def comparison = StringUtils.compareVersions(version1, version2)
        
        then: "version1 is older than version2"
        comparison < 0
        
        and: "we can extract version parts"
        StringUtils.extractVersion("Jenkins 2.361.4 LTS") == "2.361.4"
    }
    
    def "test parameter formatting and validation"() {
        given: "various parameter types"
        def params = [
            string: "value",
            number: "42",
            boolean: "true",
            list: "[a, b, c]",
            nullValue: "null"
        ]
        
        when: "we format parameters"
        def formatted = params.collectEntries { k, v ->
            [k, StringUtils.formatParameter(k, v)]
        }
        
        then: "each parameter is formatted correctly"
        formatted.string == "string=value"
        formatted.number == "number=42"
        formatted.boolean == "boolean=true"
        formatted.list == "list=[a, b, c]"
        formatted.nullValue == "nullValue=null"
    }
    
    def "test integrated workflow simulation"() {
        given: "a simulated Jenkins job name that needs processing"
        def rawJobName = "  My Test Job #123!  "
        
        when: "we process it through our utilities"
        def result = ErrorHandler.withErrorHandling("job processing", {
            // Validate input
            def validated = ValidationUtils.requireNonEmpty(rawJobName, "jobName")
            
            // Sanitize for Jenkins - removes leading/trailing spaces, replaces internal spaces with underscores
            def sanitized = StringUtils.sanitizeJobName(validated)
            
            // Convert naming convention - # and ! are preserved as they are not illegal Jenkins characters
            def kebabCase = StringUtils.camelToKebab(sanitized)
            
            return [
                original: rawJobName,
                validated: validated,
                sanitized: sanitized,
                kebabCase: kebabCase
            ]
        }, logger)
        
        then: "the job name is properly processed"
        result.validated == "My Test Job #123!"  // trim() removes leading/trailing spaces
        result.sanitized == "My_Test_Job_#123!"  // spaces replaced with underscores, # and ! preserved
        result.kebabCase == "my_test_job_#123!"  // lowercase with underscores preserved
    }
}