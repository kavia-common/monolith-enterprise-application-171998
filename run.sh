#!/bin/bash
set -euo pipefail

# Start script for Snowman backend

# Name of target jar
JAR_PATH="target/Snowman.jar"

# 1. If JAR is missing, automatically build it with tests skipped
if [ ! -f "$JAR_PATH" ]; then
  echo "Artifact $JAR_PATH not found. Attempting build (skip tests)."
  if [ -x "./mvnw" ]; then
    ./mvnw -B -DskipTests=true clean package
  elif command -v mvn >/dev/null 2>&1; then
    mvn -B -DskipTests=true clean package
  else
    echo "ERROR: Maven Wrapper (./mvnw) or Maven (mvn) not found. Please install Maven." >&2
    exit 1
  fi
fi

# 2. If still missing, show available jars and error out
if [ ! -f "$JAR_PATH" ]; then
  echo "ERROR: Expected runnable JAR not found at $JAR_PATH"
  echo "Available files in target/:"
  ls -1 target || true
  exit 1
fi

# 3. Allow overriding port via env or arg, else default 3001
if [[ $# -gt 0 ]]; then
  PORT="$1"
elif [[ -n "${PORT:-}" ]]; then
  PORT="${PORT}"
else
  PORT="3001"
fi

echo "Starting Snowman JAR on port ${PORT} ..."
exec java -Dport="${PORT}" -Dserver.port="${PORT}" -jar "$JAR_PATH"
