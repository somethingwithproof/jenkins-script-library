/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Thomas Vincent
 */

package com.github.thomasvincent.jenkinsscripts.pipeline

import groovy.text.SimpleTemplateEngine
import com.github.thomasvincent.jenkinsscripts.util.ValidationUtils

/**
 * Generates Jenkins pipeline scripts from templates.
 *
 * Usage:
 * def generator = new PipelineGenerator(templateText)
 * def script = generator.generate([project: 'demo'])
 */
class PipelineGenerator {
    private final String template

    PipelineGenerator(String template) {
        this.template = ValidationUtils.requireNonEmpty(template, 'Template')
    }

    static PipelineGenerator fromFile(File templateFile) {
        ValidationUtils.requireNonNull(templateFile, 'Template file')
        return new PipelineGenerator(templateFile.getText('UTF-8'))
    }

    String generate(Map params = [:]) {
        def engine = new SimpleTemplateEngine()
        def result = engine.createTemplate(template).make(params ?: [:])
        return result.toString()
    }

    boolean writeToFile(File destination, Map params = [:]) {
        ValidationUtils.requireNonNull(destination, 'Destination file')
        destination.setText(generate(params), 'UTF-8')
        return true
    }
}
