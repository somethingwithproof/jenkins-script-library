# Jenkins Script Library

This document provides a comprehensive list of all available scripts in the Jenkins Script Library. These scripts can be executed directly within Jenkins Script Console or as standalone Groovy scripts to automate various Jenkins management tasks.

## Available Scripts

### AnalyzeJobHealth.groovy

Analyzes health status of Jenkins jobs and provides detailed reports.

**Parameters:**
- `--job`: (Optional) Specific job name to analyze
- `--all`: (Optional) Analyze all jobs
- `--format`: (Optional) Output format (text, json)

**Example:**
```groovy
./AnalyzeJobHealth.groovy --all --format json
```

### AnalyzePipelinePerformance.groovy

Analyzes performance metrics for Jenkins pipeline jobs including execution time, wait time, and resource utilization.

**Parameters:**
- `--job`: Pipeline job name to analyze
- `--builds`: (Optional) Number of builds to analyze
- `--format`: (Optional) Output format (text, json)

**Example:**
```groovy
./AnalyzePipelinePerformance.groovy --job my-pipeline --builds 10
```

### ArchiveJobs.groovy

Archives old or inactive Jenkins jobs to reduce clutter in the Jenkins interface.

**Parameters:**
- `--jobs`: Comma-separated list of job names to archive
- `--inactive`: (Optional) Archive jobs inactive for specified days
- `--dry-run`: (Optional) Simulation mode without making actual changes

**Example:**
```groovy
./ArchiveJobs.groovy --inactive 90 --dry-run
```

### AuditJenkinsSecurity.groovy

Performs security audit of Jenkins instance to identify potential vulnerabilities.

**Parameters:**
- `--full`: (Optional) Perform comprehensive security audit
- `--report`: (Optional) Output file for audit report
- `--format`: (Optional) Output format (text, json)

**Example:**
```groovy
./AuditJenkinsSecurity.groovy --full --format json
```

### AuditJobConfigurations.groovy

Audits job configurations to identify common issues or inconsistencies.

**Parameters:**
- `--pattern`: (Optional) Pattern to match job names
- `--check`: (Optional) Specific checks to perform
- `--format`: (Optional) Output format (text, json)

**Example:**
```groovy
./AuditJobConfigurations.groovy --pattern "frontend-*" --check triggers,parameters
```

### BackupJenkinsConfig.groovy

Creates a backup of Jenkins configuration and job definitions.

**Parameters:**
- `--target`: Directory to store backup
- `--jobs`: (Optional) Include job configurations in backup
- `--plugins`: (Optional) Include plugin information

**Example:**
```groovy
./BackupJenkinsConfig.groovy --target /path/to/backup --jobs --plugins
```

### CleanBuildHistory.groovy

Cleans build history from Jenkins jobs with option to reset build numbers.

**Parameters:**
- `jobName`: Name of the job to clean
- `--reset`: (Optional) Reset build number to 1 after cleaning
- `--limit`: (Optional) Maximum number of builds to clean (default: 100)

**Example:**
```groovy
./CleanBuildHistory.groovy --reset --limit 50 my-jenkins-job
```

### CopyJob.groovy

Copies an existing Jenkins job with optional configuration replacements.

**Parameters:**
- `--source`: Full name of the source job (e.g., "Folder/JobToCopy")
- `--newName`: Name for the new job (simple name, not full path)
- `--targetFolder`: (Optional) Full path of the target folder for the new job
- `--replace`: (Optional) Configuration replacement string "oldString:newString"
- `--overwrite`: (Optional) Allow overwriting if the target job already exists
- `--dryRun`: (Optional) Simulate without actual changes

**Example:**
```groovy
./CopyJob.groovy --source Folder/OldJob --newName NewJob --replace "old-url:new-url"
```

### CreateJobFromTemplate.groovy

Creates a new Jenkins job from a predefined template.

**Parameters:**
- `--template`: Name of the template to use
- `--name`: Name for the new job
- `--params`: (Optional) Parameters to apply to template
- `--folder`: (Optional) Target folder

