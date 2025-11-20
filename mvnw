#!/bin/sh
# ----------------------------------------------------------------------------
<<<<<<< HEAD
# Maven Wrapper Startup Script
# ----------------------------------------------------------------------------

# Resolve the location of the script, in case it is a symlink
=======
# Maven Wrapper startup script for UN*X
# This script is generated based on Maven Wrapper 3.2.0, adapted to this project.
# Purpose: Ensure builds use the project's Maven wrapper instead of system Maven.
# ----------------------------------------------------------------------------

##############################################################################
# Resolve real path (in case of symlinks)
##############################################################################
>>>>>>> cga-cm42bfd598
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
<<<<<<< HEAD
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
=======
# make it absolute
BASEDIR=`cd "$BASEDIR" >/dev/null 2>&1 && pwd`

JAVA_EXE="java"
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    # IBM's JDK on AIX uses strange locations for the executables
    JAVA_EXE="$JAVA_HOME/jre/sh/java"
  else
    JAVA_EXE="$JAVA_HOME/bin/java"
  fi
fi

# Verify Java is present
if [ ! -x "$(command -v "$JAVA_EXE")" ]; then
  echo "ERROR: Java executable not found. Please set JAVA_HOME or ensure java is in PATH." >&2
  exit 1
fi

WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Download the maven-wrapper.jar if it doesn't exist
if [ ! -f "$WRAPPER_JAR" ]; then
  # shellcheck disable=SC2039
  if command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  elif command -v curl >/dev/null 2>&1; then
    curl -s -L -o "$WRAPPER_JAR" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  else
    echo "ERROR: To run Maven Wrapper, you need 'curl' or 'wget' to download $WRAPPER_JAR" >&2
>>>>>>> cga-cm42bfd598
    exit 1
  fi
fi

<<<<<<< HEAD
# Execute maven wrapper
exec "$JAVA_EXE" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  -cp "$WRAPPER_JAR" \
=======
# Download the Maven distribution referenced in properties if missing (handled by wrapper itself)
# Respect MAVEN_OPTS if set
MAVEN_OPTS="${MAVEN_OPTS:-}"

# Execute Maven Wrapper
exec "$JAVA_EXE" $MAVEN_OPTS -classpath "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$BASEDIR" \
>>>>>>> cga-cm42bfd598
  org.apache.maven.wrapper.MavenWrapperMain "$@"
