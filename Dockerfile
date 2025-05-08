# Jenkins Script Library Development and Testing Image
#
# This Dockerfile creates an environment for developing and testing the Jenkins
# Script Library. It includes Jenkins, Groovy, and necessary tools.
#
# Build: docker build -t jenkins-script-library:latest .
# Run:   docker run -d -p 8080:8080 -p 50000:50000 --name jenkins-script-library jenkins-script-library:latest

FROM jenkins/jenkins:lts-jdk17

USER root

# Set environment variables
ENV GRADLE_VERSION=7.6.2 \
    JENKINS_HOME=/var/jenkins_home

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    git \
    wget \
    jq \
    && rm -rf /var/lib/apt/lists/*

# Use Jenkins' built-in Groovy version instead of installing a separate one
# This ensures compatibility with the version Jenkins uses internally

# Install Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && ln -s /opt/gradle-${GRADLE_VERSION} /opt/gradle \
    && rm /tmp/gradle.zip

# Add to PATH
ENV PATH="$PATH:/opt/gradle/bin"

# Copy library scripts
COPY --chown=jenkins:jenkins . /var/jenkins_library

# Script to install recommended plugins, setup library, and configure Jenkins
COPY --chown=jenkins:jenkins docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

USER jenkins

# Install recommended plugins
RUN jenkins-plugin-cli --plugins \
    credentials \
    git \
    workflow-aggregator \
    pipeline-groovy-lib \
    groovy \
    script-security \
    matrix-auth \
    cloudbees-folder

# Define an entrypoint that adds our library to Jenkins
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]