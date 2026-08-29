@echo off
rem One-click: write every KEY=VALUE in chunk-upload-demo\.env into USER environment
rem variables (registry HKCU\Environment, persistent, no admin needed).
rem Re-run anytime to update values. See uninstall-env.cmd to remove them.
setlocal
set "MODULE_DIR=%~dp0"

if not exist "%MODULE_DIR%.env" (
  echo [ERROR] %MODULE_DIR%.env not found.
  echo Run first:  copy chunk-upload-demo\.env.example chunk-upload-demo\.env
  pause
  exit /b 1
)

echo Writing variables from .env into USER environment variables:
for /f "usebackq eol=# delims== tokens=1,*" %%a in ("%MODULE_DIR%.env") do (
  setx %%a "%%b" >nul
  if errorlevel 1 (
    echo   [FAIL] %%a
  ) else (
    echo   [OK]   %%a
  )
)

echo.
echo Done. NOTE: windows that are ALREADY open ^(including IDEA^) will NOT see
echo these until restarted. New terminals will see them immediately.
pause
