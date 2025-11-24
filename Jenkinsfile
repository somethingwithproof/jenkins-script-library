pipeline {
    agent {
        docker {
            image 'eclipse-temurin:21-jdk'
            args '-v $HOME/.gradle:/root/.gradle -v /tmp:/tmp'
        }
    }
    
    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        ansiColor('xterm')
    }
    
    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true'
        JAVA_TOOL_OPTIONS = '-Dfile.encoding=UTF-8'
        GITHUB_PACKAGES_CREDS = credentials('github-packages-credentials')
        PROJECT_VERSION = readVersion()
        IS_MAIN_BRANCH = "${env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master'}"
        DEPLOY_TO_STAGING = "${env.BRANCH_NAME == 'develop' || env.IS_MAIN_BRANCH == 'true'}"
        DEPLOY_TO_PRODUCTION = "${env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main'}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Extract project version from version.properties
                    def versionProps = readProperties file: 'version.properties'
                    def versionMajor = versionProps['version.major'] ?: '0'
                    def versionMinor = versionProps['version.minor'] ?: '0'
                    def versionPatch = versionProps['version.patch'] ?: '0'
                    def versionPrerelease = versionProps['version.prerelease'] ?: ''
                    
                    env.PROJECT_VERSION = "${versionMajor}.${versionMinor}.${versionPatch}${versionPrerelease ? '-' + versionPrerelease : ''}"
                    
                    echo "Building version: ${env.PROJECT_VERSION}"
                }
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean assemble'
            }
        }
        
        stage('Code Quality') {
            parallel {
                stage('CodeNarc Analysis') {
                    steps {
                        sh './gradlew codenarc'
                    }
                    post {
                        always {
                            recordIssues(
                                tools: [
                                    codeNarc(pattern: '**/build/reports/codenarc/*.xml')
                                ]
                            )
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        // Using OWASP dependency check configured in build.gradle
                        sh './gradlew dependencyCheckAnalyze'
                    }
                    post {
                        always {
                            publishHTML(
                                target: [
                                    reportDir: 'build/reports/dependency-check-report.html',
                                    reportName: 'OWASP Dependency Check',
                                    reportFiles: 'dependency-check-report.html',
                                    keepAll: true,
                                    allowMissing: false,
                                    alwaysLinkToLastBuild: true
                                ]
                            )
                        }
                        unstable {
                            echo 'Security vulnerabilities detected but below CVSS threshold'
                        }
                        failure {
                            echo 'Critical security vulnerabilities detected!'
                        }
                    }
                }
            }
        }
        
        stage('Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        // Removed '|| true' to properly handle failures
                        sh './gradlew test -PrunMinimalTests'
                    }
                    post {
                        always {
                            junit allowEmptyResults: false, testResults: '**/build/test-results/test/*.xml'
                        }
                        failure {
                            echo 'Unit tests failed!'
                        }
                    }
                }
                
                stage('Integration Tests') {
                    steps {
                        // Enable integration tests
                        sh './gradlew integrationTest'
                    }
                    post {
                        always {
                            junit allowEmptyResults: false, testResults: '**/build/test-results/integrationTest/*.xml'
                        }
                        failure {
                            echo 'Integration tests failed!'
                        }
                    }
                }
            }
        }
        
        stage('Coverage Report') {
            steps {
                sh './gradlew jacocoTestReport'
            }
            post {
                always {
                    publishHTML(
                        target: [
                            reportDir: 'build/reports/jacoco/test/html',
                            reportName: 'JaCoCo Code Coverage',
                            reportFiles: 'index.html',
                            keepAll: true,
                            allowMissing: false,
                            alwaysLinkToLastBuild: true
                        ]
                    )
                    // Optional: fail build if coverage is too low
                    sh './gradlew jacocoTestCoverageVerification'
                }
            }
        }
        
        stage('Build Package') {
            steps {
                sh './gradlew jar javadocJar sourcesJar'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
        
        stage('Version Tagging') {
            when {
                expression { return env.IS_MAIN_BRANCH == 'true' }
            }
            steps {
                // Create a version tag in Git
                withCredentials([sshUserPrivateKey(credentialsId: 'github-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                    sh '''
                        git config user.email "jenkins@example.com"
                        git config user.name "Jenkins"
                        git tag -a "v${PROJECT_VERSION}" -m "Release version ${PROJECT_VERSION}"
                        GIT_SSH_COMMAND="ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no" git push origin "v${PROJECT_VERSION}"
                    '''
                }
            }
        }
        
        stage('Publish to GitHub Packages') {
            when {
                expression { return env.IS_MAIN_BRANCH == 'true' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-packages-credentials', 
                                  usernameVariable: 'GPR_USER', 
                                  passwordVariable: 'GPR_TOKEN')]) {
                    sh '''
                        ./gradlew publish -Pgpr.user=${GPR_USER} -Pgpr.key=${GPR_TOKEN}
                    '''
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                expression { return env.DEPLOY_TO_STAGING == 'true' }
            }
            steps {
                // Deployment to staging environment
                withCredentials([string(credentialsId: 'staging-api-key', variable: 'API_KEY')]) {
                    sh '''
                        echo "Deploying version ${PROJECT_VERSION} to Staging"
                        # Add actual deployment commands here
                        # Example: curl -H "Authorization: Bearer ${API_KEY}" -X POST https://staging-server/deploy --data "version=${PROJECT_VERSION}"
                    '''
                }
            }
            post {
                success {
                    echo "Successfully deployed to Staging"
                }
                failure {
                    echo "Failed to deploy to Staging"
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                expression { return env.DEPLOY_TO_PRODUCTION == 'true' }
                // Additional manual approval for production
                beforeInput true
            }
            input {
                message "Deploy to Production?"
                ok "Approve"
                submitter "admin,manager"
            }
            steps {
                // Deployment to production environment
                withCredentials([string(credentialsId: 'production-api-key', variable: 'API_KEY')]) {
                    sh '''
                        echo "Deploying version ${PROJECT_VERSION} to Production"
                        # Add actual deployment commands here
                        # Example: curl -H "Authorization: Bearer ${API_KEY}" -X POST https://production-server/deploy --data "version=${PROJECT_VERSION}"
                    '''
                }
            }
            post {
                success {
                    echo "Successfully deployed to Production"
                }
                failure {
                    echo "Failed to deploy to Production"
                    // Optional rollback mechanism
                    echo "Initiating rollback procedure..."
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo "Build successful! Version: ${env.PROJECT_VERSION}"
        }
        failure {
            echo "Build failed! Check the logs for details."
            // Optional: Send notifications on failure
            // mail to: 'team@example.com', subject: "Failed Pipeline: ${currentBuild.fullDisplayName}", body: "The build failed. Check ${env.BUILD_URL}"
        }
    }
}

def readVersion() {
    if (fileExists('version.properties')) {
        def versionProps = readProperties file: 'version.properties'
        def versionMajor = versionProps['version.major'] ?: '0'
        def versionMinor = versionProps['version.minor'] ?: '0'
        def versionPatch = versionProps['version.patch'] ?: '0'
        def versionPrerelease = versionProps['version.prerelease'] ?: ''
        
        return "${versionMajor}.${versionMinor}.${versionPatch}${versionPrerelease ? '-' + versionPrerelease : ''}"
    }
    return '0.0.0'
}
