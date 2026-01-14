# Script para sincronizar la libreria valle-tpv-lib a todas las aplicaciones
# Uso: .\sync-lib.ps1

$ErrorActionPreference = "Stop"

$SCRIPT_DIR = $PSScriptRoot
Set-Location $SCRIPT_DIR

Write-Host "Sincronizando libreria valle-tpv-lib..." -ForegroundColor Cyan

# Lista de aplicaciones que usan la libreria
$APPS = @("valleTPV")

# Verificar que existe la libreria
if (-Not (Test-Path "valle-tpv-lib\src")) {
    Write-Host "Error: No se encuentra valle-tpv-lib\src" -ForegroundColor Red
    exit 1
}

# Sincronizar a cada aplicacion
foreach ($app in $APPS) {
    if (Test-Path $app) {
        Write-Host "  Sincronizando a $app..." -ForegroundColor Yellow
        
        # Crear directorio lib si no existe
        $libPath = "$app\src\lib"
        if (-Not (Test-Path $libPath)) {
            New-Item -ItemType Directory -Path $libPath -Force | Out-Null
        }
        
        # Limpiar y copiar
        if (Test-Path "$libPath\*") {
            Remove-Item -Path "$libPath\*" -Recurse -Force
        }
        Copy-Item -Path "valle-tpv-lib\src\*" -Destination $libPath -Recurse -Force
        
        # Compilar el Service Worker
        if (Test-Path "$app\scripts\build-sw.js") {
            Write-Host "  Compilando Service Worker..." -ForegroundColor Yellow
            Push-Location $app
            node scripts\build-sw.js
            Pop-Location
        }
        
        Write-Host "  $app sincronizado" -ForegroundColor Green
    } else {
        Write-Host "  Advertencia: No existe la carpeta $app" -ForegroundColor Yellow
    }
}

Write-Host "Sincronizacion completada"

