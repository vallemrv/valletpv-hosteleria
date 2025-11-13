import { defineStore } from 'pinia'
import API from "@/api"

export const useMainStore = defineStore('main', {
  state: () => ({
    listado: [],
    ocupado: false,
    isHttpConnected: false,
    isWsConnected: false,
    error: null,
    total: 0,
    items: [],
    lineasPedidos: [],
    receptores: [],
    empresa: null,
    // Sistema de reconexión
    reconnectAttempts: 0,
    maxReconnectAttempts: 10,
    reconnectTimer: null,
    isReconnecting: false
  }),
  
  getters: {
    isLoading: (state) => state.ocupado,
    isConnected: (state) => state.isHttpConnected || state.isWsConnected,
    getError: (state) => state.error,
    getTotal: (state) => state.total,
    getListadoData: (state) => state.listado,
    getReceptoresData: (state) => state.receptores,
    getEmpresaData: (state) => state.empresa,
    // Estado de reconexión
    getIsReconnecting: (state) => state.isReconnecting,
    reconnectionStatus: (state) => {
      if (!state.isReconnecting) return null
      return `Reintentando conexión (${state.reconnectAttempts}/${state.maxReconnectAttempts})`
    }
  },
  
  actions: {
    setListado(result) {
      this.ocupado = false
      this.isHttpConnected = true
      this.error = null
      
      // Filtrar los receptores "ticket" y "nulo" de la lista de receptores
      if (result && Array.isArray(result)) {
        this.listado = result.filter(item => {
          // Si es un string simple
          if (typeof item === 'string') {
            const itemLower = item.toLowerCase().trim()
            return itemLower !== 'ticket' && itemLower !== 'nulo'
          }
          
          // Si es un objeto con propiedad nombre
          if (item && typeof item === 'object' && item.nombre) {
            const nombreLower = item.nombre.toLowerCase().trim()
            return nombreLower !== 'ticket' && nombreLower !== 'nulo'
          }
          
          // Si es un objeto con propiedad Nombre (con mayúscula)
          if (item && typeof item === 'object' && item.Nombre) {
            const nombreLower = item.Nombre.toLowerCase().trim()
            return nombreLower !== 'ticket' && nombreLower !== 'nulo'
          }
          
          // Si es un objeto con propiedad name
          if (item && typeof item === 'object' && item.name) {
            const nameLower = item.name.toLowerCase().trim()
            return nameLower !== 'ticket' && nameLower !== 'nulo'
          }
          
          return true
        })
      } else {
        this.listado = []
      }
      
      this.total = this.listado.length
    },
    
    setError(error) {
      this.ocupado = false
      this.isHttpConnected = false
      this.error = error
      // Activar reconexión automática cuando hay error
      this.handleConnectionLoss()
    },
    
    setOcupado(estado) {
      this.ocupado = estado
    },
    
    setWsConnected(estado) {
      this.isWsConnected = estado
    },

    setReconnecting(estado) {
      this.isReconnecting = estado
    },

    setReconnectAttempts(attempts) {
      this.reconnectAttempts = attempts
    },
    
    async obtenerListado() {
      this.ocupado = true
      this.error = null
      try {
        const response = await API.get_listado()
        this.setListado(response)
      } catch (error) {
        this.setError(`Error al obtener el listado: ${error.message}`)
        console.error('Error al obtener listado:', error)
      }
    },
    
    async getDatosEmpresa() {
      try {
        const response = await API.get_datos_empresa()
        this.empresa = response
      } catch (error) {
        this.setError(`Error al obtener datos de empresa: ${error.message}`)
        console.error('Error al obtener empresa:', error)
      }
    },
    
    async getReceptores() {
      try {
        const response = await API.get_listado()
        this.receptores = response || []
      } catch (error) {
        this.setError(`Error al obtener receptores: ${error.message}`)
        console.error('Error al obtener receptores:', error)
      }
    },
    
    async getListado() {
      try {
        const response = await API.get_listado()
        // Actualizar listado con filtro
        this.setListado(response)
        // También actualizar receptores con el mismo filtro aplicado
        this.receptores = this.listado
      } catch (error) {
        this.setError(`Error al obtener el listado: ${error.message}`)
        console.error('Error al obtener listado:', error)
      }
    },

    // Métodos para WebSocket
    onConnect() {
      console.log('WebSocket conectado')
      this.setWsConnected(true)
      // Resetear reconexión cuando se conecta exitosamente
      this.resetReconnection()
    },

    onDisconnect() {
      console.log('WebSocket desconectado')
      this.setWsConnected(false)
      // NO activar reconexión automática para WebSocket
      // Los WebSockets tienen su propio sistema de reconexión
    },

    recepcionPedido(pedido) {
      console.log('Pedido recibido:', pedido)
      // Aquí puedes manejar la recepción de pedidos
      // Por ejemplo, agregarlo a una lista de pedidos o mostrar una notificación
      if (pedido) {
        // Agregar el pedido a items si existe esa estructura
        if (Array.isArray(this.items)) {
          this.items.push(pedido)
        }
      }
    },

    async getPendientes(receptoresSeleccionados) {
      try {
        console.log('Obteniendo pedidos pendientes para:', receptoresSeleccionados)
        const response = await API.get_pedidos({ receptores: receptoresSeleccionados })
        this.lineasPedidos = response || []
        console.log('Respuesta de API:', response)
        console.log('Pedidos pendientes obtenidos:', this.lineasPedidos)
      } catch (error) {
        this.setError(`Error al obtener pedidos pendientes: ${error.message}`)
        console.error('Error al obtener pedidos pendientes:', error)
      }
    },

    async recuperarPedido(pedido) {
      try {
        console.log('Recuperando pedido:', pedido);
        const response = await API.recuperar_pedido({ pedido: JSON.stringify({ id: pedido.id, idReceptor: pedido.idReceptor }) });
        this.items.push(response);
        this.lineasPedidos = this.lineasPedidos.filter(p => p.id !== pedido.id);
        console.log('Pedido recuperado y añadido a items:', response);
      } catch (error) {
        this.setError(`Error al recuperar el pedido: ${error.message}`);
        console.error('Error al recuperar el pedido:', error);
      }
    },

    // Sistema de reconexión automática
    startReconnectionTimer() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
      }
      
      // Calcular delay incremental: 1s, 2s, 5s, 10s, 15s, 30s, 60s, etc.
      const delays = [1000, 2000, 5000, 10000, 15000, 30000, 60000, 120000, 300000]
      const delay = delays[Math.min(this.reconnectAttempts, delays.length - 1)]
      
      console.log(`Reintentando conexión en ${delay/1000}s (intento ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts})`)
      this.setReconnecting(true)
      
      this.reconnectTimer = setTimeout(() => {
        this.attemptReconnection()
      }, delay)
    },

    async attemptReconnection() {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        console.log('Máximo número de intentos de reconexión alcanzado')
        this.setReconnecting(false)
        return
      }

      this.setReconnectAttempts(this.reconnectAttempts + 1)
      console.log(`Intento de reconexión ${this.reconnectAttempts}/${this.maxReconnectAttempts}`)

      try {
        // SOLO intentar reconectar HTTP/API, NO WebSocket
        // El WebSocket se reconecta automáticamente desde HomeView
        if (!this.isHttpConnected) {
          await this.getDatosEmpresa()
          await this.getListado()
        }

        // Si HTTP funciona, resetear contador y parar timer
        if (this.isHttpConnected) {
          console.log('Reconexión HTTP exitosa')
          this.resetReconnection()
        } else {
          // Si falla, programar siguiente intento
          this.startReconnectionTimer()
        }
      } catch (error) {
        console.log('Fallo en intento de reconexión:', error.message)
        this.startReconnectionTimer()
      }
    },

    resetReconnection() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
      this.setReconnectAttempts(0)
      this.setReconnecting(false)
      console.log('Sistema de reconexión reseteado')
    },

    // Iniciar reconexión cuando se detecte pérdida de conexión
    handleConnectionLoss() {
      if (!this.isReconnecting && this.reconnectAttempts < this.maxReconnectAttempts) {
        console.log('Pérdida de conexión detectada, iniciando reconexión automática')
        this.startReconnectionTimer()
      }
    }
  }
})
