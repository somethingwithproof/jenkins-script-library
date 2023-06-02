import hudson.model.*
import hudson.plugins.ec2.*

def printInstanceInfo(Slave item) {

  if (item instanceof EC2AbstractSlave) {
    def instanceInfo = [
      name: item.computer.name.replaceAll("\\(.*\\)", ""),
      privateIp: item.computer.describeInstance().getPrivateIpAddress(),
      instanceId: item.computer.describeInstance().getInstanceId(),
      amiId: item.computer.describeInstance().getImageId(),
      instanceType: item.computer.describeInstance().getInstanceType(),
      launchTime: item.computer.describeInstance().getLaunchTime().format('YYYY-MM-dd HH:mm'),
      offline: item.computer.offline
    ]

    println(instanceInfo)
  } else {
    def instanceInfo = [
      name: item.computer.name,
      hostName: item.computer.hostName,
      offline: item.computer.offline
    ]

    println(instanceInfo)
  }
}

Hudson.instance.slaves.each { printInstanceInfo(it) }
