/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

import com.github.thomasvincent.jenkinsscripts.pipeline.PipelineGenerator

/**
 * Generates a Jenkins pipeline script from a template.
 *
 * Example:
 * ```groovy
 * generatePipeline(
 *     template: 'templates/basic-pipeline.jenkinsfile',
 *     parameters: [project: 'demo'],
 *     destination: 'Jenkinsfile'
 * )
 * ```
 *
 * @param template Path to template file or template text
 * @param parameters Map of template parameters
 * @param destination Optional file path to write the generated script
 * @return Generated pipeline script
 */
def call(Map args = [:]) {
    String template = args.template
    Map params = args.parameters ?: [:]
    String destination = args.destination

    if (!template) {
        error 'template parameter is required'
    }

    String templateText
    if (fileExists(template)) {
        templateText = readFile(template)
    } else {
        templateText = template
    }

    def generator = new PipelineGenerator(templateText)
    String script = generator.generate(params)

    if (destination) {
        writeFile file: destination, text: script
    }

    return script
}
