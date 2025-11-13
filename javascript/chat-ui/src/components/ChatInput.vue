<template>
  <div class="chat-input-container">
    <!-- Contenedor principal del input -->
    <div class="input-wrapper">
      <!-- Área de texto -->
      <div class="textarea-container">
        <textarea v-model="message" placeholder="Escribe tu mensaje..." class="chat-textarea" rows="1"
          @keydown="handleKeydown" @input="adjustHeight" ref="textarea" :disabled="!isConnected"></textarea>
      </div>

      <!-- Botones de acción -->
      <div class="actions-container">
        <v-btn icon size="small" variant="text" @click="attachFile" class="action-btn" :disabled="!isConnected">
          <v-icon size="20">mdi-paperclip</v-icon>
        </v-btn>

        <v-btn icon size="small" variant="text" @click="toggleRecording"
          :class="['action-btn', { 'recording': isRecording }]" :disabled="!isConnected">
          <v-icon size="20">{{ isRecording ? 'mdi-stop' : 'mdi-microphone' }}</v-icon>
        </v-btn>

        <v-btn v-if="message.trim()" icon size="small" class="send-btn" @click="send" :disabled="!isConnected">
          <v-icon size="20">mdi-send</v-icon>
        </v-btn>
      </div>
    </div>

    <!-- Indicador de grabación -->
    <div v-if="isRecording" class="recording-indicator">
      <div class="recording-content">
        <span class="recording-dot"></span>
        <span class="recording-text">Grabando...</span>
      </div>
    </div>
  </div>
</template>

<script>
import { useWebSocketStore } from '@/stores/websocketStore';
import { useCompanyStore } from '@/stores/companyStore';
import { storeToRefs } from 'pinia';

export default {
  data: () => ({
    message: '',
    isRecording: false,
    mediaRecorder: null,
    audioChunks: [],
  }),
  setup() {
    const webSocketStore = useWebSocketStore();
    const { isConnected } = storeToRefs(webSocketStore);
    return {
      isConnected, // Usar el estado reactivo del store
    };
  },
  methods: {
    handleKeydown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        this.send();
      }
    },

    send() {
      if (this.message.trim() && this.isConnected) {
        const webSocketStore = useWebSocketStore();
        webSocketStore.sendMessage({
          text: this.message,
          sender: 'user',
        });
        this.message = '';
        this.adjustHeight();
      }
    },

    adjustHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.textarea;
        if (textarea) {
          textarea.style.height = 'auto';
          const maxHeight = 120; // Máximo 5 líneas aprox
          textarea.style.height = Math.min(textarea.scrollHeight, maxHeight) + 'px';
        }
      });
    },

    attachFile() {
      alert('Funcionalidad de adjuntar archivo aún no implementada');
    },
    async toggleRecording() {
      if (this.isRecording) {
        // Detener la grabación
        this.mediaRecorder.stop();
        this.isRecording = false;
      } else {
        // Iniciar la grabación
        try {
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          this.mediaRecorder = new MediaRecorder(stream);
          this.audioChunks = [];

          this.mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) {
              this.audioChunks.push(event.data);
            }
          };

          this.mediaRecorder.onstop = () => {
            const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
            this.sendAudio(audioBlob); // Llamada actual
            this.audioChunks = []; // Limpiar chunks

            // Añadir esto para detener las pistas y liberar el mic:
            if (this.mediaRecorder && this.mediaRecorder.stream) {
              this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
            }
            this.mediaRecorder = null; // Opcional: limpiar instancia
          };

          this.mediaRecorder.start();
          this.isRecording = true;
        } catch (error) {
          console.error('Error al acceder al micrófono:', error);
        }
      }
    },
    sendAudio(audioBlob) {
      const webSocketStore = useWebSocketStore();
      const reader = new FileReader();

      reader.onload = () => {
        const base64Audio = reader.result.split(',')[1]; // Obtener solo la parte base64
        webSocketStore.sendAudioMessage(base64Audio); // Usar sendAudioMessage del store
      };

      reader.readAsDataURL(audioBlob);
    },
  },

  mounted() {
    this.adjustHeight();
  },
};
</script>

