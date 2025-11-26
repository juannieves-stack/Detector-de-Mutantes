#!/usr/bin/env bash
# Build script for Render native deployment

set -e

echo "🔨 Building Mutant Detector API..."

# Give execute permissions to gradlew
chmod +x ./gradlew

# Clean and build
./gradlew clean build -x test

echo "✅ Build completed successfully!"
