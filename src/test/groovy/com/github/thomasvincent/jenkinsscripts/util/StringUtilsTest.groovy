package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import org.junit.BeforeClass
import static org.junit.Assert.*

class StringUtilsTest {

    @Test
    void testClassInitialization() {
        // Force class initialization - the static block will run automatically
        // when the class is first loaded
        assertNotNull(StringUtils.class)
    }

    @Test
    void testSanitizeJobName() {
        assertEquals("test_job", StringUtils.sanitizeJobName("test job"))
        assertEquals("test@job", StringUtils.sanitizeJobName("test@job"))  // @ is not replaced
        assertEquals("test#job", StringUtils.sanitizeJobName("test#job"))  // # is not replaced
        assertEquals("test_job", StringUtils.sanitizeJobName("test/job"))  // / is replaced
        assertEquals("test_job", StringUtils.sanitizeJobName("test  job"))  // Multiple spaces become single _
        assertEquals("", StringUtils.sanitizeJobName(null))
    }

    @Test
    void testSafeParseInt() {
        assertEquals(123, StringUtils.safeParseInt("123", 0))
        assertEquals(0, StringUtils.safeParseInt("abc", 0))
        assertEquals(99, StringUtils.safeParseInt("", 99))
        assertEquals(99, StringUtils.safeParseInt(null, 99))
        assertEquals(-123, StringUtils.safeParseInt("-123", 0))
    }

    @Test
    void testSafeParseBoolean() {
        assertTrue(StringUtils.safeParseBoolean("true", false))
        assertTrue(StringUtils.safeParseBoolean("TRUE", false))
        assertTrue(StringUtils.safeParseBoolean("yes", false))
        assertTrue(StringUtils.safeParseBoolean("YES", false))
        assertTrue(StringUtils.safeParseBoolean("on", false))
        assertTrue(StringUtils.safeParseBoolean("ON", false))
        assertTrue(StringUtils.safeParseBoolean("1", false))
        
        assertFalse(StringUtils.safeParseBoolean("false", true))
        assertFalse(StringUtils.safeParseBoolean("FALSE", true))
        assertFalse(StringUtils.safeParseBoolean("no", true))
        assertFalse(StringUtils.safeParseBoolean("NO", true))
        assertFalse(StringUtils.safeParseBoolean("off", true))
        assertFalse(StringUtils.safeParseBoolean("OFF", true))
        assertFalse(StringUtils.safeParseBoolean("0", true))
        
        assertTrue(StringUtils.safeParseBoolean("", true))
        assertTrue(StringUtils.safeParseBoolean(null, true))
        assertFalse(StringUtils.safeParseBoolean("invalid", false))
    }

    @Test
    void testTruncate() {
        assertEquals("hello...", StringUtils.truncate("hello world", 8, "..."))
        assertEquals("hello", StringUtils.truncate("hello", 10, "..."))
        assertEquals("", StringUtils.truncate(null, 10, "..."))
        assertEquals("ab...", StringUtils.truncate("abcdef", 5, "..."))
        assertEquals("...", StringUtils.truncate("abcdef", 2, "..."))
        assertEquals("abcdef", StringUtils.truncate("abcdef", 6, "..."))
        assertEquals("abcdef", StringUtils.truncate("abcdef", 10, "..."))
        
        // Test with empty suffix
        assertEquals("hello", StringUtils.truncate("hello world", 5, ""))
    }

    @Test
    void testFormatParameter() {
        assertEquals("test=*****", StringUtils.formatParameter("test", "TEST", true))
        assertEquals("test=*****", StringUtils.formatParameter("test", null, true))
        assertEquals("=*****", StringUtils.formatParameter("", "", true))
        assertEquals("", StringUtils.formatParameter(null, null, true))
        
        assertEquals("test=TEST", StringUtils.formatParameter("test", "TEST", false))
        assertEquals("test=", StringUtils.formatParameter("test", null, false))
        assertEquals("=", StringUtils.formatParameter("", "", false))
        assertEquals("", StringUtils.formatParameter(null, null, false))
    }

    @Test
    void testCamelToKebab() {
        assertEquals("hello-world", StringUtils.camelToKebab("helloWorld"))
        assertEquals("hello-world-test", StringUtils.camelToKebab("helloWorldTest"))
        assertEquals("hello", StringUtils.camelToKebab("hello"))
        assertEquals("", StringUtils.camelToKebab(""))
        assertEquals("", StringUtils.camelToKebab(null))
        assertEquals("a-b-c", StringUtils.camelToKebab("ABC"))
    }

    @Test
    void testKebabToCamel() {
        assertEquals("helloWorld", StringUtils.kebabToCamel("hello-world"))
        assertEquals("helloWorldTest", StringUtils.kebabToCamel("hello-world-test"))
        assertEquals("hello", StringUtils.kebabToCamel("hello"))
        assertEquals("", StringUtils.kebabToCamel(""))
        assertEquals("", StringUtils.kebabToCamel(null))
    }

    @Test
    void testRandomAlphanumeric() {
        String result = StringUtils.randomAlphanumeric(10)
        assertEquals(10, result.length())
        assertTrue(result.matches("[a-zA-Z0-9]+"))
        
        assertEquals("", StringUtils.randomAlphanumeric(0))
        assertEquals("", StringUtils.randomAlphanumeric(-1))
        
        // Test different lengths
        assertEquals(5, StringUtils.randomAlphanumeric(5).length())
        assertEquals(20, StringUtils.randomAlphanumeric(20).length())
    }

    @Test
    void testExtractVersion() {
        assertEquals("1.2.3", StringUtils.extractVersion("version-1.2.3"))
        assertEquals("1.2.3", StringUtils.extractVersion("v1.2.3"))
        assertEquals("1.2.3-SNAPSHOT", StringUtils.extractVersion("release-1.2.3-SNAPSHOT"))
        assertNull(StringUtils.extractVersion("no-version"))
        assertNull(StringUtils.extractVersion(""))
        assertNull(StringUtils.extractVersion(null))
        assertEquals("10.20.30", StringUtils.extractVersion("app-10.20.30.jar"))
    }

    @Test
    void testCompareVersions() {
        assertEquals(0, StringUtils.compareVersions("1.2.3", "1.2.3"))
        assertEquals(-1, StringUtils.compareVersions("1.2.3", "1.2.4"))
        assertEquals(1, StringUtils.compareVersions("1.2.4", "1.2.3"))
        assertEquals(-1, StringUtils.compareVersions("1.2", "1.2.3"))
        assertEquals(1, StringUtils.compareVersions("1.2.3", "1.2"))
        assertEquals(0, StringUtils.compareVersions("", ""))
        assertEquals(0, StringUtils.compareVersions(null, null))
        assertEquals(-1, StringUtils.compareVersions(null, "1.0"))
        assertEquals(1, StringUtils.compareVersions("1.0", null))
        
        // Test with different version formats
        assertEquals(-1, StringUtils.compareVersions("1.9.0", "1.10.0"))
        assertEquals(1, StringUtils.compareVersions("2.0.0", "1.99.99"))
    }

    @Test
    void testParseVersionPart() {
        assertEquals(123, StringUtils.parseVersionPart("123"))
        assertEquals(0, StringUtils.parseVersionPart("abc"))
        assertEquals(0, StringUtils.parseVersionPart(""))
        assertEquals(0, StringUtils.parseVersionPart(null))
    }
}