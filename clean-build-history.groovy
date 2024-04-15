import jenkins.model.Jenkins
import jenkins.model.AbstractItem
import jenkins.model.Item
import org.kohsuke.stapler.DataBoundConstructor
import java.util.logging.Logger

/**
 * Cleans Jenkins jobs either by removing old builds or by resetting their build numbers.
 * This utility class allows for targeted clean-up operations within a Jenkins instance,
 * affecting only specified jobs or folders.
 * 
 * @author thomasvincent
 */
class JobCleaner {
    private static final Logger LOGGER = Logger.getLogger(JobCleaner.class.name)
    private static final int DEFAULT_CLEANED_JOBS_LIMIT = 25
    private static final int DEFAULT_BUILD_TOTAL = 100

    private final Jenkins jenkins
    private final String jobName
    private final boolean resetBuildNumber
    private final int cleanedJobsLimit
    private final int buildTotal

    /**
     * Data-bound constructor to create a JobCleaner object.
     * 
     * @param jenkins the Jenkins instance this cleaner will operate on
     * @param jobName the name of the job to clean
     * @param resetBuildNumber if true, resets the build number to 1
     * @param cleanedJobsLimit the maximum number of jobs to clean within a folder
     * @param buildTotal the maximum number of builds to delete from a job
     */
    @DataBoundConstructor
    JobCleaner(Jenkins jenkins, String jobName, boolean resetBuildNumber, int cleanedJobsLimit = DEFAULT_CLEANED_JOBS_LIMIT, int buildTotal = DEFAULT_BUILD_TOTAL) {
        this.jenkins = jenkins ?: throw new IllegalArgumentException("Jenkins instance cannot be null")
        this.jobName = jobName?.trim() ?: throw new IllegalArgumentException("Job name cannot be null or blank")
        this.resetBuildNumber = resetBuildNumber
        this.cleanedJobsLimit = cleanedJobsLimit
        this.buildTotal = buildTotal
    }

    /**
     * Initiates the cleaning process for the specified job or folder.
     * It checks if the item is a folder or a job and applies the appropriate cleaning method.
     */
    void clean() {
        def item = jenkins.getItemByFullName(jobName)
        if (item instanceof AbstractFolder) {
            cleanFolder(item as AbstractFolder)
        } else if (item instanceof Job) {
            cleanJob(item as Job)
        } else {
            LOGGER.warning("Item not found or not a job/folder: $jobName")
        }
    }

    /**
     * Cleans jobs within a folder, limiting the number of cleaned jobs to the specified limit.
     * 
     * @param folder the folder to clean jobs within
     */
    private void cleanFolder(AbstractFolder folder) {
        folder.getItems(AbstractItem.class).take(cleanedJobsLimit).each { subJob ->
            new JobCleaner(jenkins, subJob.name, resetBuildNumber).clean()
        }
    }

    /**
     * Deletes builds from a job up to the specified limit and resets the build number if specified.
     * 
     * @param job the job from which builds will be deleted
     */
    private void cleanJob(Job job) {
        boolean buildsDeleted = false
        job.getBuilds().take(buildTotal).each { build ->
            try {
                build.delete()
                buildsDeleted = true
            } catch (Exception e) {
                LOGGER.severe("Failed to delete build ${build.id} for job ${job.name}: ${e.message}")
            }
        }
        if (buildsDeleted && resetBuildNumber) {
            job.nextBuildNumber = 1
            try {
                job.save()
            } catch (Exception e) {
                LOGGER.severe("Failed to reset build number for job ${job.name}: ${e.message}")
            }
        }
    }
}
