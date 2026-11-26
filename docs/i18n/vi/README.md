<!-- translations:start -->
**Languages:** [English](../../../README.md) · **Tiếng Việt** · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 48 languages (Tier 1, Africa, Asia, Balkans)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · [한국어](../ko/README.md) · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · **Tiếng Việt** · [Bahasa Indonesia](../id/README.md)
- **Africa:** [Afrikaans](../af-ZA/README.md) · [አማርኛ](../am-ET/README.md) · [العربية (مصر)](../ar-EG/README.md) · [العربية (المغرب)](../ar-MA/README.md) · [Français (Maroc)](../fr-MA/README.md) · [Français (Sénégal)](../fr-SN/README.md) · [Hausa](../ha-NG/README.md) · [Igbo](../ig-NG/README.md) · [Malagasy](../mg-MG/README.md) · [Kinyarwanda](../rw-RW/README.md) · [Soomaali](../so-SO/README.md) · [Kiswahili](../sw-KE/README.md) · [isiXhosa](../xh-ZA/README.md) · [Yorùbá](../yo-NG/README.md) · [isiZulu](../zu-ZA/README.md)
- **Asia:** [বাংলা](../bn/README.md) · [فارسی](../fa/README.md) · [עברית](../he/README.md) · [मराठी](../mr/README.md) · [தமிழ்](../ta/README.md) · [తెలుగు](../te/README.md) · [ไทย](../th/README.md) · [اردو](../ur/README.md) · [Bahasa Melayu](../ms/README.md) · [Filipino](../fil/README.md)
- **Balkans:** [Bosanski](../bs/README.md) · [Hrvatski](../hr/README.md) · [Српски](../sr/README.md)

</details>
<!-- translations:end -->

# Thư viện Script Jenkins

