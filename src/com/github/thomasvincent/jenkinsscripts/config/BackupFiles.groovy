// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config

import com.cloudbees.groovy.cps.NonCPS
import java.util.stream.Stream

class BackupFiles {
    // Jenkins' Groovy supports withCloseable on Closeable, but NIO streams implement AutoCloseable.
    @NonCPS
    static def withStream(Stream stream, Closure work) {
        try {
            return work.call(stream)
        } finally {
            stream.close()
        }
    }
}
