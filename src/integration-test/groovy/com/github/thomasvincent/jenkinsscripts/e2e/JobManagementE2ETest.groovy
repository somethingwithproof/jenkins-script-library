package com.github.thomasvincent.jenkinsscripts.e2e

import org.junit.Test
import static org.junit.Assert.*

/**
 * End-to-end integration tests for job management scripts
 */
class JobManagementE2ETest extends BaseIntegrationTest {
    
    @Test
    void testCreateJobFromTemplate() {
        // Given: A template job
        String templateJobName = "template-job"
        String templateXml = """
            <project>
                <description>Template job for testing</description>
                <keepDependencies>false</keepDependencies>
                <properties/>
                <scm class="hudson.scm.NullSCM"/>
                <builders>
                    <hudson.tasks.Shell>
                        <command>echo "Hello from template"</command>
                    </hudson.tasks.Shell>
                </builders>
                <publishers/>
                <buildWrappers/>
            </project>
        """
        
        createJob(templateJobName, templateXml)
        
        // When: Creating a new job from template
        String createScript = """
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.jobs.JobTemplate
            
            def template = new JobTemplate()
            def result = template.createFromTemplate(
                'template-job',
                'new-job-from-template',
                [
                    description: 'Created from template',
                    parameterize: true
                ]
            )
            
            return result
        """
        
        String result = executeGroovyScript(createScript)
        
        // Then: New job should be created
        assertTrue("Job creation should succeed", result.contains("SUCCESS"))
        
        String newJobConfig = getJobConfig("new-job-from-template")
        assertTrue("New job should have updated description", 
            newJobConfig.contains("Created from template"))
        
        // Cleanup
        deleteJob(templateJobName)
        deleteJob("new-job-from-template")
    }
    
    @Test
    void testDisableMultipleJobs() {
        // Given: Multiple active jobs
        List<String> jobNames = ["job1", "job2", "job3"]
        String jobXml = """
            <project>
                <description>Test job</description>
                <disabled>false</disabled>
                <builders/>
            </project>
        """
        
        jobNames.each { createJob(it, jobXml) }
        
        // When: Disabling jobs matching pattern
        String disableScript = """
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.jobs.JobDisabler
            
            def disabler = new JobDisabler()
            def result = disabler.disableJobs('job[0-9]+', 'Disabled for testing')
            
            return result
        """
        
        String result = executeGroovyScript(disableScript)
        
        // Then: All matching jobs should be disabled
        jobNames.each { jobName ->
            String config = getJobConfig(jobName)
            assertTrue("Job ${jobName} should be disabled", 
                config.contains("<disabled>true</disabled>"))
        }
        
        // Cleanup
        jobNames.each { deleteJob(it) }
    }
    
    @Test
    void testJobDependencyManagement() {
        // Given: Jobs with dependencies
        String upstreamXml = """
            <project>
                <description>Upstream job</description>
                <publishers>
                    <hudson.tasks.BuildTrigger>
                        <childProjects>downstream-job</childProjects>
                        <threshold>
                            <name>SUCCESS</name>
                        </threshold>
                    </hudson.tasks.BuildTrigger>
                </publishers>
            </project>
        """
        
        String downstreamXml = """
            <project>
                <description>Downstream job</description>
            </project>
        """
        
        createJob("upstream-job", upstreamXml)
        createJob("downstream-job", downstreamXml)
        
        // When: Analyzing dependencies
        String dependencyScript = """
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.jobs.JobDependencyManager
            
            def manager = new JobDependencyManager()
            def dependencies = manager.analyzeDependencies()
            
            return dependencies.toString()
        """
        
        String result = executeGroovyScript(dependencyScript)
        
        // Then: Dependencies should be detected
        assertTrue("Should detect upstream dependency", 
            result.contains("upstream-job"))
        assertTrue("Should detect downstream dependency", 
            result.contains("downstream-job"))
        
        // Cleanup
        deleteJob("upstream-job")
        deleteJob("downstream-job")
    }
    
    @Test
    void testBuildHistoryCleaning() {
        // Given: A job with build history
        String jobName = "job-with-history"
        createJob(jobName, """
            <project>
                <builders>
                    <hudson.tasks.Shell>
                        <command>echo "Build \${BUILD_NUMBER}"</command>
                    </hudson.tasks.Shell>
                </builders>
            </project>
        """)
        
        // Create multiple builds
        for (int i = 0; i < 5; i++) {
            int buildNumber = triggerBuild(jobName)
            waitForBuild(jobName, buildNumber)
        }
        
        // When: Cleaning old builds
        String cleanScript = """
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.jobs.JobCleaner
            
            def cleaner = new JobCleaner()
            def result = cleaner.cleanBuildHistory(
                jobNamePattern: '.*history.*',
                daysToKeep: 0,
                buildsToKeep: 2
            )
            
            return result
        """
        
        String result = executeGroovyScript(cleanScript)
        
        // Then: Only recent builds should remain
        Thread.sleep(2000) // Wait for cleanup
        
        String jobInfo = executeGroovyScript("""
            Jenkins.get().getItem('${jobName}').builds.size()
        """)
        
        assertEquals("Should keep only 2 builds", "2", jobInfo.trim())
        
        // Cleanup
        deleteJob(jobName)
    }
    
    @Test
    void testJobParameterManagement() {
        // Given: A parameterized job
        String jobName = "param-job"
        String jobXml = """
            <project>
                <properties>
                    <hudson.model.ParametersDefinitionProperty>
                        <parameterDefinitions>
                            <hudson.model.StringParameterDefinition>
                                <name>ENV</name>
                                <defaultValue>dev</defaultValue>
                            </hudson.model.StringParameterDefinition>
                        </parameterDefinitions>
                    </hudson.model.ParametersDefinitionProperty>
                </properties>
            </project>
        """
        
        createJob(jobName, jobXml)
        
        // When: Adding new parameter
        String paramScript = """
            @Library('jenkins-script-library') _
            
            import com.github.thomasvincent.jenkinsscripts.jobs.JobParameterManager
            
            def manager = new JobParameterManager()
            manager.addParameter(
                '${jobName}',
                'VERSION',
                '1.0.0',
                'Version to deploy'
            )
            
            return "SUCCESS"
        """
        
        executeGroovyScript(paramScript)
        
        // Then: New parameter should be added
        String config = getJobConfig(jobName)
        assertTrue("Should have VERSION parameter", 
            config.contains("<name>VERSION</name>"))
        assertTrue("Should have default value", 
            config.contains("<defaultValue>1.0.0</defaultValue>"))
        
        // Cleanup
        deleteJob(jobName)
    }
    
    @Override
    protected void cleanupTestResources() {
        // Clean up any remaining test jobs
        String cleanupScript = """
            Jenkins.get().items.findAll { 
                it.name.startsWith('test-') || 
                it.name.contains('-test') 
            }.each { 
                it.delete() 
            }
        """
        
        try {
            executeGroovyScript(cleanupScript)
        } catch (Exception e) {
            LOGGER.warning("Cleanup failed: ${e.message}")
        }
    }
}