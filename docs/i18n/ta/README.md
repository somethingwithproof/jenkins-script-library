<!-- translations:start -->
**Languages:** [English](../../../README.md) · **தமிழ்** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · **தமிழ்** · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Jenkins சூழல்களில் பல்வேறு செயல்பாடுகளைத் தானியக்கமாக்கவும் எளிதாக்கவும் வடிவமைக்கப்பட்ட Groovy பயன்பாடுகள் மற்றும் ஸ்கிரிப்டுகளின் தொகுப்பு. இந்த நூலகம் Jenkins-இணக்கமான நடைமுறைகளைப் பின்பற்றுகிறது மற்றும் அலகு மற்றும் ஒருங்கிணைப்புச் சோதனைகளை உள்ளடக்கியுள்ளது. நவீன சூழல்களுக்கான Jenkins தானியக்கத்தை ஆதரிக்கிறது.

## அம்சங்கள்

- **பணி மேலாண்மை**: Jenkins பணிகளை இயக்குதல்/முடக்குதல், சுத்தம் செய்தல், இடம்பெயர்த்தல் மற்றும் வார்ப்புருவாக்குதல்
- **மேகக்கணி ஒருங்கிணைப்பு**: AWS, Azure மற்றும் Kubernetes ஆகியவற்றில் Jenkins முகவர்களை நிர்வகித்தல்
- **பாதுகாப்பு**: Jenkins நிகழ்வுகளைத் தணிக்கை செய்தல் மற்றும் பாதுகாத்தல்
- **செயல்திறன் மேம்பாடு**: Jenkins செயல்திறனைக் கண்காணித்தல் மற்றும் மேம்படுத்துதல்
- **கட்டமைப்பு மேலாண்மை**: Jenkins கட்டமைப்புகளைக் காப்புப்பிரதி எடுத்தல் மற்றும் மீட்டெடுத்தல்
- **குழாய்வழி உருவாக்கம்**: மறுபயன்பாடு செய்யக்கூடிய வார்ப்புருக்களிலிருந்து Jenkins குழாய்வழிகளை (pipelines) உருவாக்குதல்

## திட்டக் கட்டமைப்பு

இந்தத் திட்டம் நிலையான Groovy திட்டக் கட்டமைப்பைப் பின்பற்றுகிறது:

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

## நிறுவல்

இந்த நூலகத்தை இரண்டு வழிகளில் பயன்படுத்தலாம்:

1. **உங்கள் Gradle/Maven திட்டத்தில் ஒரு சார்பாக**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jenkins-இல் நேரடியாக இயக்க தனிப்பட்ட ஸ்கிரிப்டுகளாக**:

   `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/`-இல் உள்ள ஸ்கிரிப்டுகளை Jenkins Script Console-இல் அல்லது Jenkins CLI மூலம் நேரடியாக இயக்கலாம்.

## பயன்பாடு

### கட்டளை-வரி ஸ்கிரிப்டுகள்

அனைத்து கட்டளை-வரி ஸ்கிரிப்டுகளும் பயன்பாட்டுத் தகவலைக் காட்ட `--help` விருப்பத்தை ஆதரிக்கின்றன.

#### பணி மேலாண்மை

##### பணி சுத்திகரிப்பு

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### பணி இயக்குதல்/முடக்குதல்

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### பணி இடம்பெயர்வு

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### பணி வார்ப்புருவாக்கம்

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### குழாய்வழி உருவாக்கம்

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### பணி பகுப்பாய்வு

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### முனை மேலாண்மை

##### முனைகளைப் பட்டியலிடுதல்

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### முனைச் செயல்பாடுகள்

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### மேகக்கணி வழங்குநர் மேலாண்மை

##### AWS மேலாண்மை

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure மேலாண்மை

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes மேலாண்மை

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins நிகழ்வு மேலாண்மை

##### ஆரோக்கியம் மற்றும் பாதுகாப்பு

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### கட்டமைப்பு மேலாண்மை

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### நிரல் API

உங்கள் சொந்த Groovy ஸ்கிரிப்டுகளில் பயன்படுத்துவதற்கான நிரல் API-ஐயும் இந்த நூலகம் வழங்குகிறது:

#### பணி மேலாண்மை

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

#### மேகக்கணி மேலாண்மை

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

## மேம்பாடு

### திட்டத்தை உருவாக்குதல்

```bash
./gradlew build
```

