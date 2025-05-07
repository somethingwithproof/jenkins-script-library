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
 * Tests for {@link PipelineUtils} class.
 */
class PipelineUtilsTest {

    @Test
    void testMapArchitecture() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> 
                if (args.script == 'uname -m') {
                    return 'x86_64'
                }
                return ''
            }
        ]
        
        def utils = new PipelineUtils(pipeline)
        
        // Test method using reflection since it's private
        def method = PipelineUtils.class.getDeclaredMethod("mapArchitecture", String.class)
        method.setAccessible(true)
        
        assertEquals("amd64", method.invoke(utils, "x86_64"))
        assertEquals("arm64", method.invoke(utils, "aarch64"))
        assertEquals("arm", method.invoke(utils, "armv7l"))
        assertEquals("i386", method.invoke(utils, "i686"))
        assertEquals("custom", method.invoke(utils, "custom"))
    }
    
    @Test
    void testMapWindowsArchitecture() {
        def pipeline = [
            isUnix: { -> false },
            powershell: { args -> '9' }
        ]
        
        def utils = new PipelineUtils(pipeline)
        
        // Test method using reflection since it's private
        def method = PipelineUtils.class.getDeclaredMethod("mapWindowsArchitecture", String.class)
        method.setAccessible(true)
        
        assertEquals("i386", method.invoke(utils, "0"))
        assertEquals("amd64", method.invoke(utils, "9"))
        assertEquals("arm64", method.invoke(utils, "12"))
        assertEquals("amd64", method.invoke(utils, "unknown"))
    }
    
    @Test
    void testCurrentOSOnLinux() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> 
                if (args.script == 'uname') {
                    return 'Linux'
                }
                return ''
            }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("linux", utils.currentOS())
    }
    
    @Test
    void testCurrentOSOnMac() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> 
                if (args.script == 'uname') {
                    return 'Darwin'
                }
                return ''
            }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("darwin", utils.currentOS())
    }
    
    @Test
    void testCurrentOSOnWindows() {
        def pipeline = [
            isUnix: { -> false }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("windows", utils.currentOS())
    }
    
    @Test
    void testCurrentOSOnException() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> throw new Exception("Command failed") }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("linux", utils.currentOS()) // Should default to linux
    }
    
    @Test
    void testCurrentArchitectureOnUnix() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> 
                if (args.script == 'uname -m') {
                    return 'x86_64'
                }
                return ''
            }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("amd64", utils.currentArchitecture())
    }
    
    @Test
    void testCurrentArchitectureOnWindows() {
        def pipeline = [
            isUnix: { -> false },
            powershell: { args -> '9' }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("amd64", utils.currentArchitecture())
    }
    
    @Test
    void testCurrentArchitectureOnException() {
        def pipeline = [
            isUnix: { -> true },
            sh: { args -> throw new Exception("Command failed") }
        ]
        
        def utils = new PipelineUtils(pipeline)
        assertEquals("amd64", utils.currentArchitecture()) // Should default to amd64
    }
}