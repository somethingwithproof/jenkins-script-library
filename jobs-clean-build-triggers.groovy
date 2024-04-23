import jenkins.model.Jenkins;
import hudson.model.Job;
import hudson.security.Permission;
import java.util.logging.Logger;
import java.util.List;

/**
 * Disables all buildable jobs in a Jenkins instance securely and responsibly:
 * ensures operations are performed only by authorized administrators.
 */
public class JenkinsJobDisabler {

    private static final Logger LOGGER = Logger.getLogger(JenkinsJobDisabler.class.getName());

    /**
     * The main method that manages the job disablement process.
     * It verifies administrator permissions before proceeding to disable buildable jobs.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        LOGGER.info("Starting job disablement operation");
        
        Jenkins jenkins = Jenkins.get(); // Updated method to the recommended Jenkins.get()
        if (!jenkins.hasPermission(Permission.ADMINISTER)) {
            LOGGER.severe("Operation aborted. User lacks required administrative privileges.");
            return;
        }

        List<Job> buildableJobs = jenkins.getAllItems(Job.class).stream()
            .filter(Job::isBuildable)
            .toList();

        buildableJobs.forEach(job -> disableJob(job));

        LOGGER.info("Job disablement operation completed");
    }

    /**
     * Disables a single buildable job and logs the outcome.
     *
     * @param job The job to be disabled.
     */
    private static void disableJob(Job job) {
        try {
            job.setBuildable(false);
            job.save();
            LOGGER.info("Successfully disabled job: " + job.getFullName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to disable job " + job.getFullName(), e);
        }
    }
}
