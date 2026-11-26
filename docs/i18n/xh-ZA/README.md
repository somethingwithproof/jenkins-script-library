<!-- translations:start -->
**Languages:** [English](../../../README.md) · **isiXhosa** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · **isiXhosa** · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Ithala leZikripthi ze-Jenkins

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Ingqokelela yezixhobo nezikripthi ze-Groovy ezenzelwe ukwenza kuzenzekele nokuququzelela imisebenzi eyahlukeneyo kwiimeko ze-Jenkins. Eli thala lilandela izenzo ezihambelana ne-Jenkins kwaye liquka uvavanyo lweeyunithi nolodibaniso. Lixhasa ukwenza kuzenzekele kwe-Jenkins kwiimeko zangoku.

## Iimpawu

- **Ulawulo lweMisebenzi**: Vula/cima, coca, fudusa, kwaye wenze iitemplate zemisebenzi ye-Jenkins
- **Udibaniso lweLifu**: Lawula ii-arhente ze-Jenkins kwi-AWS, i-Azure, kunye ne-Kubernetes
- **Ukhuseleko**: Hlola kwaye ukhusele iimeko ze-Jenkins
- **Ukuphuculwa kweNtsebenzo**: Beka iliso kwaye uphucule ukusebenza kwe-Jenkins
- **Ulawulo loQwalaselo**: Gcina iikopi zokhuseleko kwaye ubuyisele uqwalaselo lwe-Jenkins
- **Ukuveliswa kweePipeline**: Yenza iipipeline ze-Jenkins ukusuka kwiitemplate ezinokuphinda zisetyenziswe

## Ulwakhiwo lweProjekthi

Iprojekthi ilandela ulwakhiwo oluqhelekileyo lweprojekthi ye-Groovy:

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

## Ukufakelwa

Eli thala linokusetyenziswa ngeendlela ezimbini:

1. **Njengento exhomekeke kuyo kwiprojekthi yakho ye-Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Njengezikripthi ezizimeleyo eziqhutywa ngokuthe ngqo kwi-Jenkins**:

   Izikripthi ezikwi-`src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` zinokuqhutywa ngokuthe ngqo kwi-Jenkins Script Console okanye nge-Jenkins CLI.

## Ukusetyenziswa

### Izikripthi zomgca wemiyalelo

Zonke izikripthi zomgca wemiyalelo zixhasa ukhetho lwe-`--help` ukubonisa ulwazi lokusetyenziswa.

#### Ulawulo lweMisebenzi

##### Ukucocwa kweMisebenzi

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Ukuvula/Ukucima iMisebenzi

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Ukufuduswa kweMisebenzi

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Ukwenza iiTemplate zeMisebenzi

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Ukuveliswa kweePipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Uhlalutyo lweMisebenzi

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Ulawulo lweeNode

##### Ukudwelisa iiNode

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Imisebenzi yeeNode

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Ulawulo lwabaBoneleli beLifu

##### Ulawulo lwe-AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Ulawulo lwe-Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Ulawulo lwe-Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Ulawulo lweeMeko ze-Jenkins

##### Impilo noKhuseleko

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Ulawulo loQwalaselo

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### I-API yeeNkqubo

Eli thala likwanikezela nge-API yeenkqubo yokusetyenziswa kwizikripthi zakho ze-Groovy:

#### Ulawulo lweMisebenzi

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

#### Ulawulo lweLifu

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

## Uphuhliso

### Ukwakha iProjekthi

```bash
./gradlew build
```

### Ukuqhuba uVavanyo

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Qaphela**: Kukho ingxaki eyaziwayo ngento exhomekeke kuyo yemveli ye-JFFI (jffi-1.2.17-native.jar) enokubangela ukusilela kovavanyo. Ukuba udibana nale ngxaki, ungatsiba uvavanyo nge:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Uvavanyo lwe-GitHub Actions luqwalaselwe ngoku ukuba lutsibe uvavanyo olunengxaki.

#### Imeko yoVavanyo ye-Docker

Iprojekthi iquka imeko yovavanyo esekelwe kwi-Docker yokuqinisekisa ukuhambelana neenguqulelo ezahlukeneyo ze-Java:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Uvavanyo lodibaniso lusebenzisa i-JenkinsRule ukwenza imeko yethutyana ye-Jenkins yovavanyo, ngoko awudingi mncedisi we-Jenkins osebenzayo. Olu vavanyo luqinisekisa ukuba izikripthi zisebenza ngokuchanekileyo nge-API zokwenene ze-Jenkins nezakhiwo zedatha. Jonga [amaxwebhu ovavanyo lodibaniso](../../../src/integration-test/README.md) ngeenkcukacha ezithe vetshe.

### Umgangatho weKhowudi

Le projekthi isebenzisa i-CodeNarc ukuze ihlole umgangatho wekhowudi ye-Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Udibaniso lweLifu oluFumanekayo

Eli thala lixhasa aba baboneleli belifu balandelayo:

- **AWS EC2**: Lawula iimeko ze-EC2 njengee-arhente ze-Jenkins
- **Azure VMs**: Lawula ii-virtual machine ze-Azure njengee-arhente ze-Jenkins
- **Kubernetes**: Lawula ii-pod ze-Kubernetes njengee-arhente ze-Jenkins
- **Oracle Cloud**: Lawula iimeko ze-Oracle Cloud Infrastructure (OCI) njengee-arhente ze-Jenkins
- **DigitalOcean**: Lawula ii-droplet ze-DigitalOcean njengee-arhente ze-Jenkins

## Iimpawu ezizayo

- **Udibaniso lwe-Google Cloud**: Lawula ii-arhente ze-Jenkins kwi-Google Cloud Platform
- **Inkxaso ye-AWS ECS/Fargate**: Inkxaso yee-arhente ezisekelwe kwizikhongozeli kwi-AWS
- **Ulawulo lwee-Jenkins ezininzi**: Izixhobo eziphuculweyo zokulawula iimeko ezininzi ze-Jenkins
- **Ukufuduswa phakathi kwamafu**: Izixhobo zokufudusa ii-arhente phakathi kwababoneleli belifu

## Ukufaka isandla

Igalelo lamkelekile! Nceda ulandele [Izikhokelo zethu zokufaka isandla](../../../CONTRIBUTING.md).

## Ukhuseleko

Ngeenkxalabo zokhuseleko, nceda uphonononge [Umgaqo-nkqubo wethu wokhuseleko](../../../SECURITY.md).

## Ilayisenisi

Le projekthi inelayisenisi phantsi kweLayisenisi ye-MIT - jonga ifayile ye-[LICENSE](../../../LICENSE) ngeenkcukacha.
