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

package com.github.thomasvincent.jenkinsscripts.scripts

import groovy.lang.GroovyShell
import groovy.lang.Binding
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.WithoutJenkins
import org.junit.Before
import org.junit.After
import static groovy.test.GroovyAssert.shouldFail

/**
 * Base class for script integration tests.
 * 
 * Provides common functionality for testing Groovy scripts in a Jenkins environment.
 * Uses the JenkinsRule to simulate a running Jenkins instance.
 */
abstract class BaseScriptIntegrationTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule()
    
    protected GroovyShell shell
    protected ByteArrayOutputStream outContent
    protected ByteArrayOutputStream errContent
    protected PrintStream originalOut
    protected PrintStream originalErr
    protected Binding binding
    
    /**
     * Returns the path to the script under test.
     * 
     * @return Absolute path to the script file
     */
    abstract String getScriptPath()
    
    /**
     * Sets up the test environment.
     * 
     * Creates a GroovyShell with a binding that includes the Jenkins instance
     * and captures standard output and error for verification.
     */
    @Before
    void setUp() {
        // Capture stdout and stderr
        originalOut = System.out
        originalErr = System.err
        outContent = new ByteArrayOutputStream()
        errContent = new ByteArrayOutputStream()
        System.setOut(new PrintStream(outContent))
        System.setErr(new PrintStream(errContent))
        
        // Set up binding and GroovyShell
        binding = new Binding()
        binding.setVariable("jenkins", jenkinsRule.jenkins)
        shell = new GroovyShell(binding)
    }
    
    /**
     * Cleans up the test environment.
     * 
     * Restores the original stdout and stderr streams.
     */
    @After
    void cleanUp() {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }
    
    /**
     * Runs the script with the given arguments.
     * 
     * @param args Arguments to pass to the script
     * @return The result of script execution
     */
    protected Object runScript(String... args) {
        binding.setVariable("args", args as String[])
        File scriptFile = new File(getScriptPath())
        if (!scriptFile.exists()) {
            throw new FileNotFoundException("Script file not found: ${getScriptPath()}")
        }
        return shell.evaluate(scriptFile)
    }
    
    /**
     * Gets the captured standard output as a string.
     * 
     * @return The captured output
     */
    protected String getOutput() {
        return outContent.toString()
    }
    
    /**
     * Gets the captured standard error as a string.
     * 
     * @return The captured error output
     */
    protected String getError() {
        return errContent.toString()
    }
    
    /**
     * Resets the captured output and error streams.
     */
    protected void resetStreams() {
        outContent.reset()
        errContent.reset()
    }
    
    /**
     * Asserts that the output contains the specified string.
     * 
     * @param expected The string to look for
     */
    protected void assertOutputContains(String expected) {
        assert getOutput().contains(expected)
    }
    
    /**
     * Asserts that the error output contains the specified string.
     * 
     * @param expected The string to look for
     */
    protected void assertErrorContains(String expected) {
        assert getError().contains(expected)
    }
    
    /**
     * Helper to run tests without a Jenkins instance.
     */
    @WithoutJenkins
    protected static class ScriptSyntaxTest extends BaseScriptIntegrationTest {
        @Override
        String getScriptPath() {
            return null
        }
        
        /**
         * Checks if a script has valid syntax.
         * 
         * @param scriptPath Path to the script to check
         * @return True if the script has valid syntax
         */
        boolean checkScriptSyntax(String scriptPath) {
            try {
                File scriptFile = new File(scriptPath)
                if (!scriptFile.exists()) {
                    println "Script file not found: ${scriptPath}"
                    return false
                }
                
                new GroovyShell().parse(scriptFile)
                return true
            } catch (Exception e) {
                println "Script has syntax errors: ${e.message}"
                return false
            }
        }
    }
}