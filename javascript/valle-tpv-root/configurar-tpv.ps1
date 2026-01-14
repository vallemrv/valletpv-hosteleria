# ==================================================
#   Valle TPV - Configurador Único
# ==================================================
#   Este script configura TODO lo necesario para el TPV:
#   - Arranque automático
#   - Auto-login de Windows (opcional)
#   - Configuración de energía
#   - Protector de pantalla
# ==================================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "       Valle TPV - Configuración Completa" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si se ejecuta como administrador
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "⚠️  ADVERTENCIA: Este script necesita permisos de administrador" -ForegroundColor Yellow
    Write-Host "   Algunas configuraciones pueden no aplicarse correctamente" -ForegroundColor Yellow
    Write-Host ""
    $continuar = Read-Host "¿Deseas continuar de todas formas? (S/N)"
    if ($continuar -ne "S" -and $continuar -ne "s") {
        Write-Host "Operación cancelada" -ForegroundColor Red
        exit 0
    }
    Write-Host ""
}

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path

# ==================================================
# 1. ARRANQUE AUTOMÁTICO
# ==================================================
Write-Host "📋 PASO 1: Configuración de Arranque Automático" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host ""

$batPath = Join-Path $scriptPath "INICIAR-TPV.bat"
$vbsPath = Join-Path $scriptPath "INICIAR-TPV-SILENCIOSO.vbs"
$startupFolder = [Environment]::GetFolderPath("Startup")
$shortcutPath = Join-Path $startupFolder "Valle TPV.lnk"

if (-not (Test-Path $batPath)) {
    Write-Host "❌ ERROR: No se encuentra INICIAR-TPV.bat" -ForegroundColor Red
    Write-Host "   Verifica que el archivo existe en: $scriptPath" -ForegroundColor Yellow
} else {
    Write-Host "✓ Archivo encontrado: INICIAR-TPV.bat" -ForegroundColor Green
}

if (-not (Test-Path $vbsPath)) {
    Write-Host "❌ ERROR: No se encuentra INICIAR-TPV-SILENCIOSO.vbs" -ForegroundColor Red
} else {
    Write-Host "✓ Archivo encontrado: INICIAR-TPV-SILENCIOSO.vbs" -ForegroundColor Green
}

Write-Host ""
Write-Host "¿Deseas configurar el arranque automático?" -ForegroundColor Yellow
Write-Host "1. Sí, inicio silencioso (sin consola) - RECOMENDADO" -ForegroundColor White
Write-Host "2. Sí, inicio visible (con consola para diagnóstico)" -ForegroundColor White
Write-Host "3. No configurar arranque automático" -ForegroundColor White
Write-Host ""
$opcionArranque = Read-Host "Selecciona una opción (1-3)"

