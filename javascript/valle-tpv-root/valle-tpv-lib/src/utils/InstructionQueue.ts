// NO NECESITAMOS NINGUNA REFERENCIA a 'webworker' o 'self' aquí.
// Este fichero ahora es una librería pura.

import dbInstance from '../db/indexedDB';
import Instruction from '../models/instruction';

class InstructionQueue {
  private retryTimeouts: Map<number, number> = new Map();
  private readonly MAX_QUICK_ATTEMPTS = 3; // Intentos rápidos para errores temporales
  private readonly MAX_TOTAL_ATTEMPTS = 50; // Máximo total antes de eliminar definitivamente
  private readonly BASE_DELAY = 5000; // 5 segundos base
  private readonly MAX_DELAY = 300000; // 5 minutos máximo entre reintentos
  private readonly LONG_DELAY = 60000; // 1 minuto para reintentos largos
  private retryTimeout: number | null = null; // Timeout único para reintentar toda la cola
  private isProcessing = false; // Bandera para evitar procesamiento concurrente
  private externalNotifyCallback?: (action: 'increment' | 'decrement' | 'set', value?: number) => void; // Callback alternativo

  // Método para configurar callback externo (usado desde WebSocketHandler)
  setExternalNotifyCallback(callback: (action: 'increment' | 'decrement' | 'set', value?: number) => void) {
    this.externalNotifyCallback = callback;
  }

  // Método para enviar mensaje al cliente principal
  private async notifyClient(type: 'increment' | 'decrement' | 'set', value?: number) {
    try {
      // Si hay un callback externo configurado, usarlo en lugar del Service Worker
      if (this.externalNotifyCallback) {
        this.externalNotifyCallback(type, value);
        return;
      }

      // Código para Service Worker - verificar que estamos en contexto de Service Worker
      if (typeof self !== 'undefined' && 'clients' in self) {
        const clients = await (self as any).clients.matchAll();
        for (const client of clients) {
          client.postMessage({
            type: 'instruction-queue-update',
            action: type,
            value: value
          });
        }
      } else {
        console.warn('⚠️ notifyClient llamado sin callback externo ni contexto de Service Worker');
      }
    } catch (error) {
      console.error('Error notificando al cliente:', error);
    }
  }

  // Ahora devuelve 'true' si se necesita sincronizar, 'false' si no.
  async addAndProcess(url: string, endpoint: string, data: Record<string, any>): Promise<boolean> {
    const pendingInstructions = await dbInstance.getAll('instructionQueue');

    if (pendingInstructions.length === 0) {
      // ✅ CASO 1: No hay instrucciones pendientes, intentar ejecutar la nueva inmediatamente
      const instruction = new Instruction(url, endpoint, data);
      const success = await this.executeInstruction(instruction);

      if (success) {
        // ✅ Éxito: no hay nada que sincronizar
        return false;
      }

      // ❌ Falló: agregarla a la cola para reintentar después
      await dbInstance.add('instructionQueue', instruction);
      await this.notifyClient('increment');
      
      // Programar reintento automático para toda la cola
      await this.scheduleRetryForQueue();
      return true;
    } else {
      // ✅ CASO 2: Hay instrucciones pendientes - NO ejecutar directamente, solo encolar
      // Esto garantiza el orden FIFO: las instrucciones anteriores se deben procesar primero
      
      const instruction = new Instruction(url, endpoint, data);
      await dbInstance.add('instructionQueue', instruction);
      await this.notifyClient('increment');

      // NO procesar la cola aquí - ya hay un sistema de reintentos programados
      // La cola se procesará automáticamente cuando se resuelvan las instrucciones anteriores
      // o cuando el WebSocket se reconecte
      
      return true;
    }
  }

  // processQueueFromSync con procesamiento SECUENCIAL FIFO
  async processQueueFromSync(): Promise<void> {
    if (this.isProcessing) return; // Evitar procesamiento concurrente
    this.isProcessing = true;

    try {
      const allInstructions = await dbInstance.getAll('instructionQueue');
      // IMPORTANTE: Ordenar por ID para garantizar orden FIFO (primero en entrar, primero en salir)
      allInstructions.sort((a, b) => (a.id || 0) - (b.id || 0));
      
      // Procesar SECUENCIALMENTE: una instrucción a la vez
      for (let i = 0; i < allInstructions.length; i++) {
        const instruction = allInstructions[i];
        if (!instruction.id!!) continue; // Saltar instrucciones sin ID válido

        const success = await this.executeInstruction(instruction);
        if (success) {
          await dbInstance.remove('instructionQueue', instruction.id!!);
         
          // Limpiar timeout si existía
          if (this.retryTimeouts.has(instruction.id!!)) {
            clearTimeout(this.retryTimeouts.get(instruction.id!!)!);
            this.retryTimeouts.delete(instruction.id!!);
          }

          // Notificar al cliente para decrementar contador
          await this.notifyClient('decrement');
        } else {
          // CRÍTICO: Si falla, DETENER el procesamiento de la cola
          // La instrucción fallida se reintentará más tarde, pero las siguientes esperan
          await this.scheduleRetryForQueue();
          break; // Salir del bucle - no procesar instrucciones posteriores
        }
      }
    } finally {
      this.isProcessing = false;
    }
  }

