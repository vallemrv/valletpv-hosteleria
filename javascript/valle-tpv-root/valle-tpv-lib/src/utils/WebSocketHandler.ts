import {useConnectionStore} from '../store/connectionStore';
import {useInstruccionesStore} from '../store/instruccionesStore';
import dbInstance from '../db/indexedDB';
import { useMesasStore } from '../store/dbStore/mesasStore';
import { useCuentaStore } from '../store/dbStore/cuentasStore';
import { useTeclasStore } from '../store/dbStore/teclasStore';
import { useSeccionesStore } from '../store/dbStore/seccionesStore';
import { useSugerenciasStore } from '../store/dbStore/sugerenciasStore';
import { useCamarerosStore } from '../store/dbStore/camarerosStore';
import { useZonasStore } from '../store/dbStore/zonasStore';
import InstructionQueue from './InstructionQueue';

class WebSocketHandler {
  servidor: string;
  endpoint: string;
  uid_device: string;
  socket: WebSocket | null;
  reconnectInterval: number;
  connectionStore: ReturnType<typeof useConnectionStore>;
  instruccionesStore: ReturnType<typeof useInstruccionesStore>;
  private syncController: AbortController | null;
  private isSyncing: boolean;
  private shouldReconnect: boolean; // Nuevo flag
  private instructionQueue: InstructionQueue; // Nueva instancia para procesar cola

  // Definir las tablas a sincronizar en orden (empresas no se incluye)
  //private syncTables = ['mesas', 'lineaspedido', 'teclas', 'seccionescom', 'sugerencias', 'camareros', 'zonas'];
  private syncTables = ['mesas', 'camareros', 'lineaspedido', 'teclas', "seccionescom", "sugerencias", "zonas"];

  constructor(servidor: string, endpoint: string, uid_device: string) {
    this.servidor = servidor;
    this.endpoint = endpoint;
    this.uid_device = uid_device;
    this.socket = null;
    this.reconnectInterval = 5000; // 5 segundos
    this.connectionStore = useConnectionStore();
    this.instruccionesStore = useInstruccionesStore();
    this.syncController = null;
    this.isSyncing = false;
    this.shouldReconnect = true; // Por defecto sí debe reconectar
    this.instructionQueue = new InstructionQueue(); // Instanciar cola de instrucciones
    
    // Configurar callback para que el InstructionQueue actualice directamente el store
    this.instructionQueue.setExternalNotifyCallback((action, value) => {
      switch (action) {
        case 'increment':
          this.instruccionesStore.incrementar();
          break;
        case 'decrement':
          this.instruccionesStore.decrementar();
          break;
        case 'set':
          if (value !== undefined) {
            this.instruccionesStore.setEnCola(value);
          }
          break;
      }
    });

    // CRÍTICO: Inicializar el contador desde IndexedDB al crear el WebSocketHandler
    this.instructionQueue.initializeCounter().catch(error => {
      console.error('❌ Error inicializando contador de instrucciones en WebSocketHandler:', error);
    });
  }

  connect(): void {
    const url = `${this.servidor}${this.endpoint}?uid=${this.uid_device}`;
    this.socket = new WebSocket(url);
    
    // Habilitar reconexión automática al conectar
    this.shouldReconnect = true;

    this.socket.onopen = () => {
      this.connectionStore.setConnected(true);
      this.startInitialSync();
    };

    this.socket.onmessage = (event: MessageEvent) => {
      try {
        let mensajes = JSON.parse(event.data);
        this.modifyClientDB(mensajes); 
      } catch (error) {
        console.error('Error al procesar el mensaje:', error);
      }
    };

    this.socket.onclose = () => {
      console.warn('Conexión WebSocket cerrada.');
      this.connectionStore.setConnected(false);
      
      // Solo reconectar si está configurado para hacerlo
      if (this.shouldReconnect) {
        setTimeout(() => this.connect(), this.reconnectInterval);
      }
    };

    this.socket.onerror = (error: Event) => {
      console.error('Error en el WebSocket:', error);
      this.connectionStore.setError('Error en el WebSocket');
    };
  }

