const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('path');
const http = require('http');
const fs = require('fs');
const { exec } = require('child_process');

let mainWindow;
let server;

function startServer() {
  const PORT = 5173;
  let distPath = path.join(__dirname, 'web-dist');

  if (!fs.existsSync(distPath)) {
    distPath = path.join(__dirname, '..', 'valleTPV', 'dist');
  }

  server = http.createServer((req, res) => {
    let filePath = path.join(distPath, req.url === '/' ? 'index.html' : req.url);

    // Si no existe el archivo, servir index.html (para SPA routing)
    if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
      filePath = path.join(distPath, 'index.html');
    }

    const ext = path.extname(filePath);
    const contentType = {
      '.html': 'text/html',
      '.js': 'text/javascript',
      '.css': 'text/css',
      '.json': 'application/json',
      '.png': 'image/png',
      '.jpg': 'image/jpg',
      '.jpeg': 'image/jpeg',
      '.gif': 'image/gif',
      '.svg': 'image/svg+xml',
      '.ico': 'image/x-icon',
      '.woff': 'font/woff',
      '.woff2': 'font/woff2',
      '.ttf': 'font/ttf',
      '.eot': 'application/vnd.ms-fontobject',
      '.otf': 'font/otf',
      '.webp': 'image/webp'
    }[ext] || 'text/plain';

    fs.readFile(filePath, (err, data) => {
      if (err) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Not found');
      } else {
        res.writeHead(200, {
          'Content-Type': contentType,
          'Access-Control-Allow-Origin': '*',
          'Cache-Control': 'no-cache'
        });
        res.end(data);
      }
    });
  });

  server.listen(PORT);
  return PORT;
}

function createWindow() {
  let PORT;

  // Modo desarrollo: cargar desde Vite dev server
  const isDev = process.argv.includes('--dev');

  if (isDev) {
    PORT = 5173; // Puerto por defecto de Vite
    console.log('Modo desarrollo: Cargando desde http://localhost:' + PORT);
  } else {
    // Modo producción: iniciar servidor de archivos estáticos
    PORT = startServer();
  }

  // Crear ventana en modo kiosk
  mainWindow = new BrowserWindow({
    fullscreen: !isDev,
    kiosk: !isDev,
    frame: isDev,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
      enableRemoteModule: true
    }
  });

  // Cargar la app
  mainWindow.loadURL(`http://localhost:${PORT}`);

  // Abrir DevTools en modo desarrollo
  if (isDev) {
    mainWindow.webContents.openDevTools();
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  // Deshabilitar menu
  mainWindow.setMenuBarVisibility(false);
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (server) server.close();
  app.quit();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

// Salir con ESC en modo kiosk
app.on('browser-window-created', (_, window) => {
  window.webContents.on('before-input-event', (event, input) => {
    if (input.key === 'Escape') {
      app.quit();
    }
  });
});

// Manejadores IPC para cerrar aplicación y apagar ordenador
ipcMain.on('close-app', () => {
  if (server) server.close();
  app.quit();
});

ipcMain.on('shutdown-computer', () => {
  // Cerrar servidor y aplicación
  if (server) server.close();

  // Comando para apagar el ordenador en Windows
  const platform = process.platform;
  let shutdownCommand;

  if (platform === 'win32') {
    shutdownCommand = 'shutdown /s /t 5'; // Apagar en 5 segundos
  } else if (platform === 'darwin') {
    shutdownCommand = 'sudo shutdown -h +1'; // Mac OS
  } else {
    shutdownCommand = 'sudo shutdown -h +1'; // Linux
  }

  exec(shutdownCommand, (error) => {
    if (error) {
      console.error('Error al apagar el ordenador:', error);
    }
    app.quit();
  });
});
