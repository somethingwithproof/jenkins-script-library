package com.github.thomasvincent.jenkinsscripts.pipeline

import spock.lang.Specification
import spock.lang.Title

@Title("PipelineGenerator Specification")
class PipelineGeneratorSpec extends Specification {

    def "generates pipeline with parameters"() {
        given:
        String template = 'pipeline { stages { stage(\'Build\'){ steps { echo "${project}" } } } }'
        def generator = new PipelineGenerator(template)

        when:
        String result = generator.generate([project: 'demo'])

        then:
        result.contains('echo "demo"')
    }

    def "writes generated pipeline to file"() {
        given:
        String template = 'pipeline { agent any }'
        def generator = new PipelineGenerator(template)
        File tempFile = File.createTempFile('pipeline', '.jenkinsfile')

        when:
        generator.writeToFile(tempFile)

        then:
        tempFile.text.contains('pipeline { agent any }')

        cleanup:
        tempFile.delete()
    }
}
