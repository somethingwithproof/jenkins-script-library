# Blue Ocean Compatibility Guide

This guide explains how to ensure your declarative pipelines work optimally with Jenkins Blue Ocean.

## Table of Contents
- [What is Blue Ocean?](#what-is-blue-ocean)
- [Compatibility Requirements](#compatibility-requirements)
- [Visual Features](#visual-features)
- [Best Practices](#best-practices)
- [Verification Steps](#verification-steps)
- [Common Issues](#common-issues)
- [Examples](#examples)

## What is Blue Ocean?

Blue Ocean is a modern user interface for Jenkins that provides:
- Visual pipeline editor
- Clear pipeline visualization
- Better error messages and logs
- Improved user experience
- Native Git integration

## Compatibility Requirements

### Required Jenkins Version

- Jenkins 2.7+ (minimum)
- Jenkins 2.361.4+ (recommended for this library)

### Required Plugins

Blue Ocean requires these plugins (typically installed together):
- `blueocean` (meta-plugin that installs all required plugins)
- `blueocean-pipeline-editor`
- `blueocean-pipeline-api-impl`
- `blueocean-rest`
- `blueocean-git-pipeline`

### Installation

```bash
# Install via Jenkins Plugin Manager UI
# Or via Jenkins CLI:
java -jar jenkins-cli.jar -s http://jenkins-server/ install-plugin blueocean

# Or via Dockerfile:
RUN jenkins-plugin-cli --plugins blueocean:1.27.3
```

## Compatibility Requirements

### Pipeline Structure

Blue Ocean works best with **declarative pipelines**. While scripted pipelines are supported, many visual features are only available with declarative syntax.

#### ✅ Fully Compatible (Declarative)

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
        
        stage('Test') {
            parallel {
                stage('Unit') {
                    steps {
                        sh 'make test-unit'
                    }
                }
                stage('Integration') {
                    steps {
                        sh 'make test-integration'
                    }
                }
            }
        }
    }
    
    post {
        always {
            junit '**/test-results/*.xml'
        }
    }
}
```

#### ⚠️ Limited Compatibility (Scripted)

```groovy
node {
    stage('Build') {
        sh 'make build'
    }
}
```

## Visual Features

### Pipeline Visualization

Blue Ocean provides enhanced visualization for declarative pipelines:

#### Stage View

Each stage is displayed as a card with:
- Stage name
- Status (success, failure, in progress, paused)
- Duration
- Step details

```groovy
pipeline {
    agent any
    stages {
        // Each stage is a visual card in Blue Ocean
        stage('Build') {
            steps {
                echo 'Building...'
                sh 'make build'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Testing...'
                sh 'make test'
            }
        }
    }
}
```

#### Parallel Stage Visualization

Parallel stages are displayed side-by-side:

```groovy
pipeline {
    agent any
    stages {
        stage('Parallel Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'npm run test:unit'
                    }
                }
                stage('E2E Tests') {
                    steps {
                        sh 'npm run test:e2e'
                    }
                }
                stage('Performance Tests') {
                    steps {
                        sh 'npm run test:perf'
                    }
                }
            }
        }
    }
}
```

In Blue Ocean, these three test stages appear as three columns running simultaneously.

#### Input Steps

Input steps create interactive approval UI:

```groovy
pipeline {
    agent any
    stages {
        stage('Deploy to Production') {
            input {
                message 'Deploy to production?'
                ok 'Deploy'
                submitter 'admin,release-manager'
                parameters {
                    choice(name: 'REGION', choices: ['us-east-1', 'eu-west-1', 'ap-south-1'])
                }
            }
            steps {
                echo "Deploying to ${REGION}"
            }
        }
    }
}
```

Blue Ocean displays this as a clear approval dialog with the parameter selection.

### Enhanced Logs

Blue Ocean provides:
- Color-coded log output
- Collapsible sections
- Search functionality
- Download logs

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                // Logs appear in Blue Ocean with ANSI colors
                ansiColor('xterm') {
                    sh './build.sh'
                }
            }
        }
    }
}
```

### Test Results Integration

Blue Ocean has native support for test results:

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    // Blue Ocean visualizes test results
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }
}
```

Test results appear in the Blue Ocean UI with:
- Pass/fail counts
- Failure details
- Test history trends

## Best Practices

### 1. Use Descriptive Stage Names

```groovy
// ❌ Poor: Generic names
stage('Stage 1') { }
stage('Stage 2') { }

// ✅ Good: Descriptive names
stage('Compile Java Code') { }
stage('Run Unit Tests') { }
stage('Build Docker Image') { }
```

### 2. Group Related Operations

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            // Group build-related steps
            steps {
                sh 'npm install'
                sh 'npm run build'
                sh 'npm run lint'
            }
        }
        
        stage('Test') {
            parallel {
                // Group test types
                stage('Unit Tests') {
                    steps { sh 'npm run test:unit' }
                }
                stage('Integration Tests') {
                    steps { sh 'npm run test:integration' }
                }
            }
        }
    }
}
```

