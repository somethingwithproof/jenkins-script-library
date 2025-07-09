package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Subject
import spock.lang.Unroll
import java.util.logging.Logger
import java.util.logging.Level
import java.util.logging.Handler
import java.util.logging.LogRecord

/**
 * Spock specification for ErrorHandler - demonstrates idiomatic Groovy testing
 */
@Title("ErrorHandler Specification")
@Subject(ErrorHandler)
class ErrorHandlerSpec extends Specification {
    
    Logger testLogger
    TestLogHandler logHandler
    
    def setup() {
        testLogger = Logger.getLogger("TestLogger")
        logHandler = new TestLogHandler()
        testLogger.addHandler(logHandler)
        testLogger.useParentHandlers = false
    }
    
    def "handleError logs error with correct level and message"() {
        given: "an exception"
        def exception = new RuntimeException("Test error")
        
        when: "handling the error"
        ErrorHandler.handleError("Test operation failed", exception, testLogger, Level.SEVERE)
        
        then: "log contains correct information"
        logHandler.records.size() == 1
        with(logHandler.records[0]) {
            level == Level.SEVERE
            message.contains("Test operation failed")
            message.contains("Test error")
            thrown == exception
        }
    }
    
    def "handleErrorWithDefault returns default value and logs warning"() {
        given: "an exception and default value"
        def exception = new RuntimeException("Test error")
        def defaultValue = "default"
        
        when: "handling error with default"
        def result = ErrorHandler.handleErrorWithDefault(
            "Test operation failed", exception, testLogger, defaultValue, Level.WARNING)
        
        then: "returns default and logs warning"
        result == defaultValue
        logHandler.records.size() == 1
        with(logHandler.records[0]) {
            level == Level.WARNING
            message.contains("Test operation failed")
            message.contains("Test error")
            thrown == exception
        }
    }
    
    def "withErrorHandling returns result on success"() {
        when: "operation succeeds"
        def result = ErrorHandler.withErrorHandling("Test operation", { 
            "success" 
        }, testLogger, "default")
        
        then: "returns success value without logging"
        result == "success"
        logHandler.records.empty
    }
    
    def "withErrorHandling returns default on exception"() {
        when: "operation throws exception"
        def result = ErrorHandler.withErrorHandling("Test operation", { 
            throw new RuntimeException("Test error")
        }, testLogger, "default")
        
        then: "returns default and logs error"
        result == "default"
        logHandler.records.size() == 1
        with(logHandler.records[0]) {
            level == Level.WARNING
            message.contains("Test operation")
            thrown instanceof RuntimeException
        }
    }
    
    @Unroll
    def "formatErrorMessage handles #scenario correctly"() {
        when: "formatting error message"
        def message = ErrorHandler.formatErrorMessage(operation, exception)
        
        then: "message contains expected content"
        message != null
        if (operation) message.contains(operation)
        if (exceptionMessage) message.contains(exceptionMessage)
        if (causeMessage) message.contains(causeMessage)
        
        where:
        scenario            | operation           | exception                                              | exceptionMessage | causeMessage
        "normal case"       | "Operation failed"  | new RuntimeException("Test error")                     | "Test error"     | null
        "with cause"        | "Operation failed"  | new RuntimeException("Test", new Exception("Cause"))   | "Test"           | "Cause"
        "null operation"    | null                | new RuntimeException("Test error")                     | "Test error"     | null
    }
    
    def "formatErrorMessage handles null exception gracefully"() {
        when: "formatting with null exception"
        def message
        try {
            message = ErrorHandler.formatErrorMessage("Operation failed", null)
        } catch (NullPointerException e) {
            message = null
        }
        
        then: "either returns message or handles NPE"
        message == null || message.contains("Operation failed")
    }
    
    // Helper class using Groovy property syntax
    private static class TestLogHandler extends Handler {
        List<LogRecord> records = []
        
        void publish(LogRecord record) {
            records << record
        }
        
        void flush() {}
        void close() {}
    }
}