@echo off
REM ==================================================
REM   Valle TPV - Iniciador Principal
REM ==================================================
REM   Este script inicia la aplicación en modo Electron
REM ==================================================

echo Iniciando Valle TPV con Electron...
echo.

REM Ir a la carpeta electron-kiosk
cd /d "%~dp0electron-kiosk"

REM Verificar si Electron está instalado
if not exist "node_modules\electron\dist\electron.exe" (
    echo ERROR: Electron no esta instalado
    echo Ejecutando: npm install
    call npm install
    echo.
)

REM Iniciar Electron en modo kiosk
echo Abriendo aplicacion...
start "" "node_modules\electron\dist\electron.exe" .

REM Opcional: descomentar la siguiente linea para ver errores
REM pause

