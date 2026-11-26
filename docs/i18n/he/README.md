<!-- translations:start -->
**Languages:** [English](../../../README.md) · **עברית** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · **עברית** · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

אוסף של כלי עזר וסקריפטים ב-Groovy שנועד לאוטומציה והקלה של פעולות שונות בסביבות Jenkins. ספרייה זו פועלת לפי נהלים תואמי Jenkins וכוללת בדיקות יחידה ואינטגרציה. תומכת באוטומציית Jenkins עבור סביבות מודרניות.

## תכונות

- **ניהול משימות (Jobs)**: הפעלה/השבתה, ניקוי, הגירה ויצירת תבניות למשימות Jenkins
- **אינטגרציית ענן**: ניהול סוכני Jenkins על גבי AWS, Azure ו-Kubernetes
- **אבטחה**: ביקורת ואבטחה של מופעי Jenkins
- **אופטימיזציית ביצועים**: ניטור ושיפור ביצועי Jenkins
- **ניהול תצורה**: גיבוי ושחזור של תצורות Jenkins
- **יצירת צינורות (Pipelines)**: יצירת Jenkins pipelines מתבניות לשימוש חוזר

## מבנה הפרויקט

הפרויקט עוקב אחר מבנה פרויקט Groovy תקני:

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

## התקנה

ניתן להשתמש בספרייה זו בשתי דרכים:

1. **כתלות בפרויקט Gradle/Maven שלכם**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **כסקריפטים בודדים להרצה ישירה ב-Jenkins**:

   ניתן להריץ את הסקריפטים בתיקייה `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` ישירות ב-Jenkins Script Console או באמצעות Jenkins CLI.

## שימוש

### סקריפטים לשורת הפקודה

כל סקריפטי שורת הפקודה תומכים באפשרות `--help` להצגת מידע על אופן השימוש.

#### ניהול משימות (Jobs)

##### ניקוי משימות

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### הפעלה והשבתה של משימות

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### הגירת משימות

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### יצירת תבניות למשימות

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### יצירת צינורות (Pipelines)

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### ניתוח משימות

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### ניהול צמתים (Nodes)

##### הצגת רשימת צמתים

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### פעולות על צמתים

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### ניהול ספקי ענן

##### ניהול AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### ניהול Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### ניהול Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### ניהול מופעי Jenkins

##### תקינות ואבטחה

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### ניהול תצורה

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### ממשק API תכנותי

הספרייה מספקת גם ממשק API תכנותי לשימוש בסקריפטי Groovy משלכם:

#### ניהול משימות

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

boolean migrateBetween(Jenkins source, Jenkins target) {
    if (source.is(target)) {
        throw new IllegalArgumentException("Source and target must be distinct Jenkins instances")
    }
    return new JobMigrator(source, target).migrateJob("source-job", "target-job")
}
```

#### ניהול ענן

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

## פיתוח

### בניית הפרויקט

```bash
./gradlew build
```

### הרצת בדיקות

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **הערה**: קיימת בעיה מוכרת עם התלות המקומית של JFFI (jffi-1.2.17-native.jar) שעלולה לגרום לכשלי בדיקות. אם אתם נתקלים בכך, ניתן לדלג על הבדיקות באמצעות:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> הבדיקות ב-GitHub Actions מוגדרות כעת לדלג על בדיקות בעייתיות.

#### סביבת בדיקות ב-Docker

הפרויקט כולל סביבת בדיקות מבוססת Docker לאימות תאימות עם גרסאות Java שונות:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

בדיקות האינטגרציה משתמשות ב-JenkinsRule כדי ליצור מופע Jenkins זמני לבדיקה, כך שאין צורך בשרת Jenkins פועל. בדיקות אלה מאמתות שהסקריפטים פועלים כראוי מול ממשקי API ומבני נתונים אמיתיים של Jenkins. ראו [תיעוד בדיקות האינטגרציה](../../../src/integration-test/README.md) לפרטים נוספים.

### איכות קוד

פרויקט זה משתמש ב-CodeNarc לבדיקות איכות קוד ב-Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## אינטגרציות ענן זמינות

הספרייה תומכת בספקי הענן הבאים:

- **AWS EC2**: ניהול מופעי EC2 כסוכני Jenkins
- **Azure VMs**: ניהול מכונות וירטואליות של Azure כסוכני Jenkins
- **Kubernetes**: ניהול פודים של Kubernetes כסוכני Jenkins
- **Oracle Cloud**: ניהול מופעי Oracle Cloud Infrastructure (OCI) כסוכני Jenkins
- **DigitalOcean**: ניהול droplets של DigitalOcean כסוכני Jenkins

## תכונות עתידיות

- **אינטגרציה עם Google Cloud**: ניהול סוכני Jenkins ב-Google Cloud Platform
- **תמיכה ב-AWS ECS/Fargate**: תמיכה בסוכנים מבוססי מכולות ב-AWS
- **ניהול ריבוי Jenkins**: כלים משופרים לניהול מספר מופעי Jenkins
- **הגירה חוצת-עננים**: כלים להגירת סוכנים בין ספקי ענן

## תרומה לפרויקט

תרומות מתקבלות בברכה! אנא פעלו לפי [הנחיות התרומה](../../../CONTRIBUTING.md).

## אבטחה

בנושאי אבטחה, אנא עיינו ב[מדיניות האבטחה](../../../SECURITY.md).

## רישיון

פרויקט זה מופץ תחת רישיון MIT - ראו את הקובץ [LICENSE](../../../LICENSE) לפרטים.
