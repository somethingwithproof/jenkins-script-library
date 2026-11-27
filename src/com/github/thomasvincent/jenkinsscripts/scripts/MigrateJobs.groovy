#!/usr/bin/env groovy
// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.scripts

import hudson.AbortException

// A URL cannot supply the second controller object required by JobMigrator.
String explanation = 'Remote job migration is unavailable. No jobs were changed.'
if (args.length == 1 && args[0] in ['--help', '-h']) {
    println explanation
    return
}
throw new AbortException(explanation)
