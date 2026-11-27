// SPDX-FileCopyrightText: 2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 * Copyright (c) 2026 Thomas Vincent
 *
 * Build-scoped Jenkins Pipeline step for Helm chart linting and deployment.
 */

import com.github.thomasvincent.jenkinsscripts.helm.HelmHelper
import java.util.logging.Level
import java.util.logging.Logger

@groovy.transform.Field
private static final Logger LOGGER = Logger.getLogger('com.github.thomasvincent.jenkinsscripts.vars.helmDeploy')

/**
 * Deploys a Helm chart from the current build agent using validated, shell-escaped flags.
 */
def call(Map args = [:]) {
    String release = (args.release ?: '').toString().trim()
    String chart = (args.chart ?: '').toString().trim()
    if (!release || !chart) {
        error 'helmDeploy requires both release and chart parameters'
    }

    boolean atomic = args.atomic != false
    boolean wait = args.wait != false
    boolean createNamespace = args.createNamespace == true
    boolean dryRun = args.dryRun == true
    boolean shouldLint = args.lint == true

    HelmHelper helper = new HelmHelper(this)
    String helmBin = (args.helmBin ?: 'helm').toString().trim()

    try {
        if (args.toolName) {
            String toolHome = tool(name: args.toolName as String)
            helmBin = "${toolHome}/helm"
        } else if (args.version) {
            String binDir = helper.use(args.version as String)
            helmBin = "${binDir}/helm"
        }

        if (shouldLint) {
            echo "[helmDeploy] Linting chart ${chart}"
            helper.lint([
                chart      : chart,
                valuesFiles: args.valuesFiles ?: [],
                helmBin    : helmBin
            ])
        }

        echo "[helmDeploy] ${dryRun ? 'Dry-run deploying' : 'Deploying'} release ${release} from ${chart}"
        Map<String, Object> result = helper.deploy([
            release        : release,
            chart          : chart,
            namespace      : args.namespace ?: 'default',
            valuesFiles    : args.valuesFiles ?: [],
            setValues      : args.setValues ?: [:],
            atomic         : atomic,
            wait           : wait,
            timeout        : args.timeout ?: '5m',
            createNamespace: createNamespace,
            dryRun         : dryRun,
            helmBin        : helmBin
        ])

        result.put('linted', shouldLint)
        return result
    } catch (Exception e) {
        LOGGER.log(Level.WARNING, "helmDeploy failed for release ${release}", e)
        echo "[helmDeploy] ERROR: ${e.message}"
        throw e
    }
}

/**
 * Installs a specific Helm version into the current build agent's temporary tools directory.
 */
def setup(String version = '3.17.0') {
    return new HelmHelper(this).use(version)
}

/**
 * Lints a Helm chart on the current build agent.
 */
def lint(Map args = [:]) {
    if (!args.chart) {
        error 'helmDeploy.lint requires a chart parameter'
    }
    return new HelmHelper(this).lint(args)
}
