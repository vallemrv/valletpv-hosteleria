<template>
  <div class="chat-messages">
    <div v-for="message in messages" :key="message.id" class="message-wrapper">
      <div v-if="message.sender !== 'status'" class="message-container" :class="message.sender"
        @mouseenter="hoveredMessage = message.id" @mouseleave="hoveredMessage = null">
        <!-- Avatar del bot -->
        <div v-if="message.sender === 'bot'" class="message-avatar">
          <div class="bot-avatar">
            <v-icon color="white" size="18">mdi-robot</v-icon>
          </div>
        </div>

        <!-- Contenido del mensaje -->
        <div class="message-bubble" :class="message.sender">
          <div v-html="getStyledHtml(message.text, message.sender)" class="message-content"></div>

          <!-- Botones de acción para mensajes del bot -->
          <div v-if="message.sender === 'bot' && (hoveredMessage === message.id || isMobile)" class="message-actions">
            <v-btn icon size="x-small" variant="text" @click="copyMessage(message.text)" class="action-btn">
              <v-icon size="14">mdi-content-copy</v-icon>
            </v-btn>

            <SaveBotMessageButton :html-content="message.text" @openSaveDialog="$emit('openSaveDialog', $event)"
              size="x-small" class="action-btn" />
          </div>
        </div>
      </div>

      <!-- Mensaje de estado (pensando...) -->
      <div v-else class="thinking-container">
        <div class="message-avatar">
          <div class="bot-avatar">
            <v-icon color="white" size="18">mdi-robot</v-icon>
          </div>
        </div>
        <div class="thinking-content">
          <div class="thinking-bubble">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
          <!-- Texto de estado debajo de los puntos -->
          <div v-if="message.status" class="status-text">
            {{ message.status }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useWebSocketStore } from '@/stores/websocketStore';
import SaveBotMessageButton from '@/components/chat/SaveBotMessageButton.vue';
import { useNotifications } from '@/composables/useNotifications';
import thinkingGif from "@/assets/thinking.gif";
import { marked } from 'marked';

export default {
  components: {
    SaveBotMessageButton
  },

  emits: ['openSaveDialog'],

  setup() {
    const { showSuccess, showError } = useNotifications();
    return { showSuccess, showError };
  },

  data() {
    return {
      thinkingGif,
      hoveredMessage: null,
      isMobile: window.innerWidth <= 768,
    };
  },

  mounted() {
    window.addEventListener('resize', this.handleResize);
  },

  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
  },

  computed: {
    messages() {
      const webSocketStore = useWebSocketStore();
      return webSocketStore.messages; // Obtener los mensajes desde el store
    },
  },

  methods: {
    parseMarkdown(text) {
      // Convierte Markdown a HTML
      if (!text) return ''; // Buena práctica añadir esto
      try {
        const html = marked.parse(text, {
          gfm: true,
          breaks: false,
          pedantic: false
        });
        return html;
      } catch (error) {
        console.error("Error parsing Markdown:", error);
        return `<p>Error al mostrar mensaje.</p>`;
      }
    },

    // Nueva función para aplicar estilos según el tipo de mensaje
    getStyledHtml(text, sender) {
      const html = this.parseMarkdown(text);

      if (sender === 'user') {
        // Para mensajes del usuario, forzar color blanco en todos los elementos
        return html.replace(/<(p|h[1-6]|span|div|strong|b|em|i|li|ul|ol|blockquote)([^>]*)>/g,
          '<$1$2 style="color: white !important;">');
      } else if (sender === 'bot') {
        // Para mensajes del bot, añadir estilos a las tablas
        let styledHtml = html;

        // Añadir estilos a las celdas de tabla (th y td)
        styledHtml = styledHtml.replace(/<(th|td)([^>]*)>/g,
          '<$1$2 style="padding: 8px 12px; border: 1px solid #ddd; text-align: left; white-space: nowrap;">');

        // Envolver tablas en contenedor con scroll y añadir estilos
        styledHtml = styledHtml.replace(/<table([^>]*)>/g,
          '<div class="table-container"><table$1 style="width: auto; min-width: 100%; border-collapse: collapse; margin: 0; font-size: 0.95rem; white-space: nowrap;">');

        styledHtml = styledHtml.replace(/<\/table>/g, '</table></div>');

        // Añadir estilos a los encabezados de tabla
        styledHtml = styledHtml.replace(/<th([^>]*style="[^"]*")([^>]*)>/g,
          '<th$1; background-color: rgba(0, 0, 0, 0.05); font-weight: 600;"$2>');

        return styledHtml;
      }

      return html;
    },

    copyMessage(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.showSuccess('Mensaje copiado al portapapeles');
      }).catch(err => {
        console.error('Error al copiar al portapapeles:', err);
        this.showError('Error al copiar el mensaje');
      });
    },

    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    }
  },
};
</script>

