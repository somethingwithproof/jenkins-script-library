#!/bin/bash
# SPDX-FileCopyrightText: 2025-2026 Thomas Vincent <thomasvincent@gmail.com>
# SPDX-License-Identifier: MIT

set -e

# Make script executable
chmod +x gradlew

echo "=== Testing with Java 11 (Legacy Support) ==="
docker compose -f docker-compose.test.yml run --rm java11

echo "=== Testing with Java 24 ==="
docker compose -f docker-compose.test.yml run --rm java24

echo "=== Testing with Java 21 ==="
docker compose -f docker-compose.test.yml run --rm java21

echo "All compatibility tests passed!"
