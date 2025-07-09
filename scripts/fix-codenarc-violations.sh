#!/bin/bash
# Fix common CodeNarc violations in Groovy files

set -e

echo "=== Fixing CodeNarc Violations ==="

# Function to add imports if not present
add_import() {
    local file="$1"
    local import="$2"
    
    if ! grep -q "^import $import" "$file"; then
        # Find the last import line or package line
        if grep -q "^import " "$file"; then
            # Add after last import
            sed -i '' "/^import /{ :a; n; /^import /ba; i\\
import $import
}" "$file"
        elif grep -q "^package " "$file"; then
            # Add after package
            sed -i '' "/^package /a\\
\\
import $import" "$file"
        else
            # Add at beginning
            sed -i '' "1i\\
import $import\\
" "$file"
        fi
    fi
}

# Function to add annotation to class if not present
add_class_annotation() {
    local file="$1"
    local annotation="$2"
    
    if ! grep -q "^@$annotation" "$file"; then
        # Add before class declaration
        sed -i '' "/^class /i\\
@$annotation" "$file"
    fi
}

# Fix 1: Replace println with logger
echo "Fixing println statements..."
for file in src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/*.groovy; do
    if grep -q "println " "$file"; then
        echo "  Processing: $file"
        
        # Add logger import and field if needed
        if ! grep -q "java.util.logging.Logger" "$file"; then
            add_import "$file" "java.util.logging.Logger"
            
            # Add logger field after class declaration
            sed -i '' "/^class /a\\
\\
    private static final Logger LOGGER = Logger.getLogger(${file##*/}.replace('.groovy', ''))" "$file"
        fi
        
        # Replace println with logger.info
        sed -i '' 's/println "\(.*\)"/LOGGER.info("\1")/g' "$file"
        sed -i '' 's/println \(.*\)/LOGGER.info(\1.toString())/g' "$file"
    fi
done

# Fix 2: Add @CompileStatic annotations
echo "Adding @CompileStatic annotations..."
for file in src/main/groovy/com/github/thomasvincent/jenkinsscripts/**/*.groovy; do
    if grep -q "^class " "$file" && ! grep -q "@CompileStatic" "$file"; then
        echo "  Processing: $file"
        add_import "$file" "groovy.transform.CompileStatic"
        add_class_annotation "$file" "CompileStatic"
    fi
done

# Fix 3: Replace SimpleDateFormat with DateTimeFormatter
echo "Fixing SimpleDateFormat usage..."
find src -name "*.groovy" -exec grep -l "SimpleDateFormat" {} \; | while read -r file; do
    echo "  Processing: $file"
    
    # Add java.time imports
    add_import "$file" "java.time.format.DateTimeFormatter"
    add_import "$file" "java.time.LocalDateTime"
    add_import "$file" "java.time.ZoneId"
    
    # Replace SimpleDateFormat with DateTimeFormatter
    sed -i '' 's/SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm")/DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")/g' "$file"
    
    # Update format calls
    sed -i '' 's/DATE_FORMATTER\.format(\([^)]*\))/LocalDateTime.ofInstant(\1.toInstant(), ZoneId.systemDefault()).format(DATE_FORMATTER)/g' "$file"
done

# Fix 4: Extract duplicate literals to constants
echo "Extracting duplicate string literals..."
cat > /tmp/extract_constants.groovy << 'EOF'
#!/usr/bin/env groovy

def file = new File(args[0])
def content = file.text

// Common duplicate strings to extract
def constants = [
    'pattern': 'PATTERN_PARAM',
    'number': 'NUMBER_PARAM', 
    'percent': 'PERCENT_PARAM',
    'unknown': 'UNKNOWN_VALUE',
    'None\n': 'NONE_MESSAGE',
    '%.1f': 'DECIMAL_FORMAT'
]

// Add constants after class declaration
def classLine = content.findIndexOf { it.contains('class ') }
if (classLine >= 0) {
    def lines = content.readLines()
    def insertIndex = classLine + 1
    
    // Insert constants
    def constantsBlock = """
    // Constants
    private static final String PATTERN_PARAM = 'pattern'
    private static final String NUMBER_PARAM = 'number'
    private static final String PERCENT_PARAM = 'percent'
    private static final String UNKNOWN_VALUE = 'unknown'
    private static final String NONE_MESSAGE = 'None\\n'
    private static final String DECIMAL_FORMAT = '%.1f'
    private static final int MAX_DISPLAY_ITEMS = 10
"""
    
    lines.add(insertIndex, constantsBlock)
    
    // Replace occurrences
    def updatedContent = lines.join('\n')
    constants.each { literal, constant ->
        updatedContent = updatedContent.replaceAll("'${literal}'", constant)
        updatedContent = updatedContent.replaceAll('"' + literal + '"', constant)
    }
    
    // Replace duplicate number 10
    updatedContent = updatedContent.replaceAll(/(\s)10(\s|[),;])/, '$1MAX_DISPLAY_ITEMS$2')
    
    file.text = updatedContent
}
EOF

groovy /tmp/extract_constants.groovy src/main/groovy/com/github/thomasvincent/jenkinsscripts/scripts/AnalyzeJobHealth.groovy

# Fix 5: Break long lines
echo "Breaking long lines..."
for file in src/main/groovy/com/github/thomasvincent/jenkinsscripts/**/*.groovy; do
    if grep -E '^.{101,}' "$file" > /dev/null; then
        echo "  Processing: $file"
        # This is complex - we'll handle it manually in the actual fixes
    fi
done

echo "=== CodeNarc Violations Fix Script Complete ==="
echo "Note: Some violations may require manual fixes"
echo "Run './gradlew codenarcMain' to check remaining violations"