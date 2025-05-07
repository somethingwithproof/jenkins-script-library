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

import java.io.Serializable

/**
 * Pipeline utilities for Jenkins scripts.
 * 
 * Provides helper methods for determining platform characteristics
 * and environment details for Jenkins pipelines.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class PipelineUtils implements Serializable {

    private static final long serialVersionUID = 1L

    /** Reference to the pipeline script context */
    private transient def pipeline

    /**
     * Initializes a new PipelineUtils instance.
     * 
     * Takes a reference to the pipeline script context for command execution.
     * 
     * ```groovy
     * def utils = new PipelineUtils(this)
     * def os = utils.currentOS()  // Detect OS platform
     * ```
     */
    PipelineUtils(def pipeline) {
        this.pipeline = pipeline
    }

    /**
     * Determines the current operating system.
     * 
     * Uses pipeline commands to detect OS platform type.
     * 
     * ```groovy
     * def utils = new PipelineUtils(this)
     * utils.currentOS()  // 'linux', 'darwin', or 'windows'
     * ```
     */
    String currentOS() {
        if (pipeline.isUnix()) {
            try {
                def osName = pipeline.sh(script: 'uname', returnStdout: true).trim().toLowerCase()
                return osName == 'darwin' ? 'darwin' : 'linux'
            } catch (Exception e) {
                return 'linux' // Default to Linux if we can't determine
            }
        } else {
            return 'windows'
        }
    }

    /**
     * Determines the current system architecture.
     * 
     * Detects processor architecture and normalizes to standard identifiers.
     * 
     * ```groovy
     * def utils = new PipelineUtils(this)
     * utils.currentArchitecture()  // 'amd64', 'arm64', etc.
     * ```
     */
    String currentArchitecture() {
        try {
            if (pipeline.isUnix()) {
                def arch = pipeline.sh(script: 'uname -m', returnStdout: true).trim()
                return mapArchitecture(arch)
            } else {
                def arch = pipeline.powershell(script: '(Get-WmiObject Win32_Processor).Architecture', returnStdout: true).trim()
                return mapWindowsArchitecture(arch)
            }
        } catch (Exception e) {
            return 'amd64' // Default to amd64 if we can't determine
        }
    }

    /**
     * Maps UNIX architecture names to standardized identifiers.
     * 
     * Converts Unix uname architecture values to consistent naming scheme.
     * 
     * ```groovy
     * def utils = new PipelineUtils(this)
     * utils.mapArchitecture("x86_64")   // "amd64"
     * utils.mapArchitecture("aarch64")  // "arm64"
     * ```
     */
    private String mapArchitecture(String arch) {
        switch (arch) {
            case 'x86_64':
                return 'amd64'
            case 'aarch64':
                return 'arm64'
            case ~/^armv.*/:
                return 'arm'
            case ~/^i[3456]86$/:
                return 'i386'
            default:
                return arch
        }
    }

    /**
     * Maps Windows architecture codes to standardized identifiers.
     * 
     * Converts Windows architecture numeric codes to consistent naming scheme.
     * 
     * ```groovy
     * def utils = new PipelineUtils(this)
     * utils.mapWindowsArchitecture("0")   // "i386"
     * utils.mapWindowsArchitecture("9")   // "amd64"
     * utils.mapWindowsArchitecture("12")  // "arm64"
     * ```
     */
    private String mapWindowsArchitecture(String arch) {
        switch (arch) {
            case '0':
                return 'i386'
            case '9':
                return 'amd64'
            case '12':
                return 'arm64'
            default:
                return 'amd64'
        }
    }
}