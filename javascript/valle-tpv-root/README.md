# 🖥️ Valle TPV - Sistema de Punto de Venta

## 🚀 Inicio Rápido

### Para iniciar el TPV manualmente:
Doble clic en: **`INICIAR-TPV.bat`**

### Para configurar el TPV (primera vez):
1. Clic derecho en **`configurar-tpv.ps1`**
2. Selecciona **"Ejecutar con PowerShell"** o **"Ejecutar como administrador"**
3. Sigue el asistente de configuración

---

## 📁 Archivos Principales

| Archivo | Descripción |
|---------|-------------|
| `INICIAR-TPV.bat` | Inicia la aplicación manualmente |
| `INICIAR-TPV-SILENCIOSO.vbs` | Inicia sin mostrar ventana de consola |
| `configurar-tpv.ps1` | Asistente de configuración completa |

---

## ⚙️ ¿Qué hace el configurador?

El script `configurar-tpv.ps1` te ayuda a:

1. **✅ Arranque Automático** - La aplicación se inicia al encender el PC
2. **✅ Auto-Login** - Windows inicia sin pedir contraseña (opcional)
3. **✅ Configuración de Energía** - Evita que se apague la pantalla
4. **✅ Protector de Pantalla** - Lo deshabilita automáticamente

---

## 🎯 Uso Diario

### Iniciar la aplicación:
El TPV se inicia automáticamente si configuraste el arranque automático.
Si no, haz doble clic en `INICIAR-TPV.bat`

### Salir de la aplicación:
1. Clic en el menú (☰) arriba a la derecha
2. Selecciona **"Salir"**

O presiona la tecla **ESC**

### Apagar el ordenador:
1. Clic en el menú (☰) arriba a la derecha
2. Selecciona **"Apagar ordenador"**

---

## 🛠️ Desarrollo

### Compilar la aplicación:
```powershell
cd valleTPV
npm run build
```

### Sincronizar librería:
```powershell
.\sync-lib.ps1
```

---

## ❓ Solución de Problemas

### La aplicación no arranca
- Verifica que Electron esté instalado: `cd electron-kiosk; npm install`
- Comprueba que la app esté compilada: `cd valleTPV; npm run build`

### El arranque automático no funciona
- Ejecuta de nuevo `configurar-tpv.ps1` como administrador
- Verifica que existe el acceso directo en: `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup`

### El auto-login no funciona
- Asegúrate de usar tu **contraseña completa**, no el PIN
- Ejecuta el configurador como administrador

---

## 📞 Soporte

Para más información, revisa la documentación en cada archivo de script.
