#!/bin/bash
# Check and add license headers to Groovy files

set -e

LICENSE_HEADER="/*
 * Copyright (c) $(date +%Y) Thomas Vincent
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */"

# Check if file has license header
has_license() {
    head -n 10 "$1" | grep -q "Copyright"
}

# Add license header to file
add_license() {
    local file="$1"
    local temp_file=$(mktemp)
    
    echo "$LICENSE_HEADER" > "$temp_file"
    echo "" >> "$temp_file"
    cat "$file" >> "$temp_file"
    
    mv "$temp_file" "$file"
}

# Process files
if [ "$#" -eq 0 ]; then
    find src -name "*.groovy" -type f | while read -r file; do
        if ! has_license "$file"; then
            echo "Adding license to: $file"
            add_license "$file"
        fi
    done
else
    for file in "$@"; do
        if [ -f "$file" ] && ! has_license "$file"; then
            echo "Adding license to: $file"
            add_license "$file"
        fi
    done
fi