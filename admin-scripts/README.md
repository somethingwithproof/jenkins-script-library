# Controller Admin Scripts

This directory contains thin administrative entry scripts intended for the Jenkins Script Console (`Manage Jenkins -> Script Console`) or `jenkins-cli.jar groovy = < script.groovy`.

## Why these are separate from `vars/`

Jenkins Shared Library steps under `vars/` execute inside Pipeline builds (CPS-transformed workflows running on agents or trusted build contexts). Controller maintenance operations that directly inspect or mutate `Jenkins.get()`, controller filesystems, global security settings, job trees, or agent pools belong to privileged controller administration rather than everyday application builds.

Keep build-scoped automation in `vars/` and `src/com/`, and run controller-level operations from:

- Jenkins Script Console (`Manage Jenkins -> Script Console`)
- `jenkins-cli.jar` (`java -jar jenkins-cli.jar -s https://jenkins.example.com groovy = < admin-scripts/audit-security.groovy`)
- Dedicated, access-controlled administrative pipelines backed by a Global Trusted Pipeline Library
- JCasC ([examples/jcasc/jenkins.yaml](../examples/jcasc/jenkins.yaml)) and Job DSL ([examples/job-dsl/seed.groovy](../examples/job-dsl/seed.groovy)) for declarative configuration

## Available Scripts

| Script | Purpose | Underlying Library Class |
| --- | --- | --- |
| [backup-controller.groovy](backup-controller.groovy) | Create a controller configuration archive under `JENKINS_HOME/backups` or a specified path | `JenkinsConfigBackup` |
| [audit-security.groovy](audit-security.groovy) | Audit controller security configuration and print categorized findings | `JenkinsSecurityAuditor` |
| [cleanup-jobs.groovy](cleanup-jobs.groovy) | Preview or disable jobs matching a regex pattern and clean build history | `JobDisabler`, `JobCleaner` |
| [optimize-agents.groovy](optimize-agents.groovy) | Inspect cloud inventory, estimate agent costs, and run a single-pass offline agent check | `CloudInventory`, `CloudCostOptimizer`, `SelfHealingAgentManager` |

Additional CLI entry points with `CliBuilder` argument parsing remain under `src/com/github/thomasvincent/jenkinsscripts/scripts/`.

## Running with `jenkins-cli.jar`

Ensure the compiled library JAR (`./gradlew jar`) is available on the controller classpath or loaded via an init hook, then execute:

```bash
java -jar jenkins-cli.jar -s https://jenkins.example.com -auth "${JENKINS_USER}:${JENKINS_API_TOKEN}" \
  groovy = < admin-scripts/audit-security.groovy
```

Most scripts accept environment variables (or script binding variables in the Script Console) with safe defaults such as `DRY_RUN=true`.
