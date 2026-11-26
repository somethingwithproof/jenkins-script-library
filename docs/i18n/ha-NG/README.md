<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Hausa** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · **Hausa** · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Laburaren Rubutun Jenkins

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Tarin kayan aikin Groovy da rubutun umarni da aka tsara don sarrafa ayyuka ta atomatik da sauƙaƙa ayyuka daban-daban a cikin muhallin Jenkins. Wannan laburare yana bin kyawawan dabi'u masu dacewa da Jenkins kuma ya ƙunshi gwaje-gwajen sashe (unit tests) da gwaje-gwajen haɗin kai (integration tests). Yana goyon bayan sarrafa Jenkins ta atomatik don muhallin zamani.

## Fasaloli

- **Sarrafa Ayyuka (Job Management)**: Kunna/kashewa, tsaftacewa, ƙaura, da ƙirƙirar samfurin ayyukan Jenkins
- **Haɗin Kai da Girgije (Cloud Integration)**: Sarrafa wakilan Jenkins a kan AWS, Azure, da Kubernetes
- **Tsaro**: Bincike da tsare samfuran Jenkins
- **Inganta Aiki**: Sa ido da haɓaka ƙarfin aikin Jenkins
- **Sarrafa Saiti**: Ajiyar baya da dawo da saitunan Jenkins
- **Samar da Pipeline**: Ƙirƙirar pipelines na Jenkins daga samfura masu sake amfani

## Tsarin Aiki

Aikin yana bin daidaitaccen tsarin aikin Groovy:

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

## Girkawa

Ana iya amfani da wannan laburare ta hanyoyi biyu:

1. **A matsayin abin dogaro a cikin aikin Gradle/Maven naka**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **A matsayin rubutun umarni guda-guda da za a gudanar kai tsaye a cikin Jenkins**:

   Ana iya gudanar da rubutun da ke cikin `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` kai tsaye a cikin Jenkins Script Console ko ta hanyar Jenkins CLI.

## Amfani

### Rubutun Layin Umarni

Duk rubutun layin umarni suna goyon bayan zaɓin `--help` don nuna bayanin amfani.

#### Sarrafa Ayyuka

##### Tsaftace Ayyuka

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Kunna/Kashe Ayyuka

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Ƙaurar Ayyuka

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Samfurin Ayyuka

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Samar da Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Nazarin Ayyuka

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Sarrafa Node

##### Jera Nodes

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Ayyukan Node

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Sarrafa Masu Bayar da Sabis na Girgije

##### Sarrafa AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Sarrafa Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Sarrafa Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Sarrafa Samfurin Jenkins

##### Lafiya da Tsaro

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Sarrafa Saiti

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API na Shirye-shirye

Laburaren yana kuma samar da API na shirye-shirye don amfani a cikin naka rubutun Groovy:

#### Sarrafa Ayyuka

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

#### Sarrafa Girgije

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

## Haɓakawa

### Gina Aikin

```bash
./gradlew build
```

### Gudanar da Gwaje-gwaje

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Lura**: Akwai sananniyar matsala tare da abin dogaro na asali na JFFI (jffi-1.2.17-native.jar) wanda zai iya haifar da gazawar gwaji. Idan ka ci karo da wannan, za ka iya tsallake gwaje-gwaje da:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> A halin yanzu an saita gwaje-gwajen GitHub Actions don tsallake gwaje-gwajen da ke da matsala.

#### Muhallin Gwajin Docker

Aikin ya ƙunshi muhallin gwaji na tushen Docker don tabbatar da dacewa da sigogin Java daban-daban:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Gwaje-gwajen haɗin kai suna amfani da JenkinsRule don ƙirƙirar samfurin Jenkins na wucin gadi don gwaji, don haka ba ka buƙatar uwar garken Jenkins da ke gudana. Waɗannan gwaje-gwajen suna tabbatar da cewa rubutun yana aiki daidai tare da ainihin APIs da tsarin bayanai na Jenkins. Duba [takardun gwajin haɗin kai](../../../src/integration-test/README.md) don ƙarin bayani.

### Ingancin Lamba

Wannan aikin yana amfani da CodeNarc don binciken ingancin lambar Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Haɗin Kan Girgije da Ke Akwai

Laburaren yana goyon bayan masu bayar da sabis na girgije masu zuwa:

- **AWS EC2**: Sarrafa samfuran EC2 a matsayin wakilan Jenkins
- **Azure VMs**: Sarrafa na'urorin kama-da-wane (virtual machines) na Azure a matsayin wakilan Jenkins
- **Kubernetes**: Sarrafa pods na Kubernetes a matsayin wakilan Jenkins
- **Oracle Cloud**: Sarrafa samfuran Oracle Cloud Infrastructure (OCI) a matsayin wakilan Jenkins
- **DigitalOcean**: Sarrafa droplets na DigitalOcean a matsayin wakilan Jenkins

## Fasalolin da Ke Tafe

- **Haɗin Kai da Google Cloud**: Sarrafa wakilan Jenkins a kan Google Cloud Platform
- **Goyon Bayan AWS ECS/Fargate**: Goyon baya ga wakilai masu amfani da kwantena a cikin AWS
- **Sarrafa Jenkins Da Yawa**: Kayan aiki masu ƙarfi don sarrafa samfuran Jenkins da yawa
- **Ƙaura Tsakanin Girgije**: Kayan aiki don ƙaura da wakilai tsakanin masu bayar da sabis na girgije

## Ba da Gudummawa

Ana maraba da gudummawa! Da fatan za a bi [Jagororin Ba da Gudummawa](../../../CONTRIBUTING.md) namu.

## Tsaro

Don damuwar tsaro, da fatan za a duba [Manufar Tsaro](../../../SECURITY.md) namu.

## Lasisi

Wannan aikin yana da lasisi ƙarƙashin Lasisin MIT - duba fayil ɗin [LICENSE](../../../LICENSE) don cikakken bayani.
