// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Computer
import hudson.model.Label
import hudson.model.labels.LabelAtom
import jenkins.model.Jenkins

/** Run the body once, on an existing agent whose interruptible capacity was verified. */
def call(Map options = [:], Closure body) {
    String provider = options.provider
    String pool = options.pool
    String label = options.label ?: 'spot'
    if (!(provider in ['aws', 'azure', 'gcp', 'oracle', 'kubernetes'])) {
        error 'withSpotAgent requires provider aws, azure, gcp, oracle or kubernetes'
    }
    if (provider == 'kubernetes' && !(pool in ['eks', 'aks', 'gke', 'oke'])) {
        error 'Kubernetes scheduling requires pool eks, aks, gke or oke'
    }

    List remaining = onlineAgents(label)
    while (remaining) {
        String selected
        boolean matched = false
        def value
        node(selfLabels(remaining)) {
            selected = getContext(Computer).name
            if (!remaining.contains(selected)) {
                error 'Jenkins allocated an agent outside the existing candidate set'
            }
            int status = verifyCapacity(provider, pool)
            if (status == 0) {
                matched = true
                value = body()
            } else {
                echo "Skipped ${selected}: ${status == 3 ? 'capacity did not match' : 'capacity could not be verified'}"
            }
        }
        if (matched) return value
        remaining.remove(selected)
        remaining.retainAll(onlineAgents(label))
    }
    error "No verified ${provider} spot agent available for label '${label}'"
}

@NonCPS
private List<String> onlineAgents(String expression) {
    Label requested = Label.parseExpression(expression)
    def candidates = []
    for (def agent : Jenkins.get().nodes) {
        def computer = agent.toComputer()
        if (computer?.online && computer.acceptingTasks && agent.numExecutors > 0 && requested.contains(agent)) {
            candidates.add(agent.nodeName)
        }
    }
    return candidates
}

@NonCPS
private String selfLabels(List names) {
    return names.collect { name -> LabelAtom.escape(name) }.join(' || ')
}

private int verifyCapacity(String provider, String pool) {
    int status
    dir(pwd(tmp: true) + '/verify-spot-' + UUID.randomUUID().toString()) {
        try {
            writeFile file: 'verify_spot.py', text: libraryResource('com/github/thomasvincent/jenkinsscripts/cloud/verify_spot.py')
            writeFile file: 'spot_pools.json', text: libraryResource('com/github/thomasvincent/jenkinsscripts/cloud/spot_pools.json')
            withEnv(["SPOT_PROVIDER=${provider}", "SPOT_POOL=${pool ?: ''}"]) {
                status = sh(script: 'python3 verify_spot.py', returnStatus: true)
            }
        } finally {
            deleteDir()
        }
    }
    return status
}
