# Compatibility Information

## Supported Language Versions

This library is officially tested and supported on:

- **Groovy**: 4.0.x (current latest supported version)
- **Java**: JDK 17 LTS (current LTS release)
- **Jenkins**: 2.361.1+

## Groovy Version Compatibility

Groovy 4.0.x is used as it is the current stable and supported version of Groovy. Key benefits include:

- Modern language features (records, enhanced pattern matching, switch expressions)
- Active maintenance and security updates through 2025
- Improved performance
- Better integration with Java 17 features

## Verification Status

The library has been tested and verified to work with:

```
Groovy 4.0.14 on Java 17.0.9 (Temurin)
```

All Groovy 4.0.x specific features have been tested and function correctly, including:
- Records
- Switch expressions 
- Enhanced pattern matching
- Modern Groovy DSL capabilities

## Java Version Compatibility

Java 17 LTS is used as it is:

- A Long-Term Support release with guaranteed updates until 2029
- Required by newer Jenkins versions
- Provides significant performance improvements
- Includes modern language features and security enhancements

## Jenkins Compatibility Notes

When using this library with Jenkins:

1. Make sure your Jenkins instance is running on Java 11 minimum (Java 17 recommended)
2. For full feature support, Jenkins 2.361.1 or newer is required
3. Some plugins might require updates to be compatible with this library

## Development Environment

For development, we recommend:

- Java 17 SDK
- Groovy 4.0.14 or newer
- Gradle 7.6.2 or newer
- An IDE with Groovy support (IntelliJ IDEA, Eclipse, etc.)

## Building for Other Environments

If you need to use this library in environments with different version requirements:

- For Groovy 3.x compatibility: you may need to modify certain imports and API usage
- For Java 11 support: change the `sourceCompatibility` and `targetCompatibility` in `build.gradle`
- For older Jenkins versions: use the library with caution, as not all features may work