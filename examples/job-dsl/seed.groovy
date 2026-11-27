// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * Declarative Job DSL seed example for creating a Multibranch Pipeline job
 * that consumes jenkins-script-library via its repository Jenkinsfile.
 */

multibranchPipelineJob('platform/sample-service') {
    displayName('Sample Service')
    description('Multibranch Pipeline using jenkins-script-library shared steps.')

    branchSources {
        branchSource {
            source {
                git {
                    id('sample-service-git')
                    remote('https://github.com/example-org/sample-service.git')
                    credentialsId('github-app-credentials')
                    traits {
                        gitBranchDiscovery()
                        gitTagDiscovery()
                    }
                }
            }
        }
    }

    factory {
        workflowBranchProjectFactory {
            scriptPath('examples/Jenkinsfile')
        }
    }

    orphanedItemStrategy {
        discardOldItems {
            numToKeep(20)
            daysToKeep(14)
        }
    }

    triggers {
        periodicFolderTrigger {
            interval('15m')
        }
    }
}
