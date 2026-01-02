# Declarative Pipeline Quick Reference

A quick reference card for Jenkins declarative pipeline syntax.

## Basic Structure

```groovy
pipeline {
    agent any                    // Where to run
    
    options { }                  // Pipeline options
    environment { }              // Environment variables
    parameters { }               // Build parameters
    triggers { }                 // Build triggers
    
    stages {                     // Required: Build stages
        stage('Name') {
            steps { }            // Required: Steps to execute
        }
    }
    
    post { }                     // Post-build actions
}
```

## Agent

```groovy
agent any                        // Run on any available agent
agent none                       // No default agent (specify per stage)
agent { label 'linux' }          // Run on agent with label
agent { 
    docker { 
        image 'maven:3.8' 
    } 
}
agent {
    kubernetes {
        yaml '''
          kind: Pod
          spec:
            containers:
            - name: maven
              image: maven:3.8
        '''
    }
}
```

## Options

```groovy
options {
    timeout(time: 1, unit: 'HOURS')
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10'))
    disableConcurrentBuilds()
    skipDefaultCheckout()
    retry(3)
    ansiColor('xterm')
}
```

## Environment

```groovy
environment {
    // Simple variable
    VAR_NAME = 'value'
    
    // From credentials
    API_KEY = credentials('api-key-id')
    
    // Username and password
    GIT_CREDS = credentials('git-credentials')
    // Creates GIT_CREDS, GIT_CREDS_USR, GIT_CREDS_PSW
    
    // From expression
    BUILD_VERSION = "${env.BUILD_NUMBER}"
}
```

## Parameters

```groovy
parameters {
    string(name: 'VERSION', defaultValue: '1.0.0', description: 'Version to deploy')
    text(name: 'NOTES', defaultValue: '', description: 'Release notes')
    booleanParam(name: 'DEBUG', defaultValue: false, description: 'Enable debug mode')
    choice(name: 'ENV', choices: ['dev', 'staging', 'prod'], description: 'Environment')
    password(name: 'PASSWORD', defaultValue: '', description: 'Password')
}
```

## Triggers

```groovy
triggers {
    // Cron syntax
    cron('H */4 * * *')              // Every 4 hours
    cron('H 2 * * *')                // Daily at 2 AM
    
    // Poll SCM
    pollSCM('H/15 * * * *')          // Every 15 minutes
    
    // Upstream jobs
    upstream(upstreamProjects: 'job1,job2', threshold: hudson.model.Result.SUCCESS)
}
```

## Stages and Steps

```groovy
stages {
    stage('Build') {
        steps {
            // Shell command
            sh 'make build'
            
            // Windows batch
            bat 'build.bat'
            
            // PowerShell
            powershell 'Build-Project'
            
            // Echo
            echo 'Building...'
            
            // Error
            error 'Build failed!'
            
            // Script block for complex logic
            script {
                def version = readFile('VERSION').trim()
                env.BUILD_VERSION = version
            }
        }
    }
}
```

## When Conditions

```groovy
stage('Deploy') {
    when {
        // Branch conditions
        branch 'main'
        branch pattern: 'release-.*', comparator: 'REGEXP'
        
        // Build cause
        triggeredBy 'TimerTrigger'
        
        // Environment variable
        environment name: 'DEPLOY', value: 'true'
        
        // Expression
        expression { return params.DEPLOY }
        
        // Tag
        tag 'v*'
        tag pattern: 'v\\d+\\.\\d+\\.\\d+', comparator: 'REGEXP'
        
        // Change request (PR)
        changeRequest()
        changeRequest target: 'main'
        
        // Combining conditions
        allOf {
            branch 'main'
            environment name: 'DEPLOY', value: 'true'
        }
        
        anyOf {
            branch 'main'
            branch 'develop'
        }
        
        not {
            branch 'feature/*'
        }
    }
    steps {
        // ...
    }
}
```

## Parallel Execution

```groovy
stage('Test') {
    parallel {
        stage('Unit Tests') {
            steps {
                sh 'npm run test:unit'
            }
        }
        stage('Integration Tests') {
            steps {
                sh 'npm run test:integration'
            }
        }
        stage('E2E Tests') {
            steps {
                sh 'npm run test:e2e'
            }
        }
    }
}
```

## Input

