/*
 * MIT License
 * Copyright (c) 2024 Thomas Vincent
 *
 * Groovy DSL for declarative pipeline generation.
 * Provides a fluent API for building Jenkins pipelines programmatically.
 */
package com.github.thomasvincent.jenkinsscripts.dsl

import groovy.transform.CompileStatic

/**
 * DSL builder for creating Jenkins declarative pipelines.
 * Supports stages, parallel execution, post actions, and more.
 */
class PipelineDSL {

    /**
     * Entry point for building a pipeline.
     */
    static String pipeline(@DelegatesTo(PipelineBuilder) Closure config) {
        PipelineBuilder builder = new PipelineBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config()
        return builder.build()
    }

    /**
     * Create a simple CI pipeline from parameters.
     */
    static String simpleCIPipeline(Map<String, Object> params) {
        String language = params.language ?: 'java'
        String buildCommand = params.buildCommand ?: getBuildCommand(language)
        String testCommand = params.testCommand ?: getTestCommand(language)
        List<String> notifyChannels = (params.notifyChannels ?: []) as List<String>

        return pipeline {
            agent 'any'

            options {
                timeout(time: 30, unit: 'MINUTES')
                buildDiscarder(logRotator(numToKeepStr: '10'))
                timestamps()
            }

            stages {
                stage('Checkout') {
                    steps {
                        checkout 'scm'
                    }
                }

                stage('Build') {
                    steps {
                        sh buildCommand
                    }
                }

                stage('Test') {
                    steps {
                        sh testCommand
                    }
                    post {
                        always {
                            junit '**/test-results/*.xml'
                        }
                    }
                }

                if (params.deploy) {
                    stage('Deploy') {
                        when {
                            branch 'main'
                        }
                        steps {
                            sh params.deployCommand ?: 'echo "Deploy step"'
                        }
                    }
                }
            }

            post {
                failure {
                    notifyChannels.each { channel ->
                        echo "Notifying ${channel}"
                    }
                }
            }
        }
    }

    private static String getBuildCommand(String language) {
        switch (language.toLowerCase()) {
            case 'java': return './gradlew build -x test'
            case 'maven': return 'mvn clean compile -DskipTests'
            case 'node':
            case 'nodejs':
            case 'javascript': return 'npm ci && npm run build'
            case 'python': return 'pip install -r requirements.txt'
            case 'go':
            case 'golang': return 'go build ./...'
            case 'rust': return 'cargo build --release'
            default: return 'echo "Build step"'
        }
    }

    private static String getTestCommand(String language) {
        switch (language.toLowerCase()) {
            case 'java': return './gradlew test'
            case 'maven': return 'mvn test'
            case 'node':
            case 'nodejs':
            case 'javascript': return 'npm test'
            case 'python': return 'pytest'
            case 'go':
            case 'golang': return 'go test ./...'
            case 'rust': return 'cargo test'
            default: return 'echo "Test step"'
        }
    }

    /**
     * Pipeline builder class.
     */
    static class PipelineBuilder {
        private String agentLabel = 'any'
        private Map<String, Object> agentConfig = [:]
        private List<String> environmentVars = []
        private List<String> optionsList = []
        private List<String> parametersList = []
        private List<String> triggersList = []
        private StagesBuilder stagesBuilder = new StagesBuilder()
        private PostBuilder postBuilder = new PostBuilder()
        private List<String> tools = []

        void agent(String label) {
            this.agentLabel = label
        }

        void agent(@DelegatesTo(AgentBuilder) Closure config) {
            AgentBuilder builder = new AgentBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.agentConfig = builder.config
        }

        void environment(@DelegatesTo(EnvironmentBuilder) Closure config) {
            EnvironmentBuilder builder = new EnvironmentBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.environmentVars = builder.vars
        }

        void options(@DelegatesTo(OptionsBuilder) Closure config) {
            OptionsBuilder builder = new OptionsBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.optionsList = builder.options
        }

        void parameters(@DelegatesTo(ParametersBuilder) Closure config) {
            ParametersBuilder builder = new ParametersBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.parametersList = builder.params
        }

        void triggers(@DelegatesTo(TriggersBuilder) Closure config) {
            TriggersBuilder builder = new TriggersBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.triggersList = builder.triggers
        }

