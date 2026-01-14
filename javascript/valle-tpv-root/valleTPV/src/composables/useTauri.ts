// src/composables/useTauri.ts
// Funciones para interactuar con Electron

export const useTauri = () => {
  // Verificar si estamos en entorno Electron
  const isElectron = (): boolean => {
    return typeof window !== 'undefined' && (window as any).require !== undefined
  }

  // Cerrar la aplicación (compatible con Electron y navegador)
  const cerrarAplicacion = async (): Promise<void> => {
    try {
      // Si está en Electron
      if (isElectron()) {
        const { ipcRenderer } = (window as any).require('electron')
        ipcRenderer.send('close-app')
        return
      }

      // Si es navegador web, intentar cerrar ventana
      window.close()
      
      // Si no se puede cerrar (bloqueado por navegador), mostrar mensaje
      setTimeout(() => {
        alert('Para salir de la aplicación, cierra esta pestaña o ventana del navegador.')
      }, 100)
    } catch (error) {
      console.error('Error al cerrar la aplicación:', error)
    }
  }

  // Apagar el ordenador
  const apagarOrdenador = async (): Promise<void> => {
    try {
      // Si está en Electron
      if (isElectron()) {
        const { ipcRenderer } = (window as any).require('electron')
        ipcRenderer.send('shutdown-computer')
        return
      }

      // Si es navegador web, no es posible apagar el ordenador
      alert('Esta función solo está disponible en la versión de escritorio.')
    } catch (error) {
      console.error('Error al apagar el ordenador:', error)
    }
  }

  return {
    isElectron,
    cerrarAplicacion,
    apagarOrdenador
  }
}