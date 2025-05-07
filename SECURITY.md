# Security Policy

## Supported Versions

The following versions of Jenkins Script Library are currently supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

To report a security vulnerability, please follow these steps:

1. **Do NOT disclose the vulnerability publicly** 
2. Email security details to thomas.vincent@gmail.com
3. Include as much information as possible:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fixes (if any)

## Response Timeline

- Initial response: Within 48 hours
- Assessment: Within 1 week
- Fix timeline: Communicated after assessment

## Security Best Practices

When using this library:

1. Always run scripts with the minimum required permissions
2. Keep your Jenkins instance updated
3. Review all scripts before execution
4. Use credential binding rather than hardcoded credentials
5. Implement audit logging for script executions

## License

MIT License

Copyright (c) 2023-2025 Thomas Vincent