[![CI](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/somethingwithproof/jenkins-script-library/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=somethingwithproof_jenkins-script-library&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=somethingwithproof_jenkins-script-library)
[![Release](https://img.shields.io/github/v/release/somethingwithproof/jenkins-script-library)](https://github.com/somethingwithproof/jenkins-script-library/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](../../../LICENSE)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/somethingwithproof/jenkins-script-library/badge)](https://scorecard.dev/viewer/?uri=github.com/somethingwithproof/jenkins-script-library)

Một bộ sưu tập các tiện ích và kịch bản Groovy được thiết kế để tự động hóa và hỗ trợ nhiều tác vụ vận hành khác nhau trong môi trường Jenkins. Thư viện này tuân theo các thực hành tương thích với Jenkins, đồng thời bao gồm cả kiểm thử đơn vị và kiểm thử tích hợp. Hỗ trợ tự động hóa Jenkins cho các môi trường hiện đại.

## Tính năng

- **Quản lý Job**: Bật/tắt, dọn dẹp, di chuyển và tạo mẫu cho các job Jenkins
- **Tích hợp đám mây**: Quản lý các agent Jenkins trên AWS, Azure và Kubernetes
- **Bảo mật**: Kiểm tra và tăng cường bảo mật cho các phiên bản Jenkins
- **Tối ưu hiệu năng**: Giám sát và cải thiện hiệu năng Jenkins
- **Quản lý cấu hình**: Sao lưu và khôi phục cấu hình Jenkins
- **Tạo Pipeline**: Khởi tạo các pipeline Jenkins từ các mẫu có thể tái sử dụng

## Cấu trúc dự án

Dự án tuân theo cấu trúc tiêu chuẩn của dự án Groovy:

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

## Cài đặt

Thư viện này có thể được sử dụng theo hai cách:

1. **Là một thành phần phụ thuộc trong dự án Gradle/Maven của bạn**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Dưới dạng các kịch bản riêng lẻ để chạy trực tiếp trong Jenkins**:

   Các kịch bản trong `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` có thể được thực thi trực tiếp trong Jenkins Script Console hoặc thông qua Jenkins CLI.

## Hướng dẫn sử dụng

### Kịch bản dòng lệnh

Tất cả các kịch bản dòng lệnh đều hỗ trợ tùy chọn `--help` để hiển thị thông tin hướng dẫn sử dụng.

#### Quản lý Job

##### Dọn dẹp Job

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### Bật/Tắt Job

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### Di chuyển Job

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### Tạo mẫu Job

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### Tạo Pipeline

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### Phân tích Job

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### Quản lý Node

##### Liệt kê Node

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### Thao tác trên Node

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### Quản lý nhà cung cấp đám mây

##### Quản lý AWS

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Quản lý Azure

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Quản lý Kubernetes

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Quản lý phiên bản Jenkins

##### Trạng thái hoạt động và bảo mật

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### Quản lý cấu hình

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### API lập trình

Thư viện cũng cung cấp API lập trình để sử dụng trong các kịch bản Groovy của riêng bạn:

#### Quản lý Job

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

#### Quản lý đám mây

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

## Phát triển

### Biên dịch dự án

```bash
./gradlew build
```

### Chạy kiểm thử

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **Lưu ý**: Hiện có một lỗi đã biết với thành phần phụ thuộc native JFFI (jffi-1.2.17-native.jar) có thể gây lỗi kiểm thử. Nếu bạn gặp sự cố này, bạn có thể bỏ qua kiểm thử bằng lệnh:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> Các bài kiểm thử trên GitHub Actions hiện được cấu hình để bỏ qua những kiểm thử gặp lỗi này.

#### Môi trường kiểm thử Docker

Dự án đi kèm một môi trường kiểm thử dựa trên Docker để xác minh khả năng tương thích với các phiên bản Java khác nhau:

```sh
docker compose -f docker-compose.test.yml run --rm java17
docker compose -f docker-compose.test.yml run --rm java21
```

Các bài kiểm thử tích hợp sử dụng JenkinsRule để tạo một phiên bản Jenkins tạm thời cho việc kiểm thử, vì vậy bạn không cần một máy chủ Jenkins đang chạy sẵn. Các kiểm thử này xác minh rằng kịch bản hoạt động chính xác với API và cấu trúc dữ liệu thực của Jenkins. Xem [tài liệu kiểm thử tích hợp](../../../src/integration-test/README.md) để biết thêm chi tiết.

### Chất lượng mã nguồn

Dự án này sử dụng CodeNarc để kiểm tra chất lượng mã nguồn Groovy:

```bash
./gradlew codenarcMain codenarcTest
```

## Các tích hợp đám mây khả dụng

Thư viện hỗ trợ các nhà cung cấp đám mây sau:

- **AWS EC2**: Quản lý các phiên bản EC2 làm agent Jenkins
- **Azure VMs**: Quản lý các máy ảo Azure làm agent Jenkins
- **Kubernetes**: Quản lý các pod Kubernetes làm agent Jenkins
- **Oracle Cloud**: Quản lý các phiên bản Oracle Cloud Infrastructure (OCI) làm agent Jenkins
- **DigitalOcean**: Quản lý các droplet DigitalOcean làm agent Jenkins

## Tính năng sắp ra mắt

- **Tích hợp Google Cloud**: Quản lý các agent Jenkins trên Google Cloud Platform
- **Hỗ trợ AWS ECS/Fargate**: Hỗ trợ các agent dựa trên container trên AWS
- **Quản lý đa Jenkins**: Công cụ nâng cao để quản lý nhiều phiên bản Jenkins
- **Di chuyển đa đám mây**: Công cụ di chuyển agent giữa các nhà cung cấp đám mây

## Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng tuân theo [Hướng dẫn đóng góp](../../../CONTRIBUTING.md) của chúng tôi.

## Bảo mật

Đối với các vấn đề về bảo mật, vui lòng xem [Chính sách bảo mật](../../../SECURITY.md) của chúng tôi.

## Giấy phép

Dự án này được cấp phép theo Giấy phép MIT — xem tệp [LICENSE](../../../LICENSE) để biết chi tiết.
