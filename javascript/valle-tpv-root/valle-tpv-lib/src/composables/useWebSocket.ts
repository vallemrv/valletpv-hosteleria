import { computed } from 'vue';
import { useConnectionStore } from '../store/connectionStore';
import { useWebSocketManager } from './useWebSocketManager';

/**
 * Composable unificado para gestión de WebSocket y estado de conexión
 * Combina connectionStore (estado) con webSocketManager (gestión automática)
 */
export function useWebSocket() {
  const connectionStore = useConnectionStore();
  const webSocketManager = useWebSocketManager();

  // Estados reactivos basados en connectionStore
  const isConnected = computed(() => connectionStore.isConnected);
  const hasError = computed(() => connectionStore.hasError);
  const errorMessage = computed(() => connectionStore.errorMessage);

  // Estado combinado
  const connectionState = computed(() => ({
    isConnected: connectionStore.isConnected,
    hasError: connectionStore.hasError,
    errorMessage: connectionStore.errorMessage
  }));

  // Acciones del WebSocketManager
  const forceReconnect = () => webSocketManager.forceReconnect();
  const manualSync = () => webSocketManager.manualSync();
  const getCurrentWebSocket = () => webSocketManager.getCurrentWebSocket();
  const enqueueInstruction = (endpoint: string, data: Record<string, any>) => 
    webSocketManager.enqueueInstruction(endpoint, data);
  const getPendingInstructionsCount = () => webSocketManager.getPendingInstructionsCount();

  // Función helper para obtener el icono según el estado
  const getStatusIcon = computed(() => {
    if (connectionStore.isConnected) return 'mdi-wifi';
    if (connectionStore.hasError) return 'mdi-wifi-off';
    return 'mdi-wifi-strength-1';
  });

  // Función helper para obtener el color según el estado
  const getStatusColor = computed(() => {
    if (connectionStore.isConnected) return 'success';
    if (connectionStore.hasError) return 'error';
    return 'warning';
  });

  // Función helper para obtener el texto según el estado
  const getStatusText = computed(() => {
    if (connectionStore.isConnected) return 'Conectado';
    if (connectionStore.hasError) return 'Error';
    return 'Desconectado';
  });

  return {
    // Estados reactivos
    isConnected,
    hasError,
    errorMessage,
    connectionState,
    
    // Acciones
    forceReconnect,
    manualSync,
    getCurrentWebSocket,
    enqueueInstruction,
    getPendingInstructionsCount,
    
    // Helpers para UI
    getStatusIcon,
    getStatusColor,
    getStatusText,
    
    // Stores originales (por si se necesitan)
    connectionStore,
    webSocketManager
  };
}

export default useWebSocket;
