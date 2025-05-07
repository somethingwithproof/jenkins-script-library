ruleset {
    description 'CodeNarc RuleSet for Jenkins Script Library'

    // Basic Rules
    EmptyClass
    EmptyMethod
    ReturnFromFinallyBlock
    StringInstantiation
    UnusedVariable
    UnnecessaryBooleanExpression
    
    // Braces
    IfStatementBraces
    WhileStatementBraces
    ForStatementBraces
    
    // Exceptions
    CatchException
    ThrowException
    ExceptionExtendsError
    CatchIllegalMonitorStateException
    
    // Concurrency
    BusyWait
    DoubleCheckedLocking
    InconsistentPropertyLocking
    
    // Security
    FileCreateTempFile
    InsecureRandom
    SystemExit
    
    // Unused
    UnusedImport
    UnusedMethodParameter
    UnusedPrivateField
    UnusedPrivateMethod
    
    // Size
    CyclomaticComplexity {
        maxMethodComplexity = 15
    }
    MethodCount {
        maxMethods = 30
    }
    MethodSize {
        maxLines = 100
    }
    NestedBlockDepth {
        maxNestedBlockDepth = 5
    }
    
    // Code Smells
    DuplicateImport
    DuplicateStringLiteral {
        ignoreStrings = ['', '\'\'', '""', '\\n', '\\t', ' ']
    }
    
    // Grails
    GrailsPublicControllerMethod
    GrailsServletContextReference
    
    // Formatting
    ClassJavadoc
    LineLength {
        length = 120
    }
    
    // Miscellaneous
    GrailsStatelessService
}