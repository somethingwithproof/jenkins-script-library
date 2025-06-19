package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import static org.junit.Assert.*

class GroovyCompatibilityVerifierJUnitTest {
    
    @Test
    void testIsCompatibleGroovyVersion() {
        boolean result = GroovyCompatibilityVerifier.isCompatibleGroovyVersion()
        assertTrue(result)
    }
    
    @Test
    void testIsCompatibleJavaVersion() {
        boolean result = GroovyCompatibilityVerifier.isCompatibleJavaVersion()
        assertTrue(result)
    }
    
    @Test
    void testCompareVersions() {
        assertTrue(GroovyCompatibilityVerifier.compareVersions("4.0.0", "3.0.15") > 0)
        assertTrue(GroovyCompatibilityVerifier.compareVersions("1.8.0", "11.0.2") < 0)
        assertEquals(0, GroovyCompatibilityVerifier.compareVersions("2.4.0", "2.4.0"))
        assertTrue(GroovyCompatibilityVerifier.compareVersions("3.0", "3.0.0") < 0)
        assertTrue(GroovyCompatibilityVerifier.compareVersions("3.0.1", "3.0") > 0)
        assertTrue(GroovyCompatibilityVerifier.compareVersions("17.0.1", "11.0.20") > 0)
    }
    
    @Test
    void testTestGroovy40Features() {
        boolean result = GroovyCompatibilityVerifier.testGroovy40Features()
        assertTrue(result)
    }
    
    @Test
    void testGetEnvironmentDetails() {
        Map<String, String> details = GroovyCompatibilityVerifier.getEnvironmentDetails()
        
        assertNotNull(details)
        assertTrue(details.containsKey('groovyVersion'))
        assertTrue(details.containsKey('javaVersion'))
        assertTrue(details.containsKey('javaVendor'))
        assertTrue(details.containsKey('osName'))
        assertNotNull(details.groovyVersion)
        assertNotNull(details.javaVersion)
    }
    
    @Test
    void testPersonClass() {
        def person = new GroovyCompatibilityVerifier.Person("Bob", 35)
        
        assertEquals("Bob", person.name)
        assertEquals(35, person.age)
    }
    
    @Test
    void testMainMethod() {
        // Capture stdout
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        PrintStream originalOut = System.out
        System.out = new PrintStream(baos)
        
        try {
            GroovyCompatibilityVerifier.main(new String[0])
            String output = baos.toString()
            
            assertTrue(output.contains("Jenkins Script Library Compatibility Verifier"))
            assertTrue(output.contains("Current environment:"))
            assertTrue(output.contains("Groovy version:"))
            assertTrue(output.contains("Java version:"))
        } finally {
            System.out = originalOut
        }
    }
}