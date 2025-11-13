#!/bin/bash

# Script para sincronizar la librería valle-tpv-lib a todas las aplicaciones
# Uso: ./sync-lib.sh

set -e  # Detener si hay error

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "🔄 Sincronizando librería valle-tpv-lib..."

# Lista de aplicaciones que usan la librería
APPS=("valleTPV")
# Agregar más apps aquí cuando las crees:
# APPS=("valleTPV" "valleTPV-app2" "valleTPV-app3")

# Verificar que existe la librería
if [ ! -d "valle-tpv-lib/src" ]; then
    echo "❌ Error: No se encuentra valle-tpv-lib/src"
    exit 1
fi

# Sincronizar a cada aplicación
for app in "${APPS[@]}"; do
    if [ -d "$app" ]; then
        echo "  📦 Sincronizando a $app..."
        
        # Crear directorio lib si no existe
        mkdir -p "$app/src/lib"
        
        # Limpiar y copiar
        rm -rf "$app/src/lib/"*
        cp -r valle-tpv-lib/src/* "$app/src/lib/"
        
        # Compilar el Service Worker
        if [ -f "$app/scripts/build-sw.js" ]; then
            echo "  🔨 Compilando Service Worker..."
            cd "$app"
            node scripts/build-sw.js
            cd "$SCRIPT_DIR"
        fi
        
        echo "  ✅ $app sincronizado"
    else
        echo "  ⚠️  Advertencia: No existe la carpeta $app"
    fi
done

echo ""
echo "✅ Sincronización completada!"
