<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Kiswahili** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · **Kiswahili** · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Mkusanyiko wa zana na hati za Groovy zilizoundwa kuotomatisha na kurahisisha shughuli mbalimbali katika mazingira ya Jenkins. Maktaba hii hufuata mbinu zinazolingana na Jenkins na inajumuisha majaribio ya vitengo na ujumuishaji. Inasaidia uotomatishaji wa Jenkins kwa mazingira ya kisasa.

## Vipengele

- **Usimamizi wa Kazi (Jobs)**: Wezesha/zima, safisha, hamisha, na unda violezo vya kazi za Jenkins
- **Muunganisho wa Wingu**: Simamia mawakala (agents) wa Jenkins kwenye AWS, Azure, na Kubernetes
- **Usalama**: Kagua na linda mifumo ya Jenkins
- **Uboreshaji wa Utendaji**: Fuatilia na boresha utendaji wa Jenkins
- **Usimamizi wa Usanidi**: Hifadhi nakala rudufu na rejesha usanidi wa Jenkins
- **Uundaji wa Pipeline**: Unda pipeline za Jenkins kutoka violezo vinavyoweza kutumika tena

## Muundo wa Mradi

Mradi unafuata muundo wa kawaida wa mradi wa Groovy:

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

## Usakinishaji

Maktaba hii inaweza kutumika kwa njia mbili:

1. **Kama kitegemezi katika mradi wako wa Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Kama hati za binafsi za kuendesha moja kwa moja katika Jenkins**:

   Hati zilizo katika `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` zinaweza kuendeshwa moja kwa moja kwenye Jenkins Script Console au kupitia Jenkins CLI.

## Matumizi

### Hati za Mstari wa Amri

Hati zote za mstari wa amri zinakubali chaguo la `--help` kuonyesha maelezo ya matumizi.

#### Usimamizi wa Kazi

##### Usafishaji wa Kazi

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Kuwezesha/Kuzima Kazi

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Uhamishaji wa Kazi

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Uundaji wa Violezo vya Kazi

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Uundaji wa Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Uchambuzi wa Kazi

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Usimamizi wa Nodi

##### Kuorodhesha Nodi

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Shughuli za Nodi

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Usimamizi wa Watoa Huduma za Wingu

##### Usimamizi wa AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Usimamizi wa Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Usimamizi wa Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Usimamizi wa Mifumo ya Jenkins

##### Afya na Usalama

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Usimamizi wa Usanidi

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API ya Kiprogramu

Maktaba pia hutoa API ya kiprogramu kwa matumizi katika hati zako za Groovy:

#### Usimamizi wa Kazi

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

#### Usimamizi wa Wingu

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

## Uendelezaji

### Kujenga Mradi

```bash
./gradlew build
```

### Kuendesha Majaribio

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Kumbuka**: Kuna tatizo linalojulikana na kitegemezi asili cha JFFI (jffi-1.2.17-native.jar) ambacho kinaweza kusababisha majaribio kushindwa. Ukikutana na hili, unaweza kuruka majaribio kwa:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Majaribio ya GitHub Actions kwa sasa yamesanidiwa kuruka majaribio yenye matatizo.

#### Mazingira ya Majaribio ya Docker

Mradi unajumuisha mazingira ya majaribio yanayotegemea Docker ili kuthibitisha uoanifu na matoleo tofauti ya Java:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Majaribio ya ujumuishaji hutumia JenkinsRule kuunda mfumo wa muda wa Jenkins kwa ajili ya majaribio, hivyo huhitaji seva ya Jenkins inayoendesha. Majaribio haya huthibitisha kuwa hati zinafanya kazi ipasavyo na API halisi za Jenkins na miundo ya data. Tazama [nyaraka za majaribio ya ujumuishaji](../../../src/integration-test/README.md) kwa maelezo zaidi.

### Ubora wa Msimbo

Mradi huu hutumia CodeNarc kwa ukaguzi wa ubora wa msimbo wa Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Miunganisho ya Wingu Inayopatikana

Maktaba inasaidia watoa huduma wa wingu wafuatao:

- **AWS EC2**: Simamia mifumo ya EC2 kama mawakala wa Jenkins
- **Azure VMs**: Simamia mashine pepe za Azure kama mawakala wa Jenkins
- **Kubernetes**: Simamia podi za Kubernetes kama mawakala wa Jenkins
- **Oracle Cloud**: Simamia mifumo ya Oracle Cloud Infrastructure (OCI) kama mawakala wa Jenkins
- **DigitalOcean**: Simamia droplets za DigitalOcean kama mawakala wa Jenkins

## Vipengele Vijavyo

- **Muunganisho wa Google Cloud**: Simamia mawakala wa Jenkins kwenye Google Cloud Platform
- **Usaidizi wa AWS ECS/Fargate**: Usaidizi wa mawakala wanaotegemea kontena katika AWS
- **Usimamizi wa Jenkins Nyingi**: Zana zilizoboreshwa za kusimamia mifumo mingi ya Jenkins
- **Uhamishaji Kati ya Mawingu**: Zana za kuhamisha mawakala kati ya watoa huduma wa wingu

## Kuchangia

Michango inakaribishwa! Tafadhali fuata [Miongozo yetu ya Kuchangia](../../../CONTRIBUTING.md).

## Usalama

Kwa masuala ya usalama, tafadhali kagua [Sera yetu ya Usalama](../../../SECURITY.md).

## Leseni

Mradi huu una leseni chini ya Leseni ya MIT - tazama faili ya [LICENSE](../../../LICENSE) kwa maelezo zaidi.
