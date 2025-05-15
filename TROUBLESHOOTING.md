# Troubleshooting Guide

## Known Issues

### JFFI Native Dependency Issue

**Issue:** Tests fail with the error:
```
Could not find jffi-1.2.17-native.jar (com.github.jnr:jffi:1.2.17).
Searched in the following locations:
file:/Users/thomasvincent/.m2/repository/com/github/jnr/jffi/1.2.17/jffi-1.2.17-native.jar
```

**Fix Options:**

1. **Skip tests**: Pass the `-PskipTests` property to Gradle:
   ```bash
   ./gradlew build -PskipTests
   ```

2. **Run minimal tests only**: Use the `-PrunMinimalTests` property:
   ```bash
   ./gradlew test -PrunMinimalTests
   ```

3. **Fix repository configuration**: If you need all tests to pass, you'll need to add the JFrog repository where JFFI artifacts are hosted:
   ```groovy
   // Add to your repositories in build.gradle
   maven {
       url 'https://repo.jfrog.org/artifactory/archive/'
   }
   maven {
       url 'https://repo.maven.org/archive/'
   }
   maven {
       url 'https://repository.jboss.org/nexus/content/repositories/deprecated'
   }
   ```

### JCenter Deprecation Warning

**Issue:** Warning about JCenter repository being deprecated:
```
The RepositoryHandler.jcenter() method has been deprecated. This is scheduled to be removed in Gradle 8.0.
```

**Fix:**
- Remove the JCenter repository from build.gradle
- Replace dependencies with equivalents from Maven Central

### CI/CD Pipeline Workarounds

The project has implemented the following workarounds in the CI/CD pipelines:

1. **GitHub Actions**: Skips tests that depend on JFFI native libraries
2. **Jenkinsfile**: Uses conditional testing with allowance for test failures
3. **Integration Tests**: Temporarily disabled to allow builds to pass

## Solutions in Progress

1. **Dependency Migration**: Moving away from JCenter to Maven Central
2. **Native Library Dependencies**: Investigating alternative approaches for native library integration
3. **Code Refactoring**: Isolating code that requires problematic dependencies

## Reporting Issues

If you encounter additional issues, please create a GitHub issue with:
1. The exact error message
2. Your environment details (OS, Java version, Gradle version)
3. Steps to reproduce the issue