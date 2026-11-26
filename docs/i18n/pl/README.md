<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Polski** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · **Polski** · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

Zbiór narzędzi i skryptów w języku Groovy przeznaczonych do automatyzacji i usprawniania różnorodnych operacji w środowiskach Jenkins. Biblioteka stosuje praktyki zgodne z Jenkins oraz zawiera testy jednostkowe i integracyjne. Wspiera automatyzację Jenkins w nowoczesnych środowiskach.

## Funkcje

- **Zarządzanie zadaniami**: włączanie/wyłączanie, czyszczenie, migracja i tworzenie szablonów zadań Jenkins
- **Integracja z chmurą**: zarządzanie agentami Jenkins w AWS, Azure i Kubernetes
- **Bezpieczeństwo**: audytowanie i zabezpieczanie instancji Jenkins
- **Optymalizacja wydajności**: monitorowanie i poprawa wydajności Jenkins
- **Zarządzanie konfiguracją**: tworzenie kopii zapasowych i przywracanie konfiguracji Jenkins
- **Generowanie potoków**: tworzenie potoków Jenkins na podstawie szablonów wielokrotnego użytku

## Struktura projektu

Projekt stosuje standardową strukturę projektu Groovy:

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

## Instalacja

Z tej biblioteki można korzystać na dwa sposoby:

1. **Jako zależność w projekcie Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jako pojedyncze skrypty uruchamiane bezpośrednio w Jenkins**:

   Skrypty w katalogu `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` można uruchamiać bezpośrednio w Jenkins Script Console lub poprzez Jenkins CLI.

## Użycie

### Skrypty wiersza poleceń

Wszystkie skrypty wiersza poleceń obsługują opcję `--help` wyświetlającą informacje o użyciu.

#### Zarządzanie zadaniami

##### Czyszczenie zadań

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Włączanie i wyłączanie zadań

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Migracja zadań

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Szablony zadań

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Generowanie potoków

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Analiza zadań

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Zarządzanie węzłami

##### Wyświetlanie listy węzłów

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Operacje na węzłach

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Zarządzanie dostawcami chmury

##### Zarządzanie AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Zarządzanie Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Zarządzanie Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Zarządzanie instancją Jenkins

##### Kondycja i bezpieczeństwo

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Zarządzanie konfiguracją

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API programistyczne

Biblioteka udostępnia również API programistyczne do wykorzystania we własnych skryptach Groovy:

#### Zarządzanie zadaniami

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

// Copy a job between two Jenkins instances provided by the caller
// (for example, an in-process JenkinsRule harness; no remote connector is bundled)
boolean migrateBetween(Jenkins source, Jenkins target) {
    if (source.is(target)) {
        throw new IllegalArgumentException("Source and target must be distinct Jenkins instances")
    }
    return new JobMigrator(source, target).migrateJob("source-job", "target-job")
}
migrateBetween(jenkins, targetJenkins)
```

#### Zarządzanie chmurą

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

## Rozwój

### Budowanie projektu

```bash
./gradlew build
```

### Uruchamianie testów

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Uwaga**: Występuje znany problem z natywną zależnością JFFI (jffi-1.2.17-native.jar), który może powodować błędy testów. W takim przypadku można pominąć testy poleceniem:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Testy w GitHub Actions są obecnie skonfigurowane tak, aby pomijać problematyczne testy.

#### Środowisko testowe Docker

Projekt zawiera środowisko testowe oparte na Dockerze do weryfikacji zgodności z różnymi wersjami Javy:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Testy integracyjne wykorzystują JenkinsRule do tworzenia tymczasowej instancji Jenkins na potrzeby testów, dzięki czemu nie jest wymagany działający serwer Jenkins. Testy te sprawdzają poprawność działania skryptów z rzeczywistymi interfejsami API i strukturami danych Jenkins. Więcej szczegółów znajdziesz w [dokumentacji testów integracyjnych](../../../src/integration-test/README.md).

### Jakość kodu

Projekt używa narzędzia CodeNarc do kontroli jakości kodu Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Dostępne integracje z chmurą

Biblioteka obsługuje następujących dostawców chmury:

- **AWS EC2**: zarządzanie instancjami EC2 jako agentami Jenkins
- **Azure VMs**: zarządzanie maszynami wirtualnymi Azure jako agentami Jenkins
- **Kubernetes**: zarządzanie podami Kubernetes jako agentami Jenkins
- **Oracle Cloud**: zarządzanie instancjami Oracle Cloud Infrastructure (OCI) jako agentami Jenkins
- **DigitalOcean**: zarządzanie dropletami DigitalOcean jako agentami Jenkins

## Planowane funkcje

- **Integracja z Google Cloud**: zarządzanie agentami Jenkins w Google Cloud Platform
- **Obsługa AWS ECS/Fargate**: obsługa agentów kontenerowych w AWS
- **Zarządzanie wieloma instancjami Jenkins**: rozbudowane narzędzia do zarządzania wieloma instancjami Jenkins
- **Migracja między chmurami**: narzędzia do migracji agentów między dostawcami chmury

## Współtworzenie

Wszelki wkład w projekt jest mile widziany! Prosimy o przestrzeganie naszych [wytycznych dotyczących współtworzenia](../../../CONTRIBUTING.md).

## Bezpieczeństwo

W sprawach dotyczących bezpieczeństwa zapoznaj się z naszą [polityką bezpieczeństwa](../../../SECURITY.md).

## Licencja

Projekt jest udostępniany na licencji MIT – szczegóły znajdują się w pliku [LICENSE](../../../LICENSE).
