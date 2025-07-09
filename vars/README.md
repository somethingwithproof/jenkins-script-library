# Jenkins Shared Library Variables

This directory contains global variables that can be used in Jenkins pipelines when this library is loaded.

## Usage

To use this shared library in your Jenkins pipeline:

```groovy
@Library('jenkins-script-library') _

pipeline {
    agent any
    stages {
        stage('Example') {
            steps {
                // Call any global variable defined in this directory
                echo "Using Jenkins Script Library"
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