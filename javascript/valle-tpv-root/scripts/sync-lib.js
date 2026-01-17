const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const rootDir = path.resolve(__dirname, '..');
const sourceLib = path.join(rootDir, 'valle-tpv-lib', 'src');
const apps = ['valleTPV'];

console.log('Sincronizando libreria valle-tpv-lib...');

if (!fs.existsSync(sourceLib)) {
    console.error('Error: No se encuentra valle-tpv-lib/src');
    process.exit(1);
}

apps.forEach(app => {
    const appDir = path.join(rootDir, app);
    if (fs.existsSync(appDir)) {
        console.log(`  Sincronizando a ${app}...`);
        const destLib = path.join(appDir, 'src', 'lib');

        try {
            // Limpiar destino existe
            if (fs.existsSync(destLib)) {
                fs.rmSync(destLib, { recursive: true, force: true });
            }
            fs.mkdirSync(destLib, { recursive: true });

            // Copiar archivos
            // fs.cpSync disponible en Node 16.7+
            fs.cpSync(sourceLib, destLib, { recursive: true });

            // Compilar SW si existe el script
            const buildSw = path.join(appDir, 'scripts', 'build-sw.js');
            if (fs.existsSync(buildSw)) {
                console.log('  Compilando Service Worker...');
                execSync(`node scripts/build-sw.js`, { cwd: appDir, stdio: 'inherit' });
            }
            console.log(`  ${app} sincronizado`);
        } catch (e) {
            console.error(`Error al sincronizar ${app}:`, e);
            process.exit(1);
        }
    } else {
        console.warn(`  Advertencia: No existe la carpeta ${app}`);
    }
});

console.log('Sincronización completada!');
