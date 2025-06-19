package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import static org.junit.Assert.*

class ValidationUtilsTest {

    @Test
    void testRequireNonNullWithValidObject() {
        String value = "test"
        String result = ValidationUtils.requireNonNull(value, "value")
        assertEquals("Should return the same object", value, result)
    }

    @Test(expected = IllegalArgumentException.class)
    void testRequireNonNullWithNull() {
        ValidationUtils.requireNonNull(null, "value")
    }

    @Test
    void testRequireNonEmptyWithValidString() {
        String value = "test"
        String result = ValidationUtils.requireNonEmpty(value, "value")
        assertEquals("Should return the same string", value, result)
    }

    @Test(expected = IllegalArgumentException.class)
    void testRequireNonEmptyWithNull() {
        ValidationUtils.requireNonEmpty(null, "value")
    }

    @Test(expected = IllegalArgumentException.class)
    void testRequireNonEmptyWithEmptyString() {
        ValidationUtils.requireNonEmpty("", "value")
    }

    @Test
    void testRequirePositiveWithValidNumber() {
        int value = 10
        int result = ValidationUtils.requirePositive(value, "value", -1)
        assertEquals("Should return the same number", value, result)
    }

    @Test
    void testRequirePositiveWithZero() {
        int result = ValidationUtils.requirePositive(0, "value", 99)
        assertEquals("Should return default value", 99, result)
    }

    @Test
    void testRequirePositiveWithNegative() {
        int result = ValidationUtils.requirePositive(-5, "value", 99)
        assertEquals("Should return default value", 99, result)
    }
    
    @Test
    void testRequireFileExists() {
        // Create a temp file for testing
        File tempFile = File.createTempFile("test", ".txt")
        tempFile.deleteOnExit()
        
        String result = ValidationUtils.requireFileExists(tempFile.absolutePath, "file")
        assertEquals(tempFile.absolutePath, result)
        
        // Clean up
        tempFile.delete()
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireFileExistsWithNonExistent() {
        ValidationUtils.requireFileExists("/non/existent/file.txt", "file")
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireFileExistsWithNull() {
        ValidationUtils.requireFileExists(null, "file")
    }
    
    @Test
    void testRequireDirectoryExists() {
        // Create a temp directory for testing
        File tempDir = File.createTempDir()
        tempDir.deleteOnExit()
        
        String result = ValidationUtils.requireDirectoryExists(tempDir.absolutePath, "dir")
        assertEquals(tempDir.absolutePath, result)
        
        // Clean up
        tempDir.delete()
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireDirectoryExistsWithFile() {
        // Create a temp file (not a directory)
        File tempFile = File.createTempFile("test", ".txt")
        tempFile.deleteOnExit()
        
        try {
            ValidationUtils.requireDirectoryExists(tempFile.absolutePath, "dir")
        } finally {
            tempFile.delete()
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireDirectoryExistsWithNonExistent() {
        ValidationUtils.requireDirectoryExists("/non/existent/dir", "dir")
    }
    
    @Test
    void testRequireDirectoryExistsWithNull() {
        try {
            ValidationUtils.requireDirectoryExists(null, "dir")
            fail("Should throw IllegalArgumentException")
        } catch (IllegalArgumentException e) {
            // Expected
            assertTrue(e.message.contains("dir"))
        }
    }
    
    @Test
    void testRequireInRange() {
        assertEquals(5, ValidationUtils.requireInRange(5, 1, 10, "value"))
        assertEquals(1, ValidationUtils.requireInRange(1, 1, 10, "value"))
        assertEquals(10, ValidationUtils.requireInRange(10, 1, 10, "value"))
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireInRangeBelow() {
        ValidationUtils.requireInRange(0, 1, 10, "value")
    }
    
    @Test(expected = IllegalArgumentException.class)
    void testRequireInRangeAbove() {
        ValidationUtils.requireInRange(11, 1, 10, "value")
    }
}