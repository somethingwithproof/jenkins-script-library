#!/bin/bash
#
# Docker entrypoint script for Jenkins Script Library
#
# This script configures Jenkins to use the script library, then starts Jenkins

set -e

# If first arg is jenkins, skip and run original entrypoint
if [ "$1" = "jenkins" ]; then
  exec /usr/local/bin/jenkins.sh "$@"
fi

# Setup the library
echo "Setting up Jenkins Script Library..."

# Create init script to configure the library
cat > /var/jenkins_home/init.groovy.d/add-script-library.groovy << 'EOF'
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import org.jenkinsci.plugins.workflow.libs.SCMRetriever
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever
import jenkins.plugins.git.GitSCMSource
import hudson.scm.SCM
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec

// Add the script library to Jenkins
def jenkins = Jenkins.getInstance()

// Create a library configuration
def libraryConfig = new LibraryConfiguration(
    "jenkins-script-library", 
    new SCMRetriever(new GitSCM(
        null, 
        [new BranchSpec("*/main")], 
        false, 
        [], 
        null, 
        null, 
        []
    ))
)
libraryConfig.setDefaultVersion("main")
libraryConfig.setImplicit(true)

// Get or create the global libraries configuration
def globalLibs = jenkins.getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries")
if (globalLibs) {
    def existingLibs = globalLibs.getLibraries() ?: []
    def newLibs = existingLibs.findAll { it.name != libraryConfig.name }
    newLibs.add(libraryConfig)
    globalLibs.setLibraries(newLibs)
    jenkins.save()
    println "Added Jenkins Script Library to global libraries"
} else {
    println "WARNING: Could not configure script library - GlobalLibraries descriptor not found"
}
EOF

# Start Jenkins
exec /usr/local/bin/jenkins.sh "$@"