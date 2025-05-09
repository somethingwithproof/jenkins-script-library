# Jenkins Script Library Integration Tests

This directory contains integration tests for the Jenkins Script Library that verify the functionality with a real Jenkins environment.

## Overview

Integration tests are critical to ensure that the scripts work correctly in a real Jenkins environment. They test the interaction with Jenkins-specific APIs and behaviors that can't be easily mocked in unit tests.

### Test Features

The integration tests verify:

1. **Job Management**
   - Cleaning build history
   - Disabling jobs
   - Resetting build numbers

2. **Node Management**
   - Listing slave nodes
   - Starting offline slave nodes
   - Querying node status

3. **Security Auditing**
   - User account auditing
   - Permission checks
   - API token validation

4. **Configuration Backup**
   - Jenkins config backup
   - Job config backup
   - Plugin config backup

5. **Helm Installation**
   - Tool installation
   - Version detection
   - Installation validation

## Running the Tests

### Using Gradle

Integration tests can be run using the Gradle integration test task:

```bash
./gradlew integrationTest
```

### Running Specific Tests

To run specific integration tests:

```bash
./gradlew integrationTest --tests "com.github.thomasvincent.jenkinsscripts.jobs.*"
```

### Test Environment

The integration tests use `JenkinsRule` to create a temporary Jenkins instance for testing. This provides a realistic environment without needing a running Jenkins server.

## Test Implementation Details

### JenkinsRule

Tests use the `JenkinsRule` JUnit rule, which:
- Creates a temporary Jenkins instance for each test
- Handles setup and teardown automatically
- Provides methods for creating test jobs and nodes

### Test Structure

Each integration test follows this pattern:
1. Setup test data (jobs, nodes, users)
2. Execute the script functionality
3. Verify the results

### Docker-based Testing

For more comprehensive tests, you can use the Docker environment:

1. Build the Docker image:
   ```bash
   docker build -t jenkins-script-library:test .
   ```

2. Start Jenkins with the library:
   ```bash
   docker-compose up -d
   ```

3. Access Jenkins at http://localhost:8080/

4. Test scripts manually through the Jenkins Script Console

## Adding New Tests

When adding new scripts, include corresponding integration tests by:

1. Creating a new test class in the appropriate package
2. Using JenkinsRule to set up a test environment
3. Verifying all key features of the script
4. Documenting test assumptions and limitations

## Test Coverage Goals

- Maintain at least 85% code coverage
- All public classes should have integration tests
- Test both success paths and error handling
- Verify script behavior with real Jenkins APIs