<style scoped>
/* Contenedor principal de mensajes */
.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0;
}

/* Wrapper de cada mensaje */
.message-wrapper {
  display: flex;
  width: 100%;
}

/* Contenedor del mensaje */
.message-container {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
}

.message-container.user {
  justify-content: flex-end;
  margin-left: auto;
  margin-right: 0;
}

.message-container.bot {
  justify-content: flex-start;
  overflow: hidden;
}

@media (max-width: 768px) {
  .message-container.user {
    margin-left: auto;
    margin-right: 5%;
  }

  .message-container.bot {
    margin-right: 10%;
  }
}

/* Avatar del bot */
.message-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
}

.bot-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* Burbujas de mensaje */
.message-bubble {
  position: relative;
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  transition: all 0.2s ease;
  display: inline-block;
  overflow: hidden;
  max-width: max-content;
  overflow-x: auto;
}

.message-bubble.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
  margin-left: auto;
  max-width: 90%;
}

.message-bubble.bot {
  background: #f8f9fa;
  color: #2d3748;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
  position: relative;
  max-width: 85%;
}

.message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* Contenido del mensaje */
.message-content {
  font-size: 15px;
  line-height: 1.9;
  word-break: break-word;
  min-width: fit-content;
  max-width: max-content;
  padding: 15px;
}

.message-bubble.user {
  padding: 0;
  min-width: fit-content;
  max-width: max-content;
}

/* Estilos para el contenido del usuario */
.message-bubble.user .message-content,
.message-bubble.user .message-content * {
  color: white !important;

}

.message-bubble.user .message-content h1,
.message-bubble.user .message-content h2,
.message-bubble.user .message-content h3,
.message-bubble.user .message-content h4,
.message-bubble.user .message-content h5,
.message-bubble.user .message-content h6 {
  color: white !important;
  margin: 8px 0 4px 0;
}

.message-bubble.user .message-content p {
  margin: 0 0 8px 0;
  color: white !important;
}

.message-bubble.user .message-content p:last-child {
  margin-bottom: 0;
}

/* Estilos para el contenido del bot */
.message-bubble.bot .message-content {
  color: #2d3748;
}

.message-bubble.bot .message-content h1,
.message-bubble.bot .message-content h2,
.message-bubble.bot .message-content h3,
.message-bubble.bot .message-content h4,
.message-bubble.bot .message-content h5,
.message-bubble.bot .message-content h6 {
  color: #1a202c;
  margin: 12px 0 8px 0;
  font-weight: 600;
}

.message-bubble.bot .message-content p {
  margin: 0 0 8px 0;
  color: #2d3748;
}

.message-bubble.bot .message-content p:last-child {
  margin-bottom: 0;
}

.message-bubble.bot .message-content strong,
.message-bubble.bot .message-content b {
  color: #1a202c;
  font-weight: 600;
}

.message-bubble.bot .message-content code {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
}

.message-bubble.bot .message-content pre {
  background: #f7fafc;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #667eea;
  margin: 8px 0;
  overflow-x: auto;
}

