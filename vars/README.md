# Jenkins Pipeline Shared Library Steps

This directory contains Pipeline-compatible shared library steps that can be used directly in Jenkins Pipelines.

## Available Steps

### disableJobs
Disable Jenkins jobs programmatically.

```groovy
// Disable a single job
disableJobs(jobName: 'my-job')

// Disable jobs matching a pattern
disableJobs(pattern: 'test-.*', dryRun: true)

// Disable jobs in a folder
disableJobs(folderPath: 'my-folder', recursive: true)
```

### backupJenkinsConfig
Backup Jenkins configuration to a file.

```groovy
// Basic backup to workspace
def backupFile = backupJenkinsConfig()

// Custom backup with options
backupJenkinsConfig(
    destination: '/backup/jenkins-config.tar.gz',
    includeJobs: true,
    includePlugins: true,
    compress: true
)

// Archive the backup
backupJenkinsConfig(archive: true)

// Restore from backup
backupJenkinsConfig.restore(backupFile: '/backup/jenkins-config.tar.gz')
```

### auditSecurity
Perform security audit of Jenkins configuration.

```groovy
// Basic security audit
def issues = auditSecurity()

// Comprehensive audit with email report
auditSecurity(
    emailReport: 'security@company.com',
    includePluginVulnerabilities: true,
    checkWeakPasswords: true
)

// Fail build if critical issues found
def audit = auditSecurity(failOnCritical: true)
```

### analyzePipelineMetrics
Analyze pipeline performance with anomaly detection and recommendations.

```groovy
// Analyze current job
def metrics = analyzePipelineMetrics()
echo "Success rate: ${metrics.summary.successRate}%"

// Analyze specific job with custom history
def metrics = analyzePipelineMetrics(
    jobName: 'my-folder/my-pipeline',
    historyLimit: 50
)

// Check for anomalies
if (metrics.hasAnomalies()) {
    metrics.anomalies.each { anomaly ->
        echo "Anomaly in build #${anomaly.buildNumber}: ${anomaly.type}"
    }
}

// Get recommendations
metrics.getTopRecommendations(3).each { rec ->
    echo "[${rec.priority}] ${rec.message}"
}
```

### rotateCredentials
Manage credential rotation, compliance, and usage tracking.

```groovy
// Analyze credential health
def analysis = rotateCredentials.analyze()
if (!analysis.isCompliant()) {
    error "Credentials are not compliant: ${analysis.getViolations()}"
}

// Rotate old credentials (dry run by default)
def rotation = rotateCredentials.rotate(
    policy: [PASSWORD: 60, API_KEY: 90]
)
echo "Would rotate ${rotation.rotated.size()} credentials"

// Find unused credentials
def unused = rotateCredentials.findUnused(days: 30)
unused.each { cred ->
    echo "Unused: ${cred.id} - Last used: ${cred.lastUsed}"
}

// Check compliance status
def compliance = rotateCredentials.checkCompliance()
if (!compliance.compliant) {
    emailext(
        to: 'security@company.com',
        subject: 'Credential Compliance Alert',
        body: "Violations found: ${compliance.violations.join('\n')}"
    )
}
```

### optimizeCloudCosts
Optimize cloud costs across AWS, Azure, GCP, and Kubernetes.

```groovy
// Analyze current costs
def costs = optimizeCloudCosts.analyze()
echo "Total monthly cost: \$${costs.totalCost.monthly}"

// Apply optimizations if significant savings available
if (costs.hasSavingsOpportunity(100)) {
    def optimized = optimizeCloudCosts.optimize(dryRun: false)
    echo "Applied ${optimized.actions.size()} optimizations"
    echo "Saving \$${optimized.estimatedMonthlySavings}/month"
}

// Monitor budgets
def budgetStatus = optimizeCloudCosts.checkBudgets([
    'total': 5000,
    'team:platform': 2000
])
budgetStatus.alerts.each { alert ->
    if (alert.severity == 'HIGH') {
        error alert.message
    }
}

// Get cost allocations
def allocations = optimizeCloudCosts.getAllocations()
echo "Team costs:"
allocations.byTeam.each { team, cost ->
    echo "  ${team}: \$${cost * 24 * 30}/month"
}

// Generate report
def report = optimizeCloudCosts.generateReport()
emailext(
    to: 'devops@company.com',
    subject: 'Cloud Cost Report',
    body: report
)
```

## Usage in Pipeline

To use these steps in your Pipeline:

1. Configure this repository as a Global Pipeline Library in Jenkins
2. Import the library in your Pipeline:

```groovy
@Library('jenkins-script-library') _

pipeline {
    agent any
    
    stages {
        stage('Audit Security') {
            steps {
                script {
                    def audit = auditSecurity()
                    echo "Found ${audit.summary.critical} critical issues"
                }
            }
        }
        
        stage('Backup Config') {
            steps {
                script {
                    def backup = backupJenkinsConfig(archive: true)
                    echo "Backup created: ${backup}"
                }
            }
        }
    }
}
```

## Writing New Steps

To add a new Pipeline step:

1. Create a new file in this directory (e.g., `myStep.groovy`)
2. Define a `call` method that accepts parameters
3. Use `@NonCPS` annotation for methods that don't need serialization
4. Use Jenkins APIs properly for master/agent compatibility

Example:
```groovy
def call(Map args = [:]) {
    // Validate inputs
    if (!args.required) {
        error "Missing required parameter"
    }
    
    // Use Jenkins APIs
    def jenkins = Jenkins.get()
    
    // Use proper logging
    def logger = new JenkinsLogger(currentBuild.rawBuild.getListener(), 'myStep')
    logger.info("Executing my step")
    
    // Return results
    return [success: true, data: result]
}
```