### சோதனைகளை இயக்குதல்

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **குறிப்பு**: JFFI இயல்பான சார்பில் (jffi-1.2.17-native.jar) சோதனைத் தோல்விகளை ஏற்படுத்தக்கூடிய அறியப்பட்ட சிக்கல் உள்ளது. இதை நீங்கள் எதிர்கொண்டால், பின்வருமாறு சோதனைகளைத் தவிர்க்கலாம்:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> GitHub Actions சோதனைகள் தற்போது சிக்கலான சோதனைகளைத் தவிர்க்கும் வகையில் கட்டமைக்கப்பட்டுள்ளன.

#### Docker சோதனைச் சூழல்

வெவ்வேறு Java பதிப்புகளுடனான இணக்கத்தன்மையைச் சரிபார்க்க, இந்தத் திட்டத்தில் Docker-அடிப்படையிலான சோதனைச் சூழல் உள்ளது:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

ஒருங்கிணைப்புச் சோதனைகள் சோதனைக்காகத் தற்காலிக Jenkins நிகழ்வை உருவாக்க JenkinsRule-ஐப் பயன்படுத்துகின்றன; எனவே இயங்கும் Jenkins சேவையகம் உங்களுக்குத் தேவையில்லை. உண்மையான Jenkins API-கள் மற்றும் தரவுக் கட்டமைப்புகளுடன் ஸ்கிரிப்டுகள் சரியாகச் செயல்படுகின்றன என்பதை இந்தச் சோதனைகள் உறுதிப்படுத்துகின்றன. மேலும் விவரங்களுக்கு [ஒருங்கிணைப்புச் சோதனை ஆவணத்தைப்](../../../src/integration-test/README.md) பார்க்கவும்.

### குறியீட்டுத் தரம்

இந்தத் திட்டம் Groovy குறியீட்டுத் தரச் சரிபார்ப்புகளுக்கு CodeNarc-ஐப் பயன்படுத்துகிறது:

```bash
./gradlew codenarcMain codenarcTest
```

## கிடைக்கும் மேகக்கணி ஒருங்கிணைப்புகள்

இந்த நூலகம் பின்வரும் மேகக்கணி வழங்குநர்களை ஆதரிக்கிறது:

- **AWS EC2**: EC2 நிகழ்வுகளை Jenkins முகவர்களாக நிர்வகித்தல்
- **Azure VMs**: Azure மெய்நிகர் கணினிகளை Jenkins முகவர்களாக நிர்வகித்தல்
- **Kubernetes**: Kubernetes பாட்களை Jenkins முகவர்களாக நிர்வகித்தல்
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI) நிகழ்வுகளை Jenkins முகவர்களாக நிர்வகித்தல்
- **DigitalOcean**: DigitalOcean டிராப்லெட்டுகளை Jenkins முகவர்களாக நிர்வகித்தல்

## வரவிருக்கும் அம்சங்கள்

- **Google Cloud ஒருங்கிணைப்பு**: Google Cloud Platform-இல் Jenkins முகவர்களை நிர்வகித்தல்
- **AWS ECS/Fargate ஆதரவு**: AWS-இல் கொள்கலன்-அடிப்படையிலான முகவர்களுக்கான ஆதரவு
- **பல-Jenkins மேலாண்மை**: பல Jenkins நிகழ்வுகளை நிர்வகிப்பதற்கான மேம்படுத்தப்பட்ட கருவிகள்
- **மேகக்கணி-இடையேயான இடம்பெயர்வு**: மேகக்கணி வழங்குநர்களுக்கு இடையே முகவர்களை இடம்பெயர்ப்பதற்கான கருவிகள்

## பங்களித்தல்

பங்களிப்புகள் வரவேற்கப்படுகின்றன! தயவுசெய்து எங்கள் [பங்களிப்பு வழிகாட்டுதல்களைப்](../../../CONTRIBUTING.md) பின்பற்றவும்.

## பாதுகாப்பு

பாதுகாப்பு குறித்த கவலைகளுக்கு, தயவுசெய்து எங்கள் [பாதுகாப்புக் கொள்கையை](../../../SECURITY.md) மதிப்பாய்வு செய்யவும்.

## உரிமம்

இந்தத் திட்டம் MIT உரிமத்தின் கீழ் உரிமம் பெற்றுள்ளது - விவரங்களுக்கு [LICENSE](../../../LICENSE) கோப்பைப் பார்க்கவும்.
