// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

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
import com.cloudbees.groovy.cps.NonCPS
import groovy.transform.CompileStatic
import com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory
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

    private final CloudInventory inventory
    private final Jenkins jenkins
    private final Map<String, CloudProvider> providers = [:]
    private final Map<String, AgentUsageData> usageData = new ConcurrentHashMap<>()

    // Cost thresholds
    private static final double IDLE_COST_THRESHOLD = 0.10 // $/hour
    private static final int IDLE_TIME_THRESHOLD = 30 // minutes
    private static final double SPOT_SAVINGS_THRESHOLD = 0.30 // 30% savings

    CloudCostOptimizer(Jenkins jenkins, CloudInventory inventory = new CloudInventory()) {
        this.inventory = inventory
        this.jenkins = jenkins
        initializeProviders()
    }

    /**
     * Analyzes cloud costs and generates optimization recommendations.
     */
    @NonCPS
    Map<String, Object> analyzeCosts(Map<String, Object> options = [:]) {
        Map<String, Object> providerResults = [:]
        Set<String> activeComputerNames = new HashSet<>()
        jenkins.computers.each { Computer c ->
            if (c?.name != null && getProvider(c.node) != null) {
                activeComputerNames.add(c.name)
            }
        }
        pruneRemovedComputers(activeComputerNames)

        providers.each { String name, CloudProvider provider ->
            providerResults[name] = analyzeProvider(provider)
        }

        Map<String, Object> analysis = [
            timestamp: Instant.now().toString(),
            providers: providerResults,
            totalCost: calculateTotalCosts(providerResults),
            savings: identifySavings(providerResults),
            recommendations: [],
            allocations: [:]
        ] as Map<String, Object>

        analysis.recommendations = generateRecommendations(analysis)

        if (options.includeAllocations) {
            analysis.allocations = calculateCostAllocations()
        }

        return analysis
    }

    @NonCPS
    private void pruneRemovedComputers(Set<String> activeComputerNames) {
        usageData.keySet().removeIf { String name -> !activeComputerNames.contains(name) }
    }

    /**
     * Optimizes cloud resources based on current usage.
     */
    @NonCPS
    Map<String, Object> optimizeResources(boolean dryRun = true) {
        List<Map<String, Object>> actions = []
        double estimatedSavings = 0.0d
        Set<String> terminatedAgents = new HashSet<>()

        analyzeCosts()
        // Find idle agents
        List<Map<String, Object>> idleAgents = findIdleAgents()
        idleAgents.each { Map<String, Object> agent ->
            double hourlyCost = ((Number) agent.hourlyCost).doubleValue()
            String agentName = agent.name as String
            Map<String, Object> action = [
                type: 'TERMINATE_IDLE',
                agent: agentName,
                provider: agent.provider,
                idleTime: agent.idleMinutes,
                hourlyCost: hourlyCost,
                savingsPerHour: hourlyCost
            ] as Map<String, Object>


            terminatedAgents.add(agentName)
            actions.add(action)
            estimatedSavings += hourlyCost
        }

        // Find spot instance opportunities, excluding agents slated for termination
        List<Map<String, Object>> spotOpportunities = findSpotOpportunities(terminatedAgents)
        spotOpportunities.each { Map<String, Object> opp ->
            double currentCost = ((Number) opp.currentCost).doubleValue()
            double spotCost = ((Number) opp.spotCost).doubleValue()
            double savingsPerHour = currentCost - spotCost
            Map<String, Object> action = [
                type: 'CONVERT_TO_SPOT',
                agent: opp.agent,
                provider: opp.provider,
                currentCost: currentCost,
                spotCost: spotCost,
                savingsPerHour: savingsPerHour
            ] as Map<String, Object>


            actions.add(action)
            estimatedSavings += savingsPerHour
        }

        // Right-size instances, excluding agents slated for termination
        List<Map<String, Object>> rightsizeOpportunities = findRightsizeOpportunities(terminatedAgents)
        rightsizeOpportunities.each { Map<String, Object> opp ->
            double currentCost = ((Number) opp.currentCost).doubleValue()
            double newCost = ((Number) opp.newCost).doubleValue()
            double savingsPerHour = currentCost - newCost
            Map<String, Object> action = [
                type: 'RIGHTSIZE_INSTANCE',
                provider: opp.provider,
                agent: opp.agent,
                currentType: opp.currentType,
                recommendedType: opp.recommendedType,
                currentCost: currentCost,
                newCost: newCost,
                savingsPerHour: savingsPerHour
            ] as Map<String, Object>


            actions.add(action)
            estimatedSavings += savingsPerHour
        }

        Map<String, Integer> counts = [planned: 0, applied: 0, unsupported: 0, failed: 0]
        for (Map<String, Object> action : actions) {
            Map identity = inventory.owner(jenkins.getComputer(action.agent as String)?.node)
            if (identity) action.putAll(identity)
            String status = 'planned'
            if (!dryRun) {
                try {
                    Boolean confirmed = providers[action.provider as String]?.applyAction(action)
                    status = confirmed == null ? 'unsupported' : (confirmed ? 'applied' : 'failed')
                } catch (Exception failure) {
                    status = 'failed'
                    action.error = failure.class.simpleName
                }
            }
            action.status = status
            counts[status] = counts[status] + 1
        }
        return [
            timestamp: Instant.now().toString(),
            dryRun: dryRun,
            actions: actions,
            counts: counts,
            estimatedSavings: estimatedSavings,
            estimatedMonthlySavings: estimatedSavings * 24.0d * 30.0d
        ] as Map<String, Object>
    }

    /**
     * Monitors budget and sends alerts.
     */
    @NonCPS
    Map<String, Object> monitorBudget(Map<String, Double> budgets) {
        Map<String, Map<String, Double>> budgetStatus = [:]
        List<Map<String, Object>> alerts = []

        Map<String, Double> currentCosts = getCurrentMonthlyCosts()

        budgets.each { String category, Double budgetVal ->
            double budget = budgetVal != null ? budgetVal.doubleValue() : 0.0d
            double spent = currentCosts[category] != null ? currentCosts[category].doubleValue() : 0.0d
            double percentUsed = budget > 0.0d
                    ? Math.round((spent / budget * 100.0d) * 100.0d) / 100.0d
                    : (spent > 0.0d ? 100.0d : 0.0d)

            budgetStatus.put(category, [
                budget: budget,
                spent: spent,
                remaining: budget - spent,
                percentUsed: percentUsed
            ])

            if (percentUsed >= 90.0d) {
                alerts.add([
                    severity: 'HIGH',
                    category: category,
                    message: "Budget alert: ${category} has used ${percentUsed}% of monthly budget"
                ] as Map<String, Object>)
            } else if (percentUsed >= 75.0d) {
                alerts.add([
                    severity: 'MEDIUM',
                    category: category,
                    message: "Budget warning: ${category} has used ${percentUsed}% of monthly budget"
                ] as Map<String, Object>)
            }
        }

        return [
            timestamp: Instant.now().toString(),
            budgetStatus: budgetStatus,
            alerts: alerts
        ] as Map<String, Object>
    }

    /**
     * Tracks costs by team/project/pipeline.
     */
    @NonCPS
    Map<String, Object> trackCostAllocation() {
        Map<String, Double> byTeam = [:]
        Map<String, Double> byProject = [:]
        Map<String, Double> byPipeline = [:]
        double unallocated = 0.0d

        jenkins.computers.each { Computer computer ->
            Node node = computer?.node
            if (node != null && isCloudNode(node)) {
                double cost = getHourlyCost(node)
                String labels = node.labelString ?: ''

                String team = extractLabelValue(labels, 'team')
                if (team) {
                    byTeam[team] = (byTeam[team] ?: 0.0d) + cost
                }

                String project = extractLabelValue(labels, 'project')
                if (project) {
                    byProject[project] = (byProject[project] ?: 0.0d) + cost
                }

                def executor = computer.executors?.find { it.isBusy() }
                if (executor?.currentExecutable instanceof hudson.model.Run) {
                    hudson.model.Run build = (hudson.model.Run) executor.currentExecutable
                    String pipeline = build.parent.fullName
                    byPipeline[pipeline] = (byPipeline[pipeline] ?: 0.0d) + cost
                }

                if (!team && !project) {
                    unallocated += cost
                }
            }
        }

        return [
            byTeam: byTeam,
            byProject: byProject,
            byPipeline: byPipeline,
            unallocated: unallocated
        ] as Map<String, Object>
    }

    /**
     * Analyzes a single cloud provider.
     */
    @NonCPS
    private Map<String, Object> analyzeProvider(CloudProvider provider) {
        int activeAgents = 0
        double totalCost = 0.0d
        List<Map<String, Object>> instances = []

        jenkins.computers.each { Computer computer ->
            if (computer?.node != null && isProviderNode(computer.node, provider)) {
                Map<String, Object> instance = analyzeInstance(computer, provider)
                instances.add(instance)
                activeAgents++
                totalCost += ((Number) instance.hourlyCost).doubleValue()
            }
        }

        return [
            name: provider.name,
            activeAgents: activeAgents,
            totalCost: totalCost,
            monthlyCost: totalCost * 24.0d * 30.0d,
            instances: instances
        ] as Map<String, Object>
    }

    /**
     * Analyzes a single instance.
     */
    @NonCPS
    private Map<String, Object> analyzeInstance(Computer computer, CloudProvider provider) {
        Node node = computer.node
        String instanceType = getInstanceType(node)
        Map<String, Object> instance = [
            name: computer.name,
            type: instanceType,
            provider: provider.name,
            hourlyCost: provider.getHourlyCost(instanceType),
            isSpot: isSpotInstance(node),
            labels: node.labelString ?: '',
            uptime: getUptime(computer),
            idleTime: getIdleTime(computer),
            utilization: calculateUtilization(computer)
        ] as Map<String, Object>

        updateUsageData(computer.name, instance)

        return instance
    }

    /**
     * Finds idle agents that can be terminated.
     */
    @NonCPS
    private List<Map<String, Object>> findIdleAgents() {
        List<Map<String, Object>> idleAgents = []

        usageData.each { String name, AgentUsageData data ->
            if (data.idleMinutes > IDLE_TIME_THRESHOLD &&
                data.hourlyCost > IDLE_COST_THRESHOLD) {
                idleAgents.add([
                    name: name,
                    provider: data.provider,
                    idleMinutes: data.idleMinutes,
                    hourlyCost: data.hourlyCost
                ] as Map<String, Object>)
            }
        }

        return idleAgents
    }

    /**
     * Finds opportunities to use spot instances, skipping excluded agents.
     */
    @NonCPS
    private List<Map<String, Object>> findSpotOpportunities(Set<String> excludedAgents = Collections.emptySet()) {
        List<Map<String, Object>> opportunities = []

        jenkins.computers.each { Computer computer ->
            Node node = computer?.node
            if (node != null && !excludedAgents.contains(computer.name)
                    && isCloudNode(node) && !isSpotInstance(node)) {
                CloudProvider provider = getProvider(node)
                String instanceType = getInstanceType(node)
                double currentCost = provider.getHourlyCost(instanceType)
                double spotCost = provider.getSpotPrice(instanceType)

                if (currentCost > 0.0d && spotCost < currentCost * (1.0d - SPOT_SAVINGS_THRESHOLD)) {
                    opportunities.add([
                        agent: computer.name,
                        provider: provider.name,
                        instanceType: instanceType,
                        currentCost: currentCost,
                        spotCost: spotCost,
                        savingsPercent: (currentCost - spotCost) / currentCost
                    ] as Map<String, Object>)
                }
            }
        }

        return opportunities
    }

    /**
     * Finds opportunities to rightsize instances, skipping excluded agents.
     */
    @NonCPS
    private List<Map<String, Object>> findRightsizeOpportunities(Set<String> excludedAgents = Collections.emptySet()) {
        List<Map<String, Object>> opportunities = []

        usageData.each { String name, AgentUsageData data ->
            if (!excludedAgents.contains(name) && data.avgUtilization < 30.0d) {
                Computer computer = jenkins.getComputer(name)
                if (computer?.node != null) {
                    CloudProvider provider = getProvider(computer.node)
                    String currentType = getInstanceType(computer.node)
                    String recommendedType = provider.recommendInstanceType(data.avgUtilization, currentType)

                    if (recommendedType != currentType) {
                        opportunities.add([
                            agent: name,
                            currentType: currentType,
                            provider: provider.name,
                            recommendedType: recommendedType,
                            currentCost: provider.getHourlyCost(currentType),
                            newCost: provider.getHourlyCost(recommendedType),
                            utilization: data.avgUtilization
                        ] as Map<String, Object>)
                    }
                }
            }
        }

        return opportunities
    }

    /**
     * Calculates total costs across providers.
     */
    @NonCPS
    private Map<String, Double> calculateTotalCosts(Map<String, Object> providersMap) {
        double hourly = 0.0d

        providersMap.each { String name, Object providerObj ->
            Map<String, Object> provider = (Map<String, Object>) providerObj
            hourly += ((Number) provider.totalCost).doubleValue()
        }

        double daily = hourly * 24.0d
        double monthly = daily * 30.0d
        double yearly = monthly * 12.0d

        return [
            hourly: hourly,
            daily: daily,
            monthly: monthly,
            yearly: yearly
        ]
    }

    /**
     * Identifies potential savings without double-counting idle agents.
     */
    @NonCPS
    private Map<String, Double> identifySavings(Map<String, Object> providersMap) {
        double idleAgentsSavings = 0.0d
        double spotSavings = 0.0d
        double rightsizingSavings = 0.0d
        Set<String> idleAgentNames = new HashSet<>()

        findIdleAgents().each { Map<String, Object> agent ->
            idleAgentNames.add(agent.name as String)
            idleAgentsSavings += ((Number) agent.hourlyCost).doubleValue()
        }

        findSpotOpportunities(idleAgentNames).each { Map<String, Object> opp ->
            spotSavings += ((Number) opp.currentCost).doubleValue() - ((Number) opp.spotCost).doubleValue()
        }

        findRightsizeOpportunities(idleAgentNames).each { Map<String, Object> opp ->
            rightsizingSavings += ((Number) opp.currentCost).doubleValue() - ((Number) opp.newCost).doubleValue()
        }

        double total = idleAgentsSavings + spotSavings + rightsizingSavings
        return [
            idleAgents: idleAgentsSavings,
            spotInstances: spotSavings,
            rightsizing: rightsizingSavings,
            total: total,
            monthlyTotal: total * 24.0d * 30.0d
        ]
    }

    /**
     * Generates cost optimization recommendations.
     */
    @NonCPS
    private List<Map<String, Object>> generateRecommendations(Map<String, Object> analysis) {
        List<Map<String, Object>> recommendations = []
        Map<String, Double> savings = (Map<String, Double>) analysis.savings

        double idleSavings = savings.idleAgents ?: 0.0d
        if (idleSavings > 0.0d) {
            recommendations.add([
                type: 'IDLE_AGENTS',
                priority: 'HIGH',
                message: "Terminate idle agents to save \$${String.format('%.2f', idleSavings)}/hour",
                impact: 'Immediate cost reduction with no impact on active builds'
            ] as Map<String, Object>)
        }

        double spotSavings = savings.spotInstances ?: 0.0d
        if (spotSavings > 0.0d) {
            recommendations.add([
                type: 'SPOT_INSTANCES',
                priority: 'MEDIUM',
                message: "Convert to spot instances to save \$${String.format('%.2f', spotSavings)}/hour",
                impact: 'Significant savings with minimal risk for non-critical workloads'
            ] as Map<String, Object>)
        }

        double rightsizeSavings = savings.rightsizing ?: 0.0d
        if (rightsizeSavings > 0.0d) {
            recommendations.add([
                type: 'RIGHTSIZING',
                priority: 'MEDIUM',
                message: "Rightsize underutilized instances to save \$${String.format('%.2f', rightsizeSavings)}/hour",
                impact: 'Optimize resource allocation based on actual usage'
            ] as Map<String, Object>)
        }

        Map<String, Object> providersMap = (Map<String, Object>) analysis.providers
        providersMap?.each { String name, Object providerObj ->
            Map<String, Object> provider = (Map<String, Object>) providerObj
            List instances = (List) provider.instances
            if (instances != null && instances.size() > 10) {
                recommendations.add([
                    type: 'RESERVED_INSTANCES',
                    priority: 'LOW',
                    message: "Consider reserved instances for ${name} to save up to 40%",
                    impact: 'Long-term savings for predictable workloads'
                ] as Map<String, Object>)
            }
        }

        return recommendations
    }

    /**
     * Calculates cost allocations.
     */
    @NonCPS
    private Map<String, Object> calculateCostAllocations() {
        return trackCostAllocation()
    }

    /**
     * Gets current monthly costs in a single pass over active cloud computers so total is never double-counted.
     */
    @NonCPS
    private Map<String, Double> getCurrentMonthlyCosts() {
        Map<String, Double> costs = [:]
        double totalHourly = 0.0d
        double monthlyFactor = 24.0d * 30.0d

        jenkins.computers.each { Computer computer ->
            Node node = computer?.node
            if (node != null && isCloudNode(node)) {
                double hourlyCost = getHourlyCost(node)
                totalHourly += hourlyCost
                String labels = node.labelString ?: ''

                String team = extractLabelValue(labels, 'team')
                if (team) {
                    String key = "team:${team}"
                    costs[key] = (costs[key] ?: 0.0d) + (hourlyCost * monthlyFactor)
                }

                String project = extractLabelValue(labels, 'project')
                if (project) {
                    String key = "project:${project}"
                    costs[key] = (costs[key] ?: 0.0d) + (hourlyCost * monthlyFactor)
                }
            }
        }

        costs['total'] = totalHourly * monthlyFactor
        return costs
    }

    /**
     * Updates usage data atomically for an instance.
     */
    @NonCPS
    private void updateUsageData(String name, Map<String, Object> instance) {
        usageData.compute(name) { String key, AgentUsageData existing ->
            AgentUsageData data = existing ?: new AgentUsageData()
            data.name = name
            data.provider = instance.provider as String
            data.hourlyCost = ((Number) instance.hourlyCost).doubleValue()
            data.idleMinutes = ((Number) instance.idleTime).intValue()
            data.totalMinutes += 1
            data.utilizationSum += ((Number) instance.utilization).doubleValue()
            data.avgUtilization = data.utilizationSum / data.totalMinutes
            return data
        }
    }

    /**
     * Gets hourly cost for a cloud node.
     */
    @NonCPS
    private double getHourlyCost(Node node) {
        CloudProvider provider = getProvider(node)
        return provider.getHourlyCost(getInstanceType(node))
    }

    /**
     * Initializes cloud providers.
     */
    @NonCPS
    private void initializeProviders() {
        providers['AWS'] = new AWSProvider()
        providers['Azure'] = new AzureProvider()
        providers['GCP'] = new GCPProvider()
        providers['Kubernetes'] = new KubernetesProvider()
    }

    /**
     * Checks if node is a cloud node.
     */
    @NonCPS
    private boolean isCloudNode(Node node) {
        return getProvider(node) != null
    }

    @NonCPS
    private boolean isProviderNode(Node node, CloudProvider provider) {
        return getProvider(node) == provider
    }

    @NonCPS
    private CloudProvider getProvider(Node node) {
        return providers.get(inventory.owner(node)?.get('provider') as String)
    }

    /**
     * Gets instance type from node.
     */
    @NonCPS
    private String getInstanceType(Node node) {
        return extractLabelValue(node.labelString ?: '', 'instance-type') ?: 'm5.large'
    }

    /**
     * Checks if node is a spot instance.
     */
    @NonCPS
    private boolean isSpotInstance(Node node) {
        String labels = node.labelString ?: ''
        return labels.contains('spot') || labels.contains('preemptible')
    }

    /**
     * Gets computer uptime in minutes.
     */
    @NonCPS
    private int getUptime(Computer computer) {
        long connectTime = computer.connectTime
        if (connectTime > 0L) {
            return Duration.between(Instant.ofEpochMilli(connectTime), Instant.now()).toMinutes().intValue()
        }
        return 0
    }

    /**
     * Gets idle time in minutes.
     */
    @NonCPS
    private int getIdleTime(Computer computer) {
        if (computer.isIdle()) {
            long lastBusyTime = computer.idleStartMilliseconds
            if (lastBusyTime > 0L) {
                return Duration.between(Instant.ofEpochMilli(lastBusyTime), Instant.now()).toMinutes().intValue()
            }
        }
        return 0
    }

    /**
     * Calculates utilization percentage.
     */
    @NonCPS
    private double calculateUtilization(Computer computer) {
        def executors = computer.executors
        if (!executors) {
            return 0.0d
        }
        int busyExecutors = executors.count { it.isBusy() }.intValue()
        return (busyExecutors * 100.0d) / executors.size()
    }

    /**
     * Extracts label value.
     */
    @NonCPS
    private String extractLabelValue(String labels, String key) {
        if (!labels) {
            return null
        }
        def matcher = (labels =~ /${java.util.regex.Pattern.quote(key)}=([^\s]+)/)
        return matcher.find() ? matcher.group(1) : null
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

        /** Null means unsupported. True requires confirmation from the provider. */
        @NonCPS
        Boolean applyAction(Map<String, Object> action) { return null }
        abstract double getHourlyCost(String instanceType)
        abstract double getSpotPrice(String instanceType)
        abstract String recommendInstanceType(double utilization, String currentType)
    }

    /**
     * AWS provider implementation.
     */
    static class AWSProvider extends CloudProvider {
        @NonCPS
        String getName() { 'AWS' }
        @NonCPS
        String getIdentifier() { 'EC2' }

        @NonCPS
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

        @NonCPS
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.6 // 40% discount average
        }

        @NonCPS
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
        @NonCPS
        String getName() { 'Azure' }
        @NonCPS
        String getIdentifier() { 'Azure' }

        @NonCPS
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

        @NonCPS
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.5 // 50% discount average
        }

        @NonCPS
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
        @NonCPS
        String getName() { 'GCP' }
        @NonCPS
        String getIdentifier() { 'GCE' }

        @NonCPS
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

        @NonCPS
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.3 // 70% discount for preemptible
        }

        @NonCPS
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
        @NonCPS
        String getName() { 'Kubernetes' }
        @NonCPS
        String getIdentifier() { 'K8s' }

        @NonCPS
        double getHourlyCost(String instanceType) {
            // Based on underlying node pool costs
            return 0.1
        }

        @NonCPS
        double getSpotPrice(String instanceType) {
            return getHourlyCost(instanceType) * 0.7
        }

        @NonCPS
        String recommendInstanceType(double utilization, String currentType) {
            return currentType // Kubernetes handles resource allocation
        }
    }
}
