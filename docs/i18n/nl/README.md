<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Nederlands** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · **Nederlands** · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Een verzameling Groovy-hulpprogramma's en -scripts ontworpen om uiteenlopende bewerkingen binnen Jenkins-omgevingen te automatiseren en te vereenvoudigen. Deze bibliotheek volgt Jenkins-compatibele praktijken en bevat unit- en integratietests. Ondersteunt Jenkins-automatisering voor moderne omgevingen.

## Functies

- **Jobbeheer**: Jenkins-jobs inschakelen/uitschakelen, opschonen, migreren en omzetten naar sjablonen
- **Cloudintegratie**: Jenkins-agents beheren op AWS, Azure en Kubernetes
- **Beveiliging**: Jenkins-instanties auditen en beveiligen
- **Prestatieoptimalisatie**: Jenkins-prestaties monitoren en verbeteren
- **Configuratiebeheer**: Back-ups maken en herstellen van Jenkins-configuraties
- **Pipeline-generatie**: Jenkins-pipelines maken op basis van herbruikbare sjablonen

## Projectstructuur

Het project volgt de standaard Groovy-projectstructuur:

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

## Installatie

Deze bibliotheek kan op twee manieren worden gebruikt:

1. **Als afhankelijkheid in je Gradle/Maven-project**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Als afzonderlijke scripts om rechtstreeks in Jenkins uit te voeren**:

   De scripts in `src/com/github/thomasvincent/jenkinsscripts/scripts/` kunnen rechtstreeks worden uitgevoerd in de Jenkins Script Console of via de Jenkins CLI.

## Gebruik

### Commandoregelscripts

Alle commandoregelscripts ondersteunen de optie `--help` om gebruiksinformatie weer te geven.

#### Jobbeheer

##### Jobs opschonen

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Jobs inschakelen/uitschakelen

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Jobmigratie

Migratie naar een andere controller is niet beschikbaar.

##### Jobsjablonen

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pipeline-generatie

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Jobanalyse

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Nodebeheer

##### Nodes weergeven

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Nodebewerkingen

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Cloudproviderbeheer

##### AWS-beheer

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure-beheer

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes-beheer

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Beheer van Jenkins-instanties

##### Gezondheid en beveiliging

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Configuratiebeheer

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programmatische API

De bibliotheek biedt ook een programmatische API voor gebruik in je eigen Groovy-scripts:

#### Jobbeheer

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

#### Cloudbeheer

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

## Ontwikkeling

### Het project bouwen

```bash
./gradlew build
```

### Tests uitvoeren

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Opmerking**: Er is een bekend probleem met de native JFFI-afhankelijkheid (jffi-1.2.17-native.jar) dat testfouten kan veroorzaken. Als je dit tegenkomt, kun je tests overslaan met:
>
> ```bash
> ./gradlew build -x test
> ```
>
> De GitHub Actions-tests zijn momenteel geconfigureerd om problematische tests over te slaan.

#### Docker-testomgeving

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Codekwaliteit

Dit project gebruikt CodeNarc voor kwaliteitscontroles van Groovy-code:

```bash
./gradlew codenarcMain codenarcTest
```

## Beschikbare cloudintegraties

De bibliotheek ondersteunt de volgende cloudproviders:

- **AWS EC2**: EC2-instanties beheren als Jenkins-agents
- **Azure VMs**: Virtuele machines in Azure beheren als Jenkins-agents
- **Kubernetes**: Kubernetes-pods beheren als Jenkins-agents
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI)-instanties beheren als Jenkins-agents

## Geplande functies

- **Google Cloud-integratie**: Jenkins-agents beheren op Google Cloud Platform
- **AWS ECS/Fargate-ondersteuning**: Ondersteuning voor op containers gebaseerde agents in AWS
- **Multi-Jenkins-beheer**: Uitgebreidere tools voor het beheren van meerdere Jenkins-instanties
- **Cross-cloudmigratie**: Tools voor het migreren van agents tussen cloudproviders

## Bijdragen

Bijdragen zijn welkom! Volg onze [richtlijnen voor bijdragen](../../../CONTRIBUTING.md).

## Beveiliging

Raadpleeg bij beveiligingsproblemen ons [beveiligingsbeleid](../../../SECURITY.md).

## Licentie

Dit project is gelicentieerd onder de MIT-licentie - zie het bestand [LICENSE](../../../LICENSE) voor details.
