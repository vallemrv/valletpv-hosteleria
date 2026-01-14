# ==================================================
#   Valle TPV - Agregar al Inicio de Windows
# ==================================================
#   Este script crea un acceso directo en la carpeta
#   de inicio de Windows para que Valle TPV se
#   ejecute automáticamente al arrancar el sistema
# ==================================================

Write-Host "Configurando Valle TPV para inicio automatico..." -ForegroundColor Cyan
Write-Host ""

# Obtener la ruta del archivo VBS
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$vbsFile = Join-Path $scriptPath "INICIAR-SILENCIOSO.vbs"

# Verificar que el archivo existe
if (-not (Test-Path $vbsFile)) {
    Write-Host "ERROR: No se encontro INICIAR-SILENCIOSO.vbs" -ForegroundColor Red
    Write-Host "Ruta esperada: $vbsFile" -ForegroundColor Yellow
    pause
    exit 1
}

# Obtener la carpeta de inicio de Windows
$startupFolder = [System.Environment]::GetFolderPath('Startup')
Write-Host "Carpeta de inicio: $startupFolder" -ForegroundColor Gray
Write-Host ""

# Crear un acceso directo
$shortcutPath = Join-Path $startupFolder "Valle TPV.lnk"
$WScriptShell = New-Object -ComObject WScript.Shell
$shortcut = $WScriptShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $vbsFile
$shortcut.WorkingDirectory = $scriptPath
$shortcut.Description = "Valle TPV - Sistema de Punto de Venta"
$shortcut.Save()

Write-Host "========================================" -ForegroundColor Green
Write-Host " Valle TPV configurado correctamente!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "La aplicacion se iniciara automaticamente cuando arranque Windows." -ForegroundColor White
Write-Host ""
Write-Host "Ubicacion del acceso directo:" -ForegroundColor Yellow
Write-Host $shortcutPath -ForegroundColor Gray
Write-Host ""
Write-Host "Para eliminar del inicio, simplemente borra este acceso directo." -ForegroundColor Yellow
Write-Host ""
pause
