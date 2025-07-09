# Integration Testing Guide

## Overview

This Jenkins Script Library uses a comprehensive Docker-based integration testing framework that provides end-to-end testing of all scripts in a real Jenkins environment.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Environment                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Jenkins   │  │  LocalStack  │  │     Azurite     │   │
│  │  Container  │  │   (AWS Mock) │  │  (Azure Mock)   │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  PostgreSQL │  │    Docker    │  │   Test Runner   │   │
│  │  (Backups)  │  │    (DinD)    │  │   Container     │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+
- Gradle 8.14+

### Running Tests

```bash
# Run all integration tests in Docker
./gradlew dockerIntegrationTest

# Run specific test class
./gradlew integrationTest --tests JobManagementE2ETest

# Run tests against existing Jenkins
./gradlew integrationTest -PjenkinsUrl=http://localhost:8080 -PjenkinsUser=admin -PjenkinsPassword=admin

# Start environment and keep it running for development
docker-compose -f docker-compose.integration.yml up -d

# View logs
docker-compose -f docker-compose.integration.yml logs -f jenkins

# Stop environment
docker-compose -f docker-compose.integration.yml down -v
```

## Test Categories

### 1. Job Management Tests
Tests for job-related scripts including creation, copying, disabling, and parameter management.

```groovy
@Test
void testCreateJobFromTemplate() {
    // Tests job creation from template with parameter injection
}

@Test
void testDisableMultipleJobs() {
    // Tests pattern-based job disabling
}
```

### 2. Cloud Node Management Tests
Tests for AWS, Azure, and Kubernetes node management.

```groovy
@Test
void testAWSNodeProvisioning() {
    // Tests EC2 instance provisioning via LocalStack
}

@Test
void testKubernetesAgentScaling() {
    // Tests K8s pod agent scaling
}
```

### 3. Security Audit Tests
Tests for security scanning and compliance checking.

```groovy
@Test
void testSecurityVulnerabilityScan() {
    // Tests plugin vulnerability detection
}

@Test
void testPermissionAudit() {
    // Tests user permission analysis
}
```

### 4. Configuration Backup Tests
Tests for Jenkins configuration backup and restore.

```groovy
@Test
void testFullBackup() {
    // Tests complete Jenkins backup to PostgreSQL
}

@Test
void testIncrementalBackup() {
    // Tests incremental backup with compression
}
```

## Writing New Tests

### Base Test Class
All integration tests should extend `BaseIntegrationTest`:

```groovy
class MyNewE2ETest extends BaseIntegrationTest {
    
    @Test
    void testMyFeature() {
        // Given: Setup test data
        createJob("test-job", jobXml)
        
        // When: Execute script
        String result = executeGroovyScript("""
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.MyClass
            new MyClass().doSomething()
        """)
        
        // Then: Verify results
        assertTrue("Should succeed", result.contains("SUCCESS"))
        
        // Cleanup handled automatically
    }
}
```

### Available Test Utilities

- `createJob(name, xml)` - Create a test job
- `deleteJob(name)` - Delete a job
- `triggerBuild(jobName, params)` - Trigger a build
- `waitForBuild(jobName, buildNumber)` - Wait for build completion
- `executeGroovyScript(script)` - Execute Groovy in Jenkins
- `getJobConfig(name)` - Get job configuration XML
- `getBuildConsole(jobName, buildNumber)` - Get build output

### Mock Services

#### LocalStack (AWS)
- Endpoint: `http://localhost:4566`
- Services: EC2, S3, IAM, STS
- Credentials: `test/test`

#### Azurite (Azure)
- Blob: `http://localhost:10000`
- Queue: `http://localhost:10001`
- Table: `http://localhost:10002`

#### PostgreSQL
- URL: `jdbc:postgresql://localhost:5432/jenkins`
- User: `jenkins`
- Password: `jenkins`

## CI/CD Integration

### GitHub Actions
```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  integration-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Run integration tests
        run: ./gradlew dockerIntegrationTest
        
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: build/reports/tests/
```

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    
    stages {
        stage('Integration Test') {
            steps {
                sh './gradlew dockerIntegrationTest'
            }
        }
    }
    
    post {
        always {
            junit 'build/test-results/integrationTest/*.xml'
            publishHTML([
                reportDir: 'build/reports/tests/integrationTest',
                reportFiles: 'index.html',
                reportName: 'Integration Test Report'
            ])
        }
    }
}
```

## Debugging

### View Container Logs
```bash
# All containers
docker-compose -f docker-compose.integration.yml logs

# Specific service
docker-compose -f docker-compose.integration.yml logs jenkins

# Follow logs
docker-compose -f docker-compose.integration.yml logs -f
```

### Access Jenkins UI
When tests are running, Jenkins is available at http://localhost:8080
- Username: `admin`
- Password: `admin`

### Debug Mode
```bash
# Run with debug logging
./gradlew integrationTest --debug

# Run with test output
./gradlew integrationTest --info -Dtest.showStandardStreams=true
```

### Common Issues

1. **Port conflicts**: Change ports in docker-compose.integration.yml
2. **Slow startup**: Increase timeout in BaseIntegrationTest
3. **Permission errors**: Ensure Docker daemon is accessible
4. **Network issues**: Check Docker network configuration

## Best Practices

1. **Test Isolation**: Each test should be independent
2. **Cleanup**: Always clean up resources (automatic with base class)
3. **Meaningful Names**: Use descriptive test method names
4. **Fast Feedback**: Keep tests focused and fast
5. **Mock External Services**: Use WireMock or container mocks
6. **Parallel Execution**: Design tests to run in parallel

## Performance Tips

1. **Reuse Environment**: Use `-Dkeep.environment=true` to keep containers running
2. **Selective Testing**: Use `--tests` to run specific tests
3. **Local Development**: Start environment once, run tests multiple times
4. **Resource Limits**: Configure Docker resource limits for consistency

## Extending the Framework

### Adding New Mock Services
1. Add service to docker-compose.integration.yml
2. Configure health check
3. Add utility methods to BaseIntegrationTest
4. Document usage

### Custom Assertions
```groovy
protected void assertJobExists(String jobName) {
    String result = executeGroovyScript(
        "Jenkins.get().getItem('${jobName}') != null"
    )
    assertTrue("Job ${jobName} should exist", 
        Boolean.parseBoolean(result.trim()))
}
```

## Troubleshooting

### Enable Debug Logging
```groovy
// In test setup
Logger.getLogger("").level = Level.ALL
```

### Container Health Checks
```bash
docker-compose -f docker-compose.integration.yml ps
```

### Reset Environment
```bash
docker-compose -f docker-compose.integration.yml down -v
docker system prune -f
```