@echo off
rem One-click: remove the variables defined in chunk-upload-demo\.env from USER
rem environment variables (setx cannot delete, so we delete the registry values).
setlocal
set "MODULE_DIR=%~dp0"

if not exist "%MODULE_DIR%.env" (
  echo [ERROR] %MODULE_DIR%.env not found ^(it is the key list to remove^).
  pause
  exit /b 1
)

echo Removing variables defined in .env from USER environment:
for /f "usebackq eol=# delims== tokens=1,*" %%a in ("%MODULE_DIR%.env") do (
  reg delete "HKCU\Environment" /v %%a /f >nul 2>&1
  echo   [OK] %%a
)

echo.
echo Done. Restart open terminals / IDEA to fully forget them.
pause
