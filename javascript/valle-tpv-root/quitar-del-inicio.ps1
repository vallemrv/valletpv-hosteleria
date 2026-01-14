# ==================================================
#   Valle TPV - Quitar del Inicio de Windows
# ==================================================
#   Este script elimina el acceso directo de la
#   carpeta de inicio para que Valle TPV no se
#   ejecute automáticamente al arrancar el sistema
# ==================================================

Write-Host "Quitando Valle TPV del inicio automatico..." -ForegroundColor Cyan
Write-Host ""

# Obtener la carpeta de inicio de Windows
$startupFolder = [System.Environment]::GetFolderPath('Startup')
$shortcutPath = Join-Path $startupFolder "Valle TPV.lnk"

# Verificar si existe el acceso directo
if (Test-Path $shortcutPath) {
    Remove-Item $shortcutPath -Force
    Write-Host "========================================" -ForegroundColor Green
    Write-Host " Acceso directo eliminado!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Valle TPV ya no se iniciara automaticamente." -ForegroundColor White
} else {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host " No se encontro el acceso directo" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Valle TPV no estaba configurado en el inicio." -ForegroundColor White
}

Write-Host ""
pause
