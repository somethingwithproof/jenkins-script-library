# Compatibility Information

## Supported Language Versions

This library is officially tested and supported on:

- **Groovy**: 2.4.x (Jenkins LTS built-in version)
- **Java**: JDK 11+ (Jenkins LTS requirement)
- **Jenkins**: 2.361.1+

## Groovy Version Compatibility

Groovy 2.4.x is used as it is the version bundled with Jenkins LTS. Key benefits include:

- Direct compatibility with Jenkins' runtime environment
- Consistent behavior across different Jenkins installations
- No need for additional Groovy installations
- Reliable execution in Jenkins Pipeline environments

## Verification Status

The library has been tested and verified to work with:

```
Groovy 2.4.x on Java 11+ (Jenkins LTS)
```

All Jenkins-compatible Groovy features have been tested and function correctly, including:
- Groovy DSL capabilities
- Closures and functional programming features
- Jenkins Pipeline integration
- Script security sandbox compatibility

## Java Version Compatibility

Java 11+ is used as it is:

- Required by current Jenkins LTS versions
- Widely supported in enterprise environments
- Compatible with most Jenkins plugins
- Provides good performance while maintaining compatibility

## Jenkins Compatibility Notes

When using this library with Jenkins:

1. This library is designed to work with Jenkins' built-in Groovy runtime (2.4.x)
2. For full feature support, Jenkins 2.361.1 or newer is required
3. Some plugins might require updates to be compatible with this library
4. No additional Groovy installation is needed

## Development Environment

For development, we recommend:

- Java 11 SDK (to match Jenkins LTS requirements)
- Groovy 2.4.x (to match Jenkins built-in version)
- Gradle 7.6.2 or newer
- An IDE with Groovy support (IntelliJ IDEA, Eclipse, etc.)

## Building for Other Environments

If you need to use this library in environments with different version requirements:

- For newer Groovy versions: the library will continue to work as it uses features compatible with Groovy 2.4+
- For Java 8 environments: some features may need adjustment, but core functionality should work
- For older Jenkins versions: use the library with caution, as not all features may work