#!/usr/bin/env bash
set -euo pipefail

JAR_PATH="target/Snowman.jar"

echo "Listing target directory (if exists) before start:"
ls -la target || true

# Guard: If the executable JAR is missing, build it first with Maven Wrapper (preferred) or system Maven.
if [ ! -f "$JAR_PATH" ]; then
  echo "Executable jar not found at $JAR_PATH"
  MVN_CMD="./mvnw"; [ -x "./mvnw" ] || MVN_CMD="$(command -v mvn || true)"
  if [ -n "${MVN_CMD}" ]; then
    echo "Attempting to build the project with Maven (${MVN_CMD})..."
    if [ "${SKIP_TESTS:-1}" = "1" ]; then
      "${MVN_CMD}" -q -DskipTests package --batch-mode --errors --fail-at-end
    else
      "${MVN_CMD}" -q package --batch-mode --errors --fail-at-end
    fi
  else
    echo "ERROR: Neither Maven Wrapper (./mvnw) nor system Maven (mvn) is available."
    echo "Please ensure the Maven Wrapper is executable or install Maven, then run: ./mvnw -DskipTests package --batch-mode --errors --fail-at-end"
    exit 1
  fi
fi

echo "Listing target directory after build attempt:"
ls -la target || true

# After build attempt, verify the jar exists; if not, try to discover the actual jar and use it.
if [ ! -f "$JAR_PATH" ]; then
  echo "WARNING: Expected $JAR_PATH not found. Discovering built jars in target/..."
  ALT_JAR="$(ls -1 target/*.jar 2>/dev/null | head -n1 || true)"
  if [ -n "${ALT_JAR}" ]; then
    echo "Using discovered jar: ${ALT_JAR}"
    JAR_PATH="${ALT_JAR}"
  else
    echo "ERROR: Build did not produce any jar in target/. Please check build logs and pom.xml configuration." >&2
    exit 1
  fi
fi

# Determine port: prefer CLI arg, then PORT env, else default 3001 for preview
PORT_ARG="${1:-}"
if [[ -n "$PORT_ARG" ]]; then
  PORT="$PORT_ARG"
else
  PORT="${PORT:-3001}"
fi

echo "Starting Snowman from '${JAR_PATH}' on port ${PORT}..."
# Use -Dserver.port for Spring/Jetty and keep legacy -Dport for any custom reads.
exec java -Dport="${PORT}" -Dserver.port="${PORT}" -jar "${JAR_PATH}"
