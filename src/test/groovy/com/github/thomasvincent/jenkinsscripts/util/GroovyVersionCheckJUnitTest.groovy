package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import static org.junit.Assert.*

class GroovyVersionCheckJUnitTest {
    
    @Test
    void testIsCompatibleGroovyVersion() {
        boolean result = GroovyVersionCheck.isCompatibleGroovyVersion()
        assertTrue(result)
    }
    
    @Test
    void testTestGroovy40Features() {
        boolean result = GroovyVersionCheck.testGroovy40Features()
        assertTrue(result)
    }
    
    @Test
    void testGetEnvironmentDetails() {
        Map<String, Object> details = GroovyVersionCheck.getEnvironmentDetails()
        
        assertNotNull(details)
        assertTrue(details.containsKey('groovyVersion'))
        assertTrue(details.containsKey('javaVersion'))
        assertTrue(details.containsKey('javaVendor'))
        assertTrue(details.containsKey('osName'))
        assertTrue(details.containsKey('timestamp'))
        assertNotNull(details.groovyVersion)
        assertNotNull(details.javaVersion)
        assertNotNull(details.timestamp)
    }
    
    @Test
    void testPersonClass() {
        def person = new GroovyVersionCheck.Person("Alice", 25)
        
        assertEquals("Alice", person.name)
        assertEquals(25, person.age)
    }
}