## Security Policy

We take security seriously. If you discover any security related issues, please email thomasvincent@gmail.com instead of using the issue tracker.

### Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

### Reporting a Vulnerability

Please report (suspected) security vulnerabilities to thomasvincent@gmail.com. You will receive a response from us within 48 hours. If the issue is confirmed, we will release a patch as soon as possible depending on complexity but historically within 7 days.

### Additional Security Considerations

When using these Jenkins scripts, please consider the following security best practices:

1. Always run scripts with the minimum required permissions in Jenkins
2. Review code before execution, especially when dealing with system operations
3. Keep your Jenkins instance and plugins up to date
4. Use credential binding rather than hardcoding secrets in scripts
5. Implement audit logging for script executions
