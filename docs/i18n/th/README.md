<!-- translations:start -->
**Languages:** [English](../../../README.md) · **ไทย** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · **ไทย** · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Jenkins Script Library

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

ชุดยูทิลิตีและสคริปต์ภาษา Groovy ที่ออกแบบมาเพื่อทำงานอัตโนมัติและอำนวยความสะดวกในการปฏิบัติงานด้านต่าง ๆ ภายในสภาพแวดล้อม Jenkins ไลบรารีนี้ปฏิบัติตามแนวทางที่เข้ากันได้กับ Jenkins พร้อมรวมยูนิตเทสต์และอินทิเกรชันเทสต์ไว้ด้วย และรองรับการทำงานอัตโนมัติของ Jenkins สำหรับสภาพแวดล้อมสมัยใหม่

## ฟีเจอร์หลัก

- **การจัดการจ็อบ**: เปิด/ปิดการใช้งาน ทำความสะอาด ย้ายระบบ และสร้างเทมเพลตสำหรับจ็อบ Jenkins
- **การเชื่อมต่อกับคลาวด์**: จัดการ Jenkins agent บน AWS, Azure และ Kubernetes
- **ความปลอดภัย**: ตรวจสอบและเสริมความปลอดภัยให้กับอินสแตนซ์ Jenkins
- **การเพิ่มประสิทธิภาพการทำงาน**: มอนิเตอร์และปรับปรุงประสิทธิภาพของ Jenkins
- **การจัดการการกำหนดค่า**: สำรองและกู้คืนการกำหนดค่าของ Jenkins
- **การสร้างไปป์ไลน์**: สร้างไปป์ไลน์ของ Jenkins จากเทมเพลตที่นำกลับมาใช้ซ้ำได้

## โครงสร้างโปรเจกต์

โปรเจกต์นี้ใช้โครงสร้างมาตรฐานของโปรเจกต์ Groovy:

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

## การติดตั้ง

ไลบรารีนี้สามารถใช้งานได้สองวิธี:

1. **ใช้งานเป็นดีเพนเดนซีในโปรเจกต์ Gradle/Maven ของคุณ**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **ใช้งานเป็นสคริปต์เดี่ยวเพื่อรันโดยตรงใน Jenkins**:

   สคริปต์ใน `src/com/github/thomasvincent/jenkinsscripts/scripts/` สามารถรันได้โดยตรงใน Jenkins Script Console หรือผ่าน Jenkins CLI

## การใช้งาน

### สคริปต์บรรทัดคำสั่ง

สคริปต์บรรทัดคำสั่งทั้งหมดรองรับตัวเลือก `--help` เพื่อแสดงข้อมูลวิธีใช้งาน

#### การจัดการจ็อบ

##### การทำความสะอาดจ็อบ

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### การเปิด/ปิดการใช้งานจ็อบ

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### การย้ายจ็อบ

ยังไม่รองรับการย้ายงานไปยังคอนโทรลเลอร์ระยะไกล

##### การสร้างเทมเพลตจ็อบ

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### การสร้างไปป์ไลน์

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### การวิเคราะห์จ็อบ

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### การจัดการโหนด

##### การแสดงรายการโหนด

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### การดำเนินการกับโหนด

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### การจัดการผู้ให้บริการคลาวด์

##### การจัดการ AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### การจัดการ Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### การจัดการ Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### การจัดการอินสแตนซ์ Jenkins

##### สุขภาพระบบและความปลอดภัย

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### การจัดการการกำหนดค่า

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API สำหรับการเขียนโปรแกรม

ไลบรารียังมี API สำหรับการเขียนโปรแกรมเพื่อนำไปใช้ในสคริปต์ Groovy ของคุณเอง:

#### การจัดการจ็อบ

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

#### การจัดการคลาวด์

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

## การพัฒนา

### การบิลด์โปรเจกต์

```bash
./gradlew build
```

### การรันการทดสอบ

```bash
# Run unit tests
./gradlew test

# Run all tests
./gradlew check
```

> **หมายเหตุ**: มีปัญหาที่ทราบอยู่แล้วกับดีเพนเดนซีเนทีฟ JFFI (jffi-1.2.17-native.jar) ซึ่งอาจทำให้การทดสอบล้มเหลว หากคุณพบปัญหานี้ สามารถข้ามการทดสอบได้ด้วยคำสั่ง:
>
> ```bash
> ./gradlew build -x test
> ```
>
> ปัจจุบันการทดสอบบน GitHub Actions ถูกตั้งค่าให้ข้ามชุดทดสอบที่มีปัญหาไว้แล้ว

#### สภาพแวดล้อมการทดสอบด้วย Docker

[`java24`](../../../Dockerfile.test) → `eclipse-temurin:24-jdk`

[`java21`](../../../Dockerfile.java21) → `eclipse-temurin:21-jdk`

```sh
docker compose -f docker-compose.test.yml run --rm java24
docker compose -f docker-compose.test.yml run --rm java21
```

### คุณภาพโค้ด

โปรเจกต์นี้ใช้ CodeNarc สำหรับตรวจสอบคุณภาพโค้ด Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## การเชื่อมต่อคลาวด์ที่รองรับ

ไลบรารีนี้รองรับผู้ให้บริการคลาวด์ต่อไปนี้:

- **AWS EC2**: จัดการอินสแตนซ์ EC2 เป็น Jenkins agent
- **Azure VMs**: จัดการเครื่องเสมือน Azure เป็น Jenkins agent
- **Kubernetes**: จัดการพ็อด Kubernetes เป็น Jenkins agent
- **Oracle Cloud**: จัดการอินสแตนซ์ Oracle Cloud Infrastructure (OCI) เป็น Jenkins agent
- **DigitalOcean**: จัดการ droplet ของ DigitalOcean เป็น Jenkins agent

## ฟีเจอร์ที่กำลังจะมาถึง

- **การเชื่อมต่อ Google Cloud**: จัดการ Jenkins agent บน Google Cloud Platform
- **การรองรับ AWS ECS/Fargate**: รองรับ agent แบบคอนเทนเนอร์บน AWS
- **การจัดการหลายอินสแตนซ์ Jenkins**: เครื่องมือขั้นสูงสำหรับจัดการอินสแตนซ์ Jenkins หลายตัวพร้อมกัน
- **การย้ายข้ามคลาวด์**: เครื่องมือสำหรับย้าย agent ระหว่างผู้ให้บริการคลาวด์

## การมีส่วนร่วม

ยินดีต้อนรับทุกการมีส่วนร่วม! โปรดปฏิบัติตาม[แนวทางการมีส่วนร่วม](../../../CONTRIBUTING.md)ของเรา

## ความปลอดภัย

หากมีข้อกังวลด้านความปลอดภัย โปรดตรวจสอบ[นโยบายความปลอดภัย](../../../SECURITY.md)ของเรา

## สัญญาอนุญาต

โปรเจกต์นี้เผยแพร่ภายใต้สัญญาอนุญาต MIT ดูรายละเอียดได้ในไฟล์ [LICENSE](../../../LICENSE)
