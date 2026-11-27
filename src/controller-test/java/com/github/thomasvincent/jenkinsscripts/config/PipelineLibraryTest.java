// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.FilePath;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RealJenkinsRule;

import static org.junit.Assert.assertEquals;

public class PipelineLibraryTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();
    @Rule public final RealJenkinsRule controller = new RealJenkinsRule()
        .withWar(new File(System.getProperty("jth.jenkins-war.path")))
        .includeTestClasspathPlugins(false)
        .javaOptions("-Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true")
        .withTimeout(180);

    @Test public void loadLibraryValidatePipelinesAndResumeBuild() throws Throwable {
        File repo = temporary.newFolder("library");
        for (String dir : new String[] {"src/com", "vars", "resources"}) {
            new FilePath(new File(dir).getAbsoluteFile()).copyRecursiveTo(new FilePath(new File(repo, dir)));
        }
        git(repo, "init", "--quiet", "-b", "main");
        git(repo, "add", "--", "src", "vars", "resources");
        git(repo, "commit", "--quiet", "-m", "Library fixture");

        Path plugins = controller.getHome().toPath().resolve("plugins");
        Files.createDirectories(plugins);
        try (var archives = Files.list(Path.of(System.getProperty("pipeline.pluginDir")))) {
            for (Path archive : archives.toList()) {
                try (JarFile plugin = new JarFile(archive.toFile())) {
                    String id = plugin.getManifest().getMainAttributes().getValue("Short-Name");
                    Files.copy(archive, plugins.resolve(id + ".jpi"));
                }
            }
        }

        // Gradle starts tests with an isolated worker classloader. The harness starts a separate JVM.
        String previous = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path", System.getProperty("pipeline.testClasspath"));
            String root = new File(".").getCanonicalPath();
            controller.startJenkins();
            controller.runRemotely(PipelineLibraryTest::runFixture, root, repo.getAbsolutePath(), "beforeRestart");
            controller.stopJenkins();
            controller.startJenkins();
            controller.runRemotely(PipelineLibraryTest::runFixture, root, repo.getAbsolutePath(), "afterRestart");
        } finally {
            System.setProperty("java.class.path", previous);
        }
    }

    private static void git(File repo, String... args) throws Exception {
        var command = new java.util.ArrayList<>(java.util.List.of("git", "-c", "user.name=Fixture",
            "-c", "user.email=fixture@localhost", "-c", "commit.gpgsign=false", "-c", "core.hooksPath=/dev/null"));
        command.addAll(java.util.List.of(args));
        assertEquals(0, new ProcessBuilder(command).directory(repo).inheritIO().start().waitFor());
    }

    private static void runFixture(JenkinsRule r, String root, String repo, String phase) throws Exception {
        Binding binding = new Binding();
        binding.setVariable("rule", r);
        binding.setVariable("libraryRepo", repo);
        binding.setVariable("project", new File(root));
        var shell = new GroovyShell(r.jenkins.getPluginManager().uberClassLoader, binding);
        shell.parse(new File(root, "src/controller-test/fixtures/PipelineController.groovy"))
            .invokeMethod(phase, new Object[0]);
    }
}
