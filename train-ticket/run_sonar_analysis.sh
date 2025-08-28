#!/bin/bash

echo "Setting up SonarQube analysis for Train-Ticket microservices..."

# Check if SonarQube is running
if ! curl -s http://localhost:9000/api/system/status | grep -q "UP"; then
    echo "Error: SonarQube is not running or not accessible at http://localhost:9000"
    echo "Please ensure SonarQube Docker container is running."
    exit 1
fi

echo "SonarQube is running. Starting analysis..."

# Create a simple analysis without authentication for default setup
echo "Running SonarQube analysis..."

# Try with default credentials first
sonar-scanner \
    -Dsonar.projectKey=train-ticket \
    -Dsonar.projectName="Train Ticket Microservices" \
    -Dsonar.projectVersion=1.0 \
    -Dsonar.host.url=http://localhost:9000 \
    -Dsonar.login=admin \
    -Dsonar.password=admin \
    -Dsonar.sourceEncoding=UTF-8

# If that fails, try without authentication (for fresh SonarQube instances)
if [ $? -ne 0 ]; then
    echo "Trying without authentication..."
    sonar-scanner \
        -Dsonar.projectKey=train-ticket \
        -Dsonar.projectName="Train Ticket Microservices" \
        -Dsonar.projectVersion=1.0 \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.sourceEncoding=UTF-8
fi

echo "Analysis complete! Check results at: http://localhost:9000/dashboard?id=train-ticket"