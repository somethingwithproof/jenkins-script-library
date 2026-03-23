# Jenkins Script Library Development and Testing Image
#
# This Dockerfile creates an environment for developing and testing the Jenkins
# Script Library. It includes Jenkins, Groovy, and necessary tools.
#
# Build: docker build -t jenkins-script-library:latest .
# Run:   docker run -d -p 8080:8080 -p 50000:50000 --name jenkins-script-library jenkins-script-library:latest
# Dockerfile

FROM jenkins/jenkins:2.541.3-lts-jdk17

LABEL maintainer="you@example.com"
LABEL description="Jenkins Script Library Dev Environment"

USER root

# Set env vars
ENV GRADLE_VERSION=8.14.2 \
    JENKINS_HOME=/var/jenkins_home \
    GRADLE_HOME=/opt/gradle

# Install tools
RUN apt-get update && apt-get install -y \
        curl \
        unzip \
        git \
        wget \
        jq \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O /tmp/gradle.zip \
    && unzip /tmp/gradle.zip -d /opt \
    && ln -s /opt/gradle-${GRADLE_VERSION} /opt/gradle \
    && rm /tmp/gradle.zip

ENV PATH="${PATH}:${GRADLE_HOME}/bin"

# Copy source library
COPY --chown=jenkins:jenkins . /var/jenkins_library

# Entrypoint script
COPY --chown=jenkins:jenkins docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Version script
COPY --chown=jenkins:jenkins version.sh /usr/local/bin/version.sh
RUN chmod +x /usr/local/bin/version.sh

USER jenkins

# Install essential Jenkins plugins
RUN jenkins-plugin-cli --plugins \
        credentials \
        git \
        workflow-aggregator \
        pipeline-groovy-lib \
        groovy \
        script-security \
        matrix-auth \
        cloudbees-folder

# Entrypoint
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD ["jenkins"]