```groovy
stage('Deploy') {
    input {
        message 'Deploy to production?'
        ok 'Deploy'
        submitter 'admin,manager'
        parameters {
            choice(name: 'REGION', choices: ['us-east-1', 'eu-west-1'])
        }
    }
    steps {
        echo "Deploying to ${REGION}"
    }
}
```

## Post Actions

```groovy
post {
    always {
        // Always run
        cleanWs()
    }
    success {
        // On success
        archiveArtifacts artifacts: 'build/*.jar'
    }
    failure {
        // On failure
        mail to: 'team@example.com',
             subject: "Failed: ${currentBuild.fullDisplayName}",
             body: "Build failed: ${env.BUILD_URL}"
    }
    unstable {
        // When marked unstable
        echo 'Build is unstable'
    }
    aborted {
        // When aborted
        echo 'Build was aborted'
    }
    changed {
        // When status changes from previous build
        echo 'Build status changed'
    }
    fixed {
        // When build succeeds after failure
        echo 'Build is fixed!'
    }
    regression {
        // When build fails after success
        echo 'Build regressed'
    }
    cleanup {
        // Always run after all other post conditions
        echo 'Cleanup complete'
    }
}
```

## Common Steps

### Checkout

```groovy
checkout scm                     // Checkout from source control

checkout([
    $class: 'GitSCM',
    branches: [[name: '*/main']],
    userRemoteConfigs: [[url: 'https://github.com/user/repo.git']]
])
```

### Archive Artifacts

```groovy
archiveArtifacts artifacts: 'build/*.jar', fingerprint: true
archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
```

### Test Results

```groovy
junit '**/test-results/*.xml'
junit testResults: '**/test-results/*.xml', allowEmptyResults: true
```

### Stash/Unstash

```groovy
// Save files
stash name: 'build-artifacts', includes: 'build/**'

// Restore files
unstash 'build-artifacts'
```

### Credentials

```groovy
withCredentials([string(credentialsId: 'api-key', variable: 'API_KEY')]) {
    sh 'curl -H "Authorization: Bearer $API_KEY" https://api.example.com'
}

withCredentials([usernamePassword(
    credentialsId: 'git-creds',
    usernameVariable: 'GIT_USER',
    passwordVariable: 'GIT_PASS'
)]) {
    sh 'git push https://${GIT_USER}:${GIT_PASS}@github.com/user/repo.git'
}

withCredentials([sshUserPrivateKey(
    credentialsId: 'ssh-key',
    keyFileVariable: 'SSH_KEY',
    usernameVariable: 'SSH_USER'
)]) {
    sh 'scp -i $SSH_KEY file.txt $SSH_USER@server:/path'
}
```

### File Operations

```groovy
// Read file
def content = readFile 'version.txt'

// Write file
writeFile file: 'output.txt', text: 'content'

// File exists
if (fileExists('config.yaml')) {
    // ...
}

// Read properties
def props = readProperties file: 'build.properties'

// Read JSON
def json = readJSON file: 'data.json'

// Read YAML
def yaml = readYAML file: 'config.yaml'
```

### Build Operations

```groovy
// Trigger another job
build job: 'downstream-job', wait: true

build job: 'deploy-job',
      parameters: [
          string(name: 'VERSION', value: '1.0.0'),
          booleanParam(name: 'PROD', value: true)
      ]

// Set build result
currentBuild.result = 'UNSTABLE'
currentBuild.result = 'FAILURE'

// Set build description
currentBuild.description = "Version: ${version}"
currentBuild.displayName = "#${env.BUILD_NUMBER} - ${version}"
```

## Matrix Builds

```groovy
stage('Build Matrix') {
    matrix {
        axes {
            axis {
                name 'PLATFORM'
                values 'linux', 'windows', 'macos'
            }
            axis {
                name 'JAVA_VERSION'
                values '11', '17', '21'
            }
        }
        stages {
            stage('Build') {
                steps {
                    echo "Building on ${PLATFORM} with Java ${JAVA_VERSION}"
                }
            }
        }
    }
}
```

## Stage-Level Agent

```groovy
pipeline {
    agent none
    stages {
        stage('Build') {
            agent {
                docker {
                    image 'maven:3.8'
                }
            }
            steps {
                sh 'mvn clean package'
            }
        }
        
        stage('Test') {
            agent {
                label 'test-runner'
            }
            steps {
                sh './run-tests.sh'
            }
        }
    }
}
```

