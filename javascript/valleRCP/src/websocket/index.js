class Timer {
    constructor(callback, timerCalc) {
        this.callback = callback;
        this.timerCalc = timerCalc;
        this.timer = null;
        this.tries = 0;
    }

    reset() {
        this.tries = 0
        clearTimeout(this.timer)
    }

    scheduleTimeout() {
        clearTimeout(this.timer)
        this.timer = setTimeout(() => {
            this.tries = this.tries + 1
            this.callback()
        }, this.timerCalc(this.tries + 1))
    }
}

// Función para determinar el protocolo WebSocket correcto
const getWebSocketProtocol = (server) => {
    // Si el servidor ya incluye el protocolo, lo adaptamos
    if (server.startsWith('https://')) {
        return server.replace('https://', 'wss://');
    }
    if (server.startsWith('http://')) {
        return server.replace('http://', 'ws://');
    }
    if (server.startsWith('wss://') || server.startsWith('ws://')) {
        return server;
    }

    // Si estamos en un entorno HTTPS, usamos WSS por defecto
    if (window.location.protocol === 'https:') {
        return `wss://${server}`;
    }

    // Si el servidor contiene el puerto 443 o palabras clave de HTTPS, usamos WSS
    if (server.includes(':443') || server.includes('ssl') || server.includes('secure')) {
        return `wss://${server}`;
    }

    // Por defecto, usamos WS
    return `ws://${server}`;
};

export default class VWebsocket {
    static activeSockets = {}; // Registro de sockets activos

    constructor(server, receptor, store) {
        // Usar el nombre del receptor en minúsculas como identificador
        const receptorKey = receptor.Nombre.toLowerCase();

        // Verificar si ya existe una conexión para este receptor
        const existingSocket = VWebsocket.activeSockets[receptorKey];
        if (existingSocket) {
            console.warn(`⚠️ Ya existe una conexión para el receptor: ${receptor.Nombre}. Cerrando la anterior.`);
            existingSocket.disconnect();
            delete VWebsocket.activeSockets[receptorKey];
        }

        const wsProtocol = getWebSocketProtocol(server);
        this.socketUrl = `${wsProtocol}/ws/comunicacion/${receptorKey}`;
        this.customSocket = null;
        this.store = store;
        this.receptor = receptor;
        this.receptorKey = receptorKey; // Guardar para usarlo después
        this.reconnectTimer = new Timer(() => {
            this.disconnect();
            this.connect();
        }, this.reconnectAfterMs);

        // Registrar la nueva instancia
        VWebsocket.activeSockets[receptorKey] = this;
    }

    reconnectAfterMs(tries) {
        return [1000, 2000, 5000, 10000][tries - 1] || 10000;
    }

    connect() {
        this.reconnectTimer.reset();

        if (this.customSocket) this.disconnect();

        this.customSocket = new WebSocket(this.socketUrl);

        this.customSocket.onopen = async (event) => {
            this.reconnectTimer.reset();
            this.store.onConnect();

            // Sincronizar pedidos al conectar
            try {
                const result = await this.store.sincronizarPedidos(this.receptor);
            } catch (error) {
                console.error('Error en sincronización inicial:', error);
            }
        };

        this.customSocket.onclose = (event) => {
            this.store.onDisconnect();
            if (event.code !== 1000) {
                this.reconnectTimer.scheduleTimeout();
            }
            // Eliminar del registro al cerrar la conexión
            delete VWebsocket.activeSockets[this.receptorKey];
        };

        this.customSocket.onerror = (event) => {
            console.error(`Error en WebSocket para receptor: ${this.receptor.Nombre}`, event);
            this.store.onDisconnect();
        };

        this.customSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                console.log('WebSocket received:', data);
                // Extraer el mensaje (puede estar anidado)
                const mensaje = typeof data.message === 'string'
                    ? JSON.parse(data.message)
                    : data.message || data;

                // Verificar que el mensaje sea para este receptor
                // Comparación estricta por nombre de receptor (case-insensitive)
                // servidor.receptor == store.receptor.Nombre.toLowerCase()
                let esParaEsteReceptor = false;

                if (this.receptor.Nombre) {
                    const incomingReceptor = mensaje.receptor ? mensaje.receptor.toString().toLowerCase() : '';
                    const localReceptor = this.receptor.Nombre.toString().toLowerCase();
                    esParaEsteReceptor = incomingReceptor === localReceptor;
                } else {
                    // Fallback solo si no tenemos Nombre local (caso raro)
                    esParaEsteReceptor = mensaje.receptor === this.receptor.nomimp;
                }

                // Manejar diferentes tipos de operaciones
                switch (mensaje.op) {
                    case 'pedido':
                        // Nuevo pedido recibido
                        if (esParaEsteReceptor) {
                            this.store.recepcionPedido(mensaje);
                        }
                        break;

                    case 'servir_lineas':
                        // Marcar líneas como servidas
                        if (esParaEsteReceptor && mensaje.ids && Array.isArray(mensaje.ids)) {
                            this.store.servirLineas(mensaje.ids);
                        }
                        break;

                    case 'borrar_lineas':
                        // Borrar líneas (cobradas/borradas)
                        if (esParaEsteReceptor && mensaje.ids && Array.isArray(mensaje.ids)) {
                            this.store.borrarLineas(mensaje.ids);
                        }
                        break;

                    case 'marcar_urgente':
                        // Marcar líneas o pedido completo como urgente
                        if (esParaEsteReceptor && mensaje.ids && Array.isArray(mensaje.ids)) {
                            this.store.marcarUrgente(mensaje.ids, mensaje.pedido_id || null);
                        }
                        break;

                    default:
                        console.warn('Operación desconocida recibida:', mensaje.op);
                }
            } catch (error) {
                console.error('Error al procesar mensaje WebSocket:', error, event.data);
            }
        };
    }

    disconnect() {
        this.reconnectTimer.reset();

        if (this.customSocket) {
            this.customSocket.onclose = function () { };
            this.customSocket.close();
        }
        // Eliminar del registro al desconectar
        delete VWebsocket.activeSockets[this.receptorKey];
    }

    static disconnectAll() {
        const socketCount = Object.keys(VWebsocket.activeSockets).length;
        if (socketCount > 0) {
            console.log(`🔌 Desconectando ${socketCount} WebSockets activos`);
            for (const receptor in VWebsocket.activeSockets) {
                VWebsocket.activeSockets[receptor].disconnect();
            }
        }
        // Limpiar completamente el registro de sockets activos
        VWebsocket.activeSockets = {};
    }
}