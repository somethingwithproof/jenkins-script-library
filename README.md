# Jenkins Script Library

A collection of Groovy utilities and scripts designed to automate and facilitate various operations within Jenkins environments. This library follows Jenkins-compatible practices and includes comprehensive test coverage.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Groovy 2.4](https://img.shields.io/badge/Groovy-2.4-blue.svg)](https://groovy-lang.org/)
[![Java 8+](https://img.shields.io/badge/Java-8+-orange.svg)](https://adoptium.net/)
[![Jenkins 2.249.3+](https://img.shields.io/badge/Jenkins-2.249.3+-green.svg)](https://jenkins.io/)

## Features

- **Job Management**: Enable/disable, clean, migrate, and templatize Jenkins jobs
- **Cloud Integration**: Manage Jenkins agents on AWS, Azure, and Kubernetes
- **Security**: Audit and secure Jenkins instances
- **Performance Optimization**: Monitor and improve Jenkins performance
- **Configuration Management**: Backup and restore Jenkins configurations

## Project Structure

The project follows standard Groovy project structure:

```
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

This library can be used in two ways:

1. **As a dependency in your Gradle/Maven project**:
   
   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **As individual scripts to run directly in Jenkins**:
   
   The scripts in `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` can be executed directly in Jenkins Script Console or through the Jenkins CLI.

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
```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Job Templating
```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
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
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programmatic API

The library also provides a comprehensive programmatic API for use in your own Groovy scripts:

#### Job Management
```groovy
import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import com.github.thomasvincent.jenkinsscripts.jobs.JobMigrator
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

// Migrate a job between Jenkins instances with JenkinsAccessor
def localAccessor = new LocalJenkinsAccessor(jenkins)
def migrator = new JobMigrator(localAccessor, localAccessor)
migrator.migrateJob("source-job", "target-job")
```

#### Cloud Management
```groovy
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.OracleCloudNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.DigitalOceanNodeManager
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

// Manage DigitalOcean droplets
def doManager = new DigitalOceanNodeManager(Jenkins.get())
def doNodes = doManager.getDigitalOceanNodes()
doManager.provisionNewDroplet("my-do-template")
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Note**: There's a known issue with the JFFI native dependency (jffi-1.2.17-native.jar) that may cause test failures. If you encounter this, you can skip tests with:
> ```bash
> ./gradlew build -PskipTests
> ```
> The GitHub Actions tests are currently configured to skip problematic tests.

#### Docker Testing Environment

The project includes a Docker-based testing environment to verify compatibility with different Java versions:

```bash
# Run tests in Java 8 (default)
./run-tests.sh java8

# Run tests in Java 11 with longer timeout
./run-tests.sh java11 30m

# Run tests in Java 17 with longer timeout
./run-tests.sh java17 30m

# Run all Java versions
./run-tests.sh all 1h

# Run tests with different scopes
./run-tests.sh java8 30m false minimal  # Run minimal test set (core utilities only)
./run-tests.sh java8 30m false default  # Run default test set
./run-tests.sh java8 30m false all      # Run all tests including integration tests
```

The Docker testing environment includes:

- Pre-downloading dependencies for faster test execution
- Volume caching for Gradle and Maven dependencies
- Shared base configuration for consistent environment
- Support for different test scopes (minimal, default, all)
- Memory limits and health checks for Docker containers

#### Java Compatibility

This library supports the following Java versions:
- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

We maintain backward compatibility with Java 8 to support legacy Jenkins installations. The library is compiled with Java 8 compatibility options by default.

The integration tests use JenkinsRule to create a temporary Jenkins instance for testing, so you don't need a running Jenkins server. These tests verify that the scripts work correctly with real Jenkins APIs and data structures. See the [integration test documentation](src/integration-test/README.md) for more details.

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
- **DigitalOcean**: Manage DigitalOcean droplets as Jenkins agents

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