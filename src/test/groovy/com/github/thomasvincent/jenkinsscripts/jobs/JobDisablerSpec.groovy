/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
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

package com.github.thomasvincent.jenkinsscripts.jobs

import jenkins.model.Jenkins
import hudson.model.Job
import hudson.security.Permission
import spock.lang.Specification
import spock.lang.Subject

/**
 * Unit tests for JobDisabler class.
 * 
 * <p>This specification verifies the behavior of the JobDisabler class, which is
 * responsible for disabling Jenkins jobs.</p>
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JobDisablerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def job1 = Mock(Job)
    def job2 = Mock(Job)
    
    @Subject
    JobDisabler disabler
    
    def setup() {
        // Set up job mocks
        job1.fullName >> "job1"
        job2.fullName >> "job2"
        
        // Set up Jenkins mock
        jenkins.getItemByFullName("job1", Job.class) >> job1
        jenkins.getItemByFullName("job2", Job.class) >> job2
        jenkins.getItemByFullName("non-existent", Job.class) >> null
        
        // Create disabler
        disabler = new JobDisabler(jenkins)
    }
    
    def "should disable a specific job"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        
        when:
        def result = disabler.disableJob("job1")
        
        then:
        result == true
        1 * job1.setBuildable(false)
        1 * job1.save()
    }
    
    def "should return false when disabling non-existent job"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        
        when:
        def result = disabler.disableJob("non-existent")
        
        then:
        result == false
        0 * job1.setBuildable(_)
        0 * job2.setBuildable(_)
    }
    
    def "should return false when lacking administrative permissions"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> false
        
        when:
        def result = disabler.disableJob("job1")
        
        then:
        result == false
        0 * job1.setBuildable(_)
    }
    
    def "should disable all buildable jobs"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        jenkins.getAllItems(Job.class) >> [job1, job2]
        job1.isBuildable() >> true
        job2.isBuildable() >> true
        
        when:
        def count = disabler.disableAllJobs()
        
        then:
        count == 2
        1 * job1.setBuildable(false)
        1 * job1.save()
        1 * job2.setBuildable(false)
        1 * job2.save()
    }
    
    def "should handle exceptions when disabling jobs"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        job1.setBuildable(false) >> { throw new IOException("Test exception") }
        
        when:
        def result = disabler.disableJob("job1")
        
        then:
        result == false
    }
    
    def "should return false for null job name with validation error"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        
        when:
        def result = disabler.disableJob(null)
        
        then:
        result == false
        0 * job1.setBuildable(_)
        0 * job2.setBuildable(_)
    }
    
    def "should return false for empty job name with validation error"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        
        when:
        def result = disabler.disableJob("")
        
        then:
        result == false
        0 * job1.setBuildable(_)
        0 * job2.setBuildable(_)
    }
    
    def "should return false for blank job name with validation error"() {
        given:
        jenkins.hasPermission(Permission.ADMINISTER) >> true
        
        when:
        def result = disabler.disableJob("   ")
        
        then:
        result == false
        0 * job1.setBuildable(_)
        0 * job2.setBuildable(_)
    }
}