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

package com.github.thomasvincent.jenkinsscripts.cost

import hudson.model.Computer
import hudson.model.Node
import hudson.model.labels.LabelAtom
import jenkins.model.Jenkins
import groovy.transform.CompileStatic
import java.time.Instant
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Optimizes cloud costs across multiple providers by analyzing usage patterns and making recommendations.
 * 
 * This class provides advanced cost optimization features:
 * - Real-time cost tracking across AWS, Azure, GCP, and Kubernetes
 * - Spot/preemptible instance recommendations
 * - Idle agent detection and termination
 * - Cost allocation by team/project/pipeline
 * - Budget monitoring and alerts
 * 
 * @author Thomas Vincent
 * @since 1.4.0
 */
@CompileStatic
class CloudCostOptimizer {
    
    private static final Logger LOGGER = Logger.getLogger(CloudCostOptimizer.class.name)
    
    private final Jenkins jenkins
    private final Map<String, CloudProvider> providers = [:]
    private final Map<String, AgentUsageData> usageData = new ConcurrentHashMap<>()
    
    // Cost thresholds
    private static final double IDLE_COST_THRESHOLD = 0.10 // $/hour
    private static final int IDLE_TIME_THRESHOLD = 30 // minutes
    private static final double SPOT_SAVINGS_THRESHOLD = 0.30 // 30% savings
    
    CloudCostOptimizer(Jenkins jenkins) {
        this.jenkins = jenkins
        initializeProviders()
    }
    
    /**
     * Analyzes cloud costs and generates optimization recommendations.
     */
    Map<String, Object> analyzeCosts(Map<String, Object> options = [:]) {
        def analysis = [
            timestamp: Instant.now().toString(),
            providers: [:],
            totalCost: [:],
            savings: [:],
            recommendations: [],
            allocations: [:]
        ]
        
        // Analyze each cloud provider
        providers.each { name, provider ->
            analysis.providers[name] = analyzeProvider(provider)
        }
        
        // Calculate totals
        analysis.totalCost = calculateTotalCosts(analysis.providers as Map)
        
        // Identify savings opportunities
        analysis.savings = identifySavings(analysis.providers as Map)
        
        // Generate recommendations
        analysis.recommendations = generateRecommendations(analysis)
        
        // Calculate cost allocations
        if (options.includeAllocations) {
            analysis.allocations = calculateCostAllocations()
        }
        
        return analysis
    }
    
    /**
     * Optimizes cloud resources based on current usage.
     */
    Map<String, Object> optimizeResources(boolean dryRun = true) {
        def results = [
            timestamp: Instant.now().toString(),
            dryRun: dryRun,
            actions: [],
            estimatedSavings: 0.0
        ]
        
        // Find idle agents
        def idleAgents = findIdleAgents()
        idleAgents.each { agent ->
            def action = [
                type: 'TERMINATE_IDLE',
                agent: agent.name,
                provider: agent.provider,
                idleTime: agent.idleMinutes,
                hourlyCost: agent.hourlyCost,
                savingsPerHour: agent.hourlyCost
            ]
            
            if (!dryRun) {
                terminateAgent(agent)
            }
            
            results.actions << action
            results.estimatedSavings += action.savingsPerHour
        }
        
        // Find spot instance opportunities
        def spotOpportunities = findSpotOpportunities()
        spotOpportunities.each { opp ->
            def action = [
                type: 'CONVERT_TO_SPOT',
                agent: opp.agent,
                provider: opp.provider,
                currentCost: opp.currentCost,
                spotCost: opp.spotCost,
                savingsPerHour: opp.currentCost - opp.spotCost
            ]
            
            if (!dryRun && opp.savingsPercent > SPOT_SAVINGS_THRESHOLD) {
                convertToSpot(opp)
            }
            
            results.actions << action
            results.estimatedSavings += action.savingsPerHour
        }
        
        // Right-size instances
        def rightsizeOpportunities = findRightsizeOpportunities()
        rightsizeOpportunities.each { opp ->
            def action = [
                type: 'RIGHTSIZE_INSTANCE',
                agent: opp.agent,
                currentType: opp.currentType,
                recommendedType: opp.recommendedType,
                currentCost: opp.currentCost,
                newCost: opp.newCost,
                savingsPerHour: opp.currentCost - opp.newCost
            ]
            
            if (!dryRun) {
                rightsizeInstance(opp)
            }
            
            results.actions << action
            results.estimatedSavings += action.savingsPerHour
        }
        
        results.estimatedMonthlySavings = results.estimatedSavings * 24 * 30
        
        return results
    }
    
