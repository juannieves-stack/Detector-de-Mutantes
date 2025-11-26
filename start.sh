#!/usr/bin/env bash
# Start script for Render native deployment

set -e

echo "🚀 Starting Mutant Detector API..."

# Find the JAR file
JAR_FILE=$(find build/libs -name "*.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found!"
    exit 1
fi

echo "📦 Found JAR: $JAR_FILE"

# Start the application
java -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    --server.port=${PORT:-8080}
