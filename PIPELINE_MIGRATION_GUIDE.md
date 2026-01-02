# Declarative Pipeline Migration Guide

This guide helps you migrate from scripted to declarative Jenkins pipeline syntax.

## Table of Contents
- [Why Declarative?](#why-declarative)
- [Key Differences](#key-differences)
- [Migration Examples](#migration-examples)
- [Best Practices](#best-practices)
- [Backward Compatibility](#backward-compatibility)
- [Blue Ocean Compatibility](#blue-ocean-compatibility)
- [Validation and Linting](#validation-and-linting)

## Why Declarative?

Declarative pipelines offer several advantages:

- **Readability**: More structured and easier to understand
- **IDE Support**: Better syntax highlighting and auto-completion
- **Linting**: Built-in validation catches errors early
- **Error Handling**: Structured post sections for consistent error handling
- **Blue Ocean**: Full compatibility with Jenkins Blue Ocean UI
- **Onboarding**: Easier for new team members to learn

## Key Differences

### Structure

**Scripted Pipeline:**
```groovy
node('linux') {
    stage('Build') {
        // imperative code
    }
}
```

**Declarative Pipeline:**
```groovy
pipeline {
    agent { label 'linux' }
    stages {
        stage('Build') {
            steps {
                // declarative steps
            }
        }
    }
}
```

### Key Components

| Component | Scripted | Declarative |
|-----------|----------|-------------|
| Container | `node {}` | `agent {}` |
| Steps | Direct execution | Inside `steps {}` |
| Conditions | `if/else` | `when {}` |
| Error Handling | `try/catch` | `post {}` |
| Environment | `env.VAR = 'value'` | `environment {}` |

## Migration Examples

### Example 1: Basic Build

**Before (Scripted):**
```groovy
node('linux') {
    stage('Checkout') {
        checkout scm
    }
    
    stage('Build') {
        sh 'make build'
    }
    
    stage('Test') {
        sh 'make test'
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent { label 'linux' }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
        
        stage('Test') {
            steps {
                sh 'make test'
            }
        }
    }
}
```

### Example 2: Environment Variables

**Before (Scripted):**
```groovy
node {
    env.JAVA_HOME = '/usr/lib/jvm/java-11'
    env.BUILD_VERSION = '1.0.0'
    
    stage('Build') {
        sh 'mvn clean install'
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent any
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-11'
        BUILD_VERSION = '1.0.0'
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
}
```

### Example 3: Conditional Execution

**Before (Scripted):**
```groovy
node {
    stage('Build') {
        sh 'make build'
    }
    
    stage('Deploy') {
        if (env.BRANCH_NAME == 'main') {
            sh 'make deploy'
        }
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'make deploy'
            }
        }
    }
}
```

### Example 4: Error Handling

**Before (Scripted):**
```groovy
node {
    try {
        stage('Build') {
            sh 'make build'
        }
    } catch (Exception e) {
        echo "Build failed: ${e.message}"
        throw e
    } finally {
        cleanWs()
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
    }
    
    post {
        failure {
            echo "Build failed: ${currentBuild.result}"
        }
        always {
            cleanWs()
        }
    }
}
```

### Example 5: Parallel Execution

**Before (Scripted):**
```groovy
node {
    stage('Test') {
        parallel(
            'Unit Tests': {
                sh 'npm run test:unit'
            },
            'Integration Tests': {
                sh 'npm run test:integration'
            }
        )
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent any
    
    stages {
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
            }
        }
    }
}
```

### Example 6: Docker Agents

**Before (Scripted):**
```groovy
node {
    docker.image('maven:3.8-jdk-11').inside {
        stage('Build') {
            sh 'mvn clean package'
        }
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent {
        docker {
            image 'maven:3.8-jdk-11'
        }
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}
```

### Example 7: Input and Approval

**Before (Scripted):**
```groovy
node {
    stage('Build') {
        sh 'make build'
    }
    
    stage('Deploy') {
        input message: 'Deploy to production?', ok: 'Deploy'
        sh 'make deploy'
    }
}
```

**After (Declarative):**
```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'make build'
            }
        }
        
        stage('Deploy') {
            input {
                message 'Deploy to production?'
                ok 'Deploy'
            }
            steps {
                sh 'make deploy'
            }
        }
    }
}
```

### Example 8: Complex Script Logic

When you need complex scripting logic, use `script {}` blocks:

**Declarative with Script Block:**
```groovy
pipeline {
    agent any
    
    stages {
        stage('Dynamic Build') {
            steps {
                script {
                    // Complex logic that requires imperative code
                    def components = ['frontend', 'backend', 'api']
                    
                    for (component in components) {
                        sh "make build-${component}"
                        
                        if (fileExists("${component}/tests")) {
                            sh "make test-${component}"
                        }
                    }
                }
            }
        }
    }
}
```

## Best Practices

### 1. Use Options Block

```groovy
pipeline {
    agent any
    
    options {
        timeout(time: 1, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    
    stages {
        // ...
    }
}
```

### 2. Leverage Post Conditions

```groovy
pipeline {
    agent any
    
    stages {
        // ...
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'target/*.jar'
        }
        failure {
            mail to: 'team@example.com',
                 subject: "Failed: ${currentBuild.fullDisplayName}",
                 body: "Build failed: ${env.BUILD_URL}"
        }
        unstable {
            echo 'Build is unstable'
        }
        cleanup {
            cleanWs()
        }
    }
}
```

### 3. Use When Conditions

```groovy
stage('Deploy to Staging') {
    when {
        allOf {
            branch 'develop'
            not { changeRequest() }
        }
    }
    steps {
        sh 'deploy-staging.sh'
    }
}

stage('Deploy to Production') {
    when {
        allOf {
            branch 'main'
            tag pattern: 'v\\d+\\.\\d+\\.\\d+', comparator: 'REGEXP'
        }
    }
    steps {
        sh 'deploy-production.sh'
    }
}
```

### 4. Define Stage-Specific Agents

```groovy
pipeline {
    agent none
    
    stages {
        stage('Build') {
            agent {
                docker {
                    image 'maven:3.8-jdk-11'
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

### 5. Use Parameters for Reusable Pipelines

```groovy
pipeline {
    agent any
    
    parameters {
        string(name: 'DEPLOY_ENV', defaultValue: 'staging', description: 'Environment to deploy to')
        choice(name: 'BUILD_TYPE', choices: ['debug', 'release'], description: 'Build type')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run tests?')
    }
    
    stages {
        stage('Deploy') {
            when {
                expression { params.DEPLOY_ENV == 'production' }
            }
            steps {
                echo "Deploying to ${params.DEPLOY_ENV}"
            }
        }
    }
}
```

## Backward Compatibility

### Supporting Legacy Pipelines

If you need to support both scripted and declarative pipelines during migration:

1. **Keep Shared Library Functions Agnostic**: The functions in `vars/` work with both syntaxes
2. **Use Wrapper Functions**: Create wrapper functions that work in both contexts
3. **Gradual Migration**: Migrate one stage at a time

### Example Wrapper

```groovy
// vars/buildJava.groovy
def call(Map config = [:]) {
    // Works in both scripted and declarative
    def javaVersion = config.javaVersion ?: '11'
    def buildTool = config.buildTool ?: 'maven'
    
    if (buildTool == 'maven') {
        sh "mvn clean package -Djava.version=${javaVersion}"
    } else if (buildTool == 'gradle') {
        sh "./gradlew build -PjavaVersion=${javaVersion}"
    }
}
```

**Usage in Scripted:**
```groovy
node {
    stage('Build') {
        buildJava(javaVersion: '11', buildTool: 'maven')
    }
}
```

**Usage in Declarative:**
```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                buildJava(javaVersion: '11', buildTool: 'maven')
            }
        }
    }
}
```

## Blue Ocean Compatibility

Declarative pipelines are fully compatible with Jenkins Blue Ocean. Key features:

### Visual Pipeline Editor
Blue Ocean provides a visual editor for declarative pipelines:
1. Navigate to Blue Ocean in Jenkins
2. Click "New Pipeline"
3. Use the visual editor to create stages
4. The editor generates declarative syntax

### Enhanced Visualization
- Stage view with clear progression
- Parallel stages shown side-by-side
- Input steps display approval UI
- Better error visualization

### Requirements for Blue Ocean

```groovy
pipeline {
    agent any
    
    // Blue Ocean shows these as collapsible sections
    options {
        // Options displayed in pipeline settings
    }
    
    stages {
        // Each stage is a visual block
        stage('Build') {
            steps {
                // Steps are shown with status indicators
                sh 'make build'
            }
        }
        
        // Parallel stages shown side-by-side
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
    
    // Post actions shown in summary
    post {
        always {
            junit '**/test-results/*.xml'
        }
    }
}
```

## Validation and Linting

### Jenkins Pipeline Linter

Validate your pipeline syntax before committing:

```bash
# Using Jenkins CLI
java -jar jenkins-cli.jar -s http://jenkins-server/ declarative-linter < Jenkinsfile

# Using curl
curl -X POST -F "jenkinsfile=<Jenkinsfile" http://jenkins-server/pipeline-model-converter/validate
```

### IntelliJ IDEA / VS Code

Install Jenkins plugins for your IDE:
- **IntelliJ IDEA**: Jenkins Control Plugin, Groovy support
- **VS Code**: Jenkins Pipeline Linter Connector

### Pre-commit Hook

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Validate Jenkinsfile before commit

JENKINSFILE="Jenkinsfile"

if [ -f "$JENKINSFILE" ]; then
    echo "Validating Jenkinsfile..."
    
    # Using Jenkins API
    JENKINS_URL="http://localhost:8080"
    RESULT=$(curl -s -X POST -F "jenkinsfile=<${JENKINSFILE}" \
             "${JENKINS_URL}/pipeline-model-converter/validate")
    
    if echo "$RESULT" | grep -q "Errors"; then
        echo "Jenkinsfile validation failed!"
        echo "$RESULT"
        exit 1
    fi
    
    echo "Jenkinsfile is valid!"
fi

exit 0
```

### Common Validation Errors

#### Missing Required Sections
```groovy
// ❌ Invalid - missing stages
pipeline {
    agent any
}

// ✅ Valid
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building'
            }
        }
    }
}
```

#### Invalid Agent Configuration
```groovy
// ❌ Invalid - agent inside steps
pipeline {
    stages {
        stage('Build') {
            steps {
                agent any  // Wrong!
            }
        }
    }
}

// ✅ Valid - agent at pipeline or stage level
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building'
            }
        }
    }
}
```

#### Mixing Scripted and Declarative
```groovy
// ❌ Invalid - can't use node in declarative
pipeline {
    agent any
    stages {
        node {  // Wrong!
            sh 'make build'
        }
    }
}

// ✅ Valid - use script block for imperative code
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    // Imperative code here
                    sh 'make build'
                }
            }
        }
    }
}
```

## Additional Resources

- [Jenkins Pipeline Syntax Reference](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Declarative Pipeline Best Practices](https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/)
- [Blue Ocean Documentation](https://www.jenkins.io/doc/book/blueocean/)
- [Jenkins Shared Libraries](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)

## Migration Checklist

Use this checklist when migrating a pipeline:

- [ ] Replace `node {}` with `pipeline { agent {} }`
- [ ] Move steps into `steps {}` blocks
- [ ] Convert `try/catch` to `post {}` sections
- [ ] Move environment variables to `environment {}` block
- [ ] Convert `if/else` conditions to `when {}` blocks
- [ ] Update parallel execution to use `parallel {}` block
- [ ] Add `options {}` for pipeline configuration
- [ ] Test with Jenkins Pipeline Linter
- [ ] Verify in Blue Ocean UI
- [ ] Update documentation

## Getting Help

If you encounter issues during migration:

1. Check the [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/) documentation
2. Use the "Pipeline Syntax" snippet generator in Jenkins
3. Test incrementally - migrate one stage at a time
4. Use the Jenkins community forums and Stack Overflow

## Examples in This Repository

See the following files for complete examples:
- `Jenkinsfile` - Main build pipeline (declarative)
- `examples/advanced-pipeline.jenkinsfile` - Complex pipeline example
- `templates/basic-pipeline.jenkinsfile` - Basic template
- `vars/` - Shared library functions compatible with both syntaxes