/* Contenedor con scroll para tablas */
.message-content .table-container {
  margin: 12px 0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Estilos para tablas */
.message-content table {
  border-collapse: collapse;
  margin: 0;
  border-radius: 8px;
  white-space: nowrap;
}

.message-content th,
.message-content td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.message-content th {
  background: #f7fafc;
  font-weight: 600;
  color: #2d3748;
}

.message-content td {
  color: #4a5568;
}

.message-content tr:hover {
  background: #f7fafc;
}

/* Botones de acción */
.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.message-actions:hover {
  opacity: 1;
}

.action-btn {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #e2e8f0 !important;
  border-radius: 6px !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.2s ease !important;
}

.action-btn:hover {
  background: white !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15) !important;
  transform: translateY(-1px);
}

/* Contenedor de pensando */
.thinking-container {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-right: 20%;
}

.thinking-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@media (max-width: 768px) {
  .thinking-container {
    margin-right: 10%;
  }
}

/* Burbuja de pensando */
.thinking-bubble {
  background: #f8f9fa;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  padding: 16px 20px;
  position: relative;
}

/* Texto de estado debajo de los puntos */
.status-text {
  font-size: 12px;
  color: #a0aec0;
  font-style: italic;
  padding: 0 8px;
  opacity: 0.8;
  animation: fadeIn 0.3s ease-in;
}

/* Animación de puntos pensando */
.thinking-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  background: #cbd5e0;
  border-radius: 50%;
  animation: thinking 1.4s infinite ease-in-out;
}

.thinking-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.thinking-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

.thinking-dots span:nth-child(3) {
  animation-delay: 0;
}

@keyframes thinking {

  0%,
  80%,
  100% {
    transform: scale(1);
    opacity: 0.5;
  }

  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }

  to {
    opacity: 0.8;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .chat-messages {
    gap: 12px;
  }

  .message-bubble {
    padding: 10px 14px;
    font-size: 14px;
  }

  .message-bubble.user {
    max-width: 95%;
  }

  .message-bubble.bot {
    max-width: calc(100vw - 70px);
    /* Ancho máximo para el bot en móviles */
  }

  .message-content {
    font-size: 14px;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }

  /* Solo para tablas: contenedor con scroll */
  .message-content .table-container {
    overflow-x: auto;
    width: 100%;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: white;
  }

  .message-content table {
    min-width: 300px;
    width: max-content;
  }

  /* Solo para código: contenedor con scroll */
  .message-content pre {
    overflow-x: auto;
    white-space: pre;
    word-wrap: normal;
    max-width: 100%;
  }

  .bot-avatar {
    width: 28px;
    height: 28px;
  }

  .message-avatar {
    width: 28px;
    height: 28px;
  }

  .message-actions {
    margin-top: 6px;
  }

  /* Mostrar siempre los botones en móvil */
  .message-container.bot .message-actions {
    opacity: 1;
  }
}

/* Scroll personalizado para tablas */
.message-content::-webkit-scrollbar {
  height: 6px;
}

.message-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.message-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.message-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* Mejoras para modo oscuro */
@media (prefers-color-scheme: dark) {
  .message-bubble.bot {
    background: #2d3748;
    border-color: #4a5568;
    color: #e2e8f0;
  }

  .message-bubble.bot .message-content,
  .message-bubble.bot .message-content p,
  .message-bubble.bot .message-content span {
    color: #e2e8f0 !important;
  }

  .message-bubble.bot .message-content h1,
  .message-bubble.bot .message-content h2,
  .message-bubble.bot .message-content h3,
  .message-bubble.bot .message-content h4,
  .message-bubble.bot .message-content h5,
  .message-bubble.bot .message-content h6 {
    color: #f7fafc !important;
  }

  .thinking-bubble {
    background: #2d3748;
    border-color: #4a5568;
  }

  .thinking-dots span {
    background: #718096;
  }

  .status-text {
    color: #718096;
  }

  .action-btn {
    background: rgba(45, 55, 72, 0.9) !important;
    border-color: #4a5568 !important;
    color: #e2e8f0 !important;
  }

  .action-btn:hover {
    background: #2d3748 !important;
  }
}
</style>