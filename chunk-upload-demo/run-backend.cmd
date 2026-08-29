@echo off
rem Start chunk-upload-demo backend (CMD version of run-backend.sh)
rem Sensitive config lives in chunk-upload-demo\.env (copy from .env.example)
setlocal
set "MODULE_DIR=%~dp0"
set "ROOT_DIR=%MODULE_DIR%.."

if not exist "%MODULE_DIR%.env" (
  echo [ERROR] %MODULE_DIR%.env not found.
  echo Run first:  copy chunk-upload-demo\.env.example chunk-upload-demo\.env
  echo Then fill in real values and re-run this script.
  pause
  exit /b 1
)

rem Parse .env line by line: skip '#' comments and blank lines, export KEY=VALUE
for /f "usebackq eol=# delims== tokens=1,*" %%a in ("%MODULE_DIR%.env") do set "%%a=%%b"

rem Override JAVA_HOME to JDK 17 (default points to jdk8 on this machine)
set "JAVA_HOME=D:\dev\jdks\jdk17\jdk-17.0.12\jdk-17.0.12"

call mvn -f "%ROOT_DIR%\pom.xml" spring-boot:run -pl chunk-upload-demo

rem Keep the window open so build/startup errors stay visible when double-clicked
pause
