<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Filipino** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · **Filipino**
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Isang koleksiyon ng mga utility at script sa Groovy na idinisenyo upang i-automate at padaliin ang iba't ibang operasyon sa loob ng mga kapaligiran ng Jenkins. Sumusunod ang library na ito sa mga kasanayang tugma sa Jenkins at may kasamang mga unit at integration test. Sinusuportahan nito ang awtomasyon ng Jenkins para sa mga modernong kapaligiran.

## Mga Feature

- **Pamamahala ng Job**: I-enable/i-disable, linisin, i-migrate, at gawing template ang mga job ng Jenkins
- **Integrasyon sa Cloud**: Pamahalaan ang mga agent ng Jenkins sa AWS, Azure, at Kubernetes
- **Seguridad**: I-audit at i-secure ang mga instance ng Jenkins
- **Pag-optimize ng Pagganap**: Subaybayan at pagbutihin ang pagganap ng Jenkins
- **Pamamahala ng Konfigurasyon**: I-backup at i-restore ang mga konfigurasyon ng Jenkins
- **Pagbuo ng Pipeline**: Gumawa ng mga pipeline ng Jenkins mula sa mga magagamit muling template

## Istruktura ng Proyekto

Sumusunod ang proyekto sa karaniwang istruktura ng proyektong Groovy:

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

## Pag-install

Maaaring gamitin ang library na ito sa dalawang paraan:

1. **Bilang dependency sa iyong proyektong Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Bilang mga indibidwal na script na direktang patatakbuhin sa Jenkins**:

   Ang mga script sa `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` ay maaaring direktang patakbuhin sa Jenkins Script Console o sa pamamagitan ng Jenkins CLI.

## Paggamit

### Mga Command-line Script

Sinusuportahan ng lahat ng command-line script ang opsyong `--help` upang ipakita ang impormasyon sa paggamit.

#### Pamamahala ng Job

##### Paglilinis ng Job

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Pag-enable/Pag-disable ng Job

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Pag-migrate ng Job

Hindi magagamit ang remote migration. Kahit magbigay ng target URL, ginagamit ng script ang kasalukuyang Jenkins controller.

##### Paggawa ng Template ng Job

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pagbuo ng Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Pagsusuri ng Job

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Pamamahala ng Node

##### Paglilista ng mga Node

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Mga Operasyon sa Node

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Pamamahala ng Cloud Provider

##### Pamamahala ng AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Pamamahala ng Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Pamamahala ng Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Pamamahala ng Instance ng Jenkins

##### Kalusugan at Seguridad

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Pamamahala ng Konfigurasyon

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programmatic API

Nagbibigay din ang library ng programmatic API na magagamit sa sarili mong mga Groovy script:

JAR → [`util`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/util), [`pipeline`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/pipeline)

`src/main/groovy` → [`jobs`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/jobs), [`cloud`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/cloud)

#### Pamamahala ng Job

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

#### Pamamahala ng Cloud

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

## Pag-develop

### Pag-build ng Proyekto

```bash
./gradlew build
```

### Pagpapatakbo ng mga Pagsusuri

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Tala**: May kilalang isyu sa JFFI native dependency (jffi-1.2.17-native.jar) na maaaring magdulot ng pagkabigo sa mga pagsusuri. Kung maranasan mo ito, maaari mong laktawan ang mga pagsusuri gamit ang:
>
> ```bash
> ./gradlew build -x test
> ```
>
> Kasalukuyang naka-configure ang mga pagsusuri sa GitHub Actions upang laktawan ang mga problemadong pagsusuri.

#### Kapaligiran sa Pagsusuri gamit ang Docker

[`java17`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:24-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

### Kalidad ng Code

Gumagamit ang proyektong ito ng CodeNarc para sa mga pagsusuri ng kalidad ng code sa Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Mga Magagamit na Integrasyon sa Cloud

Sinusuportahan ng library ang mga sumusunod na cloud provider:

- **AWS EC2**: Pamahalaan ang mga instance ng EC2 bilang mga agent ng Jenkins
- **Azure VMs**: Pamahalaan ang mga virtual machine ng Azure bilang mga agent ng Jenkins
- **Kubernetes**: Pamahalaan ang mga pod ng Kubernetes bilang mga agent ng Jenkins
- **Oracle Cloud**: Pamahalaan ang mga instance ng Oracle Cloud Infrastructure (OCI) bilang mga agent ng Jenkins
- **DigitalOcean**: Pamahalaan ang mga droplet ng DigitalOcean bilang mga agent ng Jenkins

## Mga Paparating na Feature

- **Integrasyon sa Google Cloud**: Pamahalaan ang mga agent ng Jenkins sa Google Cloud Platform
- **Suporta sa AWS ECS/Fargate**: Suporta para sa mga agent na nakabatay sa container sa AWS
- **Pamamahala ng Maramihang Jenkins**: Pinahusay na mga tool para sa pamamahala ng maramihang instance ng Jenkins
- **Cross-Cloud Migration**: Mga tool para sa pag-migrate ng mga agent sa pagitan ng mga cloud provider

## Pag-aambag

Malugod na tinatanggap ang mga ambag! Mangyaring sundin ang aming [Mga Alituntunin sa Pag-aambag](../../../CONTRIBUTING.md).

## Seguridad

Para sa mga alalahanin sa seguridad, mangyaring suriin ang aming [Patakaran sa Seguridad](../../../SECURITY.md).

## Lisensya

Lisensyado ang proyektong ito sa ilalim ng MIT License - tingnan ang file na [LICENSE](../../../LICENSE) para sa mga detalye.
