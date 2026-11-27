<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Bahasa Indonesia** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · **Bahasa Indonesia**
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

Kumpulan utilitas dan skrip Groovy yang dirancang untuk mengotomatiskan dan mempermudah berbagai operasi di lingkungan Jenkins. Pustaka ini mengikuti praktik yang kompatibel dengan Jenkins serta menyertakan pengujian unit dan integrasi. Mendukung otomatisasi Jenkins untuk lingkungan modern.

## Fitur

- **Manajemen Job**: Mengaktifkan/menonaktifkan, membersihkan, memigrasikan, dan membuat templat job Jenkins
- **Integrasi Cloud**: Mengelola agen Jenkins di AWS, Azure, dan Kubernetes
- **Keamanan**: Mengaudit dan mengamankan instans Jenkins
- **Optimasi Kinerja**: Memantau dan meningkatkan kinerja Jenkins
- **Manajemen Konfigurasi**: Mencadangkan dan memulihkan konfigurasi Jenkins
- **Pembuatan Pipeline**: Membuat pipeline Jenkins dari templat yang dapat digunakan kembali

## Struktur Proyek

Proyek ini mengikuti struktur proyek Groovy standar:

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

Pustaka ini dapat digunakan dengan dua cara:

1. **Sebagai dependensi dalam proyek Gradle/Maven Anda**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Sebagai skrip terpisah untuk dijalankan langsung di Jenkins**:

   Skrip di `src/com/github/thomasvincent/jenkinsscripts/scripts/` dapat dijalankan langsung di Jenkins Script Console atau melalui Jenkins CLI.

## Penggunaan

### Skrip Baris Perintah

Semua skrip baris perintah mendukung opsi `--help` untuk menampilkan informasi penggunaan.

#### Manajemen Job

##### Pembersihan Job

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Pengaktifan/Penonaktifan Job

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Migrasi Job

Migrasi ke pengontrol jarak jauh belum tersedia.

##### Pembuatan Templat Job

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pembuatan Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Analisis Job

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Manajemen Node

##### Menampilkan Daftar Node

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Operasi Node

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Manajemen Penyedia Cloud

##### Manajemen AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Manajemen Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Manajemen Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Manajemen Instans Jenkins

##### Kesehatan dan Keamanan

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Manajemen Konfigurasi

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API Programatik

Pustaka ini juga menyediakan API programatik untuk digunakan dalam skrip Groovy Anda sendiri:

#### Manajemen Job

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

#### Manajemen Cloud

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

## Pengembangan

### Membangun Proyek

```bash
./gradlew build
```

### Menjalankan Pengujian

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Catatan**: Terdapat masalah yang diketahui pada dependensi asli JFFI (jffi-1.2.17-native.jar) yang dapat menyebabkan kegagalan pengujian. Jika Anda mengalaminya, Anda dapat melewati pengujian dengan:
>
> ```bash
> ./gradlew build -x test
> ```
>
> Pengujian GitHub Actions saat ini dikonfigurasi untuk melewati pengujian yang bermasalah.

#### Lingkungan Pengujian Docker

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Kualitas Kode

Proyek ini menggunakan CodeNarc untuk pemeriksaan kualitas kode Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Integrasi Cloud yang Tersedia

Pustaka ini mendukung penyedia cloud berikut:

- **AWS EC2**: Mengelola instans EC2 sebagai agen Jenkins
- **Azure VMs**: Mengelola mesin virtual Azure sebagai agen Jenkins
- **Kubernetes**: Mengelola pod Kubernetes sebagai agen Jenkins
- **Oracle Cloud**: Mengelola instans Oracle Cloud Infrastructure (OCI) sebagai agen Jenkins
- **DigitalOcean**: Mengelola droplet DigitalOcean sebagai agen Jenkins

## Fitur Mendatang

- **Integrasi Google Cloud**: Mengelola agen Jenkins di Google Cloud Platform
- **Dukungan AWS ECS/Fargate**: Dukungan untuk agen berbasis kontainer di AWS
- **Manajemen Multi-Jenkins**: Alat yang disempurnakan untuk mengelola beberapa instans Jenkins
- **Migrasi Lintas Cloud**: Alat untuk memigrasikan agen antar penyedia cloud

## Berkontribusi

Kontribusi sangat diterima! Harap ikuti [Panduan Kontribusi](../../../CONTRIBUTING.md) kami.

## Keamanan

Untuk masalah keamanan, silakan tinjau [Kebijakan Keamanan](../../../SECURITY.md) kami.

## Lisensi

Proyek ini dilisensikan di bawah Lisensi MIT - lihat berkas [LICENSE](../../../LICENSE) untuk rinciannya.
