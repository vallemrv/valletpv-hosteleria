
const { app, BrowserWindow, globalShortcut } = require('electron')
const os = require("os")
const path = require("path")
const config = require(path.join(__dirname, "../package.json"))

  // Mantén una referencia global del objeto window, si no lo haces, la ventana
  // se cerrará automáticamente cuando el objeto JavaScript sea eliminado por el recolector de basura.
  let win

  function createWindow () {
    // Crea la ventana del navegador.
    win = new BrowserWindow({ width: 1090, height: 800 })


    // Abre las herramientas de desarrollo (DevTools).
    var platform = os.platform()
    if (platform === 'darwin') {
      globalShortcut.register('Command+Option+I', () => {
        win.webContents.openDevTools()
      })
    }
    // y carga el archivo index.html de la aplicación.
    win.loadFile('render/index.html')



    // Emitido cuando la ventana es cerrada.
    win.on('closed', () => {
      // Elimina la referencia al objeto window, normalmente  guardarías las ventanas
      // en un vector si tu aplicación soporta múltiples ventanas, este es el momento
      // en el que deberías borrar el elemento correspondiente.
      win = null
    })
    win.onbeforeunload = (e) => {
        // Prevent Command-R from unloading the window contents.
        e.returnValue = false
    }

    
  }

  // Este método será llamado cuando Electron haya terminado
  // la inicialización y esté listo para crear ventanas del navegador.
  // Algunas APIs pueden usarse sólo después de que este evento ocurra.
  app.on('ready', createWindow)

  // Sal cuando todas las ventanas hayan sido cerradas.
  app.on('window-all-closed', () => {
    // En macOS es común para las aplicaciones y sus barras de menú
    // que estén activas hasta que el usuario salga explicitamente con Cmd + Q
      app.quit()

  })

  app.on('activate', () => {
    // En macOS es común volver a crear una ventana en la aplicación cuando el
    // icono del dock es clicado y no hay otras ventanas abiertas.
    if (win === null) {
      createWindow()
    }
  })

  // En este archivo puedes incluir el resto del código del proceso principal de
  // tu aplicación. También puedes ponerlos en archivos separados y requerirlos aquí.