  reconnect(): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      console.warn('Ya existe una conexión WebSocket activa.');
      return;
    }

    console.warn('Conexión WebSocket cerrada. Intentando reconectar...');
    setTimeout(() => this.connect(), this.reconnectInterval);
  }

  private async modifyClientDB(data: any): Promise<void> {
    try {
      const { tb, op, obj } = data;

      // Determinar si obj es un array o un objeto simple
      const objectsToProcess = Array.isArray(obj) ? obj : [obj];

      // Procesar cada objeto (o el único objeto si no es array)
      for (const currentObj of objectsToProcess) {
        await this.processSingleOperation(tb, op, currentObj);
      }

    } catch (error) {
      console.error('Error al modificar la base de datos del cliente:', error);
    }
  }

  private async processSingleOperation(tb: string, op: string, obj: any): Promise<void> {
    // Inicializar el store correspondiente usando switch
    let store: any = null;

    switch (tb) {
      case 'mesas':
      case 'mesasabiertas':
        if (tb === 'mesasabiertas') {
          const mesaLocal = await dbInstance.getById('mesas', obj.id || obj.ID);
          if (!mesaLocal) {
            console.warn(`Mesa con ID ${obj.id || obj.ID} no existe localmente. Ignorando instrucción de mesaabierta.`);
            return; // Salir si la mesa no existe localmente
          }
          mesaLocal.abierta = obj.abierta;
          mesaLocal.num = obj.num;
          op='md'; // Forzar operación de modificación
          obj = mesaLocal; // Reemplazar obj con la mesa local actualizada
        }
        store = useMesasStore();
        break;
      case 'lineaspedido':
        store = useCuentaStore();
        break;
      case 'teclas':
        store = useTeclasStore();
        break;
      case 'seccionescom':
        store = useSeccionesStore();
        break;
      case 'sugerencias':
        store = useSugerenciasStore();
        break;
      case 'camareros':
        store = useCamarerosStore();
        break;
      case 'zonas':
        store = useZonasStore();
        break;
      default:
        console.warn(`Tabla no manejada en el cliente: ${tb}`);
        return;
    }

    // Ejecutar la operación correspondiente
    switch (op) {
      case 'insert':
        // Verificar si el objeto ya existe antes de insertar
        const existingObj = await dbInstance.getById(tb, obj.id || obj.ID);
        if (existingObj) {
          await store.update(obj);
        } else {
          await store.insert(obj);
        }
        break;
      case 'md':
        await store.update(obj);
        break;
      case 'rm':
        await store.rm(obj.id || obj.ID);
        break;
      default:
        console.warn(`Operación no manejada: ${op} para tabla ${tb}`);
    }
  }

  private async startInitialSync(): Promise<void> {
    // Cancelar cualquier sincronización en curso
    this.cancelCurrentSync();
    
    // Crear nuevo controlador para esta sincronización
    this.syncController = new AbortController();
    this.isSyncing = true;
    
    try {
      // PASO 1: Primero procesar la cola de instrucciones pendientes si las hay
      await this.processInstructionQueueBeforeSync();
      
      // PASO 2: Verificar si la sincronización fue cancelada antes de continuar
      if (this.syncController?.signal.aborted) {
        throw new Error('Sync cancelled');
      }
      
      // PASO 3: Proceder con la sincronización normal de las tablas del WebSocket
      await this.syncAllTables();
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        console.info('Sincronización cancelada - Nueva conexión detectada');
      } else {
        console.error('Error en sincronización:', error);
      }
    } finally {
      this.isSyncing = false;
      this.syncController = null;
    }
  }

  private async processInstructionQueueBeforeSync(): Promise<void> {
    try {
      // Verificar si hay instrucciones pendientes en la cola
      const pendingInstructionsBeforeProcess = await dbInstance.getAll('instructionQueue');
      
      if (pendingInstructionsBeforeProcess.length > 0) {
        console.info(`🔄 Procesando ${pendingInstructionsBeforeProcess.length} instrucción(es) pendiente(s) antes de sincronizar WebSocket...`);
        
        // Procesar toda la cola de instrucciones
        // El InstructionQueue automáticamente actualizará el store vía el callback configurado
        await this.instructionQueue.processQueueFromSync();
        
        // Verificar si la sincronización fue cancelada durante el procesamiento
        if (this.syncController?.signal.aborted) {
          throw new Error('Sync cancelled');
        }
        
        // Verificar cuántas instrucciones quedan después del procesamiento
        const pendingInstructionsAfterProcess = await dbInstance.getAll('instructionQueue');
        console.info(`✅ Cola procesada. Instrucciones restantes: ${pendingInstructionsAfterProcess.length}`);
        
        // Esperar unos segundos después de procesar la cola para dar tiempo a que se completen las operaciones
        await this.delay(3000);
        
        // Verificar nuevamente si la sincronización fue cancelada después del delay
        if (this.syncController?.signal.aborted) {
          throw new Error('Sync cancelled');
        }
      }
    } catch (error) {
      if (error instanceof Error && error.message === 'Sync cancelled') {
        throw error; // Re-lanzar para que se maneje en startInitialSync
      }
      console.error('❌ Error procesando cola de instrucciones antes de sincronizar:', error);
      
      // Incluso si hay error, intentar actualizar el contador del store por seguridad
      try {
        await this.instruccionesStore.refreshFromDB();
      } catch (refreshError) {
        console.error('Error refrescando store después de error:', refreshError);
      }
      
      // Continuar con la sincronización del WebSocket incluso si hay errores en la cola
    }
  }

  private cancelCurrentSync(): void {
    if (this.syncController && this.isSyncing) {
      this.syncController.abort();
      this.syncController = null;
      this.isSyncing = false;
    }
  }

  private async syncAllTables(): Promise<void> {
    for (const table of this.syncTables) {
      // Verificar si la sincronización fue cancelada
      if (this.syncController?.signal.aborted) {
        throw new Error('Sync cancelled');
      }
      
      try {
        const  thereChanges = await dbInstance.syncWithServer(table, this.uid_device, this.servidor);

        // Verificar nuevamente antes de actualizar store
        if (this.syncController?.signal.aborted) {
          throw new Error('Sync cancelled');
        }
        
        if (thereChanges) {
          await this.refreshStore(table);
        }

        // Verificar antes de la pausa
        if (this.syncController?.signal.aborted) {
          throw new Error('Sync cancelled');
        }

        //esperamos un segundo para procesar otra tabla
        await this.delay(500);

      } catch (error) {
        if (error instanceof Error && error.message === 'Sync cancelled') {
          throw error; // Re-lanzar para que se maneje en startInitialSync
        }
        console.error(`Error sincronizando tabla ${table}:`, error);
      }
    }
  }

  private async delay(ms: number): Promise<void> {
    return new Promise((resolve, reject) => {
      const timeout = setTimeout(resolve, ms);
      
      // Si se cancela, rechazar la promesa
      if (this.syncController?.signal) {
        this.syncController.signal.addEventListener('abort', () => {
          clearTimeout(timeout);
          reject(new Error('Sync cancelled'));
        });
      }
    });
  }

  private async refreshStore(table: string): Promise<void> {
    try {
      switch (table) {
        case 'mesas':
          const mesasStore = useMesasStore();
          mesasStore.isLoadDB = false; // Forzar recarga
          await mesasStore.initStore();
          break;
        case 'lineaspedido':
          const lineasPedidoStore = useCuentaStore();
          lineasPedidoStore.isLoadDB = false; // Forzar recarga
          await lineasPedidoStore.initStore();
          break;
        case 'teclas':
          const teclasStore = useTeclasStore();
          teclasStore.isLoadDB = false; // Forzar recarga
          await teclasStore.initStore();
          break;
        case 'seccionescom':
          const seccionesComStore = useSeccionesStore();
          seccionesComStore.isLoadDB = false; // Forzar recarga
          await seccionesComStore.initStore();
          break;
        case 'sugerencias':
          const sugerenciasStore = useSugerenciasStore();
          sugerenciasStore.isLoadDB = false; // Forzar recarga
          await sugerenciasStore.initStore();
          break;
        case 'camareros':
          const camarerosStore = useCamarerosStore();
          camarerosStore.isLoadDB = false; // Forzar recarga
          await camarerosStore.initStore();
          break;
        case 'zonas':
          const zonasStore = useZonasStore();
          zonasStore.isLoadDB = false; // Forzar recarga
          await zonasStore.initStore();
          break;
        default:
          console.warn(`Store no encontrado para la tabla: ${table}`);
      }
    } catch (error) {
      console.error(`Error refrescando store para tabla ${table}:`, error);
    }
  }

  disconnect(): void {
    // Cancelar cualquier sincronización en curso
    this.cancelCurrentSync();
    
    // Deshabilitar reconexión automática
    this.shouldReconnect = false;
    
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.connectionStore.setConnected(false);
    
  }

  // Método público para ejecutar sincronización manual
  async manualSync(): Promise<void> {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      await this.startInitialSync();
    } else {
      console.warn('WebSocket no está conectado. No se puede sincronizar.');
    }
  }

  // Método público para verificar si está sincronizando
  isCurrentlySyncing(): boolean {
    return this.isSyncing;
  }

  // Método público para cancelar sincronización actual
  cancelSync(): void {
    this.cancelCurrentSync();
  }

  // Método público para encolar instrucciones cuando no hay conexión o el servidor falla
  async enqueueInstruction(endpoint: string, data: Record<string, any>): Promise<boolean> {
    try {
      const needsSync = await this.instructionQueue.addAndProcess(this.servidor, endpoint, data);
      return needsSync;
    } catch (error) {
      console.error('❌ Error al encolar instrucción:', error);
      return false;
    }
  }

  // Método público para obtener el número de instrucciones pendientes
  async getPendingInstructionsCount(): Promise<number> {
    try {
      const pendingInstructions = await dbInstance.getAll('instructionQueue');
      return pendingInstructions.length;
    } catch (error) {
      console.error('❌ Error obteniendo instrucciones pendientes:', error);
      return 0;
    }
  }
}

export default WebSocketHandler;
