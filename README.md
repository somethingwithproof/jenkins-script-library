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

A collection of Groovy utilities and scripts designed to automate and facilitate various operations within Jenkins environments. This library follows Jenkins-compatible practices and includes unit and integration tests. Supports Jenkins automation for modern environments.

## Features

- **Job Management**: Enable/disable, clean, migrate, and templatize Jenkins jobs
- **Cloud Integration**: Manage Jenkins agents on AWS, Azure, and Kubernetes
- **Security**: Audit and secure Jenkins instances
- **Performance Optimization**: Monitor and improve Jenkins performance
- **Configuration Management**: Backup and restore Jenkins configurations
- **Pipeline Generation**: Create Jenkins pipelines from reusable templates

## Project Structure

The project follows standard Groovy project structure:

```text
jenkins-script-library/
├── src/
│   ├── main/groovy/            # Main source code
│   │   └── com/github/thomasvincent/jenkinsscripts/
│   │       ├── cloud/          # Cloud provider integrations
│   │       ├── config/         # Configuration management
│   │       ├── helm/           # Helm management
│   │       ├── jobs/           # Job management
│   │       ├── nodes/          # Node management
│   │       ├── scripts/        # Command-line script entry points
│   │       ├── security/       # Security utilities
│   │       └── util/           # Common utilities
│   ├── test/groovy/            # Unit tests
│   └── integration-test/groovy/ # Integration tests
├── build.gradle                # Gradle build configuration
├── config/                     # Configuration files
│   └── codenarc/               # CodeNarc rules for code quality
├── gradle/wrapper/            # Gradle wrapper files
├── gradlew                     # Gradle wrapper script
├── LICENSE                     # MIT License
├── README.md                   # This file
└── SECURITY.md                 # Security policy
```

## Installation

Load the shared library from this repository in Jenkins, or use its Gradle artifact. Jenkins loads pipeline steps from `vars/` and packaged classes from `src/com/`. Both use the same source tree.

1. **As a dependency in your Gradle/Maven project**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **As individual scripts to run directly in Jenkins**:

   The scripts in `src/com/github/thomasvincent/jenkinsscripts/scripts/` can be executed directly in Jenkins Script Console or through the Jenkins CLI.

## Usage

### Command-line Scripts

All command-line scripts support the `--help` option to display usage information.

#### Job Management

##### Job Cleaning

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Job Enabling/Disabling

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Job Migration

The command-line migration entry point stops before changing jobs. The in-process
`JobMigrator` API requires two distinct Jenkins controller objects.

##### Job Templating

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pipeline Generation

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Job Analysis

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Node Management

##### Listing Nodes

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Node Operations

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Cloud Provider Management

##### AWS Management

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure Management

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes Management

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins Instance Management

##### Health and Security

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Configuration Management

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --destination /var/backups/jenkins

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programmatic API

The library also provides a programmatic API for use in your own Groovy scripts:

#### Job Management

```groovy
import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import com.github.thomasvincent.jenkinsscripts.jobs.JobTemplate
import jenkins.model.Jenkins

// Clean build history for a job
def jenkins = Jenkins.get()
def cleaner = new JobCleaner(jenkins, "my-job", true, 100)
cleaner.clean()

// Create a job from a template
def template = new JobTemplate(
    jenkins: jenkins,
    templateJobName: "template-job",
    parameters: [
        'PROJECT_NAME': 'frontend-app',
        'GIT_URL': 'https://github.com/myorg/frontend-app.git'
    ]
)
template.applyTemplate("new-job")
```

#### Cloud Management

```groovy
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.OracleCloudNodeManager
import jenkins.model.Jenkins

// Manage EC2 instances
def awsManager = new AWSNodeManager(Jenkins.get())
def templates = awsManager.getEC2Templates()
def nodes = awsManager.getEC2Nodes()
awsManager.provisionNewInstance("my-ec2-template")

// Manage Azure VMs
def azureManager = new AzureNodeManager(Jenkins.get())
def azureNodes = azureManager.getAzureNodesInfo()
azureManager.provisionNewVM("my-azure-template")

// Manage Kubernetes agents
def k8sManager = new KubernetesNodeManager(Jenkins.get())
def k8sNodes = k8sManager.getKubernetesNodes()
k8sManager.provisionNewPod("my-k8s-template")

// Manage Oracle Cloud instances
def ociManager = new OracleCloudNodeManager(Jenkins.get())
def ociNodes = ociManager.getOCIComputeInstances()
ociManager.provisionNewInstance("my-oci-template")

```