        void tools(@DelegatesTo(ToolsBuilder) Closure config) {
            ToolsBuilder builder = new ToolsBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.tools = builder.tools
        }

        void stages(@DelegatesTo(StagesBuilder) Closure config) {
            config.delegate = stagesBuilder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        }

        void post(@DelegatesTo(PostBuilder) Closure config) {
            config.delegate = postBuilder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        }

        String build() {
            StringBuilder sb = new StringBuilder()
            sb.append("pipeline {\n")

            // Agent
            if (agentConfig) {
                sb.append("    agent {\n")
                agentConfig.each { k, v ->
                    sb.append("        ${k} ${formatValue(v)}\n")
                }
                sb.append("    }\n")
            } else {
                sb.append("    agent ${agentLabel}\n")
            }

            // Environment
            if (environmentVars) {
                sb.append("    environment {\n")
                environmentVars.each { sb.append("        ${it}\n") }
                sb.append("    }\n")
            }

            // Options
            if (optionsList) {
                sb.append("    options {\n")
                optionsList.each { sb.append("        ${it}\n") }
                sb.append("    }\n")
            }

            // Parameters
            if (parametersList) {
                sb.append("    parameters {\n")
                parametersList.each { sb.append("        ${it}\n") }
                sb.append("    }\n")
            }

            // Triggers
            if (triggersList) {
                sb.append("    triggers {\n")
                triggersList.each { sb.append("        ${it}\n") }
                sb.append("    }\n")
            }

            // Tools
            if (tools) {
                sb.append("    tools {\n")
                tools.each { sb.append("        ${it}\n") }
                sb.append("    }\n")
            }

            // Stages
            sb.append("    stages {\n")
            sb.append(stagesBuilder.build())
            sb.append("    }\n")

            // Post
            String postContent = postBuilder.build()
            if (postContent) {
                sb.append("    post {\n")
                sb.append(postContent)
                sb.append("    }\n")
            }

            sb.append("}\n")
            return sb.toString()
        }

        private static String formatValue(Object value) {
            if (value instanceof String) {
                return "'${value}'"
            }
            return value.toString()
        }
    }

    /**
     * Agent configuration builder.
     */
    static class AgentBuilder {
        Map<String, Object> config = [:]

        void docker(String image) {
            config['docker'] = image
        }

        void docker(Map params) {
            StringBuilder sb = new StringBuilder("{\n")
            params.each { k, v ->
                sb.append("            ${k} '${v}'\n")
            }
            sb.append("        }")
            config['docker'] = sb.toString()
        }

        void kubernetes(Map params) {
            config['kubernetes'] = params
        }

        void label(String label) {
            config['label'] = label
        }

        void node(Map params) {
            config['node'] = params
        }
    }

    /**
     * Environment variables builder.
     */
    static class EnvironmentBuilder {
        List<String> vars = []

        void set(String name, String value) {
            vars.add("${name} = '${value}'")
        }

        void credentials(String name, String credentialsId) {
            vars.add("${name} = credentials('${credentialsId}')")
        }

        Object propertyMissing(String name, Object value) {
            vars.add("${name} = '${value}'")
        }
    }

    /**
     * Options builder.
     */
    static class OptionsBuilder {
        List<String> options = []

        void timeout(Map params) {
            options.add("timeout(time: ${params.time}, unit: '${params.unit}')")
        }

        void buildDiscarder(String config) {
            options.add("buildDiscarder(${config})")
        }

        void timestamps() {
            options.add("timestamps()")
        }

        void disableConcurrentBuilds() {
            options.add("disableConcurrentBuilds()")
        }

        void skipDefaultCheckout() {
            options.add("skipDefaultCheckout()")
        }

        void retry(int count) {
            options.add("retry(${count})")
        }

        String logRotator(Map params) {
            List<String> args = []
            if (params.numToKeepStr) args.add("numToKeepStr: '${params.numToKeepStr}'")
            if (params.daysToKeepStr) args.add("daysToKeepStr: '${params.daysToKeepStr}'")
            if (params.artifactNumToKeepStr) args.add("artifactNumToKeepStr: '${params.artifactNumToKeepStr}'")
            return "logRotator(${args.join(', ')})"
        }
    }

    /**
     * Parameters builder.
     */
    static class ParametersBuilder {
        List<String> params = []

