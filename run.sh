#!/bin/bash
set -euo pipefail

# Build artifact name produced by maven-shade-plugin
JAR_PATH="target/Snowman.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Error: Expected runnable JAR not found at $JAR_PATH"
  echo "Hint: Run './mvnw -DskipTests package' to build it."
  exit 1
fi

exec java -jar "$JAR_PATH"
