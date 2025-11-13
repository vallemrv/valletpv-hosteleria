import { defineStore } from 'pinia';
import { watch } from 'vue';
import { useEmpresasStore } from './dbStore/empresasStore';
import { set } from '@vueuse/core';

export interface Denominacion {
  cantidad: number;
  toCentimos: number;
  euros: number;
}

// Definición del store `cashKeeperStore`
export const useCashKeeperStore = defineStore('cashKeeper', {
  // Estado inicial del store
  state: () => ({
    isConnected: false,
    socket: null as WebSocket | null,
    intervalQ: null as ReturnType<typeof setInterval> | null,
    totalAdmitido: 0,
    hasError: false,
    errorMessage: '',
    ocupada: false,
    totalCobro: 0,
    successMessage: '',
    operationStatus: 'idle' as 'idle' | 'dispensing' | 'canceling' | 'paying' | 'finished',
    cambioEsperado: null as string | number | null,
    denominacionesRecicladores: {
      "20E":  { cantidad: 0, toCentimos: 2000, euros: 20 },
      "10E":  { cantidad: 0, toCentimos: 1000, euros: 10 },
      "5E":   { cantidad: 0, toCentimos: 500,  euros: 5 },
      "2E":   { cantidad: 0, toCentimos: 200,  euros: 2 },
      "1E":   { cantidad: 0, toCentimos: 100,  euros: 1 },
      "50C":  { cantidad: 0, toCentimos: 50,   euros: 0.5 },
      "20C":  { cantidad: 0, toCentimos: 20,   euros: 0.2 },
      "10C":  { cantidad: 0, toCentimos: 10,   euros: 0.1 },
      "5C":   { cantidad: 0, toCentimos: 5,    euros: 0.05 },
      "2C":   { cantidad: 0, toCentimos: 2,    euros: 0.02 },
      "1C":   { cantidad: 0, toCentimos: 1,    euros: 0.01 }
    } as Record<string, Denominacion>,
    denominacionesStacker: {
      "200E": { cantidad: 0, toCentimos: 20000, euros: 200 },  // Billete de 200€
      "100E": { cantidad: 0, toCentimos: 10000, euros: 100 },  // Billete de 100€
      "50E":  { cantidad: 0, toCentimos: 5000,  euros: 50 },   // Billete de 50€
      "20E":  { cantidad: 0, toCentimos: 2000,  euros: 20 },
      "10E":  { cantidad: 0, toCentimos: 1000,  euros: 10 },
      "5E":   { cantidad: 0, toCentimos: 500,   euros: 5 },
      "2E":   { cantidad: 0, toCentimos: 200,   euros: 2 },
      "1E":   { cantidad: 0, toCentimos: 100,   euros: 1 },
      "50C":  { cantidad: 0, toCentimos: 50,    euros: 0.5 },
      "20C":  { cantidad: 0, toCentimos: 20,    euros: 0.2 },
      "10C":  { cantidad: 0, toCentimos: 10,    euros: 0.1 },
      "5C":   { cantidad: 0, toCentimos: 5,     euros: 0.05 },
      "2C":   { cantidad: 0, toCentimos: 2,     euros: 0.02 },
      "1C":   { cantidad: 0, toCentimos: 1,     euros: 0.01 }
    } as Record<string, Denominacion>,
    
    isFinalizando: false, 
    denominacionADevolver: null as { denominacion: number, cantidad: number } | null, 

    reconnectInterval: 5000, 
    maxReconnectAttempts: 10, 
    reconnectAttempts: 0, 
    reconnectTimer: null as ReturnType<typeof setTimeout> | null,
    shouldReconnect: true, 
    
    heartbeatInterval: 30000, 
    heartbeatTimer: null as ReturnType<typeof setInterval> | null,
    pingTimeout: 10000, 
    pingTimeoutTimer: null as ReturnType<typeof setTimeout> | null,
    lastPongTime: Date.now(), 
  }),

  getters: {
    cambio: (state) => Math.max(0, state.totalAdmitido - state.totalCobro),
    denominacionesRecicladoresMenoresAdmitido: (state) => {
      const totalAdmitidoCentimos = Math.round(state.totalAdmitido * 100);
      return Object.fromEntries(
        Object.entries(state.denominacionesRecicladores).filter(
          ([_, denom]) => denom.toCentimos <= totalAdmitidoCentimos
        )
      );
    },
    
    /**
     * Verifica si hay suficiente cambio en los recicladores para devolver el cambio calculado.
     * Utiliza un algoritmo greedy (codicioso) para simular la devolución de cambio.
     * @returns {boolean} true si se puede dar el cambio, false si no hay suficientes billetes/monedas
     */
    puedeDarCambio: (state): boolean => {
      const cambioACentimos = Math.round((state.totalAdmitido - state.totalCobro) * 100);
      
      // Si no hay cambio a devolver, siempre es posible
      if (cambioACentimos <= 0) {
        return true;
      }
      
      // Copiar denominaciones disponibles para simular la dispensación
      const disponibles: Record<number, number> = {};
      Object.entries(state.denominacionesRecicladores).forEach(([key, denom]) => {
        disponibles[denom.toCentimos] = denom.cantidad;
      });
      
      // Ordenar denominaciones de mayor a menor
      const denominacionesOrdenadas = Object.keys(disponibles)
        .map(Number)
        .sort((a, b) => b - a);
      
      let restante = cambioACentimos;
      
      // Algoritmo greedy: intentar usar las denominaciones mayores primero
      for (const valorCentimos of denominacionesOrdenadas) {
        if (restante <= 0) break;
        
        const cantidadDisponible = disponibles[valorCentimos];
        if (cantidadDisponible > 0 && valorCentimos <= restante) {
          const cantidadNecesaria = Math.floor(restante / valorCentimos);
          const cantidadAUsar = Math.min(cantidadNecesaria, cantidadDisponible);
          
          restante -= cantidadAUsar * valorCentimos;
        }
      }
      
      // Si restante es 0, se puede dar el cambio exacto
      return restante === 0;
    },
    
    /**
     * Obtiene un mensaje detallado sobre el cambio que falta
     * @returns {string} mensaje descriptivo del problema de cambio
     */
    mensajeCambioInsuficiente: (state): string => {
      const cambioACentimos = Math.round((state.totalAdmitido - state.totalCobro) * 100);
      
      if (cambioACentimos <= 0) {
        return '';
      }
      
      // Copiar denominaciones disponibles
      const disponibles: Record<number, number> = {};
      Object.entries(state.denominacionesRecicladores).forEach(([key, denom]) => {
        disponibles[denom.toCentimos] = denom.cantidad;
      });
      
      // Ordenar denominaciones de mayor a menor
      const denominacionesOrdenadas = Object.keys(disponibles)
        .map(Number)
        .sort((a, b) => b - a);
      
      let restante = cambioACentimos;
      
      // Simular dispensación
      for (const valorCentimos of denominacionesOrdenadas) {
        if (restante <= 0) break;
        
        const cantidadDisponible = disponibles[valorCentimos];
        if (cantidadDisponible > 0 && valorCentimos <= restante) {
          const cantidadNecesaria = Math.floor(restante / valorCentimos);
          const cantidadAUsar = Math.min(cantidadNecesaria, cantidadDisponible);
          
          restante -= cantidadAUsar * valorCentimos;
        }
      }
      
      if (restante === 0) {
        return '';
      }
      
      // Construir mensaje sobre lo que falta
      const cambioTotal = (cambioACentimos / 100).toFixed(2);
      const faltante = (restante / 100).toFixed(2);
      
      return `No hay suficiente cambio en los recicladores. Se necesita devolver ${cambioTotal}€ pero faltan ${faltante}€ en denominaciones disponibles.`;
    },
  },

  actions: {
    parseUrl(url_cash_keeper: string): { host: string; port: number } | null {
      if (!url_cash_keeper || typeof url_cash_keeper !== 'string') {
        return null;
      }

      let trimmed = url_cash_keeper.trim();
      if (!trimmed) {
        return null;
      }

      // Eliminar prefijos ws:// o wss:// si existen
      trimmed = trimmed.replace(/^wss?:\/\//, '');

      const colonCount = (trimmed.match(/:/g) || []).length;
      if (colonCount !== 1) {
        return null;
      }

      const [host, portStr] = trimmed.split(':');

      if (!host || host.includes(' ') || host.includes('/') || host.includes('\\') || host.includes('?') || host.includes('#')) {
        return null;
      }

      const port = parseInt(portStr, 10);
      if (isNaN(port) || port < 1 || port > 65535) {
        return null;
      }

      return { host: host.trim(), port };
    },

    conectar() {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (!empresaActiva || !Boolean(empresaActiva.usa_cash_keeper)) {
        console.warn('La empresa activa no está configurada para usar CashKeeper');
        this.setError('La empresa activa no está configurada para usar CashKeeper');
        this.operationStatus = 'finished';
        return;
      }

      if (!empresaActiva?.url_cash_keeper) {
        console.error('No hay configuración de CashKeeper (URL)');
        this.setError('No hay configuración de CashKeeper. Verifique la configuración de la empresa.');
        this.operationStatus = 'finished';
        return;
      }

      const urlData = this.parseUrl(empresaActiva.url_cash_keeper);
      if (!urlData) {
        console.error('Formato de URL o puerto inválido');
        this.setError('Formato de URL o puerto inválido en la configuración de CashKeeper');
        this.operationStatus = 'finished';
        return;
      }

      this.shouldReconnect = true;

      try {
        const wsUrl = `ws://${urlData.host}:${urlData.port}/cashkeeper`;
        this.socket = new WebSocket(wsUrl);

        this.socket.onopen = () => {
          this.isConnected = true;
          this.reconnectAttempts = 0; 
          this.lastPongTime = Date.now(); 
          this.startHeartbeat(); 
        };

        this.socket.onmessage = (event) => {
          const data = JSON.parse(event.data);
          if (data.type === 'ping_pong') {
            this.confirmPong();
            return;
          }
          if (data.type === 'instruction_response') {
            this.manejarRespuesta(data.instruccion, data.respuesta);
          }
        };

        this.socket.onerror = (error) => {
          console.error('Error en la conexión WebSocket:', error);
          this.isConnected = false;
          this.socket = null;
          this.setError('Error en la conexión con CashKeeper. Verifique que el dispositivo esté encendido y conectado.');
          this.operationStatus = 'finished';
        };

        this.socket.onclose = () => {
          this.isConnected = false;
          this.socket = null;
          this.stopHeartbeat(); 
          
          if (this.shouldReconnect) {
            this.scheduleReconnect();
          }
        };
      } catch (error) {
        console.error('Error al conectar:', error);
        this.setError(`Error al conectar con CashKeeper: ${error}`);
        this.operationStatus = 'finished';
      }
    },

    desconectar() {
      this.shouldReconnect = false; 
      this.cancelReconnect(); 
      this.stopHeartbeat(); 
      
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      }
      this.isConnected = false;
      this.resetValues(); 
      ('Desconectado del CashKeeper');
    },

    enviarComando(comando: string) {
      if (this.socket && this.isConnected && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(comando + '\n');
      } else {
        console.error('Socket no conectado o no listo');
      }
    },

    cargarDenominaciones() {
      if (this.isConnected) {
        this.enviarComando('#Y#');
      } else {
        this.setError('Socket no conectado');
        this.operationStatus = 'finished';
      }
    },

    parseDenominaciones(denomStr: string): Record<number, number> {
      const result: Record<number, number> = {};
      if (!denomStr) return result;

      const items = denomStr.split(';').flatMap(part => part.split(','));

      items.forEach(item => {
        if (item) {
          const [centimosStr, cantidadStr] = item.split(':');
          const centimos = Number(centimosStr);
          const cantidad = Number(cantidadStr);
          if (!isNaN(centimos) && !isNaN(cantidad)) {
            result[centimos] = cantidad;
          }
        }
      });
      return result;
    },

    manejarRespuesta(comando: string | null, respuesta: string) {
       if (!comando) {
        return;
      }
      const parts = respuesta.split('#').filter(Boolean);
      let hasSuccess = false;
      let hasError = false;

      if (parts.length > 0) {
        const result = this.manejarErrores(parts[0]);
        if (result.isError) {
          hasError = true;
        } else {
          hasSuccess = true;
        }
      }
      comando = comando.trim();
      switch (comando) {
        case '#B#':
        case '#A#':
          if (hasSuccess && !hasError) {
            this.arrancarIntervaloQ();
          }
          break;
        case '#Q#':
          if (this.isFinalizando) {
            console.warn('Ignorando respuesta #Q# porque estamos finalizando');
            return;
          }
          if (parts.length === 2 && !isNaN(Number(parts[1]))) {
            this.totalAdmitido = Number(parts[1]) / 100; 
          }
          break;
        case '#J#':
          if (parts.length === 2 && !isNaN(Number(parts[1]))) {
            this.totalAdmitido = Number(parts[1]) / 100; 

            if (this.operationStatus === 'dispensing' && this.denominacionADevolver) {
              const { denominacion, cantidad } = this.denominacionADevolver;
              this.denominacionADevolver = null;

              let denominacionesStr;
              if (denominacion >= 500) {
                denominacionesStr = `;${denominacion}:${cantidad}`;
              } else {
                denominacionesStr = `${denominacion}:${cantidad};`;
              }
              const cmd = `#U#${denominacionesStr}#0#0#0#`;
              this.enviarComando(cmd);
              this.cambioEsperado = denominacionesStr;

            } else if (this.operationStatus === 'canceling') {
              const totalADevolverCentimos = Math.round(this.totalAdmitido * 100);
              if (totalADevolverCentimos > 0) {
                this.cambioEsperado = totalADevolverCentimos;
                this.enviarComando(`#P#${totalADevolverCentimos}#0#0#0#`);
              } else {
                this.operationStatus = 'finished';
                this.successMessage = 'Operación cancelada';
                setTimeout(() => {
                  this.resetValues();
                }, 500);
              }

            } else {
              const cambioCentimos = Math.round(this.cambio * 100);
              if (cambioCentimos > 0) {
                this.cambioEsperado = cambioCentimos;
                this.enviarComando(`#P#${cambioCentimos}#0#0#0#`);
              } else {
                this.operationStatus = 'finished';
                setTimeout(() => {
                  this.resetValues();
                }, 500);
              }
            }
          }
          break;
        case '#P#':
          this.ocupada = false;
        
          if (parts.length >= 2) {
            const b = Number(parts[1]);
            if (b === this.cambioEsperado) {
              if (this.operationStatus === 'canceling') {
                this.successMessage = 'Operación cancelada correctamente';
              } else {
                this.successMessage = 'Cambio dispensado correctamente';
              }
              this.totalAdmitido -= b / 100;
              this.operationStatus = 'finished';

              // Delay antes de resetear para permitir que el diálogo muestre el mensaje
              setTimeout(() => {
                this.resetValues();
              }, 500);
            } else {
              this.setError('Error por falta de cambio');
              setTimeout(() => {
                this.resetValues();
              }, 500);
            }
          }
          break;
        case '#U#':
          if (parts.length >= 2) {
            // La respuesta puede incluir warnings como WR:LEVEL, extraemos las denominaciones
            // parts[1] contiene las denominaciones dispensadas (puede ser el segundo elemento si hay warning)
            const denominacionesIndex = parts[0].startsWith('WR:') ? 1 : 1;
            const b = parts[denominacionesIndex];
            
            // Calculamos el importe real dispensado por la boca de cambio según el dispositivo
            const importeRespuestaDispositivo = this.calcularImporteDenominaciones(b);
            
            let importeDispensado = importeRespuestaDispositivo;
            
            // Si el dispositivo reporta 0 (billetes van al stacker), calculamos basándose en el arqueo
            if (importeRespuestaDispositivo < 0.01) {
              if (this.cambioEsperado && typeof this.cambioEsperado === 'string') {
                // cambioEsperado contiene las denominaciones que se enviaron: ej ";500:1,1000:1"
                importeDispensado = this.calcularImporteDenominaciones(this.cambioEsperado);
              } else if (this.cambioEsperado && typeof this.cambioEsperado === 'number') {
                // Si cambioEsperado es un número, es el importe en céntimos
                importeDispensado = this.cambioEsperado / 100;
              }
            }
            
            this.successMessage = 'Denominaciones dispensadas correctamente';
            this.totalAdmitido -= importeDispensado;
            
            const restante = this.totalAdmitido;
            if (restante > 0.01) { // Usamos 0.01 para evitar problemas de precisión de punto flotante
              const restanteCentimos = Math.round(restante * 100);
              this.enviarComando(`#P#${restanteCentimos}#0#0#0#`);
              this.cambioEsperado = restanteCentimos;
            } else {
              this.operationStatus = 'finished';
              setTimeout(() => {
                this.resetValues();
              }, 500);
            }
          }
          break;
        case '#Y#':
          if (parts.length >= 3) {
            const b = parts[1]; 
            const c = parts[2]; 
            const recicladoresParsed = this.parseDenominaciones(b);
            const stackerParsed = this.parseDenominaciones(c);
           
            const newRecicladores = { ...this.denominacionesRecicladores };
            Object.keys(recicladoresParsed).forEach(centimosStr => {
              const centimos = Number(centimosStr);
              const cantidad = recicladoresParsed[centimos];
              const key = Object.keys(newRecicladores).find(k => newRecicladores[k].toCentimos === centimos);

              if (key) {
                newRecicladores[key] = { ...newRecicladores[key], cantidad: cantidad };
              }
            });
            this.denominacionesRecicladores = newRecicladores;

            const newStacker = { ...this.denominacionesStacker };
            Object.keys(stackerParsed).forEach(centimosStr => {
              const centimos = Number(centimosStr);
              const cantidad = stackerParsed[centimos];
              const key = Object.keys(newStacker).find(k => newStacker[k].toCentimos === centimos);
              if (key) {
                newStacker[key] = { ...newStacker[key], cantidad: cantidad };
              }
            });
            this.denominacionesStacker = newStacker;
            
          }
          break;
        default:
          console.warn('Comando no manejado:', comando);
          console.warn('Respuesta recibida:', respuesta);
          break;
      }
    },

    finalizarEstadoDeCambio(denominacion: number, cantidad: number = 1) {
      if (!this.isConnected) {
        this.setError('CashKeeper no está conectado. Verifique la conexión del dispositivo.');
        this.operationStatus = 'finished';
        return;
      }

      if (denominacion <= 0 || !Number.isInteger(denominacion)) {
        this.setError('La denominación debe ser un número entero positivo en céntimos.');
        this.operationStatus = 'finished';
        return;
      }

      if (cantidad <= 0 || !Number.isInteger(cantidad)) {
        this.setError('La cantidad debe ser un número entero positivo.');
        this.operationStatus = 'finished';
        return;
      }

      this.isFinalizando = true;
      this.operationStatus = 'dispensing';
      this.denominacionADevolver = { denominacion, cantidad };

      if (this.intervalQ) {
        clearInterval(this.intervalQ);
        this.intervalQ = null;
      }

      setTimeout(() => {
        this.enviarComando('#J#');
      }, 500);
    },

    arrancarIntervaloQ() {
      if (this.intervalQ) {
        clearInterval(this.intervalQ);
      }
      this.intervalQ = setInterval(() => {
        if (this.isConnected && !this.isFinalizando) {
          this.enviarComando('#Q#');
        }
      }, 700);
    },

    manejarErrores(part: string): { isError: boolean; mensaje: string } {
      return { isError: false, mensaje: '' };
    },

    setError(errorMsg: string) {
      this.hasError = true;
      this.errorMessage = errorMsg;
      this.successMessage = '';
     },

    iniciarCobro(total: number) {
      if (total < 0 || isNaN(total)) {
        this.setError('El total de cobro debe ser un número válido y no negativo');
        this.operationStatus = 'finished';
        return;
      }
      
      if (!this.isConnected) {
        this.setError('CashKeeper no está conectado. Verifique la conexión del dispositivo.');
        this.operationStatus = 'finished';
        return;
      }
      
      this.resetValues(); 
      this.totalCobro = total;
      this.operationStatus = 'paying';
      this.enviarComando('#B#0#0#0#');
      this.ocupada = true;
    },

    cancelarOperacion() {
      this.isFinalizando = true;
      this.operationStatus = 'canceling';
      
      if (this.intervalQ) {
        clearInterval(this.intervalQ);
        this.intervalQ = null;
      }

      if (this.isConnected) {
        setTimeout(() => {
          this.enviarComando('#J#'); 
        }, 500);
      } else {
        this.setError('Socket no conectado');
        setTimeout(() => {
          this.resetValues();
        }, 500);
      }
    },

    finalizarCobro() {
      this.isFinalizando = true;
      
      if (this.intervalQ) {
        clearInterval(this.intervalQ);
        this.intervalQ = null;
      }
      
      if (this.isConnected) {
        setTimeout(() => {
          this.enviarComando('#J#'); 
        }, 500);
      } else {
        this.setError('Socket no conectado');
        this.isFinalizando = false; 
      }
    },

    resetValues() {
      this.totalAdmitido = 0;
      this.totalCobro = 0;
      this.ocupada = false;
      this.successMessage = '';
      this.hasError = false;
      this.errorMessage = '';
      this.operationStatus = 'idle';
      this.isFinalizando = false; 
      this.denominacionADevolver = null;
      this.cambioEsperado = null;
      
      // Limpiar intervalos si existen
      if (this.intervalQ) {
        clearInterval(this.intervalQ);
        this.intervalQ = null;
      }
    },
    
    calcularImporteDenominaciones(denominacionesStr: string): number {
      let total = 0;
      const [monedasStr, billetesStr] = denominacionesStr.split(';');
      const items = [...(monedasStr ? monedasStr.split(',') : []), ...(billetesStr ? billetesStr.split(',') : [])];
      items.forEach(item => {
        if (item) {
          const [centimos, cantidad] = item.split(':').map(Number);
          if (!isNaN(centimos) && !isNaN(cantidad)) {
            total += centimos * cantidad;
          }
        }
      });
      return total / 100; 
    },

    iniciarCambio() {
      if (!this.isConnected) {
        this.setError('CashKeeper no está conectado. Verifique la conexión del dispositivo.');
        this.operationStatus = 'finished';
        return;
      }
      
      this.resetValues(); 
      this.cargarDenominaciones(); 

      setTimeout(() => {
        this.enviarComando('#B#0#0#0#');
      }, 500);
      
    },

    scheduleReconnect() {
      // Reconexión infinita mientras shouldReconnect sea true
      // Limitamos el backoff exponencial a un máximo de 60 segundos
      const maxDelay = 60000; // 60 segundos máximo entre intentos
      const calculatedDelay = this.reconnectInterval * Math.pow(2, Math.min(this.reconnectAttempts, 5));
      const delay = Math.min(calculatedDelay, maxDelay);
      
      this.reconnectAttempts++;

     
      this.reconnectTimer = setTimeout(() => {
        if (this.shouldReconnect && !this.isConnected) {
           this.conectar();
        }
      }, delay);
    },

    cancelReconnect() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }
    },

    startHeartbeat() {
      this.stopHeartbeat(); 
      
      this.heartbeatTimer = setInterval(() => {
        if (this.isConnected && this.socket && this.socket.readyState === WebSocket.OPEN) {
          this.sendPing();
        }
      }, this.heartbeatInterval);
    },

    stopHeartbeat() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer);
        this.heartbeatTimer = null;
      }
      if (this.pingTimeoutTimer) {
        clearTimeout(this.pingTimeoutTimer);
        this.pingTimeoutTimer = null;
      }
    },

    sendPing() {
      if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;

      this.socket.send("ping");
      
      this.pingTimeoutTimer = setTimeout(() => {
        console.warn('Timeout esperando respuesta al ping, reconectando...');
        this.desconectar();
        if (this.shouldReconnect) {
          this.scheduleReconnect();
        }
      }, this.pingTimeout);
    },

    confirmPong() {
      this.lastPongTime = Date.now();
      if (this.pingTimeoutTimer) {
        clearTimeout(this.pingTimeoutTimer);
        this.pingTimeoutTimer = null;
      }
    },

    inicializarConexion() {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (empresaActiva && Boolean(empresaActiva.usa_cash_keeper) && empresaActiva.url_cash_keeper) {
        this.conectar();
      } else if (empresaActiva && !Boolean(empresaActiva.usa_cash_keeper)) {
        console.warn('La empresa activa no usa CashKeeper');
      } else {
        console.warn('No hay configuración de CashKeeper o empresa no usa CashKeeper, esperando configuración...');
      }
    },

    verificarConexion() {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;
      
      return {
        isConnected: this.isConnected,
        hasError: this.hasError,
        errorMessage: this.errorMessage,
        shouldReconnect: this.shouldReconnect,
        reconnectAttempts: this.reconnectAttempts,
        reconexionInfinita: true, // Ahora reconecta siempre que shouldReconnect sea true
        lastPongTime: new Date(this.lastPongTime).toLocaleString(),
        tiempoDesdeUltimoPong: Date.now() - this.lastPongTime,
        empresaUsaCashKeeper: Boolean(empresaActiva?.usa_cash_keeper),
        empresaNombre: empresaActiva?.nombre || 'Ninguna',
        urlCashKeeper: empresaActiva?.url_cash_keeper || 'No configurada'
      };
    },

  },
  });


