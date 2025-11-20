#!/bin/bash
set -euo pipefail

JAR_PATH="target/Snowman.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Executable jar not found at $JAR_PATH"
  echo "Build the project first with: mvn -DskipTests package"
  exit 1
fi

exec java -jar "$JAR_PATH"
