#!/bin/bash
# Validate Jenkins scripts for idiomatic patterns

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Jenkins idiomatic patterns to check
check_file() {
    local file="$1"
    local errors=0
    
    echo -e "${YELLOW}Validating: $file${NC}"
    
    # Check for @NonCPS annotation where needed
    if grep -E "(each|collect|findAll|find|any|every)" "$file" | grep -v "@NonCPS" > /dev/null 2>&1; then
        echo -e "${YELLOW}Warning: Potential CPS issue - consider @NonCPS for closures in $file${NC}"
    fi
    
    # Check for proper script security annotations
    if grep -E "new File|execute\(|eval\(" "$file" > /dev/null 2>&1; then
        if ! grep -E "@Whitelisted|approved by administrator" "$file" > /dev/null 2>&1; then
            echo -e "${RED}Error: Potentially unsafe operations without approval in $file${NC}"
            ((errors++))
        fi
    fi
    
    # Check for proper use of Jenkins.instance
    if grep -E "Jenkins\.getInstance\(\)" "$file" > /dev/null 2>&1; then
        echo -e "${YELLOW}Warning: Use Jenkins.get() instead of getInstance() in $file${NC}"
    fi
    
    # Check for proper error handling
    if grep -E "catch\s*\(\s*Exception\s+\w+\s*\)" "$file" > /dev/null 2>&1; then
        if ! grep -E "logger|println|throw" "$file" > /dev/null 2>&1; then
            echo -e "${RED}Error: Caught exceptions should be logged or rethrown in $file${NC}"
            ((errors++))
        fi
    fi
    
    # Check for pipeline syntax
    if grep -E "pipeline\s*{" "$file" > /dev/null 2>&1; then
        # Check for agent declaration
        if ! grep -E "agent\s+(any|none|{)" "$file" > /dev/null 2>&1; then
            echo -e "${RED}Error: Pipeline missing agent declaration in $file${NC}"
            ((errors++))
        fi
    fi
    
    # Check for proper import statements
    if grep -E "^import\s+java\.(io|net|lang\.reflect)" "$file" > /dev/null 2>&1; then
        echo -e "${YELLOW}Warning: Restricted imports detected in $file - may require approval${NC}"
    fi
    
    # Check for hardcoded credentials
    if grep -E "(password|token|secret|key)\s*=\s*['\"][^'\"]+['\"]" "$file" > /dev/null 2>&1; then
        echo -e "${RED}Error: Potential hardcoded credentials in $file${NC}"
        ((errors++))
    fi
    
    return $errors
}

# Main
total_errors=0

if [ "$#" -eq 0 ]; then
    # Validate all Groovy files
    while IFS= read -r -d '' file; do
        if check_file "$file"; then
            :
        else
            ((total_errors+=$?))
        fi
    done < <(find . -name "*.groovy" -type f -print0)
else
    # Validate specific files
    for file in "$@"; do
        if [ -f "$file" ]; then
            if check_file "$file"; then
                :
            else
                ((total_errors+=$?))
            fi
        fi
    done
fi

if [ $total_errors -eq 0 ]; then
    echo -e "${GREEN}All files validated successfully!${NC}"
    exit 0
else
    echo -e "${RED}Validation failed with $total_errors errors${NC}"
    exit 1
fi