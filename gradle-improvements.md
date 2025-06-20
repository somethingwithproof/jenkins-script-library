# Gradle Build Improvements

## Recommended Changes

### 1. Remove Configuration-Time Output
Replace:
```groovy
println "Jenkins Core: ${versions.jenkinsCore}"
```
With:
```groovy
tasks.register('printVersions') {
    doLast {
        println "Jenkins Core: ${versions.jenkinsCore}"
    }
}
```

### 2. Standardize Test Framework
Use consistent test framework configuration:
```groovy
test {
    useJUnitPlatform()  // If using JUnit 5
    // or useJUnit()    // If using JUnit 4
}
```

### 3. Add Missing Best Practices

#### Publishing Configuration
```groovy
publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            
            pom {
                name = 'Jenkins Script Library'
                description = project.description
                url = 'https://github.com/thomasvincent/jenkins-script-library'
                
                licenses {
                    license {
                        name = 'Apache License 2.0'
                        url = 'https://www.apache.org/licenses/LICENSE-2.0'
                    }
                }
                
                scm {
                    url = 'https://github.com/thomasvincent/jenkins-script-library'
                    connection = 'scm:git:git://github.com/thomasvincent/jenkins-script-library.git'
                    developerConnection = 'scm:git:ssh://github.com/thomasvincent/jenkins-script-library.git'
                }
            }
        }
    }
}
```

#### Add Javadoc and Sources JARs
```groovy
java {
    withJavadocJar()
    withSourcesJar()
}
```

#### Version Management from Properties
```groovy
version = project.hasProperty('projectVersion') ? project.projectVersion : '1.0.0-SNAPSHOT'
```

#### Dependency Vulnerability Scanning
Add to plugins:
```groovy
id 'org.owasp.dependencycheck' version '8.4.0'
```

#### Reproducible Builds
```groovy
tasks.withType(AbstractArchiveTask) {
    preserveFileTimestamps = false
    reproducibleFileOrder = true
}
```

### 4. Fix Test Reporting
```groovy
tasks.withType(Test) {
    reports {
        html.required = true
        junitXml.required = true
    }
    
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
        exceptionFormat = 'full'
    }
}
```

### 5. Add Gradle Task Dependencies Visualization
```groovy
tasks.register('taskTree') {
    doLast {
        println "Run: ./gradlew <task> taskTree"
    }
}
```

### 6. Enable Incremental Compilation
```groovy
tasks.withType(GroovyCompile) {
    groovyOptions.optimizationOptions.indy = true
    options.incremental = true
}
```

### 7. Add Build Scans (Optional)
```groovy
plugins {
    id 'com.gradle.build-scan' version '3.15.1'
}

buildScan {
    termsOfServiceUrl = 'https://gradle.com/terms-of-service'
    termsOfServiceAgree = 'yes'
}
```

## Build Health Monitoring

Add these tasks for build health:

```groovy
tasks.register('buildHealth') {
    dependsOn 'dependencyUpdates', 'dependencyCheckAnalyze'
    description = 'Performs dependency updates and security checks'
}

tasks.register('dependencyUpdates', DependencyUpdatesTask) {
    rejectVersionIf {
        isNonStable(it.candidate.version)
    }
}

def isNonStable = { String version ->
    def stableKeyword = ['RELEASE', 'FINAL', 'GA'].any { it -> version.toUpperCase().contains(it) }
    def regex = /^[0-9,.v-]+(-r)?$/
    return !stableKeyword && !(version ==~ regex)
}
```