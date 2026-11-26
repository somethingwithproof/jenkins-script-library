<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Bahasa Melayu** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · **Bahasa Melayu** · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Koleksi utiliti dan skrip Groovy yang direka untuk mengautomasikan dan memudahkan pelbagai operasi dalam persekitaran Jenkins. Pustaka ini mengikut amalan yang serasi dengan Jenkins serta menyertakan ujian unit dan integrasi. Menyokong automasi Jenkins untuk persekitaran moden.

## Ciri-ciri

- **Pengurusan Tugas**: Dayakan/lumpuhkan, bersihkan, migrasikan, dan jadikan tugas Jenkins sebagai templat
- **Penyepaduan Awan**: Urus ejen Jenkins pada AWS, Azure, dan Kubernetes
- **Keselamatan**: Audit dan lindungi tika Jenkins
- **Pengoptimuman Prestasi**: Pantau dan tingkatkan prestasi Jenkins
- **Pengurusan Konfigurasi**: Sandarkan dan pulihkan konfigurasi Jenkins
- **Penjanaan Saluran Paip**: Cipta saluran paip Jenkins daripada templat boleh guna semula

## Struktur Projek

Projek ini mengikut struktur projek Groovy piawai:

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

## Pemasangan

Pustaka ini boleh digunakan melalui dua cara:

1. **Sebagai kebergantungan dalam projek Gradle/Maven anda**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Sebagai skrip individu untuk dijalankan secara terus dalam Jenkins**:

   Skrip dalam `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` boleh dilaksanakan secara terus dalam Jenkins Script Console atau melalui Jenkins CLI.

## Penggunaan

### Skrip Baris Arahan

Semua skrip baris arahan menyokong pilihan `--help` untuk memaparkan maklumat penggunaan.

#### Pengurusan Tugas

##### Pembersihan Tugas

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Pengaktifan/Penyahaktifan Tugas

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Migrasi Tugas

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Pembuatan Templat Tugas

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Penjanaan Saluran Paip

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Analisis Tugas

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Pengurusan Nod

##### Menyenaraikan Nod

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Operasi Nod

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Pengurusan Pembekal Awan

##### Pengurusan AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Pengurusan Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Pengurusan Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Pengurusan Tika Jenkins

##### Kesihatan dan Keselamatan

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Pengurusan Konfigurasi

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API Berprogram

Pustaka ini juga menyediakan API berprogram untuk digunakan dalam skrip Groovy anda sendiri:

#### Pengurusan Tugas

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

#### Pengurusan Awan

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

## Pembangunan

### Membina Projek

```bash
./gradlew build
```

### Menjalankan Ujian

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Nota**: Terdapat isu yang diketahui berkaitan kebergantungan asli JFFI (jffi-1.2.17-native.jar) yang mungkin menyebabkan kegagalan ujian. Jika anda menghadapi isu ini, anda boleh melangkau ujian dengan:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Ujian GitHub Actions pada masa ini dikonfigurasikan untuk melangkau ujian yang bermasalah.

#### Persekitaran Pengujian Docker

Projek ini menyertakan persekitaran pengujian berasaskan Docker untuk mengesahkan keserasian dengan versi Java yang berbeza:

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

Persekitaran pengujian Docker merangkumi:

- Pramuat turun kebergantungan untuk pelaksanaan ujian yang lebih pantas
- Cache volum untuk kebergantungan Gradle dan Maven
- Konfigurasi asas berkongsi untuk persekitaran yang konsisten
- Sokongan untuk skop ujian yang berbeza (minimal, default, all)
- Had memori dan semakan kesihatan untuk kontena Docker

#### Keserasian Java

Pustaka ini menyokong versi Java berikut:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

Kami mengekalkan keserasian ke belakang dengan Java 8 untuk menyokong pemasangan Jenkins legasi. Pustaka ini dikompil dengan pilihan keserasian Java 8 secara lalai.

Ujian integrasi menggunakan JenkinsRule untuk mencipta tika Jenkins sementara bagi tujuan pengujian, jadi anda tidak memerlukan pelayan Jenkins yang sedang berjalan. Ujian ini menentusahkan bahawa skrip berfungsi dengan betul bersama API dan struktur data Jenkins sebenar. Lihat [dokumentasi ujian integrasi](../../../src/integration-test/README.md) untuk butiran lanjut.

### Kualiti Kod

Projek ini menggunakan CodeNarc untuk semakan kualiti kod Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Penyepaduan Awan yang Tersedia

Pustaka ini menyokong pembekal awan berikut:

- **AWS EC2**: Urus tika EC2 sebagai ejen Jenkins
- **Azure VMs**: Urus mesin maya Azure sebagai ejen Jenkins
- **Kubernetes**: Urus pod Kubernetes sebagai ejen Jenkins
- **Oracle Cloud**: Urus tika Oracle Cloud Infrastructure (OCI) sebagai ejen Jenkins
- **DigitalOcean**: Urus droplet DigitalOcean sebagai ejen Jenkins

## Ciri Akan Datang

- **Penyepaduan Google Cloud**: Urus ejen Jenkins pada Google Cloud Platform
- **Sokongan AWS ECS/Fargate**: Sokongan untuk ejen berasaskan kontena dalam AWS
- **Pengurusan Berbilang Jenkins**: Alatan yang dipertingkat untuk mengurus berbilang tika Jenkins
- **Migrasi Rentas Awan**: Alatan untuk memigrasikan ejen antara pembekal awan

## Menyumbang

Sumbangan amat dialu-alukan! Sila ikuti [Garis Panduan Sumbangan](../../../CONTRIBUTING.md) kami.

## Keselamatan

Untuk kebimbangan keselamatan, sila semak [Dasar Keselamatan](../../../SECURITY.md) kami.

## Lesen

Projek ini dilesenkan di bawah Lesen MIT - lihat fail [LICENSE](../../../LICENSE) untuk butiran.
