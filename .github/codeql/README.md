# CodeQL Configuration

## CodeQL and Groovy Compatibility

This directory contains configuration files for CodeQL static analysis.

### Why is CodeQL disabled for this repository?

CodeQL does not properly support Groovy code analysis. The CodeQL analyzer attempts to parse Groovy files as Java, which leads to errors during analysis because of Groovy-specific syntax and features that are not valid in Java.

The configuration file `codeql-groovy-config.yml` explicitly disables CodeQL scanning for all files in this repository to prevent workflow failures.

### Alternative Static Analysis

Instead of CodeQL, this repository uses the following for static analysis:

1. **CodeNarc**: Groovy-specific static analysis tool that checks for common bugs, bad practices, and style issues
2. **Custom security checks**: Implemented in CI workflows to detect hardcoded credentials and other security issues
3. **Manual code reviews**: Required for all pull requests

### Future Consideration

If CodeQL adds proper Groovy support in the future, we will revisit this configuration.