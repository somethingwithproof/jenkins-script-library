# Compatibility Testing

This project supports current non-EOL versions of Java and Jenkins. This document explains how to test compatibility.

## Supported Environments

| Java Version | Jenkins LTS | Status |
|--------------|-------------|--------|
| Java 11      | 2.387.3     | Legacy support (being phased out) |
| Java 17      | 2.479.3+    | Primary supported environment |
| Java 21      | 2.509+      | Future-compatible environment |

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
1. Build and test with Java 11 (legacy)
2. Build and test with Java 17 (current)
3. Build and test with Java 21 (future)

### Testing Specific Environments

You can also test specific environments:

```bash
# Test with Java 11 (legacy support)
docker-compose -f docker-compose.test.yml run --rm java11

# Test with Java 17 (current LTS)
docker-compose -f docker-compose.test.yml run --rm java17

# Test with Java 21 (future compatibility)
docker-compose -f docker-compose.test.yml run --rm java21
```

## Implementation Details

The build system automatically detects the Java version and selects appropriate Jenkins dependencies:

- For Java 11, it uses Jenkins 2.387.3 (older LTS with Java 11 support)
- For Java 17+, it uses Jenkins 2.479.3+ (current LTS requiring Java 17+)
- For Java 21, it uses Jenkins 2.509+ (newest LTS with Java 21 support)

This configuration is managed in `build.gradle` with conditional logic based on the detected Java version.

## Java 8 Support Status

Java 8 support has been discontinued as it is no longer supported by current Jenkins LTS releases. If you need to run this library on Java 8, please use an older version of the library that was tested with Java 8.