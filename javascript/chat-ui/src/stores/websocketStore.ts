import { defineStore } from 'pinia';
import { useCompanyStore } from '@/stores/companyStore';

export const useWebSocketStore = defineStore('websocket', {
  state: () => ({
    socket: null as WebSocket | null,
    messages: [] as { id: number; text: string; sender: 'user' | 'bot' | 'status'; status?: string }[],
    isConnected: false,
    shouldReconnect: true,
    errorMessage: '', // Estado para almacenar mensajes de error
  }),

  getters: {
    // Getter para verificar el estado real de la conexión
    isActuallyConnected: (state) => {
      return state.socket !== null && state.socket.readyState === WebSocket.OPEN;
    },
  },

  actions: {
    initializeWebSocket() {
      const companyStore = useCompanyStore();
      const companyData = companyStore.companyData;
      
      if (!companyData || !companyData.url) {
        console.error('No se puede inicializar WebSocket: companyData es null o no tiene URL.');
        return;
      }

     
      // Cerrar conexión existente si existe
      if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
        this.shouldReconnect = false; // Evitar reconexión automática del socket anterior
        this.isConnected = false; // Marcar como desconectado inmediatamente
        this.socket.close(); // Cerrar la conexión anterior
        this.socket = null;
        console.log('Conexión WebSocket anterior cerrada');
      }

      const connectWebSocket = () => {
        const url = companyData.url.startsWith('https')
        ? companyData.url.replace('https', 'wss')
        : companyData.url.replace('http', 'ws');
      const token = companyData.token;
      const user = companyData.user;
      const websocketUrl = `${url}/ws/chatbot/gestion/?token=${token}&user=${user}`;
      this.errorMessage = ""; // Limpiar mensaje de error al intentar reconectar


        if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
          this.socket = new WebSocket(websocketUrl);

          this.socket.onopen = () => {
            console.log('WebSocket conectado');
            this.isConnected = true;
            this.shouldReconnect = true; // Permitir reconexión automática
            // NO borrar mensajes aquí - solo cuando se pulse "nuevo chat"
          };

          this.socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log('Mensaje recibido:', data);
            if (data.type === 'message') {
              const existingMessageIndex = this.messages.findIndex(
                (message) => message.id === data.message_id
              );
              
              if (data.sender === 'status') {
                // Mensaje de estado del servidor
                if (existingMessageIndex !== -1) {
                  // Actualizar el mensaje de estado existente
                  this.messages[existingMessageIndex] = {
                    id: data.message_id,
                    text: data.message,
                    sender: 'status',
                    status: data.message,
                  };
                } else {
                  // Crear nuevo mensaje de estado
                  this.messages.push({
                    id: data.message_id,
                    text: data.message,
                    sender: 'status',
                    status: data.message,
                  });
                }
              } else if (data.sender === 'bot') {
                // Respuesta final del bot - reemplazar el mensaje de estado
                if (existingMessageIndex !== -1) {
                  this.messages[existingMessageIndex] = {
                    id: data.message_id,
                    text: data.message,
                    sender: 'bot',
                  };
                } else {
                  this.messages.push({
                    id: data.message_id,
                    text: data.message,
                    sender: 'bot',
                  });
                }
              } else {
                // Otros tipos de mensajes (user, etc.)
                if (existingMessageIndex !== -1) {
                  this.messages[existingMessageIndex] = {
                    id: data.message_id,
                    text: data.message,
                    sender: data.sender,
                  };
                } else {
                  this.messages.push({
                    id: data.message_id,
                    text: data.message,
                    sender: data.sender,
                  });
                }
              }
            } else if (data.type === 'error') {
              this.errorMessage = data.message; // Almacenar mensaje de error
            } else {
              console.warn('Tipo de mensaje desconocido:', data);
            }
          };

          this.socket.onerror = (error) => {
            console.error('Error en WebSocket:', error);
          };

          this.socket.onclose = () => {
            this.isConnected = false;
            // NO borrar mensajes aquí - mantenerlos para que el usuario pueda verlos
            console.log('WebSocket desconectado');
            this.socket = null;
            if (!this.shouldReconnect) {
              console.log('Reconexión cancelada');
              return;
            }
            console.log('Intentando reconectar...');
            setTimeout(connectWebSocket, 5000);
            
          };
        }
      };

      connectWebSocket();
    },

    sendMessage(obj: { text: string, sender: string }) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.messages.push({
          id: Date.now(),
          text: obj.text,
          sender: obj.sender,
        });
        this.socket.send(
          JSON.stringify({
            type: 'message',
            text: obj.text,
            sender: obj.sender,
          })
        );
      } else {
        console.error('WebSocket no está conectado');
        this.errorMessage = 'No se puede enviar el mensaje. WebSocket desconectado.';
      }
    },

    addMessage(message: { text: string, sender: 'user' | 'bot', timestamp?: string }) {
      // Método para agregar un mensaje al chat sin enviarlo por WebSocket
      this.messages.push({
        id: Date.now(),
        text: message.text,
        sender: message.sender,
      });
    },

    sendAudioMessage(base64Audio: string) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.messages.push({
          id: Date.now(),
          text: '[Audio Message]',
          sender: 'user',
        });
        this.socket.send(
          JSON.stringify({
            type: 'audio',
            audio: base64Audio,
            sender: 'user',
          })
        );
      } else {
        console.error('WebSocket no está conectado');
        this.errorMessage = 'No se puede enviar el audio. WebSocket desconectado.';
      }
    },

    disconnect() {
      this.shouldReconnect = false;
      if (this.socket) {
        this.socket.close();
        this.socket = null;
        this.isConnected = false;
        console.log('WebSocket desconectado manualmente');
      }
    },

    clearMessages() {
      // Nuevo método para limpiar mensajes solo cuando se pulse "nuevo chat"
      this.messages = [];
      console.log('Mensajes borrados manualmente');
    },

    clearErrorMessage() {
      this.errorMessage = ''; // Método para limpiar el mensaje de error
    },
  },
});