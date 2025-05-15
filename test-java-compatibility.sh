#!/bin/bash
set -e

# Make script executable
chmod +x gradlew

echo "=== Testing with Java 8 ==="
docker-compose -f docker-compose.test.yml run --rm java8

echo "=== Testing with Java 11 ==="
docker-compose -f docker-compose.test.yml run --rm java11

echo "=== Testing with Java 17 ==="
docker-compose -f docker-compose.test.yml run --rm java17

echo "All compatibility tests passed!"