**Example:**
```groovy
./CreateJobFromTemplate.groovy --template java-maven --name my-project --params "REPO_URL=https://github.com/org/repo"
```

### DisableJobs.groovy

Disables specified Jenkins jobs to prevent them from running.

**Parameters:**
- `--jobs`: Comma-separated list of job names to disable
- `--pattern`: (Optional) Pattern to match job names
- `--reason`: (Optional) Reason for disabling jobs
- `--dry-run`: (Optional) Simulation mode

**Example:**
```groovy
./DisableJobs.groovy --pattern "deprecated-*" --reason "Deprecated services"
```

### EnableJobs.groovy

Enables previously disabled Jenkins jobs.

**Parameters:**
- `--jobs`: Comma-separated list of job names to enable
- `--pattern`: (Optional) Pattern to match job names
- `--dry-run`: (Optional) Simulation mode

**Example:**
```groovy
./EnableJobs.groovy --jobs job1,job2,job3
```

### JenkinsInstanceHealthCheck.groovy

Performs a comprehensive health check of a Jenkins instance.

**Parameters:**
- `--checks`: (Optional) Specific health checks to run
- `--threshold`: (Optional) Alert threshold for issues
- `--format`: (Optional) Output format (text, json)

**Example:**
```groovy
./JenkinsInstanceHealthCheck.groovy --checks disk,memory,plugins --format json
```

### ListCloudNodes.groovy

Lists all cloud-provisioned nodes along with their status.

**Parameters:**
- `--provider`: (Optional) Cloud provider to filter by (aws, azure, gcp, kubernetes)
- `--status`: (Optional) Filter by node status
- `--json`: (Optional) Output in JSON format

**Example:**
```groovy
./ListCloudNodes.groovy --provider aws --json
```

### ListSlaveNodes.groovy

Lists Jenkins slave nodes with detailed status information.

**Parameters:**
- `[slaveName]`: (Optional) Specific slave node to get details for
- `--json`: (Optional) Output in JSON format
- `--all`: (Optional) List all slaves (default if no slave name is provided)

**Example:**
```groovy
./ListSlaveNodes.groovy --json my-slave-node-name
```

### ManageAzureVMAgents.groovy

Manages Azure VM agents for Jenkins, with operations to create, resize, and remove nodes.

**Parameters:**
- `--action`: Action to perform (create, delete, resize, list)
- `--name`: (Optional) Agent name for specific actions
- `--size`: (Optional) VM size for create or resize actions
- `--count`: (Optional) Number of agents to create

**Example:**
```groovy
./ManageAzureVMAgents.groovy --action create --name azure-agent --size Standard_D2s_v3
```

### ManageEC2Agents.groovy

Manages EC2 instances used as Jenkins agents.

**Parameters:**
- `--action`: Action to perform (create, terminate, list)
- `--instance`: (Optional) EC2 instance ID for specific actions
- `--type`: (Optional) Instance type for create actions
- `--label`: (Optional) Node label

**Example:**
```groovy
./ManageEC2Agents.groovy --action list --label build-agent
```

### ManageJobDependencies.groovy

Manages dependencies between Jenkins jobs, creating upstream/downstream relationships.

**Parameters:**
- `--job`: Job to manage dependencies for
- `--upstream`: (Optional) Comma-separated list of upstream jobs
- `--downstream`: (Optional) Comma-separated list of downstream jobs
- `--remove`: (Optional) Remove specified dependencies

**Example:**
```groovy
./ManageJobDependencies.groovy --job final-build --upstream prep-job,build-components
```

### ManageJobParameters.groovy

Manages parameters for parameterized Jenkins jobs.

**Parameters:**
- `--job`: Job to manage parameters for
- `--add`: (Optional) Add parameter "name:type:defaultValue"
- `--remove`: (Optional) Remove parameter by name
- `--update`: (Optional) Update parameter "name:newDefault"

**Example:**
```groovy
./ManageJobParameters.groovy --job my-job --add "BRANCH:string:main"
```

### ManageKubernetesAgents.groovy

Manages Kubernetes pod agents for Jenkins.

