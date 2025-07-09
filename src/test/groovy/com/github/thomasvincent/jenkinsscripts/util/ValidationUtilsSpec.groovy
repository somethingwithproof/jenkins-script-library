package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title

@Title("ValidationUtils Specification")
class ValidationUtilsSpec extends Specification {

    def "requireNonNull returns value when not null"() {
        given:
        def value = "test"
        
        when:
        def result = ValidationUtils.requireNonNull(value, "value")
        
        then:
        result == value
    }

    def "requireNonNull throws exception when null"() {
        when:
        ValidationUtils.requireNonNull(null, "value")
        
        then:
        thrown(IllegalArgumentException)
    }

    def "requireNonEmpty returns value when valid"() {
        given:
        def value = "test"
        
        when:
        def result = ValidationUtils.requireNonEmpty(value, "value")
        
        then:
        result == value
    }

    def "requireNonEmpty throws exception for null or empty values"() {
        when:
        ValidationUtils.requireNonEmpty(input, "value")
        
        then:
        thrown(IllegalArgumentException)
        
        where:
        input << [null, ""]
    }

    def "requirePositive handles positive, zero, and negative values correctly"() {
        expect:
        ValidationUtils.requirePositive(value, "value", defaultValue) == expectedResult
        
        where:
        value | defaultValue | expectedResult
        10    | -1           | 10
        0     | 99           | 99
        -5    | 42           | 42
    }

    def "requireInRange returns value when in range"() {
        expect:
        ValidationUtils.requireInRange(value, min, max, "value") == value
        
        where:
        value | min | max
        5     | 1   | 10
        1     | 1   | 10
        10    | 1   | 10
    }

    def "requireInRange throws exception when out of range"() {
        when:
        ValidationUtils.requireInRange(value, min, max, "value")
        
        then:
        thrown(IllegalArgumentException)
        
        where:
        value | min | max
        0     | 1   | 10
        11    | 1   | 10
    }

    def "requireFileExists validates existing file"() {
        given:
        def tempFile = File.createTempFile("test", ".txt")
        tempFile.deleteOnExit()
        
        when:
        def result = ValidationUtils.requireFileExists(tempFile.absolutePath, "file")
        
        then:
        result == tempFile.absolutePath
        
        cleanup:
        tempFile.delete()
    }

    def "requireFileExists throws for null or non-existent file"() {
        when:
        ValidationUtils.requireFileExists(file, "file")
        
        then:
        thrown(IllegalArgumentException)
        
        where:
        file << [null, "/non/existent/file.txt"]
    }

    def "requireDirectoryExists validates existing directory"() {
        given:
        def tempDir = File.createTempDir()
        tempDir.deleteOnExit()
        
        when:
        def result = ValidationUtils.requireDirectoryExists(tempDir.absolutePath, "directory")
        
        then:
        result == tempDir.absolutePath
        
        cleanup:
        tempDir.delete()
    }

    def "requireDirectoryExists throws for invalid inputs"() {
        given:
        def tempFile = scenario == "file" ? File.createTempFile("test", ".txt") : null
        tempFile?.deleteOnExit()
        
        when:
        ValidationUtils.requireDirectoryExists(input ?: tempFile?.absolutePath, "directory")
        
        then:
        thrown(IllegalArgumentException)
        
        cleanup:
        tempFile?.delete()
        
        where:
        scenario        | input
        "null"          | null
        "non-existent"  | "/non/existent/dir"
        "file"          | null  // Will use tempFile
    }
}