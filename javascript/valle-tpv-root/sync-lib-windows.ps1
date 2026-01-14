# Script para sincronizar la libreria valle-tpv-lib a todas las aplicaciones
# Uso: .\sync-lib-windows.ps1

$ErrorActionPreference = "Stop"

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $SCRIPT_DIR

Write-Host "Sincronizando libreria valle-tpv-lib..." -ForegroundColor Cyan

$APPS = @("valleTPV")

if (-not (Test-Path "valle-tpv-lib\src")) {
    Write-Host "Error: No se encuentra valle-tpv-lib\src" -ForegroundColor Red
    exit 1
}

foreach ($app in $APPS) {
    if (Test-Path $app) {
        Write-Host "  Sincronizando a $app..." -ForegroundColor Yellow
        
        $libPath = Join-Path $app "src\lib"
        if (-not (Test-Path $libPath)) {
            New-Item -ItemType Directory -Path $libPath -Force | Out-Null
        }
        
        if (Test-Path $libPath) {
            Get-ChildItem -Path $libPath -Recurse | Remove-Item -Force -Recurse
        }
        Copy-Item -Path "valle-tpv-lib\src\*" -Destination $libPath -Recurse -Force
        
        $buildSwScript = Join-Path $app "scripts\build-sw.js"
        if (Test-Path $buildSwScript) {
            Write-Host "  Compilando Service Worker..." -ForegroundColor Yellow
            Push-Location $app
            node scripts/build-sw.js
            Pop-Location
        }
        
        Write-Host "  $app sincronizado" -ForegroundColor Green
    }
    else {
        Write-Host "  Advertencia: No existe la carpeta $app" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Sincronizacion completada!" -ForegroundColor Green
