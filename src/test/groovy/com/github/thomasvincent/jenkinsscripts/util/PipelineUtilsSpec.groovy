package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("PipelineUtils Specification")
class PipelineUtilsSpec extends Specification {

    def mockPipeline
    def utils

    def setup() {
        // Create a simple mock that implements the necessary methods
        mockPipeline = new MockPipeline()
        utils = new PipelineUtils(mockPipeline)
    }

    @Unroll
    def "currentOS returns '#expected' on Unix system with uname output '#unameOutput'"() {
        given:
        mockPipeline.unix = true
        mockPipeline.shOutput = unameOutput.trim()
        
        expect:
        utils.currentOS() == expected
        
        where:
        unameOutput | expected
        "Darwin\n"  | "darwin"
        "darwin\n"  | "darwin"
        "Linux\n"   | "linux"
        "linux\n"   | "linux"
    }

    def "currentOS returns 'windows' on non-Unix system"() {
        given:
        mockPipeline.unix = false
        
        expect:
        utils.currentOS() == "windows"
    }

    def "currentOS returns 'linux' as default when Unix command fails"() {
        given:
        mockPipeline.unix = true
        mockPipeline.throwOnSh = true
        
        expect:
        utils.currentOS() == "linux"
    }

    @Unroll
    def "currentArchitecture returns '#expected' on Unix with uname output '#unameOutput'"() {
        given:
        mockPipeline.unix = true
        mockPipeline.shOutput = unameOutput.trim()
        
        expect:
        utils.currentArchitecture() == expected
        
        where:
        unameOutput  | expected
        "x86_64\n"   | "amd64"
        "aarch64\n"  | "arm64"
        "armv7l\n"   | "arm"
        "i686\n"     | "386"
    }

    @Unroll
    def "currentArchitecture returns '#expected' on Windows with architecture '#windowsArch'"() {
        given:
        mockPipeline.unix = false
        mockPipeline.powershellOutput = windowsArch.trim()
        
        expect:
        utils.currentArchitecture() == expected
        
        where:
        windowsArch | expected
        "0\n"       | "i386"
        "9\n"       | "amd64"
        "12\n"      | "arm64"
        "AMD64\n"   | "amd64"
        "EM64T\n"   | "amd64"
        "x86\n"     | "386"
    }

    def "currentArchitecture returns 'amd64' as default when command fails"() {
        given:
        mockPipeline.unix = true
        mockPipeline.throwOnSh = true
        
        expect:
        utils.currentArchitecture() == "amd64"
    }

    @Unroll
    def "mapArchitecture maps '#input' to '#expected'"() {
        expect:
        utils.mapArchitecture(input) == expected
        
        where:
        input       | expected
        "x86_64"    | "amd64"
        "x64"       | "amd64"
        "aarch64"   | "arm64"
        "armv7l"    | "arm"
        "armv6l"    | "arm"
        "i386"      | "386"
        "i686"      | "386"
        "i586"      | "386"
        "ppc64le"   | "ppc64le"
        "s390x"     | "s390x"
        "unknown"   | "unknown"
    }

    @Unroll
    def "mapWindowsArchitecture maps '#arch' to '#expected'"() {
        expect:
        utils.mapWindowsArchitecture(arch) == expected
        
        where:
        arch      | expected
        "0"       | "i386"
        "9"       | "amd64"
        "12"      | "arm64"
        "AMD64"   | "amd64"
        "EM64T"   | "amd64"
        "x86"     | "386"
        "unknown" | "amd64"
    }
    
    // Simple mock class for testing
    static class MockPipeline {
        boolean unix = true
        String shOutput = ""
        String powershellOutput = ""
        boolean throwOnSh = false
        
        boolean isUnix() {
            return unix
        }
        
        String sh(Map args) {
            if (throwOnSh) {
                throw new IOException("Command failed")
            }
            return shOutput
        }
        
        String powershell(Map args) {
            return powershellOutput
        }
    }
}