    /**
     * Monitors budget and sends alerts.
     */
    Map<String, Object> monitorBudget(Map<String, Double> budgets) {
        def monitoring = [
            timestamp: Instant.now().toString(),
            budgetStatus: [:],
            alerts: []
        ]
        
        def currentCosts = getCurrentMonthlyCosts()
        
        budgets.each { category, budget ->
            def spent = currentCosts[category] ?: 0.0
            def percentUsed = (spent / budget * 100).round(2)
            
            monitoring.budgetStatus[category] = [
                budget: budget,
                spent: spent,
                remaining: budget - spent,
                percentUsed: percentUsed
            ]
            
            // Generate alerts
            if (percentUsed >= 90) {
                monitoring.alerts << [
                    severity: 'HIGH',
                    category: category,
                    message: "Budget alert: ${category} has used ${percentUsed}% of monthly budget"
                ]
            } else if (percentUsed >= 75) {
                monitoring.alerts << [
                    severity: 'MEDIUM',
                    category: category,
                    message: "Budget warning: ${category} has used ${percentUsed}% of monthly budget"
                ]
            }
        }
        
        return monitoring
    }
    
    /**
     * Tracks costs by team/project/pipeline.
     */
    Map<String, Object> trackCostAllocation() {
        def allocations = [
            byTeam: [:],
            byProject: [:],
            byPipeline: [:],
            unallocated: 0.0
        ]
        
        jenkins.computers.each { computer ->
            if (computer.node && isCloudNode(computer.node)) {
                def cost = getHourlyCost(computer.node)
                def labels = computer.node.labelString
                
                // Extract team from labels
                def team = extractLabelValue(labels, 'team')
                if (team) {
                    allocations.byTeam[team] = (allocations.byTeam[team] ?: 0.0) + cost
                }
                
                // Extract project from labels
                def project = extractLabelValue(labels, 'project')
                if (project) {
                    allocations.byProject[project] = (allocations.byProject[project] ?: 0.0) + cost
                }
                
                // Track by current build
                def executor = computer.executors.find { it.isBusy() }
                if (executor?.currentExecutable) {
                    def build = executor.currentExecutable
                    def pipeline = build.parent.fullName
                    allocations.byPipeline[pipeline] = (allocations.byPipeline[pipeline] ?: 0.0) + cost
                }
                
                // Track unallocated
                if (!team && !project) {
                    allocations.unallocated += cost
                }
            }
        }
        
        return allocations
    }
    
    /**
     * Analyzes a single cloud provider.
     */
    private Map analyzeProvider(CloudProvider provider) {
        def analysis = [
            name: provider.name,
            activeAgents: 0,
            totalCost: 0.0,
            instances: []
        ]
        
        jenkins.computers.each { computer ->
            if (computer.node && isProviderNode(computer.node, provider)) {
                def instance = analyzeInstance(computer, provider)
                analysis.instances << instance
                analysis.activeAgents++
                analysis.totalCost += instance.hourlyCost
            }
        }
        
        analysis.monthlyCost = analysis.totalCost * 24 * 30
        
        return analysis
    }
    
    /**
     * Analyzes a single instance.
     */
    private Map analyzeInstance(Computer computer, CloudProvider provider) {
        def node = computer.node
        def instance = [
            name: computer.name,
            type: getInstanceType(node),
            provider: provider.name,
            hourlyCost: provider.getHourlyCost(getInstanceType(node)),
            isSpot: isSpotInstance(node),
            labels: node.labelString,
            uptime: getUptime(computer),
            idleTime: getIdleTime(computer),
            utilization: calculateUtilization(computer)
        ]
        
        // Track usage data
        updateUsageData(computer.name, instance)
        
        return instance
    }
    
    /**
     * Finds idle agents that can be terminated.
     */
    private List<Map> findIdleAgents() {
        def idleAgents = []
        
        usageData.each { name, data ->
            if (data.idleMinutes > IDLE_TIME_THRESHOLD && 
                data.hourlyCost > IDLE_COST_THRESHOLD) {
                idleAgents << [
                    name: name,
                    provider: data.provider,
                    idleMinutes: data.idleMinutes,
                    hourlyCost: data.hourlyCost
                ]
            }
        }
        
        return idleAgents
    }
    
