# Jenkins Scripts Library

[![CI](https://github.com/thomasvincent/jenkins-script-library/actions/workflows/ci.yml/badge.svg)](https://github.com/thomasvincent/jenkins-script-library/actions/workflows/ci.yml)
[![Security Scan](https://github.com/thomasvincent/jenkins-script-library/actions/workflows/security.yml/badge.svg)](https://github.com/thomasvincent/jenkins-script-library/actions/workflows/security.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This repository contains a collection of Groovy scripts designed to automate and facilitate various operations within Jenkins environments. The focus is on scripts that enhance Jenkins functionality, including managing build histories, build triggers, and slave nodes. These scripts are regularly updated and tested to ensure they adhere to current Jenkins and Groovy best practices.

## Table of Contents

- [Jenkins Scripts Library](#jenkins-scripts-library)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Scripts](#scripts)
    - [Build History Management](#build-history-management)
    - [Build Trigger Management](#build-trigger-management)
    - [Slave Node Management](#slave-node-management)
    - [Helm Installation Management](#helm-installation-management)
  - [Installation](#installation)
  - [Usage](#usage)
    - [Jenkins Script Console](#jenkins-script-console)
  - [Security Considerations](#security-considerations)
  - [Contributing](#contributing)
  - [Code of Conduct](#code-of-conduct)
  - [License](#license)

## Overview

The provided scripts offer functionalities such as:

- **Cleaning build histories** to improve Jenkins server performance and manage disk space efficiently.
- **Managing build triggers** to streamline the build process and maintain a clean job configuration.
- **Listing and starting Jenkins slave nodes** to ensure optimal resource utilization and reduce manual intervention for managing agents.

## Scripts

The scripts are categorized based on their primary function:

### Build History Management
- `clean-build-history.groovy`: Purges old builds from specified jobs to free up space and improve server performance.

### Build Trigger Management
- `jobs-clean-build-triggers.groovy`: Removes specified build triggers from jobs, helping maintain clean and efficient job configurations.

### Slave Node Management
- `slave-list-all-slave-nodes.groovy`: Lists all configured slave nodes, providing a comprehensive overview of available agents.
- `slave-start-offline-slave-nodes.groovy`: Automatically starts offline slave nodes, ensuring all agents are ready for use and reducing downtime.

### Helm Installation Management
- `HelmInstallationManager.groovy`: Manages Helm installations in Jenkins.

## Installation

Clone this repository to your local machine or Jenkins server:

```bash
git clone https://github.com/thomasvincent/jenkins-script-library.git
cd jenkins-script-library
```

## Usage

To run these scripts, you will need Groovy installed on your machine and the Jenkins CLI configured for your Jenkins server. Scripts can be executed using the Groovy command line interface:

```bash
groovy <script-name.groovy> [parameters]
```

Example:

```bash
groovy clean-build-history.groovy --job="my-jenkins-job" --keep=10
```

Please ensure you have the appropriate permissions within your Jenkins environment to execute the operations performed by the scripts.

### Jenkins Script Console

Many of these scripts can also be run directly in the Jenkins Script Console:

1. Navigate to "Manage Jenkins" > "Script Console"
2. Copy the script content
3. Modify any parameters as needed
4. Click "Run"

## Security Considerations

When using these scripts, please consider the following security best practices:

1. Always run scripts with the minimum required permissions in Jenkins
2. Review code before execution, especially when dealing with system operations
3. Keep your Jenkins instance and plugins up to date
4. Use credential binding rather than hardcoding secrets in scripts
5. Implement audit logging for script executions

For more information on security, please see our [SECURITY.md](SECURITY.md) file.

## Contributing

Contributions to this repository are welcome! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file for details on how to contribute.

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## License

This repository and its contents are licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
