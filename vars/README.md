# Jenkins Shared Library Variables

Project overview and status: [jenkins-script-library](../README.md).

This directory contains global variables that can be used in Jenkins pipelines when this library is loaded.

## Usage

A trusted Pipeline can compare installed plugins with a manifest checked into the workspace.

```groovy
@Library('jenkins-script-library') _

pipeline {
    agent any
    stages {
        stage('Audit controller plugins') {
            steps {
                script {
                    def report = auditPlugins(expected: readFile('config/plugins.txt'))
                    if (report.drift) {
                        error "Plugin drift: missing=${report.missing}, extra=${report.extra}, mismatched=${report.mismatched}"
                    }
                }
            }
        }
    }
}
```

## Available Variables

Place your global pipeline variables here as `.groovy` files. Each file becomes a callable step in your pipeline.

Example structure:

- `vars/myStep.groovy` - Defines a `myStep()` function available in pipelines
- `vars/myStep.txt` - Optional documentation for the step

## Best Practices

1. Keep global variables simple and focused
2. Document each variable with a corresponding `.txt` file
3. Use proper error handling
4. Follow Groovy naming conventions
5. Test your shared library variables thoroughly

## Controller administration

Call `auditPlugins()` from a trusted Jenkinsfile to return the installed plugins.
Pass manifest text with `auditPlugins(expected: readFile('config/plugins.txt'))`
to compare versions. The step returns plain lists and maps for use after a
Pipeline suspension. The caller decides whether drift fails the build.

Use `destination` for the exact controller-side ZIP filename or new uncompressed
directory. Use `destinationDir` to create a uniquely named backup inside a parent
directory, including a parent that does not exist yet. With neither option, backups
go under `JENKINS_HOME/backups`. For compressed backups, an existing directory
also works as a parent. An exact target must be available when the backup is published.

```groovy
backupJenkinsConfig(destination: '/srv/jenkins-backups/nightly.zip')
backupJenkinsConfig(destinationDir: '/srv/jenkins-backups', compress: false)
```

With `archive: true`, Jenkins fingerprints and archives the completed backup from
the controller. The agent workspace receives no copy. Full backups can contain
credentials and controller keys. Use artifact archiving only on administrative jobs
whose artifact readers are authorized to access the full backup.

To preview a selective restore on the controller, call
`backupJenkinsConfig.restore(backupFile: archive, files: ['config.xml'], dryRun: true)`.
The preview returns the selected paths. Applying the restore requires Jenkins
administrator permission and preserves copies of changed files.
