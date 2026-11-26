<!-- translations:start -->
**Languages:** [English](../../../README.md) · **मराठी** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · **मराठी** · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Jenkins वातावरणातील विविध कार्ये स्वयंचलित आणि सुलभ करण्यासाठी तयार केलेल्या Groovy युटिलिटीज आणि स्क्रिप्ट्सचा संग्रह. ही लायब्ररी Jenkins-सुसंगत पद्धतींचे पालन करते आणि त्यात युनिट व इंटिग्रेशन चाचण्यांचा समावेश आहे. आधुनिक वातावरणासाठी Jenkins ऑटोमेशनला समर्थन देते.

## वैशिष्ट्ये

- **जॉब व्यवस्थापन**: Jenkins जॉब्स सक्षम/अक्षम करणे, स्वच्छ करणे, स्थलांतरित करणे आणि टेम्प्लेट तयार करणे
- **क्लाउड एकत्रीकरण**: AWS, Azure आणि Kubernetes वर Jenkins एजंट्सचे व्यवस्थापन करणे
- **सुरक्षा**: Jenkins इन्स्टन्सेसचे ऑडिट करणे आणि सुरक्षित करणे
- **कार्यप्रदर्शन सुधारणा**: Jenkins च्या कार्यप्रदर्शनाचे निरीक्षण आणि सुधारणा करणे
- **कॉन्फिगरेशन व्यवस्थापन**: Jenkins कॉन्फिगरेशनचा बॅकअप घेणे आणि पुनर्संचयित करणे
- **पाइपलाइन निर्मिती**: पुनर्वापरयोग्य टेम्प्लेट्समधून Jenkins पाइपलाइन्स तयार करणे

## प्रकल्प रचना

हा प्रकल्प मानक Groovy प्रकल्प रचनेचे पालन करतो:

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

## इन्स्टॉलेशन

ही लायब्ररी दोन प्रकारे वापरता येते:

1. **तुमच्या Gradle/Maven प्रकल्पात अवलंबन (dependency) म्हणून**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jenkins मध्ये थेट चालवण्यासाठी स्वतंत्र स्क्रिप्ट्स म्हणून**:

   `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` मधील स्क्रिप्ट्स थेट Jenkins Script Console मध्ये किंवा Jenkins CLI द्वारे चालवता येतात.

## वापर

### कमांड-लाइन स्क्रिप्ट्स

वापराची माहिती प्रदर्शित करण्यासाठी सर्व कमांड-लाइन स्क्रिप्ट्स `--help` पर्यायाला समर्थन देतात.

#### जॉब व्यवस्थापन

##### जॉब स्वच्छता

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### जॉब सक्षम/अक्षम करणे

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### जॉब स्थलांतरण

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### जॉब टेम्प्लेटिंग

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### पाइपलाइन निर्मिती

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

#### नोड व्यवस्थापन

##### नोड्सची यादी पाहणे

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### नोड कार्ये

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### क्लाउड प्रदाता व्यवस्थापन

##### AWS व्यवस्थापन

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure व्यवस्थापन

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes व्यवस्थापन

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins इन्स्टन्स व्यवस्थापन

##### स्थिती आणि सुरक्षा

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### कॉन्फिगरेशन व्यवस्थापन

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### प्रोग्रामॅटिक API

तुमच्या स्वतःच्या Groovy स्क्रिप्ट्समध्ये वापरण्यासाठी ही लायब्ररी प्रोग्रामॅटिक API देखील प्रदान करते:

#### जॉब व्यवस्थापन

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

#### क्लाउड व्यवस्थापन

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

### प्रकल्प बिल्ड करणे

```bash
./gradlew build
```

### चाचण्या चालवणे

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **टीप**: JFFI नेटिव्ह अवलंबनामध्ये (jffi-1.2.17-native.jar) एक ज्ञात समस्या आहे ज्यामुळे चाचण्या अयशस्वी होऊ शकतात. तुम्हाला ही समस्या आल्यास, तुम्ही यासह चाचण्या वगळू शकता:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> GitHub Actions चाचण्या सध्या समस्याग्रस्त चाचण्या वगळण्यासाठी कॉन्फिगर केल्या आहेत.

#### Docker चाचणी वातावरण

विविध Java आवृत्त्यांसह सुसंगतता पडताळण्यासाठी या प्रकल्पात Docker-आधारित चाचणी वातावरणाचा समावेश आहे:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

इंटिग्रेशन चाचण्या चाचणीसाठी तात्पुरता Jenkins इन्स्टन्स तयार करण्यासाठी JenkinsRule वापरतात, त्यामुळे तुम्हाला चालू Jenkins सर्व्हरची आवश्यकता नाही. या चाचण्या स्क्रिप्ट्स खऱ्या Jenkins APIs आणि डेटा स्ट्रक्चर्ससह योग्य प्रकारे कार्य करतात याची पडताळणी करतात. अधिक तपशीलांसाठी [इंटिग्रेशन चाचणी दस्तऐवजीकरण](../../../src/integration-test/README.md) पहा.

### कोड गुणवत्ता

हा प्रकल्प Groovy कोड गुणवत्ता तपासणीसाठी CodeNarc वापरतो:

```bash
./gradlew codenarcMain codenarcTest
```

## उपलब्ध क्लाउड एकत्रीकरणे

ही लायब्ररी पुढील क्लाउड प्रदात्यांना समर्थन देते:

- **AWS EC2**: Jenkins एजंट्स म्हणून EC2 इन्स्टन्सेसचे व्यवस्थापन करा
- **Azure VMs**: Jenkins एजंट्स म्हणून Azure व्हर्च्युअल मशीन्सचे व्यवस्थापन करा
- **Kubernetes**: Jenkins एजंट्स म्हणून Kubernetes पॉड्सचे व्यवस्थापन करा
- **Oracle Cloud**: Jenkins एजंट्स म्हणून Oracle Cloud Infrastructure (OCI) इन्स्टन्सेसचे व्यवस्थापन करा
- **DigitalOcean**: Jenkins एजंट्स म्हणून DigitalOcean ड्रॉपलेट्सचे व्यवस्थापन करा

## आगामी वैशिष्ट्ये

- **Google Cloud एकत्रीकरण**: Google Cloud Platform वर Jenkins एजंट्सचे व्यवस्थापन करा
- **AWS ECS/Fargate समर्थन**: AWS मध्ये कंटेनर-आधारित एजंट्ससाठी समर्थन
- **बहु-Jenkins व्यवस्थापन**: अनेक Jenkins इन्स्टन्सेस व्यवस्थापित करण्यासाठी प्रगत साधने
- **क्रॉस-क्लाउड स्थलांतरण**: क्लाउड प्रदात्यांमध्ये एजंट्स स्थलांतरित करण्यासाठी साधने

## योगदान

योगदानाचे स्वागत आहे! कृपया आमच्या [योगदान मार्गदर्शक तत्त्वांचे](../../../CONTRIBUTING.md) पालन करा.

## सुरक्षा

सुरक्षाविषयक चिंतेसाठी, कृपया आमचे [सुरक्षा धोरण](../../../SECURITY.md) पहा.

## परवाना

हा प्रकल्प MIT परवान्याअंतर्गत परवानाकृत आहे - तपशीलांसाठी [LICENSE](../../../LICENSE) फाइल पहा.
