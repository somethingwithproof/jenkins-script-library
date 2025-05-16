# Compatibility Information

## Supported Language Versions

This library is officially tested and supported on:

- **Groovy**: 4.0.x (Latest for Jenkins 2.4xx+)
- **Java**: JDK 17+ (Current Jenkins LTS requirement)
- **Jenkins**: 2.479.3+ (Current LTS stream)

## Groovy Version Compatibility

Groovy compatibility is maintained across versions:

- Jenkins 2.4xx+ uses Groovy 4.0.x
- Backward compatibility with Groovy 3.0.x is maintained
- Legacy support for Groovy 2.4.x is limited and not recommended

Key benefits of using modern Groovy versions:

- Enhanced performance and security
- Better error handling and diagnostics
- Access to newer language features
- Improved integration with Java 17+

## Verification Status

The library has been tested and verified to work with:

```
Groovy 4.0.x on Java 17+ (Current Jenkins LTS)
```

All Jenkins-compatible Groovy features have been tested and function correctly, including:
- Groovy DSL capabilities
- Closures and functional programming features
- Jenkins Pipeline integration
- Script security sandbox compatibility

## Java Version Compatibility

Java 17+ is the primary supported platform as it is:

- Required by current Jenkins LTS versions (2.4xx+)
- The current LTS version of Java with long-term support
- Provides significant performance and security improvements
- Standard in most enterprise environments

> **Note**: Java 11 support is maintained for compatibility but will be phased out in future releases as Jenkins moves to Java 17+ requirements.

## Jenkins Compatibility Notes

When using this library with Jenkins:

1. This library is designed to work with Jenkins 2.479.3+ using Groovy 4.0.x
2. For full feature support, the latest Jenkins LTS is recommended
3. Some plugins may require updates to be compatible with this library
4. No additional Groovy installation is needed

## Development Environment

For development, we recommend:

- Java 17 SDK (to match current Jenkins LTS requirements)
- Gradle 8.x or newer
- An IDE with Groovy support (IntelliJ IDEA, Eclipse, etc.)
- Docker for containerized testing

## Building for Other Environments

If you need to use this library in environments with different version requirements:

- For Java 11 environments: limited support is maintained but will be phased out
- For newer Java versions (Java 21+): the library should work without modifications
- For older Jenkins versions: use with caution as compatibility is not guaranteed