  // Programar reintento para toda la cola (nuevo enfoque secuencial)
  private async scheduleRetryForQueue(): Promise<void> {
    // Cancelar reintento anterior si existe
    if (this.retryTimeout) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }

    // Calcular delay basado en el número de instrucciones pendientes
    const pendingInstructions = await dbInstance.getAll('instructionQueue');
    const firstInstruction = pendingInstructions.sort((a, b) => (a.id || 0) - (b.id || 0))[0];

    if (!firstInstruction) return; // No hay instrucciones pendientes

    const attempts = firstInstruction.attempts || 0;

    // Solo eliminar después de MUCHOS intentos (datos críticos)
    if (attempts >= this.MAX_TOTAL_ATTEMPTS) {
      // Limpiar toda la cola
      for (const instr of pendingInstructions) {
        if (instr.id) {
          await dbInstance.remove('instructionQueue', instr.id);
          await this.notifyClient('decrement');
        }
      }
      return;
    }

    // Estrategia de reintento inteligente
    let delay: number;
    if (attempts <= this.MAX_QUICK_ATTEMPTS) {
      // Primeros 3 intentos: backoff exponencial rápido (5s, 10s, 20s)
      delay = this.BASE_DELAY * Math.pow(2, attempts);
    } else {
      // Después de 3 intentos: intervalos de 1-5 minutos para problemas prolongados
      delay = Math.min(this.LONG_DELAY * Math.pow(1.5, attempts - this.MAX_QUICK_ATTEMPTS), this.MAX_DELAY);
    }

   
    // Actualizar contador de intentos de la primera instrucción
    if (firstInstruction.id) {
      await dbInstance.update('instructionQueue', firstInstruction.id, { attempts: attempts + 1 });
    }

