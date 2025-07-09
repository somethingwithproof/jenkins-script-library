/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.thomasvincent.jenkinsscripts.jobs

import com.github.thomasvincent.jenkinsscripts.DockerIntegrationTest
import spock.lang.Unroll

/**
 * Integration tests for job management scripts.
 * 
 * Tests job creation, copying, disabling, archival, and other job-related operations.
 */
class JobManagementIntegrationTest extends DockerIntegrationTest {
    
    def setup() {
        // Create test jobs with various configurations
        createTestJob('test-active-job')
        createTestJob('test-old-job')
        createTestJob('test-template-job')
        
        // Create a job with parameters
        def parameterizedJobConfig = '''<?xml version='1.1' encoding='UTF-8'?>
<project>
  <description>Parameterized test job</description>
  <properties>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.StringParameterDefinition>
          <name>ENVIRONMENT</name>
          <description>Target environment</description>
          <defaultValue>dev</defaultValue>
        </hudson.model.StringParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY</name>
          <description>Deploy after build</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <triggers>
    <hudson.triggers.TimerTrigger>
      <spec>H/15 * * * *</spec>
    </hudson.triggers.TimerTrigger>
  </triggers>
  <builders>
    <hudson.tasks.Shell>
      <command>echo "Environment: ${ENVIRONMENT}"</command>
    </hudson.tasks.Shell>
  </builders>
</project>'''
        createTestJob('test-parameterized-job', parameterizedJobConfig)
        
        // Trigger some builds for history
        ['test-active-job', 'test-old-job'].each { job ->
            3.times { 
                httpClient.post("/job/${job}/build", "")
                Thread.sleep(500)
            }
        }
    }
    
    def "test CopyJob script creates job copy"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/CopyJob.groovy', [
            sourceJob: 'test-template-job',
            targetJob: 'test-copied-job'
        ])
        
        then:
        result != null
        result.contains('copied') || result.contains('created')
        getJobs().contains('test-copied-job')
        
        cleanup:
        deleteTestJob('test-copied-job')
    }
    
    def "test CreateJobFromTemplate script"() {
        given:
        // Create a template configuration
        def templateScript = """
import jenkins.model.Jenkins

// Store template in Jenkins for the test
Jenkins.instance.setProperty(
    new hudson.model.StringParameterValue(
        'test-template',
        '''<project>
  <description>Job created from template: \${JOB_NAME}</description>
  <builders>
    <hudson.tasks.Shell>
      <command>echo "Created from template"</command>
    </hudson.tasks.Shell>
  </builders>
</project>'''
    )
)
"""
        httpClient.executeScript(templateScript)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/CreateJobFromTemplate.groovy', [
            templateName: 'test-template',
            jobName: 'test-from-template',
            parameters: [JOB_NAME: 'test-from-template']
        ])
        
        then:
        result != null
        getJobs().contains('test-from-template')
        
        cleanup:
        deleteTestJob('test-from-template')
    }
    
    def "test DisableJobs script disables matching jobs"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/DisableJobs.groovy', [
            pattern: 'test-old-.*'
        ])
        
        then:
        result != null
        result.contains('test-old-job')
        
        when:
        def jobStatus = httpClient.get('/job/test-old-job/api/json')
        
        then:
        jobStatus.json.buildable == false
    }
    
    def "test DisableJobTriggers script removes triggers"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/DisableJobTriggers.groovy', [
            pattern: 'test-parameterized-.*'
        ])
        
        then:
        result != null
        result.contains('test-parameterized-job') || result.contains('trigger')
        
        when:
        def jobConfig = httpClient.get('/job/test-parameterized-job/config.xml')
        
        then:
        !jobConfig.content.contains('<hudson.triggers.TimerTrigger>') || 
        jobConfig.content.contains('<!-- Disabled')
    }
    
    def "test ManageJobParameters script"() {
        when:
        def listResult = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageJobParameters.groovy', [
            action: 'list',
            jobName: 'test-parameterized-job'
        ])
        
        then:
        listResult != null
        listResult.contains('ENVIRONMENT')
        listResult.contains('DEPLOY')
        
        when:
        def addResult = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageJobParameters.groovy', [
            action: 'add',
            jobName: 'test-parameterized-job',
            parameterName: 'VERSION',
            parameterType: 'string',
            defaultValue: '1.0.0'
        ])
        
        then:
        addResult != null
        addResult.contains('VERSION') || addResult.contains('added')
    }
    
    def "test CleanBuildHistory script"() {
        given:
        // Ensure builds have completed
        Thread.sleep(2000)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/CleanBuildHistory.groovy', [
            daysToKeep: 0,
            buildsToKeep: 1
        ])
        
        then:
        result != null
        result.contains('Cleaned') || result.contains('build history')
        
        when:
        def jobBuilds = httpClient.get('/job/test-active-job/api/json?tree=builds[number]')
        
        then:
        jobBuilds.json.builds.size() <= 1
    }
    
    def "test ArchiveJobs script"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ArchiveJobs.groovy', [
            pattern: 'test-old-.*',
            archiveLocation: '/tmp/jenkins-archives'
        ])
        
        then:
        result != null
        result.contains('test-old-job') || result.contains('archived')
    }
    
    def "test AnalyzeJobHealth script"() {
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/AnalyzeJobHealth.groovy')
        
        then:
        result != null
        result.contains('Job Health Analysis') || result.contains('health report')
        result.contains('test-active-job') || result.contains('test-old-job')
    }
    
    def "test OptimizeJobScheduling script"() {
        given:
        // Create jobs with various schedules
        def scheduledJobConfig = '''<?xml version='1.1' encoding='UTF-8'?>
<project>
  <triggers>
    <hudson.triggers.TimerTrigger>
      <spec>0 0 * * *</spec>
    </hudson.triggers.TimerTrigger>
  </triggers>
</project>'''
        
        createTestJob('test-scheduled-1', scheduledJobConfig)
        createTestJob('test-scheduled-2', scheduledJobConfig)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/OptimizeJobScheduling.groovy')
        
        then:
        result != null
        result.contains('Schedule Optimization') || result.contains('scheduling')
        
        cleanup:
        deleteTestJob('test-scheduled-1')
        deleteTestJob('test-scheduled-2')
    }
    
    @Unroll
    def "test job dependency management for #scenario"() {
        given:
        createTestJob('test-upstream-job')
        createTestJob('test-downstream-job')
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/ManageJobDependencies.groovy', [
            action: action,
            upstreamJob: 'test-upstream-job',
            downstreamJob: 'test-downstream-job'
        ])
        
        then:
        result != null
        result.contains(expectedContent)
        
        cleanup:
        deleteTestJob('test-upstream-job')
        deleteTestJob('test-downstream-job')
        
        where:
        scenario           | action    | expectedContent
        'add dependency'   | 'add'     | 'dependency'
        'list dependencies'| 'list'    | 'Dependencies'
    }
    
    def "test job migration between folders"() {
        given:
        // Create a folder structure
        def createFolderScript = """
import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder

def jenkins = Jenkins.instance
def folder = new Folder(jenkins, 'test-target-folder')
jenkins.add(folder, folder.name)
jenkins.save()
println "Created folder: test-target-folder"
"""
        httpClient.executeScript(createFolderScript)
        
        when:
        def result = executeScript('src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/MigrateJobs.groovy', [
            sourcePattern: 'test-template-.*',
            targetFolder: 'test-target-folder'
        ])
        
        then:
        result != null
        result.contains('migrated') || result.contains('moved')
        
        cleanup:
        // Clean up folder
        httpClient.post("/job/test-target-folder/doDelete", "")
    }
}