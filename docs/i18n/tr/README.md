<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Türkçe** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · **Türkçe** · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

Jenkins ortamlarındaki çeşitli işlemleri otomatikleştirmek ve kolaylaştırmak için tasarlanmış Groovy yardımcı programları ve betiklerinden oluşan bir koleksiyon. Bu kütüphane Jenkins uyumlu uygulamaları izler ve birim ile entegrasyon testlerini içerir. Modern ortamlar için Jenkins otomasyonunu destekler.

## Özellikler

- **İş Yönetimi**: Jenkins işlerini etkinleştirin/devre dışı bırakın, temizleyin, taşıyın ve şablonlaştırın
- **Bulut Entegrasyonu**: AWS, Azure ve Kubernetes üzerindeki Jenkins aracılarını yönetin
- **Güvenlik**: Jenkins örneklerini denetleyin ve güvenliğini sağlayın
- **Performans Optimizasyonu**: Jenkins performansını izleyin ve iyileştirin
- **Yapılandırma Yönetimi**: Jenkins yapılandırmalarını yedekleyin ve geri yükleyin
- **Pipeline Oluşturma**: Yeniden kullanılabilir şablonlardan Jenkins pipeline'ları oluşturun

## Proje Yapısı

Proje standart Groovy proje yapısını izler:

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

## Kurulum

Bu kütüphane iki şekilde kullanılabilir:

1. **Gradle/Maven projenizde bir bağımlılık olarak**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Doğrudan Jenkins içinde çalıştırılacak bağımsız betikler olarak**:

   `src/com/github/thomasvincent/jenkinsscripts/scripts/` dizinindeki betikler doğrudan Jenkins Script Console üzerinden veya Jenkins CLI aracılığıyla çalıştırılabilir.

## Kullanım

### Komut Satırı Betikleri

Tüm komut satırı betikleri kullanım bilgilerini görüntülemek için `--help` seçeneğini destekler.

#### İş Yönetimi

##### İş Temizleme

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### İş Etkinleştirme/Devre Dışı Bırakma

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### İş Taşıma

Uzak bir denetleyiciye geçiş yapılamaz.

##### İş Şablonlama

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pipeline Oluşturma

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### İş Analizi

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Düğüm Yönetimi

##### Düğümleri Listeleme

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Düğüm İşlemleri

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Bulut Sağlayıcı Yönetimi

##### AWS Yönetimi

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure Yönetimi

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes Yönetimi

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins Örneği Yönetimi

##### Sağlık ve Güvenlik

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Yapılandırma Yönetimi

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programatik API

Kütüphane ayrıca kendi Groovy betiklerinizde kullanmanız için programatik bir API sunar:

#### İş Yönetimi

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

#### Bulut Yönetimi

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

## Geliştirme

### Projeyi Derleme

```bash
./gradlew build
```

### Testleri Çalıştırma

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Not**: Test hatalarına neden olabilecek JFFI yerel bağımlılığı (jffi-1.2.17-native.jar) ile ilgili bilinen bir sorun vardır. Bununla karşılaşırsanız testleri şu şekilde atlayabilirsiniz:
>
> ```bash
> ./gradlew build -x test
> ```
>
> GitHub Actions testleri şu anda sorunlu testleri atlayacak şekilde yapılandırılmıştır.

#### Docker Test Ortamı

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### Kod Kalitesi

Bu proje, Groovy kod kalitesi kontrolleri için CodeNarc kullanır:

```bash
./gradlew codenarcMain codenarcTest
```

## Mevcut Bulut Entegrasyonları

Kütüphane aşağıdaki bulut sağlayıcılarını destekler:

- **AWS EC2**: EC2 örneklerini Jenkins aracıları olarak yönetin
- **Azure VM'ler**: Azure sanal makinelerini Jenkins aracıları olarak yönetin
- **Kubernetes**: Kubernetes pod'larını Jenkins aracıları olarak yönetin
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI) örneklerini Jenkins aracıları olarak yönetin

## Planlanan Özellikler

- **Google Cloud Entegrasyonu**: Google Cloud Platform üzerindeki Jenkins aracılarını yönetin
- **AWS ECS/Fargate Desteği**: AWS'de kapsayıcı tabanlı aracılar için destek
- **Çoklu Jenkins Yönetimi**: Birden fazla Jenkins örneğini yönetmek için gelişmiş araçlar
- **Bulutlar Arası Geçiş**: Aracıları bulut sağlayıcıları arasında taşımak için araçlar

## Katkıda Bulunma

Katkılarınızı memnuniyetle karşılıyoruz! Lütfen [Katkıda Bulunma Kılavuzumuzu](../../../CONTRIBUTING.md) izleyin.

## Güvenlik

Güvenlikle ilgili endişeleriniz için lütfen [Güvenlik Politikamızı](../../../SECURITY.md) inceleyin.

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır - ayrıntılar için [LICENSE](../../../LICENSE) dosyasına bakın.
