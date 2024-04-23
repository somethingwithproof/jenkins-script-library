import jenkins.model.Jenkins;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to clean Jenkins jobs by either removing old builds or resetting their build numbers.
 */
public class JobCleaner {
    private static final Logger LOGGER = Logger.getLogger(JobCleaner.class.getName());
    private static final int DEFAULT_CLEANED_JOBS_LIMIT = 25;
    private static final int DEFAULT_BUILD_TOTAL = 100;

    private final Jenkins jenkins;
    private final String jobName;
    private final boolean resetBuildNumber;
    private final int cleanedJobsLimit;
    private final int buildTotal;

    /**
     * Constructs a JobCleaner object with specified parameters.
     *
     * @param jenkins           The Jenkins instance.
     * @param jobName           Name of the job to clean.
     * @param resetBuildNumber  If true, resets the build number to 1.
     * @param cleanedJobsLimit  Max number of jobs to clean within a folder.
     * @param buildTotal        Max number of builds to delete from a job.
     */
    @DataBoundConstructor
    public JobCleaner(Jenkins jenkins, String jobName, boolean resetBuildNumber, int cleanedJobsLimit, int buildTotal) {
        this.jenkins = validateNotNull(jenkins, "Jenkins instance cannot be null");
        this.jobName = validateNotNullOrEmpty(jobName, "Job name cannot be null or empty").trim();
        this.resetBuildNumber = resetBuildNumber;
        this.cleanedJobsLimit = Math.max(1, cleanedJobsLimit);
        this.buildTotal = Math.max(1, buildTotal);
    }

    /**
     * Initiates the cleaning process.
     */
    public void clean() {
        TopLevelItem item = jenkins.getItemByFullName(jobName, TopLevelItem.class);
        if (item == null) {
            LOGGER.warning("Item not found: " + jobName);
            return;
        }

        if (item instanceof AbstractProject) {
            cleanJob((AbstractProject) item);
        } else {
            LOGGER.warning("Unsupported job type: " + jobName);
        }
    }

    private void cleanJob(AbstractProject<?, ?> job) {
        try {
            job.getBuilds().stream().limit(buildTotal).forEach(build -> {
                try {
                    build.delete();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to delete build " + build.getNumber() + " for job " + job.getName(), e);
                }
            });
            if (resetBuildNumber) {
                resetBuildNumber(job);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error accessing builds for job " + job.getName(), e);
        }
    }

    private void resetBuildNumber(AbstractProject<?, ?> job) {
        job.updateNextBuildNumber(1);
        try {
            job.save();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to reset build number for job " + job.getName(), e);
        }
    }

    private static <T> T validateNotNull(T arg, String message) {
        if (arg == null) {
            throw new IllegalArgumentException(message);
        }
        return arg;
    }

    private static String validateNotNullOrEmpty(String arg, String message) {
        if (arg == null || arg.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return arg;
    }
}
