# Build the Jenkins shared library

Use the checked-in Gradle wrapper with JDK 21, Git and Python 3. From the repository root, run:

```bash
./gradlew build
./gradlew integrationTest
```

The controller tests load the shared library into a disposable Jenkins installation.

## Rehearse a release

```bash
./gradlew clean verifyRelease -PreleaseTag=v1.2.3
```

Use the intended version tag in place of `v1.2.3`. Local builds without a release tag use the version in `VERSION`. The task verifies the ZIP and compressed TAR in `build/distributions/` against the tracked library sources, Pipeline steps, resources, examples and templates. It also checks the compiled library JAR in `build/libs/`.

Publishing a GitHub release builds the tagged revision and attaches all three verified files. A manual run of the Release workflow rehearses the build on the selected ref.
