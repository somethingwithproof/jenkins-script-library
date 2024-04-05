/**
 * Removes all build triggers from all jobs in the Jenkins instance.
 *
 * @author thomasvincent
 */

import hudson.model.Hudson
import hudson.model.AbstractProject
import hudson.triggers.Trigger

/**
 * Removes all build triggers of a specific type from all jobs in the Jenkins instance.
 *
 * @author thomasvincent
 */
class RemoveBuildTriggers {

  /**
   * The type of build trigger to remove.
   */
  private static final Class<Trigger> BUILD_TRIGGER_TYPE = Trigger.class

  /**
   * Removes all build triggers from the specified job.
   *
   * @param job The job to remove build triggers from.
   */
  private static void removeBuildTriggers(AbstractProject job) {
    job.getTriggers().findAll { it.getClass() == BUILD_TRIGGER_TYPE }.each { trigger ->
      job.removeTrigger(trigger.getDescriptor())
    }
    job.save()
  }

  /**
   * Iterates over all items in Jenkins, including those in folders, and removes the specified build triggers.
   */
  static void processAllItems() {
    Hudson.instance.allItems.each { item ->
      if (item instanceof AbstractProject) {
        removeBuildTriggers(item)
      }
    }
  }

  /**
   * The main method.
   *
   * @param args The command-line arguments.
   */
  static void main(String[] args) {
    processAllItems()
  }
}
