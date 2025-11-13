// src/composables/useTauri.ts
import { invoke } from '@tauri-apps/api/core'

// Extender el tipo Window para incluir __TAURI__
declare global {
  interface Window {
    __TAURI__?: any
  }
}

export const useTauri = () => {
  // Función de ejemplo para invocar comandos de Rust
  const saludar = async (nombre: string): Promise<string> => {
    try {
      return await invoke('saludar', { nombre })
    } catch (error) {
      console.error('Error al invocar comando Tauri:', error)
      return `Hola ${nombre} desde JavaScript (fallback)`
    }
  }

  // Verificar si estamos en entorno Tauri
  const isTauri = (): boolean => {
    return typeof window !== 'undefined' && window.__TAURI__ !== undefined
  }

  return {
    saludar,
    isTauri
  }
}