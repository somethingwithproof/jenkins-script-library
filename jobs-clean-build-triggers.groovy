/**
 * Removes all build triggers from all jobs in the Jenkins instance.
 *
 * @author Bard
 */
class RemoveBuildTriggers {

  /**
   * The type of build trigger to remove.
   */
  private const val BUILD_TRIGGER_TYPE = BuildTrigger.class

  /**
   * Removes all build triggers from the specified job.
   *
   * @param job The job to remove build triggers from.
   */
  def removeBuildTriggers(Actionable job) {
    if (job instanceof AbstractProject) {
      job.getTriggers()
        .findAll { it.getClass() == BUILD_TRIGGER_TYPE }
        .forEach { job.remove(it) }
    }
  }

  /**
   * The main method.
   *
   * @param args The command-line arguments.
   */
  static void main(String[] args) {
    Hudson.instance.items.each { it.removeBuildTriggers(new RemoveBuildTriggers()) }
  }
}