**Parameters:**
- `--action`: Action to perform (create, delete, list)
- `--name`: (Optional) Pod name for specific actions
- `--yaml`: (Optional) YAML file for pod configuration
- `--namespace`: (Optional) Kubernetes namespace

**Example:**
```groovy
./ManageKubernetesAgents.groovy --action create --yaml pod-template.yaml
```

### MigrateJobs.groovy

Migrates jobs between folders or instances of Jenkins.

**Parameters:**
- `--jobs`: Comma-separated list of job names to migrate
- `--source`: (Optional) Source folder or Jenkins instance
- `--target`: Target folder or Jenkins instance
- `--copy`: (Optional) Keep original job after migration

**Example:**
```groovy
./MigrateJobs.groovy --jobs job1,job2 --target "Production/Frontend"
```

### OptimizeAgentResources.groovy

Analyzes and optimizes resource allocation for Jenkins agents.

**Parameters:**
- `--agent`: (Optional) Specific agent to optimize
- `--resources`: (Optional) Resources to optimize (cpu, memory)
- `--apply`: (Optional) Apply recommended optimizations

**Example:**
```groovy
./OptimizeAgentResources.groovy --resources cpu,memory --apply
```

### OptimizeJobScheduling.groovy

Optimizes scheduling of Jenkins jobs to improve resource utilization.

**Parameters:**
- `--jobs`: (Optional) Comma-separated list of jobs to optimize
- `--strategy`: (Optional) Scheduling strategy (balanced, performance, cost)
- `--apply`: (Optional) Apply recommended optimizations

**Example:**
```groovy
./OptimizeJobScheduling.groovy --strategy balanced --apply
```

### SecurityVulnerabilityScan.groovy

Scans Jenkins for security vulnerabilities and provides remediation steps.

**Parameters:**
- `--level`: (Optional) Scan level (basic, advanced, comprehensive)
- `--plugins`: (Optional) Scan plugins for vulnerabilities
- `--report`: (Optional) Output file for scan report

**Example:**
```groovy
./SecurityVulnerabilityScan.groovy --level comprehensive --plugins --report scan-results.json
```

### StartOfflineSlaveNodes.groovy

Attempts to start offline slave nodes that are temporarily disconnected.

**Parameters:**
- `--nodes`: (Optional) Comma-separated list of nodes to start
- `--all`: (Optional) Attempt to start all offline nodes
- `--retry`: (Optional) Number of retries for connection attempts

**Example:**
```groovy
./StartOfflineSlaveNodes.groovy --all --retry 3
```

### DisableJobTriggers.groovy

Disables build triggers on Jenkins jobs.

**Parameters:**
- `--jobs`: Comma-separated list of job names
- `--trigger`: (Optional) Specific trigger type to disable
- `--all-triggers`: (Optional) Disable all triggers
- `--dry-run`: (Optional) Simulation mode

**Example:**
```groovy
./DisableJobTriggers.groovy --jobs my-job --all-triggers
```

### ManageHelmInstallation.groovy

Manages Helm installations for Jenkins.

**Parameters:**
- `--action`: Action to perform (install, update, remove, list)
- `--version`: (Optional) Helm version for install or update
- `--name`: (Optional) Installation name

**Example:**
```groovy
./ManageHelmInstallation.groovy --action install --version 3.8.2 --name helm-latest
```

## Usage Notes

1. These scripts can be executed in the Jenkins Script Console or as standalone Groovy scripts.
2. For standalone execution, ensure that required Jenkins libraries are in the classpath.
3. Some scripts require administrative privileges in Jenkins.
4. Always use the `--help` option to see detailed usage information for each script.
5. Use the `--dry-run` option (when available) to simulate operations without making actual changes.

## Script Development

To contribute new scripts to this library, please follow the structure of existing scripts and include:

1. Proper documentation with example usage
2. Command-line argument parsing with helpful options
3. Error handling and informative output
4. Unit tests where applicable

For more information on contributing, see the [CONTRIBUTING.md](CONTRIBUTING.md) file.