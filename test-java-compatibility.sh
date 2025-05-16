#!/bin/bash
set -e

# Make script executable
chmod +x gradlew

echo "=== Testing with Java 11 (Legacy Support) ==="
docker-compose -f docker-compose.test.yml run --rm java11

echo "=== Testing with Java 17 (Current LTS) ==="
docker-compose -f docker-compose.test.yml run --rm java17

echo "=== Testing with Java 21 (Future Compatibility) ==="
docker-compose -f docker-compose.test.yml run --rm java21

echo "All compatibility tests passed!"