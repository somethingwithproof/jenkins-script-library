# Jenkins Script Library - Available Scripts

This document provides a comprehensive guide to all scripts available in the Jenkins Script Library. These scripts can be used directly in Jenkins Script Console or as part of your Jenkins automation workflows.

## Script Usage

All scripts are located in the `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts` directory. To use a script in Jenkins:

1. Navigate to "Manage Jenkins" > "Script Console"
2. Copy the script content 
3. Modify parameters as needed
4. Execute the script

Alternatively, you can use these scripts programmatically or as part of Jenkins Pipeline jobs.

## Available Scripts

### AnalyzeJobHealth

Analyzes the health of Jenkins jobs and provides metrics on their stability.

**Parameters:**
- `jobNamePattern` - Pattern to match job names (regex supported)
- `daysToAnalyze` - Number of days of build history to analyze

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.AnalyzeJobHealth

def analyzer = new AnalyzeJobHealth()
analyzer.analyzeJobs(".*-build", 30)
```

### AnalyzePipelinePerformance

Analyzes Jenkins Pipeline job performance to identify bottlenecks and slow stages.

**Parameters:**
- `jobName` - Name of the pipeline job to analyze
- `buildsToAnalyze` - Number of builds to include in analysis

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.AnalyzePipelinePerformance

def analyzer = new AnalyzePipelinePerformance()
analyzer.analyze("my-pipeline", 10)
```

### ArchiveJobs

Archives old or unused Jenkins jobs to improve performance.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)
- `daysInactive` - Number of days of inactivity before archiving
- `archiveAction` - Action to take: "disable", "move", or "backup"

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ArchiveJobs

def archiver = new ArchiveJobs()
archiver.archiveInactiveJobs(".*-test", 90, "disable")
```

### AuditJenkinsSecurity

Audits Jenkins security settings and identifies potential vulnerabilities.

**Parameters:**
- `outputFormat` - Format for audit results ("text", "json", or "html")
- `scanDepth` - Depth of security scan ("basic" or "detailed")

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.AuditJenkinsSecurity

def auditor = new AuditJenkinsSecurity()
def results = auditor.audit("json", "detailed")
println results
```

### AuditJobConfigurations

Audits Jenkins job configurations for best practices and security issues.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)
- `auditCategories` - List of categories to audit (e.g., "security", "performance")

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.AuditJobConfigurations

def auditor = new AuditJobConfigurations()
auditor.auditJobs(".*", ["security", "performance"])
```

### BackupJenkinsConfig

Creates a backup of Jenkins configuration files.

**Parameters:**
- `backupDir` - Directory to store backups
- `includeJobs` - Whether to include job configurations (true/false)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.BackupJenkinsConfig

def backup = new BackupJenkinsConfig()
backup.createBackup("/var/backups/jenkins", true)
```

### CleanBuildHistory

Cleans up old build history to free up disk space.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)
- `daysToKeep` - Number of days of build history to keep
- `maxBuildsToKeep` - Maximum number of builds to keep per job

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.CleanBuildHistory

def cleaner = new CleanBuildHistory()
cleaner.clean(".*", 30, 100)
```

### CopyJob

Copies a Jenkins job with customizable parameters.

**Parameters:**
- `sourceJobName` - Name of the source job
- `targetJobName` - Name of the target job
- `parameterOverrides` - Map of parameters to override in the target job

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.CopyJob

def copier = new CopyJob()
copier.copy("source-job", "target-job", [BRANCH: "develop", ENVIRONMENT: "staging"])
```

### CreateJobFromTemplate

Creates a new Jenkins job from a template.

**Parameters:**
- `templateName` - Name of the template job
- `newJobName` - Name for the new job
- `parameters` - Map of parameters for the new job

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.CreateJobFromTemplate

def creator = new CreateJobFromTemplate()
creator.create("template-job", "new-feature-job", [REPO: "git@github.com:org/repo.git", BRANCH: "feature/xyz"])
```

### DisableJobs

Disables Jenkins jobs matching specified criteria.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)
- `reason` - Reason for disabling (added as job description)
- `daysInactive` - Optional: Only disable jobs inactive for this many days

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.DisableJobs

def disabler = new DisableJobs()
disabler.disable(".*-legacy", "Project deprecated", 60)
```

### EnableJobs

Enables previously disabled Jenkins jobs.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.EnableJobs

def enabler = new EnableJobs()
enabler.enable(".*-project")
```

### JenkinsInstanceHealthCheck

Performs a health check on a Jenkins instance.

**Parameters:**
- `checkCategories` - List of categories to check (e.g., "system", "plugins", "security")
- `outputFormat` - Format for health check results ("text", "json", or "html")

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.JenkinsInstanceHealthCheck

def healthCheck = new JenkinsInstanceHealthCheck()
def results = healthCheck.check(["system", "plugins", "security"], "json")
println results
```

### ListCloudNodes

Lists Jenkins cloud nodes (agents) with their status.

