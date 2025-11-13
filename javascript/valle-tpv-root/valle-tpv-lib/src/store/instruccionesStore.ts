import { defineStore } from 'pinia';
import dbInstance from '../db/indexedDB';
import { useEmpresasStore } from './dbStore/empresasStore';
import { useWebSocketManager } from '../composables/useWebSocketManager';

export const useInstruccionesStore = defineStore('instrucciones', {
  state: () => ({
    enCola: 0
  }),
  actions: {
    setEnCola(valor: number) {
      this.enCola = valor;
    },
    incrementar() {
      this.enCola += 1;
    },
    decrementar() {
      if (this.enCola > 0) {
        this.enCola -= 1;
      }
    },
    async refreshFromDB() {
      try {
        const instrucciones = await dbInstance.getAll('instructionQueue');
        this.setEnCola(instrucciones.length);
      } catch (error) {
        console.error('Error refreshing instructions from DB:', error);
      }
    },
    // Configurar listener para mensajes del Service Worker
    setupServiceWorkerListener() {
      if (!navigator.serviceWorker) return;

      navigator.serviceWorker.addEventListener('message', (event) => {
        if (event.data.type === 'instruction-queue-update') {
          const { action, value } = event.data;
          
          switch (action) {
            case 'increment':
              this.incrementar();
              break;
            case 'decrement':
              this.decrementar();
              break;
            case 'set':
              this.setEnCola(value);
              break;
          }
          
         }
      });
    },
    async encolarInstruccion(endpoint: string, data: Record<string, any>) {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (!empresaActiva || !empresaActiva.url_servidor) {
        console.error('No se pudo obtener la URL del servidor desde el store de empresas.');
        return;
      }

      if (!navigator.serviceWorker || !navigator.serviceWorker.controller) {
        console.error('El Service Worker no está activo.');
        return;
      }

      // Enviar mensaje al Service Worker para encolar la instrucción
      navigator.serviceWorker.controller.postMessage({
        url: empresaActiva.url_servidor, 
        endpoint,
        data: { ...data, uid: empresaActiva.uid }
      });
    },

    // Método alternativo usando WebSocket directamente (cuando Service Worker no está disponible)
    async encolarInstruccionDirecta(endpoint: string, data: Record<string, any>) {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (!empresaActiva || !empresaActiva.url_servidor) {
        console.error('No se pudo obtener la URL del servidor desde el store de empresas.');
        return false;
      }

      try {
        // Usar WebSocket manager (ahora importado estáticamente)
        const wsManager = useWebSocketManager();
        
        // Encolar usando el WebSocket directamente
        const dataWithUid = { ...data, uid: empresaActiva.uid };
        const needsSync = await wsManager.enqueueInstruction(endpoint, dataWithUid);
        
        if (needsSync) {
          console.warn('⏳ Instrucción encolada. Se procesará cuando haya conexión.');
        } else {
          console.warn('✅ Instrucción ejecutada inmediatamente.');
        }
        
        return true;
      } catch (error) {
        console.error('❌ Error al encolar instrucción directa:', error);
        return false;
      }
    }
  }
});
