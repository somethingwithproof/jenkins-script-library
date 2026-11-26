<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Українська** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · **Українська** · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Набір утиліт і скриптів на Groovy, призначених для автоматизації та спрощення різноманітних операцій у середовищах Jenkins. Бібліотека дотримується практик, сумісних із Jenkins, і містить модульні та інтеграційні тести. Підтримує автоматизацію Jenkins для сучасних середовищ.

## Можливості

- **Керування завданнями**: увімкнення/вимкнення, очищення, міграція та шаблонізація завдань Jenkins
- **Хмарна інтеграція**: керування агентами Jenkins в AWS, Azure і Kubernetes
- **Безпека**: аудит і посилення захисту інстансів Jenkins
- **Оптимізація продуктивності**: моніторинг і підвищення продуктивності Jenkins
- **Керування конфігурацією**: резервне копіювання та відновлення конфігурацій Jenkins
- **Генерація конвеєрів**: створення конвеєрів Jenkins із багаторазових шаблонів

## Структура проєкту

Проєкт дотримується стандартної структури проєкту Groovy:

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

## Встановлення

Цю бібліотеку можна використовувати двома способами:

1. **Як залежність у вашому проєкті Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Як окремі скрипти для безпосереднього запуску в Jenkins**:

   Скрипти в каталозі `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` можна виконувати безпосередньо в Jenkins Script Console або через Jenkins CLI.

## Використання

### Скрипти командного рядка

Усі скрипти командного рядка підтримують параметр `--help` для виведення довідки щодо використання.

#### Керування завданнями

##### Очищення завдань

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Увімкнення/вимкнення завдань

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Міграція завдань

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Шаблонізація завдань

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Генерація конвеєрів

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Аналіз завдань

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Керування вузлами

##### Список вузлів

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Операції з вузлами

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Керування хмарними провайдерами

##### Керування AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Керування Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Керування Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Керування інстансом Jenkins

##### Стан і безпека

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Керування конфігурацією

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Програмний API

Бібліотека також надає програмний API для використання у власних скриптах Groovy:

#### Керування завданнями

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

#### Керування хмарними ресурсами

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

## Розробка

### Збірка проєкту

```bash
./gradlew build
```

### Запуск тестів

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Примітка**: існує відома проблема з нативною залежністю JFFI (jffi-1.2.17-native.jar), яка може спричиняти збої тестів. Якщо ви зіткнетеся з нею, ви можете пропустити тести командою:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Тести GitHub Actions наразі налаштовано на пропуск проблемних тестів.

#### Тестове середовище Docker

Проєкт містить тестове середовище на основі Docker для перевірки сумісності з різними версіями Java:

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

Тестове середовище Docker включає:

- Попереднє завантаження залежностей для швидшого виконання тестів
- Кешування томів для залежностей Gradle і Maven
- Спільну базову конфігурацію для узгодженого середовища
- Підтримку різних обсягів тестування (minimal, default, all)
- Обмеження пам'яті та перевірки справності для контейнерів Docker

#### Сумісність із Java

Ця бібліотека підтримує такі версії Java:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

Ми підтримуємо зворотну сумісність із Java 8 для підтримки застарілих інсталяцій Jenkins. За замовчуванням бібліотека компілюється з параметрами сумісності Java 8.

Інтеграційні тести використовують JenkinsRule для створення тимчасового інстансу Jenkins під час тестування, тому запущений сервер Jenkins не потрібен. Ці тести перевіряють коректність роботи скриптів із реальними API та структурами даних Jenkins. Докладніше дивіться в [документації з інтеграційного тестування](../../../src/integration-test/README.md).

### Якість коду

Цей проєкт використовує CodeNarc для перевірки якості коду Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Доступні хмарні інтеграції

Бібліотека підтримує таких хмарних провайдерів:

- **AWS EC2**: керування інстансами EC2 як агентами Jenkins
- **Azure VMs**: керування віртуальними машинами Azure як агентами Jenkins
- **Kubernetes**: керування подами Kubernetes як агентами Jenkins
- **Oracle Cloud**: керування інстансами Oracle Cloud Infrastructure (OCI) як агентами Jenkins
- **DigitalOcean**: керування дроплетами DigitalOcean як агентами Jenkins

## Заплановані можливості

- **Інтеграція з Google Cloud**: керування агентами Jenkins у Google Cloud Platform
- **Підтримка AWS ECS/Fargate**: підтримка контейнерних агентів в AWS
- **Керування кількома інстансами Jenkins**: розширені інструменти для керування кількома інстансами Jenkins
- **Міжхмарна міграція**: інструменти для міграції агентів між хмарними провайдерами

## Участь у розробці

Внески вітаються! Дотримуйтеся наших [настанов для учасників](../../../CONTRIBUTING.md).

## Безпека

З питань безпеки ознайомтеся з нашою [політикою безпеки](../../../SECURITY.md).

## Ліцензія

Цей проєкт ліцензовано за ліцензією MIT — подробиці дивіться у файлі [LICENSE](../../../LICENSE).