### 3. Use Post Sections

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
            post {
                success {
                    // Blue Ocean shows success actions clearly
                    archiveArtifacts artifacts: 'build/*.jar'
                }
                failure {
                    // Blue Ocean highlights failure actions
                    echo 'Build failed!'
                }
            }
        }
    }
}
```

### 4. Leverage Options Block

```groovy
pipeline {
    agent any
    
    options {
        // These options are visible in Blue Ocean UI
        timeout(time: 1, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }
    
    stages {
        // ...
    }
}
```

### 5. Add Metadata

```groovy
pipeline {
    agent any
    
    stages {
        stage('Deploy') {
            steps {
                script {
                    // Set build description visible in Blue Ocean
                    currentBuild.description = "Deployed version ${env.BUILD_VERSION}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${env.BRANCH_NAME}"
                }
                sh 'deploy.sh'
            }
        }
    }
}
```

## Verification Steps

### 1. Access Blue Ocean

Navigate to: `http://your-jenkins-server/blue`

Or click "Open Blue Ocean" in the Jenkins sidebar.

### 2. Check Pipeline Visualization

1. Open your pipeline in Blue Ocean
2. Verify that all stages are displayed
3. Check that parallel stages appear side-by-side
4. Confirm that post actions are shown

### 3. Test Interactive Features

```groovy
pipeline {
    agent any
    
    parameters {
        string(name: 'VERSION', defaultValue: '1.0.0')
        choice(name: 'ENV', choices: ['dev', 'staging', 'prod'])
    }
    
    stages {
        stage('Verify Parameters') {
            steps {
                echo "Version: ${params.VERSION}"
                echo "Environment: ${params.ENV}"
            }
        }
    }
}
```

1. Click "Run" in Blue Ocean
2. Verify parameters are shown
3. Enter values and confirm they work

### 4. Verify Branch Handling

Blue Ocean has excellent Git integration:

```groovy
pipeline {
    agent any
    
    stages {
        stage('Branch Info') {
            steps {
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying main branch'
            }
        }
    }
}
```

Check that:
- Branch name is displayed
- Branch-specific stages execute correctly
- Pull request builds are handled

## Common Issues

### Issue 1: Scripted Pipeline Not Fully Visualized

**Problem**: Using scripted syntax reduces visualization.

**Solution**: Convert to declarative syntax:

```groovy
// ❌ Before (Scripted)
node {
    stage('Build') {
        sh 'make build'
    }
}

// ✅ After (Declarative)
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
    }
}
```

### Issue 2: Nested Stages Not Displayed

**Problem**: Blue Ocean doesn't show nested stages well.

**Solution**: Flatten stage structure or use parallel:

```groovy
// ❌ Avoid nested stages
stage('Parent') {
    stage('Child') { }  // Not well visualized
}

// ✅ Use parallel or sequential stages
stage('Tests') {
    parallel {
        stage('Unit') { }
        stage('Integration') { }
    }
}
```

### Issue 3: Complex Script Blocks

**Problem**: Large script blocks appear as single steps.

**Solution**: Break into multiple steps:

```groovy
// ❌ Poor visualization
stage('Build') {
    steps {
        script {
            // 50 lines of code
        }
    }
}

// ✅ Better visualization
stage('Build') {
    steps {
        sh 'step1.sh'
        sh 'step2.sh'
        sh 'step3.sh'
    }
}
```

### Issue 4: Missing Test Results

**Problem**: Test results not showing in Blue Ocean.

**Solution**: Ensure proper test reporting:

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'npm test'
            }
            post {
                always {
                    // Critical: Add this for Blue Ocean test visualization
                    junit '**/test-results/*.xml'
                }
            }
        }
    }
}
```

## Examples

### Example 1: Multi-Stage Pipeline with Blue Ocean

```groovy
pipeline {
    agent any
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    
    environment {
        APP_VERSION = '1.0.0'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo "Building version ${APP_VERSION}"
                sh './gradlew clean build'
            }
        }
        
        stage('Quality Checks') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh './gradlew test'
                    }
                    post {
                        always {
                            junit '**/build/test-results/test/*.xml'
                        }
                    }
                }
                
                stage('Code Coverage') {
                    steps {
                        sh './gradlew jacocoTestReport'
                    }
                    post {
                        always {
                            publishHTML([
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Coverage Report'
                            ])
                        }
                    }
                }
                
                stage('Static Analysis') {
                    steps {
                        sh './gradlew codenarc'
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                sh './gradlew jar'
                archiveArtifacts artifacts: 'build/libs/*.jar'
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging...'
                sh './deploy-staging.sh'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            input {
                message 'Deploy to production?'
                ok 'Deploy'
            }
            steps {
                echo 'Deploying to production...'
                sh './deploy-production.sh'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

### Example 2: Blue Ocean with Custom Visualization

```groovy
pipeline {
    agent any
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    // Set build metadata for Blue Ocean
                    def version = sh(returnStdout: true, script: 'cat VERSION').trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - v${version}"
                    currentBuild.description = "Branch: ${env.BRANCH_NAME}"
                }
            }
        }
        
        stage('Build & Test') {
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
    }
}
```

## Testing Blue Ocean Compatibility

Use this checklist to verify Blue Ocean compatibility:

- [ ] Pipeline appears in Blue Ocean interface
- [ ] All stages are visualized correctly
- [ ] Parallel stages appear side-by-side
- [ ] Input steps show approval UI
- [ ] Test results are displayed
- [ ] Logs are readable and searchable
- [ ] Build history shows trend graphs
- [ ] Branch navigation works
- [ ] Parameters are editable before run
- [ ] Post actions are clearly shown

## Additional Resources

- [Blue Ocean Documentation](https://www.jenkins.io/doc/book/blueocean/)
- [Blue Ocean Getting Started](https://www.jenkins.io/doc/book/blueocean/getting-started/)
- [Blue Ocean Pipeline Editor](https://www.jenkins.io/doc/book/blueocean/pipeline-editor/)
- [Jenkins Declarative Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)

## Support

If you encounter Blue Ocean compatibility issues:

1. Verify your pipeline uses declarative syntax
2. Check that Blue Ocean plugins are installed and up-to-date
3. Review the [Common Issues](#common-issues) section
4. Consult the [Migration Guide](PIPELINE_MIGRATION_GUIDE.md)
5. Check Jenkins and Blue Ocean logs for errors
