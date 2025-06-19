#!/bin/bash
set -e

# Handle user/group creation if running as root
if [ "$(id -u)" = "0" ]; then
    # Create group and user if they don't exist
    if ! getent group "${DOCKER_GROUP_ID:-1000}" >/dev/null 2>&1; then
        groupadd -g "${DOCKER_GROUP_ID:-1000}" testuser
    fi
    
    if ! id -u testuser >/dev/null 2>&1; then
        useradd -u "${DOCKER_USER_ID:-1000}" -g "${DOCKER_GROUP_ID:-1000}" -m -s /bin/bash testuser
    fi
    
    # Fix permissions
    chown -R "${DOCKER_USER_ID:-1000}:${DOCKER_GROUP_ID:-1000}" /app/.gradle /app/build || true
    
    # Switch to the test user
    exec gosu testuser "$@"
else
    # Already running as non-root user
    exec "$@"
fi