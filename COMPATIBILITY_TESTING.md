# Compatibility Testing

This project supports multiple versions of Java and Jenkins. This document explains how to test compatibility.

## Supported Environments

| Java Version | Jenkins LTS | Notes |
|--------------|-------------|-------|
| Java 8       | 2.249.3     | Legacy environment |
| Java 11      | 2.387.3     | Modern environment |
| Java 17      | 2.387.3+    | Latest environment |

## Testing with Docker

We provide a Docker-based testing suite to verify compatibility across environments.

### Prerequisites

- Docker
- Docker Compose

### Running Compatibility Tests

To test the library with all supported Java versions:

```bash
./test-java-compatibility.sh
```

This script will:
1. Build and test with Java 8
2. Build and test with Java 11
3. Build and test with Java 17

### Testing Specific Environments

You can also test specific environments:

```bash
# Test with Java 8
docker-compose -f docker-compose.test.yml run --rm java8

# Test with Java 11
docker-compose -f docker-compose.test.yml run --rm java11

# Test with Java 17
docker-compose -f docker-compose.test.yml run --rm java17
```

## Implementation Details

The build system automatically detects the Java version and selects appropriate Jenkins dependencies:

- For Java 8, it uses Jenkins 2.249.3 (last LTS that works with Java 8)
- For Java 11+, it uses Jenkins 2.387.3 (modern LTS that requires Java 11+)

This configuration is managed in `build.gradle` with conditional logic based on the detected Java version.