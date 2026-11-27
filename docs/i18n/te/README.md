<!-- translations:start -->
**Languages:** [English](../../../README.md) · **తెలుగు** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · **తెలుగు** · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Jenkins ఎన్విరాన్‌మెంట్లలో వివిధ ఆపరేషన్లను ఆటోమేట్ చేయడానికి మరియు సులభతరం చేయడానికి రూపొందించిన Groovy యుటిలిటీలు మరియు స్క్రిప్ట్‌ల సమాహారం. ఈ లైబ్రరీ Jenkins-అనుకూల పద్ధతులను అనుసరిస్తుంది మరియు యూనిట్ మరియు ఇంటిగ్రేషన్ పరీక్షలను కలిగి ఉంటుంది. ఆధునిక ఎన్విరాన్‌మెంట్ల కోసం Jenkins ఆటోమేషన్‌కు మద్దతు ఇస్తుంది.

## ఫీచర్లు

- **జాబ్ నిర్వహణ**: Jenkins జాబ్‌లను ఎనేబుల్/డిజేబుల్ చేయడం, క్లీన్ చేయడం, మైగ్రేట్ చేయడం మరియు టెంప్లేట్‌లుగా మార్చడం
- **క్లౌడ్ ఇంటిగ్రేషన్**: AWS, Azure మరియు Kubernetesలో Jenkins ఏజెంట్లను నిర్వహించడం
- **భద్రత**: Jenkins ఇన్‌స్టాన్స్‌లను ఆడిట్ చేసి సురక్షితం చేయడం
- **పనితీరు ఆప్టిమైజేషన్**: Jenkins పనితీరును మానిటర్ చేసి మెరుగుపరచడం
- **కాన్ఫిగరేషన్ నిర్వహణ**: Jenkins కాన్ఫిగరేషన్‌లను బ్యాకప్ చేసి పునరుద్ధరించడం
- **పైప్‌లైన్ జనరేషన్**: పునర్వినియోగ టెంప్లేట్‌ల నుండి Jenkins పైప్‌లైన్‌లను సృష్టించడం

## ప్రాజెక్ట్ నిర్మాణం

ఈ ప్రాజెక్ట్ ప్రామాణిక Groovy ప్రాజెక్ట్ నిర్మాణాన్ని అనుసరిస్తుంది:

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

## ఇన్‌స్టాలేషన్

ఈ లైబ్రరీని రెండు విధాలుగా ఉపయోగించవచ్చు:

1. **మీ Gradle/Maven ప్రాజెక్ట్‌లో డిపెండెన్సీగా**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jenkinsలో నేరుగా అమలు చేయడానికి వ్యక్తిగత స్క్రిప్ట్‌లుగా**:

   `src/com/github/thomasvincent/jenkinsscripts/scripts/` లోని స్క్రిప్ట్‌లను Jenkins Script Consoleలో లేదా Jenkins CLI ద్వారా నేరుగా అమలు చేయవచ్చు.

## వినియోగం

### కమాండ్-లైన్ స్క్రిప్ట్‌లు

అన్ని కమాండ్-లైన్ స్క్రిప్ట్‌లు వినియోగ సమాచారాన్ని ప్రదర్శించడానికి `--help` ఆప్షన్‌కు మద్దతు ఇస్తాయి.

#### జాబ్ నిర్వహణ

##### జాబ్ క్లీనింగ్

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### జాబ్ ఎనేబుల్/డిజేబుల్

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### జాబ్ మైగ్రేషన్

రిమోట్ కంట్రోలర్‌కు మైగ్రేషన్ అందుబాటులో లేదు.

##### జాబ్ టెంప్లేటింగ్

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### పైప్‌లైన్ జనరేషన్

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### జాబ్ విశ్లేషణ

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### నోడ్ నిర్వహణ

##### నోడ్‌ల జాబితా

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### నోడ్ ఆపరేషన్లు

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### క్లౌడ్ ప్రొవైడర్ నిర్వహణ

##### AWS నిర్వహణ

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure నిర్వహణ

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes నిర్వహణ

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins ఇన్‌స్టాన్స్ నిర్వహణ