    /**
     * Finds opportunities to use spot instances.
     */
    private List<Map> findSpotOpportunities() {
        def opportunities = []
        
        jenkins.computers.each { computer ->
            if (computer.node && isCloudNode(computer.node) && !isSpotInstance(computer, computer.node)) {
                def provider = getProvider(computer.node)
                def instanceType = getInstanceType(computer, computer.node)
                def currentCost = provider.getHourlyCost(instanceType)
                def spotCost = provider.getSpotPrice(instanceType)
                
                if (spotCost < currentCost * (1 - SPOT_SAVINGS_THRESHOLD)) {
                    opportunities << [
                        agent: computer.name,
                        provider: provider.name,
                        instanceType: instanceType,
                        currentCost: currentCost,
                        spotCost: spotCost,
                        savingsPercent: (currentCost - spotCost) / currentCost
                    ]
                }
            }
        }
        
        return opportunities
    }
    
    /**
     * Finds opportunities to rightsize instances.
     */
    private List<Map> findRightsizeOpportunities() {
        def opportunities = []
        
        usageData.each { name, data ->
            if (data.avgUtilization < 30) {
                def computer = jenkins.getComputer(name)
                if (computer?.node) {
                    def provider = getProvider(computer.node)
                    def currentType = getInstanceType(computer, computer.node)
                    def recommendedType = provider.recommendInstanceType(data.avgUtilization, currentType)
                    
                    if (recommendedType != currentType) {
                        opportunities << [
                            agent: name,
                            currentType: currentType,
                            recommendedType: recommendedType,
                            currentCost: provider.getHourlyCost(currentType),
                            newCost: provider.getHourlyCost(recommendedType),
                            utilization: data.avgUtilization
                        ]
                    }
                }
            }
        }
        
        return opportunities
    }
    
    /**
     * Calculates total costs across providers.
     */
    private Map calculateTotalCosts(Map providers) {
        def total = [
            hourly: 0.0,
            daily: 0.0,
            monthly: 0.0,
            yearly: 0.0
        ]
        
        providers.each { name, provider ->
            total.hourly += provider.totalCost
        }
        
        total.daily = total.hourly * 24
        total.monthly = total.daily * 30
        total.yearly = total.monthly * 12
        
        return total
    }
    
    /**
     * Identifies potential savings.
     */
    private Map identifySavings(Map providers) {
        def savings = [
            idleAgents: 0.0,
            spotInstances: 0.0,
            rightsizing: 0.0,
            total: 0.0
        ]
        
        // Calculate idle agent savings
        findIdleAgents().each { agent ->
            savings.idleAgents += agent.hourlyCost
        }
        
        // Calculate spot instance savings
        findSpotOpportunities().each { opp ->
            savings.spotInstances += (opp.currentCost - opp.spotCost)
        }
        
        // Calculate rightsizing savings
        findRightsizeOpportunities().each { opp ->
            savings.rightsizing += (opp.currentCost - opp.newCost)
        }
        
        savings.total = savings.idleAgents + savings.spotInstances + savings.rightsizing
        savings.monthlyTotal = savings.total * 24 * 30
        
        return savings
    }
    
    /**
     * Generates cost optimization recommendations.
     */
    private List<Map> generateRecommendations(Map analysis) {
        def recommendations = []
        def savings = analysis.savings as Map
        
        if (savings.idleAgents > 0) {
            recommendations << [
                type: 'IDLE_AGENTS',
                priority: 'HIGH',
                message: "Terminate idle agents to save \$${savings.idleAgents.round(2)}/hour",
                impact: 'Immediate cost reduction with no impact on active builds'
            ]
        }
        
        if (savings.spotInstances > 0) {
            recommendations << [
                type: 'SPOT_INSTANCES',
                priority: 'MEDIUM',
                message: "Convert to spot instances to save \$${savings.spotInstances.round(2)}/hour",
                impact: 'Significant savings with minimal risk for non-critical workloads'
            ]
        }
        
        if (savings.rightsizing > 0) {
            recommendations << [
                type: 'RIGHTSIZING',
                priority: 'MEDIUM',
                message: "Rightsize underutilized instances to save \$${savings.rightsizing.round(2)}/hour",
                impact: 'Optimize resource allocation based on actual usage'
            ]
        }
        
        // Add provider-specific recommendations
        analysis.providers.each { name, provider ->
            if (provider.instances.size() > 10) {
                recommendations << [
                    type: 'RESERVED_INSTANCES',
                    priority: 'LOW',
                    message: "Consider reserved instances for ${name} to save up to 40%",
                    impact: 'Long-term savings for predictable workloads'
                ]
            }
        }
        
        return recommendations
    }
    
