<<<<<<< HEAD
@ECHO OFF
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Startup Script for Windows
@REM ----------------------------------------------------------------------------

setlocal

set "BASEDIR=%~dp0"
if "%MAVEN_BASEDIR%"=="" (
  set "MAVEN_PROJECTBASEDIR=%BASEDIR%"
) else (
  set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
)

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)

if not exist "%JAVA_EXE%" (
  echo ERROR: Java executable not found. Please set JAVA_HOME or ensure java is on PATH. 1>&2
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  if not exist "%WRAPPER_PROPERTIES%" (
    echo ERROR: %WRAPPER_PROPERTIES% not found 1>&2
    exit /b 1
  )
  for /f "usebackq tokens=1* delims==" %%A in ("%WRAPPER_PROPERTIES%") do (
    if /I "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
  )
  if "%WRAPPER_URL%"=="" (
    echo ERROR: wrapperUrl not defined in %WRAPPER_PROPERTIES% 1>&2
    exit /b 1
  )
  echo Downloading Maven Wrapper jar from: %WRAPPER_URL%
  if exist "%ProgramFiles%\Git\usr\bin\curl.exe" (
    "%ProgramFiles%\Git\usr\bin\curl.exe" -fsSL -o "%WRAPPER_JAR%" "%WRAPPER_URL%"
  ) else if exist "%ProgramFiles%\Git\mingw64\bin\curl.exe" (
    "%ProgramFiles%\Git\mingw64\bin\curl.exe" -fsSL -o "%WRAPPER_JAR%" "%WRAPPER_URL%"
  ) else if exist "%ProgramFiles%\Git\mingw64\bin\wget.exe" (
    "%ProgramFiles%\Git\mingw64\bin\wget.exe" -q -O "%WRAPPER_JAR%" "%WRAPPER_URL%"
  ) else (
    where curl >nul 2>nul && curl -fsSL -o "%WRAPPER_JAR%" "%WRAPPER_URL%" || (
      where wget >nul 2>nul && wget -q -O "%WRAPPER_JAR%" "%WRAPPER_URL%" || (
        echo ERROR: curl or wget is required to download the Maven Wrapper jar. 1>&2
        exit /b 1
      )
=======
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
>>>>>>> cga-cm42bfd598
    )
  )
)

<<<<<<< HEAD
"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
=======
set MAVEN_OPTS=%MAVEN_OPTS%

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%WRAPPER_BASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
>>>>>>> cga-cm42bfd598
