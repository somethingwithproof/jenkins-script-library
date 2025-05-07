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

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

/**
 * Tests for {@link GroovyVersionCheck} class.
 */
class GroovyVersionCheckTest {

    @Test
    void testGroovyVersionCompatibility() {
        assertTrue(GroovyVersionCheck.isCompatibleGroovyVersion(), 
                   "Current Groovy version should be compatible")
    }
    
    @Test
    void testGroovy40Features() {
        assertTrue(GroovyVersionCheck.testGroovy40Features(), 
                   "Groovy 4.0 features should work")
    }
    
    @Test
    void testEnvironmentDetails() {
        def details = GroovyVersionCheck.getEnvironmentDetails()
        
        assertNotNull(details, "Environment details should not be null")
        assertNotNull(details.groovyVersion, "Groovy version should be present")
        assertNotNull(details.javaVersion, "Java version should be present")
        
        System.out.println("Running on: Groovy ${details.groovyVersion}, Java ${details.javaVersion}")
    }
}