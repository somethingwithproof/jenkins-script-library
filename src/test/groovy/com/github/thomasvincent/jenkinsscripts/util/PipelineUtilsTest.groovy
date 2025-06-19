package com.github.thomasvincent.jenkinsscripts.util

import org.junit.Test
import org.junit.Before
import static org.junit.Assert.*

class PipelineUtilsTest {
    
    private PipelineUtils utils
    private MockPipeline pipeline
    
    @Before
    void setup() {
        pipeline = new MockPipeline()
        utils = new PipelineUtils(pipeline)
    }
    
    @Test
    void testCurrentOSWindows() {
        pipeline.isUnixSystem = false
        assertEquals("windows", utils.currentOS())
    }
    
    @Test
    void testCurrentOSMac() {
        pipeline.isUnixSystem = true
        pipeline.mockOsName = "Darwin"
        assertEquals("darwin", utils.currentOS())
    }
    
    @Test
    void testCurrentOSLinux() {
        pipeline.isUnixSystem = true
        pipeline.mockOsName = "Linux"
        assertEquals("linux", utils.currentOS())
    }
    
    @Test
    void testCurrentOSUnknown() {
        pipeline.isUnixSystem = true
        pipeline.mockOsName = "UnknownOS"
        assertEquals("linux", utils.currentOS())
    }
    
    @Test
    void testCurrentArchitectureAMD64() {
        pipeline.isUnixSystem = true
        pipeline.mockArch = "x86_64"
        assertEquals("amd64", utils.currentArchitecture())
    }
    
    @Test
    void testCurrentArchitectureWindows() {
        pipeline.isUnixSystem = false
        
        // Test Windows architecture mappings
        pipeline.mockWindowsArch = "9"
        assertEquals("amd64", utils.currentArchitecture())
        
        pipeline.mockWindowsArch = "0"
        assertEquals("i386", utils.currentArchitecture())
        
        pipeline.mockWindowsArch = "12"
        assertEquals("arm64", utils.currentArchitecture())
        
        // Test default
        pipeline.mockWindowsArch = "99"
        assertEquals("amd64", utils.currentArchitecture())
    }
    
    @Test
    void testCurrentArchitectureUnixVariants() {
        pipeline.isUnixSystem = true
        
        pipeline.mockArch = "aarch64"
        assertEquals("arm64", utils.currentArchitecture())
        
        pipeline.mockArch = "armv7l"
        assertEquals("arm", utils.currentArchitecture())
        
        pipeline.mockArch = "i686"
        assertEquals("386", utils.currentArchitecture())
        
        pipeline.mockArch = "custom"
        assertEquals("custom", utils.currentArchitecture())
    }
    
    @Test
    void testMapArchitecture() {
        assertEquals("amd64", utils.mapArchitecture("x86_64"))
        assertEquals("amd64", utils.mapArchitecture("x64"))
        assertEquals("386", utils.mapArchitecture("i386"))
        assertEquals("386", utils.mapArchitecture("i686"))
        assertEquals("arm64", utils.mapArchitecture("aarch64"))
        assertEquals("custom", utils.mapArchitecture("custom"))
    }
    
    @Test
    void testMapWindowsArchitecture() {
        assertEquals("amd64", utils.mapWindowsArchitecture("AMD64"))
        assertEquals("amd64", utils.mapWindowsArchitecture("EM64T"))
        assertEquals("386", utils.mapWindowsArchitecture("x86"))
        assertEquals("amd64", utils.mapWindowsArchitecture("unknown"))
    }
    
    @Test
    void testCurrentOSWithException() {
        // Test exception handling in currentOS
        pipeline.isUnixSystem = true
        pipeline = new MockPipeline() {
            @Override
            def sh(Map args) {
                throw new RuntimeException("Command failed")
            }
        }
        utils = new PipelineUtils(pipeline)
        assertEquals("linux", utils.currentOS())
    }
    
    @Test
    void testCurrentArchitectureWithException() {
        // Test exception handling in currentArchitecture
        pipeline = new MockPipeline() {
            @Override
            def sh(Map args) {
                throw new RuntimeException("Command failed")
            }
            
            @Override
            def powershell(Map args) {
                throw new RuntimeException("Command failed")
            }
        }
        utils = new PipelineUtils(pipeline)
        assertEquals("amd64", utils.currentArchitecture())
    }
    
    // Mock Pipeline class for testing
    private static class MockPipeline {
        String mockWindowsArch = "AMD64"
        String mockOsName = "Linux"
        String mockArch = "x86_64"
        boolean isUnixSystem = true
        
        boolean isUnix() {
            return isUnixSystem
        }
        
        def sh(Map args) {
            if (args.script == 'uname') {
                return mockOsName + "\n"
            } else if (args.script == 'uname -m') {
                return mockArch + "\n"
            } else if (args.script?.contains("PROCESSOR_ARCHITECTURE")) {
                return mockWindowsArch
            }
            return ""
        }
        
        def powershell(Map args) {
            if (args.script?.contains("Win32_Processor")) {
                return mockWindowsArch + "\n"
            }
            return ""
        }
        
        def bat(Map args) {
            if (args.script?.contains("PROCESSOR_ARCHITECTURE")) {
                return mockWindowsArch
            }
            return ""
        }
    }
}