        void string(Map config) {
            params.add("string(name: '${config.name}', defaultValue: '${config.defaultValue ?: ''}', description: '${config.description ?: ''}')")
        }

        void booleanParam(Map config) {
            params.add("booleanParam(name: '${config.name}', defaultValue: ${config.defaultValue ?: false}, description: '${config.description ?: ''}')")
        }

        void choice(Map config) {
            String choices = (config.choices as List).collect { "'${it}'" }.join(', ')
            params.add("choice(name: '${config.name}', choices: [${choices}], description: '${config.description ?: ''}')")
        }

        void text(Map config) {
            params.add("text(name: '${config.name}', defaultValue: '${config.defaultValue ?: ''}', description: '${config.description ?: ''}')")
        }
    }

    /**
     * Triggers builder.
     */
    static class TriggersBuilder {
        List<String> triggers = []

        void cron(String schedule) {
            triggers.add("cron('${schedule}')")
        }

        void pollSCM(String schedule) {
            triggers.add("pollSCM('${schedule}')")
        }

        void githubPush() {
            triggers.add("githubPush()")
        }

        void upstream(Map config) {
            triggers.add("upstream(upstreamProjects: '${config.projects}', threshold: hudson.model.Result.${config.threshold ?: 'SUCCESS'})")
        }
    }

    /**
     * Tools builder.
     */
    static class ToolsBuilder {
        List<String> tools = []

        void jdk(String version) {
            tools.add("jdk '${version}'")
        }

        void maven(String version) {
            tools.add("maven '${version}'")
        }

        void gradle(String version) {
            tools.add("gradle '${version}'")
        }

        void nodejs(String version) {
            tools.add("nodejs '${version}'")
        }
    }

    /**
     * Stages builder.
     */
    static class StagesBuilder {
        List<StageBuilder> stages = []

        void stage(String name, @DelegatesTo(StageBuilder) Closure config) {
            StageBuilder builder = new StageBuilder(name)
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            stages.add(builder)
        }

        String build() {
            return stages.collect { it.build() }.join('\n')
        }
    }

    /**
     * Stage builder.
     */
    static class StageBuilder {
        final String name
        private StepsBuilder stepsBuilder = new StepsBuilder()
        private String whenCondition
        private List<StageBuilder> parallelStages = []
        private PostBuilder postBuilder = new PostBuilder()
        private String agentLabel

        StageBuilder(String name) {
            this.name = name
        }

        void agent(String label) {
            this.agentLabel = label
        }

        void when(@DelegatesTo(WhenBuilder) Closure config) {
            WhenBuilder builder = new WhenBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.whenCondition = builder.build()
        }

        void steps(@DelegatesTo(StepsBuilder) Closure config) {
            config.delegate = stepsBuilder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        }

        void parallel(@DelegatesTo(StagesBuilder) Closure config) {
            StagesBuilder builder = new StagesBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            this.parallelStages = builder.stages
        }

        void post(@DelegatesTo(PostBuilder) Closure config) {
            config.delegate = postBuilder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
        }

        String build() {
            StringBuilder sb = new StringBuilder()
            sb.append("        stage('${name}') {\n")

            if (agentLabel) {
                sb.append("            agent { label '${agentLabel}' }\n")
            }

            if (whenCondition) {
                sb.append("            when {\n")
                sb.append("                ${whenCondition}\n")
                sb.append("            }\n")
            }

            if (parallelStages) {
                sb.append("            parallel {\n")
                parallelStages.each { stage ->
                    sb.append(stage.build().replaceAll('(?m)^', '    '))
                }
                sb.append("            }\n")
            } else {
                sb.append("            steps {\n")
                sb.append(stepsBuilder.build())
                sb.append("            }\n")
            }

            String postContent = postBuilder.build()
            if (postContent) {
                sb.append("            post {\n")
                sb.append(postContent)
                sb.append("            }\n")
            }

            sb.append("        }\n")
            return sb.toString()
        }
    }

    /**
     * Steps builder.
     */
    static class StepsBuilder {
        List<String> steps = []

        void sh(String command) {
            steps.add("sh '${escapeQuotes(command)}'")
        }

        void sh(Map params) {
            if (params.script) {
                steps.add("sh script: '''${params.script}''', returnStatus: ${params.returnStatus ?: false}")
            }
        }

