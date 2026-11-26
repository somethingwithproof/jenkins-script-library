<!-- translations:start -->
**Languages:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · **हिन्दी**

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · **हिन्दी** · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

Jenkins परिवेशों में विभिन्न परिचालनों को स्वचालित और सुगम बनाने के लिए डिज़ाइन की गई Groovy उपयोगिताओं और स्क्रिप्ट का एक संग्रह। यह लाइब्रेरी Jenkins-संगत प्रथाओं का पालन करती है और इसमें यूनिट तथा एकीकरण परीक्षण शामिल हैं। आधुनिक परिवेशों के लिए Jenkins स्वचालन का समर्थन करती है।

## विशेषताएँ

- **जॉब प्रबंधन**: Jenkins जॉब को सक्षम/अक्षम करें, साफ़ करें, माइग्रेट करें और टेम्पलेट बनाएँ
- **क्लाउड एकीकरण**: AWS, Azure और Kubernetes पर Jenkins एजेंट प्रबंधित करें
- **सुरक्षा**: Jenkins इंस्टेंस का ऑडिट करें और उन्हें सुरक्षित बनाएँ
- **प्रदर्शन अनुकूलन**: Jenkins के प्रदर्शन की निगरानी करें और उसमें सुधार करें
- **कॉन्फ़िगरेशन प्रबंधन**: Jenkins कॉन्फ़िगरेशन का बैकअप लें और उसे पुनर्स्थापित करें
- **पाइपलाइन निर्माण**: पुनः प्रयोज्य टेम्पलेट से Jenkins पाइपलाइन बनाएँ

## प्रोजेक्ट संरचना

यह प्रोजेक्ट मानक Groovy प्रोजेक्ट संरचना का पालन करता है:

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

## इंस्टॉलेशन

इस लाइब्रेरी का उपयोग दो तरीकों से किया जा सकता है:

1. **अपने Gradle/Maven प्रोजेक्ट में निर्भरता के रूप में**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jenkins में सीधे चलाने के लिए अलग-अलग स्क्रिप्ट के रूप में**:

   `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` में मौजूद स्क्रिप्ट को सीधे Jenkins Script Console में या Jenkins CLI के माध्यम से निष्पादित किया जा सकता है।

## उपयोग

### कमांड-लाइन स्क्रिप्ट

सभी कमांड-लाइन स्क्रिप्ट उपयोग की जानकारी प्रदर्शित करने के लिए `--help` विकल्प का समर्थन करती हैं।

#### जॉब प्रबंधन

##### जॉब सफ़ाई

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### जॉब सक्षम/अक्षम करना

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### जॉब माइग्रेशन

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### जॉब टेम्पलेटिंग

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### पाइपलाइन निर्माण

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### जॉब विश्लेषण

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### नोड प्रबंधन

##### नोड सूचीबद्ध करना

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### नोड परिचालन

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### क्लाउड प्रदाता प्रबंधन

##### AWS प्रबंधन

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure प्रबंधन

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes प्रबंधन

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins इंस्टेंस प्रबंधन

##### स्वास्थ्य और सुरक्षा

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### कॉन्फ़िगरेशन प्रबंधन

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### प्रोग्रामेटिक API

यह लाइब्रेरी आपकी अपनी Groovy स्क्रिप्ट में उपयोग के लिए एक प्रोग्रामेटिक API भी प्रदान करती है:

#### जॉब प्रबंधन

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

#### क्लाउड प्रबंधन

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

## विकास

### प्रोजेक्ट बिल्ड करना

```bash
./gradlew build
```

### परीक्षण चलाना

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **नोट**: JFFI नेटिव निर्भरता (jffi-1.2.17-native.jar) के साथ एक ज्ञात समस्या है जो परीक्षण विफलताओं का कारण बन सकती है। यदि आपको यह समस्या आती है, तो आप इसके साथ परीक्षण छोड़ सकते हैं:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> GitHub Actions परीक्षण वर्तमान में समस्याग्रस्त परीक्षणों को छोड़ने के लिए कॉन्फ़िगर किए गए हैं।

#### Docker परीक्षण परिवेश

विभिन्न Java संस्करणों के साथ संगतता सत्यापित करने के लिए प्रोजेक्ट में एक Docker-आधारित परीक्षण परिवेश शामिल है:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

एकीकरण परीक्षण के लिए एक अस्थायी Jenkins इंस्टेंस बनाने हेतु JenkinsRule का उपयोग किया जाता है, इसलिए आपको चल रहे Jenkins सर्वर की आवश्यकता नहीं होती। ये परीक्षण सत्यापित करते हैं कि स्क्रिप्ट वास्तविक Jenkins API और डेटा संरचनाओं के साथ सही ढंग से काम करती हैं। अधिक विवरण के लिए [एकीकरण परीक्षण दस्तावेज़ीकरण](../../../src/integration-test/README.md) देखें।

### कोड गुणवत्ता

यह प्रोजेक्ट Groovy कोड गुणवत्ता जाँच के लिए CodeNarc का उपयोग करता है:

```bash
./gradlew codenarcMain codenarcTest
```

## उपलब्ध क्लाउड एकीकरण

यह लाइब्रेरी निम्नलिखित क्लाउड प्रदाताओं का समर्थन करती है:

- **AWS EC2**: EC2 इंस्टेंस को Jenkins एजेंट के रूप में प्रबंधित करें
- **Azure VMs**: Azure वर्चुअल मशीनों को Jenkins एजेंट के रूप में प्रबंधित करें
- **Kubernetes**: Kubernetes पॉड्स को Jenkins एजेंट के रूप में प्रबंधित करें
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI) इंस्टेंस को Jenkins एजेंट के रूप में प्रबंधित करें
- **DigitalOcean**: DigitalOcean ड्रॉपलेट्स को Jenkins एजेंट के रूप में प्रबंधित करें

## आगामी विशेषताएँ

- **Google Cloud एकीकरण**: Google Cloud Platform पर Jenkins एजेंट प्रबंधित करें
- **AWS ECS/Fargate समर्थन**: AWS में कंटेनर-आधारित एजेंट के लिए समर्थन
- **मल्टी-Jenkins प्रबंधन**: एकाधिक Jenkins इंस्टेंस प्रबंधित करने के लिए उन्नत टूल
- **क्रॉस-क्लाउड माइग्रेशन**: क्लाउड प्रदाताओं के बीच एजेंट माइग्रेट करने के लिए टूल

## योगदान

योगदान का स्वागत है! कृपया हमारे [योगदान दिशानिर्देश](../../../CONTRIBUTING.md) का पालन करें।

## सुरक्षा

सुरक्षा संबंधी चिंताओं के लिए, कृपया हमारी [सुरक्षा नीति](../../../SECURITY.md) की समीक्षा करें।

## लाइसेंस

यह प्रोजेक्ट MIT लाइसेंस के अंतर्गत लाइसेंस प्राप्त है - विवरण के लिए [LICENSE](../../../LICENSE) फ़ाइल देखें।
