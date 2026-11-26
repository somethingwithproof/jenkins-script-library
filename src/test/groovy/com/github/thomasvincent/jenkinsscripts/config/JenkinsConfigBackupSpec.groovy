/*
 * MIT License
 *
 * Copyright (c) 2023-2026 Thomas Vincent
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

package com.github.thomasvincent.jenkinsscripts.config

import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class JenkinsConfigBackupSpec extends Specification {

    @TempDir
    Path tempDir

    private Path createJenkinsHome() {
        Path home = tempDir.resolve("jenkins-home")
        Files.createDirectories(home.resolve("jobs/my-job/builds/1"))
        Files.createDirectories(home.resolve("jobs/my-job/workspace"))
        Files.writeString(home.resolve("config.xml"), "<hudson/>")
        Files.writeString(home.resolve("jobs/my-job/config.xml"), "<project/>")
        Files.writeString(home.resolve("jobs/my-job/builds/1/log"), "huge build log")
        Files.writeString(home.resolve("jobs/my-job/workspace/temp.txt"), "workspace file")
        return home
    }

    def "requires Jenkins.ADMINISTER permission"() {
        given:
        Jenkins jenkins = Mock(Jenkins) {
            hasPermission(Jenkins.ADMINISTER) >> false
        }
        JenkinsConfigBackup backup = new JenkinsConfigBackup(jenkins)

        when:
        backup.createBackup(tempDir.resolve("backups").toString())

        then:
        thrown(SecurityException)
    }

    def "compressed backup excludes job builds and workspace while backing up config.xml"() {
        given:
        Path home = createJenkinsHome()
        Path backupDir = tempDir.resolve("backups")
        Jenkins jenkins = Mock(Jenkins) {
            hasPermission(Jenkins.ADMINISTER) >> true
            getRootDir() >> home.toFile()
        }
        JenkinsConfigBackup backup = new JenkinsConfigBackup(jenkins)
            .withConfigFiles(["config.xml", "jobs/"])
            .withCompression(true)

        when:
        String zipPath = backup.createBackup(backupDir.toString())

        then:
        zipPath.endsWith(".zip")
        Files.exists(Path.of(zipPath))

        when:
        Set<String> entryNames = [] as Set
        new ZipFile(zipPath).withCloseable { ZipFile zf ->
            zf.entries().each { ZipEntry e -> entryNames.add(e.name) }
        }

        then:
        entryNames.contains("config.xml")
        entryNames.contains("jobs/my-job/config.xml")
        !entryNames.any { it.contains("builds/") }
        !entryNames.any { it.contains("workspace/") }
    }

    def "uncompressed backup, listing, restore, and purgeOldBackups work cleanly"() {
        given:
        Path home = createJenkinsHome()
        Path backupDir = tempDir.resolve("backups-uncompressed")
        Jenkins jenkins = Mock(Jenkins) {
            hasPermission(Jenkins.ADMINISTER) >> true
            getRootDir() >> home.toFile()
        }
        JenkinsConfigBackup backup = new JenkinsConfigBackup(jenkins)
            .withConfigFiles(["config.xml", "jobs/"])
            .withCompression(false)

        when:
        String createdPath = backup.createBackup(backupDir.toString())
        List<Map<String, Object>> available = backup.listAvailableBackups(backupDir.toString())

        then:
        Files.exists(Path.of(createdPath, "config.xml"))
        Files.exists(Path.of(createdPath, "jobs/my-job/config.xml"))
        !Files.exists(Path.of(createdPath, "jobs/my-job/builds/1/log"))
        available.size() == 1
        available[0].compressed == false
        available[0].size > 0

        when:
        Files.writeString(home.resolve("config.xml"), "<modified/>")
        boolean restored = backup.restoreBackup(createdPath)

        then:
        restored
        Files.readString(home.resolve("config.xml")) == "<hudson/>"
    }

    def "restoreBackup rejects Zip-Slip traversal entries"() {
        given:
        Path home = createJenkinsHome()
        Path maliciousZip = tempDir.resolve("evil.zip")
        new ZipOutputStream(Files.newOutputStream(maliciousZip)).withCloseable { ZipOutputStream zos ->
            zos.putNextEntry(new ZipEntry("../../outside.txt"))
            zos.write("escape".bytes)
            zos.closeEntry()
        }
        Jenkins jenkins = Mock(Jenkins) {
            hasPermission(Jenkins.ADMINISTER) >> true
            getRootDir() >> home.toFile()
        }
        JenkinsConfigBackup backup = new JenkinsConfigBackup(jenkins)

        when:
        backup.restoreBackup(maliciousZip.toString())

        then:
        thrown(SecurityException)
        !Files.exists(tempDir.resolve("outside.txt"))
    }
}
