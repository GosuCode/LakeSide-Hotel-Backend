#!/bin/bash

# Data Seeding Script for LakeSide Hotel Demo
# This script clears existing data and seeds fresh sample data

echo "LakeSide Hotel Data Seeding Script"
echo "=================================="
echo "Clearing existing data and seeding fresh sample data..."

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Run the application with seeding enabled
mvn spring-boot:run -Dseed.data=true
