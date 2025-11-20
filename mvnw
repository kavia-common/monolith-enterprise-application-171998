#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Startup Script
# ----------------------------------------------------------------------------

# Resolve the location of the script, in case it is a symlink
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
BASEDIR=`dirname "$PRG"`
APP_BASEDIR=`cd "$BASEDIR"; pwd`

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$APP_BASEDIR"}

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Java executable
if [ -n "$JAVA_HOME" ] ; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi

if [ ! -x "$JAVA_EXE" ] ; then
  echo "ERROR: Java executable not found. Please set JAVA_HOME or ensure java is on PATH." >&2
  exit 1
fi

# Download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$WRAPPER_PROPERTIES" ]; then
    echo "ERROR: $WRAPPER_PROPERTIES not found" >&2
    exit 1
  fi
  DIST_URL=`sed -n 's/^wrapperUrl=//p' "$WRAPPER_PROPERTIES"`
  if [ -z "$DIST_URL" ]; then
    echo "ERROR: wrapperUrl not defined in $WRAPPER_PROPERTIES" >&2
    exit 1
  fi
  echo "Downloading Maven Wrapper jar from: $DIST_URL"
  jarDir=`dirname "$WRAPPER_JAR"`
  mkdir -p "$jarDir"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$DIST_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$DIST_URL"
  else
    echo "ERROR: curl or wget is required to download the Maven Wrapper jar." >&2
    exit 1
  fi
fi

# Execute maven wrapper
exec "$JAVA_EXE" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  -cp "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
