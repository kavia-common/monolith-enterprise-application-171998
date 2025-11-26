#!/usr/bin/env bash
set -euo pipefail

# Preferred primary jar path
JAR_PRIMARY="target/Snowman.jar"
# Fallback: any shaded fat jar
JAR_SHADED=$(ls -1 target/*-shaded.jar 2>/dev/null | head -n1 || true)

echo "Listing target directory (if exists):"
ls -la target || true

# If neither exists yet, attempt a quick build with tests fully skipped
choose_jar() {
  if [ -f "$JAR_PRIMARY" ]; then
    echo "$JAR_PRIMARY"
    return 0
  fi
  if [ -n "${JAR_SHADED:-}" ] && [ -f "$JAR_SHADED" ]; then
    echo "$JAR_SHADED"
    return 0
  fi
  return 1
}

JAR_TO_RUN=""
if ! JAR_TO_RUN="$(choose_jar)"; then
  echo "No runnable JAR found. Attempting to build with Maven (tests skipped)..."
  MVN_CMD="./mvnw"; [ -x "./mvnw" ] || MVN_CMD="$(command -v mvn || true)"
  if [ -z "${MVN_CMD}" ]; then
    echo "ERROR: Maven not available. Please ensure ./mvnw is executable or mvn is installed."
    exit 1
  fi
  "${MVN_CMD}" -q clean package -Dmaven.test.skip=true -DskipTests -DskipITs --batch-mode --errors --fail-at-end
  echo "Re-listing target after build:"
  ls -la target || true
  # Re-evaluate candidates after build
  JAR_SHADED=$(ls -1 target/*-shaded.jar 2>/dev/null | head -n1 || true)
  if ! JAR_TO_RUN="$(choose_jar)"; then
    # As a last resort, pick the first jar in target
    ALT_JAR="$(ls -1 target/*.jar 2>/dev/null | head -n1 || true)"
    if [ -n "${ALT_JAR}" ]; then
      JAR_TO_RUN="${ALT_JAR}"
      echo "Falling back to discovered JAR: ${JAR_TO_RUN}"
    else
      echo "ERROR: Build did not produce any JAR in target/. Please inspect pom.xml and build logs."
      exit 1
    fi
  fi
fi

# Determine port: CLI arg > PORT env > default 3001
PORT_ARG="${1:-}"
if [[ -n "$PORT_ARG" ]]; then
  PORT="$PORT_ARG"
else
  PORT="${PORT:-3001}"
fi

echo "Starting Snowman using: ${JAR_TO_RUN} on host 0.0.0.0 port ${PORT}"
# Bind to all interfaces by default for preview system; allow override via SERVER_ADDRESS env if set.
SERVER_ADDRESS="${SERVER_ADDRESS:-0.0.0.0}"
# Use -Dserver.port for embedded server (highest precedence), also pass legacy -Dport; application falls back to PORT env or 3001 default.
exec java -jar -Dserver.address="${SERVER_ADDRESS}" -Dserver.port="${PORT}" -Dport="${PORT}" "${JAR_TO_RUN}"