## Script Block

For complex logic, use script block:

```groovy
steps {
    script {
        // Imperative Groovy code
        def version = readFile('VERSION').trim()
        
        if (version.startsWith('1.')) {
            echo "Legacy version ${version}"
            sh 'make build-legacy'
        } else {
            echo "Modern version ${version}"
            sh 'make build'
        }
        
        // Loops
        ['frontend', 'backend', 'api'].each { component ->
            sh "make build-${component}"
        }
        
        // Try/catch
        try {
            sh 'risky-command'
        } catch (Exception e) {
            echo "Command failed: ${e.message}"
            currentBuild.result = 'UNSTABLE'
        }
    }
}
```

## Complete Example

```groovy
pipeline {
    agent {
        docker {
            image 'maven:3.8-jdk-11'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    
    options {
        timeout(time: 1, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }
    
    environment {
        APP_VERSION = '1.0.0'
        DEPLOY_CREDS = credentials('deploy-credentials')
    }
    
    parameters {
        choice(name: 'ENV', choices: ['dev', 'staging', 'prod'], description: 'Target environment')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run tests?')
    }
    
    stages {
        stage('Build') {
            steps {
                echo "Building version ${APP_VERSION}"
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            when {
                expression { return params.RUN_TESTS }
            }
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test'
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Integration Tests') {
                    steps {
                        sh 'mvn verify -Pintegration-tests'
                    }
                }
            }
        }
        
        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo "Deploying to ${params.ENV}"
                sh './deploy.sh ${params.ENV}'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            archiveArtifacts artifacts: '**/target/*.jar'
            echo 'Build succeeded!'
        }
        failure {
            mail to: 'team@example.com',
                 subject: "Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build failed: ${env.BUILD_URL}"
        }
    }
}
```

## Common Patterns

### Conditional Stage Execution

```groovy
stage('Deploy to Prod') {
    when {
        allOf {
            branch 'main'
            expression { return currentBuild.result == null || currentBuild.result == 'SUCCESS' }
        }
    }
    steps {
        // Deploy
    }
}
```

### Timeout for Specific Stage

```groovy
stage('Long Running') {
    options {
        timeout(time: 30, unit: 'MINUTES')
    }
    steps {
        sh './long-running-task.sh'
    }
}
```

### Retry on Failure

```groovy
stage('Flaky Test') {
    options {
        retry(3)
    }
    steps {
        sh './flaky-test.sh'
    }
}
```

### Skip Stage on Commit Message

```groovy
stage('Optional Stage') {
    when {
        not {
            expression { 
                return env.GIT_COMMIT_MESSAGE?.contains('[skip ci]') 
            }
        }
    }
    steps {
        // ...
    }
}
```

## Environment Variables

Available environment variables:

```
BUILD_NUMBER        - Build number
BUILD_ID            - Build ID
BUILD_URL           - Build URL
JOB_NAME            - Job name
JOB_URL             - Job URL
BUILD_TAG           - "jenkins-${JOB_NAME}-${BUILD_NUMBER}"
EXECUTOR_NUMBER     - Executor number
NODE_NAME           - Node name
NODE_LABELS         - Node labels
WORKSPACE           - Workspace directory
JENKINS_HOME        - Jenkins home directory
JENKINS_URL         - Jenkins URL
BUILD_DISPLAY_NAME  - Build display name

GIT_COMMIT          - Git commit hash
GIT_BRANCH          - Git branch
GIT_URL             - Git URL
GIT_AUTHOR_NAME     - Git author name
GIT_AUTHOR_EMAIL    - Git author email
BRANCH_NAME         - Branch name (Multibranch)
CHANGE_ID           - Pull request ID (Multibranch)
CHANGE_URL          - Pull request URL (Multibranch)
CHANGE_TITLE        - Pull request title (Multibranch)
CHANGE_AUTHOR       - Pull request author (Multibranch)
```

## Resources

- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Pipeline Steps Reference](https://www.jenkins.io/doc/pipeline/steps/)
- [Blue Ocean Documentation](https://www.jenkins.io/doc/book/blueocean/)
- [Pipeline Migration Guide](PIPELINE_MIGRATION_GUIDE.md)
- [Blue Ocean Guide](BLUE_OCEAN_GUIDE.md)
