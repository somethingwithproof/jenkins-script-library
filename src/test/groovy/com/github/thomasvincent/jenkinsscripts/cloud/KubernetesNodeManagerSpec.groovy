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

package com.github.thomasvincent.jenkinsscripts.cloud

import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import hudson.model.Node
import hudson.model.Computer
import hudson.model.Label
import hudson.model.labels.LabelAtom
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave
import org.csanchez.jenkins.plugins.kubernetes.PodTemplate
import org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate
import spock.lang.Specification
import spock.lang.Subject

class KubernetesNodeManagerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def locationConfig = Mock(JenkinsLocationConfiguration)
    def mockK8sCloud = Mock(KubernetesCloud)
    def mockPodTemplate = Mock(PodTemplate)
    def mockContainer = Mock(ContainerTemplate)
    def mockKubeSlave = Mock(KubernetesSlave)
    def mockComputer = Mock(Computer)
    def mockLabel = Mock(Label)

    @Subject
    def kubeManager

    def setup() {
        // Setup JenkinsLocationConfiguration
        JenkinsLocationConfiguration.metaClass.static.get = { return locationConfig }
        locationConfig.getUrl() >> "https://jenkins.example.com/"

        // Setup mockContainer
        mockContainer.getName() >> "jnlp"
        mockContainer.getImage() >> "jenkins/inbound-agent:latest"
        mockContainer.getWorkingDir() >> "/home/jenkins"
        mockContainer.getResourceLimitCpu() >> "1"
        mockContainer.getResourceLimitMemory() >> "1Gi"
        
        // Setup mockPodTemplate
        mockPodTemplate.getName() >> "kubernetes-pod"
        mockPodTemplate.getLabel() >> "kubernetes"
        mockPodTemplate.getNamespace() >> "jenkins"
        mockPodTemplate.getSlaveConnectTimeout() >> 300
        mockPodTemplate.getContainers() >> [mockContainer]
        
        // Setup mockK8sCloud
        mockK8sCloud.getName() >> "kubernetes-cloud"
        mockK8sCloud.getTemplates() >> [mockPodTemplate]
        
        // Setup mockKubeSlave
        mockKubeSlave.getNodeName() >> "kubernetes-pod-1234"
        mockKubeSlave.getNamespace() >> "jenkins"
        mockKubeSlave.getCloudName() >> "kubernetes-cloud"
        mockKubeSlave.getTemplateLabel() >> "kubernetes"
        mockKubeSlave.getPodName() >> "jenkins-agent-pod-1234"
        mockKubeSlave.getComputer() >> mockComputer
        
        // Setup mockComputer
        mockComputer.isOffline() >> false
        mockComputer.countBusy() >> 1
        
        // Setup mockLabel
        mockLabel.getName() >> "kubernetes"
        
        // Setup jenkins
        jenkins.getLabel("kubernetes") >> mockLabel
        
        // Create manager
        kubeManager = new KubernetesNodeManager(jenkins)
        kubeManager.kubeClouds = [mockK8sCloud]  // Set directly for testing
    }

    def "getKubernetesClouds should cache results"() {
        given:
        def mgr = new KubernetesNodeManager(jenkins)
        jenkins.clouds >> [mockK8sCloud]
        
        when:
        def result1 = mgr.getKubernetesClouds()
        def result2 = mgr.getKubernetesClouds()
        
        then:
        result1 == [mockK8sCloud]
        result2 == [mockK8sCloud]
        1 * jenkins.clouds // Called only once due to caching
    }
    
    def "getKubernetesClouds should handle exceptions"() {
        given:
        def mgr = new KubernetesNodeManager(jenkins)
        jenkins.clouds >> { throw new Exception("Test exception") }
        
        when:
        def result = mgr.getKubernetesClouds()
        
        then:
        result == []
    }
    
    def "isKubernetesCloudConfigured should return true when clouds exist"() {
        when:
        def result = kubeManager.isKubernetesCloudConfigured()
        
        then:
        result == true
    }
    
    def "isKubernetesCloudConfigured should return false when no clouds exist"() {
        given:
        kubeManager.kubeClouds = []
        
        when:
        def result = kubeManager.isKubernetesCloudConfigured()
        
        then:
        result == false
    }
    
    def "getKubernetesNodes should return KubernetesSlave nodes"() {
        given:
        def normalNode = Mock(Node)
        jenkins.nodes >> [mockKubeSlave, normalNode]
        
        when:
        def result = kubeManager.getKubernetesNodes()
        
        then:
        result == [mockKubeSlave]
    }
    
    def "getKubernetesNodes should handle exceptions"() {
        given:
        jenkins.nodes >> { throw new Exception("Test exception") }
        
        when:
        def result = kubeManager.getKubernetesNodes()
        
        then:
        result == []
    }
    
    def "getKubernetesNodesInfo should return node information"() {
        given:
        kubeManager.metaClass.getKubernetesNodes = { [mockKubeSlave] }
        
        when:
        def result = kubeManager.getKubernetesNodesInfo()
        
        then:
        result.size() == 1
        result[0].name == "kubernetes-pod-1234"
        result[0].kubernetes.podName == "jenkins-agent-pod-1234"
        result[0].kubernetes.namespace == "jenkins"
        result[0].kubernetes.cloudName == "kubernetes-cloud"
        result[0].kubernetes.template == "kubernetes"
    }
    
    def "getKubernetesNodesInfo should handle exceptions"() {
        given:
        kubeManager.metaClass.getKubernetesNodes = { throw new Exception("Test exception") }
        
        when:
        def result = kubeManager.getKubernetesNodesInfo()
        
        then:
        result == []
    }
    
    def "extractKubernetesNodeInfo should validate input"() {
        when:
        kubeManager.extractKubernetesNodeInfo(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "extractKubernetesNodeInfo should add Kubernetes-specific information"() {
        when:
        def result = kubeManager.extractKubernetesNodeInfo(mockKubeSlave)
        
        then:
        result.kubernetes.podName == "jenkins-agent-pod-1234"
        result.kubernetes.namespace == "jenkins"
        result.kubernetes.cloudName == "kubernetes-cloud"
        result.kubernetes.template == "kubernetes"
    }
    
    def "extractKubernetesNodeInfo should handle exceptions"() {
        given:
        mockKubeSlave.getPodName() >> { throw new Exception("Test exception") }
        
        when:
        def result = kubeManager.extractKubernetesNodeInfo(mockKubeSlave)
        
        then:
        !result.kubernetes
    }
    
    def "getPodTemplates should return templates from all clouds"() {
        given:
        def cloud2 = Mock(KubernetesCloud)
        def template2 = Mock(PodTemplate)
        cloud2.getTemplates() >> [template2]
        kubeManager.kubeClouds = [mockK8sCloud, cloud2]
        
        when:
        def result = kubeManager.getPodTemplates()
        
        then:
        result == [mockPodTemplate, template2]
    }
    
    def "getPodTemplates should handle exceptions"() {
        given:
        mockK8sCloud.getTemplates() >> { throw new Exception("Test exception") }
        
        when:
        def result = kubeManager.getPodTemplates()
        
        then:
        result == []
    }
    
    def "getPodTemplatesInfo should return template information"() {
        when:
        def result = kubeManager.getPodTemplatesInfo()
        
        then:
        result.size() == 1
        result[0].name == "kubernetes-pod"
        result[0].label == "kubernetes"
        result[0].namespace == "jenkins"
        result[0].slaveConnectTimeout == 300
        result[0].containers.size() == 1
        result[0].containers[0].name == "jnlp"
        result[0].containers[0].image == "jenkins/inbound-agent:latest"
        result[0].containers[0].workingDir == "/home/jenkins"
    }
    
    def "provisionNewPod should validate input"() {
        when:
        kubeManager.provisionNewPod(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "provisionNewPod should return false when no templates match"() {
        when:
        def result = kubeManager.provisionNewPod("nonexistent-template")
        
        then:
        result == false
    }
    
    def "provisionNewPod should return false when no clouds exist"() {
        given:
        kubeManager.kubeClouds = []
        
        when:
        def result = kubeManager.provisionNewPod("kubernetes")
        
        then:
        result == false
    }
    
    def "provisionNewPod should provision with matching template"() {
        when:
        def result = kubeManager.provisionNewPod("kubernetes")
        
        then:
        result == true
        1 * mockK8sCloud.provision(_, "kubernetes")
    }
    
    def "terminatePod should validate input"() {
        when:
        kubeManager.terminatePod(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "terminatePod should return false when pod not found"() {
        given:
        kubeManager.metaClass.getKubernetesNodesInfo = { [] }
        
        when:
        def result = kubeManager.terminatePod("nonexistent-pod")
        
        then:
        result == false
    }
    
    def "terminatePod should terminate matching pod"() {
        given:
        kubeManager.metaClass.getKubernetesNodesInfo = { [
            [name: "kubernetes-pod-1234", kubernetes: [podName: "jenkins-agent-pod-1234"]]
        ]}
        jenkins.getNode("kubernetes-pod-1234") >> mockKubeSlave
        
        when:
        def result = kubeManager.terminatePod("jenkins-agent-pod-1234")
        
        then:
        result == true
        1 * mockKubeSlave.terminate()
    }
    
    def "formatNodeInfo should handle Kubernetes-specific information"() {
        given:
        def nodeInfo = [
            name: "kubernetes-pod-1234",
            numExecutors: 1,
            labels: "kubernetes java",
            offline: false,
            kubernetes: [
                podName: "jenkins-agent-pod-1234",
                namespace: "jenkins",
                cloudName: "kubernetes-cloud",
                template: "kubernetes",
                containers: [
                    [
                        name: "jnlp",
                        image: "jenkins/inbound-agent:latest",
                        resourceLimitCpu: "1",
                        resourceLimitMemory: "1Gi",
                        workingDir: "/home/jenkins",
                        ports: ["50000:50000"]
                    ]
                ]
            ]
        ]
        
        when:
        def result = kubeManager.formatNodeInfo(nodeInfo)
        
        then:
        result.contains("Kubernetes Node: kubernetes-pod-1234")
        result.contains("Status: ONLINE")
        result.contains("Executors: 1")
        result.contains("Labels: kubernetes java")
        result.contains("Kubernetes Details:")
        result.contains("Pod Name: jenkins-agent-pod-1234")
        result.contains("Namespace: jenkins")
        result.contains("Cloud: kubernetes-cloud")
        result.contains("Template: kubernetes")
        result.contains("Containers:")
        result.contains("Name: jnlp")
        result.contains("Image: jenkins/inbound-agent:latest")
        result.contains("CPU Limit: 1")
        result.contains("Memory Limit: 1Gi")
        result.contains("Working Dir: /home/jenkins")
        result.contains("Ports: 50000:50000")
    }
}