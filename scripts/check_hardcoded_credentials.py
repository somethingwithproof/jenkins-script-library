"""Reject literal credential assignments without printing credential values."""

import re
import sys
from pathlib import Path


ASSIGNMENT = re.compile(
    r"""\b\w*(?:password|token|secret)\s*=\s*(['"])[^'"]+\1""",
)


def main() -> int:
    paths = [Path(name) for name in sys.argv[1:]] or list(Path(".").rglob("*.groovy"))
    failed = False
    for path in paths:
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if ASSIGNMENT.search(line):
                print(f"{path}:{number}: literal credential assignment", file=sys.stderr)
                failed = True
    return int(failed)


if __name__ == "__main__":
    sys.exit(main())