##### ఆరోగ్యం మరియు భద్రత

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### కాన్ఫిగరేషన్ నిర్వహణ

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### ప్రోగ్రామాటిక్ API

మీ స్వంత Groovy స్క్రిప్ట్‌లలో ఉపయోగించడానికి ఈ లైబ్రరీ ప్రోగ్రామాటిక్ APIని కూడా అందిస్తుంది:

#### జాబ్ నిర్వహణ

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

#### క్లౌడ్ నిర్వహణ

```groovy
import com.github.thomasvincent.jenkinsscripts.cloud.AWSNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.AzureNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.KubernetesNodeManager
import com.github.thomasvincent.jenkinsscripts.cloud.OracleCloudNodeManager
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

```

## అభివృద్ధి

### ప్రాజెక్ట్ బిల్డ్ చేయడం

```bash
./gradlew build
```

### పరీక్షలు అమలు చేయడం

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **గమనిక**: JFFI నేటివ్ డిపెండెన్సీ (jffi-1.2.17-native.jar)తో తెలిసిన సమస్య ఉంది, ఇది పరీక్ష వైఫల్యాలకు కారణం కావచ్చు. మీరు దీనిని ఎదుర్కొంటే, ఈ క్రింది విధంగా పరీక్షలను దాటవేయవచ్చు:
>
> ```bash
> ./gradlew build -x test
> ```
>
> GitHub Actions పరీక్షలు ప్రస్తుతం సమస్యాత్మక పరీక్షలను దాటవేసేలా కాన్ఫిగర్ చేయబడ్డాయి.

#### Docker పరీక్షా ఎన్విరాన్‌మెంట్

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### కోడ్ నాణ్యత

ఈ ప్రాజెక్ట్ Groovy కోడ్ నాణ్యత తనిఖీల కోసం CodeNarcను ఉపయోగిస్తుంది:

```bash
./gradlew codenarcMain codenarcTest
```

## అందుబాటులో ఉన్న క్లౌడ్ ఇంటిగ్రేషన్లు

ఈ లైబ్రరీ క్రింది క్లౌడ్ ప్రొవైడర్లకు మద్దతు ఇస్తుంది:

- **AWS EC2**: EC2 ఇన్‌స్టాన్స్‌లను Jenkins ఏజెంట్లుగా నిర్వహించడం
- **Azure VMs**: Azure వర్చువల్ మెషీన్లను Jenkins ఏజెంట్లుగా నిర్వహించడం
- **Kubernetes**: Kubernetes పాడ్‌లను Jenkins ఏజెంట్లుగా నిర్వహించడం
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI) ఇన్‌స్టాన్స్‌లను Jenkins ఏజెంట్లుగా నిర్వహించడం

## రాబోయే ఫీచర్లు

- **Google Cloud ఇంటిగ్రేషన్**: Google Cloud Platformలో Jenkins ఏజెంట్లను నిర్వహించడం
- **AWS ECS/Fargate మద్దతు**: AWSలో కంటైనర్-ఆధారిత ఏజెంట్లకు మద్దతు
- **మల్టీ-Jenkins నిర్వహణ**: బహుళ Jenkins ఇన్‌స్టాన్స్‌లను నిర్వహించడానికి మెరుగైన సాధనాలు
- **క్రాస్-క్లౌడ్ మైగ్రేషన్**: క్లౌడ్ ప్రొవైడర్ల మధ్య ఏజెంట్లను మైగ్రేట్ చేయడానికి సాధనాలు

## సహకారం

సహకారాలు స్వాగతించబడతాయి! దయచేసి మా [సహకార మార్గదర్శకాలు](../../../CONTRIBUTING.md)ను అనుసరించండి.

## భద్రత

భద్రతా సమస్యల కోసం, దయచేసి మా [భద్రతా విధానం](../../../SECURITY.md)ను సమీక్షించండి.

## లైసెన్స్

ఈ ప్రాజెక్ట్ MIT లైసెన్స్ క్రింద లైసెన్స్ పొందింది - వివరాల కోసం [LICENSE](../../../LICENSE) ఫైల్ చూడండి.
