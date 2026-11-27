# SPDX-FileCopyrightText: 2026 Thomas Vincent
# SPDX-License-Identifier: MIT
"""Verify release contents against the tracked Jenkins library at the build revision."""

import subprocess
import sys
import tarfile
import zipfile
from pathlib import Path


def verify(project, version):
    stem = f"{project}-{version}"
    listing = subprocess.check_output([
        "git", "ls-files", "-z", "--", "src/com", "src/hudson", "vars", "resources",
        "examples/*.jenkinsfile", "templates/*.jenkinsfile", "README.md", "LICENSE",
    ])
    expected = {f"{stem}/{name}": Path(name).read_bytes()
                for name in listing.decode().split("\0") if name}
    distribution = Path("build/distributions") / stem
    with zipfile.ZipFile(f"{distribution}.zip") as archive:
        files = [entry for entry in archive.infolist() if not entry.is_dir()]
        compare(expected, [(entry.filename, archive.read(entry)) for entry in files], "ZIP")
    with tarfile.open(f"{distribution}.tar.gz", "r:gz") as archive:
        entries = []
        for entry in archive:
            if entry.isdir():
                continue
            if not entry.isfile():
                raise ValueError(f"Unexpected TAR member type for {entry.name}")
            entries.append((entry.name, archive.extractfile(entry).read()))
        compare(expected, entries, "TAR")
    with zipfile.ZipFile(Path("build/libs") / f"{stem}.jar") as jar:
        required = "com/github/thomasvincent/jenkinsscripts/dsl/PipelineDSL.class"
        if jar.testzip() or required not in jar.namelist():
            raise ValueError("Invalid compiled library JAR")


def compare(expected, entries, kind):
    actual = dict(entries)
    if len(entries) != len(actual):
        raise ValueError(f"Duplicate {kind} members")
    if actual.keys() != expected.keys():
        raise ValueError(f"Unexpected or missing {kind} paths: {sorted(actual.keys() ^ expected.keys())}")
    for name, content in actual.items():
        if expected[name] != content:
            raise ValueError(f"{kind} contents differ for {name}")


if __name__ == "__main__":
    verify(*sys.argv[1:])
