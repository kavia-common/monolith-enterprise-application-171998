#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper startup script for UN*X
# This script is generated based on Maven Wrapper 3.2.0, adapted to this project.
# Purpose: Ensure builds use the project's Maven wrapper instead of system Maven.
# ----------------------------------------------------------------------------

##############################################################################
# Resolve real path (in case of symlinks)
##############################################################################
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
    exit 1
  fi
fi

# Download the Maven distribution referenced in properties if missing (handled by wrapper itself)
# Respect MAVEN_OPTS if set
MAVEN_OPTS="${MAVEN_OPTS:-}"

# Execute Maven Wrapper
exec "$JAVA_EXE" $MAVEN_OPTS -classpath "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$BASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
