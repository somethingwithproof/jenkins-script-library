// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

/** Submit a single spot-capacity request from the current Unix agent. Preview by default. */
def call(Map options = [:]) {
    if (options.containsKey('apply') && !(options.apply instanceof Boolean)) {
        error 'apply must be a boolean'
    }
    def config = new LinkedHashMap(options)
    boolean apply = config.remove('apply') == true
    String result
    dir(pwd(tmp: true) + '/spot-' + UUID.randomUUID().toString()) {
        try {
            sh 'chmod 700 .'
            writeFile file: 'provision.py', text: libraryResource('com/github/thomasvincent/jenkinsscripts/cloud/provision_spot.py')
            writeFile file: 'spot_pools.json', text: libraryResource('com/github/thomasvincent/jenkinsscripts/cloud/spot_pools.json')
            writeFile file: 'request.json', text: JsonOutput.toJson(config)
            String command = apply ? 'python3 provision.py request.json --apply' : 'python3 provision.py request.json'
            result = sh(script: command, returnStdout: true).trim()
        } finally {
            deleteDir()
        }
    }
    return parseResult(result)
}

@NonCPS
private Map parseResult(String json) {
    return new JsonSlurperClassic().parseText(json) as Map
}
