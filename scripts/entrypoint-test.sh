#!/bin/bash
set -e

# Default to UID/GID 1000 if not specified
USER_ID=${DOCKER_USER_ID:-1000}
GROUP_ID=${DOCKER_GROUP_ID:-1000}

echo "Starting with UID: $USER_ID, GID: $GROUP_ID"

# Create the user and group
groupadd -g $GROUP_ID testuser || echo "Group $GROUP_ID already exists"
useradd -u $USER_ID -g $GROUP_ID -m -s /bin/bash testuser || echo "User $USER_ID already exists"

# Make sure the needed directories exist and have correct permissions
mkdir -p /app/build /app/.gradle
chown -R $USER_ID:$GROUP_ID /app/build /app/.gradle

# Handle volume permissions for test reporting
if [ -d "/app/build/reports" ]; then
  chmod -R 777 /app/build/reports
fi

# Create test results directory if it doesn't exist
mkdir -p /app/build/test-results
chmod -R 777 /app/build/test-results

# If running as root but with DOCKER_USER_ID set, execute as the requested user
if [ "$(id -u)" = "0" ] && [ "$USER_ID" != "0" ]; then
  echo "Switching to user testuser (UID: $USER_ID)"
  exec gosu testuser "$@"
else
  # Execute command as the current user
  exec "$@"
fi