    /**
     * Calculates cost allocations.
     */
    private Map calculateCostAllocations() {
        return trackCostAllocation()
    }
    
    /**
     * Gets current monthly costs.
     */
    private Map<String, Double> getCurrentMonthlyCosts() {
        def costs = [:]
        def allocations = trackCostAllocation()
        
        // By team
        allocations.byTeam.each { team, hourlyCost ->
            costs["team:${team}"] = hourlyCost * 24 * 30
        }
        
        // By project
        allocations.byProject.each { project, hourlyCost ->
            costs["project:${project}"] = hourlyCost * 24 * 30
        }
        
        // Total
        costs['total'] = allocations.values().flatten().sum { it instanceof Number ? it : 0 } * 24 * 30
        
        return costs
    }
    
    /**
     * Updates usage data for an instance.
     */
    private void updateUsageData(String name, Map instance) {
        def data = usageData.get(name, new AgentUsageData())
        data.name = name
        data.provider = instance.provider
        data.hourlyCost = instance.hourlyCost
        data.idleMinutes = instance.idleTime
        data.totalMinutes += 1
        data.utilizationSum += instance.utilization
        data.avgUtilization = data.utilizationSum / data.totalMinutes
        usageData[name] = data
    }
    
    /**
     * Terminates an idle agent.
     */
    private void terminateAgent(Map agent) {
        LOGGER.info("Terminating idle agent: ${agent.name}")
        // Implementation would call provider API to terminate
    }
    
    /**
     * Converts instance to spot.
     */
    private void convertToSpot(Map opportunity) {
        LOGGER.info("Converting ${opportunity.agent} to spot instance")
        // Implementation would call provider API to convert
    }
    
    /**
     * Rightsizes an instance.
     */
    private void rightsizeInstance(Map opportunity) {
        LOGGER.info("Rightsizing ${opportunity.agent} from ${opportunity.currentType} to ${opportunity.recommendedType}")
        // Implementation would call provider API to resize
    }
    
    /**
     * Initializes cloud providers.
     */
    private void initializeProviders() {
        providers['AWS'] = new AWSProvider()
        providers['Azure'] = new AzureProvider()
        providers['GCP'] = new GCPProvider()
        providers['Kubernetes'] = new KubernetesProvider()
    }
    
    /**
     * Checks if node is a cloud node.
     */
    private boolean isCloudNode(Node node) {
        return node.class.name.contains('Cloud') || 
               node.labelString.contains('cloud') ||
               providers.any { name, provider -> isProviderNode(node, provider) }
    }
    
    /**
     * Checks if node belongs to a specific provider.
     */
    private boolean isProviderNode(Node node, CloudProvider provider) {
        return node.class.name.contains(provider.identifier) ||
               node.labelString.toLowerCase().contains(provider.name.toLowerCase())
    }
    
    /**
     * Gets the provider for a node.
     */
    private CloudProvider getProvider(Node node) {
        providers.values().find { provider -> isProviderNode(node, provider) } ?: providers['AWS']
    }
    
    /**
     * Gets instance type from node.
     */
    private String getInstanceType(Node node) {
        extractLabelValue(node.labelString, 'instance-type') ?: 'm5.large'
    }
    
    /**
     * Checks if node is a spot instance.
     */
    private boolean isSpotInstance(Node node) {
        node.labelString.contains('spot') || node.labelString.contains('preemptible')
    }
    
    /**
     * Gets computer uptime in minutes.
     */
    private int getUptime(Computer computer) {
        def connectTime = computer.connectTime
        if (connectTime > 0) {
            return Duration.between(Instant.ofEpochMilli(connectTime), Instant.now()).toMinutes()
        }
        return 0
    }
    
