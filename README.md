# Jenkins Scripts Repository

This repository contains a collection of Groovy scripts designed to automate and facilitate various operations within Jenkins environments. The focus is on scripts that enhance Jenkins functionality, including managing build histories, build triggers, and slave nodes. While these scripts were originally created some years ago, there is an ongoing effort to update, test, and ensure they adhere to current Jenkins and Groovy best practices.

## Overview

The provided scripts offer functionalities such as:

- **Cleaning build histories** to improve Jenkins server performance and manage disk space efficiently.
- **Managing build triggers** to streamline the build process and maintain a clean job configuration.
- **Listing and starting Jenkins slave nodes** to ensure optimal resource utilization and reduce manual intervention for managing agents.

## Scripts

The scripts are categorized based on their primary function:

- **Build History Management**
  - `clean-build-history.groovy`: Purges old builds from specified jobs to free up space.

- **Build Trigger Management**
  - `jobs-clean-build-triggers.groovy`: Removes specified build triggers from jobs, aiding in the maintenance of job configurations.

- **Slave Node Management**
  - `slave-list-all-slave-nodes.groovy`: Lists all configured slave nodes, providing a quick overview of available agents.
  - `slave-start-offline-slave-nodes.groovy`: Automatically starts offline slave nodes, ensuring all agents are ready for use.

## Usage

To run these scripts, you will need Groovy installed on your machine and the Jenkins CLI configured for your Jenkins server. Scripts can be executed using the Groovy command line interface:

```bash
groovy <script-name.groovy> [parameters]

Please ensure you have the appropriate permissions within your Jenkins environment to execute the operations performed by the scripts.

## Contributing

I welcome contributions to this repository! If you'd like to contribute:

## Fork the repository.
Create a new branch for your contribution.
Submit a pull request with a detailed description of your changes and any relevant tests.
Contributions should adhere to the existing coding style and include documentation as appropriate.

## License

This repository and its contents are licensed under the MIT License - see the LICENSE.md file for details.
