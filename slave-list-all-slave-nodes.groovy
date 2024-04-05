import hudson.model.Slave
import hudson.model.Computer
import hudson.plugins.ec2.EC2Computer
import java.text.SimpleDateFormat
import java.util.logging.Logger

class SlaveInfoPrinter {

    private static final Logger LOGGER = Logger.getLogger(SlaveInfoPrinter.class.getName())
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    /**
     * Prints detailed information about a Jenkins slave, with specific enhancements for EC2 slaves.
     *
     * @param slave The Jenkins slave to print information about.
     */
    static void printInstanceInfo(Slave slave) {
        Computer computer = slave.computer
        Map<String, Object> instanceInfo = [:] // Use Map for dynamic keys

        try {
            if (computer instanceof EC2Computer) {
                EC2Computer ec2Computer = (EC2Computer) computer
                def instance = ec2Computer.describeInstance()

                instanceInfo.name = ec2Computer.name.replaceAll("\\(.*\\)", "")
                instanceInfo.privateIp = instance?.getPrivateIpAddress()
                instanceInfo.instanceId = instance?.getInstanceId()
                instanceInfo.amiId = instance?.getImageId()
                instanceInfo.instanceType = instance?.getInstanceType()
                instanceInfo.launchTime = DATE_FORMATTER.format(instance?.getLaunchTime())
                instanceInfo.offline = ec2Computer.offline
            } else {
                instanceInfo.name = computer.name
                instanceInfo.hostName = computer.hostName
                instanceInfo.offline = computer.offline
            }

            logInstanceInfo(instanceInfo)
        } catch (Exception e) {
            LOGGER.severe("Error fetching instance information for ${computer.name}: ${e.message}")
        }
    }

    /**
     * Logs the collected instance information.
     *
     * @param instanceInfo The information map to log.
     */
    private static void logInstanceInfo(Map<String, Object> instanceInfo) {
        String infoMessage = instanceInfo.collect { key, value -> "$key: $value" }.join(", ")
        LOGGER.info(infoMessage)
    }
}

