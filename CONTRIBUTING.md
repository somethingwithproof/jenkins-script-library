# Contributing to Jenkins Script Library

Thank you for your interest in contributing to the Jenkins Script Library! This document provides guidelines for contributions and how to get started with development.

## Development Environment

### Prerequisites

- Java 11 or higher
- Groovy 2.4.x (Jenkins LTS built-in version)
- Gradle 7.6.2+
- Docker (for testing with Jenkins)

### Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR-USERNAME/jenkins-script-library.git`
3. Set up the development environment:
   ```bash
   cd jenkins-script-library
   ./gradlew build
   ```

## Using Docker for Testing

The repository includes a Docker environment for easy testing:

```bash
# Build the Docker image
docker build -t jenkins-script-library:latest .

# Start Jenkins with the library loaded
docker-compose up -d

# Access Jenkins at http://localhost:8080/
```

## Project Structure

```
jenkins-script-library/
├── src/
│   ├── main/groovy/          # Main source code
│   │   └── com/github/thomasvincent/jenkinsscripts/
│   │       ├── config/       # Configuration management 
│   │       ├── helm/         # Helm management
│   │       ├── jobs/         # Job management
│   │       ├── nodes/        # Node management
│   │       ├── scripts/      # Command-line scripts
│   │       ├── security/     # Security functions
│   │       └── util/         # Utility classes
│   ├── test/groovy/          # Unit tests
│   └── integration-test/groovy/ # Integration tests
├── build.gradle              # Gradle build configuration
```

## Coding Guidelines

1. Follow the existing code style and formatting
2. Write comprehensive JavaDoc comments for all public classes and methods
3. Add license headers to all new files
4. Include unit tests for all new features
5. Follow SOLID principles
6. Keep code DRY (Don't Repeat Yourself)
7. Use idiomatic Groovy style compatible with Jenkins

## Testing

Run tests using Gradle:

```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests
./gradlew check
```

## Creating Pull Requests

1. Create a feature branch: `git checkout -b my-new-feature`
2. Make your changes
3. Add tests for your changes
4. Run tests: `./gradlew check`
5. Commit your changes using conventional commit format: `git commit -am 'feat: add some feature'`
6. Push to the branch: `git push origin my-new-feature`
7. Submit a pull request

## Pull Request Guidelines

- Include a clear description of the changes
- Link any related issues
- Ensure all tests pass
- Update documentation if needed
- Verify that your code works with supported Groovy and Java versions

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

## License

By contributing to this project, you agree that your contributions will be licensed under the project's MIT License.