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
import hudson.model.AbstractProject
import hudson.model.Build
import hudson.model.TopLevelItem
import spock.lang.Specification
import spock.lang.Subject

/**
 * Unit tests for JobCleaner class.
 * 
 * <p>This specification verifies the behavior of the JobCleaner class, which is 
 * responsible for cleaning up builds from Jenkins jobs and optionally resetting
 * build numbers.</p>
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class JobCleanerSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def project = Mock(AbstractProject)
    def builds = []
    
    @Subject
    JobCleaner cleaner
    
    def setup() {
        // Set up mock builds
        5.times { i ->
            def build = Mock(Build)
            build.getNumber() >> i + 1
            builds.add(build)
        }
        
        // Configure project to return builds
        project.getName() >> "test-job"
        project.getBuilds() >> builds
    }
    
    def "should clean builds for a valid project"() {
        given:
        jenkins.getItemByFullName("test-job", TopLevelItem.class) >> project
        cleaner = new JobCleaner(jenkins, "test-job", false, 25, 3)
        
        when:
        def result = cleaner.clean()
        
        then:
        result == true
        3 * builds[_].delete()
        0 * project.updateNextBuildNumber(_)
    }
    
    def "should reset build number when requested"() {
        given:
        jenkins.getItemByFullName("test-job", TopLevelItem.class) >> project
        cleaner = new JobCleaner(jenkins, "test-job", true, 25, 3)
        
        when:
        def result = cleaner.clean()
        
        then:
        result == true
        3 * builds[_].delete()
        1 * project.updateNextBuildNumber(1)
        1 * project.save()
    }
    
    def "should return false when job is not found"() {
        given:
        jenkins.getItemByFullName("non-existent-job", TopLevelItem.class) >> null
        cleaner = new JobCleaner(jenkins, "non-existent-job", false, 25, 3)
        
        when:
        def result = cleaner.clean()
        
        then:
        result == false
        0 * builds[_].delete()
        0 * project.updateNextBuildNumber(_)
    }
    
    def "should return false when job is not an AbstractProject"() {
        given:
        def item = Mock(TopLevelItem)
        jenkins.getItemByFullName("invalid-job", TopLevelItem.class) >> item
        cleaner = new JobCleaner(jenkins, "invalid-job", false, 25, 3)
        
        when:
        def result = cleaner.clean()
        
        then:
        result == false
        0 * builds[_].delete()
        0 * project.updateNextBuildNumber(_)
    }
    
    def "should validate constructor parameters"() {
        when:
        new JobCleaner(null, "test-job")
        
        then:
        thrown(IllegalArgumentException)
        
        when:
        new JobCleaner(jenkins, null)
        
        then:
        thrown(IllegalArgumentException)
        
        when:
        new JobCleaner(jenkins, "")
        
        then:
        thrown(IllegalArgumentException)
        
        when:
        new JobCleaner(jenkins, "test-job", false, -1, 10)
        
        then:
        noExceptionThrown()
        
        when:
        new JobCleaner(jenkins, "test-job", false, 25, -1)
        
        then:
        noExceptionThrown()
    }
}