package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import static org.junit.Assert.*

class LicenseHeaderJUnitTest {
    
    @Test
    void testGetMitLicenseHeader() {
        String header = LicenseHeader.getMitLicenseHeader()
        
        assertNotNull(header)
        assertTrue(header.contains("MIT License"))
        assertTrue(header.contains("Copyright (c) 2023-2025 Thomas Vincent"))
        assertTrue(header.contains("Permission is hereby granted, free of charge"))
        assertTrue(header.contains("THE SOFTWARE IS PROVIDED \"AS IS\""))
        assertTrue(header.startsWith("/*"))
        assertTrue(header.endsWith("*/"))
    }
    
    @Test
    void testLicenseHeaderContainsAllRequiredSections() {
        String header = LicenseHeader.getMitLicenseHeader()
        
        // MIT license requirements
        assertTrue(header.contains("to use, copy, modify, merge, publish, distribute, sublicense, and/or sell"))
        assertTrue(header.contains("copies of the Software"))
        assertTrue(header.contains("The above copyright notice and this permission notice shall be included"))
        assertTrue(header.contains("WITHOUT WARRANTY OF ANY KIND"))
        assertTrue(header.contains("MERCHANTABILITY"))
        assertTrue(header.contains("FITNESS FOR A PARTICULAR PURPOSE"))
        assertTrue(header.contains("NONINFRINGEMENT"))
    }
}