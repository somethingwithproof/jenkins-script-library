<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Malagasy** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · **Malagasy** · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
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

Angon-tsoratra sy fitaovana Groovy natao hampandehanana ho azy sy hanamorana asa isan-karazany ao anatin'ny tontolo Jenkins. Manaraka fomba fiasa mifanaraka amin'ny Jenkins ity tranomboky ity ary ahitana fitsapana singa (unit tests) sy fitsapana fampidirana (integration tests). Manohana ny fampandehanana ho azy Jenkins ho an'ny tontolo maoderina.

## Endri-javatra

- **Fitantanana asa (Job Management)**: Mamelona/mamono, manadio, mamindra, ary manamboatra modely ho an'ny asa Jenkins
- **Fampidirana rahona (Cloud Integration)**: Mitantana mpiasa (agents) Jenkins ao amin'ny AWS, Azure, ary Kubernetes
- **Fiarovana**: Manamarina (audit) sy miaro ny tranga Jenkins
- **Fanatsarana ny fahaiza-miasa**: Manara-maso sy manatsara ny fahaiza-miasan'ny Jenkins
- **Fitantanana fandrindrana**: Manao tahiry fiarovana sy mamerina ny fandrindrana Jenkins
- **Famoronana pipeline**: Mamorona pipeline Jenkins avy amin'ny modely azo ampiasaina indray

## Firafitry ny tetikasa

Manaraka ny firafitry ny tetikasa Groovy mahazatra ny tetikasa:

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

## Fametrahana

Azo ampiasaina amin'ny fomba roa ity tranomboky ity:

1. **Amin'ny maha fiankinan-doha ao amin'ny tetikasa Gradle/Maven anao**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Amin'ny maha soratra tsirairay alefa mivantana ao amin'ny Jenkins**:

   Ny soratra ao amin'ny `src/com/github/thomasvincent/jenkinsscripts/scripts/` dia azo alefa mivantana ao amin'ny Jenkins Script Console na amin'ny alalan'ny Jenkins CLI.

## Fampiasana

### Soratra andalana baiko (Command-line Scripts)

Manohana ny safidy `--help` ny soratra andalana baiko rehetra mba hanehoana ny fampahalalana momba ny fampiasana.

#### Fitantanana asa

##### Fanadiovana asa

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Famelomana/Famonoana asa

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Famindrana asa

Tsy azo afindra amin’ny mpandrindra lavitra ny asa.

##### Fanaovana modely asa

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Famoronana pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Famakafakana asa

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Fitantanana node

##### Fitanisana node

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Asa eo amin'ny node

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Fitantanana mpanome rahona (Cloud Provider Management)

##### Fitantanana AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Fitantanana Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Fitantanana Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Fitantanana tranga Jenkins

##### Fahasalamana sy fiarovana

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Fitantanana fandrindrana

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API ara-pandaharana (Programmatic API)

Manome API ara-pandaharana ihany koa ity tranomboky ity mba hampiasaina ao amin'ny soratra Groovy anao manokana:

#### Fitantanana asa

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

#### Fitantanana rahona

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

## Fampandrosoana

### Fanamboarana ny tetikasa

```bash
./gradlew build
```

### Fampandehanana fitsapana

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Fanamarihana**: Misy olana fantatra amin'ny fiankinan-doha voajanahary JFFI (jffi-1.2.17-native.jar) izay mety hiteraka tsy fahombiazan'ny fitsapana. Raha sendra izany ianao dia afaka mandingana ny fitsapana amin'ny alalan'ny:
>
> ```bash
> ./gradlew build -x test
> ```
>
> Voarindra handalo ireo fitsapana miteraka olana amin'izao fotoana izao ny fitsapana GitHub Actions.

#### Tontolo fitsapana Docker

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Kalitaon'ny kaody

Mampiasa CodeNarc ity tetikasa ity ho an'ny fanamarinana ny kalitaon'ny kaody Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Fampidirana rahona azo ampiasaina

Manohana ireto mpanome rahona ireto ity tranomboky ity:

- **AWS EC2**: Mitantana tranga EC2 ho mpiasa Jenkins
- **Azure VMs**: Mitantana milina virtoaly Azure ho mpiasa Jenkins
- **Kubernetes**: Mitantana pod Kubernetes ho mpiasa Jenkins
- **Oracle Cloud**: Mitantana tranga Oracle Cloud Infrastructure (OCI) ho mpiasa Jenkins

## Endri-javatra ho avy

- **Fampidirana Google Cloud**: Mitantana mpiasa Jenkins ao amin'ny Google Cloud Platform
- **Fanohanana AWS ECS/Fargate**: Fanohanana mpiasa mifototra amin'ny kaontenera ao amin'ny AWS
- **Fitantanana Jenkins maromaro**: Fitaovana nohatsaraina hitantanana tranga Jenkins maromaro
- **Famindrana eo anelanelan'ny rahona**: Fitaovana hamindrana mpiasa eo anelanelan'ny mpanome rahona

## Fandraisana anjara

Tongasoa ny fandraisana anjara! Araho azafady ny [Torolalana momba ny fandraisana anjara](../../../CONTRIBUTING.md).

## Fiarovana

Ho an'ny ahiahy momba ny fiarovana, jereo azafady ny [Politika momba ny fiarovana](../../../SECURITY.md).

## Fahazoan-dalana

Nahazo alalana eo ambanin'ny Fahazoan-dalana MIT ity tetikasa ity - jereo ny rakitra [LICENSE](../../../LICENSE) raha mila antsipiriany.
