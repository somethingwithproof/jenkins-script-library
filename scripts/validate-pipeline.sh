#!/usr/bin/env bash

# Declarative Pipeline Validator
# Validates Jenkinsfile syntax using Jenkins API or local linting

set -euo pipefail

# Configuration
JENKINS_URL="${JENKINS_URL:-http://localhost:8080}"
JENKINS_USER="${JENKINS_USER:-}"
JENKINS_TOKEN="${JENKINS_TOKEN:-}"
JENKINSFILE="${1:-Jenkinsfile}"
VERBOSE="${VERBOSE:-false}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $*"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $*"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $*"
}

show_usage() {
    cat << EOF
Usage: $0 [OPTIONS] [JENKINSFILE]

Validates Jenkins declarative pipeline syntax.

OPTIONS:
    -h, --help              Show this help message
    -u, --url URL           Jenkins URL (default: http://localhost:8080)
    -v, --verbose           Verbose output
    --user USER             Jenkins username
    --token TOKEN           Jenkins API token
    --local                 Use local validation only (no Jenkins API)

EXAMPLES:
    # Validate default Jenkinsfile
    $0

    # Validate specific file
    $0 path/to/Jenkinsfile

    # Validate with Jenkins API
    $0 --url https://jenkins.example.com --user admin --token abc123

    # Local validation only
    $0 --local

ENVIRONMENT VARIABLES:
    JENKINS_URL             Jenkins server URL
    JENKINS_USER            Jenkins username
    JENKINS_TOKEN           Jenkins API token
    VERBOSE                 Enable verbose output (true/false)

EOF
}

# Parse arguments
LOCAL_ONLY=false
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_usage
            exit 0
            ;;
        -u|--url)
            JENKINS_URL="$2"
            shift 2
            ;;
        --user)
            JENKINS_USER="$2"
            shift 2
            ;;
        --token)
            JENKINS_TOKEN="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --local)
            LOCAL_ONLY=true
            shift
            ;;
        -*)
            log_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
        *)
            JENKINSFILE="$1"
            shift
            ;;
    esac
done

# Check if file exists
if [[ ! -f "$JENKINSFILE" ]]; then
    log_error "File not found: $JENKINSFILE"
    exit 1
fi

log_info "Validating: $JENKINSFILE"

# Local syntax check
log_info "Performing local syntax check..."

# Check for basic structure
if ! grep -q "^pipeline" "$JENKINSFILE"; then
    log_warn "File does not appear to use declarative syntax (missing 'pipeline' block)"
fi

# Check for required sections
if ! grep -q "agent" "$JENKINSFILE"; then
    log_error "Missing required 'agent' directive"
    exit 1
fi

if ! grep -q "stages" "$JENKINSFILE"; then
    log_error "Missing required 'stages' section"
    exit 1
fi

# Check for common issues
ISSUES=()

# Check for node blocks in declarative (should use agent instead)
if grep -q "^\s*node\s*{" "$JENKINSFILE"; then
    ISSUES+=("Found 'node' block - should use 'agent' in declarative pipelines")
fi

# Check for script blocks that are too large
script_block_count=$(grep -c "script\s*{" "$JENKINSFILE" || true)
if [[ $script_block_count -gt 5 ]]; then
    ISSUES+=("Warning: Found $script_block_count 'script' blocks - consider refactoring to shared library functions")
fi

# Check for try-catch outside script blocks
if grep -qE "^\s*try\s*{" "$JENKINSFILE"; then
    # Check if this try block is inside a script block
    if ! grep -B5 "^\s*try\s*{" "$JENKINSFILE" | grep -q "script\s*{"; then
        ISSUES+=("Found 'try-catch' outside 'script' block - wrap in script{} or use post{} sections")
    fi
fi

# Check for stage inside stage (nested stages) - but exclude parallel blocks
# This checks if there are multiple stage declarations that might be nested
TEMP_FILE="/tmp/jenkinsfile_check_$$"
grep -v "parallel\s*{" "$JENKINSFILE" > "$TEMP_FILE" 2>/dev/null || true
if grep -A5 "stage(" "$TEMP_FILE" | grep -c "stage(" | grep -qE "^[2-9]"; then
    # Only flag if multiple stage declarations are really nested (not in parallel)
    ISSUES+=("Warning: Possible nested stages detected - consider using parallel{} or sequential stages")
fi
rm -f "$TEMP_FILE"

# Report local issues
if [[ ${#ISSUES[@]} -gt 0 ]]; then
    log_warn "Local syntax check found potential issues:"
    for issue in "${ISSUES[@]}"; do
        echo "  - $issue"
    done
    echo
fi

# Jenkins API validation
if [[ "$LOCAL_ONLY" == "false" ]]; then
    log_info "Validating with Jenkins API..."
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        log_warn "curl not found - skipping Jenkins API validation"
        exit 0
    fi
    
    # Prepare authentication
    AUTH_ARGS=""
    if [[ -n "$JENKINS_USER" ]] && [[ -n "$JENKINS_TOKEN" ]]; then
        AUTH_ARGS="-u ${JENKINS_USER}:${JENKINS_TOKEN}"
    fi
    
    # Try to validate
    VALIDATION_URL="${JENKINS_URL}/pipeline-model-converter/validate"
    
    if [[ "$VERBOSE" == "true" ]]; then
        log_info "Validation URL: $VALIDATION_URL"
    fi
    
    # Use proper quoting for AUTH_ARGS
    if [[ -n "$AUTH_ARGS" ]]; then
        RESPONSE=$(curl -s -w "\n%{http_code}" $AUTH_ARGS -X POST \
            -F "jenkinsfile=<$JENKINSFILE" \
            "$VALIDATION_URL" 2>&1 || true)
    else
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
            -F "jenkinsfile=<$JENKINSFILE" \
            "$VALIDATION_URL" 2>&1 || true)
    fi
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [[ "$HTTP_CODE" == "200" ]]; then
        # Check response for errors
        if echo "$BODY" | grep -qi "error"; then
            log_error "Jenkins API validation failed:"
            echo "$BODY" | jq -r '.errors[]?' 2>/dev/null || echo "$BODY"
            exit 1
        else
            log_info "Jenkins API validation passed!"
        fi
    elif [[ "$HTTP_CODE" == "000" ]]; then
        log_warn "Could not connect to Jenkins at $JENKINS_URL"
        log_warn "Skipping Jenkins API validation"
    else
        log_warn "Jenkins API returned HTTP $HTTP_CODE"
        if [[ "$VERBOSE" == "true" ]]; then
            echo "$BODY"
        fi
        log_warn "Skipping Jenkins API validation"
    fi
fi

# Success
log_info "Validation complete! ✓"

# Provide recommendations
if [[ ${#ISSUES[@]} -eq 0 ]]; then
    echo
    log_info "No issues found. Your pipeline follows declarative syntax best practices."
else
    echo
    log_warn "Consider addressing the issues mentioned above."
    log_info "See PIPELINE_MIGRATION_GUIDE.md for more information."
fi

exit 0
