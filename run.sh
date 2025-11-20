#!/usr/bin/env bash
set -euo pipefail

JAR_PATH="target/Snowman.jar"

# If the executable JAR is missing, build it using Maven Wrapper if available.
if [ ! -f "$JAR_PATH" ]; then
  echo "Executable jar not found at $JAR_PATH"
  if [ -x "./mvnw" ]; then
    echo "Attempting to build the project with Maven Wrapper..."
    ./mvnw -DskipTests package
  elif command -v mvn >/dev/null 2>&1; then
    echo "Maven Wrapper not found. Attempting to build the project with system Maven..."
    mvn -DskipTests package
  else
    echo "ERROR: Neither Maven Wrapper (./mvnw) nor system Maven (mvn) is available."
    echo "Please ensure the Maven Wrapper is executable or install Maven, then run: ./mvnw -DskipTests package"
    exit 1
  fi
fi

# After build attempt, verify the jar exists
if [ ! -f "$JAR_PATH" ]; then
  echo "ERROR: Build did not produce $JAR_PATH. Please check build logs and pom.xml configuration." >&2
  exit 1
fi

# Determine port: prefer CLI arg, then PORT env, else default 3001 for preview
PORT_ARG="${1:-}"
if [[ -n "$PORT_ARG" ]]; then
  PORT="$PORT_ARG"
else
  PORT="${PORT:-3001}"
fi

echo "Starting Snowman on port ${PORT}..."
# Use -Dserver.port for Spring/Jetty and keep legacy -Dport for any custom reads.
exec java -Dport="${PORT}" -Dserver.port="${PORT}" -jar "$JAR_PATH"