<style scoped>
/* Contenedor principal */
.chat-input-container {
  width: 100%;
  position: relative;
}

/* Wrapper del input */
.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 24px;
  padding: 12px 16px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.input-wrapper:hover {
  border-color: #cbd5e0;
}

/* Contenedor del textarea */
.textarea-container {
  flex: 1;
  min-height: 24px;
}

/* Textarea */
.chat-textarea {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 15px;
  line-height: 1.5;
  color: #2d3748;
  min-height: 24px;
  max-height: 120px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.chat-textarea::placeholder {
  color: #a0aec0;
}

.chat-textarea:disabled {
  color: #a0aec0;
  cursor: not-allowed;
}

/* Contenedor de acciones */
.actions-container {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

/* Botones de acción */
.action-btn {
  border-radius: 12px !important;
  transition: all 0.2s ease !important;
  color: #718096 !important;
  min-width: 36px !important;
  width: 36px !important;
  height: 36px !important;
}

.action-btn:hover {
  background: #f7fafc !important;
  color: #4a5568 !important;
  transform: scale(1.05);
}

.action-btn:disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
}

.action-btn:disabled:hover {
  transform: none !important;
  background: transparent !important;
}

/* Botón de envío */
.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  border-radius: 12px !important;
  transition: all 0.2s ease !important;
  min-width: 36px !important;
  width: 36px !important;
  height: 36px !important;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3) !important;
}

.send-btn:hover {
  transform: scale(1.05) !important;
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.4) !important;
}

.send-btn:disabled {
  opacity: 0.5 !important;
  cursor: not-allowed !important;
}

.send-btn:disabled:hover {
  transform: none !important;
}

/* Estado de grabación */
.action-btn.recording {
  background: #fed7d7 !important;
  color: #c53030 !important;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.7;
  }
}

/* Indicador de grabación */
.recording-indicator {
  position: absolute;
  top: -32px;
  left: 16px;
  z-index: 10;
}

.recording-content {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fed7d7;
  padding: 4px 12px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #feb2b2;
}

.recording-text {
  font-size: 12px;
  color: #c53030;
  font-weight: 500;
}

.recording-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c53030;
  animation: pulse 1s infinite;
}

/* Responsive */
@media (max-width: 768px) {
  .input-wrapper {
    padding: 10px 14px;
    border-radius: 20px;
  }

  .chat-textarea {
    font-size: 16px;
    /* Prevenir zoom en iOS */
    touch-action: manipulation;
    /* Agrega esto */
  }

  .action-btn,
  .send-btn {
    min-width: 32px !important;
    width: 32px !important;
    height: 32px !important;
  }

  .actions-container {
    gap: 2px;
  }

  .status-indicator {
    top: -28px;
    left: 12px;
  }

  .recording-indicator {
    top: -28px;
    left: 12px;
  }

  .status-content,
  .recording-content {
    padding: 3px 8px;
  }

  .status-text,
  .recording-text {
    font-size: 11px;
  }
}

/* Scroll personalizado para el textarea */
.chat-textarea::-webkit-scrollbar {
  width: 4px;
}

.chat-textarea::-webkit-scrollbar-track {
  background: transparent;
}

.chat-textarea::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.chat-textarea::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

/* Modo oscuro */
@media (prefers-color-scheme: dark) {
  .input-wrapper {
    background: #2d3748;
    border-color: #4a5568;
  }

  .input-wrapper:focus-within {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2), 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  .input-wrapper:hover {
    border-color: #718096;
  }

  .chat-textarea {
    color: #e2e8f0;
  }

  .chat-textarea::placeholder {
    color: #718096;
  }

  .action-btn {
    color: #a0aec0 !important;
  }

  .action-btn:hover {
    background: #4a5568 !important;
    color: #e2e8f0 !important;
  }

  .status-content {
    background: #2d3748;
    border-color: #4a5568;
  }

  .recording-content {
    background: #c53030;
    border-color: #c53030;
  }

  .status-text {
    color: #a0aec0;
  }

  .recording-text {
    color: white;
  }
}

/* Transiciones suaves */
* {
  transition: color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
}
</style>