package com.github.thomasvincent.jenkinsscripts.util

/**
 * License header utilities for Jenkins scripts.
 * 
 * Provides standard license headers for inclusion in source files
 * to ensure consistent licensing across the Jenkins Script Library.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class LicenseHeader {
    
    /**
     * Returns the standard MIT license header.
     * 
     * Provides the complete MIT license header text for inclusion in source files.
     * 
     * ```groovy
     * String header = LicenseHeader.getMitLicenseHeader()
     * File sourceFile = new File("MyClass.groovy")
     * sourceFile.write(header + "\n\npackage mypackage\n\nclass MyClass {\n}")
     * ```
     */
    static String getMitLicenseHeader() {
        return '''/*
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
 */'''
    }
}