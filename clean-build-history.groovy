import com.cloudbees.hudson.plugins.folder.AbstractFolder
import hudson.model.AbstractItem
import groovy.transform.Field

/**
 * Cleans jobs and folders.
 */
class JobCleaner {

  /**
   * The name of the job or folder to clean.
   */
  @Field String jobName

  /**
   * Whether to reset the build number to 1 after cleaning.
   */
  @Field boolean resetBuildNumber

  /**
   * Creates a new JobCleaner.
   *
   * @param jobName The name of the job or folder to clean.
   * @param resetBuildNumber Whether to reset the build number to 1 after cleaning.
   */
  JobCleaner(String jobName, boolean resetBuildNumber) {
    this.jobName = jobName
    this.resetBuildNumber = resetBuildNumber
  }

  /**
   * Cleans the specified job or folder.
   */
  void clean() {
    def job = Jenkins.instance.getItemByFullName(jobName)
    if (job instanceof AbstractFolder) {
      cleanFolder(job)
    } else if (job instanceof Job) {
      cleanJob(job)
    }
  }

  /**
   * Cleans the specified folder recursively.
   *
   * @param folder The folder to clean.
   */
  private void cleanFolder(AbstractFolder folder) {
    def cleanedJobsLimit = 2
    def subJobs = folder.getItems()
    subJobs.take(cleanedJobsLimit).each { subJob ->
      new JobCleaner(subJob.name, resetBuildNumber).clean()
    }
  }

  /**
   * Cleans the specified job.
   *
   * @param job The job to clean.
   */
  private void cleanJob(Job job) {
    def buildTotal = 5
    def buildsDeleted = false
    job.getBuilds().take(buildTotal).each { build ->
      buildsDeleted = true
      build.delete()
    }
    if (buildsDeleted) {
      println "Job ${job.name} cleaned successfully."
    }
    if (resetBuildNumber) {
      job.nextBuildNumber = 1
      job.save()
    }
  }

}
