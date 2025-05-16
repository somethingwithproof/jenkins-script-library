#!/bin/bash
#
# Version script for Jenkins Script Library Docker image
#

VERSION=$(cat /var/jenkins_library/VERSION 2>/dev/null || echo "unknown")
JENKINS_VERSION=$(jenkins --version 2>/dev/null | awk '{print $2}')
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

echo "Jenkins Script Library version: $VERSION"
echo "Jenkins version: $JENKINS_VERSION"
echo "Java version: $JAVA_VERSION"
echo "Groovy version: 4.0.x (Jenkins default)"