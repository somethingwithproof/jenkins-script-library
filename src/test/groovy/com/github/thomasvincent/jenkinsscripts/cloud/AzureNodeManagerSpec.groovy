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
import com.microsoft.azure.vmagent.AzureVMCloud
import com.microsoft.azure.vmagent.AzureVMAgent
import com.microsoft.azure.vmagent.AzureVMAgentTemplate
import spock.lang.Specification
import spock.lang.Subject

class AzureNodeManagerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def locationConfig = Mock(JenkinsLocationConfiguration)
    def mockAzureCloud = Mock(AzureVMCloud)
    def mockTemplate = Mock(AzureVMAgentTemplate)
    def mockAzureAgent = Mock(AzureVMAgent)
    def mockComputer = Mock(Computer)

    @Subject
    def azureManager

    def setup() {
        // Setup JenkinsLocationConfiguration
        JenkinsLocationConfiguration.metaClass.static.get = { return locationConfig }
        locationConfig.getUrl() >> "https://jenkins.example.com/"

        // Setup mockTemplate
        mockTemplate.getTemplateName() >> "windows-vm"
        mockTemplate.getLabels() >> "azure windows"
        mockTemplate.getLocation() >> "eastus"
        mockTemplate.getVirtualMachineSize() >> "Standard_D2s_v3"
        mockTemplate.getOsType() >> "Windows"
        mockTemplate.getImageTopLevelType() >> "marketplace"
        mockTemplate.getAgentLaunchMethod() >> "SSH"
        mockTemplate.getStorageAccountType() >> "Premium_LRS"
        mockTemplate.getDiskType() >> "Managed"
        mockTemplate.getNoOfParallelJobs() >> 2
        mockTemplate.getRetentionStrategy() >> Mock(Object) { getClass() >> { Class.forName("Object") } }
        
        // Setup mockAzureCloud
        mockAzureCloud.getName() >> "azure-cloud"
        mockAzureCloud.getVmTemplates() >> [mockTemplate]
        
        // Setup mockAzureAgent
        mockAzureAgent.getNodeName() >> "azure-vm-1"
        mockAzureAgent.getServicePrincipalName() >> "jenkins-sp"
        mockAzureAgent.getResourceGroupName() >> "jenkins-rg"
        mockAzureAgent.getTemplateName() >> "windows-vm"
        mockAzureAgent.getLocation() >> "eastus"
        mockAzureAgent.getVirtualMachineSize() >> "Standard_D2s_v3"
        mockAzureAgent.getOsType() >> "Windows"
        mockAzureAgent.getAgentLaunchMethod() >> "SSH"
        mockAzureAgent.getComputer() >> mockComputer
        mockAzureAgent.getRetentionStrategy() >> Mock(Object) { getClass() >> { Class.forName("Object") } }
        
        // Setup mockComputer
        mockComputer.isOffline() >> false
        mockComputer.countBusy() >> 1
        
        // Create manager
        azureManager = new AzureNodeManager(jenkins)
        azureManager.azureClouds = [mockAzureCloud]  // Set directly for testing
    }

    def "getAzureClouds should cache results"() {
        given:
        def mgr = new AzureNodeManager(jenkins)
        jenkins.clouds >> [mockAzureCloud]
        
        when:
        def result1 = mgr.getAzureClouds()
        def result2 = mgr.getAzureClouds()
        
        then:
        result1 == [mockAzureCloud]
        result2 == [mockAzureCloud]
        1 * jenkins.clouds // Called only once due to caching
    }
    
    def "getAzureClouds should handle exceptions"() {
        given:
        def mgr = new AzureNodeManager(jenkins)
        jenkins.clouds >> { throw new Exception("Test exception") }
        
        when:
        def result = mgr.getAzureClouds()
        
        then:
        result == []
    }
    
    def "isAzureCloudConfigured should return true when clouds exist"() {
        when:
        def result = azureManager.isAzureCloudConfigured()
        
        then:
        result == true
    }
    
    def "isAzureCloudConfigured should return false when no clouds exist"() {
        given:
        azureManager.azureClouds = []
        
        when:
        def result = azureManager.isAzureCloudConfigured()
        
        then:
        result == false
    }
    
    def "getAzureNodes should return AzureVMAgent nodes"() {
        given:
        def normalNode = Mock(Node)
        jenkins.nodes >> [mockAzureAgent, normalNode]
        
        when:
        def result = azureManager.getAzureNodes()
        
        then:
        result == [mockAzureAgent]
    }
    
    def "getAzureNodes should handle exceptions"() {
        given:
        jenkins.nodes >> { throw new Exception("Test exception") }
        
        when:
        def result = azureManager.getAzureNodes()
        
        then:
        result == []
    }
    
    def "getAzureNodesInfo should return node information"() {
        given:
        azureManager.metaClass.getAzureNodes = { [mockAzureAgent] }
        
        when:
        def result = azureManager.getAzureNodesInfo()
        
        then:
        result.size() == 1
        result[0].name == "azure-vm-1"
        result[0].azure.vmName == "azure-vm-1"
        result[0].azure.resourceGroupName == "jenkins-rg"
        result[0].azure.templateName == "windows-vm"
        result[0].azure.location == "eastus"
        result[0].azure.vmSize == "Standard_D2s_v3"
        result[0].azure.osType == "Windows"
    }
    
    def "getAzureNodesInfo should handle exceptions"() {
        given:
        azureManager.metaClass.getAzureNodes = { throw new Exception("Test exception") }
        
        when:
        def result = azureManager.getAzureNodesInfo()
        
        then:
        result == []
    }
    
    def "extractAzureNodeInfo should validate input"() {
        when:
        azureManager.extractAzureNodeInfo(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "extractAzureNodeInfo should add Azure-specific information"() {
        when:
        def result = azureManager.extractAzureNodeInfo(mockAzureAgent)
        
        then:
        result.azure.vmName == "azure-vm-1"
        result.azure.servicePrincipal == "jenkins-sp"
        result.azure.resourceGroupName == "jenkins-rg"
        result.azure.templateName == "windows-vm"
        result.azure.location == "eastus"
        result.azure.vmSize == "Standard_D2s_v3"
        result.azure.osType == "Windows"
        result.azure.agentLaunchMethod == "SSH"
        result.azure.retentionStrategy == "Object"
    }
    
    def "extractAzureNodeInfo should handle exceptions"() {
        given:
        mockAzureAgent.getNodeName() >> { throw new Exception("Test exception") }
        
        when:
        def result = azureManager.extractAzureNodeInfo(mockAzureAgent)
        
        then:
        !result.azure
    }
    
    def "getAzureTemplates should return templates from all clouds"() {
        given:
        def cloud2 = Mock(AzureVMCloud)
        def template2 = Mock(AzureVMAgentTemplate)
        cloud2.getVmTemplates() >> [template2]
        azureManager.azureClouds = [mockAzureCloud, cloud2]
        
        when:
        def result = azureManager.getAzureTemplates()
        
        then:
        result == [mockTemplate, template2]
    }
    
    def "getAzureTemplates should handle exceptions"() {
        given:
        mockAzureCloud.getVmTemplates() >> { throw new Exception("Test exception") }
        
        when:
        def result = azureManager.getAzureTemplates()
        
        then:
        result == []
    }
    
    def "getAzureTemplatesInfo should return template information"() {
        when:
        def result = azureManager.getAzureTemplatesInfo()
        
        then:
        result.size() == 1
        result[0].templateName == "windows-vm"
        result[0].labels == "azure windows"
        result[0].location == "eastus"
        result[0].vmSize == "Standard_D2s_v3"
        result[0].retentionStrategy == "Object"
        result[0].osType == "Windows"
        result[0].imageTopLevelType == "marketplace"
        result[0].launchMethod == "SSH"
        result[0].storageAccountType == "Premium_LRS"
        result[0].diskType == "Managed"
        result[0].noOfParallelJobs == 2
    }
    
    def "provisionNewVM should validate input"() {
        when:
        azureManager.provisionNewVM(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "provisionNewVM should return false when no templates match"() {
        when:
        def result = azureManager.provisionNewVM("nonexistent-template")
        
        then:
        result == false
    }
    
    def "provisionNewVM should return false when no clouds exist"() {
        given:
        azureManager.azureClouds = []
        
        when:
        def result = azureManager.provisionNewVM("windows-vm")
        
        then:
        result == false
    }
    
    def "provisionNewVM should provision with matching template"() {
        when:
        def result = azureManager.provisionNewVM("windows-vm")
        
        then:
        result == true
        1 * mockTemplate.provision()
    }
    
    def "cleanupVM should validate input"() {
        when:
        azureManager.cleanupVM(null)
        
        then:
        thrown(IllegalArgumentException)
    }
    
    def "cleanupVM should return false when node not found"() {
        given:
        jenkins.getNode("nonexistent-vm") >> null
        
        when:
        def result = azureManager.cleanupVM("nonexistent-vm")
        
        then:
        result == false
    }
    
    def "cleanupVM should return false when node is not an AzureVMAgent"() {
        given:
        def regularNode = Mock(Node)
        jenkins.getNode("regular-node") >> regularNode
        
        when:
        def result = azureManager.cleanupVM("regular-node")
        
        then:
        result == false
    }
    
    def "cleanupVM should clean up Azure VM"() {
        given:
        jenkins.getNode("azure-vm-1") >> mockAzureAgent
        
        when:
        def result = azureManager.cleanupVM("azure-vm-1")
        
        then:
        result == true
        1 * mockAzureAgent.deprovision()
    }
    
    def "formatNodeInfo should handle Azure-specific information"() {
        given:
        def nodeInfo = [
            name: "azure-vm-1",
            numExecutors: 2,
            labels: "azure windows",
            offline: false,
            azure: [
                vmName: "azure-vm-1",
                resourceGroupName: "jenkins-rg",
                location: "eastus",
                vmSize: "Standard_D2s_v3",
                osType: "Windows",
                agentLaunchMethod: "SSH",
                templateName: "windows-vm",
                retentionStrategy: "AzureVMCloudRetensionStrategy"
            ]
        ]
        
        when:
        def result = azureManager.formatNodeInfo(nodeInfo)
        
        then:
        result.contains("Azure VM Node: azure-vm-1")
        result.contains("Status: ONLINE")
        result.contains("Executors: 2")
        result.contains("Labels: azure windows")
        result.contains("Azure Details:")
        result.contains("VM Name: azure-vm-1")
        result.contains("Resource Group: jenkins-rg")
        result.contains("Location: eastus")
        result.contains("VM Size: Standard_D2s_v3")
        result.contains("OS Type: Windows")
        result.contains("Launch Method: SSH")
        result.contains("Template: windows-vm")
        result.contains("Retention Strategy: AzureVMCloudRetensionStrategy")
    }
}