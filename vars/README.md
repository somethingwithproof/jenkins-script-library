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

## Spot capacity

`provisionSpot` requests one new interruptible VM or schedules one Pod on an
interruptible pool. It runs on a Unix agent with Python 3.9 or later. Submission
uses the corresponding authenticated cloud CLI on that agent. A preview validates
the request locally and returns the provider, target and `planned` status. Pass
`apply: true` to submit. A `submitted` result includes the resource identifier.
Jenkins agent registration and readiness depend on the supplied image, launch
template or Pod configuration.

```groovy
def request = [
    provider: 'aws',
    region: 'us-east-1',
    requestId: "ci-nightly-linux-${env.BUILD_NUMBER}",
    spec: [LaunchTemplate: [LaunchTemplateId: 'lt-0123456789abcdef0', Version: '3']]
]
def plan = provisionSpot(request)
echo "Planned capacity request for ${plan.target}"
def submitted = provisionSpot(request + [apply: true])
echo "Submitted ${submitted.resourceId}"
```

| Provider | Required inputs | Interruption behavior |
| --- | --- | --- |
| `aws` | `region`, stable `requestId`, and EC2 RunInstances `spec` with `LaunchTemplate` or `ImageId` plus the normal launch settings | Requests one one-time Spot instance. The client token identifies retries. |
| `azure` | `subscription`, `resourceGroup`, `name`, and an Azure Compute VM REST `spec` with location, hardware, storage, OS and network settings | Creates a Spot VM in Azure public cloud. The create-only precondition protects existing VMs. Eviction defaults to Deallocate. The spec may select Delete. |
| `gcp` | `project`, `zone`, `name`, and `instanceTemplate` | Creates a named Spot VM with the STOP termination action and automatic restart disabled. |
| `oracle` | Commercial OCI `region`, stable `requestId`, and native LaunchInstanceDetails `spec` with compartment, availability domain, shape, source and network settings | Requests a preemptible instance with a terminate action. The boot volume is preserved by default. |
| `kubernetes` | `context`, `namespace`, `pool` and a named v1 Pod `spec` | Requires an existing interruptible pool. The Pod remains pending if matching capacity is unavailable. |

Kubernetes accepts `eks` (managed node group), `aks`, `gke` and `oke`. The step
adds the provider's required node selector and preserves existing selectors. AKS, GKE
and OKE also receive a matching scheduling toleration. A Pod that fixes a node
directly, changes the scheduler, or specifies a conflicting pool selector is rejected.

Use a stable request ID or resource name for the same logical request. After an
ambiguous provider response, check that target before retrying. Actual cloud
charges depend on the resource lifecycle and the provider's bill. Existing agents
are neither replaced nor terminated by this step.