**Parameters:**
- `cloudType` - Type of cloud provider ("aws", "azure", "kubernetes", etc.)
- `includeOffline` - Whether to include offline nodes (true/false)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ListCloudNodes

def lister = new ListCloudNodes()
def nodes = lister.list("aws", true)
nodes.each { println it }
```

### ListSlaveNodes

Lists all Jenkins slave nodes with their status.

**Parameters:**
- `showDetails` - Level of detail to show ("basic", "full")
- `filterStatus` - Filter by status ("online", "offline", "all")

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ListSlaveNodes

def lister = new ListSlaveNodes()
def nodes = lister.list("full", "all")
nodes.each { println it }
```

### ManageAzureVMAgents

Manages Azure VM agents for Jenkins.

**Parameters:**
- `action` - Action to perform ("create", "start", "stop", "delete")
- `nodeName` - Name of the node
- `vmSize` - Size of VM (for "create" action)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ManageAzureVMAgents

def manager = new ManageAzureVMAgents()
manager.manage("create", "build-agent", "Standard_D2s_v3")
```

### ManageEC2Agents

Manages EC2 agents for Jenkins.

**Parameters:**
- `action` - Action to perform ("launch", "terminate", "list")
- `instanceType` - Type of EC2 instance (for "launch" action)
- `count` - Number of instances to manage

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ManageEC2Agents

def manager = new ManageEC2Agents()
manager.manage("launch", "t3.medium", 2)
```

### ManageJobDependencies

Manages dependencies between Jenkins jobs.

**Parameters:**
- `jobName` - Name of the job
- `action` - Action to perform ("add", "remove", "list")
- `dependencyJobName` - Name of the dependency job (for "add"/"remove" actions)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ManageJobDependencies

def manager = new ManageJobDependencies()
manager.manage("my-job", "add", "dependency-job")
```

### ManageJobParameters

Manages parameters for Jenkins jobs.

**Parameters:**
- `jobName` - Name of the job
- `action` - Action to perform ("add", "update", "remove", "list")
- `paramName` - Name of the parameter
- `paramValue` - Default value for the parameter (for "add"/"update" actions)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ManageJobParameters

def manager = new ManageJobParameters()
manager.manage("my-job", "add", "ENVIRONMENT", "staging")
```

### ManageKubernetesAgents

Manages Kubernetes agents for Jenkins.

**Parameters:**
- `action` - Action to perform ("create", "delete", "scale")
- `podName` - Name of the pod template
- `containerCount` - Number of containers (for "scale" action)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.ManageKubernetesAgents

def manager = new ManageKubernetesAgents()
manager.manage("scale", "build-agent", 5)
```

### MigrateJobs

Migrates Jenkins jobs between Jenkins instances or folders.

**Parameters:**
- `jobPattern` - Pattern to match job names (regex supported)
- `targetLocation` - Target location to migrate jobs to
- `migrationType` - Type of migration ("copy", "move")

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.MigrateJobs

def migrator = new MigrateJobs()
migrator.migrate(".*-project", "NewFolder", "copy")
```

### OptimizeAgentResources

Optimizes resource usage of Jenkins agents.

**Parameters:**
- `strategy` - Optimization strategy ("cost", "performance", "balanced")
- `agentPattern` - Pattern to match agent names (regex supported)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.OptimizeAgentResources

def optimizer = new OptimizeAgentResources()
optimizer.optimize("balanced", ".*-agent")
```

### OptimizeJobScheduling

Optimizes the scheduling of Jenkins jobs to improve performance.

**Parameters:**
- `strategy` - Scheduling strategy ("fairshare", "priority", "time")
- `jobPattern` - Pattern to match job names (regex supported)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.OptimizeJobScheduling

def optimizer = new OptimizeJobScheduling()
optimizer.optimize("priority", ".*-build")
```

### SecurityVulnerabilityScan

Scans Jenkins for security vulnerabilities.

**Parameters:**
- `scanType` - Type of scan ("basic", "comprehensive")
- `fixAutomatically` - Whether to fix issues automatically (true/false)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.SecurityVulnerabilityScan

def scanner = new SecurityVulnerabilityScan()
def results = scanner.scan("comprehensive", false)
println results
```

### StartOfflineSlaveNodes

Starts offline Jenkins slave nodes.

**Parameters:**
- `nodePattern` - Pattern to match node names (regex supported)
- `waitForOnline` - Whether to wait for nodes to come online (true/false)

**Example:**
```groovy
import com.github.thomasvincent.jenkinsscripts.scripts.StartOfflineSlaveNodes

def starter = new StartOfflineSlaveNodes()
starter.start(".*-agent", true)
```

## Development Guidelines

When developing new scripts or modifying existing ones, follow these guidelines:

1. Include proper error handling with try/catch blocks
2. Add logging with appropriate log levels
3. Document all parameters and return values
4. Maintain backward compatibility when possible
5. Write unit tests for all new functionality
6. Follow the existing code style and formatting

For more information about contributing to this library, see [CONTRIBUTING.md](CONTRIBUTING.md).