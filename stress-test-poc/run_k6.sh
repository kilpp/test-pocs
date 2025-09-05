#!/bin/bash

cd "$(dirname "$0")"

# Start postgres
echo "Starting postgres..."
docker-compose up -d postgres

# Start the spring boot application
echo "Starting spring boot application..."
./gradlew bootRun &
BOOT_PID=$!

# Wait for the application to start
echo "Waiting for application to start..."
sleep 30

# Run k6 tests
echo "Running k6 tests..."
k6 run k6-runner/src/main/k6/stress-test.js

# Stop the spring boot application
echo "Stopping spring boot application..."
kill $BOOT_PID

# Stop docker
echo "Stopping docker..."
docker-compose down
