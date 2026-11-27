// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import hudson.EnvVars
import hudson.model.Label
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

// The test agents have distinct capacity responses and use the production verifier.
def fakeBin = new File(rule.jenkins.rootDir, 'spot-test-bin')
assert fakeBin.mkdir()
def kubectl = new File(fakeBin, 'kubectl')
kubectl.text = '''#!/bin/sh
case "$*" in
  *"get pod "*) printf '1234\\tfixture-worker' ;;
  *"get node "*) printf %s "$FIXTURE_CAPACITY" ;;
  *) exit 1 ;;
esac
'''
assert kubectl.setExecutable(true)
def agents = [:]
for (String kind : ['regular', 'spot']) {
    def env = new EnvVars('POD_NAME', 'fixture-pod', 'POD_NAMESPACE', 'ci', 'POD_UID', '1234',
        'PATH+SPOT_TEST', fakeBin.absolutePath, 'FIXTURE_CAPACITY', kind == 'spot' ? 'SPOT' : 'ON_DEMAND')
    def agent = rule.createOnlineSlave(Label.get("fixture-${kind}"), env)
    agents[kind] = agent.nodeName
}
def job = rule.jenkins.createProject(WorkflowJob, 'schedule-spot')
job.definition = new CpsFlowDefinition('''
    @Library('jenkins-script-library') _
    int runs = 0
    for (String excluded : ['fixture-regular', 'no-such-agent']) {
        boolean rejected = false
        try {
            withSpotAgent(provider: 'kubernetes', pool: 'eks', label: excluded) { runs++ }
        } catch (hudson.AbortException expected) {
            rejected = expected.message.contains('No verified')
        }
        assert rejected && runs == 0
    }
    boolean unverified = false
    try {
        withEnv(['POD_UID=9999']) {
            withSpotAgent(provider: 'kubernetes', pool: 'eks', label: 'fixture-spot') { runs++ }
        }
    } catch (hudson.AbortException expected) {
        unverified = expected.message.contains('No verified')
    }
    assert unverified && runs == 0
    String actual = withSpotAgent(provider: 'kubernetes', pool: 'eks', label: 'fixture-regular || fixture-spot') {
        runs++
        return env.NODE_NAME
    }
    assert runs == 1
    echo "Verified worker ${actual}"
    boolean caughtFailure = false
    try {
        withSpotAgent(provider: 'kubernetes', pool: 'eks', label: 'fixture-spot') {
            runs++
            error 'caller failure'
        }
    } catch (hudson.AbortException expected) {
        caughtFailure = expected.message == 'caller failure'
    }
    assert caughtFailure && runs == 2
''', true)
def build = rule.buildAndAssertSuccess(job)
rule.assertLogContains("Skipped ${agents.regular}", build)
rule.assertLogContains("Verified worker ${agents.spot}", build)
