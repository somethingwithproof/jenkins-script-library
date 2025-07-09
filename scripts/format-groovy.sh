#!/bin/bash
# Format Groovy files according to Google Java Style Guide

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to format a single file
format_file() {
    local file="$1"
    echo -e "${YELLOW}Formatting: $file${NC}"
    
    # Apply Google style formatting rules
    # 1. Fix indentation (4 spaces)
    sed -i.bak 's/\t/    /g' "$file"
    
    # 2. Fix line length (max 100 chars) - warn only
    if grep -n '.\{101\}' "$file" > /dev/null; then
        echo -e "${YELLOW}Warning: Lines exceeding 100 characters in $file${NC}"
    fi
    
    # 3. Fix spacing around operators
    sed -i.bak 's/\([^[:space:]]\)+\([^[:space:]+=]\)/\1 + \2/g' "$file"
    sed -i.bak 's/\([^[:space:]]\)-\([^[:space:]-=]\)/\1 - \2/g' "$file"
    sed -i.bak 's/\([^[:space:]]\)\*\([^[:space:]*=]\)/\1 * \2/g' "$file"
    sed -i.bak 's/\([^[:space:]]\)\/\([^[:space:]/=]\)/\1 \/ \2/g' "$file"
    
    # 4. Fix spacing around colons in maps
    sed -i.bak 's/:\([^[:space:]]\)/: \1/g' "$file"
    sed -i.bak 's/\([^[:space:]]\):/\1 :/g' "$file"
    
    # 5. Fix spacing after commas
    sed -i.bak 's/,\([^[:space:]]\)/, \1/g' "$file"
    
    # 6. Remove trailing whitespace
    sed -i.bak 's/[[:space:]]*$//' "$file"
    
    # 7. Ensure file ends with newline
    if [ -n "$(tail -c1 "$file")" ]; then
        echo >> "$file"
    fi
    
    # 8. Fix opening brace placement (same line)
    sed -i.bak ':a;N;$!ba;s/\n[[:space:]]*{/ {/g' "$file"
    
    # Remove backup files
    rm -f "${file}.bak"
}

# Main
if [ "$#" -eq 0 ]; then
    # Format all Groovy files
    find . -name "*.groovy" -type f | while read -r file; do
        format_file "$file"
    done
else
    # Format specific files
    for file in "$@"; do
        if [ -f "$file" ]; then
            format_file "$file"
        else
            echo -e "${RED}Error: File not found: $file${NC}"
            exit 1
        fi
    done
fi

echo -e "${GREEN}Formatting complete!${NC}"