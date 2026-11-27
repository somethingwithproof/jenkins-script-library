// SPDX-FileCopyrightText: 2026 Thomas Vincent
// SPDX-License-Identifier: MIT
package com.github.thomasvincent.jenkinsscripts.config

import com.cloudbees.groovy.cps.NonCPS

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import java.util.zip.ZipFile
import static java.nio.file.LinkOption.NOFOLLOW_LINKS
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

/** Plans restoration before writing, and preserves originals in a private recovery directory. */
class BackupRestore {

    private static final Object RESTORE_LOCK = new Object()
    private static final String ORIGINAL_SUFFIX = '.pre-restore'

    @NonCPS
    static Map restore(Path controllerHome, Path archive, Collection<String> names, boolean preview) {
        Path home = controllerHome.toRealPath()
        if (Files.isSymbolicLink(archive)) {
            throw new IOException('The backup must be a regular file or directory')
        }
        if (Files.isDirectory(archive, NOFOLLOW_LINKS)) {
            Map<Path, Boolean> entries = [:]
            BackupFiles.withStream(Files.walk(archive)) { paths ->
                paths.forEach { source ->
                    if (source == archive) {
                        return
                    }
                    boolean directory = Files.isDirectory(source, NOFOLLOW_LINKS)
                    if (!directory && !Files.isRegularFile(source, NOFOLLOW_LINKS)) {
                        throw new IOException("Unsupported backup entry: ${source}")
                    }
                    Path path = archive.relativize(source)
                    if (relative(path.toString()) != path) {
                        throw new IOException("Ambiguous directory backup entry: ${path}")
                    }
                    entries[path] = directory
                }
            }
            return apply(home, entries, names, preview) { path, file ->
                Files.newInputStream(archive.resolve(path), NOFOLLOW_LINKS).withCloseable { stream ->
                    Files.copy(stream, file, REPLACE_EXISTING)
                }
            }
        }
        return new ZipFile(archive.toFile()).withCloseable { zip ->
            Map<Path, Boolean> entries = [:]
            Map<Path, java.util.zip.ZipEntry> sources = [:]
            zip.entries().each { entry ->
                Path path = relative(entry.name)
                if (entries.put(path, entry.directory) != null) {
                    throw new IOException("Duplicate backup entry: ${path}")
                }
                sources[path] = entry
            }
            apply(home, entries, names, preview) { path, file ->
                def entry = sources[path]
                def checksum = new CRC32()
                new CheckedInputStream(zip.getInputStream(entry), checksum).withCloseable { stream ->
                    Files.copy(stream, file, REPLACE_EXISTING)
                }
                if (checksum.value != entry.crc) {
                    throw new IOException("Damaged backup entry: ${path}")
                }
            }
        }
    }

    @NonCPS
    private static Path relative(String name) {
        String portable = name.replace('\\', '/')
        if (!portable || portable.startsWith('/') || portable ==~ /^[A-Za-z]:.*/ ||
                portable.split('/').any { part -> part in ['', '.', '..'] }) {
            throw new SecurityException("Invalid backup path: ${name}")
        }
        return Paths.get(portable)
    }

    @NonCPS
    private static void checkTarget(Path home, Path path, boolean directory) {
        Path target = home.resolve(path)
        Path cursor = home
        for (Path part : path) {
            cursor = cursor.resolve(part)
            if (Files.isSymbolicLink(cursor)) {
                throw new IOException("Symbolic link in restore destination: ${cursor}")
            }
            if (Files.exists(cursor, NOFOLLOW_LINKS)) {
                boolean expectsDirectory = cursor != target || directory
                if (expectsDirectory ? !Files.isDirectory(cursor, NOFOLLOW_LINKS) : !Files.isRegularFile(cursor, NOFOLLOW_LINKS)) {
                    throw new IOException("Incompatible restore destination: ${cursor}")
                }
            }
        }
    }

