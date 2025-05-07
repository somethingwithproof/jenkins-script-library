pipeline {
    agent {
        docker {
            image 'eclipse-temurin:17-jdk'
            args '-v /tmp:/tmp'
        }
    }
    
    options {
        timeout(time: 10, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean assemble --no-daemon'
            }
        }
        
        stage('CodeNarc Analysis') {
            steps {
                sh './gradlew codenarcMain codenarcTest --no-daemon'
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
        
        stage('Test') {
            steps {
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
        
        stage('Integration Test') {
            steps {
                sh './gradlew integrationTest --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/integrationTest/*.xml'
                }
            }
        }
        
        stage('Coverage Report') {
            steps {
                sh './gradlew jacocoTestReport --no-daemon'
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
                }
            }
        }
        
        stage('Build Package') {
            steps {
                sh './gradlew jar scriptJar --no-daemon'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}