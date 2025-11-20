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

echo "Starting Snowman..."
exec java -jar "$JAR_PATH"