    @NonCPS
    private static Map apply(Path home, Map<Path, Boolean> entries, Collection<String> names,
                             boolean preview, Closure stage) {
        def requested = names.collect { name -> relative(name) }
        requested.each { prefix ->
            if (!entries.keySet().any { entry -> entry.startsWith(prefix) }) {
                throw new IOException("Entry absent from backup: ${prefix}")
            }
        }
        def selected = entries.keySet().findAll { path -> !requested || requested.any { prefix -> path.startsWith(prefix) } }.sort()
        selected.each { path ->
            checkTarget(home, path, entries[path])
            for (Path parent = path.parent; parent != null; parent = parent.parent) {
                if (entries[parent] == false) {
                    throw new IOException("File used as directory in backup: ${parent}")
                }
            }
        }
        Map report = [entries: selected.collect { path -> path.toString() }, dryRun: preview, recoveryDirectory: null]
        if (preview || !selected) {
            return report
        }
        synchronized (RESTORE_LOCK) {
            return writeSelection(home, entries, selected, report, stage)
        }
    }

    @NonCPS
    private static Map writeSelection(Path home, Map<Path, Boolean> entries, Collection<Path> selected,
                                      Map report, Closure stage) {
        Path staging = Files.createTempDirectory('jenkins-restore-')
        List<Path> changed = [], directories = []
        Path recovery = null
        try {
            selected.findAll { path -> !entries[path] }.each { path ->
                Path file = staging.resolve(path)
                Files.createDirectories(file.parent)
                stage.call(path, file)
            }
            selected.each { path ->
                checkTarget(home, path, entries[path])
                Path target = home.resolve(path)
                Path directory = entries[path] ? target : target.parent
                createDirectories(directory, directories)
                if (entries[path]) {
                    return
                }
                Path original = null
                if (Files.exists(target, NOFOLLOW_LINKS)) {
                    if (Files.mismatch(target, staging.resolve(path)) == -1L) {
                        return
                    }
                    if (recovery == null) {
                        recovery = Files.createTempDirectory(home, '.jenkins-pre-restore-')
                    }
                    original = recovery.resolve(path.toString() + ORIGINAL_SUFFIX)
                    Files.createDirectories(original.parent)
                    Files.copy(target, original, COPY_ATTRIBUTES)
                }
                replaceFile(staging.resolve(path), target, original)
                changed.add(path)
            }
            report.recoveryDirectory = recovery?.toString()
            return report
        } catch (IOException | RuntimeException failure) {
            rollBack(home, recovery, changed, directories, failure)
            throw failure
        } finally {
            BackupFiles.withStream(Files.walk(staging)) { paths -> paths.sorted(Comparator.reverseOrder()).forEach { path -> Files.delete(path) } }
        }
    }

    @NonCPS
    private static void createDirectories(Path directory, List<Path> created) {
        List<Path> missing = []
        Path cursor = directory
        while (!Files.exists(cursor, NOFOLLOW_LINKS)) {
            missing.add(0, cursor)
            cursor = cursor.parent
        }
        missing.each { dir ->
            Files.createDirectory(dir)
            created.add(dir)
        }
    }

    @NonCPS
    private static void replaceFile(Path staged, Path target, Path original) {
        Path next = Files.createTempFile(target.parent, '.jenkins-restore-', '.tmp')
        try {
            // Preserve existing permissions; newly restored files use private temp-file permissions.
            if (original != null) {
                Files.copy(original, next, COPY_ATTRIBUTES, REPLACE_EXISTING)
            }
            Files.newOutputStream(next).withCloseable { out -> Files.copy(staged, out) }
            Files.move(next, target, ATOMIC_MOVE, REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(next)
        }
    }

    @NonCPS
    private static void rollBack(Path home, Path recovery, List<Path> changed, List<Path> directories,
                                 Exception failure) {
        changed.reverseEach { path ->
            try {
                Path original = recovery?.resolve(path.toString() + ORIGINAL_SUFFIX)
                if (original != null && Files.exists(original)) {
                    Files.copy(original, home.resolve(path), COPY_ATTRIBUTES, REPLACE_EXISTING)
                } else {
                    Files.deleteIfExists(home.resolve(path))
                }
            } catch (IOException rollback) {
                failure.addSuppressed(rollback)
            }
        }
        directories.reverseEach { dir ->
            try {
                Files.deleteIfExists(dir)
            } catch (IOException rollback) {
                failure.addSuppressed(rollback)
            }
        }
    }

}
