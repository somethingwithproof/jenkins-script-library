// SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
// SPDX-License-Identifier: MIT

/*
 * MIT License
 *
 * Copyright (c) 2024-2026 Thomas Vincent
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

package com.github.thomasvincent.jenkinsscripts.helm

import com.cloudbees.groovy.cps.NonCPS
import com.github.thomasvincent.jenkinsscripts.util.PipelineUtils

import java.io.Serializable
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Build-scoped Helm CLI helper for Jenkins pipelines.
 *
 * Installs a requested Helm binary into an agent-local temporary directory and
 * executes validated, shell-escaped Helm lint and upgrade/install commands
 * without relying on controller-side plugin descriptors.
 *
 * @author Thomas Vincent
 * @since 1.0
 */
class HelmHelper implements Serializable {

    private static final long serialVersionUID = 1L
    private static final String DEFAULT_VERSION = '3.17.0'
    private static final Pattern VERSION_PATTERN = Pattern.compile(/^v?[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z.]+)?$/)
    private static final Pattern RELEASE_PATTERN = Pattern.compile(/^[a-z0-9]([-a-z0-9]{0,51}[a-z0-9])?$/)
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(/^[a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?$/)
    private static final Pattern TIMEOUT_PATTERN = Pattern.compile(/^[0-9]+(ns|us|ms|s|m|h)$/)

    private transient Logger log

    /** Reference to the pipeline script context (Serializable in Jenkins CPS). */
    private final def pipeline

    /** Lazily re-initialized utility for platform detection. */
    private transient PipelineUtils utils

    HelmHelper(def pipeline) {
        this.pipeline = pipeline
        this.utils = pipeline != null ? new PipelineUtils(pipeline) : null
    }

    @NonCPS
    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(HelmHelper.class.name)
        }
        return log
    }

    private PipelineUtils getUtils() {
        if (utils == null) {
            if (pipeline == null) {
                throw new IllegalStateException('Pipeline script context is not initialized')
            }
            utils = new PipelineUtils(pipeline)
        }
        return utils
    }

    /**
     * Installs and activates a Helm version on the current build agent.
     *
     * @param version Semantic version (for example, '3.17.0' or 'v3.17.0')
     * @return Agent directory containing the installed helm executable
     */
    String use(String version = DEFAULT_VERSION) {
        String normalizedVersion = normalizeVersion(version)
        PipelineUtils pipelineUtils = getUtils()
        String os = pipelineUtils.currentOS()
        String arch = normalizeArchitecture(pipelineUtils.currentArchitecture())
        String extension = determineFileExtension()
        String downloadUrl = buildDownloadUrl(normalizedVersion, os, arch, extension)
        String baseTmp = pipeline.pwd(tmp: true)
        String binDir = "${baseTmp}/helm/${normalizedVersion}/${os}-${arch}"

        try {
            if (pipeline.isUnix()) {
                String installScript = [
                    'set -eu',
                    "BIN_DIR=${shellQuote(binDir)}",
                    'if [ ! -x "$BIN_DIR/helm" ]; then',
                    '  mkdir -p "$BIN_DIR"',
                    "  ARCHIVE=\"\$BIN_DIR/helm.${extension}\"",
                    "  curl -fsSL ${shellQuote(downloadUrl)} -o \"\$ARCHIVE\"",
                    '  tar -xzf "$ARCHIVE" -C "$BIN_DIR" --strip-components=1',
                    '  rm -f "$ARCHIVE"',
                    '  chmod +x "$BIN_DIR/helm"',
                    'fi'
                ].join('\n')
                pipeline.sh(script: installScript)
                String currentPath = pipeline.env?.PATH ?: ''
                pipeline.env.PATH = currentPath ? "${binDir}:${currentPath}" : binDir
            } else {
                String psScript = [
                    '$ErrorActionPreference = "Stop"',
                    "\$binDir = '${binDir.replace("'", "''")}'",
                    'if (-not (Test-Path (Join-Path $binDir "helm.exe"))) {',
                    '  New-Item -ItemType Directory -Force -Path $binDir | Out-Null',
                    '  $archive = Join-Path $binDir "helm.zip"',
                    "  Invoke-WebRequest -Uri '${downloadUrl}' -OutFile \$archive",
                    '  Expand-Archive -Path $archive -DestinationPath $binDir -Force',
                    '  Remove-Item -Path $archive -Force',
                    '}'
                ].join('\n')
                pipeline.powershell(script: psScript)
                String currentPath = pipeline.env?.PATH ?: ''
                pipeline.env.PATH = currentPath ? "${binDir};${currentPath}" : binDir
            }

            pipeline.echo "Using Helm ${normalizedVersion} installed at ${binDir}"
            return binDir
        } catch (Exception e) {
            getLog().log(Level.WARNING, "Failed to install Helm ${normalizedVersion}", e)
            pipeline.error("Failed to set up Helm ${normalizedVersion}: ${e.message}")
            throw e
        }
    }

    /**
     * Lints a Helm chart using shell-escaped arguments.
     */
    Map<String, Object> lint(Map args = [:]) {
        String chart = (args.chart ?: '').toString().trim()
        if (!chart) {
            throw new IllegalArgumentException('chart parameter is required')
        }
        String command = buildLintCommand(args)
        try {
            pipeline.sh(script: command)
        } catch (Exception e) {
            getLog().log(Level.WARNING, "Helm lint failed for chart ${chart}", e)
            throw e
        }
        return [
            chart  : chart,
            command: command,
            status : 'linted'
        ] as Map<String, Object>
    }

    /**
     * Runs `helm upgrade --install` with validated parameters and safe shell quoting.
     */
    Map<String, Object> deploy(Map args = [:]) {
        String release = (args.release ?: '').toString().trim()
        String chart = (args.chart ?: '').toString().trim()
        String namespace = (args.namespace ?: 'default').toString().trim()
        boolean atomic = args.atomic != false
        boolean wait = args.wait != false
        boolean createNamespace = args.createNamespace == true
        boolean dryRun = args.dryRun == true

        String command = buildDeployCommand(args)
        try {
            pipeline.sh(script: command)
        } catch (Exception e) {
            getLog().log(Level.WARNING, "Helm deploy failed for release ${release}", e)
            throw e
        }

        return [
            release        : release,
            chart          : chart,
            namespace      : namespace,
            atomic         : atomic,
            wait           : wait,
            createNamespace: createNamespace,
            dryRun         : dryRun,
            command        : command,
            status         : dryRun ? 'dry-run' : 'deployed'
        ] as Map<String, Object>
    }

    @NonCPS
    String buildLintCommand(Map args = [:]) {
        String chart = (args.chart ?: '').toString().trim()
        if (!chart) {
            throw new IllegalArgumentException('chart parameter is required')
        }
        String helmBin = (args.helmBin ?: 'helm').toString().trim()
        List<String> parts = [shellQuote(helmBin), 'lint', shellQuote(chart)]
        List valuesFiles = (args.valuesFiles instanceof List) ? (args.valuesFiles as List) : []
        for (Object file : valuesFiles) {
            String path = file?.toString()?.trim()
            if (path) {
                parts.add('-f')
                parts.add(shellQuote(path))
            }
        }
        return parts.join(' ')
    }

    @NonCPS
    String buildDeployCommand(Map args = [:]) {
        String release = (args.release ?: '').toString().trim()
        String chart = (args.chart ?: '').toString().trim()
        String namespace = (args.namespace ?: 'default').toString().trim()
        String timeout = (args.timeout ?: '5m').toString().trim()
        String helmBin = (args.helmBin ?: 'helm').toString().trim()

        if (!release || !RELEASE_PATTERN.matcher(release).matches()) {
            throw new IllegalArgumentException("Invalid Helm release name: '${release}'")
        }
        if (!chart) {
            throw new IllegalArgumentException('chart parameter is required')
        }
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid Kubernetes namespace: '${namespace}'")
        }
        if (!TIMEOUT_PATTERN.matcher(timeout).matches()) {
            throw new IllegalArgumentException("Invalid Helm timeout: '${timeout}'")
        }

        boolean atomic = args.atomic != false
        boolean wait = args.wait != false
        boolean createNamespace = args.createNamespace == true
        boolean dryRun = args.dryRun == true

        List<String> parts = [
            shellQuote(helmBin),
            'upgrade',
            '--install',
            shellQuote(release),
            shellQuote(chart),
            '--namespace',
            shellQuote(namespace),
            '--timeout',
            shellQuote(timeout)
        ]

        if (createNamespace) {
            parts.add('--create-namespace')
        }
        if (atomic) {
            parts.add('--atomic')
        }
        if (wait) {
            parts.add('--wait')
        }
        if (dryRun) {
            parts.add('--dry-run')
        }

        List valuesFiles = (args.valuesFiles instanceof List) ? (args.valuesFiles as List) : []
        for (Object file : valuesFiles) {
            String path = file?.toString()?.trim()
            if (path) {
                parts.add('-f')
                parts.add(shellQuote(path))
            }
        }

        if (args.setValues instanceof Map) {
            (args.setValues as Map).each { Object k, Object v ->
                String key = k?.toString()?.trim()
                if (key) {
                    parts.add('--set')
                    parts.add(shellQuote("${key}=${v == null ? '' : v.toString()}"))
                }
            }
        }

        return parts.join(' ')
    }

    @NonCPS
    String normalizeVersion(String version) {
        String candidate = (version ?: '').trim()
        if (!VERSION_PATTERN.matcher(candidate).matches()) {
            throw new IllegalArgumentException("Invalid Helm version: '${version}'")
        }
        return candidate.startsWith('v') ? candidate : "v${candidate}"
    }

    @NonCPS
    String normalizeArchitecture(String architecture) {
        return (architecture ?: 'amd64').replaceAll('i', '')
    }

    @NonCPS
    String determineFileExtension() {
        return pipeline.isUnix() ? 'tar.gz' : 'zip'
    }

    @NonCPS
    String buildDownloadUrl(String version, String os, String arch, String extension) {
        String normalizedVersion = normalizeVersion(version)
        return "https://get.helm.sh/helm-${normalizedVersion}-${os}-${arch}.${extension}"
    }

    @NonCPS
    static String shellQuote(String value) {
        return "'" + (value ?: '').replace("'", "'\"'\"'") + "'"
    }
}
