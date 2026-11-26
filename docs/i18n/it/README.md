<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Italiano** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · **Italiano** · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

Una raccolta di utility e script Groovy progettati per automatizzare e semplificare diverse operazioni negli ambienti Jenkins. Questa libreria segue le best practice compatibili con Jenkins e include test unitari e di integrazione. Supporta l'automazione Jenkins per ambienti moderni.

## Funzionalità

- **Gestione dei job**: Abilita/disabilita, pulisce, migra e crea template per i job Jenkins
- **Integrazione cloud**: Gestisce gli agent Jenkins su AWS, Azure e Kubernetes
- **Sicurezza**: Esegue audit e mette in sicurezza le istanze Jenkins
- **Ottimizzazione delle prestazioni**: Monitora e migliora le prestazioni di Jenkins
- **Gestione della configurazione**: Esegue il backup e il ripristino delle configurazioni Jenkins
- **Generazione di pipeline**: Crea pipeline Jenkins da template riutilizzabili

## Struttura del progetto

Il progetto segue la struttura standard dei progetti Groovy:

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

## Installazione

Questa libreria può essere utilizzata in due modi:

1. **Come dipendenza nel tuo progetto Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Come singoli script da eseguire direttamente in Jenkins**:

   Gli script in `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` possono essere eseguiti direttamente nella Jenkins Script Console o tramite la Jenkins CLI.

## Utilizzo

### Script da riga di comando

Tutti gli script da riga di comando supportano l'opzione `--help` per mostrare le informazioni di utilizzo.

#### Gestione dei job

##### Pulizia dei job

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Abilitazione/Disabilitazione dei job

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Migrazione dei job

La migrazione verso un controller remoto non è disponibile. Anche specificando un URL di destinazione, lo script usa il controller Jenkins corrente.

##### Creazione di template per i job

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Generazione di pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Analisi dei job

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Gestione dei nodi

##### Elenco dei nodi

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Operazioni sui nodi

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Gestione dei provider cloud

##### Gestione AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Gestione Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Gestione Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Gestione delle istanze Jenkins

##### Stato di salute e sicurezza

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Gestione della configurazione

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API programmatica

La libreria fornisce anche un'API programmatica da utilizzare nei propri script Groovy:

JAR → [`util`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/util), [`pipeline`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/pipeline)

`src/main/groovy` → [`jobs`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/jobs), [`cloud`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/cloud)

#### Gestione dei job

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

#### Gestione cloud

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

## Sviluppo

### Compilazione del progetto

```bash
./gradlew build
```

### Esecuzione dei test

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Nota**: Esiste un problema noto con la dipendenza nativa JFFI (jffi-1.2.17-native.jar) che può causare errori nei test. Se riscontri questo problema, puoi saltare i test con:
>
> ```bash
> ./gradlew build -x test
> ```
>
> I test di GitHub Actions sono attualmente configurati per saltare i test problematici.

#### Ambiente di test Docker

[`java17`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:24-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

### Qualità del codice

Questo progetto utilizza CodeNarc per i controlli di qualità del codice Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Integrazioni cloud disponibili

La libreria supporta i seguenti provider cloud:

- **AWS EC2**: Gestisce istanze EC2 come agent Jenkins
- **Azure VMs**: Gestisce macchine virtuali Azure come agent Jenkins
- **Kubernetes**: Gestisce pod Kubernetes come agent Jenkins
- **Oracle Cloud**: Gestisce istanze Oracle Cloud Infrastructure (OCI) come agent Jenkins
- **DigitalOcean**: Gestisce droplet DigitalOcean come agent Jenkins

## Funzionalità future

- **Integrazione Google Cloud**: Gestisce agent Jenkins su Google Cloud Platform
- **Supporto AWS ECS/Fargate**: Supporto per agent basati su container in AWS
- **Gestione multi-Jenkins**: Strumenti avanzati per gestire più istanze Jenkins
- **Migrazione cross-cloud**: Strumenti per migrare agent tra diversi provider cloud

## Contribuire

I contributi sono benvenuti! Segui le nostre [Linee guida per i contributi](../../../CONTRIBUTING.md).

## Sicurezza

Per questioni relative alla sicurezza, consulta la nostra [Policy di sicurezza](../../../SECURITY.md).

## Licenza

Questo progetto è distribuito con licenza MIT: consulta il file [LICENSE](../../../LICENSE) file per i dettagli.