    // Programar reintento de toda la cola
    this.retryTimeout = setTimeout(async () => {
      this.retryTimeout = null;
      await this.processQueueFromSync();
    }, delay);
  }

  // Programar reintento con estrategia inteligente
  private async scheduleRetry(instruction: Instruction): Promise<void> {
    if (!instruction.id!!) return; // No procesar instrucciones sin ID

    const attempts = instruction.attempts || 0;
    
    // Solo eliminar después de MUCHOS intentos (datos críticos)
    if (attempts >= this.MAX_TOTAL_ATTEMPTS) {
      console.error(`Instrucción #${instruction.id!} alcanzó el límite máximo de ${this.MAX_TOTAL_ATTEMPTS} intentos. Eliminando de la cola.`);
      await dbInstance.remove('instructionQueue', instruction.id!!);
      await this.notifyClient('decrement');
      return;
    }

    // Estrategia de reintento inteligente
    let delay: number;
    
    if (attempts <= this.MAX_QUICK_ATTEMPTS) {
      // Primeros 3 intentos: backoff exponencial rápido (5s, 10s, 20s)
      delay = this.BASE_DELAY * Math.pow(2, attempts);
    } else {
      // Después de 3 intentos: intervalos de 1-5 minutos para problemas prolongados
      delay = Math.min(this.LONG_DELAY * Math.pow(1.5, attempts - this.MAX_QUICK_ATTEMPTS), this.MAX_DELAY);
    }
    
  
    // Cancelar timeout anterior si existe
    if (this.retryTimeouts.has(instruction.id!!)) {
      clearTimeout(this.retryTimeouts.get(instruction.id!!)!);
    }

    // Programar nuevo intento
    const timeoutId = setTimeout(async () => {
      
      const success = await this.executeInstruction(instruction);
      if (success) {
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        this.retryTimeouts.delete(instruction.id!!);
      } else {
        // Actualizar la instrucción en la BD con el nuevo contador de intentos
        await dbInstance.update('instructionQueue', instruction.id!, { attempts: instruction.attempts });
        // Programar siguiente reintento
        await this.scheduleRetry(instruction);
      }
    }, delay);

    this.retryTimeouts.set(instruction.id!, timeoutId);
  }

  // Método para inicializar el contador con las instrucciones pendientes
  async initializeCounter(): Promise<void> {
    const allInstructions = await dbInstance.getAll('instructionQueue');
    
    // Notificar al cliente el valor inicial
    await this.notifyClient('set', allInstructions.length);
    
    // IMPORTANTE: Reactivar reintentos para instrucciones pendientes después de cortes de luz
    await this.resumePendingRetries();
  }

  // Reactivar reintentos para instrucciones que quedaron pendientes (ej: después de corte de luz)
  private async resumePendingRetries(): Promise<void> {
    const pendingInstructions = await dbInstance.getAll('instructionQueue');
    
    for (const instruction of pendingInstructions) {
      // Programar reintento inmediato para instrucciones que estaban pendientes
      setTimeout(async () => {
        const success = await this.executeInstruction(instruction);
        if (success) {
          await dbInstance.remove('instructionQueue', instruction.id!!);
          await this.notifyClient('decrement');
        } else {
          // Si falla, programar reintentos normales
          await dbInstance.update('instructionQueue', instruction.id!, { attempts: instruction.attempts });
          await this.scheduleRetry(instruction);
        }
      }, Math.random() * 5000); // Distribuir los reintentos en 0-5 segundos para evitar sobrecarga
    }
  }

  // executeInstruction con análisis inteligente de errores
  private async executeInstruction(instruction: Instruction): Promise<boolean> {
    try {
      // Incrementar contador de intentos
      instruction.attempts = (instruction.attempts || 0) + 1;
      
      const { url, endpoint, data } = instruction;
      const formData = new FormData();
      for (const [key, value] of Object.entries(data)) {
        formData.append(key, String(value));
      }
      const response = await fetch(`${url}/${endpoint}`, {
        method: 'POST',
        body: formData,
      });
      
      if (response.ok) {
        // Éxito - eliminar de la cola
        return true;
      } else if (response.status === 400) {
        // 400 Bad Request: Datos malformados - ELIMINAR (no se puede reparar)
        console.error(`❌ Instrucción #${instruction.id!} datos malformados (400). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 401) {
        // 401 Unauthorized: No autenticado - ELIMINAR (token inválido/expirado)
        console.error(`❌ Instrucción #${instruction.id!} no autorizado (401). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 403) {
        // 403 Forbidden: Sin permisos - ELIMINAR (acceso denegado)
        console.error(`❌ Instrucción #${instruction.id!} acceso denegado (403). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 404) {
        // 404 Not Found: Endpoint no existe - ELIMINAR (URL incorrecta)
        console.error(`❌ Instrucción #${instruction.id!} endpoint no encontrado (404). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 409) {
        // 409 Conflict: Conflicto de estado - ELIMINAR (ej: duplicado, ya procesado)
        console.error(`❌ Instrucción #${instruction.id!} conflicto (409). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 422) {
        // 422 Unprocessable Entity: Validación fallida - ELIMINAR (datos no válidos)
        console.error(`❌ Instrucción #${instruction.id!} validación fallida (422). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status >= 400 && response.status < 500) {
        // Otros errores 4xx - ELIMINAR (generalmente errores del cliente irrecuperables)
        console.error(`❌ Instrucción #${instruction.id!} error del cliente (${response.status}). Eliminando.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 500) {
        // 500 Internal Server Error: Bug en el servidor - ELIMINAR
        // Un bug puede tardar días en arreglarse y paraliza toda la cola
        console.error(`❌ Instrucción #${instruction.id!} error interno del servidor (500). Eliminando - requiere corrección del código.`);
        await dbInstance.remove('instructionQueue', instruction.id!!);
        await this.notifyClient('decrement');
        return true;
      } else if (response.status === 502) {
        // 502 Bad Gateway: Servidor gateway no disponible - REINTENTAR
        console.warn(`🔄 Instrucción #${instruction.id!} gateway no disponible (502) - intento ${instruction.attempts}. Reintentando...`);
        return false;
      } else if (response.status === 503) {
        // 503 Service Unavailable: Servidor temporalmente no disponible - REINTENTAR
        console.warn(`🔄 Instrucción #${instruction.id!} servicio no disponible (503) - intento ${instruction.attempts}. Reintentando...`);
        return false;
      } else if (response.status === 504) {
        // 504 Gateway Timeout: Timeout del gateway - REINTENTAR
        console.warn(`🔄 Instrucción #${instruction.id!} timeout del gateway (504) - intento ${instruction.attempts}. Reintentando...`);
        return false;
      } else if (response.status >= 500) {
        // Otros errores 5xx: Errores del servidor que pueden ser temporales - REINTENTAR
        console.warn(`🔄 Instrucción #${instruction.id!} error del servidor (${response.status}) - intento ${instruction.attempts}. Reintentando...`);
        return false;
      } else {
        // Códigos de estado no esperados - REINTENTAR por seguridad
        console.warn(`❓ Instrucción #${instruction.id!} código de estado desconocido (${response.status}) - intento ${instruction.attempts}. Reintentando...`);
        return false;
      }
    } catch (error) {
      // Errores de red (sin conexión, servidor caído, DNS, timeout, CORS, etc.) - REINTENTAR
      console.warn(`🌐 Instrucción #${instruction.id!} error de red (intento ${instruction.attempts || 1}). Reintentando...`, error);
      return false;
    }
  }

  // Limpiar todos los timeouts pendientes
  public clearAllRetries(): void {
    for (const [id, timeoutId] of this.retryTimeouts) {
      clearTimeout(timeoutId);
    }
    this.retryTimeouts.clear();
  }
}

export default InstructionQueue;