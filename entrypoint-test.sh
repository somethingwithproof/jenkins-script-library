#!/bin/bash
set -e

# Handle user/group creation if running as root
if [ "$(id -u)" = "0" ]; then
    target_uid="${DOCKER_USER_ID:-1000}"
    target_gid="${DOCKER_GROUP_ID:-1000}"

    # Reuse identities already supplied by the base image, otherwise create
    # matching ones for bind-mounted files from the host.
    target_group="$(getent group "$target_gid" | cut -d: -f1 || true)"
    if [ -z "$target_group" ]; then
        target_group=testuser
        groupadd -g "$target_gid" "$target_group"
    fi

    target_user="$(getent passwd "$target_uid" | cut -d: -f1 || true)"
    if [ -z "$target_user" ]; then
        target_user=testuser
        useradd -u "$target_uid" -g "$target_group" -m -s /bin/bash "$target_user"
    fi

    # Fix permissions
    chown -R "$target_uid:$target_gid" /app/.gradle /app/build || true

    # Switch to the test user
    exec gosu "$target_user" "$@"
else
    # Already running as non-root user
    exec "$@"
fi
