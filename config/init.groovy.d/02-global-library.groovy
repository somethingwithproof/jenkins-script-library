import org.jenkinsci.plugins.workflow.libs.*
import jenkins.plugins.git.GitSCMSource
import jenkins.model.Jenkins

def jenkins = Jenkins.get()
def globalLibraries = jenkins.getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries")

// Configure the script library
def library = new LibraryConfiguration(
    "jenkins-script-library",
    new SCMSourceRetriever(
        new GitSCMSource(
            null,
            "file:///var/jenkins_home/script-library",
            "*",
            "",
            "",
            false
        )
    )
)

library.defaultVersion = "main"
library.implicit = true
library.allowVersionOverride = true
library.includeInChangesets = true

// Add to global libraries
globalLibraries.libraries = [library]
globalLibraries.save()

println "Global library 'jenkins-script-library' configured"