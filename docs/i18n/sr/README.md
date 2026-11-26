<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Српски** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · **Српски**

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Збирка Groovy алата и скрипти намењених аутоматизацији и олакшавању различитих операција у Jenkins окружењима. Ова библиотека прати праксе компатибилне са Jenkins-ом и обухвата јединичне и интеграционе тестове. Подржава Jenkins аутоматизацију за савремена окружења.

## Функције

- **Управљање пословима**: Омогућавање/онемогућавање, чишћење, миграција и прављење Jenkins послова из шаблона
- **Интеграција са облаком**: Управљање Jenkins агентима на AWS, Azure и Kubernetes платформама
- **Безбедност**: Ревизија и заштита Jenkins инстанци
- **Оптимизација перформанси**: Надзор и унапређење перформанси Jenkins-а
- **Управљање конфигурацијом**: Прављење резервних копија и враћање Jenkins подешавања
- **Генерисање пајплајна**: Креирање Jenkins пајплајна из вишекратно употребљивих шаблона

## Структура пројекта

Пројекат прати стандардну структуру Groovy пројекта:

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

## Инсталација

Ова библиотека може да се користи на два начина:

1. **Као зависност у вашем Gradle/Maven пројекту**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Као појединачне скрипте за директно извршавање у Jenkins-у**:

   Скрипте у `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` могу се извршавати директно у Jenkins Script Console или путем Jenkins CLI алата.

## Коришћење

### Скрипте командне линије

Све скрипте командне линије подржавају опцију `--help` за приказ упутства за коришћење.

#### Управљање пословима

##### Чишћење послова

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Омогућавање/онемогућавање послова

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Миграција послова

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Креирање послова из шаблона

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Генерисање пајплајна

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Анализа послова

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Управљање чворовима

##### Приказ чворова

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Операције над чворовима

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Управљање провајдерима облака

##### Управљање AWS-ом

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Управљање Azure-ом

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Управљање Kubernetes-ом

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Управљање Jenkins инстанцом

##### Стање и безбедност

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Управљање конфигурацијом

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Програмски API

Библиотека такође пружа програмски API за коришћење у вашим Groovy скриптама:

#### Управљање пословима

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

#### Управљање облаком

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

## Развој

### Изградња пројекта

```bash
./gradlew build
```

### Покретање тестова

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Напомена**: Постоји позната грешка са JFFI нативном зависношћу (jffi-1.2.17-native.jar) која може изазвати неуспех тестова. Ако наиђете на тај проблем, тестове можете прескочити помоћу:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> GitHub Actions тестови су тренутно подешени да прескачу проблематичне тестове.

#### Docker окружење за тестирање

Пројекат укључује Docker окружење за тестирање ради провере компатибилности са различитим верзијама Јаве:

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

Docker окружење за тестирање укључује:

- Унапред преузете зависности за брже извршавање тестова
- Кеширање волумена за Gradle и Maven зависности
- Заједничку основну конфигурацију за уједначено окружење
- Подршку за различите опсеге тестова (minimal, default, all)
- Ограничења меморије и провере стања за Docker контејнере

#### Компатибилност са Јавом

Ова библиотека подржава следеће верзије Јаве:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

Одржавамо повратну компатибилност са Јавом 8 ради подршке старијим Jenkins инсталацијама. Библиотека се подразумевано компајлира са опцијама компатибилности за Јаву 8.

Интеграциони тестови користе JenkinsRule за креирање привремене Jenkins инстанце за тестирање, тако да вам није потребан покренут Jenkins сервер. Ови тестови проверавају да скрипте исправно раде са стварним Jenkins API-јима и структурама података. За више детаља погледајте [документацију за интеграционе тестове](../../../src/integration-test/README.md).

### Квалитет кода

Овај пројекат користи CodeNarc за проверу квалитета Groovy кода:

```bash
./gradlew codenarcMain codenarcTest
```

## Доступне интеграције са облаком

Библиотека подржава следеће провајдере облака:

- **AWS EC2**: Управљање EC2 инстанцама као Jenkins агентима
- **Azure VMs**: Управљање Azure виртуелним машинама као Jenkins агентима
- **Kubernetes**: Управљање Kubernetes подовима као Jenkins агентима
- **Oracle Cloud**: Управљање Oracle Cloud Infrastructure (OCI) инстанцама као Jenkins агентима
- **DigitalOcean**: Управљање DigitalOcean дроплетима као Jenkins агентима

## Планиране функције

- **Google Cloud интеграција**: Управљање Jenkins агентима на Google Cloud платформи
- **AWS ECS/Fargate подршка**: Подршка за агенте засноване на контејнерима у AWS-у
- **Управљање са више Jenkins инстанци**: Унапређени алати за управљање са више Jenkins сервера
- **Миграција између облака**: Алати за миграцију агената између провајдера облака

## Доприноси

Доприноси су добродошли! Пратите наше [смернице за доприносе](../../../CONTRIBUTING.md).

## Безбедност

За безбедносна питања прегледајте нашу [безбедносну политику](../../../SECURITY.md).

## Лиценца

Овај пројекат је лиценциран под MIT лиценцом - за детаље погледајте датотеку [LICENSE](../../../LICENSE).
