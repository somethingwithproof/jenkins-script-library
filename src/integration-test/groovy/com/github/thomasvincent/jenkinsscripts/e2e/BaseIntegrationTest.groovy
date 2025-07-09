package com.github.thomasvincent.jenkinsscripts.e2e

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import groovy.json.JsonSlurper
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.logging.Logger

/**
 * Base class for end-to-end integration tests with Docker environment
 * 
 * Follows Google style guide and Jenkins idiomatic patterns
 */
abstract class BaseIntegrationTest {
    
    private static final Logger LOGGER = Logger.getLogger(BaseIntegrationTest.class.name)
    private static final int JENKINS_PORT = 8080
    private static final String JENKINS_URL = "http://localhost:${JENKINS_PORT}"
    private static final String JENKINS_USER = "admin"
    private static final String JENKINS_PASSWORD = "admin"
    
    @Rule
    public TestName testName = new TestName()
    
    protected HttpClient httpClient
    protected String jenkinsUrl
    protected String authHeader
    protected JsonSlurper jsonSlurper
    
    // Docker compose for full environment
    protected static DockerComposeContainer environment
    
    // WireMock for mocking external services
    protected WireMockServer wireMockServer
    
    @BeforeClass
    static void setupEnvironment() {
        // Start Docker Compose environment if not already running
        if (System.getenv("JENKINS_URL") == null) {
            environment = new DockerComposeContainer(
                new File("docker-compose.integration.yml"))
                .withExposedService("jenkins", JENKINS_PORT,
                    Wait.forHttp("/login")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withExposedService("localstack", 4566)
                .withExposedService("azurite", 10000)
                .withExposedService("postgres", 5432)
                
            environment.start()
        }
    }
    
    @Before
    void setup() {
        LOGGER.info("Setting up test: ${testName.methodName}")
        
        // Initialize HTTP client
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
            
        // Set Jenkins URL
        jenkinsUrl = System.getenv("JENKINS_URL") ?: JENKINS_URL
        
        // Create auth header
        String auth = "${JENKINS_USER}:${JENKINS_PASSWORD}"
        authHeader = "Basic " + Base64.encoder.encodeToString(auth.bytes)
        
        // Initialize JSON parser
        jsonSlurper = new JsonSlurper()
        
        // Start WireMock for external service mocking
        wireMockServer = new WireMockServer(8089)
        wireMockServer.start()
        WireMock.configureFor("localhost", 8089)
        
        // Wait for Jenkins to be ready
        waitForJenkins()
        
        // Create test workspace
        createTestWorkspace()
    }
    
    @After
    void teardown() {
        LOGGER.info("Tearing down test: ${testName.methodName}")
        
        try {
            // Clean up test resources
            cleanupTestResources()
            
            // Stop WireMock
            wireMockServer.stop()
        } catch (Exception e) {
            LOGGER.warning("Error during teardown: ${e.message}")
        }
    }
    
    /**
     * Wait for Jenkins to be ready
     */
    protected void waitForJenkins() {
        int retries = 30
        while (retries > 0) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("${jenkinsUrl}/api/json"))
                    .header("Authorization", authHeader)
                    .GET()
                    .build()
                    
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString())
                    
                if (response.statusCode() == 200) {
                    LOGGER.info("Jenkins is ready")
                    return
                }
            } catch (Exception e) {
                // Expected during startup
            }
            
            retries--
            Thread.sleep(2000)
        }
        
        throw new RuntimeException("Jenkins failed to start within timeout")
    }
    
    /**
     * Create test workspace and load script library
     */
    protected void createTestWorkspace() {
        // Create global pipeline library
        String libraryConfig = """
            import jenkins.plugins.git.GitSCMSource
            import org.jenkinsci.plugins.workflow.libs.*
            
            def jenkins = Jenkins.get()
            def globalLibraries = jenkins.getDescriptor(
                "org.jenkinsci.plugins.workflow.libs.GlobalLibraries")
            
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
            
            globalLibraries.libraries = [library]
            globalLibraries.save()
        """
        
        executeGroovyScript(libraryConfig)
    }
    
    /**
     * Clean up test resources
     */
    protected void cleanupTestResources() {
        // Override in subclasses for specific cleanup
    }
    
    /**
     * Execute a Groovy script in Jenkins
     */
    protected String executeGroovyScript(String script) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/scriptText"))
            .header("Authorization", authHeader)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("script=${URLEncoder.encode(script, 'UTF-8')}"))
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        if (response.statusCode() != 200) {
            throw new RuntimeException("Script execution failed: ${response.body()}")
        }
        
        return response.body()
    }
    
    /**
     * Create a test job
     */
    protected void createJob(String jobName, String jobXml) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/createItem?name=${jobName}"))
            .header("Authorization", authHeader)
            .header("Content-Type", "application/xml")
            .POST(HttpRequest.BodyPublishers.ofString(jobXml))
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to create job: ${response.statusCode()}")
        }
    }
    
    /**
     * Delete a test job
     */
    protected void deleteJob(String jobName) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/job/${jobName}/doDelete"))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()
            
        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }
    
    /**
     * Get job configuration
     */
    protected String getJobConfig(String jobName) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/job/${jobName}/config.xml"))
            .header("Authorization", authHeader)
            .GET()
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        return response.body()
    }
    
    /**
     * Trigger a job build
     */
    protected int triggerBuild(String jobName, Map<String, String> parameters = [:]) {
        String params = parameters.collect { k, v -> 
            "${k}=${URLEncoder.encode(v, 'UTF-8')}" 
        }.join('&')
        
        String url = "${jenkinsUrl}/job/${jobName}/buildWithParameters"
        if (!parameters.isEmpty()) {
            url += "?${params}"
        }
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        // Extract build number from queue item
        Thread.sleep(2000) // Wait for build to start
        return getLastBuildNumber(jobName)
    }
    
    /**
     * Get last build number
     */
    protected int getLastBuildNumber(String jobName) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/job/${jobName}/lastBuild/buildNumber"))
            .header("Authorization", authHeader)
            .GET()
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        return Integer.parseInt(response.body().trim())
    }
    
    /**
     * Wait for build to complete
     */
    protected String waitForBuild(String jobName, int buildNumber, int timeoutSeconds = 60) {
        int waited = 0
        while (waited < timeoutSeconds) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("${jenkinsUrl}/job/${jobName}/${buildNumber}/api/json"))
                .header("Authorization", authHeader)
                .GET()
                .build()
                
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString())
                
            def build = jsonSlurper.parseText(response.body())
            if (!build.building) {
                return build.result
            }
            
            Thread.sleep(1000)
            waited++
        }
        
        throw new RuntimeException("Build did not complete within timeout")
    }
    
    /**
     * Get build console output
     */
    protected String getBuildConsole(String jobName, int buildNumber) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${jenkinsUrl}/job/${jobName}/${buildNumber}/consoleText"))
            .header("Authorization", authHeader)
            .GET()
            .build()
            
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString())
            
        return response.body()
    }
    
    /**
     * Create test data
     */
    protected void setupMockServices() {
        // Setup LocalStack AWS endpoints
        System.setProperty("aws.endpointUrl", "http://localhost:4566")
        System.setProperty("aws.region", "us-east-1")
        
        // Setup Azurite endpoints
        System.setProperty("azure.storage.endpoint", "http://localhost:10000")
        
        // Setup PostgreSQL
        System.setProperty("db.url", "jdbc:postgresql://localhost:5432/jenkins")
        System.setProperty("db.user", "jenkins")
        System.setProperty("db.password", "jenkins")
    }
}