#!/bin/bash
set -euo pipefail

# Build artifact name produced by maven-shade-plugin (verified via build)
# Available artifacts after build:
# - target/Snowman.jar (runnable uber JAR)
# - target/original-Snowman.jar (original before shading)
# - target/enterprise-application-1.0-SNAPSHOT.jar (non-uber)
# Usage: ./run.sh
# Optional: PORT=3001 ./run.sh  (maps to -Dport=3001)
JAR_PATH="target/Snowman.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Error: Expected runnable JAR not found at $JAR_PATH"
  echo "Hint: Build the project: './mvnw -DskipTests package'"
  echo "If build succeeded but name differs, available files in target/:"
  ls -1 target || true
  exit 1
fi

# Allow overriding port via PORT env var for convenience
PORT_ARG=""
if [[ "${PORT:-}" != "" ]]; then
  PORT_ARG="-Dport=${PORT}"
fi

# Allow extra JVM options via JAVA_OPTS
exec java ${JAVA_OPTS:-} ${PORT_ARG} -jar "$JAR_PATH"
