import {  watch } from 'vue';
import { useEmpresasStore } from '../store/dbStore/empresasStore';
import { useConnectionStore } from '../store/connectionStore';
import WebSocketHandler from '../utils/WebSocketHandler';

class WebSocketManager {
  private wsHandler: WebSocketHandler | null = null;
  private empresasStore = useEmpresasStore();
  private connectionStore = useConnectionStore();
  private isInitialized = false;

  constructor() {
    this.initWatch();
  }

  private initWatch() {
    if (this.isInitialized) return;
    
    // Watch para cambios en la empresa activa
    watch(
      () => this.empresasStore.empresaActiva,
      (nuevaEmpresa, empresaAnterior) => {
        
        // Si había una empresa anterior, desconectar WebSocket
        if (empresaAnterior && this.wsHandler) {
          this.disconnect();
        }
        
        // Si hay nueva empresa activa, conectar nuevo WebSocket
        if (nuevaEmpresa) {
          this.connectToEmpresa(nuevaEmpresa);
        }
      },
      { immediate: true } // Ejecutar inmediatamente al iniciar
    );
    
    this.isInitialized = true;
  }

  private connectToEmpresa(empresa: any) {
    if (!empresa?.url_servidor || !empresa?.uid) {
      console.warn('Empresa sin URL o UID válidos:', empresa);
      return;
    }

    try {
      // Crear nuevo WebSocketHandler
      // El uid de la empresa se pasa como uid_device al WebSocket
      this.wsHandler = new WebSocketHandler(
        empresa.url_servidor,
        '/ws/comunicacion/devices',
        empresa.uid
      );
      
      // Conectar
      this.wsHandler.connect();
      
    } catch (error) {
      console.error('Error al conectar WebSocket:', error);
    }
  }

  private disconnect() {
    if (this.wsHandler) {
      this.wsHandler.disconnect();
      this.wsHandler = null;
    }
  }

  // Métodos públicos para control manual
  public forceReconnect() {
    const empresaActiva = this.empresasStore.empresaActiva;
    if (empresaActiva) {
      this.disconnect();
      this.connectToEmpresa(empresaActiva);
    }
  }

  public getCurrentWebSocket(): WebSocketHandler | null {
    return this.wsHandler;
  }

  public isConnected(): boolean {
    return this.connectionStore.isConnected;
  }

  public hasError(): boolean {
    return this.connectionStore.hasError;
  }

  public getErrorMessage(): string {
    return this.connectionStore.errorMessage;
  }

  public getConnectionState() {
    return {
      isConnected: this.connectionStore.isConnected,
      hasError: this.connectionStore.hasError,
      errorMessage: this.connectionStore.errorMessage
    };
  }

  public async manualSync(): Promise<void> {
    if (this.wsHandler) {
      await this.wsHandler.manualSync();
    } else {
      console.warn('No hay WebSocket activo para sincronizar');
    }
  }

  public async enqueueInstruction(endpoint: string, data: Record<string, any>): Promise<boolean> {
    if (this.wsHandler) {
      return await this.wsHandler.enqueueInstruction(endpoint, data);
    } else {
      console.error('❌ No hay WebSocket activo. No se puede encolar la instrucción.');
      return false;
    }
  }

  public async getPendingInstructionsCount(): Promise<number> {
    if (this.wsHandler) {
      return await this.wsHandler.getPendingInstructionsCount();
    }
    return 0;
  }

  public destroy() {
    this.disconnect();
    this.isInitialized = false;
  }
}

// Singleton instance
let webSocketManagerInstance: WebSocketManager | null = null;

export function useWebSocketManager() {
  if (!webSocketManagerInstance) {
    webSocketManagerInstance = new WebSocketManager();
  }
  
  return webSocketManagerInstance;
}

export { WebSocketManager };
