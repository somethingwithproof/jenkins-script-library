<!-- translations:start -->
**Languages:** [English](../../../README.md) · **العربية (مصر)** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · **العربية (مصر)** · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

مجموعة من أدوات وسكربتات Groovy المصمّمة علشان تأتمت وتسهّل عمليات مختلفة جوه بيئات Jenkins. المكتبة دي بتتبع ممارسات متوافقة مع Jenkins وبتضم اختبارات وحدة (unit tests) واختبارات تكامل (integration tests)، وبتدعم أتمتة Jenkins للبيئات الحديثة.

## الميزات

- **إدارة المهام (Jobs)**: تفعيل وتعطيل، وتنظيف، وترحيل، وتحويل مهام Jenkins لقوالب
- **التكامل السحابي**: إدارة وكلاء (agents) Jenkins على AWS وAzure وKubernetes
- **الأمان**: فحص وتأمين نسخ Jenkins
- **تحسين الأداء**: مراقبة وتحسين أداء Jenkins
- **إدارة الإعدادات**: نسخ احتياطي واستعادة لإعدادات Jenkins
- **توليد الـ Pipelines**: إنشاء Jenkins pipelines من قوالب قابلة لإعادة الاستخدام

## هيكل المشروع

المشروع بيتبع هيكل مشاريع Groovy القياسي:

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

## التثبيت

تقدر تستخدم المكتبة دي بطريقتين:

1. **كتبعية (dependency) في مشروع Gradle أو Maven بتاعك**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **كسكربتات منفصلة تتشغّل مباشرة جوه Jenkins**:

   السكربتات الموجودة في `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` تقدر تشغّلها مباشرة في Jenkins Script Console أو عن طريق Jenkins CLI.

## الاستخدام

### سكربتات سطر الأوامر

كل سكربتات سطر الأوامر بتدعم خيار `--help` علشان تعرض معلومات الاستخدام.

#### إدارة المهام (Jobs)

##### تنظيف المهام

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### تفعيل وتعطيل المهام

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### ترحيل المهام

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### تحويل المهام لقوالب

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### توليد الـ Pipelines

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### تحليل المهام

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### إدارة العُقد (Nodes)

##### عرض قائمة العُقد

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### عمليات العُقد

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### إدارة مزوّدي السحابة

##### إدارة AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### إدارة Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### إدارة Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### إدارة نسخة Jenkins

##### الحالة والأمان

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### إدارة الإعدادات

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### الواجهة البرمجية (Programmatic API)

المكتبة كمان بتوفّر واجهة برمجية (API) تقدر تستخدمها في سكربتات Groovy بتاعتك:

#### إدارة المهام

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

#### إدارة السحابة

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

## التطوير

### بناء المشروع

```bash
./gradlew build
```

### تشغيل الاختبارات

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **ملحوظة**: فيه مشكلة معروفة مع تبعية JFFI الأصلية (jffi-1.2.17-native.jar) ممكن تسبب فشل في الاختبارات. لو قابلتك المشكلة دي، تقدر تتخطى الاختبارات باستخدام:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> اختبارات GitHub Actions مضبوطة حاليًا إنها تتخطى الاختبارات اللي فيها مشاكل.

#### بيئة الاختبار بـ Docker

المشروع فيه بيئة اختبار مبنية على Docker علشان تتأكد من التوافق مع إصدارات Java المختلفة:

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

بيئة الاختبار بـ Docker بتتضمن:

- تنزيل التبعيات مسبقًا علشان تشغيل الاختبارات يبقى أسرع
- تخزين مؤقت (volume caching) لتبعيات Gradle وMaven
- إعدادات أساسية مشتركة علشان البيئة تفضل متسقة
- دعم لنطاقات اختبار مختلفة (minimal، وdefault، وall)
- حدود للذاكرة وفحوصات صحة (health checks) لحاويات Docker

#### التوافق مع Java

المكتبة دي بتدعم إصدارات Java التالية:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

بنحافظ على التوافق مع Java 8 علشان ندعم تثبيتات Jenkins القديمة. المكتبة بتتجمع (compiled) بخيارات التوافق مع Java 8 بشكل افتراضي.

اختبارات التكامل بتستخدم JenkinsRule علشان تعمل نسخة Jenkins مؤقتة للاختبار، فمش هتحتاج سيرفر Jenkins شغّال. الاختبارات دي بتتأكد إن السكربتات شغالة صح مع واجهات برمجة التطبيقات (APIs) وهياكل البيانات الحقيقية لـ Jenkins. راجع [توثيق اختبارات التكامل](../../../src/integration-test/README.md) لتفاصيل أكتر.

### جودة الكود

المشروع ده بيستخدم CodeNarc لفحوصات جودة كود Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## التكاملات السحابية المتاحة

المكتبة بتدعم مزوّدي السحابة دول:

- **AWS EC2**: إدارة نسخ EC2 كوكلاء (agents) لـ Jenkins
- **Azure VMs**: إدارة الأجهزة الافتراضية على Azure كوكلاء لـ Jenkins
- **Kubernetes**: إدارة Kubernetes pods كوكلاء لـ Jenkins
- **Oracle Cloud**: إدارة نسخ Oracle Cloud Infrastructure (OCI) كوكلاء لـ Jenkins
- **DigitalOcean**: إدارة DigitalOcean droplets كوكلاء لـ Jenkins

## الميزات القادمة

- **تكامل Google Cloud**: إدارة وكلاء Jenkins على Google Cloud Platform
- **دعم AWS ECS/Fargate**: دعم للوكلاء المبنيين على الحاويات في AWS
- **إدارة أكتر من Jenkins**: أدوات محسّنة لإدارة أكتر من نسخة Jenkins
- **الترحيل بين السحابات**: أدوات لترحيل الوكلاء بين مزوّدي السحابة المختلفين

## المساهمة

المساهمات مرحب بيها! يرجى اتباع [إرشادات المساهمة](../../../CONTRIBUTING.md).

## الأمان

لو عندك أي مخاوف أمنية، يرجى مراجعة [سياسة الأمان](../../../SECURITY.md).

## الرخصة

المشروع ده مرخّص برخصة MIT — راجع ملف [LICENSE](../../../LICENSE) للتفاصيل.
