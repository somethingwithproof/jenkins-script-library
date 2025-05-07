# Contributing to Jenkins Script Library

Thank you for your interest in contributing to the Jenkins Script Library! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Contributing to Jenkins Script Library](#contributing-to-jenkins-script-library)
  - [Table of Contents](#table-of-contents)
  - [Code of Conduct](#code-of-conduct)
  - [Getting Started](#getting-started)
  - [How to Contribute](#how-to-contribute)
    - [Reporting Bugs](#reporting-bugs)
    - [Suggesting Enhancements](#suggesting-enhancements)
    - [Pull Requests](#pull-requests)
  - [Development Guidelines](#development-guidelines)
    - [Coding Standards](#coding-standards)
    - [Testing](#testing)
    - [Documentation](#documentation)
  - [Commit Guidelines](#commit-guidelines)

## Code of Conduct

This project adheres to the Contributor Covenant Code of Conduct. By participating, you are expected to uphold this code. Please read the [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
   ```bash
   git clone https://github.com/your-username/jenkins-script-library.git
   cd jenkins-script-library
   ```
3. Add the original repository as a remote to keep your fork in sync
   ```bash
   git remote add upstream https://github.com/thomasvincent/jenkins-script-library.git
   ```
4. Create a branch for your work
   ```bash
   git checkout -b feature/your-feature-name
   ```

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue using the bug report template. Include:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Jenkins version and relevant plugin versions
- Any additional context or screenshots

### Suggesting Enhancements

For feature requests or enhancements:

- Use the feature request issue template
- Clearly describe the feature and its benefits
- Provide examples of how the feature would be used
- Indicate if you're willing to help implement the feature

### Pull Requests

1. Update your fork to the latest upstream version
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```
2. Create a new branch for your changes
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Make your changes
4. Commit your changes following the [commit guidelines](#commit-guidelines)
5. Push your branch to your fork
   ```bash
   git push origin feature/your-feature-name
   ```
6. Open a pull request against the main branch of the original repository

## Development Guidelines

### Coding Standards

- Follow the [Jenkins Groovy coding style](https://wiki.jenkins.io/display/JENKINS/Groovy+Guidelines)
- Use meaningful variable and function names
- Include comments for complex logic
- Keep functions focused on a single responsibility
- Avoid hardcoded values; use constants or configuration

### Testing

- Add appropriate tests for your changes
- Ensure all tests pass before submitting a pull request
- Test your scripts in a Jenkins environment similar to where they will be used

### Documentation

- Update documentation to reflect your changes
- Include examples for new features
- Document parameters, return values, and exceptions
- Add inline comments for complex code sections

## Commit Guidelines

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Types include:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code changes that neither fix bugs nor add features
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Changes to the build process or auxiliary tools

Examples:
- `feat: add support for Jenkins Pipeline jobs`
- `fix: correct handling of special characters in job names`
- `docs: update README with new script examples`

Thank you for contributing to the Jenkins Script Library!
