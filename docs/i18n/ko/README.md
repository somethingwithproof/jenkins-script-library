<!-- translations:start -->
**Languages:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Português (BR)](../pt-BR/README.md) · [Русский](../ru/README.md) · [日本語](../ja/README.md) · **한국어** · [简体中文](../zh-CN/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md)

<details>
<summary>All 45 languages (Tier 1, Africa, Asia)</summary>

- **Tier 1:** [English](../../../README.md) · [Deutsch](../de/README.md) · [Español](../es/README.md) · [Français](../fr/README.md) · [Italiano](../it/README.md) · [Português (BR)](../pt-BR/README.md) · [Nederlands](../nl/README.md) · [Русский](../ru/README.md) · [Українська](../uk/README.md) · [日本語](../ja/README.md) · **한국어** · [简体中文](../zh-CN/README.md) · [繁體中文](../zh-TW/README.md) · [العربية](../ar/README.md) · [हिन्दी](../hi/README.md) · [Polski](../pl/README.md) · [Türkçe](../tr/README.md) · [Svenska](../sv/README.md) · [Tiếng Việt](../vi/README.md) · [Bahasa Indonesia](../id/README.md)
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

Jenkins 환경에서 다양한 운영 작업을 자동화하고 간소화하기 위해 설계된 Groovy 유틸리티 및 스크립트 모음입니다. 이 라이브러리는 Jenkins 호환 모범 사례를 따르며 단위 테스트와 통합 테스트를 포함합니다. 최신 환경에서의 Jenkins 자동화를 지원합니다.

## 주요 기능

- **작업 관리**: Jenkins 작업의 활성화/비활성화, 정리, 마이그레이션 및 템플릿화
- **클라우드 연동**: AWS, Azure 및 Kubernetes에서 Jenkins 에이전트 관리
- **보안**: Jenkins 인스턴스 감사 및 보안 강화
- **성능 최적화**: Jenkins 성능 모니터링 및 개선
- **구성 관리**: Jenkins 구성 백업 및 복원
- **파이프라인 생성**: 재사용 가능한 템플릿에서 Jenkins 파이프라인 생성

## 프로젝트 구조

이 프로젝트는 표준 Groovy 프로젝트 구조를 따릅니다:

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

## 설치

이 라이브러리는 두 가지 방법으로 사용할 수 있습니다:

1. **Gradle/Maven 프로젝트의 의존성으로 사용**:

   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **Jenkins에서 직접 실행하는 개별 스크립트로 사용**:

   `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/`의 스크립트는 Jenkins Script Console이나 Jenkins CLI를 통해 직접 실행할 수 있습니다.

## 사용법

### 명령줄 스크립트

모든 명령줄 스크립트는 사용법 정보를 표시하는 `--help` 옵션을 지원합니다.

#### 작업 관리

##### 작업 정리

```bash
# Clean build history for a job with limit of 50 builds and reset build number
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

##### 작업 활성화/비활성화

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all

# Enable a specific job
groovy EnableJobs.groovy my-jenkins-job
```

##### 작업 마이그레이션

```bash
# Migrate a specific job
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job

# Migrate all jobs matching a pattern
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --pattern "frontend-.*"

# Use a replacement config file to modify properties during migration
groovy MigrateJobs.groovy --url https://target-jenkins.example.com --user admin --password adminPass --job my-job --replacements-file replacements.json
```

##### 작업 템플릿화

```bash
# Create a job from a template
groovy CreateJobFromTemplate.groovy --template-job template-job --target-job new-job --params-file params.json
```

##### 파이프라인 생성

```groovy
// Generate a Jenkinsfile from template
generatePipeline(
    template: 'templates/basic-pipeline.jenkinsfile',
    parameters: [project: 'my-app'],
    destination: 'Jenkinsfile'
)
```

##### 작업 분석

```bash
# Analyze job health
groovy AnalyzeJobHealth.groovy --job my-jenkins-job

# Analyze pipeline performance
groovy AnalyzePipelinePerformance.groovy --job my-pipeline-job --builds 10

# Audit job configurations
groovy AuditJobConfigurations.groovy --pattern "*.xml" --output audit-report.json
```

#### 노드 관리

##### 노드 목록 조회

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# List cloud-based nodes
groovy ListCloudNodes.groovy --cloud-type aws
```

##### 노드 작업

```bash
# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

#### 클라우드 공급자 관리

##### AWS 관리

```bash
# List EC2 instances used as Jenkins agents
groovy ManageEC2Agents.groovy --list

# Provision a new EC2 instance from template
groovy ManageEC2Agents.groovy --provision --template "my-ec2-template"

# Terminate an EC2 instance
groovy ManageEC2Agents.groovy --terminate --instance-id i-1234567890abcdef0
```

##### Azure 관리

```bash
# List Azure VMs used as Jenkins agents
groovy ManageAzureVMAgents.groovy --list

# Provision a new Azure VM from template
groovy ManageAzureVMAgents.groovy --provision --template "my-azure-template"

# Clean up an Azure VM
groovy ManageAzureVMAgents.groovy --cleanup --node-name azure-agent-01
```

##### Kubernetes 관리

```bash
# List Kubernetes pods used as Jenkins agents
groovy ManageKubernetesAgents.groovy --list

# Provision a new Kubernetes pod from template
groovy ManageKubernetesAgents.groovy --provision --template "my-k8s-template"

# Delete a Kubernetes pod
groovy ManageKubernetesAgents.groovy --delete --pod-name k8s-agent-xyz123
```

