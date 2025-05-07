# Upgrade Guide

## Upgrading to Version 1.0.0

### Overview

Version 1.0.0 is the first stable release of the Jenkins Script Library. This guide provides information about compatibility and upgrade considerations.

### Groovy and Java Versions

The library targets Groovy 4.0.x and Java 17+, which are both officially supported versions. It has been specifically tested with:

- Groovy 4.0.14 (recommended)
- Java 17 LTS
- Jenkins 2.361.1 and newer

### Dependency Changes

If you were using pre-release versions, note that the dependency coordinates have changed:

```groovy
// Previous (pre-release)
implementation 'com.github.thomasvincent:jenkins-scripts:0.x.y'

// New (1.0.0)
implementation 'com.github.thomasvincent:jenkins-script-library:1.0.0'
```

### API Changes

- All classes now follow a standard structure with proper package organization
- All utility classes now follow SOLID principles with explicit dependencies
- Script entry points are now in the `com.github.thomasvincent.jenkinsscripts.scripts` package

### Migration from Legacy Scripts

If you were using the individual scripts from previous versions:

1. Replace direct script usage with corresponding class usage:
   - `clean-build-history.groovy` → `com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner`
   - `slave-list-all-slave-nodes.groovy` → `com.github.thomasvincent.jenkinsscripts.nodes.SlaveInfoManager`
   - `slave-start-offline-slave-nodes.groovy` → `com.github.thomasvincent.jenkinsscripts.nodes.ComputerLauncher`

2. Adjust for new method signatures and parameter names

3. Use the script entry points in the `scripts` package for CLI usage

### Example Migrations

#### Old way (direct script usage):
```groovy
evaluate(new File("clean-build-history.groovy"))
```

#### New way (library usage):
```groovy
import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
import jenkins.model.Jenkins

def jenkins = Jenkins.get()
def cleaner = new JobCleaner(jenkins, "my-job", true, 25, 100)
cleaner.clean()
```