    /**
     * Gets idle time in minutes.
     */
    private int getIdleTime(Computer computer) {
        if (computer.isIdle()) {
            def lastBusyTime = computer.idleStartMilliseconds
            if (lastBusyTime > 0) {
                return Duration.between(Instant.ofEpochMilli(lastBusyTime), Instant.now()).toMinutes()
            }
        }
        return 0
    }
    
    /**
     * Calculates utilization percentage.
     */
    private double calculateUtilization(Computer computer) {
        def executors = computer.executors
        def busyExecutors = executors.count { it.isBusy() }
        return executors.size() > 0 ? (busyExecutors / executors.size() * 100) : 0
    }
    
    /**
     * Extracts label value.
     */
    private String extractLabelValue(String labels, String key) {
        def pattern = ~/${key}=([^\s]+)/
        def matcher = labels =~ pattern
        return matcher ? matcher[0][1] : null
    }
    
    /**
     * Agent usage data holder.
     */
    static class AgentUsageData {
        String name
        String provider
        double hourlyCost
        int idleMinutes
        int totalMinutes
        double utilizationSum
        double avgUtilization
    }
    
    /**
     * Base cloud provider interface.
     */
    static abstract class CloudProvider {
        abstract String getName()
        abstract String getIdentifier()
        abstract double getHourlyCost(String instanceType)
        abstract double getSpotPrice(String instanceType)
        abstract String recommendInstanceType(double utilization, String currentType)
    }
    
    /**
     * AWS provider implementation.
     */
    static class AWSProvider extends CloudProvider {
        String getName() { 'AWS' }
        String getIdentifier() { 'EC2' }
        
        double getHourlyCost(String instanceType) {
            // Simplified pricing - real implementation would use AWS API
            def prices = [
                't3.micro': 0.0104,
                't3.small': 0.0208,
                't3.medium': 0.0416,
                't3.large': 0.0832,
                'm5.large': 0.096,
                'm5.xlarge': 0.192,
                'm5.2xlarge': 0.384,
                'c5.large': 0.085,
                'c5.xlarge': 0.17
            ]
            return prices[instanceType] ?: 0.1
        }
        
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.6 // 40% discount average
        }
        
        String recommendInstanceType(double utilization, String currentType) {
            if (utilization < 20 && currentType.contains('large')) {
                return currentType.replace('large', 'medium')
            } else if (utilization < 10 && currentType.contains('medium')) {
                return currentType.replace('medium', 'small')
            }
            return currentType
        }
    }
    
    /**
     * Azure provider implementation.
     */
    static class AzureProvider extends CloudProvider {
        String getName() { 'Azure' }
        String getIdentifier() { 'Azure' }
        
        double getHourlyCost(String instanceType) {
            def prices = [
                'Standard_B1s': 0.0104,
                'Standard_B2s': 0.0416,
                'Standard_D2s_v3': 0.096,
                'Standard_D4s_v3': 0.192,
                'Standard_F2s_v2': 0.085
            ]
            return prices[instanceType] ?: 0.1
        }
        
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.5 // 50% discount average
        }
        
        String recommendInstanceType(double utilization, String currentType) {
            if (utilization < 30 && currentType.contains('D4')) {
                return currentType.replace('D4', 'D2')
            }
            return currentType
        }
    }
    
    /**
     * GCP provider implementation.
     */
    static class GCPProvider extends CloudProvider {
        String getName() { 'GCP' }
        String getIdentifier() { 'GCE' }
        
        double getHourlyCost(String instanceType) {
            def prices = [
                'f1-micro': 0.0076,
                'g1-small': 0.0257,
                'n1-standard-1': 0.0475,
                'n1-standard-2': 0.095,
                'n1-standard-4': 0.19
            ]
            return prices[instanceType] ?: 0.1
        }
        
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.3 // 70% discount for preemptible
        }
        
        String recommendInstanceType(double utilization, String currentType) {
            if (utilization < 25 && currentType == 'n1-standard-4') {
                return 'n1-standard-2'
            }
            return currentType
        }
    }
    
    /**
     * Kubernetes provider implementation.
     */
    static class KubernetesProvider extends CloudProvider {
        String getName() { 'Kubernetes' }
        String getIdentifier() { 'K8s' }
        
        double getHourlyCost(String instanceType) {
            // Based on underlying node pool costs
            return 0.1
        }
        
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.7
        }
        
        String recommendInstanceType(double utilization, String currentType) {
            return currentType // Kubernetes handles resource allocation
        }
    }
}