#### Jenkins 인스턴스 관리

##### 상태 점검 및 보안

```bash
# Perform a Jenkins instance health check
groovy JenkinsInstanceHealthCheck.groovy

# Audit Jenkins security settings
groovy AuditJenkinsSecurity.groovy --detailed

# Scan for security vulnerabilities
groovy SecurityVulnerabilityScan.groovy --critical-only
```

##### 구성 관리

```bash
# Backup Jenkins configuration
groovy BackupJenkinsConfig.groovy --output jenkins-backup.tar.gz

# Optimize agent resources
groovy OptimizeAgentResources.groovy --reclaim-idle

# Optimize job scheduling
groovy OptimizeJobScheduling.groovy --balance-load
```

### 프로그래밍 API

이 라이브러리는 사용자 정의 Groovy 스크립트에서 사용할 수 있는 프로그래밍 API도 제공합니다:

#### 작업 관리

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

#### 클라우드 관리

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

## 개발

### 프로젝트 빌드

```bash
./gradlew build
```

### 테스트 실행

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

> **참고**: JFFI 네이티브 의존성(jffi-1.2.17-native.jar)과 관련된 알려진 이슈로 인해 테스트가 실패할 수 있습니다. 이 문제가 발생하면 다음 명령으로 테스트를 건너뛸 수 있습니다:
>
> ```bash
> ./gradlew build -PskipTests
> ```
>
> 현재 GitHub Actions 테스트는 문제가 발생하는 테스트를 건너뛰도록 구성되어 있습니다.

#### Docker 테스트 환경

이 프로젝트는 다양한 Java 버전과의 호환성을 검증하기 위한 Docker 기반 테스트 환경을 포함하고 있습니다:

```bash
# Run tests in Java 8 (default)
./run-tests.sh java8

# Run tests in Java 11 with longer timeout
./run-tests.sh java11 30m

# Run tests in Java 17 with longer timeout
./run-tests.sh java17 30m

# Run all Java versions
./run-tests.sh all 1h

# Run tests with different scopes
./run-tests.sh java8 30m false minimal  # Run minimal test set (core utilities only)
./run-tests.sh java8 30m false default  # Run default test set
./run-tests.sh java8 30m false all      # Run all tests including integration tests
```

Docker 테스트 환경에는 다음 기능이 포함되어 있습니다:

- 더 빠른 테스트 실행을 위한 의존성 사전 다운로드
- Gradle 및 Maven 의존성용 볼륨 캐싱
- 일관된 환경을 위한 공유 기본 구성
- 다양한 테스트 범위 지원(minimal, default, all)
- Docker 컨테이너용 메모리 제한 및 상태 확인

#### Java 호환성

이 라이브러리는 다음 Java 버전을 지원합니다:

- Java 8 (LTS)
- Java 11 (LTS)
- Java 17 (LTS)

레거시 Jenkins 설치 환경을 지원하기 위해 Java 8과의 하위 호환성을 유지하고 있습니다. 이 라이브러리는 기본적으로 Java 8 호환 옵션으로 컴파일됩니다.

통합 테스트는 JenkinsRule을 사용하여 테스트용 임시 Jenkins 인스턴스를 생성하므로 실행 중인 Jenkins 서버가 필요하지 않습니다. 이러한 테스트는 스크립트가 실제 Jenkins API 및 데이터 구조와 올바르게 동작하는지 검증합니다. 자세한 내용은 [통합 테스트 문서](../../../src/integration-test/README.md)를 참고하세요.

### 코드 품질

이 프로젝트는 Groovy 코드 품질 검사를 위해 CodeNarc를 사용합니다:

```bash
./gradlew codenarcMain codenarcTest
```

## 지원되는 클라우드 연동

이 라이브러리는 다음 클라우드 공급자를 지원합니다:

- **AWS EC2**: EC2 인스턴스를 Jenkins 에이전트로 관리
- **Azure VMs**: Azure 가상 머신을 Jenkins 에이전트로 관리
- **Kubernetes**: Kubernetes 파드를 Jenkins 에이전트로 관리
- **Oracle Cloud**: Oracle Cloud Infrastructure (OCI) 인스턴스를 Jenkins 에이전트로 관리
- **DigitalOcean**: DigitalOcean 드롭릿을 Jenkins 에이전트로 관리

## 예정된 기능

- **Google Cloud 연동**: Google Cloud Platform에서 Jenkins 에이전트 관리
- **AWS ECS/Fargate 지원**: AWS에서 컨테이너 기반 에이전트 지원
- **다중 Jenkins 관리**: 여러 Jenkins 인스턴스를 관리하기 위한 향상된 도구
- **크로스 클라우드 마이그레이션**: 클라우드 공급자 간 에이전트 마이그레이션 도구

## 기여하기

기여는 언제나 환영합니다! [기여 가이드라인](../../../CONTRIBUTING.md)을 따라 주세요.

## 보안

보안 관련 사항은 [보안 정책](../../../SECURITY.md)을 검토해 주세요.

## 라이선스

이 프로젝트는 MIT 라이선스로 배포됩니다. 자세한 내용은 [LICENSE](../../../LICENSE) 파일을 참고하세요.
