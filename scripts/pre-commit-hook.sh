#!/usr/bin/env bash

# Pre-commit hook for Jenkinsfile validation
# Copy this file to .git/hooks/pre-commit and make it executable:
#   cp scripts/pre-commit-hook.sh .git/hooks/pre-commit
#   chmod +x .git/hooks/pre-commit

set -e

echo "Running Jenkinsfile validation..."

# Find all Jenkinsfiles
JENKINSFILES=$(find . -name "Jenkinsfile*" -o -name "*.jenkinsfile" | grep -v ".git" || true)

if [[ -z "$JENKINSFILES" ]]; then
    echo "No Jenkinsfiles found, skipping validation"
    exit 0
fi

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

FAILED=0

for file in $JENKINSFILES; do
    # Check if file is staged for commit
    if git diff --cached --name-only | grep -q "$(basename "$file")"; then
        echo "Validating: $file"
        
        # Run validation script if available
        if [[ -f "scripts/validate-pipeline.sh" ]]; then
            if ! ./scripts/validate-pipeline.sh --local "$file"; then
                echo -e "${RED}✗ Validation failed for $file${NC}"
                FAILED=1
            else
                echo -e "${GREEN}✓ Validation passed for $file${NC}"
            fi
        else
            # Fallback to basic checks
            if ! grep -q "^pipeline" "$file"; then
                echo -e "${RED}✗ $file does not appear to use declarative syntax${NC}"
                FAILED=1
            else
                echo -e "${GREEN}✓ Basic check passed for $file${NC}"
            fi
        fi
        echo
    fi
done

if [[ $FAILED -eq 1 ]]; then
    echo -e "${RED}Jenkinsfile validation failed!${NC}"
    echo "Fix the issues above or use 'git commit --no-verify' to skip validation."
    exit 1
fi

echo -e "${GREEN}All Jenkinsfiles validated successfully!${NC}"
exit 0
