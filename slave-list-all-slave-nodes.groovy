import hudson.model.Slave
import hudson.model.Computer
import hudson.plugins.ec2.EC2Computer
import java.text.SimpleDateFormat
import java.util.logging.Logger

/**
 * This class provides functionality to print detailed information about Jenkins slaves.
 * It includes specific enhancements to handle EC2 slaves by fetching additional data available from EC2 instances.
 * 
 * @author Thomas Vincent
 */
class SlaveInfoPrinter {

    private static final Logger LOGGER = Logger.getLogger(SlaveInfoPrinter.class.getName())
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    /**
     * Prints detailed information about a specific Jenkins slave. If the slave is hosted on EC2,
     * additional EC2-specific information is retrieved and printed.
     *
     * @param slave The Jenkins slave to print information about. This can be a standard slave or an EC2 slave.
     */
    static void printInstanceInfo(Slave slave) {
        Computer computer = slave.computer
        Map<String, Object> instanceInfo = [:] // Use Map for dynamic keys

        try {
            if (computer instanceof EC2Computer) {
                EC2Computer ec2Computer = (EC2Computer) computer
                def instance = ec2Computer.describeInstance()

                // Collect EC2-specific details
                instanceInfo.name = ec2Computer.name.replaceAll("\\(.*\\)", "")
                instanceInfo.privateIp = instance?.getPrivateIpAddress()
                instanceInfo.instanceId = instance?.getInstanceId()
                instanceInfo.amiId = instance?.getImageId()
                instanceInfo.instanceType = instance?.getInstanceType()
                instanceInfo.launchTime = DATE_FORMATTER.format(instance?.getLaunchTime())
                instanceInfo.offline = ec2Computer.offline
            } else {
                // Collect general computer details
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
     * Logs the collected instance information from the specified slave. Information is formatted
     * and logged at the INFO level.
     *
     * @param instanceInfo The map containing key-value pairs of instance information to log.
     */
    private static void logInstanceInfo(Map<String, Object> instanceInfo) {
        String infoMessage = instanceInfo.collect { key, value -> "$key: $value" }.join(", ")
        LOGGER.info(infoMessage)
    }
}
