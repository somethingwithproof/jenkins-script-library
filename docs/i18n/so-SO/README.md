<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Soomaali** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · **Soomaali** · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
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

Ururinta qalabka iyo qoraallada Groovy ee loogu talagalay inay otomaatig ka dhigaan oo fududeeyaan hawlo kala duwan oo ka dhex jira deegaannada Jenkins. Maktabaddani waxay raacdaa dhaqamada la jaanqaada Jenkins waxayna ka kooban tahay tijaabooyin cutubyo (unit) iyo isku-dhaf (integration). Waxay taageertaa otomaatigga Jenkins ee deegaannada casriga ah.

## Sifooyinka

- **Maamulka Shaqooyinka (Jobs)**: Shid/dami, nadiifi, rari, oo u samee qaab-dhismeed shaqooyinka Jenkins
- **Isku-dhafka Daruurta (Cloud)**: Ku maamul wakiillada Jenkins ee AWS, Azure, iyo Kubernetes
- **Amniga**: Baadh oo sug amniga dhacdooyinka Jenkins
- **Hagaajinta Waxqabadka**: Kormeer oo wanaaji waxqabadka Jenkins
- **Maamulka Habaynta**: Nuqul-kaydi oo soo celi habaynta Jenkins
- **Soo-saaridda Pipeline**: Ka abuur pipeline-yada Jenkins qaab-dhismeedyo dib loo isticmaali karo

## Qaab-dhismeedka Mashruuca

Mashruucu wuxuu raacaa qaab-dhismeedka caadiga ah ee mashruuca Groovy:

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

## Rakibidda

Maktabaddan waxaa loo isticmaali karaa laba hab:

1. **Iyadoo ah ku-tiirsanaan ku jirta mashruucaaga Gradle/Maven**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Iyadoo ah qoraallo gaar-gaar ah oo si toos ah loogu socodsiiyo Jenkins**:

   Qoraallada ku jira `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` waxaa si toos ah loogu fulin karaa Jenkins Script Console ama iyadoo la marayo Jenkins CLI.

## Isticmaalka

### Qoraallada Khadka Amarka (CLI)

Dhammaan qoraallada khadka amarku waxay taageeraan ikhtiyaarka `--help` si ay u soo bandhigaan macluumaadka isticmaalka.

#### Maamulka Shaqooyinka

##### Nadiifinta Shaqooyinka

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Shididda/Daminta Shaqooyinka

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Raridda Shaqooyinka

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Qaab-dhismeedka Shaqooyinka

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Soo-saaridda Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Falanqaynta Shaqooyinka

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Maamulka Noodhadhka

##### Liis-garaynta Noodhadhka

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Hawlaha Noodhadhka

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Maamulka Bixiyeyaasha Daruurta

##### Maamulka AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Maamulka Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Maamulka Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Maamulka Dhacdooyinka Jenkins

##### Caafimaadka iyo Amniga

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Maamulka Habaynta

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API-ga Barnaamijka

Maktabaddu waxay sidoo kale bixisaa API barnaamij oo aad ku isticmaali karto qoraalladaada Groovy:

#### Maamulka Shaqooyinka

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

#### Maamulka Daruurta

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

## Horumarinta

### Dhisidda Mashruuca

```bash
./gradlew build
```

### Socodsiinta Tijaabooyinka

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Fiiro gaar ah**: Waxaa jirta cillad la yaqaan oo la xiriirta ku-tiirsanaanta asalka ah ee JFFI (jffi-1.2.17-native.jar) taas oo keeni karta fashil tijaabo. Haddii aad tan la kulanto, waxaad ka boodi kartaa tijaabooyinka adigoo adeegsanaya:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Tijaabooyinka GitHub Actions hadda waxaa loo habeeyey inay ka boodaan tijaabooyinka dhibaatada leh.

#### Deegaanka Tijaabada ee Docker

Mashruucu wuxuu ka kooban yahay deegaan tijaabo oo ku salaysan Docker si loo xaqiijiyo iswaafaqsanaanta noocyada kala duwan ee Java:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Tijaabooyinka isku-dhafku waxay isticmaalaan JenkinsRule si ay u abuuraan dhacdo Jenkins ku-meel-gaar ah oo tijaabo ah, sidaas darteed uma baahnid server Jenkins ah oo shaqaynaya. Tijaabooyinkani waxay xaqiijiyaan in qoraalladu si sax ah ula shaqeeyaan API-yada iyo qaab-dhismeedyada xogta ee Jenkins dhabta ah. Eeg [dukumeentiyada tijaabada isku-dhafka](../../../src/integration-test/README.md) si aad u hesho faahfaahin dheeraad ah.

### Tayada Koodhka

Mashruucani wuxuu adeegsadaa CodeNarc hubinta tayada koodhka Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Isku-dhafka Daruurta ee La Heli Karo

Maktabaddu waxay taageertaa bixiyeyaasha daruurta ee soo socda:

- **AWS EC2**: Maamul dhacdooyinka EC2 iyagoo ah wakiillo Jenkins
- **Azure VMs**: Maamul mashiinnada dalwaddii (virtual machines) ee Azure iyagoo ah wakiillo Jenkins
- **Kubernetes**: Maamul pod-yada Kubernetes iyagoo ah wakiillo Jenkins
- **Oracle Cloud**: Maamul dhacdooyinka Oracle Cloud Infrastructure (OCI) iyagoo ah wakiillo Jenkins
- **DigitalOcean**: Maamul droplet-yada DigitalOcean iyagoo ah wakiillo Jenkins

## Sifooyinka Soo Socda

- **Isku-dhafka Google Cloud**: Ku maamul wakiillada Jenkins ee Google Cloud Platform
- **Taageerada AWS ECS/Fargate**: Taageero loogu talagalay wakiillada ku salaysan koonteenarrada ee AWS
- **Maamulka Jenkins Badan (Multi-Jenkins)**: Qalab la horumariyay oo lagu maamulo dhacdooyin badan oo Jenkins ah
- **U-raridda Daruur-ka-Daruur**: Qalab lagu raro wakiillada inta u dhaxaysa bixiyeyaasha daruurta

## Ka Qaybqaadashada

Wax-ku-biirintu waa soo dhaweyn leedahay! Fadlan raac [Tilmaamaha Ka-qaybqaadashada](../../../CONTRIBUTING.md).

## Amniga

Wixii walaac amni ah, fadlan dib u eeg [Siyaasadda Amniga](../../../SECURITY.md).

## Shatiga

Mashruucani wuxuu hoos yimaadaa Shatiga MIT - eeg faylka [LICENSE](../../../LICENSE) si aad u hesho faahfaahin.