if ($opcionArranque -eq "1" -or $opcionArranque -eq "2") {
    $targetPath = if ($opcionArranque -eq "1") { $vbsPath } else { $batPath }
    
    try {
        $WScriptShell = New-Object -ComObject WScript.Shell
        $Shortcut = $WScriptShell.CreateShortcut($shortcutPath)
        $Shortcut.TargetPath = $targetPath
        $Shortcut.WorkingDirectory = $scriptPath
        $Shortcut.WindowStyle = 1
        $Shortcut.Description = "Valle TPV - Punto de Venta"
        
        $iconPath = Join-Path $scriptPath "valleTPV\public\favicon.ico"
        if (Test-Path $iconPath) {
            $Shortcut.IconLocation = $iconPath
        }
        
        $Shortcut.Save()
        
        Write-Host ""
        Write-Host "✅ Arranque automático configurado correctamente" -ForegroundColor Green
        Write-Host "   Ubicación: $shortcutPath" -ForegroundColor Cyan
    } catch {
        Write-Host "❌ Error al crear acceso directo: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⊘ Arranque automático omitido" -ForegroundColor Yellow
}

# ==================================================
# 2. AUTO-LOGIN DE WINDOWS
# ==================================================
Write-Host ""
Write-Host "📋 PASO 2: Auto-Login de Windows" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host ""
Write-Host "⚠️  ADVERTENCIA DE SEGURIDAD:" -ForegroundColor Red
Write-Host "   El auto-login elimina la protección de contraseña al iniciar" -ForegroundColor Yellow
Write-Host "   ÚSALO SOLO en equipos TPV dedicados en ubicaciones seguras" -ForegroundColor Yellow
Write-Host ""
Write-Host "¿Deseas configurar el auto-login de Windows? (S/N)" -ForegroundColor Yellow
$configurarAutologin = Read-Host

if ($configurarAutologin -eq "S" -or $configurarAutologin -eq "s") {
    Write-Host ""
    Write-Host "Ingresa tu nombre de usuario de Windows:" -ForegroundColor Cyan
    $username = Read-Host
    Write-Host "Ingresa tu CONTRASEÑA (no el PIN):" -ForegroundColor Cyan
    $password = Read-Host -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
    $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    
    try {
        # Configurar AutoAdminLogon
        Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" -Name "AutoAdminLogon" -Value "1" -Force
        Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" -Name "DefaultUsername" -Value $username -Force
        Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" -Name "DefaultPassword" -Value $plainPassword -Force
        
        Write-Host ""
        Write-Host "✅ Auto-login configurado correctamente" -ForegroundColor Green
        Write-Host "   Usuario: $username" -ForegroundColor Cyan
    } catch {
        Write-Host "❌ Error al configurar auto-login: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   Puede que necesites ejecutar como administrador" -ForegroundColor Yellow
    }
} else {
    Write-Host "⊘ Auto-login omitido" -ForegroundColor Yellow
}

# ==================================================
# 3. CONFIGURACIÓN DE ENERGÍA
# ==================================================
Write-Host ""
Write-Host "📋 PASO 3: Configuración de Energía para TPV" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Configurando opciones de energía óptimas para un TPV..." -ForegroundColor Cyan

try {
    # Evitar que se apague la pantalla
    & powercfg /change monitor-timeout-ac 0
    Write-Host "✓ Pantalla: Nunca apagar" -ForegroundColor Green
    
    # Evitar suspensión
    & powercfg /change standby-timeout-ac 0
    Write-Host "✓ Suspensión: Desactivada" -ForegroundColor Green
    
    # Evitar que se apaguen los discos
    & powercfg /change disk-timeout-ac 0
    Write-Host "✓ Discos duros: Siempre activos" -ForegroundColor Green
    
    # Plan de alto rendimiento
    & powercfg /setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c
    Write-Host "✓ Plan de energía: Alto rendimiento" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Advertencia: Algunas configuraciones de energía no se pudieron aplicar" -ForegroundColor Yellow
}

# ==================================================
# 4. PROTECTOR DE PANTALLA
# ==================================================
Write-Host ""
Write-Host "📋 PASO 4: Deshabilitar Protector de Pantalla" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
Write-Host ""

try {
    Set-ItemProperty -Path "HKCU:\Control Panel\Desktop" -Name "ScreenSaveActive" -Value "0" -Force
    Write-Host "✓ Protector de pantalla deshabilitado" -ForegroundColor Green
} catch {
    Write-Host "⚠️  No se pudo deshabilitar el protector de pantalla" -ForegroundColor Yellow
}

# ==================================================
# RESUMEN FINAL
# ==================================================
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "       ✅ Configuración Completada" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Resumen de cambios aplicados:" -ForegroundColor White
Write-Host ""
if ($opcionArranque -eq "1" -or $opcionArranque -eq "2") {
    Write-Host "  ✅ Arranque automático configurado" -ForegroundColor Green
}
if ($configurarAutologin -eq "S" -or $configurarAutologin -eq "s") {
    Write-Host "  ✅ Auto-login de Windows activado" -ForegroundColor Green
}
Write-Host "  ✅ Configuración de energía optimizada" -ForegroundColor Green
Write-Host "  ✅ Protector de pantalla deshabilitado" -ForegroundColor Green
Write-Host ""
Write-Host "💡 IMPORTANTE:" -ForegroundColor Yellow
Write-Host "   • Reinicia el equipo para que todos los cambios surtan efecto" -ForegroundColor White
Write-Host "   • La aplicación se iniciará automáticamente después del reinicio" -ForegroundColor White
Write-Host "   • Usa el botón 'Salir' en el menú para cerrar la aplicación" -ForegroundColor White
Write-Host "   • Usa el botón 'Apagar ordenador' para apagar el sistema" -ForegroundColor White
Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
