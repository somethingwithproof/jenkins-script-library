/*
 * Mock implementation of hudson.plugins.ec2.EC2Computer
 * Created for testing and compilation purposes only
 */

package hudson.plugins.ec2

import hudson.model.Computer

/**
 * Mock implementation of EC2Computer class.
 * This is a minimal implementation that provides just enough functionality
 * to support the importing code in SlaveInfoManager.groovy.
 */
class EC2Computer extends Computer {
    
    /**
     * Describes the EC2 instance associated with this computer.
     * This mock implementation returns a mock EC2Instance object with the
     * minimum required functionality.
     *
     * @return a mock EC2Instance object
     */
    def describeInstance() {
        return new MockEC2Instance()
    }
    
    /**
     * Mock implementation of an EC2 instance.
     * Provides the minimum set of methods needed by SlaveInfoManager.
     */
    class MockEC2Instance {
        String getInstanceId() { return "i-mock12345" }
        String getInstanceType() { return "t2.micro" }
        String getPrivateIpAddress() { return "10.0.0.1" }
        String getPublicIpAddress() { return "54.123.45.67" }
        String getImageId() { return "ami-12345" }
        Date getLaunchTime() { return new Date() }
        MockInstanceState getState() { return new MockInstanceState() }
        List<MockTag> getTags() { return [new MockTag(key: "Name", value: "MockInstance")] }
    }
    
    /**
     * Mock implementation of EC2 instance state.
     */
    class MockInstanceState {
        String getName() { return "running" }
    }
    
    /**
     * Mock implementation of EC2 tag.
     */
    class MockTag {
        String key
        String value
    }
}