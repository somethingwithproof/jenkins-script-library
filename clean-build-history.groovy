import jenkins.model.Jenkins
import jenkins.model.AbstractItem
import jenkins.model.Item
import org.kohsuke.stapler.DataBoundConstructor
import java.util.logging.Logger

class JobCleaner {
    private static final Logger LOGGER = Logger.getLogger(JobCleaner.class.name)
    private static final int DEFAULT_CLEANED_JOBS_LIMIT = 25
    private static final int DEFAULT_BUILD_TOTAL = 100

    private final Jenkins jenkins
    private final String jobName
    private final boolean resetBuildNumber
    private final int cleanedJobsLimit
    private final int buildTotal

    @DataBoundConstructor
    JobCleaner(Jenkins jenkins, String jobName, boolean resetBuildNumber, int cleanedJobsLimit = DEFAULT_CLEANED_JOBS_LIMIT, int buildTotal = DEFAULT_BUILD_TOTAL) {
        this.jenkins = jenkins
        this.jobName = jobName
        this.resetBuildNumber = resetBuildNumber
        this.cleanedJobsLimit = cleanedJobsLimit
        this.buildTotal = buildTotal
    }

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

    private void cleanFolder(AbstractFolder folder) {
        folder.getItems(AbstractItem.class).take(cleanedJobsLimit).each { subJob ->
            new JobCleaner(jenkins, subJob.name, resetBuildNumber).clean()
        }
    }

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
        if (buildsDeleted) {
            LOGGER.info("Job ${job.name} cleaned successfully.")
        }
        if (resetBuildNumber) {
            job.nextBuildNumber = 1
            try {
                job.save()
            } catch (Exception e) {
                LOGGER.severe("Failed to reset build number for job ${job.name}: ${e.message}")
            }
        }
    }
}
