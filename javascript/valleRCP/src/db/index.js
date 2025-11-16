// Base de datos IndexedDB para pedidos
// Usar nombre diferente a Workbox para evitar conflictos
const DB_NAME = 'ValleRCP_Pedidos'
const DB_VERSION = 1
const STORE_PEDIDOS = 'pedidos'

class PedidosDB {
  constructor() {
    this.db = null
    this.opening = null // Promise para evitar múltiples aperturas simultáneas
  }

  // Inicializar la base de datos con singleton pattern
  async init() {
    // Si ya hay una conexión abierta, devolverla
    if (this.db && !this.db.objectStoreNames) {
      this.db = null // La conexión está cerrada, resetear
    }
    
    if (this.db) {
      return this.db
    }
    
    // Si ya está abriéndose, esperar a que termine
    if (this.opening) {
      return this.opening
    }
    
    // Crear nueva promesa de apertura
    this.opening = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION)

      request.onerror = () => {
        console.error('Error al abrir IndexedDB:', request.error)
        this.opening = null
        reject(request.error)
      }

      request.onsuccess = () => {
        this.db = request.result
        this.opening = null
        
        // Manejar cierre inesperado
        this.db.onclose = () => {
          console.warn('IndexedDB cerrada inesperadamente')
          this.db = null
        }
        
        // Manejar error de conexión
        this.db.onerror = (event) => {
          console.error('Error en conexión IndexedDB:', event.target.error)
        }
        
        console.log('IndexedDB inicializada correctamente')
        resolve(this.db)
      }

      request.onupgradeneeded = (event) => {
        const db = event.target.result

        // Crear object store para pedidos si no existe
        if (!db.objectStoreNames.contains(STORE_PEDIDOS)) {
          const pedidosStore = db.createObjectStore(STORE_PEDIDOS, { 
            keyPath: 'id', 
            autoIncrement: true 
          })

          // Índices para búsquedas eficientes
          pedidosStore.createIndex('receptor', 'receptor', { unique: false })
          pedidosStore.createIndex('pedido_id', 'pedido_id', { unique: false })
          pedidosStore.createIndex('timestamp', 'timestamp', { unique: false })
          pedidosStore.createIndex('estado', 'estado', { unique: false })

          console.log('Object store "pedidos" creado con índices')
        }
      }
      
