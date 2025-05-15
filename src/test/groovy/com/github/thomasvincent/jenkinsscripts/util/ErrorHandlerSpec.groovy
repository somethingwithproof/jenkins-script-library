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

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Tests for ErrorHandler class to verify error handling behavior.
 */
@Subject(ErrorHandler)
class ErrorHandlerSpec extends Specification {

    def "handleError should log the error with the correct level"() {
        given:
        def mockLogger = Mock(Logger)
        def exception = new RuntimeException("Test exception")
        
        when:
        ErrorHandler.handleError("test operation", exception, mockLogger, Level.SEVERE)
        
        then:
        1 * mockLogger.log(Level.SEVERE, "Error during test operation: Test exception", exception)
    }
    
    def "handleErrorWithDefault should log and return default value"() {
        given:
        def mockLogger = Mock(Logger)
        def exception = new RuntimeException("Test exception")
        def defaultValue = "default"
        
        when:
        def result = ErrorHandler.handleErrorWithDefault("test operation", exception, mockLogger, defaultValue, Level.WARNING)
        
        then:
        1 * mockLogger.log(Level.WARNING, "Error during test operation: Test exception", exception)
        result == defaultValue
    }
    
    def "withErrorHandling should execute the action and return its result"() {
        given:
        def mockLogger = Mock(Logger)
        def action = { -> "success" }
        
        when:
        def result = ErrorHandler.withErrorHandling("test operation", action, mockLogger)
        
        then:
        0 * mockLogger.log(*_) // No logging should occur
        result == "success"
    }
    
    def "withErrorHandling should handle exceptions and return default value"() {
        given:
        def mockLogger = Mock(Logger)
        def action = { -> throw new RuntimeException("Test exception") }
        def defaultValue = "default"
        
        when:
        def result = ErrorHandler.withErrorHandling("test operation", action, mockLogger, defaultValue)
        
        then:
        1 * mockLogger.log(Level.WARNING, "Error during test operation: Test exception", _ as RuntimeException)
        result == defaultValue
    }
    
    def "withErrorHandling should rethrow exceptions when default value is null"() {
        given:
        def mockLogger = Mock(Logger)
        def action = { -> throw new RuntimeException("Test exception") }
        
        when:
        ErrorHandler.withErrorHandling("test operation", action, mockLogger)
        
        then:
        1 * mockLogger.log(Level.SEVERE, "Error during test operation: Test exception", _ as RuntimeException)
        thrown(RuntimeException)
    }
    
    def "error message should include root cause when available"() {
        given:
        def mockLogger = Mock(Logger)
        def rootCause = new IllegalArgumentException("Root cause")
        def exception = new RuntimeException("Wrapper", rootCause)
        
        when:
        ErrorHandler.handleError("test operation", exception, mockLogger)
        
        then:
        1 * mockLogger.log(Level.SEVERE, "Error during test operation: Wrapper (Caused by: IllegalArgumentException: Root cause)", exception)
    }
}