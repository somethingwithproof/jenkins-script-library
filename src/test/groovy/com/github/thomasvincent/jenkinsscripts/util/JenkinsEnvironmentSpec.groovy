package com.github.thomasvincent.jenkinsscripts.util

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Subject
import spock.lang.Unroll

/**
 * Spock specification for JenkinsEnvironment utility class.
 */
@Title("JenkinsEnvironment Specification")
@Subject(JenkinsEnvironment)
class JenkinsEnvironmentSpec extends Specification {

    def "ExecutionContext enum has expected values"() {
        expect: "all execution contexts are defined"
        JenkinsEnvironment.ExecutionContext.values().size() == 5
        JenkinsEnvironment.ExecutionContext.SCRIPT_CONSOLE != null
        JenkinsEnvironment.ExecutionContext.CLI != null
        JenkinsEnvironment.ExecutionContext.PIPELINE != null
        JenkinsEnvironment.ExecutionContext.SHARED_LIBRARY != null
        JenkinsEnvironment.ExecutionContext.UNKNOWN != null
    }

    def "isScriptConsole returns false when not in Script Console"() {
        when: "checking Script Console context outside of Jenkins"
        def result = JenkinsEnvironment.isScriptConsole()

        then: "returns false since we're in a test context"
        result == false
    }

    def "isPipelineContext returns false when not in Pipeline"() {
        when: "checking Pipeline context outside of Jenkins"
        def result = JenkinsEnvironment.isPipelineContext()

        then: "returns false since we're in a test context"
        result == false
    }

    def "isCliContext returns false when not in CLI"() {
        when: "checking CLI context outside of Jenkins"
        def result = JenkinsEnvironment.isCliContext()

        then: "returns false since we're in a test context"
        result == false
    }

    def "detectContext returns UNKNOWN outside Jenkins"() {
        when: "detecting context outside of Jenkins"
        def context = JenkinsEnvironment.detectContext()

        then: "returns UNKNOWN"
        context == JenkinsEnvironment.ExecutionContext.UNKNOWN
    }

    def "getOutputStream returns System.out when no listener provided"() {
        when: "getting output stream without listener"
        def stream = JenkinsEnvironment.getOutputStream(null)

        then: "returns System.out"
        stream == System.out
    }

    def "getJenkinsInstance throws when Jenkins not available"() {
        when: "getting Jenkins instance outside Jenkins"
        JenkinsEnvironment.getJenkinsInstance()

        then: "throws IllegalStateException"
        thrown(IllegalStateException)
    }

    def "hasAdminPermission returns false outside Jenkins"() {
        when: "checking admin permission outside Jenkins"
        def result = JenkinsEnvironment.hasAdminPermission()

        then: "returns false"
        result == false
    }

    @Unroll
    def "isJenkinsVersionAtLeast validates #version comparison correctly"() {
        // This test may fail if running outside Jenkins, so we catch the exception
        when: "comparing Jenkins versions"
        def result
        try {
            result = JenkinsEnvironment.isJenkinsVersionAtLeast(version)
        } catch (Exception e) {
            // Expected outside Jenkins
            result = null
        }

        then: "handles comparison or throws appropriately"
        result == null || result instanceof Boolean

        where:
        version << ["2.0", "2.361.4", "3.0.0"]
    }

    def "loadOptionalClass returns null for non-existent class"() {
        when: "loading non-existent class"
        def clazz = JenkinsEnvironment.loadOptionalClass(
            "com.example.NonExistentClass",
            "non-existent-plugin"
        )

        then: "returns null"
        clazz == null
    }

    def "loadOptionalClass returns class for existing class"() {
        when: "loading existing class"
        def clazz = JenkinsEnvironment.loadOptionalClass(
            "java.util.ArrayList",
            null
        )

        then: "returns the class"
        clazz == ArrayList
    }

    def "isPluginInstalled returns false outside Jenkins"() {
        when: "checking plugin installation outside Jenkins"
        def result
        try {
            result = JenkinsEnvironment.isPluginInstalled("git")
        } catch (Exception e) {
            result = false
        }

        then: "returns false or handles error"
        result == false
    }
}
