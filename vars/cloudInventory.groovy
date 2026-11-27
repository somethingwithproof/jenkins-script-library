// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory
import jenkins.model.Jenkins

@NonCPS
def call() {
    return new CloudInventory().inspect(Jenkins.get())
}
