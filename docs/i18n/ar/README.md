<!-- translations:start -->
**Languages:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · **العربية** · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · **العربية** · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

مجموعة من الأدوات والنصوص البرمجية بلغة Groovy المصمّمة لأتمتة وتسهيل العمليات المختلفة داخل بيئات Jenkins. تتبع هذه المكتبة الممارسات المتوافقة مع Jenkins وتتضمن اختبارات الوحدات واختبارات التكامل، كما تدعم أتمتة Jenkins في البيئات الحديثة.

## الميزات

- **إدارة المهام**: تفعيل مهام Jenkins وتعطيلها وتنظيفها وترحيلها وإنشاء قوالب لها
- **التكامل السحابي**: إدارة وكلاء Jenkins على AWS وAzure وKubernetes
- **الأمان**: تدقيق مثيلات Jenkins وتأمينها
- **تحسين الأداء**: مراقبة أداء Jenkins وتحسينه
- **إدارة التهيئة**: النسخ الاحتياطي لتهيئات Jenkins واستعادتها
- **توليد خطوط الأنابيب**: إنشاء خطوط أنابيب Jenkins من قوالب قابلة لإعادة الاستخدام

## بنية المشروع

يتبع المشروع البنية القياسية لمشاريع Groovy:

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

يمكن استخدام هذه المكتبة بطريقتين:

1. **كتبعية في مشروع Gradle/Maven الخاص بك**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **كنصوص برمجية مستقلة تُشغَّل مباشرةً في Jenkins**:

   يمكن تنفيذ النصوص البرمجية الموجودة في `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` مباشرةً في وحدة تحكم نصوص Jenkins (Jenkins Script Console) أو عبر Jenkins CLI.

## الاستخدام

### نصوص سطر الأوامر

تدعم جميع نصوص سطر الأوامر الخيار `--help` لعرض معلومات الاستخدام.

#### إدارة المهام

##### تنظيف المهام

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### تفعيل المهام وتعطيلها

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

##### إنشاء قوالب للمهام

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

#### إدارة العقد

##### سرد العقد

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

#### إدارة مثيلات Jenkins

##### السلامة والأمان

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### إدارة التهيئة

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### واجهة البرمجة (API)

توفّر المكتبة أيضاً واجهة برمجية لاستخدامها في نصوص Groovy الخاصة بك:

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

> **ملاحظة**: توجد مشكلة معروفة في تبعية JFFI الأصلية (jffi-1.2.17-native.jar) قد تتسبب في فشل الاختبارات. إذا واجهت هذه المشكلة، يمكنك تخطّي الاختبارات باستخدام:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> اختبارات GitHub Actions مهيّأة حالياً لتخطّي الاختبارات التي تسبّب مشكلات.

#### بيئة الاختبار المعتمدة على Docker

يتضمن المشروع بيئة اختبار قائمة على Docker للتحقق من التوافق مع إصدارات Java المختلفة:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

تستخدم اختبارات التكامل JenkinsRule لإنشاء مثيل Jenkins مؤقت للاختبار، بحيث لا تحتاج إلى خادم Jenkins قيد التشغيل. وتتحقق هذه الاختبارات من أن النصوص البرمجية تعمل بصورة صحيحة مع واجهات برمجة Jenkins وهياكل البيانات الحقيقية. راجع [توثيق اختبارات التكامل](../../../src/integration-test/README.md) لمزيد من التفاصيل.

### جودة الشفرة

يستخدم هذا المشروع أداة CodeNarc لفحوصات جودة شفرة Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## تكاملات السحابة المتاحة

تدعم المكتبة مزوّدي السحابة التاليين:

- **AWS EC2**: إدارة مثيلات EC2 كوكلاء Jenkins
- **Azure VMs**: إدارة الأجهزة الافتراضية على Azure كوكلاء Jenkins
- **Kubernetes**: إدارة وحدات Kubernetes (pods) كوكلاء Jenkins
- **Oracle Cloud**: إدارة مثيلات Oracle Cloud Infrastructure (OCI) كوكلاء Jenkins
- **DigitalOcean**: إدارة خوادم DigitalOcean (droplets) كوكلاء Jenkins

## الميزات القادمة

- **التكامل مع Google Cloud**: إدارة وكلاء Jenkins على Google Cloud Platform
- **دعم AWS ECS/Fargate**: دعم الوكلاء المعتمدين على الحاويات في AWS
- **إدارة مثيلات Jenkins المتعددة**: أدوات محسّنة لإدارة عدة مثيلات Jenkins
- **الترحيل عبر السحب**: أدوات لترحيل الوكلاء بين مزوّدي السحابة

## المساهمة

المساهمات مرحّب بها! يُرجى اتباع [إرشادات المساهمة](../../../CONTRIBUTING.md).

## الأمان

للمسائل المتعلقة بالأمان، يُرجى مراجعة [سياسة الأمان](../../../SECURITY.md).

## الترخيص

هذا المشروع مرخّص بموجب رخصة MIT — راجع ملف [LICENSE](../../../LICENSE) للتفاصيل.
