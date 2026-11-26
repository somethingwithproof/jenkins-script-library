<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Yorùbá** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · **Yorùbá** · [isiZulu](../zu-ZA/README.md)
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

Àkójọpọ̀ àwọn irinṣẹ́ àti ìwé-àṣẹ Groovy tí a ṣe láti sọ onírúurú iṣẹ́ di aládàáṣe àti láti mú wọn rọrùn nínú àwọn àyíká Jenkins. Àkójọpọ̀ yìí tẹ̀lé àwọn ìṣe tí ó bá Jenkins mu, ó sì ní àwọn ìdánwò ẹyọkan àti ìdánwò ìsopọ̀ nínú. Ó ń ṣètìlẹ́yìn fún ìṣiṣẹ́ aládàáṣe Jenkins fún àwọn àyíká òde òní.

## Àwọn ẹ̀yà-iṣẹ́

- **Ìṣàkóso Iṣẹ́**: Tan/pa, fọ̀ mọ́, ṣe ìṣíkiri, kí o sì ṣẹ̀dá àwòkọ́ṣe fún àwọn iṣẹ́ Jenkins
- **Ìsopọ̀ Àwọsánmà**: Ṣàkóso àwọn aṣojú Jenkins lórí AWS, Azure, àti Kubernetes
- **Ààbò**: Ṣe àyẹ̀wò àti dáàbò bo àwọn àpẹẹrẹ Jenkins
- **Ìmúdára Iṣẹ́-ṣíṣe**: Ṣe àbójútó kí o sì mú iṣẹ́-ṣíṣe Jenkins sunwọ̀n sí i
- **Ìṣàkóso Ìṣètò**: Ṣe àdàkọ ààbò kí o sì dá àwọn ìṣètò Jenkins padà
- **Ìṣẹ̀dá Ọ̀nà-ìṣiṣẹ́**: Ṣẹ̀dá àwọn ọ̀nà-ìṣiṣẹ́ Jenkins láti inú àwọn àwòkọ́ṣe tí a lè tún lò

## Ìtòlẹ́sẹẹsẹ Iṣẹ́ Àkànṣe

Iṣẹ́ àkànṣe náà tẹ̀lé ìtòlẹ́sẹẹsẹ iṣẹ́ àkànṣe Groovy tí ó wọ́pọ̀:

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

## Ìfisílẹ̀

A lè lo àkójọpọ̀ yìí ní ọ̀nà méjì:

1. **Gẹ́gẹ́ bí ìgbẹ́kẹ̀lé nínú iṣẹ́ àkànṣe Gradle/Maven rẹ**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Gẹ́gẹ́ bí àwọn ìwé-àṣẹ kọ̀ọ̀kan láti ṣiṣẹ́ tààrà nínú Jenkins**:

   A lè ṣiṣẹ́ àwọn ìwé-àṣẹ tí ó wà nínú `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` tààrà nínú Jenkins Script Console tàbí nípasẹ̀ Jenkins CLI.

## Ìlò

### Àwọn Ìwé-àṣẹ Ìlà-àṣẹ

Gbogbo àwọn ìwé-àṣẹ ìlà-àṣẹ ń ṣètìlẹ́yìn fún àṣàyàn `--help` láti fi ìsọfúnni ìlò hàn.

#### Ìṣàkóso Iṣẹ́

##### Ìwẹ̀nùmọ́ Iṣẹ́

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Títan/Pípa Iṣẹ́

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Ìṣíkiri Iṣẹ́

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Ìṣẹ̀dá Àwòkọ́ṣe Iṣẹ́

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Ìṣẹ̀dá Ọ̀nà-ìṣiṣẹ́

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Ìtúpalẹ̀ Iṣẹ́

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Ìṣàkóso Nódù

##### Kíkọ Àkójọ Àwọn Nódù

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Àwọn Ìṣiṣẹ́ Nódù

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Ìṣàkóso Olùpèsè Àwọsánmà

##### Ìṣàkóso AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Ìṣàkóso Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Ìṣàkóso Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Ìṣàkóso Àpẹẹrẹ Jenkins

##### Ìlera àti Ààbò

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Ìṣàkóso Ìṣètò

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API Ìṣètò-èdè

Àkójọpọ̀ náà tún ń pèsè API ìṣètò-èdè fún ìlò nínú àwọn ìwé-àṣẹ Groovy tìrẹ fúnra rẹ:

#### Ìṣàkóso Iṣẹ́

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

#### Ìṣàkóso Àwọsánmà

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

## Ìdàgbàsókè

### Kíkọ Iṣẹ́ Àkànṣe