// Función externa para inicializar el watcher de cambios de empresa
export function initCashKeeperWatcher() {
  const empresasStore = useEmpresasStore();
  const cashKeeperStore = useCashKeeperStore();

  // Watcher para cambios en la empresa activa
  watch(
    () => empresasStore.empresaActiva,
    (nuevaEmpresa, empresaAnterior) => {
     
      // Si había una empresa anterior con CashKeeper, desconectar y resetear
      if (empresaAnterior && Boolean(empresaAnterior.usa_cash_keeper) && cashKeeperStore.isConnected) {
        cashKeeperStore.desconectar(); // Ya incluye resetValues()
      }

      // Si hay nueva empresa que usa CashKeeper y tiene configuración, resetear y conectar
      if (nuevaEmpresa && Boolean(nuevaEmpresa.usa_cash_keeper) && nuevaEmpresa.url_cash_keeper) {
        cashKeeperStore.resetValues(); // Limpiar estado antes de conectar
        cashKeeperStore.conectar();
      } else if (nuevaEmpresa && !Boolean(nuevaEmpresa.usa_cash_keeper)) {
        cashKeeperStore.desconectar(); // Ya incluye resetValues()
      } else {
        console.warn('No hay configuración de CashKeeper o empresa no usa CashKeeper, esperando configuración...');
      }
    },
    { immediate: true } // Ejecutar inmediatamente si ya hay empresa activa
  );

  console.warn('👀 Watcher de CashKeeper inicializado');
}