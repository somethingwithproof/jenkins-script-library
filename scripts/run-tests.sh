#!/bin/bash
#
# Jenkins Script Library Test Runner
#
# This script handles test execution and reporting for the Jenkins Script Library.
# It supports different test types and provides comprehensive reporting.
#
# Usage:
#   ./scripts/run-tests.sh [command]
#
# Commands:
#   test           - Run unit tests
#   integration    - Run integration tests
#   all            - Run all tests
#   codenarc       - Run CodeNarc static analysis
#   coverage       - Generate JaCoCo coverage report
#   report         - Generate test reports only (assumes tests have been run)
#   help           - Show this help message
#
# Environment Variables:
#   JAVA_VERSION         - Java version being used (default: detected from system)
#   JENKINS_CORE_VERSION - Jenkins core version (default: 2.361.4)
#   GRADLE_OPTS          - Additional Gradle options
#   TEST_DEBUG           - Set to 'true' for verbose output
#

set -e

# Set up colors for terminal output
if [ -t 1 ]; then
  BLUE='\033[0;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[0;33m'
  NC='\033[0m' # No Color
else
  BLUE=""
  GREEN=""
  RED=""
  YELLOW=""
  NC=""
fi

# Detect Java version if not provided
if [ -z "$JAVA_VERSION" ]; then
  JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed 's/^1\.//' | cut -d'.' -f1)
fi

# Set default Jenkins core version if not provided
JENKINS_CORE_VERSION=${JENKINS_CORE_VERSION:-2.361.4}

# Print banner
echo -e "${BLUE}=============================================${NC}"
echo -e "${BLUE}   Jenkins Script Library Test Runner${NC}"
echo -e "${BLUE}=============================================${NC}"
echo -e "${GREEN}Java version:${NC} $(java -version 2>&1 | head -1)"
echo -e "${GREEN}Gradle version:${NC} $(gradle --version | grep Gradle | cut -d ' ' -f 2)"
echo -e "${GREEN}Jenkins core:${NC} ${JENKINS_CORE_VERSION}"
echo -e "${GREEN}Working directory:${NC} $(pwd)"
echo -e "${BLUE}=============================================${NC}"
echo

# Ensure we're in the project root
if [ ! -f "build.gradle" ]; then
  echo -e "${RED}Error:${NC} Script must be run from project root"
  exit 1
fi

# Create settings.gradle if it doesn't exist
if [ ! -f "settings.gradle" ]; then
  echo -e "${YELLOW}Warning:${NC} settings.gradle not found, creating minimal version"
  echo "rootProject.name = 'jenkins-script-library'" > settings.gradle
fi

# Ensure gradlew is executable
if [ -f "./gradlew" ]; then
  chmod +x ./gradlew
  GRADLE_CMD="./gradlew"
else
  GRADLE_CMD="gradle"
fi

# Default command
COMMAND=${1:-test}

# Common Gradle options
GRADLE_COMMON_OPTS="--no-daemon"
if [ "$TEST_DEBUG" = "true" ]; then
  GRADLE_COMMON_OPTS="$GRADLE_COMMON_OPTS --info --stacktrace"
fi

# Run the appropriate command
case "$COMMAND" in
  test|unit)
    echo -e "${GREEN}Running unit tests...${NC}"
    set +e
    $GRADLE_CMD clean test $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    ;;
    
  integration)
    echo -e "${GREEN}Running integration tests...${NC}"
    set +e
    $GRADLE_CMD integrationTest $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    ;;
    
  all)
    echo -e "${GREEN}Running all tests...${NC}"
    set +e
    $GRADLE_CMD clean test integrationTest $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    ;;
    
  codenarc)
    echo -e "${GREEN}Running CodeNarc analysis...${NC}"
    set +e
    $GRADLE_CMD clean codenarcMain codenarcTest $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    ;;
    
  coverage)
    echo -e "${GREEN}Generating JaCoCo coverage report...${NC}"
    set +e
    $GRADLE_CMD clean test jacocoTestReport $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    echo -e "${GREEN}Coverage report generated at:${NC} build/reports/jacoco/test/html/index.html"
    ;;
    
  report)
    echo -e "${GREEN}Generating test reports...${NC}"
    set +e
    $GRADLE_CMD jacocoTestReport $GRADLE_COMMON_OPTS
    TEST_RESULT=$?
    set -e
    echo -e "${GREEN}Test report generated at:${NC} build/reports/tests/test/index.html"
    echo -e "${GREEN}Coverage report generated at:${NC} build/reports/jacoco/test/html/index.html"
    ;;
    
  help)
    grep -A 15 "^# Usage:" "$0" | grep -v grep | sed 's/^# //g'
    exit 0
    ;;
    
  *)
    echo -e "${RED}Unknown command:${NC} $COMMAND"
    echo -e "Run '$0 help' for usage information"
    exit 1
    ;;
esac

# Process test results
if [ -n "$TEST_RESULT" ] && [ $TEST_RESULT -ne 0 ]; then
  echo -e "${RED}Tests failed with exit code:${NC} $TEST_RESULT"
  
  # Look for specific test failures
  if [ -d "build/reports/tests" ]; then
    FAILURES=$(find build/reports/tests -name "*.xml" | xargs grep -l "<failure" 2>/dev/null || true)
    if [ -n "$FAILURES" ]; then
      echo -e "${RED}The following tests failed:${NC}"
      for file in $FAILURES; do
        TEST_CLASS=$(basename "$file" | sed 's/TEST-\(.*\)\.xml/\1/')
        echo -e "  ${YELLOW}$TEST_CLASS${NC}"
        grep -A 2 "<failure" "$file" | grep -v "^--$" | sed 's/^/    /'
      done
    fi
  fi
  
  exit $TEST_RESULT
fi

echo -e "${GREEN}Tests completed successfully${NC}"
exit 0

