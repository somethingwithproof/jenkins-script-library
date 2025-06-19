#!/bin/bash
#
# Script for running tests in Docker environment
# Usage: ./scripts/run-tests.sh [test|codenarc|jacocoTestReport|all]
#

set -e

# Print environment info
echo "=== Environment Information ==="
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Gradle version: $(gradle --version | grep Gradle | cut -d ' ' -f 2)"
echo "Working directory: $(pwd)"
echo "==============================="

# Default command is 'test'
CMD=${1:-test}

case "$CMD" in
  test)
    echo "Running Gradle tests..."
    ./gradlew clean test --no-daemon --info
    ;;
    
  codenarc)
    echo "Running CodeNarc analysis..."
    ./gradlew clean codenarcMain codenarcTest --no-daemon
    ;;
    
  jacocoTestReport)
    echo "Generating JaCoCo test reports..."
    ./gradlew clean test jacocoTestReport --no-daemon
    ;;
    
  all)
    echo "Running all tests and reports..."
    ./gradlew clean test codenarcMain codenarcTest jacocoTestReport --no-daemon
    ;;
    
  *)
    echo "Unknown command: $CMD"
    echo "Usage: $0 [test|codenarc|jacocoTestReport|all]"
    exit 1
    ;;
esac

# Check for test failures
if [ -d "build/reports/tests" ]; then
  FAILURES=$(find build/reports/tests -name "*.xml" | xargs grep -l "<failure" 2>/dev/null || true)
  if [ -n "$FAILURES" ]; then
    echo "The following tests failed:"
    echo "$FAILURES" | sed 's/.*TEST-\(.*\).xml/\1/'
    exit 1
  fi
fi

echo "Tests completed successfully"
exit 0

