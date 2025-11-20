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
    )
  )
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