```bash
./gradlew build
```

### Ṣíṣiṣẹ́ Àwọn Ìdánwò

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Àkíyèsí**: Ìṣòro tí a mọ̀ wà pẹ̀lú ìgbẹ́kẹ̀lé abínibí JFFI (jffi-1.2.17-native.jar) tí ó lè fa ìkùnà ìdánwò. Tí o bá pàdé èyí, o lè fo àwọn ìdánwò náà pẹ̀lú:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Àwọn ìdánwò GitHub Actions wà ní ìṣètò lọ́wọ́lọ́wọ́ láti fo àwọn ìdánwò tí ó ní ìṣòro.

#### Àyíká Ìdánwò Docker

Iṣẹ́ àkànṣe náà ní àyíká ìdánwò tí ó dá lórí Docker láti jẹ́rìísí ìbáramu pẹ̀lú àwọn ẹ̀yà Java ọ̀tọ̀ọ̀tọ̀:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Àwọn ìdánwò ìsopọ̀ ń lo JenkinsRule láti ṣẹ̀dá àpẹẹrẹ Jenkins ìgbàdíẹ̀ fún ìdánwò, nítorí náà o kò nílò ẹ̀rọ-ìpèsè Jenkins tí ń ṣiṣẹ́. Àwọn ìdánwò wọ̀nyí ń jẹ́rìísí pé àwọn ìwé-àṣẹ náà ń ṣiṣẹ́ déédéé pẹ̀lú àwọn API Jenkins gidi àti àwọn ètò dátà. Wo [ìwé-àkọsílẹ̀ ìdánwò ìsopọ̀](../../../src/integration-test/README.md) fún àwọn àlàyé sí i.

### Dídára Kóòdù

Iṣẹ́ àkànṣe yìí ń lo CodeNarc fún àwọn ìṣàyẹ̀wò dídára kóòdù Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Àwọn Ìsopọ̀ Àwọsánmà Tí Ó Wà

Àkójọpọ̀ náà ń ṣètìlẹ́yìn fún àwọn olùpèsè àwọsánmà wọ̀nyí:

- **AWS EC2**: Ṣàkóso àwọn àpẹẹrẹ EC2 gẹ́gẹ́ bí àwọn aṣojú Jenkins
- **Azure VMs**: Ṣàkóso àwọn ẹ̀rọ àfọwọ́ṣe Azure gẹ́gẹ́ bí àwọn aṣojú Jenkins
- **Kubernetes**: Ṣàkóso àwọn pọ́ọ̀dù Kubernetes gẹ́gẹ́ bí àwọn aṣojú Jenkins
- **Oracle Cloud**: Ṣàkóso àwọn àpẹẹrẹ Oracle Cloud Infrastructure (OCI) gẹ́gẹ́ bí àwọn aṣojú Jenkins
- **DigitalOcean**: Ṣàkóso àwọn drọ́púlẹ́ẹ̀tì DigitalOcean gẹ́gẹ́ bí àwọn aṣojú Jenkins

## Àwọn Ẹ̀yà-iṣẹ́ Tí Ń Bọ̀

- **Ìsopọ̀ Google Cloud**: Ṣàkóso àwọn aṣojú Jenkins lórí Google Cloud Platform
- **Àtìlẹ́yìn AWS ECS/Fargate**: Àtìlẹ́yìn fún àwọn aṣojú tí ó dá lórí àpótí-ẹ̀rọ nínú AWS
- **Ìṣàkóso Ọ̀pọ̀-Jenkins**: Àwọn irinṣẹ́ tí a mú sunwọ̀n sí i fún ṣíṣàkóso ọ̀pọ̀lọpọ̀ àpẹẹrẹ Jenkins
- **Ìṣíkiri Láàárín Àwọsánmà**: Àwọn irinṣẹ́ fún ṣíṣe ìṣíkiri àwọn aṣojú láàárín àwọn olùpèsè àwọsánmà

## Ìkópa

Àwọn ìkópa ṣe ìtẹ́wọ́gbà! Jọ̀wọ́ tẹ̀lé [Àwọn Ìtọ́sọ́nà Ìkópa](../../../CONTRIBUTING.md) wa.

## Ààbò

Fún àwọn àníyàn ààbò, jọ̀wọ́ ṣàyẹ̀wò [Ìlànà Ààbò](../../../SECURITY.md) wa.

## Ìwé-àṣẹ

A fún iṣẹ́ àkànṣe yìí ní ìwé-àṣẹ lábẹ́ Ìwé-àṣẹ MIT - wo fáìlì [LICENSE](../../../LICENSE) fún àwọn àlàyé.