        void bat(String command) {
            steps.add("bat '${escapeQuotes(command)}'")
        }

        void echo(String message) {
            steps.add("echo '${escapeQuotes(message)}'")
        }

        void checkout(String type) {
            steps.add("checkout ${type}")
        }

        void git(Map params) {
            List<String> args = []
            if (params.url) args.add("url: '${params.url}'")
            if (params.branch) args.add("branch: '${params.branch}'")
            if (params.credentialsId) args.add("credentialsId: '${params.credentialsId}'")
            steps.add("git ${args.join(', ')}")
        }

        void junit(String pattern) {
            steps.add("junit '${pattern}'")
        }

        void archiveArtifacts(String pattern) {
            steps.add("archiveArtifacts artifacts: '${pattern}'")
        }

        void publishHTML(Map params) {
            steps.add("publishHTML(target: [reportDir: '${params.reportDir}', reportFiles: '${params.reportFiles}', reportName: '${params.reportName}'])")
        }

        void withCredentials(List credentials, @DelegatesTo(StepsBuilder) Closure block) {
            StepsBuilder inner = new StepsBuilder()
            block.delegate = inner
            block.resolveStrategy = Closure.DELEGATE_FIRST
            block()

            String credStr = credentials.collect { "[${it}]" }.join(', ')
            steps.add("withCredentials([${credStr}]) {\n${inner.build()}                }")
        }

        void script(@DelegatesTo(ScriptBuilder) Closure config) {
            ScriptBuilder builder = new ScriptBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            steps.add("script {\n${builder.build()}                }")
        }

        String build() {
            return steps.collect { "                ${it}\n" }.join('')
        }

        private static String escapeQuotes(String s) {
            return s?.replace("'", "\\'") ?: ''
        }
    }

    /**
     * Script block builder.
     */
    static class ScriptBuilder {
        List<String> lines = []

        void code(String groovyCode) {
            lines.add(groovyCode)
        }

        Object methodMissing(String name, Object args) {
            lines.add("${name}(${(args as Object[]).collect { "'${it}'" }.join(', ')})")
        }

        String build() {
            return lines.collect { "                    ${it}\n" }.join('')
        }
    }

    /**
     * When condition builder.
     */
    static class WhenBuilder {
        List<String> conditions = []

        void branch(String pattern) {
            conditions.add("branch '${pattern}'")
        }

        void environment(String name, String value) {
            conditions.add("environment name: '${name}', value: '${value}'")
        }

        void expression(String expr) {
            conditions.add("expression { ${expr} }")
        }

        void not(@DelegatesTo(WhenBuilder) Closure config) {
            WhenBuilder inner = new WhenBuilder()
            config.delegate = inner
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            conditions.add("not { ${inner.build()} }")
        }

        void allOf(@DelegatesTo(WhenBuilder) Closure config) {
            WhenBuilder inner = new WhenBuilder()
            config.delegate = inner
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            conditions.add("allOf { ${inner.conditions.join('; ')} }")
        }

        void anyOf(@DelegatesTo(WhenBuilder) Closure config) {
            WhenBuilder inner = new WhenBuilder()
            config.delegate = inner
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            conditions.add("anyOf { ${inner.conditions.join('; ')} }")
        }

        String build() {
            return conditions.join('\n                ')
        }
    }

    /**
     * Post actions builder.
     */
    static class PostBuilder {
        Map<String, StepsBuilder> conditions = [:]

        void always(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('always', config)
        }

        void success(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('success', config)
        }

        void failure(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('failure', config)
        }

        void unstable(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('unstable', config)
        }

        void cleanup(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('cleanup', config)
        }

        void aborted(@DelegatesTo(StepsBuilder) Closure config) {
            addCondition('aborted', config)
        }

        private void addCondition(String name, Closure config) {
            StepsBuilder builder = new StepsBuilder()
            config.delegate = builder
            config.resolveStrategy = Closure.DELEGATE_FIRST
            config()
            conditions[name] = builder
        }

        String build() {
            if (conditions.isEmpty()) return ''

            StringBuilder sb = new StringBuilder()
            conditions.each { condition, builder ->
                sb.append("        ${condition} {\n")
                sb.append(builder.build())
                sb.append("        }\n")
            }
            return sb.toString()
        }
    }
}
