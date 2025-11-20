@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM Generated based on Maven Wrapper 3.2.0
@REM ----------------------------------------------------------------------------
@ECHO OFF
SETLOCAL

set WRAPPER_BASEDIR=%~dp0

set JAVA_EXE=java
IF NOT "%JAVA_HOME%"=="" (
  set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
)

where %JAVA_EXE% >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO ERROR: Java executable not found. Please set JAVA_HOME or ensure java is in PATH. 1>&2
  EXIT /B 1
)

set WRAPPER_JAR=%WRAPPER_BASEDIR%.mvn\wrapper\maven-wrapper.jar

IF NOT EXIST "%WRAPPER_JAR%" (
  where curl >NUL 2>&1
  IF %ERRORLEVEL%==0 (
    curl -s -L -o "%WRAPPER_JAR%" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  ) ELSE (
    where wget >NUL 2>&1
    IF %ERRORLEVEL%==0 (
      wget -q -O "%WRAPPER_JAR%" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    ) ELSE (
      ECHO ERROR: To run Maven Wrapper, you need 'curl' or 'wget' to download %WRAPPER_JAR% 1>&2
      EXIT /B 1
    )
  )
)

set MAVEN_OPTS=%MAVEN_OPTS%

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%WRAPPER_BASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
