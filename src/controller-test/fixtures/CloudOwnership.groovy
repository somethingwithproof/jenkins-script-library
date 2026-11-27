// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
import hudson.model.Node
import hudson.plugins.ec2.AmazonEC2Cloud
import hudson.plugins.ec2.EC2OndemandSlave
import hudson.plugins.ec2.UnixData
import hudson.slaves.JNLPLauncher
import hudson.slaves.RetentionStrategy
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave
import org.csanchez.jenkins.plugins.kubernetes.PodTemplate
import jenkins.model.Jenkins

// The agents only supply configuration metadata. Never connect them to a provider.
def controller = rule.jenkins
Jenkins.AUTOMATIC_AGENT_LAUNCH = false
def aws = ['east', 'west'].collect { name ->
    new AmazonEC2Cloud(name, false, null, 'us-east-1', null, '0', [], null, null)
}
def kube = new KubernetesCloud('cluster')
controller.clouds.addAll(aws + [kube])
def nodes = []
aws.eachWithIndex { cloud, i ->
    nodes << new EC2OndemandSlave("fixture-${i}", 'fixture', '/tmp', 1, '', Node.Mode.EXCLUSIVE,
        '', '/tmp', '', '', false, '0', '', '', [], cloud.name, 0, new UnixData(null, null, null, null, null))
}
nodes << KubernetesSlave.builder().name('fixture-pod').podTemplate(new PodTemplate()).cloud(kube)
    .computerLauncher(new JNLPLauncher()).retentionStrategy(RetentionStrategy.NOOP).build()
nodes.each { node ->
    node.setLauncher(new JNLPLauncher())
    node.setRetentionStrategy(RetentionStrategy.NOOP)
    controller.addNode(node)
}
def staticAgent = rule.createSlave('cloud-static-12345678', 'aws cloud kubernetes', null)
def loader = new GroovyClassLoader(controller.pluginManager.uberClassLoader)
loader.addClasspath(new File(project, 'src').absolutePath)
def inventory = loader.loadClass('com.github.thomasvincent.jenkinsscripts.cloud.CloudInventory').newInstance()
def report = inventory.inspect(controller)
assert report.agents.collectEntries { [(it.agentId): it.cloudName] } ==
    [(nodes[0].nodeName): aws[0].name, (nodes[1].nodeName): aws[1].name, 'fixture-pod': kube.name]
assert report.clouds.count { it.provider == 'AWS' } == 2
assert !inventory.owner(staticAgent)
