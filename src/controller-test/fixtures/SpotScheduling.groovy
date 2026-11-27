// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import hudson.EnvVars
import hudson.model.Result
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
  *"doks.digitalocean.com/capacity-type"*) printf %s "$FIXTURE_CAPACITY" | tr '[:upper:]' '[:lower:]' ;;
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
    for (def invalid : [0, -1, '2', null]) {
        try {
            withSpotAgent(provider: 'aws', maxAttempts: invalid) { error 'unexpected body' }
            error 'accepted invalid attempt count'
        } catch (hudson.AbortException expected) {
            assert expected.message.contains('positive integer')
        }
    }
    boolean caughtFailure = false
    try {
        withSpotAgent(provider: 'kubernetes', pool: 'eks', label: 'fixture-spot', maxAttempts: 2) {
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

// Pause a real Pipeline on a verified worker, then remove that worker from Jenkins.
def recovery = rule.jenkins.createProject(WorkflowJob, 'recover-spot')
recovery.definition = new CpsFlowDefinition('''
    @Library('jenkins-script-library') _
    def used = []
    def result = withSpotAgent(provider: 'kubernetes', pool: 'doks', label: 'fixture-spot', maxAttempts: 2) {
        used.add(env.NODE_NAME)
        echo "Executing on ${env.NODE_NAME}"
        if (used.size() == 1) {
            input id: 'Hold', message: 'Hold first agent'
        }
        return env.NODE_NAME
    }
    assert used.size() == 2 && used[0] != result && used[1] == result
    echo "Recovered on ${result}"
''', true)
def first = recovery.scheduleBuild2(0).waitForStart()
rule.waitForMessage('Proceed or Abort', first)
def lost = rule.jenkins.getNode(agents.spot)
def replacement = rule.createOnlineSlave(Label.get('fixture-spot'), new EnvVars(
    'POD_NAME', 'fixture-pod', 'POD_NAMESPACE', 'ci', 'POD_UID', '1234',
    'PATH+SPOT_TEST', fakeBin.absolutePath, 'FIXTURE_CAPACITY', 'SPOT'))
rule.jenkins.removeNode(lost)
rule.assertBuildStatusSuccess(rule.waitForCompletion(first))
rule.assertLogContains("Executing on ${agents.spot}", first)
rule.assertLogContains("Recovered on ${replacement.nodeName}", first)

def aborted = recovery.scheduleBuild2(0).waitForStart()
rule.waitForMessage('Proceed or Abort', aborted)
aborted.doStop()
rule.assertBuildStatus(Result.ABORTED, rule.waitForCompletion(aborted))
rule.assertLogNotContains('Retrying', aborted)

// The default path must propagate node loss after entering the body exactly once.
def single = rule.jenkins.createProject(WorkflowJob, 'single-spot')
single.definition = new CpsFlowDefinition("""
    @Library('jenkins-script-library') _
    withSpotAgent(provider: 'kubernetes', pool: 'doks', label: 'fixture-spot') {
        echo 'Entered single attempt'
        input id: 'Hold', message: 'Hold single attempt'
    }
""", true)
def interrupted = single.scheduleBuild2(0).waitForStart()
rule.waitForMessage('Proceed or Abort', interrupted)
rule.jenkins.removeNode(replacement)
rule.assertBuildStatus(Result.ABORTED, rule.waitForCompletion(interrupted))
rule.assertLogContains('Entered single attempt', interrupted)
rule.assertLogNotContains('Retrying', interrupted)
