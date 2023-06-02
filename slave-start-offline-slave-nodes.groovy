import hudson.model.*

class ComputerLauncher {

  private def computerName

  ComputerLauncher(String computerName) {
    this.computerName = computerName
  }

  void start() {
    def computer = Jenkins.instance.slaves.find { it.name == computerName }

    if (computer?.offline) {
      computer.connect(false)
    }
  }
}

def startComputer(String computerName = null) {

  new ComputerLauncher(computerName).start()
}

Hudson.instance.slaves.each { startComputer(it.name) }
