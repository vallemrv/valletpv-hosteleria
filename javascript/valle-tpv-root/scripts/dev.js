const { spawn } = require('child_process');
const path = require('path');
const os = require('os');

const isWin = os.platform() === 'win32';
const npmCmd = isWin ? 'npm.cmd' : 'npm';
const npxCmd = isWin ? 'npx.cmd' : 'npx';

const rootDir = path.resolve(__dirname, '..');

function run(command, args, cwd = rootDir, env = process.env) {
    return new Promise((resolve, reject) => {
        const proc = spawn(command, args, { stdio: 'inherit', cwd, env });
        proc.on('close', (code) => {
            if (code === 0) resolve();
            else reject(new Error(`Command ${command} ${args.join(' ')} failed with code ${code}`));
        });
    });
}

function startProcess(command, args, cwd = rootDir, env = process.env) {
    const proc = spawn(command, args, { stdio: 'inherit', cwd, env });
    return proc;
}

async function startDev() {
    try {
        console.log('🚀 Iniciando entorno de desarrollo...');

        // 1. Sincronizar librería
        console.log('\n📦 Sincronizando librerías...');
        await run('node', ['scripts/sync-lib.js']);

        // 2. Iniciar servidor Vite (Web)
        console.log('\n🌐 Iniciando servidor Web (Vite)...');
        const vite = startProcess(npmCmd, ['run', 'dev:web']);

        // Esperar a que Vite inicie (un poco tonto, pero funcional)
        console.log('⏳ Esperando a que Vite esté listo...');
        await new Promise(r => setTimeout(r, 4000));

        // 3. Iniciar Electron
        console.log('\n🖥️  Iniciando Electron...');
        // Necesitamos electron instalado. Asumimos que está en electron-kiosk/node_modules o global.
        // Mejor usar 'electron .' dentro de la carpeta electron-kiosk
        const electronDir = path.join(rootDir, 'electron-kiosk');

        // Usamos npx electron . --dev desde la carpeta electron-kiosk
        // O npm run electron:dev si existiera en el package.json de electron-kiosk
        // electron-kiosk tiene "electron:dev": "electron . --dev"

        const env = { ...process.env };
        if (os.platform() === 'linux') {
            env.WEBKIT_DISABLE_DMABUF_RENDERER = '1';
        }
        const electron = startProcess(npmCmd, ['run', 'electron:dev'], electronDir, env);

        electron.on('close', () => {
            console.log('\n🛑 Electron cerrado. Terminando procesos...');
            vite.kill();
            process.exit(0);
        });

    } catch (err) {
        console.error('❌ Error:', err);
        process.exit(1);
    }
}

startDev();
