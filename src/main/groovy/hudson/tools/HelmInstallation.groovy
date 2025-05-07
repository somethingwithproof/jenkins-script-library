/*
 * MIT License
 *
 * Copyright (c) 2024 Thomas Vincent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package hudson.tools

import hudson.EnvVars
import hudson.Extension
import hudson.model.EnvironmentSpecific
import hudson.model.Node
import hudson.model.TaskListener
import hudson.slaves.NodeSpecific
import org.kohsuke.stapler.DataBoundConstructor

/**
 * Helm installation tool.
 * 
 * This class represents a Helm installation in the Jenkins system.
 * It extends ToolInstallation to provide standard tool installation
 * functionality and implements NodeSpecific and EnvironmentSpecific
 * to ensure proper per-node and per-environment configuration.
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
public class HelmInstallation extends ToolInstallation 
        implements NodeSpecific<HelmInstallation>, EnvironmentSpecific<HelmInstallation> {
    
    private static final long serialVersionUID = 1L

    /**
     * Constructor with required data binding for Jenkins.
     *
     * @param name The name of this installation
     * @param home The home directory for the installation
     * @param properties List of properties for this installation
     */
    @DataBoundConstructor
    public HelmInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties)
    }
    
    /**
     * Returns a node-specific version of this installation.
     *
     * @param node The node to get a specific installation for
     * @param log The task listener for logging
     * @return A NodeSpecific implementation of HelmInstallation
     */
    @Override
    public HelmInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new HelmInstallation(getName(), translateFor(node, log), getProperties().toList())
    }

    /**
     * Returns an environment-specific version of this installation.
     *
     * @param environment The environment variables to use for configuration
     * @return An EnvironmentSpecific implementation of HelmInstallation
     */
    @Override
    public HelmInstallation forEnvironment(EnvVars environment) {
        return new HelmInstallation(getName(), environment.expand(getHome()), getProperties().toList())
    }

    /**
     * Extension descriptor for HelmInstallation.
     */
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<HelmInstallation> {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Helm"
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public HelmInstallation[] getInstallations() {
            return Jenkins.get().getDescriptorByType(HelmInstallation.DescriptorImpl.class).getInstallations()
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void setInstallations(HelmInstallation... installations) {
            Jenkins.get().getDescriptorByType(HelmInstallation.DescriptorImpl.class).setInstallations(installations)
        }
    }
}