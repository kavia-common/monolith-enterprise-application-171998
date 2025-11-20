#!/bin/bash
set -euo pipefail

JAR_PATH="target/Snowman.jar"

# If the executable JAR is missing, try to build it if Maven is available.
if [ ! -f "$JAR_PATH" ]; then
  echo "Executable jar not found at $JAR_PATH"
  if command -v mvn >/dev/null 2>&1; then
    echo "Attempting to build the project with Maven..."
    mvn -DskipTests package
  else
    echo "Maven (mvn) is not installed or not on PATH."
    echo "Please install Maven in this environment and run: mvn -DskipTests package"
    echo "Once built, rerun this script to start the application."
    exit 1
  fi
fi

# After build attempt, verify the jar exists
if [ ! -f "$JAR_PATH" ]; then
  echo "Build did not produce $JAR_PATH. Please check build logs and pom.xml configuration."
  exit 1
fi

exec java -jar "$JAR_PATH"
