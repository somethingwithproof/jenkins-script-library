import jenkins.model.Jenkins
import hudson.model.Job
import hudson.security.Permission
import java.util.logging.Logger

/**
 * This script disables all buildable jobs in a Jenkins instance.
 * It checks user permissions before performing any operations and logs all significant actions and errors.
 * This ensures that only authorized users can perform disable operations and provides a traceable log of actions performed.
 *
 * @author Thomas Vincent
 */
public class JenkinsJobDisabler {

    private static final Logger LOGGER = Logger.getLogger("JenkinsJobDisabler");

    /**
     * The main method that runs the job disable process.
     * It first checks for administrative permissions and then processes each job, disabling it if it is currently buildable.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        LOGGER.info("Starting cleanup operation");
        Jenkins jenkins = Jenkins.getInstance();
        def user = jenkins.getMe();

        // Check for administrative permissions before proceeding
        if (!user.hasPermission(Jenkins.ADMINISTER)) {
            throw new SecurityException("User does not have necessary permissions to perform cleanup");
        }

        // Process all items, specifically looking for jobs
        jenkins.allItems.findAll { it instanceof Job }.each { job ->
            try {
                // Disable the job if it is buildable
                if (job.isBuildable()) {
                    job.setBuildable(false);
                    job.save();
                    LOGGER.info("Disabled job: ${job.name}");
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to disable job ${job.name}: ${e.message}");
            }
        }

        LOGGER.info("Completed cleanup operation");
    }
}
