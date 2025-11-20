#!/bin/bash
set -euo pipefail

# Build artifact name produced by maven-shade-plugin (verified via build)
# Available artifacts after build:
# - target/Snowman.jar (runnable uber JAR)
# - target/original-Snowman.jar (original before shading)
# - target/enterprise-application-1.0-SNAPSHOT.jar (non-uber)
JAR_PATH="target/Snowman.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Error: Expected runnable JAR not found at $JAR_PATH"
  echo "Hint: Build the project: './mvnw -DskipTests package'"
  echo "If build succeeded but name differs, available files in target/:"
  ls -1 target || true
  exit 1
fi

exec java -jar "$JAR_PATH"