      request.onblocked = () => {
        console.warn('La apertura de IndexedDB está bloqueada. Cierra otras pestañas que usen esta aplicación.')
      }
    })
    
    return this.opening
  }
  
  // Cerrar la conexión a la base de datos
  close() {
    if (this.db) {
      this.db.close()
      this.db = null
      console.log('Conexión IndexedDB cerrada')
    }
  }

  // Guardar un nuevo pedido
  async guardarPedido(pedido) {
    try {
      await this.init()
      
      return new Promise((resolve, reject) => {
        const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
        const store = transaction.objectStore(STORE_PEDIDOS)

      // Hacer una copia profunda limpia del pedido
      const pedidoLimpio = JSON.parse(JSON.stringify(pedido))

      // Agregar timestamp si no existe
      const pedidoConTimestamp = {
        ...pedidoLimpio,
        timestamp: pedidoLimpio.timestamp || Date.now(),
        estado: pedidoLimpio.estado || 'activo' // activo, completado, cancelado
      }

      // Extraer pedido_id de las líneas (todas deberían tener el mismo)
      if (pedidoLimpio.lineas && pedidoLimpio.lineas.length > 0) {
        pedidoConTimestamp.pedido_id = pedidoLimpio.lineas[0].pedido_id
      }

      const request = store.add(pedidoConTimestamp)

      request.onsuccess = () => {
        console.log('Pedido guardado en IndexedDB:', request.result)
        resolve(request.result)
      }

      request.onerror = () => {
        console.error('Error al guardar pedido:', request.error)
        reject(request.error)
      }
    })
    } catch (error) {
      console.error('Error en guardarPedido:', error)
      throw error
    }
  }

  // Obtener todos los pedidos
  async obtenerTodos() {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readonly')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const request = store.getAll()

      request.onsuccess = () => {
        resolve(request.result)
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }

  // Obtener pedidos por receptor
  async obtenerPorReceptor(receptor) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readonly')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const index = store.index('receptor')
      const request = index.getAll(receptor)

      request.onsuccess = () => {
        resolve(request.result)
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }

  // Obtener pedidos activos
  async obtenerActivos() {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readonly')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const index = store.index('estado')
      const request = index.getAll('activo')

      request.onsuccess = () => {
        resolve(request.result)
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }

  // Actualizar estado de un pedido
  async actualizarEstado(id, nuevoEstado) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const getRequest = store.get(id)

      getRequest.onsuccess = () => {
        const pedido = getRequest.result
        if (pedido) {
          pedido.estado = nuevoEstado
          pedido.updated_at = Date.now()

          const updateRequest = store.put(pedido)

          updateRequest.onsuccess = () => {
            console.log('Estado del pedido actualizado:', id, nuevoEstado)
            resolve(updateRequest.result)
          }

          updateRequest.onerror = () => {
            reject(updateRequest.error)
          }
        } else {
          reject(new Error('Pedido no encontrado'))
        }
      }

      getRequest.onerror = () => {
        reject(getRequest.error)
      }
    })
  }

  // Actualizar un pedido completo
  async actualizarPedido(id, pedidoActualizado) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
      const store = transaction.objectStore(STORE_PEDIDOS)

      // Hacer una copia profunda limpia del pedido para evitar referencias circulares
      // y objetos Proxy de Vue que no son clonables
      const pedidoLimpio = JSON.parse(JSON.stringify(pedidoActualizado))
      
      const pedidoConId = {
        ...pedidoLimpio,
        id,
        updated_at: Date.now()
      }

      const updateRequest = store.put(pedidoConId)

      updateRequest.onsuccess = () => {
        console.log('Pedido actualizado completamente:', id)
        resolve(updateRequest.result)
      }

      updateRequest.onerror = () => {
        console.error('Error al actualizar pedido:', updateRequest.error)
        reject(updateRequest.error)
      }
    })
  }

  // Actualizar estado de una línea específica
  async actualizarEstadoLinea(pedidoId, lineaId, nuevoEstado) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const getRequest = store.get(pedidoId)

      getRequest.onsuccess = () => {
        const pedido = getRequest.result
        if (pedido && pedido.lineas) {
          const linea = pedido.lineas.find(l => l.id === lineaId)
          if (linea) {
            linea.estado = nuevoEstado
            pedido.updated_at = Date.now()

            const updateRequest = store.put(pedido)

            updateRequest.onsuccess = () => {
              console.log('Estado de línea actualizado:', lineaId, nuevoEstado)
              resolve(updateRequest.result)
            }

            updateRequest.onerror = () => {
              reject(updateRequest.error)
            }
          } else {
            reject(new Error('Línea no encontrada'))
          }
        } else {
          reject(new Error('Pedido no encontrado'))
        }
      }

      getRequest.onerror = () => {
        reject(getRequest.error)
      }
    })
  }

  // Eliminar un pedido
  async eliminar(id) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const request = store.delete(id)

      request.onsuccess = () => {
        console.log('Pedido eliminado:', id)
        resolve()
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }

  // Limpiar pedidos antiguos (más de X días)
  async limpiarAntiguos(diasAtras = 7) {
    await this.init()

    const fechaLimite = Date.now() - (diasAtras * 24 * 60 * 60 * 1000)

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readwrite')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const index = store.index('timestamp')
      const request = index.openCursor(IDBKeyRange.upperBound(fechaLimite))

      let eliminados = 0

      request.onsuccess = (event) => {
        const cursor = event.target.result
        if (cursor) {
          cursor.delete()
          eliminados++
          cursor.continue()
        } else {
          console.log(`Pedidos antiguos eliminados: ${eliminados}`)
          resolve(eliminados)
        }
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }

  // Obtener un pedido por ID
  async obtenerPorId(id) {
    await this.init()

    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([STORE_PEDIDOS], 'readonly')
      const store = transaction.objectStore(STORE_PEDIDOS)
      const request = store.get(id)

      request.onsuccess = () => {
        resolve(request.result)
      }

      request.onerror = () => {
        reject(request.error)
      }
    })
  }
}

// Exportar instancia única (singleton)
export const pedidosDB = new PedidosDB()

export default pedidosDB
