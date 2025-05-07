# Jenkins Script Library

A collection of Groovy utilities and scripts designed to automate and facilitate various operations within Jenkins environments. This library follows Jenkins-compatible practices and includes comprehensive test coverage.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Groovy 2.4](https://img.shields.io/badge/Groovy-2.4-blue.svg)](https://groovy-lang.org/)
[![Java 11+](https://img.shields.io/badge/Java-11+-orange.svg)](https://adoptium.net/)
[![Jenkins 2.361.1+](https://img.shields.io/badge/Jenkins-2.361.1+-green.svg)](https://jenkins.io/)

## Features

- **Helm Installation Management**: Dynamically install and configure Helm in Jenkins pipelines
- **Build History Cleanup**: Manage build history to improve Jenkins performance and disk usage
- **Job Management**: Enable/disable jobs programmatically with proper security controls
- **Slave Node Management**: List, monitor, and control Jenkins slave nodes, including EC2 instances

## Project Structure

The project follows standard Groovy project structure:

```
jenkins-script-library/
├── src/
│   ├── main/groovy/            # Main source code
│   │   └── com/github/thomasvincent/jenkinsscripts/
│   │       ├── helm/           # Helm management classes
│   │       ├── jobs/           # Job management classes
│   │       ├── nodes/          # Node management classes
│   │       ├── scripts/        # Command-line script entry points
│   │       └── util/           # Utility classes
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

## Installation

This library can be used in two ways:

1. **As a dependency in your Gradle/Maven project**:
   
   ```groovy
   // In your build.gradle
   dependencies {
       implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
   }
   ```

2. **As individual scripts to run directly in Jenkins**:
   
   The scripts in `src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/` can be executed directly in Jenkins Script Console or through the Jenkins CLI.

## Usage

### Command-line Scripts

All command-line scripts support the `--help` option to display usage information.

#### Cleaning Build History

```bash
groovy CleanBuildHistory.groovy --limit 50 --reset my-jenkins-job
```

#### Disabling Jobs

```bash
# Disable a specific job
groovy DisableJobs.groovy my-jenkins-job

# Disable all buildable jobs
groovy DisableJobs.groovy --all
```

#### Managing Slave Nodes

```bash
# List all slave nodes
groovy ListSlaveNodes.groovy --all

# List a specific slave node with detailed information
groovy ListSlaveNodes.groovy my-slave-node

# Start all offline slave nodes
groovy StartOfflineSlaveNodes.groovy --all

# Start a specific offline slave node
groovy StartOfflineSlaveNodes.groovy my-slave-node
```

### Programmatic API

The library also provides a programmatic API for use in your own Groovy scripts:

```groovy
import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import jenkins.model.Jenkins

// Clean build history for a job
def jenkins = Jenkins.get()
def cleaner = new JobCleaner(jenkins, "my-job", true, 25, 100)
cleaner.clean()
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run integration tests (requires a running Jenkins instance)
./gradlew integrationTest

# Run all tests
./gradlew check
```

### Code Quality

This project uses CodeNarc for Groovy code quality checks:

```bash
./gradlew codenarcMain codenarcTest
```

## Contributing

Contributions are welcome! Please follow our [Contributing Guidelines](CONTRIBUTING.md).

## Security

For security concerns, please review our [Security Policy](SECURITY.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.