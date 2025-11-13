#!/bin/bash
# Script para reiniciar el historial de Git desde cero
# ADVERTENCIA: Esto eliminará TODO el historial anterior

set -e

echo "⚠️  ADVERTENCIA: Esto borrará TODO el historial de Git"
echo "El repositorio empezará desde cero con el estado actual"
echo ""
read -p "¿Estás seguro? Escribe 'SI' para continuar: " confirmacion

if [ "$confirmacion" != "SI" ]; then
    echo "❌ Operación cancelada"
    exit 1
fi

cd /home/valle/proyectos/valletpv-hosteleria

echo ""
echo "📋 Paso 1: Haciendo backup del .git actual..."
if [ -d ".git" ]; then
    mv .git .git.backup.$(date +%Y%m%d_%H%M%S)
    echo "✅ Backup creado"
fi

echo ""
echo "📋 Paso 2: Inicializando nuevo repositorio..."
git init

echo ""
echo "📋 Paso 3: Agregando todos los archivos (respetando .gitignore)..."
git add .

echo ""
echo "📋 Paso 4: Creando commit inicial..."
git commit -m "Initial commit - Clean history

- Removed all sensitive data from history
- Starting fresh from secure state
- All credentials now in .env files (not tracked)
- Settings files use environment variables"

echo ""
echo "📋 Paso 5: Renombrando rama a main..."
git branch -M main

echo ""
echo "✅ Historial limpio creado exitosamente"
echo ""
echo "📝 Próximos pasos:"
echo "   1. Verificar que todo esté correcto: git log"
echo "   2. Añadir el remote: git remote add origin git@github.com:vallemrv/valletpv-hosteleria.git"
echo "   3. Hacer push forzado: git push -f origin main"
echo ""
echo "⚠️  IMPORTANTE: Todos los colaboradores deberán:"
echo "   - Hacer backup de sus cambios locales"
echo "   - Borrar su repositorio local"
echo "   - Hacer un nuevo git clone"
