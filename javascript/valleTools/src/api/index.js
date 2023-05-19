import axios from 'axios';

// Configura axios con la URL base de tu servidor Django
const api = axios.create();

// Función para obtener un nuevo token
export async function getNewToken(username, password, url) {
  try {
    var params = new FormData()
    params.append('username', username);
    params.append("password", password)
    const response = await api.post(url+'/token/new.json', params);

    if (response.status === 200 && response.data) {
      return response.data;
    }
  } catch (error) {
    console.error('Error al obtener el token:', error);
    return null;
  }
}

class ReconnectingWebSocket {
  constructor(url, onMessageCallback) {
    this.url = url;
    this.onMessageCallback = onMessageCallback;
    this.socket = null;
    this.reconnectInterval = 1000; // Tiempo de espera antes de intentar la reconexión (en milisegundos)
    this.connect();
  }

  connect() {
    this.socket = new WebSocket(this.url);
    this.closed = false;
    this.socket.addEventListener("open", (event) => {
      console.log("WebSocket conectado:", event);
    });

    this.socket.addEventListener("message", (event) => {
      this.onMessageCallback(JSON.parse(event.data));
    });

    this.socket.addEventListener("close", (event) => {
      console.log("WebSocket desconectado:", event);
      if (!this.closed){
        this.interval = setTimeout(() => {
          console.log("Intentando reconectar...");
          if (!this.closed) this.connect();
        }, this.reconnectInterval);
      }
    });

    this.socket.addEventListener("error", (event) => {
      console.error("WebSocket error:", event);
    });
  }

  send(data) {
    if (this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(data);
    } else {
      console.error("WebSocket no está conectado.");
    }
  }

  close() {
      this.closed = true;
      this.socket.close();
      if (this.interval){
        clearInterval(this.interval);
      }
  }
}


export default ReconnectingWebSocket;