<!-- translations:start -->
**Languages:** [English](../../../README.md) · **فارسی** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · **فارسی** · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

مجموعه‌ای از ابزارها و اسکریپت‌های Groovy که برای خودکارسازی و تسهیل عملیات گوناگون در محیط‌های Jenkins طراحی شده‌اند. این کتابخانه از شیوه‌های سازگار با Jenkins پیروی می‌کند و شامل آزمون‌های واحد و یکپارچه‌سازی است. از خودکارسازی Jenkins برای محیط‌های مدرن پشتیبانی می‌کند.

## قابلیت‌ها

- **مدیریت کارها (Jobs)**: فعال‌سازی/غیرفعال‌سازی، پاک‌سازی، مهاجرت و قالب‌سازی کارهای Jenkins
- **یکپارچه‌سازی ابری**: مدیریت عامل‌های (agents) Jenkins روی AWS، Azure و Kubernetes
- **امنیت**: ممیزی و ایمن‌سازی نمونه‌های Jenkins
- **بهینه‌سازی کارایی**: پایش و بهبود کارایی Jenkins
- **مدیریت پیکربندی**: پشتیبان‌گیری و بازیابی پیکربندی‌های Jenkins
- **تولید پایپ‌لاین**: ساخت پایپ‌لاین‌های Jenkins از قالب‌های قابل استفاده مجدد

## ساختار پروژه

این پروژه از ساختار استاندارد پروژه‌های Groovy پیروی می‌کند:

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

## نصب

این کتابخانه را می‌توان به دو روش استفاده کرد:

1. **به‌عنوان وابستگی در پروژه Gradle/Maven شما**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **به‌صورت اسکریپت‌های جداگانه برای اجرای مستقیم در Jenkins**:

   اسکریپت‌های موجود در `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` را می‌توان به‌طور مستقیم در Jenkins Script Console یا از طریق Jenkins CLI اجرا کرد.

## نحوه استفاده

### اسکریپت‌های خط فرمان

همه اسکریپت‌های خط فرمان از گزینه `--help` برای نمایش اطلاعات نحوه استفاده پشتیبانی می‌کنند.

#### مدیریت کارها (Jobs)

##### پاک‌سازی کارها

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### فعال‌سازی و غیرفعال‌سازی کارها

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### مهاجرت کارها

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### قالب‌سازی کارها

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### تولید پایپ‌لاین

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### تحلیل کارها

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### مدیریت گره‌ها

##### فهرست کردن گره‌ها

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### عملیات گره‌ها

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### مدیریت ارائه‌دهندگان ابری

##### مدیریت AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### مدیریت Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### مدیریت Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### مدیریت نمونه‌های Jenkins

##### سلامت و امنیت

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### مدیریت پیکربندی

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### رابط برنامه‌نویسی (API)

این کتابخانه همچنین یک API برنامه‌نویسی برای استفاده در اسکریپت‌های Groovy خودتان ارائه می‌دهد:

#### مدیریت کارها (Jobs)

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

#### مدیریت ابر

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

## توسعه

### ساخت پروژه

```bash
./gradlew build
```

### اجرای آزمون‌ها

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **توجه**: یک مشکل شناخته‌شده در وابستگی بومی JFFI (فایل jffi-1.2.17-native.jar) وجود دارد که ممکن است باعث شکست آزمون‌ها شود. در صورت مواجهه با این مشکل، می‌توانید آزمون‌ها را با دستور زیر رد کنید:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> آزمون‌های GitHub Actions در حال حاضر به‌گونه‌ای پیکربندی شده‌اند که آزمون‌های مشکل‌ساز را رد کنند.

#### محیط آزمون Docker

این پروژه شامل یک محیط آزمون مبتنی بر Docker برای بررسی سازگاری با نسخه‌های مختلف Java است:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

آزمون‌های یکپارچه‌سازی از JenkinsRule برای ایجاد یک نمونه موقت Jenkins جهت آزمون استفاده می‌کنند، بنابراین نیازی به یک سرور Jenkins در حال اجرا ندارید. این آزمون‌ها تأیید می‌کنند که اسکریپت‌ها به‌درستی با APIها و ساختارهای داده واقعی Jenkins کار می‌کنند. برای جزئیات بیشتر [مستندات آزمون یکپارچه‌سازی](../../../src/integration-test/README.md) را ببینید.

### کیفیت کد

این پروژه از CodeNarc برای بررسی کیفیت کد Groovy استفاده می‌کند:

```bash
./gradlew codenarcMain codenarcTest
```

## یکپارچه‌سازی‌های ابری موجود

این کتابخانه از ارائه‌دهندگان ابری زیر پشتیبانی می‌کند:

- **AWS EC2**: مدیریت نمونه‌های EC2 به‌عنوان عامل‌های Jenkins
- **Azure VMs**: مدیریت ماشین‌های مجازی Azure به‌عنوان عامل‌های Jenkins
- **Kubernetes**: مدیریت پادهای Kubernetes به‌عنوان عامل‌های Jenkins
- **Oracle Cloud**: مدیریت نمونه‌های Oracle Cloud Infrastructure (OCI) به‌عنوان عامل‌های Jenkins
- **DigitalOcean**: مدیریت dropletهای DigitalOcean به‌عنوان عامل‌های Jenkins

## قابلیت‌های آتی

- **یکپارچه‌سازی با Google Cloud**: مدیریت عامل‌های Jenkins روی Google Cloud Platform
- **پشتیبانی از AWS ECS/Fargate**: پشتیبانی از عامل‌های مبتنی بر کانتینر در AWS
- **مدیریت چندگانه Jenkins**: ابزارهای پیشرفته برای مدیریت چندین نمونه Jenkins
- **مهاجرت بین‌ابری**: ابزارهایی برای انتقال عامل‌ها بین ارائه‌دهندگان ابری

## مشارکت

از مشارکت شما استقبال می‌کنیم! لطفاً از [راهنمای مشارکت](../../../CONTRIBUTING.md) ما پیروی کنید.

## امنیت

برای مسائل امنیتی، لطفاً [سیاست امنیتی](../../../SECURITY.md) ما را بررسی کنید.

## مجوز

این پروژه تحت مجوز MIT منتشر شده است - برای جزئیات فایل [LICENSE](../../../LICENSE) را ببینید.
