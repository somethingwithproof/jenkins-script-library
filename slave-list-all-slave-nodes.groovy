import hudson.model.*
import hudson.plugins.ec2.*

/**
 * This method prints information about an EC2 slave.
 *
 * @param item The EC2 slave to print information about.
 */
def printInstanceInfo(Slave item) {

  if (item instanceof EC2AbstractSlave) {
    def instanceInfo = [
      /**
       * The name of the EC2 instance.
       */
      name: item.computer.name.replaceAll("\\(.*\\)", ""),

      /**
       * The private IP address of the EC2 instance.
       */
      privateIp: item.computer.describeInstance().getPrivateIpAddress(),

      /**
       * The instance ID of the EC2 instance.
       */
      instanceId: item.computer.describeInstance().getInstanceId(),

      /**
       * The AMI ID of the EC2 instance.
       */
      amiId: item.computer.describeInstance().getImageId(),

      /**
       * The instance type of the EC2 instance.
       */
      instanceType: item.computer.describeInstance().getInstanceType(),

      /**
       * The launch time of the EC2 instance.
       */
      launchTime: item.computer.describeInstance().getLaunchTime().format('YYYY-MM-dd HH:mm'),

      /**
       * Whether or not the EC2 instance is offline.
       */
      offline: item.computer.offline
    ]

    println(instanceInfo)
  } else {
    def instanceInfo = [
      /**
       * The name of the slave.
       */
      name: item.computer.name,

      /**
       * The host name of the slave.
       */
      hostName: item.computer.hostName,

      /**
       * Whether or not the slave is offline.
       */
      offline: item.computer.offline
    ]

    println(instanceInfo)
  }
}
