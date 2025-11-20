#!/usr/bin/env bash
set -euo pipefail

JAR_PATH="target/Snowman.jar"

# If the executable JAR is missing, build it using Maven Wrapper if available.
# We only need 'package' to produce the fat jar; tests are skipped in preview for speed if SKIP_TESTS=1 is set by caller.
if [ ! -f "$JAR_PATH" ]; then
  echo "Executable jar not found at $JAR_PATH"
  MVN_CMD="./mvnw"; [ -x "./mvnw" ] || MVN_CMD="$(command -v mvn || true)"
  if [ -n "$MVN_CMD" ]; then
    echo "Attempting to build the project with Maven (${MVN_CMD})..."
    if [ "${SKIP_TESTS:-1}" = "1" ]; then
      "$MVN_CMD" -DskipTests package --batch-mode --errors --fail-at-end
    else
      "$MVN_CMD" package --batch-mode --errors --fail-at-end
    fi
  else
    echo "ERROR: Neither Maven Wrapper (./mvnw) nor system Maven (mvn) is available."
    echo "Please ensure the Maven Wrapper is executable or install Maven, then run: ./mvnw -DskipTests package --batch-mode --errors --fail-at-end"
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
