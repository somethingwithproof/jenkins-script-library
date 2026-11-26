<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Afrikaans** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** **Afrikaans** · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
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

'n Versameling Groovy-nutsmiddels en -skripte ontwerp om verskeie bedrywighede binne Jenkins-omgewings te outomatiseer en te vergemaklik. Hierdie biblioteek volg Jenkins-versoenbare praktyke en sluit eenheids- en integrasietoetse in. Ondersteun Jenkins-outomatisering vir moderne omgewings.

## Funksies

- **Taakbestuur**: Aktiveer/deaktiveer, maak skoon, migreer en sjabloniseer Jenkins-take
- **Wolkintegrasie**: Bestuur Jenkins-agente op AWS, Azure en Kubernetes
- **Sekuriteit**: Oudit en beveilig Jenkins-instansies
- **Werkverrigting-optimalisering**: Monitor en verbeter Jenkins-werkverrigting
- **Konfigurasiebestuur**: Rugsteun en herstel Jenkins-konfigurasies
- **Pyplyngenerering**: Skep Jenkins-pyplyne vanaf herbruikbare sjablone

## Projekstruktuur

Die projek volg 'n standaard Groovy-projekstruktuur:

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

## Installasie

Hierdie biblioteek kan op twee maniere gebruik word:

1. **As 'n afhanklikheid in jou Gradle/Maven-projek**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **As individuele skripte om direk in Jenkins uit te voer**:

   Die skripte in `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` kan direk in die Jenkins Script Console of deur middel van die Jenkins CLI uitgevoer word.

## Gebruik

### Opdragreëlskripte

Alle opdragreëlskripte ondersteun die `--help`-opsie om gebruiksinligting te vertoon.

#### Taakbestuur

##### Taakopruiming

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Taakaktivering/-deaktivering

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Taakmigrasie

Afstandsmigrasie is nie beskikbaar nie. Selfs met ’n teiken-URL gebruik die skrip die huidige Jenkins-beheerder.

##### Taaksjablonering

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Pyplyngenerering

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Taakanalise

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Nodusbestuur

##### Lys van nodusse

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Nodusbedrywighede

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Wolkverskafferbestuur

##### AWS-bestuur

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure-bestuur

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes-bestuur

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins-instansiebestuur

##### Gesondheid en sekuriteit

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Konfigurasiebestuur

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### Programmatiese API

Die biblioteek bied ook 'n programmatiese API vir gebruik in jou eie Groovy-skripte:

JAR → [`util`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/util), [`pipeline`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/pipeline)

`src/main/groovy` → [`jobs`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/jobs), [`cloud`](../../../src/main/groovy/com/github/thomasvincent/jenkinsscripts/cloud)

#### Taakbestuur

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

#### Wolkbestuur

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

## Ontwikkeling

### Bou van die projek

```bash
./gradlew build
```

### Uitvoer van toetse

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **Let wel**: Daar is 'n bekende probleem met die JFFI inheemse afhanklikheid (jffi-1.2.17-native.jar) wat toetsmislukkings kan veroorsaak. As jy dit teëkom, kan jy toetse oorslaan met:
>
> ```bash
> ./gradlew build -x test
> ```
>
> Die GitHub Actions-toetse is tans gekonfigureer om problematiese toetse oor te slaan.

#### Docker-toetsomgewing

[`java17`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:24-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

### Kodekwaliteit

Hierdie projek gebruik CodeNarc vir Groovy-kodekwaliteitskontroles:

```bash
./gradlew codenarcMain codenarcTest
```

## Beskikbare wolkintegrasies

Die biblioteek ondersteun die volgende wolkverskaffers:

- **AWS EC2**: Bestuur EC2-instansies as Jenkins-agente
- **Azure VMs**: Bestuur Azure virtuele masjiene as Jenkins-agente
- **Kubernetes**: Bestuur Kubernetes-pods as Jenkins-agente
- **Oracle Cloud**: Bestuur Oracle Cloud Infrastructure (OCI)-instansies as Jenkins-agente
- **DigitalOcean**: Bestuur DigitalOcean-droplets as Jenkins-agente

## Komende funksies

- **Google Cloud-integrasie**: Bestuur Jenkins-agente op Google Cloud Platform
- **AWS ECS/Fargate-ondersteuning**: Ondersteuning vir houergebaseerde agente in AWS
- **Multi-Jenkins-bestuur**: Verbeterde nutsmiddels vir die bestuur van veelvuldige Jenkins-instansies
- **Kruiswolk-migrasie**: Nutsmiddels vir die migrasie van agente tussen wolkverskaffers

## Bydraes

Bydraes is welkom! Volg asseblief ons [Bydraeriglyne](../../../CONTRIBUTING.md).

## Sekuriteit

Vir sekuriteitskwessies, hersien asseblief ons [Sekuriteitsbeleid](../../../SECURITY.md).

## Lisensie

Hierdie projek word onder die MIT-lisensie gelisensieer - sien die [LICENSE](../../../LICENSE)-lêer vir besonderhede.
