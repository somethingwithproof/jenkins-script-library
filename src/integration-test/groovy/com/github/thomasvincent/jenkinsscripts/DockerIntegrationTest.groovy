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

package com.github.thomasvincent.jenkinsscripts

import groovy.json.JsonSlurper
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.rules.ExternalResource
import spock.lang.Specification

/**
 * Base class for Docker-based integration tests.
 * 
 * This class provides utilities for testing Jenkins scripts against a real
 * Jenkins instance running in Docker.
 */
abstract class DockerIntegrationTest extends Specification {
    
    static String jenkinsUrl
    static String jenkinsUser
    static String jenkinsToken
    static HttpClient httpClient
    
    @ClassRule
    static ExternalResource dockerEnvironment = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            // Read environment variables or use defaults
            jenkinsUrl = System.getenv('JENKINS_URL') ?: 'http://localhost:8080'
            jenkinsUser = System.getenv('JENKINS_USER') ?: 'test'
            jenkinsToken = System.getenv('JENKINS_TOKEN') ?: 'test123'
            
            httpClient = new HttpClient(jenkinsUrl, jenkinsUser, jenkinsToken)
            
            // Wait for Jenkins to be ready
            waitForJenkins()
        }
        
        private void waitForJenkins() {
            int maxAttempts = 30
            int attempt = 0
            
            while (attempt < maxAttempts) {
                try {
                    def response = httpClient.get('/api/json')
                    if (response.status == 200) {
                        println "Jenkins is ready at ${jenkinsUrl}"
                        return
                    }
                } catch (Exception e) {
                    // Jenkins not ready yet
                }
                
                attempt++
                println "Waiting for Jenkins... (attempt ${attempt}/${maxAttempts})"
                Thread.sleep(2000)
            }
            
            throw new RuntimeException("Jenkins did not become ready within timeout")
        }
    }
    
    /**
     * HTTP client for Jenkins API interactions
     */
    static class HttpClient {
        private final String baseUrl
        private final String authHeader
        
        HttpClient(String baseUrl, String user, String token) {
            this.baseUrl = baseUrl.endsWith('/') ? baseUrl[0..-2] : baseUrl
            this.authHeader = "Basic " + "${user}:${token}".bytes.encodeBase64()
        }
        
        Map get(String path) {
            def url = new URL("${baseUrl}${path}")
            def connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty('Authorization', authHeader)
            connection.setRequestProperty('Accept', 'application/json')
            
            def status = connection.responseCode
            def content = status == 200 ? 
                connection.inputStream.text : 
                connection.errorStream?.text ?: ''
            
            return [
                status: status,
                content: content,
                json: status == 200 && content ? new JsonSlurper().parseText(content) : null
            ]
        }
        
        Map post(String path, String body, String contentType = 'application/x-www-form-urlencoded') {
            def url = new URL("${baseUrl}${path}")
            def connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod('POST')
            connection.setRequestProperty('Authorization', authHeader)
            connection.setRequestProperty('Content-Type', contentType)
            connection.setDoOutput(true)
            
            connection.outputStream.withWriter { it.write(body) }
            
            def status = connection.responseCode
            def content = status == 200 ? 
                connection.inputStream.text : 
                connection.errorStream?.text ?: ''
            
            return [
                status: status,
                content: content,
                json: status == 200 && content && contentType.contains('json') ? 
                    new JsonSlurper().parseText(content) : null
            ]
        }
        
        Map executeScript(String script) {
            return post('/scriptText', "script=${URLEncoder.encode(script, 'UTF-8')}")
        }
    }
    
    /**
     * Creates a test job in Jenkins
     */
    protected String createTestJob(String jobName, String config = null) {
        def defaultConfig = '''<?xml version='1.1' encoding='UTF-8'?>
<project>
  <description>Test job created by integration test</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <scm class="hudson.scm.NullSCM"/>
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers/>
  <concurrentBuild>false</concurrentBuild>
  <builders>
    <hudson.tasks.Shell>
      <command>echo "Hello from test job"</command>
    </hudson.tasks.Shell>
  </builders>
  <publishers/>
  <buildWrappers/>
</project>'''
        
        def response = httpClient.post(
            "/createItem?name=${jobName}",
            config ?: defaultConfig,
            'application/xml'
        )
        
        if (response.status != 200) {
            throw new RuntimeException("Failed to create job: ${response.content}")
        }
        
        return jobName
    }
    
    /**
     * Deletes a test job
     */
    protected void deleteTestJob(String jobName) {
        httpClient.post("/job/${jobName}/doDelete", "")
    }
    
    /**
     * Creates a test node/agent
     */
    protected String createTestNode(String nodeName, Map config = [:]) {
        def nodeConfig = [
            name: nodeName,
            nodeDescription: config.description ?: 'Test node',
            numExecutors: config.executors ?: '1',
            remoteFS: config.remoteFS ?: '/tmp/jenkins-agent',
            labelString: config.labels ?: 'test-node',
            mode: config.mode ?: 'NORMAL',
            type: 'hudson.slaves.DumbSlave',
            launcher: [
                stapler_class: 'hudson.slaves.JNLPLauncher'
            ]
        ]
        
        def formData = nodeConfig.collect { k, v ->
            "${k}=${URLEncoder.encode(v.toString(), 'UTF-8')}"
        }.join('&')
        
        def response = httpClient.post(
            "/computer/doCreateItem",
            formData
        )
        
        if (response.status != 200) {
            throw new RuntimeException("Failed to create node: ${response.content}")
        }
        
        return nodeName
    }
    
    /**
     * Deletes a test node
     */
    protected void deleteTestNode(String nodeName) {
        httpClient.post("/computer/${nodeName}/doDelete", "")
    }
    
    /**
     * Executes a script and returns the result
     */
    protected String executeScript(String scriptPath, Map binding = [:]) {
        def scriptFile = new File(scriptPath)
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Script not found: ${scriptPath}")
        }
        
        def script = scriptFile.text
        
        // Add binding variables
        def bindingScript = binding.collect { k, v ->
            "${k} = ${v instanceof String ? "'${v}'" : v}"
        }.join('\n')
        
        def fullScript = """
${bindingScript}

${script}
"""
        
        def response = httpClient.executeScript(fullScript)
        if (response.status != 200) {
            throw new RuntimeException("Script execution failed: ${response.content}")
        }
        
        return response.content
    }
    
    /**
     * Gets the list of jobs
     */
    protected List<String> getJobs() {
        def response = httpClient.get('/api/json?tree=jobs[name]')
        if (response.status == 200 && response.json) {
            return response.json.jobs.collect { it.name }
        }
        return []
    }
    
    /**
     * Gets the list of nodes
     */
    protected List<String> getNodes() {
        def response = httpClient.get('/computer/api/json?tree=computer[displayName]')
        if (response.status == 200 && response.json) {
            return response.json.computer.collect { it.displayName }
        }
        return []
    }
    
    /**
     * Cleanup method to be called after each test
     */
    def cleanup() {
        // Clean up any test jobs
        getJobs().findAll { it.startsWith('test-') }.each {
            try {
                deleteTestJob(it)
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        // Clean up any test nodes
        getNodes().findAll { it.startsWith('test-') }.each {
            try {
                deleteTestNode(it)
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
}