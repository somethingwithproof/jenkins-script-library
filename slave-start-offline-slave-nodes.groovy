import hudson.model.Computer
import jenkins.model.Jenkins
import java.util.logging.Logger

/**
 * A class to handle the starting of Jenkins slave computers.
 * It checks if a given computer is offline and attempts to connect to it.
 */
class ComputerLauncher {

    private static final Logger LOGGER = Logger.getLogger(ComputerLauncher.class.getName())
    private String computerName

    /**
     * Constructs a ComputerLauncher for managing a specific Jenkins computer.
     *
     * @param computerName the name of the computer to be managed
     */
    ComputerLauncher(String computerName) {
        this.computerName = computerName
    }

    /**
     * Starts the specified computer if it is offline.
     * Logs relevant status updates and errors during the process.
     */
    void start() {
        Computer computer = Jenkins.instance.getComputer(computerName)

        if (computer == null) {
            LOGGER.warning("Computer with name $computerName not found.")
            return
        }

        if (computer.offline) {
            try {
                computer.connect(false).get() // Synchronously wait for the connection to complete
                LOGGER.info("Computer $computerName started successfully.")
            } catch (Exception e) {
                LOGGER.severe("Failed to start computer $computerName: ${e.message}")
            }
        } else {
            LOGGER.info("Computer $computerName is already online.")
        }
    }
}

/**
 * Starts a Jenkins computer by name unless it's the master computer.
 *
 * @param computerName the name of the computer to start
 */
void startComputer(String computerName) {
    if (computerName == null) {
        LOGGER.warning("No computer name provided.")
        return
    }
    new ComputerLauncher(computerName).start()
}

// Global LOGGER for the script
def LOGGER = Logger.getLogger(ComputerLauncher.class.getName())

// Start all computers except the master
Jenkins.instance.computers.each { computer ->
    if (!(computer instanceof Jenkins.MasterComputer)) { // Skip the master
        startComputer(computer.name)
    }
}
