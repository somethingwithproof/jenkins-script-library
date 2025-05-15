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

package com.github.thomasvincent.jenkinsscripts.nodes

import hudson.model.Computer
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.Future

/**
 * Unit tests for ComputerLauncher class.
 * 
 * <p>This specification verifies the behavior of the ComputerLauncher class, which is
 * responsible for launching Jenkins agent computers that are offline.</p>
 * 
 * <p>Tests cover various scenarios including:
 * <ul>
 *   <li>Starting a specific offline computer</li>
 *   <li>Handling already online computers</li>
 *   <li>Handling non-existent computers</li>
 *   <li>Handling Jenkins master computer</li>
 *   <li>Starting all offline computers</li>
 *   <li>Exception handling during computer startup</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Vincent
 * @since 1.0
 */
class ComputerLauncherSpec extends Specification {

    def jenkins = Mock(Jenkins)
    def computer1 = Mock(Computer)
    def computer2 = Mock(Computer)
    def masterComputer = Mock(Jenkins.MasterComputer)
    def futureResult = Mock(Future)
    
    @Subject
    ComputerLauncher launcher
    
    def setup() {
        // Set up computer mocks
        computer1.name >> "slave1"
        computer2.name >> "slave2"
        masterComputer.name >> "master"
        
        // Set up mock Jenkins instance
        jenkins.getComputer("slave1") >> computer1
        jenkins.getComputer("slave2") >> computer2
        jenkins.getComputer("master") >> masterComputer
        jenkins.getComputer("non-existent") >> null
        jenkins.computers >> [masterComputer, computer1, computer2]
        
        // Set up mock connection Future
        futureResult.get(_, _) >> null
        
        // Create launcher
        launcher = new ComputerLauncher(jenkins)
    }
    
    def "should start a specific offline computer"() {
        given:
        computer1.offline >> true
        computer1.connect(false) >> futureResult
        
        when:
        def result = launcher.startComputer("slave1")
        
        then:
        result == true
        1 * computer1.connect(false) >> futureResult
    }
    
    def "should not attempt to start an online computer"() {
        given:
        computer1.offline >> false
        
        when:
        def result = launcher.startComputer("slave1")
        
        then:
        result == true
        0 * computer1.connect(_)
    }
    
    def "should not start master computer"() {
        when:
        def result = launcher.startComputer("master")
        
        then:
        result == false
        0 * masterComputer.connect(_)
    }
    
    def "should return false for non-existent computer"() {
        when:
        def result = launcher.startComputer("non-existent")
        
        then:
        result == false
        0 * computer1.connect(_)
        0 * computer2.connect(_)
    }
    
    def "should return false for null computer name with validation error"() {
        when:
        def result = launcher.startComputer(null)
        
        then:
        result == false
        0 * computer1.connect(_)
        0 * computer2.connect(_)
    }
    
    def "should return false for empty computer name with validation error"() {
        when:
        def result = launcher.startComputer("")
        
        then:
        result == false
        0 * computer1.connect(_)
        0 * computer2.connect(_)
    }
    
    def "should return false for blank computer name with validation error"() {
        when:
        def result = launcher.startComputer("   ")
        
        then:
        result == false
        0 * computer1.connect(_)
        0 * computer2.connect(_)
    }
    
    def "should start all offline computers"() {
        given:
        computer1.offline >> true
        computer2.offline >> false
        computer1.connect(false) >> futureResult
        
        when:
        def count = launcher.startAllOfflineComputers()
        
        then:
        count == 1
        1 * computer1.connect(false) >> futureResult
        0 * computer2.connect(_)
        0 * masterComputer.connect(_)
    }
    
    def "should handle exceptions when starting computers"() {
        given:
        computer1.offline >> true
        computer1.connect(false) >> { throw new Exception("Connection error") }
        
        when:
        def result = launcher.startComputer("slave1")
        
        then:
        result == false
    }
}