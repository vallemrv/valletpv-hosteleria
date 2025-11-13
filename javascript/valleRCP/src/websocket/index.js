class Timer {
    constructor(callback, timerCalc){
      this.callback  = callback;
      this.timerCalc = timerCalc;
      this.timer     = null;
      this.tries     = 0;
    }
  
    reset(){
      this.tries = 0
      clearTimeout(this.timer)
    }

    scheduleTimeout(){
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

    constructor(server, receptor, store){
        if (VWebsocket.activeSockets[receptor.nomimp]) {
            console.warn(`Ya existe una conexión para el receptor: ${receptor.nomimp}. No se creará una nueva.`);
            return VWebsocket.activeSockets[receptor.nomimp]; // Opcional: devolver la instancia existente
        }

        const wsProtocol = getWebSocketProtocol(server);
        this.socketUrl = `${wsProtocol}/ws/impresion/${receptor.nomimp}/`;
        this.customSocket = null;
        this.store = store;
        this.receptor = receptor;
        this.reconnectTimer = new Timer(() => {
            this.disconnect();
            this.connect();
        }, this.reconnectAfterMs);

        VWebsocket.activeSockets[receptor.nomimp] = this; // Registrar la nueva instancia
    }

    reconnectAfterMs(tries){
        return [1000, 2000, 5000, 10000][tries - 1] || 10000;
    }

    connect(){
        this.reconnectTimer.reset();
        
        if (this.customSocket) this.disconnect();
        
        console.log(`Conectando WebSocket: ${this.socketUrl}`);
        this.customSocket = new WebSocket(this.socketUrl);

        this.customSocket.onopen = (event) => {
            console.log(`WebSocket conectado exitosamente: ${this.receptor.nomimp}`);
            this.reconnectTimer.reset();
            this.store.onConnect();
        };

        this.customSocket.onclose = (event) => {
            console.log(`WebSocket cerrado para receptor: ${this.receptor.nomimp}, código: ${event.code}`);
            this.store.onDisconnect();
            if (event.code !== 1000) {
                this.reconnectTimer.scheduleTimeout();
            }
            // Eliminar del registro al cerrar la conexión
            delete VWebsocket.activeSockets[this.receptor.nomimp];
        };

        this.customSocket.onerror = (event) => {
            console.error(`Error en WebSocket para receptor: ${this.receptor.nomimp}`, event);
            this.store.onDisconnect();
        };

        this.customSocket.onmessage = (event) => {
            var pedido = JSON.parse(JSON.parse(event.data).message);
            if (pedido.nom_receptor == this.receptor.Nombre)
              this.store.recepcionPedido(pedido);
        };
    }

    disconnect(){
        this.reconnectTimer.reset();
        
        if (this.customSocket) {
            this.customSocket.onclose = function(){};
            this.customSocket.close();
            console.log(`WebSocket desconectado para receptor: ${this.receptor.nomimp}`);
        }
        // Eliminar del registro al desconectar
        delete VWebsocket.activeSockets[this.receptor.nomimp];
    }

    static disconnectAll() {
        console.log("Desconectando todos los WebSockets...");
        for (const receptor in VWebsocket.activeSockets) {
            VWebsocket.activeSockets[receptor].disconnect();
        }
    }
}