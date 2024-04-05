import hudson.model.Computer
import jenkins.model.Jenkins
import java.util.logging.Logger

class ComputerLauncher {

    private static final Logger LOGGER = Logger.getLogger(ComputerLauncher.class.getName())
    private String computerName

    ComputerLauncher(String computerName) {
        this.computerName = computerName
    }

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

void startComputer(String computerName) {
    if (computerName == null) {
        LOGGER.warning("No computer name provided.")
        return
    }
    new ComputerLauncher(computerName).start()
}

def LOGGER = Logger.getLogger(ComputerLauncher.class.getName())
Jenkins.instance.computers.each { computer ->
    if (!(computer instanceof Jenkins.MasterComputer)) { // Skip the master
        startComputer(computer.name)
    }
}
