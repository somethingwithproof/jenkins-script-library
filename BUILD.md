# Build Instructions

## Requirements
- Java 17+ (Java 17 recommended, Java 24+ not supported by Groovy 3.0)
- Git

## Building

**IMPORTANT**: Always use the Gradle wrapper (`./gradlew`) instead of system Gradle:

```bash
# Build and run tests
./gradlew build

# Run tests only
./gradlew test

# Clean build artifacts
./gradlew clean

# Validate library structure
./gradlew validateLibrary
```

## Why use ./gradlew?

The Gradle wrapper ensures:
- Correct Gradle version (8.14.2)
- Correct Java version (17)
- Consistent builds across environments

Using system `gradle` may fail with errors like:
```
Unsupported class file major version 68
```

This happens when system Gradle uses Java 24+, which is incompatible with Groovy 3.0.22.

## Project Structure

This is a Jenkins Script Library where:
- Scripts in `src/main/groovy` are loaded directly by Jenkins at runtime
- Only utility classes in `util/` package are compiled for testing
- No JAR is produced - Jenkins loads scripts as source code

## Gradle 8 Features Used

- Version catalogs (`gradle/libs.versions.toml`)
- Type-safe project accessors
- Lazy configuration APIs
- Repository management in settings.gradle