<!-- translations:start -->
**Languages:** [English](../../../README.md) · **العربية (المغرب)** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · **العربية (المغرب)** · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

مجموعة ديال أدوات وسكريبتات Groovy مصاوبة باش تأتمت وتسهل بزاف ديال العمليات فبيئات Jenkins. هاد المكتبة كتبع ممارسات متوافقة مع Jenkins وفيها اختبارات الوحدة والتكامل. كتدعم أتمتة Jenkins للبيئات الحديثة.

## الميزات

- **إدارة المهام (Job Management)**: تفعيل/تعطيل، تنظيف، ترحيل، وتحويل مهام Jenkins لقوالب
- **التكامل السحابي (Cloud Integration)**: إدارة عملاء (agents) ديال Jenkins على AWS، وAzure، وKubernetes
- **الأمان**: تدقيق وتأمين نسخ Jenkins
- **تحسين الأداء**: مراقبة وتحسين أداء Jenkins
- **إدارة الإعدادات**: النسخ الاحتياطي واسترجاع إعدادات Jenkins
- **توليد خطوط الأنابيب (Pipeline Generation)**: إنشاء خطوط أنابيب Jenkins من قوالب قابلة لإعادة الاستعمال

## هيكلة المشروع

المشروع كيتبع هيكلة مشاريع Groovy القياسية:

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

تقدر تستعمل هاد المكتبة بجوج طرق:

1. **كتبعية فمشروع Gradle/Maven ديالك**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **كسكريبتات مستقلة تشغلها مباشرة فـ Jenkins**:

   السكريبتات اللي فـ `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` تقدر تشغلهم مباشرة فـ Jenkins Script Console ولا عبر Jenkins CLI.

## الاستعمال

### سكريبتات سطر الأوامر

جميع سكريبتات سطر الأوامر كتدعم الخيار `--help` باش تعرض معلومات الاستعمال.

#### إدارة المهام

##### تنظيف المهام

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### تفعيل/تعطيل المهام

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

##### توليد خطوط الأنابيب

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

#### إدارة العقد (Nodes)

##### عرض قائمة العقد

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### عمليات العقد

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### إدارة مزودي السحابة

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

##### الصحة والأمان

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

### الواجهة البرمجية (API)

المكتبة كتقدم كذلك واجهة برمجية (API) باش تستعملها فسكريبتات Groovy ديالك:

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

> **ملاحظة**: كاين واحد المشكل معروف مع التبعية الأصلية JFFI (jffi-1.2.17-native.jar) اللي يقدر يسبب فشل الاختبارات. إلى صادفتي هاد المشكل، تقدر تتجاوز الاختبارات بـ:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> اختبارات GitHub Actions مضبوطة حالياً باش تتجاوز الاختبارات اللي فيها مشاكل.

#### بيئة الاختبار بـ Docker

المشروع فيه بيئة اختبار مبنية على Docker باش تتحقق من التوافق مع إصدارات Java المختلفة:

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

بيئة الاختبار بـ Docker فيها:

- تحميل مسبق للتبعيات باش الاختبارات يدوزو أسرع
- تخزين مؤقت بالحجم (Volume caching) لتبعيات Gradle وMaven
- إعدادات أساسية مشتركة لبيئة متناسقة
- دعم نطاقات اختبار مختلفة (minimal، default، all)
- حدود الذاكرة وفحوصات الصحة (health checks) لحاويات Docker

#### توافق Java

هاد المكتبة كتدعم إصدارات Java التالية:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

كنحافظو على التوافق الخلفي مع Java 8 باش ندعمو تثبيتات Jenkins القديمة. المكتبة كتتجمع (compile) بخيارات توافق Java 8 بشكل افتراضي.

اختبارات التكامل كتستعمل JenkinsRule باش تصاوب نسخة Jenkins مؤقتة للاختبار، يعني ما محتاجش يكون عندك سيرفر Jenkins خدام. هاد الاختبارات كيتحققو بلي السكريبتات خدامين مزيان مع واجهات برمجة التطبيقات وهياكل البيانات الحقيقية ديال Jenkins. شوف [وثائق اختبارات التكامل](../../../src/integration-test/README.md) لتفاصيل أكثر.

### جودة الكود

هاد المشروع كيستعمل CodeNarc لفحوصات جودة كود Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## التكاملات السحابية المتوفرة

المكتبة كتدعم مزودي السحابة التاليين:

- **AWS EC2**: إدارة نسخ EC2 كعملاء لـ Jenkins
- **Azure VMs**: إدارة الآلات الافتراضية ديال Azure كعملاء لـ Jenkins
- **Kubernetes**: إدارة pods ديال Kubernetes كعملاء لـ Jenkins
- **Oracle Cloud**: إدارة نسخ Oracle Cloud Infrastructure (OCI) كعملاء لـ Jenkins
- **DigitalOcean**: إدارة droplets ديال DigitalOcean كعملاء لـ Jenkins

## الميزات القادمة

- **تكامل Google Cloud**: إدارة عملاء Jenkins على Google Cloud Platform
- **دعم AWS ECS/Fargate**: دعم العملاء المبنيين على الحاويات فـ AWS
- **إدارة عدة نسخ Jenkins**: أدوات محسنة لإدارة عدة نسخ Jenkins
- **الترحيل بين السحابات**: أدوات لترحيل العملاء بين مزودي السحابة

## المساهمة

المساهمات مرحب بيها! عافاك تبع [إرشادات المساهمة](../../../CONTRIBUTING.md) ديالنا.

## الأمان

لأي مسائل متعلقة بالأمان، راجع [سياسة الأمان](../../../SECURITY.md) ديالنا.

## الرخصة

هاد المشروع مرخص تحت رخصة MIT - شوف ملف [LICENSE](../../../LICENSE) للتفاصيل.
