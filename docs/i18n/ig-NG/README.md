<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Igbo** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · **Igbo** · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
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

Mkpokọta ngwaọrụ na skriptị Groovy e mere iji mee ka ọrụ dị iche iche na-arụ ọrụ n'akpaghị aka ma dị mfe n'ime gburugburu Jenkins. Ọba akwụkwọ koodu a na-eso ụkpụrụ ndị kwekọrọ na Jenkins ma gụnye nnwale ngalaba na nnwale njikọta. Ọ na-akwado ọrụ n'akpaghị aka nke Jenkins maka gburugburu ọgbara ọhụrụ.

## Atụmatụ ọrụ

- **Njikwa Job**: Gbanye/gbanyụọ, hichaa, bufee, ma mee ka job Jenkins bụrụ ndebiri
- **Njikọta Igwe Ojii**: Chịkwaa agent Jenkins na AWS, Azure, na Kubernetes
- **Nchekwa**: Nyochaa ma chekwaa instance Jenkins
- **Nkwalite Arụmọrụ**: Lekọtaa ma melite arụmọrụ Jenkins
- **Njikwa Nhazi**: Chekwaa ma weghachi nhazi Jenkins
- **Mmepụta Pipeline**: Mepụta pipeline Jenkins site na ndebiri a pụrụ iji mee ihe ọzọ

## Ọdịdị Ọrụ

Ọrụ a na-eso ọdịdị ọrụ Groovy dị ka ụkpụrụ si dị:

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

## Nwụnye

Enwere ike iji ọba akwụkwọ koodu a n'ụzọ abụọ:

1. **Dị ka ndabere n'ime ọrụ Gradle/Maven gị**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Dị ka skriptị dị iche iche a ga-agba ozugbo na Jenkins**:

   Enwere ike ịgba skriptị ndị dị na `src/com/github/thomasvincent/jenkinsscripts/scripts/` ozugbo na Jenkins Script Console ma ọ bụ site na Jenkins CLI.

## Ojiji

### Skriptị Ahịrị Iwu

Skriptị ahịrị iwu niile na-akwado nhọrọ `--help` iji gosipụta ozi ojiji.

#### Njikwa Job

##### Nhicha Job

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Ịgbanye/Ịgbanyụ Job

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Mbufe Job

Enweghị ike ibugharị ọrụ gaa na Jenkins controller dị anya.

##### Ndebiri Job

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Mmepụta Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Nyocha Job

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Njikwa Nọọdụ

##### Idepụta Nọọdụ

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Ọrụ Nọọdụ

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Njikwa Onye Na-enye Igwe Ojii

##### Njikwa AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Njikwa Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Njikwa Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Njikwa Instance Jenkins

##### Ahụike na Nchekwa

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Njikwa Nhazi

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API Mmemme

Ọba akwụkwọ koodu a na-enyekwa API mmemme maka ojiji na skriptị Groovy nke gị:

#### Njikwa Job

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

#### Njikwa Igwe Ojii

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

## Mmepe

### Iwulite Ọrụ

```bash
./gradlew build
```

### Ịgba Nnwale

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Rịba ama**: E nwere nsogbu a maara na ndabere native nke JFFI (jffi-1.2.17-native.jar) nke nwere ike ịkpata ọdịda nnwale. Ọ bụrụ na ị zute nke a, ị nwere ike ịwụfe nnwale site na:
>
> ```bash
> ./gradlew build -x test
> ```
>
> A haziri nnwale GitHub Actions ugbu a iji wụfee nnwale ndị nwere nsogbu.

#### Gburugburu Nnwale Docker

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Ogo Koodu

Ọrụ a na-eji CodeNarc maka nnyocha ogo koodu Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Njikọta Igwe Ojii Dị Ugbu A

Ọba akwụkwọ koodu a na-akwado ndị na-enye ọrụ igwe ojii ndị a:

- **AWS EC2**: Chịkwaa instance EC2 dị ka agent Jenkins
- **Azure VMs**: Chịkwaa igwe mebere nke Azure dị ka agent Jenkins
- **Kubernetes**: Chịkwaa pod Kubernetes dị ka agent Jenkins
- **Oracle Cloud**: Chịkwaa instance Oracle Cloud Infrastructure (OCI) dị ka agent Jenkins

## Atụmatụ Ọrụ Na-abịa

- **Njikọta Google Cloud**: Chịkwaa agent Jenkins na Google Cloud Platform
- **Nkwado AWS ECS/Fargate**: Nkwado maka agent dabere na container na AWS
- **Njikwa Ọtụtụ Jenkins**: Ngwaọrụ e kwalitere maka ịchịkwa ọtụtụ instance Jenkins
- **Mbufe Gafee Igwe Ojii**: Ngwaọrụ maka ibufe agent n'etiti ndị na-enye ọrụ igwe ojii

## Itinye Aka

A na-anabata ntinye aka! Biko soro [Ntụziaka Itinye Aka](../../../CONTRIBUTING.md) anyị.

## Nchekwa

Maka nsogbu nchekwa, biko lelee [Atụmatụ Nchekwa](../../../SECURITY.md) anyị.

## Ikikere

A nyere ọrụ a ikikere n'okpuru Ikikere MIT - hụ faịlụ [LICENSE](../../../LICENSE) maka nkọwa.
