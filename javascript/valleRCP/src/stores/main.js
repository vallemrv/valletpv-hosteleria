import { defineStore } from 'pinia'
import { toRaw } from 'vue'
import API from "@/api"
import pedidosDB from "@/db"
import audioMario from "@/assets/mario.mp3"

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
    deviceUID: null,
    // Sistema de reconexión
    reconnectAttempts: 0,
    maxReconnectAttempts: 10,
    reconnectTimer: null,
    isReconnecting: false,
    // Pedidos en memoria
    pedidosEnMemoria: [],
    dbInitialized: false,
    // Callback para cerrar vista de servidos
    cerrarServidosCallback: null
  }),

  getters: {
    isLoading: (state) => state.ocupado,
    isConnected: (state) => state.isHttpConnected || state.isWsConnected,
    getError: (state) => state.error,
    getTotal: (state) => state.total,
    getListadoData: (state) => state.listado,
    getReceptoresData: (state) => state.receptores,

    // Pedidos activos en memoria
    pedidosActivos: (state) => {
      return state.pedidosEnMemoria.filter(p => p.estado === 'activo')
    },

    // Pedidos por receptor
    pedidosPorReceptor: (state) => (receptor) => {
      return state.pedidosEnMemoria.filter(p =>
        p.estado === 'activo' &&
        (p.receptor === receptor || p.nom_receptor === receptor)
      )
    },

    // Pedidos completados
    pedidosCompletados: (state) => {
      return state.pedidosEnMemoria.filter(p => p.estado === 'completado')
    },

    // Pedidos con líneas servidas
    pedidosConServidos: (state) => {
      return state.pedidosEnMemoria.filter(p =>
        p.estado === 'activo' &&
        p.lineas?.some(l => l.servido === true)
      )
    },

    // Líneas servidas por receptor
    lineasServidasPorReceptor: (state) => (receptor) => {
      const pedidos = state.pedidosEnMemoria.filter(p =>
        p.estado === 'activo' &&
        (p.receptor === receptor || p.nom_receptor === receptor)
      )

      const lineasServidas = []
      pedidos.forEach(pedido => {
        if (pedido.lineas) {
          pedido.lineas.forEach(linea => {
            if (linea.servido === true) {
              lineasServidas.push({
                ...linea,
                pedido_info: {
                  mesa: pedido.mesa,
                  camarero: pedido.camarero,
                  hora: pedido.hora
                }
              })
            }
          })
        }
      })

      return lineasServidas
    },

    // Pedidos urgentes
    pedidosUrgentes: (state) => {
      return state.pedidosEnMemoria.filter(p =>
        p.estado === 'activo' && p.urgente === true
      )
    },

    // Líneas urgentes
    lineasUrgentes: (state) => {
      const pedidosActivos = state.pedidosEnMemoria.filter(p => p.estado === 'activo')
      const lineasUrgentes = []

      pedidosActivos.forEach(pedido => {
        if (pedido.lineas) {
          pedido.lineas.forEach(linea => {
            if (linea.urgente === true) {
              lineasUrgentes.push({
                ...linea,
                pedido_info: {
                  id: pedido.id,
                  pedido_id: pedido.pedido_id,
                  mesa: pedido.mesa,
                  camarero: pedido.camarero,
                  hora: pedido.hora,
                  receptor: pedido.nom_receptor || pedido.receptor
                }
              })
            }
          })
        }
      })

      return lineasUrgentes
    },

    // Agrupar pedidos activos por camarero
    pedidosPorCamarero: (state) => {
      const pedidosActivos = state.pedidosEnMemoria.filter(p => p.estado === 'activo')
      const agrupado = {}

      pedidosActivos.forEach(pedido => {
        const camarero = pedido.camarero || 'Sin camarero'
        if (!agrupado[camarero]) {
          agrupado[camarero] = []
        }
        agrupado[camarero].push(pedido)
      })

      return agrupado
    },

    // Agrupar pedidos activos por receptor
    pedidosAgrupadosPorReceptor: (state) => {
      const pedidosActivos = state.pedidosEnMemoria.filter(p => p.estado === 'activo')
      const agrupado = {}

      pedidosActivos.forEach(pedido => {
        const receptor = pedido.nom_receptor || pedido.receptor || 'Sin receptor'
        if (!agrupado[receptor]) {
          agrupado[receptor] = []
        }
        agrupado[receptor].push(pedido)
      })

      return agrupado
    },

    // Agrupar líneas por artículo (idart y descripción) - para ver cuántos del mismo artículo hay
    lineasAgrupadasPorArticulo: (state) => {
      const pedidosActivos = state.pedidosEnMemoria.filter(p => p.estado === 'activo')
      const agrupado = {}

      pedidosActivos.forEach(pedido => {
        if (pedido.lineas) {
          pedido.lineas.forEach(linea => {
            const key = `${linea.idart}-${linea.descripcion}`

            if (!agrupado[key]) {
              agrupado[key] = {
                idart: linea.idart,
                descripcion: linea.descripcion,
                cantidad: 0,
                lineas: [],
                pendientes: 0,
                servidas: 0
              }
            }

            agrupado[key].cantidad++
            agrupado[key].lineas.push({
              ...linea,
              pedido_info: {
                id: pedido.id,
                mesa: pedido.mesa,
                camarero: pedido.camarero,
                hora: pedido.hora,
                receptor: pedido.nom_receptor || pedido.receptor
              }
            })

            if (linea.servido) {
              agrupado[key].servidas++
            } else {
              agrupado[key].pendientes++
            }
          })
        }
      })

      return Object.values(agrupado)
    },

    // Vista principal: Agrupar por pedido > artículos (ordenados por pedido_id)
    // Muestra: pedidos urgentes (aunque estén servidos) + pedidos con líneas pendientes
    vistaPrincipal: (state) => (receptor = null) => {
      let pedidosActivos = state.pedidosEnMemoria.filter(p => {
        if (p.estado !== 'activo') return false

        // Mostrar si es urgente (sin importar estado servido)
        if (p.urgente === true || (p.lineas && p.lineas.some(l => l.urgente === true))) {
          return true
        }

        // Mostrar si tiene al menos una línea pendiente
        if (p.lineas && p.lineas.some(l => l.servido !== true)) {
          return true
        }

        return false
      })

      // Filtrar por receptor si se especifica
      if (receptor) {
        pedidosActivos = pedidosActivos.filter(p =>
          p.receptor === receptor || p.nom_receptor === receptor
        )
      }

      // Ordenar por pedido_id (del más pequeño al más grande)
      pedidosActivos.sort((a, b) => {
        const idA = a.pedido_id || a.id || 0
        const idB = b.pedido_id || b.id || 0
        return idA - idB
      })

      // Transformar cada pedido para agrupar sus líneas por artículo
      const pedidosTransformados = pedidosActivos.map(pedido => {
        const articulos = {}

        // Agrupar líneas por artículo
        if (pedido.lineas) {
          pedido.lineas.forEach(linea => {
            const artKey = `${linea.idart}-${linea.descripcion}`

            if (!articulos[artKey]) {
              articulos[artKey] = {
                idart: linea.idart,
                descripcion: linea.descripcion,
                lineas: [],
                cantidad: 0,
                servidas: 0,
                pendientes: 0
              }
            }

            articulos[artKey].cantidad++
            articulos[artKey].lineas.push(linea)

            if (linea.servido) {
              articulos[artKey].servidas++
            } else {
              articulos[artKey].pendientes++
            }
          })
        }

        return {
          id: pedido.id,
          pedido_id: pedido.pedido_id,
          mesa: pedido.mesa,
          camarero: pedido.camarero,
          hora: pedido.hora,
          receptor: pedido.nom_receptor || pedido.receptor,
          urgente: pedido.urgente,
          articulosArray: Object.values(articulos)
        }
      })

      return pedidosTransformados
    },

    // Vista de pedidos servidos: Agrupados por camarero
    vistaPrincipalServidos: (state) => (receptor = null) => {
      let pedidosServidos = state.pedidosEnMemoria.filter(p => {
        if (p.estado !== 'activo') return false

        // Debe tener líneas y TODAS deben estar servidas
        if (!p.lineas || p.lineas.length === 0) return false

        return p.lineas.every(l => l.servido === true)
      })

      // Filtrar por receptor si se especifica
      if (receptor) {
        pedidosServidos = pedidosServidos.filter(p =>
          p.receptor === receptor || p.nom_receptor === receptor
        )
      }

      // Primero, agrupar líneas por pedido_id (en caso de que haya múltiples registros)
      const pedidosConsolidados = {}

      pedidosServidos.forEach(pedido => {
        const key = pedido.pedido_id || pedido.id

        if (!pedidosConsolidados[key]) {
          pedidosConsolidados[key] = {
            id: pedido.id,
            pedido_id: pedido.pedido_id,
            mesa: pedido.mesa,
            camarero: pedido.camarero,
            hora: pedido.hora,
            receptor: pedido.nom_receptor || pedido.receptor,
            lineas: []
          }
        }

        // Agregar líneas (evitando duplicados por ID)
        if (pedido.lineas) {
          const lineasExistentes = new Set(pedidosConsolidados[key].lineas.map(l => l.id))
          pedido.lineas.forEach(linea => {
            if (!lineasExistentes.has(linea.id)) {
              pedidosConsolidados[key].lineas.push(linea)
            }
          })
        }
      })

      // Ahora agrupar por camarero
      const camareros = {}

      Object.values(pedidosConsolidados).forEach(pedido => {
        // Buscar el nombre del camarero en diferentes campos posibles
        const nombreCamarero = pedido.camarero || pedido.nom_camarero || pedido.usuario || pedido.vendedor || pedido.camarero_nombre || 'Sin camarero'

        if (!camareros[nombreCamarero]) {
          camareros[nombreCamarero] = {
            camarero: nombreCamarero,
            pedidosArray: []
          }
        }

        // Transformar pedido: agrupar líneas por artículo
        const articulos = {}

        if (pedido.lineas) {
          pedido.lineas.forEach(linea => {
            const artKey = `${linea.idart}-${linea.descripcion}`

            if (!articulos[artKey]) {
              articulos[artKey] = {
                idart: linea.idart,
                descripcion: linea.descripcion,
                lineas: [],
                cantidad: 0,
                servidas: 0
              }
            }

            articulos[artKey].cantidad++
            articulos[artKey].lineas.push(linea)
            articulos[artKey].servidas++
          })
        }

        camareros[nombreCamarero].pedidosArray.push({
          id: pedido.id,
          pedido_id: pedido.pedido_id,
          mesa: pedido.mesa,
          camarero: pedido.camarero,
          hora: pedido.hora,
          receptor: pedido.receptor,
          articulosArray: Object.values(articulos)
        })
      })

      // Convertir a array y ordenar camareros alfabéticamente
      const result = Object.values(camareros).sort((a, b) =>
        a.camarero.localeCompare(b.camarero)
      )

      // Ordenar pedidos dentro de cada camarero por pedido_id
      result.forEach(camarero => {
        camarero.pedidosArray.sort((a, b) => {
          const idA = a.pedido_id || a.id || 0
          const idB = b.pedido_id || b.id || 0
          return idA - idB
        })
      })

      return result
    },

    // Estadísticas
    estadisticas: (state) => {
      const activos = state.pedidosEnMemoria.filter(p => p.estado === 'activo')
      const completados = state.pedidosEnMemoria.filter(p => p.estado === 'completado')

      let lineasActivas = 0
      let lineasServidas = 0
      let lineasUrgentes = 0
      let pedidosUrgentes = 0

      activos.forEach(p => {
        if (p.urgente === true) {
          pedidosUrgentes++
        }

        if (p.lineas) {
          lineasActivas += p.lineas.length
          lineasServidas += p.lineas.filter(l => l.servido === true).length
          lineasUrgentes += p.lineas.filter(l => l.urgente === true).length
        }
      })

      return {
        total: state.pedidosEnMemoria.length,
        activos: activos.length,
        completados: completados.length,
        lineasActivas,
        lineasServidas,
        lineasPendientes: lineasActivas - lineasServidas,
        pedidosUrgentes,
        lineasUrgentes
      }
    },
    getEmpresaData: (state) => state.empresa,
    getDeviceUID: (state) => state.deviceUID,
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



    async getReceptores() {
      try {
        const response = await API.get_listado()
        this.receptores = response || []
      } catch (error) {
        this.setError(`Error al obtener receptores: ${error.message}`)
        console.error('Error al obtener receptores:', error)
      }
    },

    // Verificar salud del servidor
    async checkServerHealth(serverUrl = null) {
      try {
        const response = await API.checkHealth(serverUrl)
        return response
      } catch (error) {
        console.error('Error al verificar salud del servidor:', error)
        return { success: false, error: error.message }
      }
    },

    // Crear UID del dispositivo con alias
    async createDeviceUID(alias) {
      try {
        const uid = await API.create_uid(alias)
        this.deviceUID = uid
        return uid
      } catch (error) {
        this.setError(`Error al crear UID del dispositivo: ${error.message}`)
        console.error('Error al crear UID:', error)
        throw error
      }
    },

    async getListado() {
      try {
        const response = await API.get_listado()

        // Guardar el UID en el estado si está disponible
        if (localStorage.deviceUID) {
          this.deviceUID = localStorage.deviceUID
        }

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
      this.setWsConnected(true)
      // Resetear reconexión cuando se conecta exitosamente
      this.resetReconnection()
    },

    onDisconnect() {
      this.setWsConnected(false)
      // NO activar reconexión automática para WebSocket
      // Los WebSockets tienen su propio sistema de reconexión
    },

    // Inicializar la base de datos y cargar todo en memoria
    async inicializarDB() {
      if (this.dbInitialized) return

      try {
        await pedidosDB.init()
        await this.cargarTodosLosPedidos()
        this.dbInitialized = true

        // Configurar el sistema de audio para notificaciones
        this.setupAudioUnlock()
      } catch (error) {
        console.error('Error al inicializar DB:', error)
        throw error
      }
    },

    // Método auxiliar para asegurar que el elemento de audio existe
    ensureAudioElement() {
      let audioElement = document.getElementById('notificationAudio');

      if (!audioElement) {
        audioElement = document.createElement('audio');
        audioElement.id = 'notificationAudio';
        audioElement.preload = 'auto';
        audioElement.volume = 0.7;
        audioElement.src = audioMario;
        document.body.appendChild(audioElement);
      }

      return audioElement;
    },

    // Solicitar autorización de audio de forma proactiva
    async requestAudioPermission() {
      try {
        // Asegurar que el elemento de audio existe
        const audioElement = this.ensureAudioElement();

        // Mostrar diálogo personalizado para solicitar permisos de audio
        return new Promise((resolve) => {
          // Crear modal de solicitud de audio
          const modal = document.createElement('div');
          modal.id = 'audio-permission-modal';
          modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          `;

          const dialog = document.createElement('div');
          dialog.style.cssText = `
            background: white;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
            max-width: 400px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
          `;

          dialog.innerHTML = `
            <div style="font-size: 48px; margin-bottom: 20px;">🔊</div>
            <h2 style="margin: 0 0 15px 0; color: #333;">Habilitar Sonido de Notificaciones</h2>
            <p style="color: #666; margin-bottom: 25px; line-height: 1.4;">
              Para recibir alertas sonoras cuando lleguen nuevos pedidos, necesitamos tu autorización para reproducir audio.
            </p>
            <div style="display: flex; gap: 10px; justify-content: center;">
              <button id="enable-audio" style="
                background: #4CAF50;
                color: white;
                border: none;
                padding: 12px 24px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 16px;
                font-weight: 500;
              ">Habilitar Sonido</button>
              <button id="skip-audio" style="
                background: #f5f5f5;
                color: #333;
                border: 1px solid #ddd;
                padding: 12px 24px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 16px;
              ">Omitir</button>
            </div>
          `;

          modal.appendChild(dialog);
          document.body.appendChild(modal);

          // Manejar clic en "Habilitar Sonido"
          document.getElementById('enable-audio').onclick = async () => {
            try {
              // Intentar reproducir audio para obtener permisos
              const playPromise = audioElement.play();

              if (playPromise !== undefined) {
                await playPromise;
                audioElement.pause();
                audioElement.currentTime = 0;
                console.log('🔊 Audio habilitado correctamente');

                // Marcar como habilitado en localStorage
                localStorage.audioEnabled = 'true';
              }
            } catch (error) {
              console.warn('No se pudo habilitar el audio:', error);
            }

            document.body.removeChild(modal);
            resolve(true);
          };

          // Manejar clic en "Omitir"
          document.getElementById('skip-audio').onclick = () => {
            localStorage.audioEnabled = 'false';
            document.body.removeChild(modal);
            resolve(false);
          };
        });

      } catch (error) {
        console.error('Error al solicitar permisos de audio:', error);
        return false;
      }
    },

    // Cargar todos los pedidos en memoria
    async cargarTodosLosPedidos() {
      try {
        const pedidos = await pedidosDB.obtenerTodos()
        this.pedidosEnMemoria = pedidos

        // Actualizar items con pedidos activos para compatibilidad
        this.items = pedidos.filter(p => p.estado === 'activo')
      } catch (error) {
        console.error('Error al cargar pedidos:', error)
        throw error
      }
    },

    async recepcionPedido(pedido) {

      // NORMALIZAR EL PEDIDO ANTES DE PROCESARLO
      const pedidoNormalizado = {
        ...pedido,
        estado: 'activo', // Asegurar que el pedido tenga estado activo
        timestamp: Date.now(), // Añadir timestamp
        lineas: pedido.lineas.map(linea => ({
          ...linea,
          servido: linea.servido || false,
          urgente: linea.urgente || false,
        }))
      }

      // Comprobar si el pedido ya existe en memoria
      const pedidoExistente = this.pedidosEnMemoria.find(p => p.pedido_id === pedidoNormalizado.pedido_id)

      if (!pedidoExistente) {
        try {
          const idGuardado = await pedidosDB.guardarPedido(toRaw(pedidoNormalizado))
          const pedidoConId = { ...pedidoNormalizado, id: idGuardado }

          this.pedidosEnMemoria.push(pedidoConId)

          // Actualizar items para UI (pedidos activos)
          this.items = this.pedidosEnMemoria.filter(p => p.estado === 'activo')

          // Forzar reactividad recreando el array
          this.pedidosEnMemoria = [...this.pedidosEnMemoria]

          // Reproducir sonido para nuevo pedido
          this.playNotificationSound();

          // Cerrar vista de servidos automáticamente para mostrar el nuevo pedido
          this.cerrarServidosAutomatico();

          if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(`Nuevo pedido: Mesa ${pedidoConId.mesa}`, {
              body: `${pedidoConId.lineas?.length || 0} línea(s) - ${pedidoConId.nom_receptor || pedidoConId.receptor}`,
              icon: '/img/icons/android-chrome-192x192.png',
              tag: `pedido-${pedidoConId.pedido_id || Date.now()}`
            })
          }
        } catch (error) {
          console.error('Error al guardar el pedido:', error)
        }
      } else {
        // Lógica para actualizar pedidos existentes
        // (Aquí se podría implementar la lógica de actualización si fuera necesario)
      }
    },

    // Marcar pedido como completado
    async completarPedido(pedidoId) {
      try {
        // Actualizar en IndexedDB
        await pedidosDB.actualizarEstado(pedidoId, 'completado')

        // Actualizar en memoria
        const pedido = this.pedidosEnMemoria.find(p => p.id === pedidoId)
        if (pedido) {
          pedido.estado = 'completado'
          pedido.updated_at = Date.now()
        }

        // Remover de items (solo pedidos activos)
        this.items = this.items.filter(p => p.id !== pedidoId)
      } catch (error) {
        console.error('Error al completar pedido:', error)
        throw error
      }
    },

    // Cancelar pedido
    async cancelarPedido(pedidoId) {
      try {
        await pedidosDB.actualizarEstado(pedidoId, 'cancelado')

        const pedido = this.pedidosEnMemoria.find(p => p.id === pedidoId)
        if (pedido) {
          pedido.estado = 'cancelado'
          pedido.updated_at = Date.now()
        }

        this.items = this.items.filter(p => p.id !== pedidoId)
      } catch (error) {
        console.error('Error al cancelar pedido:', error)
        throw error
      }
    },

    // Actualizar estado de una línea específica
    async actualizarEstadoLinea(pedidoId, lineaId, nuevoEstado) {
      try {
        // Actualizar en IndexedDB
        await pedidosDB.actualizarEstadoLinea(pedidoId, lineaId, nuevoEstado)

        // Actualizar en memoria
        const pedido = this.pedidosEnMemoria.find(p => p.id === pedidoId)
        if (pedido && pedido.lineas) {
          const linea = pedido.lineas.find(l => l.id === lineaId)
          if (linea) {
            linea.estado = nuevoEstado
          }
        }
      } catch (error) {
        console.error('Error al actualizar estado de línea:', error)
        throw error
      }
    },

    // Marcar líneas como servidas
    async servirLineas(idsLineas) {
      try {

        let lineasActualizadas = 0

        // Recorrer todos los pedidos activos
        for (const pedido of this.pedidosEnMemoria) {
          if (pedido.estado === 'activo' && pedido.lineas) {
            let pedidoModificado = false

            // Buscar líneas a marcar como servidas
            for (const linea of pedido.lineas) {
              if (idsLineas.includes(linea.id)) {
                linea.servido = true
                linea.urgente = false  // Quitar urgente al servir
                lineasActualizadas++
                pedidoModificado = true
              }
            }

            // Si todas las líneas están servidas, quitar urgente del pedido
            if (pedidoModificado) {
              const todasServidas = pedido.lineas.every(l => l.servido === true)
              if (todasServidas) {
                pedido.urgente = false
              }

              await pedidosDB.actualizarPedido(pedido.id, toRaw(pedido))
            }
          }
        }

        // Forzar reactividad recreando el array
        this.pedidosEnMemoria = [...this.pedidosEnMemoria]

        return lineasActualizadas
      } catch (error) {
        console.error('Error al marcar líneas como servidas:', error)
        throw error
      }
    },

    // Desmarcar líneas como servidas (para recuperar de la vista de servidos)
    async desmarcarServido(idsLineas) {
      try {

        let lineasDesmarcadas = 0

        // Buscar en todos los pedidos
        for (const pedido of this.pedidosEnMemoria) {
          if (pedido.lineas) {
            let cambios = false

            pedido.lineas.forEach(linea => {
              if (idsLineas.includes(linea.id)) {
                linea.servido = false
                cambios = true
                lineasDesmarcadas++
              }
            })

            // Si hubo cambios, actualizar en IndexedDB
            if (cambios) {
              await pedidosDB.actualizarPedido(pedido.id, toRaw(pedido))
            }
          }
        }

        // Forzar reactividad recreando el array
        this.pedidosEnMemoria = [...this.pedidosEnMemoria]

        return lineasDesmarcadas
      } catch (error) {
        console.error('Error al desmarcar servido:', error)
        throw error
      }
    },

    // Borrar líneas específicas (cobradas/borradas)
    async borrarLineas(idsLineas) {
      try {
        let lineasBorradas = 0
        const pedidosVacios = []

        // Recorrer todos los pedidos activos
        for (const pedido of this.pedidosEnMemoria) {
          if (pedido.estado === 'activo' && pedido.lineas) {
            const lineasOriginales = pedido.lineas.length

            // Filtrar las líneas que no están en la lista de IDs a borrar
            pedido.lineas = pedido.lineas.filter(linea => !idsLineas.includes(linea.id))

            const lineasEliminadas = lineasOriginales - pedido.lineas.length

            if (lineasEliminadas > 0) {
              lineasBorradas += lineasEliminadas

              // Si el pedido se quedó sin líneas, marcarlo para completar
              if (pedido.lineas.length === 0) {
                pedidosVacios.push(pedido.id)
              } else {
                // Actualizar el pedido en IndexedDB
                await pedidosDB.actualizarPedido(pedido.id, toRaw(pedido))
              }
            }
          }
        }

        // Eliminar completamente pedidos que se quedaron sin líneas
        for (const pedidoId of pedidosVacios) {
          await pedidosDB.eliminar(pedidoId)
        }

        // Eliminar pedidos vacíos de memoria
        this.pedidosEnMemoria = this.pedidosEnMemoria.filter(p => !pedidosVacios.includes(p.id))

        // Actualizar items (pedidos activos para la UI)
        this.items = this.pedidosEnMemoria.filter(p => p.estado === 'activo')

        return { lineasBorradas, pedidosEliminados: pedidosVacios.length }
      } catch (error) {
        console.error('Error al borrar líneas:', error)
        throw error
      }
    },

    // Marcar líneas o pedido completo como urgente
    async marcarUrgente(idsLineas, pedidoId = null) {
      try {

        let lineasMarcadas = 0
        let pedidosMarcados = 0

        // Recorrer todos los pedidos activos
        for (const pedido of this.pedidosEnMemoria) {
          if (pedido.estado === 'activo' && pedido.lineas) {
            let pedidoModificado = false

            // Si se especifica pedido_id, marcar todas las líneas de ese pedido
            if (pedidoId && pedido.pedido_id === pedidoId) {
              pedido.urgente = true
              pedido.lineas.forEach(linea => {
                linea.urgente = true
                // Resetear servido para que vuelva a mostrarse
                linea.servido = false
                lineasMarcadas++
              })
              pedidosMarcados++
              pedidoModificado = true
            } else {
              // Marcar solo líneas específicas
              pedido.lineas.forEach(linea => {
                if (idsLineas.includes(linea.id)) {
                  linea.urgente = true
                  // Resetear servido para que vuelva a mostrarse
                  linea.servido = false
                  lineasMarcadas++
                  pedidoModificado = true

                  // Si alguna línea es urgente, marcar el pedido también
                  pedido.urgente = true
                }
              })
            }

            // Actualizar el pedido en IndexedDB si se modificó
            if (pedidoModificado) {
              await pedidosDB.actualizarPedido(pedido.id, toRaw(pedido))
            }
          }
        }

        // Forzar reactividad recreando el array
        this.pedidosEnMemoria = [...this.pedidosEnMemoria]

        // Actualizar items (pedidos activos para la UI)
        this.items = this.pedidosEnMemoria.filter(p => p.estado === 'activo')

        // Reproducir sonido para pedido urgente
        if (lineasMarcadas > 0 || pedidosMarcados > 0) {
          this.playNotificationSound();
        }

        // Mostrar notificación de urgencia
        if ('Notification' in window && Notification.permission === 'granted') {
          const pedidoUrgente = this.pedidosEnMemoria.find(p =>
            p.pedido_id === pedidoId || p.lineas?.some(l => idsLineas.includes(l.id))
          )

          if (pedidoUrgente) {
            new Notification('¡URGENTE!', {
              body: `${pedidoUrgente.mesa || 'Mesa'} - ${lineasMarcadas} línea(s) marcadas como urgentes`,
              icon: '/img/icons/android-chrome-192x192.png',
              tag: `urgente-${Date.now()}`,
              requireInteraction: true  // Mantener notificación hasta que se cierre
            })
          }
        }

        return { lineasMarcadas, pedidosMarcados }
      } catch (error) {
        console.error('Error al marcar como urgente:', error)
        throw error
      }
    },

    // Eliminar pedido
    async eliminarPedido(pedidoId) {
      try {
        await pedidosDB.eliminar(pedidoId)

        // Eliminar de memoria
        this.pedidosEnMemoria = this.pedidosEnMemoria.filter(p => p.id !== pedidoId)
        this.items = this.items.filter(p => p.id !== pedidoId)
      } catch (error) {
        console.error('Error al eliminar pedido:', error)
        throw error
      }
    },

    // Limpiar pedidos antiguos
    async limpiarPedidosAntiguos(diasAtras = 7) {
      try {
        const eliminados = await pedidosDB.limpiarAntiguos(diasAtras)

        // Recargar todo en memoria
        await this.cargarTodosLosPedidos()

        return eliminados
      } catch (error) {
        console.error('Error al limpiar pedidos antiguos:', error)
        throw error
      }
    },

    // Sincronizar pedidos con el servidor al conectar WebSocket
    async sincronizarPedidos(receptorInput) {
      try {

        // Verificar que el receptor esté definido
        if (!receptorInput) {
          console.error('Error: receptor es undefined o null')
          return { pedidos: 0, lineasEliminadas: 0 }
        }

        // Determinar nomimp y nombre (para soporte de objeto o string)
        const nomimp = typeof receptorInput === 'object' ? receptorInput.nomimp : receptorInput;
        const nombreReceptor = typeof receptorInput === 'object' ? receptorInput.Nombre : null;

        // Obtener pedidos locales de este receptor
        // PRIORIZAR el filtrado por nom_receptor si está disponible
        const pedidosLocales = this.pedidosEnMemoria.filter(p => {
          if (p.estado !== 'activo') return false;
          
          // Si tenemos nom_receptor disponible, filtrar por él para evitar duplicados
          if (nombreReceptor && p.nom_receptor) {
            return p.nom_receptor === nombreReceptor;
          }
          
          // Fallback: usar receptor (nomimp) solo si no hay nom_receptor
          return p.receptor === nomimp;
        })

        // Enviar al servidor los pedidos que tenemos
        const pedidosParaEnviar = pedidosLocales.map(p => ({
          id: p.id,
          pedido_id: p.pedido_id,
          lineas: p.lineas?.map(l => l.id) || []
        }))

        const response = await API.sincronizar_pedidos(nomimp, pedidosParaEnviar, nombreReceptor)

        if (response) {
          // Procesar pedidos recibidos
          if (response.pedidos && Array.isArray(response.pedidos)) {

            for (const pedido of response.pedidos) {
              // Eliminar líneas duplicadas dentro del mismo pedido
              if (pedido.lineas && Array.isArray(pedido.lineas)) {
                const lineasUnicas = new Map()
                pedido.lineas.forEach(linea => {
                  if (!lineasUnicas.has(linea.id)) {
                    lineasUnicas.set(linea.id, linea)
                  }
                })
                pedido.lineas = Array.from(lineasUnicas.values())

                // Marcar todas las líneas como no servidas y no urgentes
                pedido.lineas.forEach(linea => {
                  linea.servido = false
                  linea.urgente = false
                })
              }

              // Asegurarse de que el pedido tenga el receptor correcto
              if (!pedido.receptor && !pedido.nom_receptor) {
                pedido.receptor = nomimp
              }

              // Agregar el pedido (op: "pedido" indica que es nuevo o actualizado)
              if (pedido.op === 'pedido') {
                // Filtrar por nombre de receptor PRIORITARIAMENTE para evitar duplicados
                let esParaEste = false;
                
                // Si ambos tienen nom_receptor, comparar por nom_receptor
                if (pedido.nom_receptor && nombreReceptor) {
                  esParaEste = pedido.nom_receptor === nombreReceptor;
                } 
                // Si el pedido no tiene nom_receptor pero nosotros sí tenemos nombreReceptor,
                // no es para este receptor (evita duplicados)
                else if (!pedido.nom_receptor && nombreReceptor) {
                  esParaEste = false;
                } 
                // Solo usar nomimp si ninguno tiene nom_receptor
                else {
                  esParaEste = pedido.receptor === nomimp;
                }

                if (esParaEste) {
                  await this.recepcionPedido(pedido)
                }
              }
            }
          }

          // Procesar líneas a eliminar (rm = IDs de líneas cobradas/borradas)
          if (response.rm && Array.isArray(response.rm)) {
            await this.borrarLineas(response.rm)
          }

          // Forzar actualización de items para que Vue reactive los cambios
          this.items = this.pedidosEnMemoria.filter(p => p.estado === 'activo')

          return {
            pedidos: response.pedidos?.length || 0,
            lineasEliminadas: response.rm?.length || 0
          }
        }
      } catch (error) {
        console.error('Error al sincronizar pedidos:', error)
        // No lanzar error para no interrumpir la conexión WebSocket
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

      this.setReconnecting(true)

      this.reconnectTimer = setTimeout(() => {
        this.attemptReconnection()
      }, delay)
    },

    async attemptReconnection() {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        this.setReconnecting(false)
        return
      }

      this.setReconnectAttempts(this.reconnectAttempts + 1)

      try {
        // SOLO intentar reconectar HTTP/API, NO WebSocket
        // El WebSocket se reconecta automáticamente desde HomeView
        if (!this.isHttpConnected) {
          await this.getListado()
        }

        // Si HTTP funciona, resetear contador y parar timer
        if (this.isHttpConnected) {
          this.resetReconnection()
        } else {
          // Si falla, programar siguiente intento
          this.startReconnectionTimer()
        }
      } catch (error) {
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
    },

    // Iniciar reconexión cuando se detecte pérdida de conexión
    handleConnectionLoss() {
      if (!this.isReconnecting && this.reconnectAttempts < this.maxReconnectAttempts) {
        this.startReconnectionTimer()
      }
    },

    // Reproducir sonido de notificación
    playNotificationSound() {
      // Verificar si el usuario ha habilitado el audio
      if (localStorage.audioEnabled !== 'true') {
        return; // Audio deshabilitado por el usuario
      }

      try {
        // Asegurar que el elemento de audio existe
        const audioElement = this.ensureAudioElement();

        // Intentar reproducir el audio
        const playPromise = audioElement.play();

        if (playPromise !== undefined) {
          playPromise.then(() => {
            // Audio reproducido correctamente
          }).catch(error => {
            if (error.name === 'NotAllowedError') {
              console.warn('⚠️ Audio bloqueado. Solicitar permisos de nuevo.');
              // Resetear permisos para que vuelva a preguntar
              localStorage.audioEnabled = 'false';
            } else {
              console.error('Error al reproducir sonido:', error);
            }
          });
        }
      } catch (error) {
        console.error('Error en playNotificationSound:', error);
      }
    },

    // Habilitar audio después de la primera interacción del usuario (respaldo)
    setupAudioUnlock() {
      // Solo configurar si el audio está habilitado pero falló la reproducción
      if (localStorage.audioEnabled === 'true') {
        const self = this; // Capturar el contexto
        const unlockAudio = () => {
          // Asegurar que el elemento de audio existe
          const audioElement = self.ensureAudioElement();

          if (audioElement) {
            const playPromise = audioElement.play();

            if (playPromise !== undefined) {
              playPromise.then(() => {
                audioElement.pause();
                audioElement.currentTime = 0;
              }).catch(() => {
                // Falló, seguir intentando
              });
            }
          }

          // Remover los event listeners después del primer uso
          document.removeEventListener('click', unlockAudio, true);
          document.removeEventListener('touchstart', unlockAudio, true);
        };

        // Escuchar cualquier interacción del usuario
        document.addEventListener('click', unlockAudio, true);
        document.addEventListener('touchstart', unlockAudio, true);
      }
    },

    // Registrar callback para cerrar vista de servidos
    registrarCerrarServidos(callback) {
      this.cerrarServidosCallback = callback;
    },

    // Cerrar vista de servidos si está abierta
    cerrarServidosAutomatico() {
      if (this.cerrarServidosCallback) {
        this.cerrarServidosCallback();
      }
    }
  }
})
