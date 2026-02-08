# CLAUDE.md

Groovy utilities for Jenkins automation and cloud agent management.

## Stack
- Groovy 3.0
- Java 17+
- Gradle

## Build & Test
```bash
# Build
./gradlew build

# Tests
./gradlew test
./gradlew integrationTest

# Code quality
./gradlew codenarcMain codenarcTest

# Skip tests (if JFFI issues)
./gradlew build -PskipTests
```

## Docker Test
```bash
./run-tests.sh java17 30m
```
