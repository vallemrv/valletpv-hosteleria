const { spawn, execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

const isWin = os.platform() === 'win32';
const npmCmd = isWin ? 'npm.cmd' : 'npm';
const rootDir = path.resolve(__dirname, '..');

function run(command, args, cwd = rootDir) {
    return new Promise((resolve, reject) => {
        console.log(`> ${command} ${args.join(' ')} (in ${cwd})`);
        const proc = spawn(command, args, { stdio: 'inherit', cwd, shell: true });
        proc.on('close', (code) => {
            if (code === 0) resolve();
            else reject(new Error(`Command failed with code ${code}`));
        });
    });
}

async function build() {
    try {
        console.log('🚀 Iniciando proceso de construcción...');

        // 1. Sincronizar
        console.log('\n📦 Sincronizando librerías...');
        await run('node', ['scripts/sync-lib.js']);

        // 2. Build Lib
        console.log('\n🔨 Construyendo valle-tpv-lib...');
        await run(npmCmd, ['run', 'build:lib']);

        // 3. Build Web
        console.log('\n🌐 Construyendo valleTPV (Web)...');
        await run(npmCmd, ['run', 'build:web']);

        // 4. Copiar dist web a electron
        console.log('\n📂 Copiando archivos web a electron-kiosk...');
        const webDist = path.join(rootDir, 'valleTPV', 'dist');
        const electronDir = path.join(rootDir, 'electron-kiosk');
        const electronWebDist = path.join(electronDir, 'web-dist');

        if (fs.existsSync(electronWebDist)) {
            fs.rmSync(electronWebDist, { recursive: true, force: true });
        }
        fs.mkdirSync(electronWebDist, { recursive: true });
        fs.cpSync(webDist, electronWebDist, { recursive: true });

        // 5. Build Electron
        console.log('\n⚡ Construyendo Electron...');
        // Asegurarse de que electron-kiosk tiene sus dependencias instaladas
        // Si usamos workspaces, ya deberían estar. Si no, habría que hacer npm install ahí.
        // Asumimos que el usuario ha hecho npm install en el root

        await run(npmCmd, ['run', 'build'], electronDir);

        console.log('\n✅ Construcción completada exitosamente!');
        console.log(`   Ejecutable en: ${path.join(electronDir, 'dist-electron')}`);

    } catch (err) {
        console.error('❌ Error en el build:', err);
        process.exit(1);
    }
}

build();
