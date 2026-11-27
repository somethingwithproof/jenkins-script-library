<!-- translations:start -->
**Languages:** **English** · [Deutsch](docs/i18n/de/README.md) · [Español](docs/i18n/es/README.md) · [Français](docs/i18n/fr/README.md) · [Português (BR)](docs/i18n/pt-BR/README.md) · [Русский](docs/i18n/ru/README.md) · [日本語](docs/i18n/ja/README.md) · [한국어](docs/i18n/ko/README.md) · [简体中文](docs/i18n/zh-CN/README.md) · [العربية](docs/i18n/ar/README.md) · [हिन्दी](docs/i18n/hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** **English** · [Deutsch](docs/i18n/de/README.md) · [Español](docs/i18n/es/README.md) · [Français](docs/i18n/fr/README.md) · [Italiano](docs/i18n/it/README.md) · [Português (BR)](docs/i18n/pt-BR/README.md) · [Nederlands](docs/i18n/nl/README.md) · [Русский](docs/i18n/ru/README.md) · [Українська](docs/i18n/uk/README.md) · [日本語](docs/i18n/ja/README.md) · [한국어](docs/i18n/ko/README.md) · [简体中文](docs/i18n/zh-CN/README.md) · [繁體中文](docs/i18n/zh-TW/README.md) · [العربية](docs/i18n/ar/README.md) · [हिन्दी](docs/i18n/hi/README.md) · [Polski](docs/i18n/pl/README.md) · [Türkçe](docs/i18n/tr/README.md) · [Svenska](docs/i18n/sv/README.md) · [Tiếng Việt](docs/i18n/vi/README.md) · [Bahasa Indonesia](docs/i18n/id/README.md)
- **Africa:** [Afrikaans](docs/i18n/af-ZA/README.md) · [አማርኛ](docs/i18n/am-ET/README.md) · [العربية (مصر)](docs/i18n/ar-EG/README.md) · [العربية (المغرب)](docs/i18n/ar-MA/README.md) · [Français (Maroc)](docs/i18n/fr-MA/README.md) · [Français (Sénégal)](docs/i18n/fr-SN/README.md) · [Hausa](docs/i18n/ha-NG/README.md) · [Igbo](docs/i18n/ig-NG/README.md) · [Malagasy](docs/i18n/mg-MG/README.md) · [Kinyarwanda](docs/i18n/rw-RW/README.md) · [Soomaali](docs/i18n/so-SO/README.md) · [Kiswahili](docs/i18n/sw-KE/README.md) · [isiXhosa](docs/i18n/xh-ZA/README.md) · [Yorùbá](docs/i18n/yo-NG/README.md) · [isiZulu](docs/i18n/zu-ZA/README.md)
- **Asia:** [বাংলা](docs/i18n/bn/README.md) · [فارسی](docs/i18n/fa/README.md) · [עברית](docs/i18n/he/README.md) · [मराठी](docs/i18n/mr/README.md) · [தமிழ்](docs/i18n/ta/README.md) · [తెలుగు](docs/i18n/te/README.md) · [ไทย](docs/i18n/th/README.md) · [اردو](docs/i18n/ur/README.md) · [Bahasa Melayu](docs/i18n/ms/README.md) · [Filipino](docs/i18n/fil/README.md)
- **Balkans:** [Bosanski](docs/i18n/bs/README.md) · [Hrvatski](docs/i18n/hr/README.md) · [Српски](docs/i18n/sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](./LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

`jenkins-script-library` is a Groovy library, Jenkins Pipeline shared library (`vars/`), and controller-side script suite for automating Jenkins operations. It is built for Jenkins administrators and platform engineers who manage jobs, plugins, credentials, backups, metrics, and cloud agents across AWS, Azure, Google Cloud, Kubernetes, and Oracle Cloud without maintaining ad-hoc Script Console snippets.

The Gradle build compiles all 68 Groovy source files under `src/com/` and packages `resources/` against Jenkins LTS `2.590` and Groovy `3.0.25`. CI verifies the repository with 278 unit tests (262 Spock unit and validation tests plus 16 `JenkinsPipelineUnit` tests covering all 15 `vars/` steps) across Java 21 and Java 24, controller acceptance tests in a temporary Jenkins home (`./gradlew integrationTest`), Python unit tests for spot-capacity scripts, CodeNarc static analysis, release archive verification, SonarCloud analysis, and OpenSSF Scorecard checks.

## Architectural Boundaries

Jenkins enforces distinct execution models for Pipeline builds, controller administration, declarative configuration, and core extensions. This repository separates those layers explicitly:

1. **Shared Library (`@Library`)**: `src/com/`, `vars/*.groovy` (each paired with a `vars/*.txt` Pipeline Syntax Global Variables Reference help file), and `resources/`. Build-scoped steps such as `helmDeploy`, `generatePipeline`, `pipelineDSL`, `exportPrometheusMetrics`, `provisionSpot`, and `withSpotAgent` run within Pipeline builds without mutating global controller state or spawning long-lived background threads.
2. **Controller Admin Scripts (`admin-scripts/` and `src/com/.../scripts/`)**: Thin entry scripts for privileged controller-level operations (`Jenkins.get()`, controller backups, security audits, job cleanup, and agent fleet inspection) meant for the Jenkins Script Console (`Manage Jenkins -> Script Console`) or `jenkins-cli.jar groovy =`. See [admin-scripts/README.md](admin-scripts/README.md).
3. **JCasC, Job DSL, and Maven `.hpi` Plugins**:
   - Register the shared library declaratively with JCasC ([examples/jcasc/jenkins.yaml](examples/jcasc/jenkins.yaml)) and define multibranch seed jobs with Job DSL ([examples/job-dsl/seed.groovy](examples/job-dsl/seed.groovy)).
   - Jenkins only discovers `@Extension` classes compiled with annotation processing inside installed `.hpi` plugins, never from a Shared Library or Script Console. Components that require `ToolInstallation`, `AsyncPeriodicWork`, or `RootAction` belong in a dedicated Maven `.hpi` plugin rather than a Shared Library. For that reason, the former non-functional `hudson.tools.HelmInstallation` extension stub was removed, `HelmHelper` / `helmDeploy` operate directly on build agents, `selfHealingAgents` executes a single-pass check per invocation instead of spawning a daemon thread pool, and `exportPrometheusMetrics` keeps state scoped to each build script instance.

## Capabilities

- **Job lifecycle and pipelines**: Clean build history, disable jobs or triggers, copy and templatize jobs, audit job XML configurations, analyze job health and pipeline performance, inspect dependency graphs, manage job parameters, archive inactive jobs, and generate Declarative Pipelines from templates or Groovy DSL.
- **Controller operations and security**: Audit Jenkins security configuration and plugin drift against a manifest, run area-scoped vulnerability scans, evaluate credential age and usage for rotation, and create or selectively restore controller backups.
- **Cloud agents and spot capacity**: Inventory configured cloud providers and active agents, manage agents on AWS EC2, Azure VMs, Kubernetes, and Oracle Cloud, estimate agent spend and allocations, request interruptible compute capacity (`provisionSpot`), and run builds on verified spot agents (`withSpotAgent`).
- **Observability and agent recovery**: Export build-scoped Prometheus metrics (`exportPrometheusMetrics`) without global static state and run single-pass offline-agent health checks and recovery (`selfHealingAgents`).
- **Build-scoped Helm workflows**: Lint, configure, and deploy Helm charts on build agents with validated, shell-escaped arguments (`helmDeploy` and `HelmHelper`).

## Project Structure

The repository follows the Jenkins Shared Library directory layout so Jenkins loads `vars/`, `resources/`, and `src/` from the same source tree that Gradle compiles:

```text
jenkins-script-library/
├── vars/                                 # 15 Pipeline shared library steps (.groovy + .txt help files) and vars/README.md
├── resources/
│   └── com/github/thomasvincent/jenkinsscripts/cloud/
│                                         # Python helpers and pool metadata for spot capacity
├── admin-scripts/                        # Thin Script Console / jenkins-cli controller admin scripts and README
├── src/
│   ├── com/github/thomasvincent/jenkinsscripts/
│   │   ├── analytics/                    # Pipeline metrics collection and anomaly detection
│   │   ├── cloud/                        # AWS, Azure, Kubernetes, OCI managers and CloudInventory
│   │   ├── config/                       # Controller backup, selective restore, and PluginInventory
│   │   ├── cost/                         # Cloud cost estimation, allocation, and budgets
│   │   ├── credentials/                  # Credential usage analysis and rotation management
│   │   ├── dsl/                          # Groovy Pipeline DSL builder
│   │   ├── groovydoc/                    # Package-level GroovyDoc summary
│   │   ├── healing/                      # Single-pass and scheduled offline-agent recovery manager
│   │   ├── helm/                         # Build-scoped Helm CLI helper
│   │   ├── jobs/                         # Cleaning, disabling, templating, auditing, dependencies, scheduling
│   │   ├── monitoring/                   # Prometheus metrics exporter
│   │   ├── nodes/                        # Node inspection and offline launcher utilities
│   │   ├── pipeline/                     # Template-based Jenkinsfile generator
│   │   ├── scripts/                      # 28 CLI / Script Console entry points
│   │   ├── security/                     # Jenkins security configuration auditor
│   │   ├── util/                         # Static ErrorHandler, validation, string, and compatibility utilities
│   │   └── utils/                        # Instance-based ErrorHandler with java.util.logging.Logger
│   ├── test/
│   │   ├── groovy/                       # 20 Spock and JenkinsPipelineUnit test specs (278 tests)
│   │   └── python/                       # Unittest suite for spot provisioning and verification
│   ├── controller-test/                  # Jenkins controller acceptance tests (jenkins-test-harness)
│   └── integration-test/                 # Integration test suite
├── templates/                            # Reusable Jenkinsfile templates
├── examples/
│   ├── Jenkinsfile                       # Build-scoped Declarative Pipeline using @Library steps
│   ├── advanced-pipeline.jenkinsfile     # Scheduled operations pipeline example
│   ├── jcasc/jenkins.yaml                # Declarative JCasC Global Trusted Pipeline Library config
│   └── job-dsl/seed.groovy               # Declarative Job DSL multibranch pipeline seed
├── config/                               # CodeNarc rules, plugin manifests, and init.groovy.d examples
├── scripts/                              # Repository maintenance and release verification scripts
├── build.gradle                          # Gradle build, test, CodeNarc, and distribution config
├── docker-compose.test.yml               # Java 21 and Java 24 Docker test services
├── docker-compose.yml                    # Local Jenkins controller setup
├── VERSION                               # Library version (1.0.0)
├── LICENSE                               # MIT License
└── SECURITY.md                           # Security reporting policy
```

## Installation

### 1. Jenkins Global Shared Library

Configure this repository under **Manage Jenkins -> System -> Global Trusted Pipeline Libraries** (for example, named `jenkins-script-library`, pointing at `https://github.com/somethingwithproof/jenkins-script-library.git` on a pinned tag or `main`). For declarative setup, use [examples/jcasc/jenkins.yaml](examples/jcasc/jenkins.yaml); see [config/init.groovy.d/02-global-library.groovy](config/init.groovy.d/02-global-library.groovy) for a Groovy init script alternative.

Load the library at the top of a `Jenkinsfile`:

```groovy
@Library('jenkins-script-library') _
```

Jenkins loads callable steps and Pipeline Syntax help files from `vars/`, helper scripts from `resources/`, and Groovy classes from `src/com/`.

### 2. Compiled JAR or Release Archive

GitHub Releases attach verified `jenkins-script-library-<version>.zip`, `.tar.gz`, and `.jar` artifacts. To install the compiled JAR into your local Maven repository from a clone:

```bash
./gradlew publishToMavenLocal
```

Then declare the dependency in Gradle:

```groovy
dependencies {
    implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
}
```

### 3. Controller-Side Admin Scripts

Run thin administrative scripts from [admin-scripts/](admin-scripts/) (`backup-controller.groovy`, `audit-security.groovy`, `cleanup-jobs.groovy`, `optimize-agents.groovy`) or CLI scripts in `src/com/github/thomasvincent/jenkinsscripts/scripts/` via the Jenkins Script Console or `jenkins-cli.jar groovy =` when the library classes are on the controller classpath.

## Shared Library Pipeline Steps (`vars/`)

Every `vars/<step>.groovy` file has a matching `vars/<step>.txt` help file rendered automatically in Jenkins' Pipeline Syntax Global Variables Reference. Steps that access Jenkins controller APIs confine controller objects inside `@NonCPS` helper methods so only plain serializable `Map`, `List`, or `String` values cross Pipeline CPS suspension boundaries. Boolean parameters preserve explicit `false` values (for example `archive: false`, `atomic: false`, `wait: false`, `compress: false`, `dryRun: false`, `includeRecommendations: false`, and `includeUsage: false`).

| Step | Scope | Purpose | Key entry points / parameters |
| --- | --- | --- | --- |
| `helmDeploy` | Build agent | Lint, install Helm, and run shell-escaped `helm upgrade --install` | `release`, `chart`, `namespace`, `valuesFiles`, `setValues`, `atomic`, `wait`, `timeout`, `dryRun`, `lint`, `version`, `toolName`; `.setup()`, `.lint()` |
| `generatePipeline` | Build agent | Render a Jenkinsfile from a workspace template file or template string | `template`, `parameters`, `destination` |
| `pipelineDSL` | Build agent | Generate Declarative Pipeline source via Groovy DSL | Closure builder or `.simpleCIPipeline(Map)`, `.javaPipeline()`, `.mavenPipeline()`, `.nodePipeline()`, `.pythonPipeline()`, `.goPipeline()`, `.multiBranchPipeline(Map)` |
| `exportPrometheusMetrics` | Build-scoped | Record build/stage timings per build instance and render Prometheus text format | `call(namespace:, subsystem:, histogramBuckets:)`, `.recordBuild(...)`, `.recordStage(...)`, `.summary()`, `.reset()` |
| `provisionSpot` | Build agent | Preview or submit a single interruptible VM or Kubernetes Pod request | `provider`, `apply` (default `false`), plus provider-specific inputs |
| `withSpotAgent` | Build + controller | Run a closure once on an existing agent with verified spot capacity | `provider`, `label` (default `'spot'`), `pool` (required for `kubernetes`) |
| `analyzePipelineMetrics` | Trusted controller | Collect build and stage metrics, anomalies, and recommendations | `jobName` (defaults to `env.JOB_NAME`), `historyLimit` (default `100`), `includeRecommendations` (default `true`) |
| `auditPlugins` | Trusted controller | Inspect installed plugins and compare against a manifest | `expected` (manifest text; returns `drift`, `missing`, `extra`, `mismatched`) |
| `auditSecurity` | Trusted controller | Audit controller security settings and optionally archive HTML report | `archive` (default `true`), `failOnCritical`, `emailReport` |
| `backupJenkinsConfig` | Trusted controller | Create controller backups and preview or apply selective restores | `destination` or `destinationDir`, `includeJobs`, `includePlugins`, `includeNodes`, `compress`, `archive`; `.restore(backupFile:, files:, dryRun:, listContents:)` |
| `cloudInventory` | Trusted controller | List configured clouds and online cloud agents | No parameters; returns `[clouds: [...], agents: [...]]` |
| `disableJobs` | Trusted controller | Disable a job, regex-matched jobs, or jobs in a folder | `jobName`, `pattern`, `folderPath`, `dryRun`, `recursive` |
| `optimizeCloudCosts` | Trusted controller | Estimate agent costs, check budgets, allocate costs, and plan actions | `action` (`analyze`, `optimize`, `allocate`, `budget`) or `.analyze()`, `.optimize(dryRun:)`, `.getAllocations()`, `.checkBudgets(Map)`, `.generateReport()` |
| `rotateCredentials` | Trusted controller | Analyze credential compliance, detect unused credentials, and rotate | `action` (`analyze`, `rotate`, `unused`) or `.analyze(includeUsage:)`, `.rotate(dryRun:, policy:)`, `.findUnused(days:)`, `.checkCompliance()` |
| `selfHealingAgents` | Trusted controller | Run a single-pass offline-agent health check and recovery cycle | `call(config)`, `.healthCheck(config)`, `.statistics(config)`, `.agentStatuses(config)` |

### Pipeline Examples

```groovy
@Library('jenkins-script-library') _

pipeline {
    agent any
    stages {
        stage('Metrics, Security, and Credentials') {
            steps {
                script {
                    def metrics = analyzePipelineMetrics(
                        jobName: env.JOB_NAME,
                        historyLimit: 50,
                        includeRecommendations: false
                    )
                    echo "Success rate: ${metrics.summary.successRate}%"

                    def audit = auditSecurity(archive: false, failOnCritical: true)
                    echo "Critical findings: ${audit.summary.critical}"

                    def rotation = rotateCredentials.rotate(
                        dryRun: true,
                        policy: [PASSWORD: 60, API_KEY: 90]
                    )
                    echo "Planned rotations: ${rotation.rotated.size()}"
                }
            }
        }

        stage('Helm, Prometheus, and Single-Pass Agent Check') {
            steps {
                script {
                    def deployment = helmDeploy(
                        release: 'sample-service',
                        chart: './charts/sample-service',
                        namespace: 'staging',
                        lint: true,
                        dryRun: true
                    )
                    echo "Helm status: ${deployment.status}"

                    exportPrometheusMetrics.recordBuild(env.JOB_NAME, 'SUCCESS', 12345L)
                    String promText = exportPrometheusMetrics(namespace: 'jenkins', subsystem: 'pipeline')
                    echo "Exported ${promText.readLines().size()} Prometheus lines"

                    def health = selfHealingAgents(enableAutoReconnect: true, enableAutoRestart: false)
                    echo "Inspected ${health.agentStatuses.size()} agents (${health.totalAttempts} recovery attempts)"
                }
            }
        }
    }
}
```

Generating a pipeline from a template or Groovy DSL:

```groovy
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)

String generated = pipelineDSL.simpleCIPipeline([
    language: 'java',
    deploy: true,
    deployCommand: './deploy.sh'
])
```

### Cloud Inventory, Cost Estimates, and Spot Capacity

A trusted Pipeline can call `cloudInventory()` to list configured clouds and their agents. Each agent reports its provider, configured cloud name, Jenkins node ID, and online state. Static agents and agents whose configured cloud no longer exists are omitted. Multiple configurations of the same provider remain distinct. Controllers with the optional Google Compute Engine plugin also report GCE agents and configured instance templates.

`optimizeCloudCosts.optimize(dryRun: true)` returns planned actions and estimated savings. Setting `dryRun: false` reports a per-action status; the bundled cost providers currently report unsupported operations for direct mutation and record action counts in the build description. Estimated savings are projections based on configured model rates.

To request interruptible capacity, call `provisionSpot` from a Unix agent with Python 3.9+ and the relevant authenticated cloud CLI. It supports EC2 Spot, Azure Spot VMs, Google Spot VMs, OCI preemptible instances, and Kubernetes Pods on managed interruptible pools (`eks`, `aks`, `gke`, `oke`). Preview (`apply: false`) is the default. To run a build on existing agents, call `withSpotAgent` outside a `node` block; it verifies the allocated worker's interruptible capacity before invoking the body. See [provisioning inputs](vars/README.md#spot-capacity) and [existing-agent scheduling](vars/README.md#schedule-a-build-on-existing-spot-agents).

### Controller Operations from a Trusted Pipeline

```groovy
def inventory = auditPlugins(expected: readFile('config/plugins.txt'))
if (inventory.drift) {
    error "Plugin drift: missing=${inventory.missing}, extra=${inventory.extra}, mismatched=${inventory.mismatched}"
}

String backupPath = backupJenkinsConfig(
    destinationDir: '/var/backups/jenkins',
    includeJobs: true,
    includePlugins: true,
    includeNodes: true,
    compress: true,
    archive: false
)

def entries = backupJenkinsConfig.restore(
    backupFile: backupPath,
    files: ['jobs/team/'],
    dryRun: true
)
echo "Selected ${entries.size()} entries"
```

`auditPlugins()` returns a read-only inventory. The expected manifest accepts plugin IDs, `id:latest`, and `id:version`. The controller-side `AuditPlugins.groovy` script accepts `--expected` and `--format json` and exits non-zero when drift is found.

For Pipeline backups, `destination` selects the exact output file or directory and `destinationDir` selects the parent directory for generated names (default: `JENKINS_HOME/backups`). Originals of files overwritten during a non-dry-run restore are saved under a private recovery directory in `JENKINS_HOME`; reloading restored configuration is a separate administrator action. See [backup options and artifact access](vars/README.md#controller-administration).

## Command-Line and Script Console Entry Points

The 28 scripts under `src/com/github/thomasvincent/jenkinsscripts/scripts/` support `--help` (or `-h`) to print usage. Commands below assume the compiled library JAR and Jenkins runtime classpath are available (for example via `jenkins-cli groovy` on the controller).

### Job Management

```bash
# Clean up to 50 builds and reset the build number to 1
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job

# Disable a specific job or all buildable jobs
groovy DisableJobs.groovy my-jenkins-job
groovy DisableJobs.groovy --all

# Preview disabling jobs matching a regex pattern
groovy DisableJobTriggers.groovy --pattern "test-.*" --dry-run

# Copy a job with a dry run
groovy CopyJob.groovy -s Folder/source-job -n new-job --dryRun

# Create a job from an existing template job with parameters
groovy CreateJobFromTemplate.groovy \
  --template-job template-job \
  --job new-job \
  --param PROJECT_NAME=frontend \
  --param GIT_URL=https://github.com/myorg/frontend.git

# Or batch-create jobs from a JSON definition file
groovy CreateJobFromTemplate.groovy --template-job template-job --jobs-file jobs.json

# Archive a job with metadata
groovy ArchiveJobs.groovy --job old-job --reason "Retired" --archive-dir /var/jenkins_archives

# Analyze job health and pipeline performance
groovy AnalyzeJobHealth.groovy --job my-jenkins-job
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10
groovy AnalyzePipelineMetrics.groovy my-pipeline-job 50 summary

# Audit job configurations matching a job-name pattern as JSON
groovy AuditJobConfigurations.groovy --pattern "deploy.*" --json --output audit-report.json

# Detect circular dependencies or export a Graphviz DOT diagram
groovy ManageJobDependencies.groovy --circular
groovy ManageJobDependencies.groovy --diagram --output dependencies.dot

# Inventory job parameters across jobs
groovy ManageJobParameters.groovy --inventory --json
```

**Job migration note:** `MigrateJobs.groovy` stops with an `AbortException` without modifying jobs because a remote URL cannot supply the second in-process `Jenkins` controller object required by `JobMigrator`.

### Node and Cloud Management

```bash
# List static/agent nodes or start offline nodes
groovy ListSlaveNodes.groovy --all
groovy ListSlaveNodes.groovy --json my-slave-node
groovy StartOfflineSlaveNodes.groovy --all
groovy StartOfflineSlaveNodes.groovy my-slave-node

# List cloud nodes by provider or summary statistics
groovy ListCloudNodes.groovy --aws
groovy ListCloudNodes.groovy --kubernetes --json
groovy ListCloudNodes.groovy --stats

# AWS EC2 agents
groovy ManageEC2Agents.groovy --list
groovy ManageEC2Agents.groovy --provision "my-ec2-template"
groovy ManageEC2Agents.groovy --terminate i-1234567890abcdef0

# Azure VM agents
groovy ManageAzureVMAgents.groovy --list
groovy ManageAzureVMAgents.groovy --provision "my-azure-template"
groovy ManageAzureVMAgents.groovy --cleanup azure-agent-01

# Kubernetes pod agents
groovy ManageKubernetesAgents.groovy --list
groovy ManageKubernetesAgents.groovy --provision "my-k8s-template"
groovy ManageKubernetesAgents.groovy --terminate jenkins-agent-xyz123

# Helm tool installations
groovy ManageHelmInstallation.groovy --list
groovy ManageHelmInstallation.groovy --version 3.14.0
```

### Security, Credentials, Backup, and Scheduling

```bash
# Audit Jenkins security settings and export HTML
groovy AuditJenkinsSecurity.groovy --format html --output security-report.html
groovy AuditJenkinsSecurity.groovy --categories authentication,csrf,agents

# Compare installed plugins against a manifest
groovy AuditPlugins.groovy --expected config/plugins.txt --format json

# Scan specific security areas at a minimum severity threshold
groovy SecurityVulnerabilityScan.groovy --areas plugins,credentials,scripts --severity critical --detailed

# Analyze, preview rotation, or find unused credentials
groovy ManageCredentials.groovy analyze --include-usage
groovy ManageCredentials.groovy rotate --dry-run
groovy ManageCredentials.groovy unused --days 60

# Back up Jenkins configuration and retain the 5 newest archives
groovy BackupJenkinsConfig.groovy --destination /var/backups/jenkins --keep 5

# Inspect or preview a selective restore from a ZIP or directory backup
groovy BackupJenkinsConfig.groovy --restore /var/backups/jenkins/snapshot.zip --list-contents
groovy BackupJenkinsConfig.groovy --restore /var/backups/jenkins/snapshot.zip --files config.xml,jobs/team/ --dry-run

# Analyze or dry-run cloud agent scaling and cost optimization
groovy OptimizeAgentResources.groovy --analyze --json
groovy OptimizeAgentResources.groovy --scale --provider aws --min 2 --max 10 --dry-run
groovy OptimizeCloudCosts.groovy analyze --include-allocations
groovy OptimizeCloudCosts.groovy optimize --dry-run

# Analyze build queue load or throttle concurrent builds for a job
groovy OptimizeJobScheduling.groovy --analyze
groovy OptimizeJobScheduling.groovy --throttle-job my-job --max 3
groovy OptimizeJobScheduling.groovy --recommendations
```

## Programmatic Groovy API

The packaged classes under `src/com/github/thomasvincent/jenkinsscripts/` can be imported directly in Jenkins Script Console scripts, init hooks, or custom Groovy tooling.

### Jobs and Dependencies

```groovy
import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import com.github.thomasvincent.jenkinsscripts.jobs.JobDependencyManager
import com.github.thomasvincent.jenkinsscripts.jobs.JobTemplate
import jenkins.model.Jenkins

def jenkins = Jenkins.get()

// Clean up to 100 builds (25-job folder recursion cap) and reset build number to 1
def cleaner = new JobCleaner(jenkins, 'my-job', true, 25, 100)
cleaner.clean()

// Create a job from an existing template job (templateXml = null loads from templateJobName)
def template = new JobTemplate(
    jenkins,
    null,
    'template-job',
    [
        PROJECT_NAME: 'frontend-app',
        GIT_URL: 'https://github.com/myorg/frontend-app.git'
    ]
)
template.applyTemplate('new-job')

// Inspect job dependencies and detect cycles
def depManager = new JobDependencyManager(jenkins)
List<List<String>> cycles = depManager.findCircularDependencies()
List<String> orphaned = depManager.findOrphanedJobs()
```

### Cloud Agents and Inventory

```groovy
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.OracleCloudNodeManager
import jenkins.model.Jenkins

def jenkins = Jenkins.get()

// Inventory all configured clouds and active cloud agents
Map inventory = new CloudInventory().inspect(jenkins)

// AWS EC2
def awsManager = new AWSNodeManager(jenkins)
def ec2Templates = awsManager.getEC2Templates()
def ec2Nodes = awsManager.getEC2Nodes()
awsManager.provisionNewInstance('my-ec2-template')

// Azure VMs
def azureManager = new AzureNodeManager(jenkins)
def azureNodes = azureManager.getAzureNodesInfo()
azureManager.provisionNewVM('my-azure-template')

// Kubernetes pods
def k8sManager = new KubernetesNodeManager(jenkins)
def k8sNodes = k8sManager.getKubernetesNodes()
k8sManager.provisionNewPod('my-k8s-template')

// Oracle Cloud Infrastructure
def ociManager = new OracleCloudNodeManager(jenkins)
def ociNodes = ociManager.getOracleCloudNodes()
def ociTemplates = ociManager.getOracleCloudTemplates()
ociManager.provisionNewInstance('my-oci-template')
```

### Backups, Cost Analysis, Monitoring, Recovery, and Error Handling

```groovy
import com.github.thomasvincent.jenkinsscripts.config.JenkinsConfigBackup
import com.github.thomasvincent.jenkinsscripts.cost.CloudCostOptimizer
import com.github.thomasvincent.jenkinsscripts.healing.SelfHealingAgentManager
import com.github.thomasvincent.jenkinsscripts.monitoring.PrometheusMetricsExporter
import com.github.thomasvincent.jenkinsscripts.util.ErrorHandler
import java.util.logging.Logger
import jenkins.model.Jenkins

def jenkins = Jenkins.get()
def logger = Logger.getLogger('jenkins-ops')

// Controller backup and dry-run restore preview
def backup = new JenkinsConfigBackup(jenkins)
String archivePath = backup.performBackup('/var/backups/jenkins', true, true, true, true, true)
Map preview = backup.restore(archivePath, [files: ['config.xml'], dryRun: true])

// Cost analysis and optimization planning
def costOptimizer = new CloudCostOptimizer(jenkins)
Map costReport = costOptimizer.analyzeCosts()
Map plan = costOptimizer.optimizeResources(true)

// Self-healing agent manager
def healer = new SelfHealingAgentManager([maxRetryAttempts: 5, healthCheckIntervalMs: 120000], jenkins)
healer.start()
Map healingStats = healer.getStatistics()
healer.stop()

// Prometheus metrics export
def exporter = new PrometheusMetricsExporter('jenkins', 'pipeline', 10)
exporter.recordBuildCompletion('my-job', 'SUCCESS', 12345L)
String metricsText = exporter.exportMetrics()

// Static error handling helper (an instance-based ErrorHandler also exists in ...jenkinsscripts.utils)
def safeResult = ErrorHandler.withErrorHandling('load job count', {
    jenkins.getAllItems().size()
}, logger, 0)
```

## Cloud Provider Support

| Provider | `CloudInventory` | `CloudNodesManager` subclass | `provisionSpot` | `withSpotAgent` |
| --- | --- | --- | --- | --- |
| AWS (EC2) | Available | `AWSNodeManager` | Available (EC2 Spot) | Available (IMDSv2) |
| Azure (VMs) | Available | `AzureNodeManager` | Available (Spot VMs) | Available (IMDS) |
| Kubernetes | Available | `KubernetesNodeManager` | Available (`eks`, `aks`, `gke`, `oke`) | Available (Pod UID + worker label) |
| Oracle Cloud (OCI) | Available | `OracleCloudNodeManager` | Available (preemptible instances) | Available (OCI metadata + CLI) |
| Google Cloud (GCP) | Available (optional GCE plugin) | Planned | Available (GCE Spot VMs) | Available (GCE metadata) |

Planned work not yet implemented:

- Dedicated GCP `CloudNodesManager` subclass for GCE node lifecycle management outside `provisionSpot`.
- AWS ECS/Fargate container-agent management.
- Direct cloud-provider mutation backends for `CloudCostOptimizer.optimizeResources(false)`.

## Development, Testing, and Releases

### Requirements

- **Java 21** (CI baseline; the unit test suite is also tested on **Java 24** in Docker)
- **Gradle 8.14.2** (bundled via `./gradlew`)
- **Python 3.9+** (required for `testSpotRequests`, `verifyRelease`, and `provisionSpot` / `withSpotAgent` execution)

### Gradle Commands

```bash
# Clean, compile all 68 Groovy source files, run 278 Spock + JenkinsPipelineUnit tests, and run CodeNarc
./gradlew clean check

# Compile and assemble JAR + ZIP/TAR.GZ distributions without running tests
./gradlew build -x test

# Run Spock and JenkinsPipelineUnit tests only
./gradlew test

# Run Python spot tests and Jenkins controller acceptance tests (Jenkins LTS 2.590)
./gradlew integrationTest

# Run CodeNarc static analysis reports (HTML reports under build/reports/codenarc/)
./gradlew codenarcMain codenarcTest

# Assemble and verify versioned release distributions (.zip, .tar.gz) and JAR
./gradlew verifyRelease -PreleaseTag=v1.0.0
```

`codenarc` is configured with `ignoreFailures = true` in [build.gradle](build.gradle) so lint findings produce HTML reports without blocking compilation.

### Docker Testing and Local Jenkins Environment

Run the Gradle unit test suite inside pinned JDK containers using [docker-compose.test.yml](docker-compose.test.yml):

- `java24` ([Dockerfile.test](Dockerfile.test)): `eclipse-temurin:24-jdk` with Gradle 8.14.2
- `java21` ([Dockerfile.java21](Dockerfile.java21)): `eclipse-temurin:21-jdk` with Gradle 8.14.2

```bash
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

A `java17` service alias remains under the `legacy` Compose profile pointing to the `java24` definition for backwards compatibility with older invocations.

To start a local Jenkins controller with the library mounted read-only (exposed at `http://localhost:8080/jenkins` and agent port `50000`) using [docker-compose.yml](docker-compose.yml):

```bash
docker compose up -d
```

## Contributing

Contributions are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request, and sign off commits with `git commit -s` for DCO verification.

## Security

Report security vulnerabilities privately following [SECURITY.md](SECURITY.md). Do not open public issues for unpatched security flaws.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
