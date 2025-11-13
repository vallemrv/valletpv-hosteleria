import { defineStore } from 'pinia';
import { useEmpresasStore } from './dbStore/empresasStore';

export interface PinPadState {
  ws: WebSocket | null;
  estado: string;
  ultimoRecibo: any | null;
  error: string | null;
  totalACobrar: number;
  mesaId: string | null;
  reconectandoAutomaticamente: boolean;
  intentosReconexion: number;
  cobroYaEnviado: boolean; // Bandera para evitar reenvíos duplicados
}

export const usePinPadStore = defineStore('pinPad', {
  state: (): PinPadState => ({
    ws: null,
    estado: 'desconectado', // desconectado, conectado, iniciando_sesion, sesion_iniciada, cobrando, esperando_tarjeta, reiniciando_pinpad, cobro_aceptado, cobro_denegado, cobro_cancelado, error, servidor_detenido
    ultimoRecibo: null,
    error: null,
    totalACobrar: 0,
    mesaId: null,
    reconectandoAutomaticamente: false,
    intentosReconexion: 0,
    cobroYaEnviado: false
  }),

  getters: {
    estaConectado: (state): boolean => {
      return state.ws !== null && state.ws.readyState === WebSocket.OPEN;
    },

    puedeIniciarCobro: (state): boolean => {
      const conectado = state.ws !== null && state.ws.readyState === WebSocket.OPEN;
      return conectado;
    },

    estaProcesandoCobro: (state): boolean => {
      // Estados donde el diálogo debe mantenerse abierto y el timer pausado
      const estadosCobro = [
        'cobrando',           // Enviando cobro al servidor
        'iniciando_sesion',   // Servidor iniciando sesión
        'sesion_iniciada',    // Sesión lista (temporal)
        'esperando_tarjeta',  // Esperando que cliente pase tarjeta
        'reiniciando_pinpad'  // Reiniciando hardware
      ];
      
      const estaProcesando = estadosCobro.includes(state.estado);
      return estaProcesando;
    },

    cobroEnProceso: (state): boolean => {
      // Hay un cobro activo si hay importe, mesaId y no está en estados finales
      return state.totalACobrar > 0 && 
             state.mesaId !== null &&
             state.estado !== 'cobro_aceptado' && 
             state.estado !== 'cobro_denegado' && 
             state.estado !== 'cobro_cancelado';
    },

    mensajeEstado: (state): string => {
      switch (state.estado) {
        case 'desconectado':
          return 'Desconectado del servidor';
        case 'conectado':
          return 'Conectando...';
        case 'iniciando_sesion':
          return 'Iniciando sesión...';
        case 'sesion_iniciada':
          return 'Sesión iniciada. Listo para cobrar';
        case 'cobrando':
          return 'Enviando cobro al servidor...';
        case 'procesando_peticion':
          return 'Procesando Peticion';
        case 'reiniciando_pinpad':
          return 'Reiniciando PinPad...';
        case 'esperando_tarjeta':
          return 'Esperando que el cliente pase la tarjeta...';
        case 'cobro_aceptado':
          return '✅ Pago aceptado correctamente';
        case 'cobro_denegado':
          return '❌ Pago denegado por el banco';
        case 'cobro_cancelado':
          return '🚫 Pago cancelado';
        case 'error':
          return state.error || 'Error en el sistema';
        case 'servidor_detenido':
          return 'Servidor detenido';
        default:
          return state.estado;
      }
    }
  },

  actions: {
    conectar() {
      const empresasStore = useEmpresasStore();
      const empresaActiva = empresasStore.empresaActiva;

      if (!empresaActiva?.url_pinpad) {
        this.estado = 'error';
        this.error = 'No hay URL de PinPad configurada';
        console.error('❌ No hay URL de PinPad configurada');
        return;
      }

      // Asegurar que la URL tiene el protocolo ws:// o wss://
      let wsUrl = empresaActiva.url_pinpad;
      const originalUrl = wsUrl;
      
      if (!wsUrl.startsWith('ws://') && !wsUrl.startsWith('wss://')) {
        // Detectar si la página es HTTPS para usar wss:// en lugar de ws://
        // PERO permitir override manual si la URL ya incluye protocolo
        const isHttps = typeof window !== 'undefined' && window.location.protocol === 'https:';
        wsUrl = (isHttps ? 'wss://' : 'ws://') + wsUrl;
      }

      try {
        this.ws = new WebSocket(wsUrl);

        // Agregar timeout para detectar conexiones que no responden
        const connectionTimeout = setTimeout(() => {
          if (this.ws && this.ws.readyState === WebSocket.CONNECTING) {
            console.error('⏰ Timeout de conexión WebSocket (10s)');
            this.ws!.close();
            this.estado = 'error';
            this.error = 'Timeout de conexión - verifica la URL y configuración de Caddy';
          }
        }, 10000);

        this.ws!.onopen = () => {
          clearTimeout(connectionTimeout);
            this.estado = 'conectado';
            this.error = null;
            
            // Si estábamos reconectando por un cobro perdido, solicitar recibos pendientes
            if (this.reconectandoAutomaticamente && this.cobroEnProceso) {
              console.log('🔍 Solicitando recibos pendientes tras reconexión...');
              setTimeout(() => {
                this.solicitarRecibosPendientes(); // ✅ Usar función que incluye parámetros
              }, 1000); // Esperar un poco a que se estabilice la conexión
            }
        };
        
        this.ws!.onmessage = (event) => {
          const data = JSON.parse(event.data);
          this.manejarRespuesta(data);
        };
        
        this.ws!.onerror = (err) => {
           this.error = `Error de conexión con el servidor PinPad (${wsUrl})`;
        };
        
        this.ws!.onclose = (event) => { 
          // Limpiar la referencia del WebSocket
          this.ws = null;
          
          // Determinar si fue un cierre limpio o error
          const fueCierreLimpio = event.code === 1000 || event.code === 1001;
          
          // Si hay un cobro en proceso, NO cambiar estado y intentar reconectar
          if (this.cobroEnProceso) {
            this.error = 'Conexión perdida durante cobro - Reintentando conexión...';
            this.iniciarReconexionAutomatica();
          } else {
            // Solo si no hay cobro activo
            if (fueCierreLimpio) {
              this.estado = 'desconectado';
              this.error = null;
            } else {
              this.estado = 'desconectado';
              this.error = 'Conexión perdida con el servidor PinPad';
            }
            this.reconectandoAutomaticamente = false;
            this.intentosReconexion = 0;
          }
        };
      } catch (err) {
        console.error('❌ URL intentada:', wsUrl);
        this.error = 'No se pudo crear la conexión WebSocket al PinPad';
        this.estado = 'error';
      }
    },

    conectar_ws() {
     this.conectar();
    },

    desconectar_ws() {
       if (this.ws) {
        this.ws.close(1000, 'Desconexión manual');
        this.ws = null;
      }
      this.estado = 'desconectado';
      this.error = null;
      this.totalACobrar = 0;
      this.mesaId = null;
      this.cobroYaEnviado = false; // Resetear bandera
      this.detenerReconexionAutomatica(); // Detener reconexión automática
    },

    manejarRespuesta(data: any) {
      // Limpiar error anterior
      this.error = null;

      switch (data.mensaje) {
        case 'iniciando':
           this.estado = 'iniciando_sesion';
          break;
        
        case 'iniciado':
         this.estado = 'sesion_iniciada';
          
          // Si hay un cobro pendiente con mesa_id Y NO se ha enviado ya, reenviarlo automáticamente
          if (this.totalACobrar > 0 && this.mesaId && !this.cobroYaEnviado) {
            this.estado = 'cobrando';
            this.cobroYaEnviado = true; // Marcar como enviado
            this.enviarComando('iniciar_cobro', { 
              importe: this.totalACobrar,
              mesa_id: this.mesaId
            });
          }
          break;
        
        case 'inicializado':
          this.estado = 'sesion_iniciada';
          
          // Si hay un cobro pendiente con mesa_id Y NO se ha enviado ya, reenviarlo automáticamente
          if (this.totalACobrar > 0 && this.mesaId && !this.cobroYaEnviado) {
            this.estado = 'cobrando';
            this.cobroYaEnviado = true; // Marcar como enviado
            this.enviarComando('iniciar_cobro', { 
              importe: this.totalACobrar,
              mesa_id: this.mesaId
            });
          }
          break;
        
        case 'ocupado':
          this.estado = 'error';
          this.error = 'Ya hay un cobro en proceso en el PinPad';
          this.totalACobrar = 0;
          this.cobroYaEnviado = false; // Resetear bandera
          break;
        
        case 'timeout':
          this.estado = 'error';
          this.error = 'Timeout esperando recibo del PinPad (600s)';
          this.totalACobrar = 0;
          this.cobroYaEnviado = false; // Resetear bandera
          break;
        
        case 'pinpad':
          this.estado = 'reiniciando_pinpad';
          break;
        
        case 'esperando':
          this.estado = 'esperando_tarjeta';
          break;
        
        case 'procesando_peticion':
          setTimeout(() => {
           this.estado = 'procesando_peticion';
          }, 500);
          break;
        
        case 'aceptado':
          setTimeout(() => {
           this.estado = 'cobro_aceptado';
            this.ultimoRecibo = data.recibo;
            // NO limpiar totalACobrar aquí - se hace en finalizarTransaccionExitosa
          }, 500);
          break;
        
        case 'recibo_pendiente':
          // Recibo que se completó mientras estábamos desconectados
          this.estado = 'cobro_aceptado';
          this.ultimoRecibo = data.recibo;
          this.totalACobrar = data.importe || 0; // Restaurar importe del recibo
          this.reconectandoAutomaticamente = false; // Detener reconexión
          this.intentosReconexion = 0;
          this.error = null;
          console.log('✅ Recibo pendiente recibido:', data);
          break;
        
        case 'sin_recibos_pendientes':
          // No hay recibos pendientes, el cobro puede haberse perdido o aún estar procesándose
          if (this.reconectandoAutomaticamente && this.cobroEnProceso) {
            this.reconectandoAutomaticamente = false;
            this.intentosReconexion = 0;
            this.error = 'Reconectado - No hay recibos pendientes. Verifique el estado del cobro.';
            console.log('⚠️ No hay recibos pendientes para el cobro actual');
          }
          break;
        
        case 'denegada':
          this.estado = 'cobro_denegado';
          this.ultimoRecibo = null;
          this.error = 'El pago fue denegado por el banco';
          // Limpiar el cobro fallido
          this.totalACobrar = 0;
          this.cobroYaEnviado = false; // Resetear bandera
          break;
        
        case 'cancelado':
          this.estado = 'cobro_cancelado';
          this.ultimoRecibo = null;
          // Limpiar el cobro cancelado
          this.totalACobrar = 0;
          this.cobroYaEnviado = false; // Resetear bandera
          break;
        
        case 'fallo':
          this.estado = 'error';
          this.error = 'Salga y vuelva a enviar el pago al PinPad';
          // Limpiar el cobro con error
          this.totalACobrar = 0;
          this.cobroYaEnviado = false; // Resetear bandera
          break;
        
        case 'finalizado':
         this.estado = 'servidor_detenido';
          break;
        
        default:
          if (data.error) {
            this.error = data.error;
            this.estado = 'error';
            this.totalACobrar = 0;
            this.cobroYaEnviado = false; // Resetear bandera
            console.error('❌ Error:', data.error);
          } else {
            // Mensaje no implementado/desconocido
            console.warn('⚠️ Mensaje no implementado recibido:', data);
            console.warn('⚠️ Mensaje completo:', JSON.stringify(data, null, 2));
          }
      }
    },

    enviarComando(comando: string, params: Record<string, any> = {}): boolean {
      if (!this.estaConectado) {
        this.error = 'No hay conexión con el servidor';
        console.error('❌ No conectado al servidor PinPad');
        return false;
      }

      try {
        const mensaje = { comando, ...params };
        this.ws!.send(JSON.stringify(mensaje));
        return true;
      } catch (err) {
        this.error = 'Error al enviar comando';
        console.error('❌ Error al enviar:', err);
        return false;
      }
    },

    iniciarCobro(importe: number, mesaId: string): boolean {
      
      const importeNum = parseFloat(importe.toString());
      if (isNaN(importeNum) || importeNum <= 0) {
        this.error = 'Importe inválido';
        console.error('❌ Importe inválido:', importe);
        return false;
      }

      if (!mesaId) {
        this.error = 'ID de mesa es obligatorio';
        console.error('❌ ID de mesa requerido:', mesaId);
        return false;
      }

      if (!this.estaConectado) {
        this.error = 'No hay conexión con el servidor';
        console.error('❌ No hay conexión WebSocket para iniciar cobro');
        return false;
      }

      
      // Establecer el importe, mesaId y estado de cobrando
      this.totalACobrar = importeNum;
      this.mesaId = mesaId;
      this.error = null;
      this.ultimoRecibo = null;
      this.estado = 'cobrando';  // Estado específico para cobro en proceso
      this.cobroYaEnviado = true; // Marcar como enviado para evitar duplicados
      
      // Enviar comando de cobro con importe y mesaId (siempre ambos)
      const resultado = this.enviarComando('iniciar_cobro', { 
        importe: importeNum,
        mesa_id: mesaId
      });
      
      if (!resultado) {
        console.error('❌ Falló el envío del comando iniciar_cobro');
        this.estado = 'conectado'; // Revertir estado si falló
      }
      
      return resultado;
    },

    cancelarCobro(): boolean {
      if (!this.estaProcesandoCobro) {
        this.error = 'No hay ningún cobro en proceso';
        return false;
      }

     return this.enviarComando('cancelar_cobro');
    },

    detenerServidor(): boolean {
      return this.enviarComando('detener');
    },

    desconectar() {
      if (this.ws) {
        this.ws.close(1000, 'Desconexión manual'); // Cierre limpio
        this.ws = null;
      }
      this.estado = 'desconectado';
      this.error = null;
    },

    limpiarEstado() {
      this.ultimoRecibo = null;
      this.error = null;
      this.totalACobrar = 0;
      this.mesaId = null;
      this.cobroYaEnviado = false; // Resetear bandera
      this.detenerReconexionAutomatica(); // Detener cualquier reconexión en proceso
      
      // Si está en estado de error, cancelado, denegado o aceptado, volver a sesion_iniciada si está conectado
      if (this.estaConectado && (this.estado === 'cobro_cancelado' || this.estado === 'cobro_denegado' || this.estado === 'cobro_aceptado')) {
        this.estado = 'sesion_iniciada';
      }
    },

    finalizarTransaccionExitosa() {
      // Guardamos el último recibo
      const recibo = this.ultimoRecibo;
      
      // Limpiamos SOLO el importe y error, mantenemos el recibo temporalmente
      this.totalACobrar = 0;
      this.mesaId = null;
      this.error = null;
      this.cobroYaEnviado = false; // Resetear bandera
      
      // Volver a estado base sin cambiar conexión
      if (this.estaConectado) {
        this.estado = 'conectado';
      }
      
      return recibo;
    },

    resetTransaction() {
      this.totalACobrar = 0;
      this.mesaId = null;
      this.error = null;
      this.ultimoRecibo = null;
      this.cobroYaEnviado = false; // Resetear bandera
      
      // Solo cambiar estado si no está en medio de un cobro
      if (this.estaConectado && !this.estaProcesandoCobro) {
        this.estado = 'conectado';
      }
    },

    iniciarReconexionAutomatica() {
      if (this.reconectandoAutomaticamente || !this.cobroEnProceso) {
        return; // Ya está reconectando o no hay cobro en proceso
      }

      this.reconectandoAutomaticamente = true;
      this.intentosReconexion = 0;
      
      const maxIntentos = 10; // Máximo 10 intentos
      const delayBase = 2000; // 2 segundos base

      const intentarReconexion = () => {
        if (!this.cobroEnProceso || this.intentosReconexion >= maxIntentos) {
          this.reconectandoAutomaticamente = false;
          if (this.intentosReconexion >= maxIntentos) {
            this.error = 'No se pudo reconectar después de 10 intentos - Cobro puede haberse procesado';
          }
          return;
        }

        this.intentosReconexion++;
        this.error = `Reintentando conexión... (${this.intentosReconexion}/${maxIntentos})`;
        
        console.log(`🔄 Intento de reconexión ${this.intentosReconexion}/${maxIntentos}`);
        
        // Intentar reconectar
        this.conectar();
        
        // Programar siguiente intento si este falla
        const delay = delayBase * Math.pow(1.5, this.intentosReconexion - 1); // Backoff exponencial
        setTimeout(() => {
          if (!this.estaConectado && this.reconectandoAutomaticamente) {
            intentarReconexion();
          } else if (this.estaConectado) {
            // Reconexión exitosa
            this.error = 'Reconectado - Verificando recibos pendientes...';
            console.log('✅ Reconexión exitosa - Solicitando recibos pendientes');
            
            // Solicitar recibos pendientes
            setTimeout(() => {
              this.solicitarRecibosPendientes();
            }, 500);
          }
        }, Math.min(delay, 10000)); // Máximo 10 segundos de delay
      };

      // Primer intento inmediato
      setTimeout(intentarReconexion, 1000);
    },

    detenerReconexionAutomatica() {
      this.reconectandoAutomaticamente = false;
      this.intentosReconexion = 0;
    },

    solicitarRecibosPendientes(): boolean {
      if (!this.estaConectado) {
        console.warn('⚠️ No se pueden solicitar recibos pendientes - no hay conexión');
        return false;
      }
      
      if (!this.mesaId) {
        console.warn('⚠️ No se pueden solicitar recibos pendientes - no hay mesa_id');
        return false;
      }
      
      console.log('📋 Solicitando recibos pendientes al servidor...');
      return this.enviarComando('solicitar_recibos_pendientes', {
        importe_esperado: this.totalACobrar,
        mesa_id: this.mesaId
      });
    }
  }
});