### Cloud inventory and cost estimates

A trusted Pipeline can call `cloudInventory()` to list configured clouds and their
agents. Each agent has a provider, the configured cloud name, its Jenkins node ID,
and its online state. Static agents and agents whose configured cloud is gone
are omitted. Multiple configurations of the same provider remain distinct. Controllers with the optional
Google Compute Engine plugin also report GCE agents and configured instance templates.

`optimizeCloudCosts.optimize(dryRun: true)` returns planned actions and estimated
savings. An apply request reports a status for each action. The bundled cost
providers currently report unsupported operations. The build description reports
action counts. Estimated savings are projections based on configured model rates.

To request interruptible capacity, call `provisionSpot` from a Pipeline agent.
It supports EC2 Spot, Azure Spot VMs, Google Spot VMs, OCI preemptible instances
and Kubernetes Pods on managed interruptible pools. Preview is the default.
To run a build on agents you already have, use `withSpotAgent`. It verifies the
allocated worker's interruptible capacity before running your build steps.
See [provisioning inputs](vars/README.md#spot-capacity) and
[existing-agent scheduling](vars/README.md#schedule-a-build-on-existing-spot-agents).

### Controller operations from a trusted Pipeline

```groovy
def inventory = auditPlugins(expected: readFile('config/plugins.txt'))
if (inventory.drift) {
    error "Plugin drift: missing=${inventory.missing}, extra=${inventory.extra}, mismatched=${inventory.mismatched}"
}

def entries = backupJenkinsConfig.restore(
    backupFile: '/var/backups/jenkins/snapshot.zip', files: ['jobs/team/'], dryRun: true
)
echo "Selected ${entries.size()} entries"
```

`auditPlugins()` returns a read-only inventory. The expected manifest accepts plugin
IDs, `id:latest` and `id:version`. The controller-side `AuditPlugins.groovy` script
accepts `--expected` and `--format json` for automation. A comparison with drift
fails the command after printing the report.

Use `BackupJenkinsConfig.groovy --restore ARCHIVE --list-contents` to inspect a
backup and `--files config.xml,jobs/team/ --dry-run` to preview a selection.
Omit the preview flags to apply it. ZIPs and directory backups are supported.
Originals of changed files are saved under a private recovery directory in
`JENKINS_HOME`. Reviewing and reloading the restored configuration is a separate
administrator action. Archive and restore paths refer to the controller filesystem.

For Pipeline backups, `destination` selects the exact output and `destinationDir`
selects the parent for generated names. The default is `JENKINS_HOME/backups`.
See [backup options and artifact access](vars/README.md#controller-administration).

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run controller operations against a temporary Jenkins home
./gradlew integrationTest

# Run all configured checks
./gradlew check
```

> **Note**: There's a known issue with the JFFI native dependency (jffi-1.2.17-native.jar) that may cause test failures. If you encounter this, you can skip tests with:
>
> ```bash
> ./gradlew build -x test
> ```
>
> The GitHub Actions tests are currently configured to skip problematic tests.

#### Docker Testing Environment

The Compose services run the Gradle test suite on Java 21 and Java 24.

[`java24`](Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Code Quality

This project uses CodeNarc for Groovy code quality checks:

```bash
./gradlew codenarcMain codenarcTest
```

## Available Cloud Integrations

The library supports the following cloud providers:

- **AWS EC2**: Manage EC2 instances as Jenkins agents
- **Azure VMs**: Manage Azure virtual machines as Jenkins agents
- **Kubernetes**: Manage Kubernetes pods as Jenkins agents
- **Oracle Cloud**: Manage Oracle Cloud Infrastructure (OCI) instances as Jenkins agents

## Upcoming Features

- **Google Cloud Integration**: Manage Jenkins agents on Google Cloud Platform
- **AWS ECS/Fargate Support**: Support for container-based agents in AWS
- **Multi-Jenkins Management**: Enhanced tools for managing multiple Jenkins instances
- **Cross-Cloud Migration**: Tools for migrating agents between cloud providers

## Contributing

Contributions are welcome! Please follow our [Contributing Guidelines](CONTRIBUTING.md).

## Security

For security concerns, please review our [Security Policy](SECURITY.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
