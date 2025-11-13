<template>
  <div class="chat-layout">
    <!-- Barra superior fija -->
    <v-app-bar app class="app-bar" flat>
      <v-toolbar-title class="app-title" @click="resetChat">ValleBot</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn v-if="companyData" icon @click="resetChat">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
      <v-btn v-if="companyData" icon @click="openSavedMessagesDrawer">
        <v-icon>mdi-bookmark-multiple</v-icon>
        <v-tooltip activator="parent" location="bottom">Mensajes Guardados</v-tooltip>
      </v-btn>
      <v-btn icon @click="openEmpresaDialog">
        <v-icon>mdi-plus</v-icon>
      </v-btn>
      <company-dropdown v-if="companies.length > 0" :companies="companies" :selectedCompany="companyData"
        @edit-company="handleEditCompany" />
    </v-app-bar>

    <!-- Contenido principal -->
    <div class="main-content">
      <!-- Welcome message cuando no hay empresa -->
      <div v-if="!companyData" class="welcome-container">
        <div class="welcome-content">
          <v-img :src="logoValleBot" class="responsive-logo mb-4" alt="Logo ValleBot" />
          <v-card class="pa-4 pa-sm-6" elevation="6">
            Para chatear con una empresa, primero hay que crear una empresa.
          </v-card>
        </div>
      </div>

      <!-- Chat cuando hay empresa -->
      <div v-if="companyData" class="chat-layout-container">
        <!-- Estado inicial: empresa centrada con input -->
        <div v-if="!hasStartedChat" class="initial-state">
          <div class="company-info">
            <div class="company-name">{{ companyData.name }}</div>
            <div class="welcome-message">¿En qué puedo ayudarte?</div>
          </div>
          <div class="input-area-centered">
            <div class="input-container-modern">
              <chat-input />

              <!-- Indicador de conexión debajo del input en estado inicial -->
              <div class="connection-status-input">
                <span
                  :class="['status-dot', { 'connected': websocketStore.isConnected, 'disconnected': !websocketStore.isConnected }]"></span>
                <span class="status-text">
                  {{ websocketStore.isConnected ? 'Conectado' : 'Desconectado' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Estado con chat: mensajes + input fijo -->
        <div v-else class="chat-active-state">
          <!-- Área de mensajes con scroll dinámico -->
          <div class="messages-area" ref="messagesArea">
            <div class="messages-container">
              <chat-message @openSaveDialog="handleOpenSaveDialog" />
            </div>
          </div>

          <!-- Área del input con indicador de conexión -->
          <div class="input-area-fixed">
            <div class="input-container-modern">
              <chat-input />

              <!-- Indicador de conexión debajo del input -->
              <div v-if="companyData" class="connection-status-input">
                <span
                  :class="['status-dot', { 'connected': websocketStore.isConnected, 'disconnected': !websocketStore.isConnected }]"></span>
                <span class="status-text">
                  {{ websocketStore.isConnected ? 'Conectado' : 'Desconectado' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Botón de scroll hacia abajo -->
    <v-btn v-if="companyData && hasStartedChat && showScrollButton" class="scroll-down-btn" elevation="5" icon
      @click="scrollToBottom">
      <v-icon>mdi-arrow-down</v-icon>
    </v-btn>

    <!-- Snackbar para errores -->
    <v-snackbar v-model="snackbarVisible" color="error" timeout="5000" location="top">
      {{ websocketStore.errorMessage }}
      <template v-slot:action="{ attrs }">
        <v-btn color="white" text @click="closeSnackbar" v-bind="attrs">Cerrar</v-btn>
      </template>
    </v-snackbar>

    <!-- Dialogs y componentes flotantes -->
    <empresa-dialog ref="empresaDialog" />
    <save-message-dialog ref="saveMessageDialog" />
    <saved-messages-panel v-model="savedMessagesDrawerOpen" @reuseMessage="handleReuseMessage" />
    <notification-container />
  </div>
</template>

<script>
import ChatMessage from '@/components/ChatMessage.vue';
import ChatInput from '@/components/ChatInput.vue';
import EmpresaDialog from '@/components/EmpresaDialog.vue';
import CompanyDropdown from '@/components/CompanyDropdown.vue';
import SavedMessagesPanel from '@/components/chat/SavedMessagesPanel.vue';
import SaveMessageDialog from '@/components/chat/SaveMessageDialog.vue';
import NotificationContainer from '@/components/NotificationContainer.vue';
import logoValleBot from '@/assets/logoValleBot.svg';
import { useCompanyStore } from '@/stores/companyStore';
import { useWebSocketStore } from '@/stores/websocketStore';

export default {
  components: {
    ChatMessage,
    ChatInput,
    EmpresaDialog,
    CompanyDropdown,
    SavedMessagesPanel,
    SaveMessageDialog,
    NotificationContainer
  },
  setup() {
    const websocketStore = useWebSocketStore();
    return { websocketStore };
  },
  data() {
    return {
      showScrollButton: false, // Controla la visibilidad del botón de scroll
      scrollPosition: 0, // Almacena la posición del scroll
      showSettingsDialog: false, // Controla la visibilidad del cuadro de diálogo
      logoValleBot, // SVG del logo
      snackbarVisible: false, // Controla la visibilidad del snackbar
      savedMessagesDrawerOpen: false, // Controla la visibilidad del drawer de mensajes guardados
    };
  },
  computed: {
    companyStore() {
      return useCompanyStore(); // Acceso al store de compañías
    },
    companies() {
      return this.companyStore.companyDataList; // Lista de compañías desde el store
    },
    companyData() {
      return this.companyStore.companyData; // Compañía activa desde el store
    },
    hasStartedChat() {
      return this.websocketStore.messages.length > 0; // El chat ha comenzado si hay mensajes
    },
  },
  methods: {
    scrollToBottom() {
      this.$nextTick(() => {
        const messagesArea = this.$refs.messagesArea;
        if (messagesArea) {
          messagesArea.scrollTop = messagesArea.scrollHeight;
        }
      });
    },
    handleScroll() {
      const messagesArea = this.$refs.messagesArea;
      if (!messagesArea) return;

      const isAtBottom = messagesArea.scrollTop + messagesArea.clientHeight >= messagesArea.scrollHeight - 10;
      this.showScrollButton = !isAtBottom && this.hasStartedChat;
    },
    openEmpresaDialog() {
      // Abrir el cuadro de diálogo de Empresa usando su referencia
      this.$refs.empresaDialog.openDialog();
    },
    handleEditCompany(company) {
      // Abrir el cuadro de diálogo de Empresa con los datos de la empresa a editar
      this.$refs.empresaDialog.openDialog(company);
    },
    handleUpdateCompanies(newCompany) {
      this.companyStore.addCompany(newCompany); // Agregar o actualizar la compañía en el store
      this.companyStore.setActiveCompany(newCompany); // Establecer la nueva compañía como activa
    },
    resetChat() {
      this.websocketStore.clearMessages(); // Limpiar mensajes
      this.websocketStore.socket?.close(); // Desconectar WebSocket
    },
    closeSnackbar() {
      this.snackbarVisible = false; // Ocultar el snackbar
      this.websocketStore.clearErrorMessage(); // Limpiar el mensaje de error en el store
    },
    openSavedMessagesDrawer() {
      this.savedMessagesDrawerOpen = true;
    },
    handleReuseMessage(htmlContent) {
      // El contenido viene como HTML, lo insertamos directamente en el chat
      this.websocketStore.addMessage({
        text: htmlContent,
        sender: 'bot',
        timestamp: new Date().toISOString()
      });
    },
    handleOpenSaveDialog(htmlContent) {
      console.log('ChatView received openSaveDialog event with content:', htmlContent);
      // Abrir el diálogo de guardar mensaje usando su referencia
      this.$refs.saveMessageDialog.openDialog(htmlContent);
    },
    setupScrollListener() {
      const messagesArea = this.$refs.messagesArea;
      if (messagesArea) {
        messagesArea.addEventListener('scroll', this.handleScroll);
      }
    },
    removeScrollListener() {
      const messagesArea = this.$refs.messagesArea;
      if (messagesArea) {
        messagesArea.removeEventListener('scroll', this.handleScroll);
      }
    },
    // NUEVO: Detecta teclado y ajusta (MODO NATIVO)
    handleKeyboardChange() {
      // 1. La API moderna (VirtualKeyboard)
      if ('virtualKeyboard' in navigator) {
        // CLAVE: Le decimos al navegador que NO redimensione la vista.
        // El teclado se superpondrá.
        navigator.virtualKeyboard.overlaysContent = true;

        navigator.virtualKeyboard.addEventListener('geometrychange', (e) => {
          // Obtenemos la altura del teclado
          const kbHeight = e.target.boundingRect.height;

          // Actualizamos una variable CSS global
          document.documentElement.style.setProperty('--keyboard-height', `${kbHeight}px`);

          // Si el teclado se abre (altura > 0), scrolleamos al fondo
          if (kbHeight > 0) {
            this.scrollToBottom();
          }
        });
      } else {
        // 2. Fallback para navegadores antiguos (usando 'resize')
        // Esto es menos fiable y puede causar el "reflow" que no te gusta,
        // pero es mejor que nada si la API nueva no existe.
        let initialHeight = window.innerHeight;
        window.addEventListener('resize', () => {
          const currentHeight = window.innerHeight;
          if (document.activeElement?.tagName === 'INPUT' || document.activeElement?.tagName === 'TEXTAREA') {
            if (currentHeight < initialHeight) {
              // Se abrió el teclado (estimado)
              const kbHeight = initialHeight - currentHeight;
              document.documentElement.style.setProperty('--keyboard-height', `${kbHeight}px`);
              this.scrollToBottom();
            } else {
              // Se cerró
              document.documentElement.style.setProperty('--keyboard-height', '0px');
            }
          } else {
            document.documentElement.style.setProperty('--keyboard-height', '0px');
          }
          // Actualizamos la altura inicial solo si no hay un input activo
          if (document.activeElement?.tagName !== 'INPUT' && document.activeElement?.tagName !== 'TEXTAREA') {
            initialHeight = currentHeight;
          }
        });
      }

      // 3. Ayuda para enfocar el input (esto estaba bien)
      document.addEventListener('focusin', (e) => {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
          setTimeout(() => {
            e.target.scrollIntoView({
              behavior: 'smooth',
              block: 'nearest',
            });
          }, 300);
        }
      });
    },
  },
  watch: {
    'websocketStore.messages': {
      handler(newValue, oldValue) {
        this.scrollToBottom();
      },
      deep: true,
    },
    companyData: {
      handler(newCompanyData, oldCompanyData) {
        if (newCompanyData) {
          // Si se cambia de empresa, limpiar mensajes
          if (oldCompanyData && oldCompanyData.id !== newCompanyData.id) {
            this.websocketStore.clearMessages();
            this.websocketStore.disconnect(); // Cerrar WebSocket si está conectado

            // Esperar un poco antes de reconectar para asegurar que se cerró completamente
            setTimeout(() => {
              this.websocketStore.initializeWebSocket(); // Inicializar WebSocket con la nueva compañía
            }, 100);
          } else {
            // Primera carga o misma empresa
            if (!this.websocketStore.isConnected) {
              this.websocketStore.initializeWebSocket();
            }
          }

          // Configurar el listener de scroll cuando se carga una empresa
          this.$nextTick(() => {
            this.setupScrollListener();
          });
        }
      },
      immediate: true, // Ejecutar el watcher inmediatamente al montar
    },
    hasStartedChat(isStarted) {
      if (isStarted) {
        this.$nextTick(() => {
          this.setupScrollListener();
        });
      }
    },
    'websocketStore.errorMessage': {
      handler(newValue) {
        if (newValue) {
          if (newValue === "") {
            this.snackbarVisible = false; // Ocultar el snackbar si no hay mensaje de error
          } else {
            this.snackbarVisible = true; // Mostrar el snackbar si hay un mensaje de error
          }
        }
      },
      immediate: true,
    },
  },
  mounted() {
    this.$nextTick(() => {
      this.setupScrollListener();
      this.handleKeyboardChange(); // NUEVO: Inicia detección de teclado
    });
  },
  beforeUnmount() {
    this.removeScrollListener();
  },
};
</script>

<style scoped>
/* Layout principal */
.chat-layout {
  height: 100dvh;
  /* Altura dinámica (o 100vh si prefieres fija) */
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;

  /* CLAVE: Aquí está la magia.
    Empujamos todo el layout desde abajo la altura del teclado.
    El `flex: 1` del messages-area se encogerá automáticamente.
  */
  padding-bottom: var(--keyboard-height, 0px);
  transition: padding-bottom 0.15s ease-out;
}

/* Barra superior */
.app-bar {
  background-color: transparent !important;
  flex-shrink: 0;
  z-index: 1000;
}

@media (max-width: 999px) {
  .app-bar {
    background-color: white !important;
    box-shadow: 0 3px 5px -1px rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.14), 0 1px 18px 0 rgba(0, 0, 0, 0.12) !important;
  }

  .chat-layout {
    padding-top: 60px;
  }
}

.app-title {
  font-size: 1.5rem;
  font-weight: bold;
  cursor: pointer;
}

/* Contenido principal */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Welcome container */
.welcome-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
}

.responsive-logo {
  width: 200px;
  height: 200px;
  max-width: 100%;
  transition: width 0.3s ease, height 0.3s ease;
}

@media (min-width: 768px) {
  .responsive-logo {
    width: 250px;
    height: 250px;
  }
}

/* Layout del chat */
.chat-layout-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

/* Estado inicial: empresa centrada con input */
.initial-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 40px;
}

/* Información de la empresa */
.company-info {
  text-align: center;
  padding: 0 20px;
}

.company-name {
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-message {
  font-size: 1.25rem;
  font-weight: 400;
  color: #718096;
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .company-name {
    font-size: 1.5rem;
  }

  .welcome-message {
    font-size: 1.1rem;
  }

  .initial-state {
    gap: 30px;
    padding: 16px;
  }
}

/* 1. Base (móviles, < 768px) */
.input-area-centered {
  width: 100%;
  max-width: 98%;
  /* <-- AJUSTADO (antes era 70%) */
  padding: 0 16px;
  margin: 0 auto;
  /* Para centrar el elemento */
  box-sizing: border-box;
}

/* 2. Medianas (>= 768px) */
@media (min-width: 768px) {
  .input-area-centered {
    max-width: 98%;
  }
}

/* 3. Grandes (>= 1000px) */
@media (min-width: 1000px) {
  .input-area-centered {
    max-width: 70%;
  }
}

/* Contenedor moderno del input */
.input-container-modern {
  background: white;
  border-radius: 24px;
  border: 1px solid #e2e8f0;
  padding: 6px;
  transition: all 0.3s ease;
}

.input-container-modern:hover {
  border-color: #cbd5e0;
}

.input-container-modern:focus-within {
  border-color: #667eea;
}

/* Indicador de conexión en el input */
.connection-status-input {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 16px 2px 16px;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.connection-status-input:hover {
  opacity: 1;
}

.connection-status-input .status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.connection-status-input .status-dot.connected {
  background-color: #48bb78;
  box-shadow: 0 0 4px rgba(72, 187, 120, 0.4);
}

.connection-status-input .status-dot.disconnected {
  background-color: #f56565;
  box-shadow: 0 0 4px rgba(245, 101, 101, 0.4);
  animation: pulse-error 2s infinite;
}

.connection-status-input .status-text {
  font-size: 15px;
  font-weight: 500;
  color: #718096;
}

@keyframes pulse-error {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.6;
  }
}

/* Estado activo del chat */
.chat-active-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

/* Área de mensajes con scroll dinámico */
.messages-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 16px env(safe-area-inset-bottom, 0px) 16px;
  /* AGREGADO: padding dinámico para teclado/notch */
  scroll-behavior: smooth;
  min-height: 0;
  /* Permite que se encoja */
  /* Mejoras para móviles */
  -webkit-overflow-scrolling: touch;
  /* Ocupar todo el espacio disponible */
  height: 100%;
  padding: 16px 16px calc(16px + env(safe-area-inset-bottom, 0px)) 16px;
  /* Cambia el bottom a safe-area, no keyboard */
}

@media (min-width: 1000px) {
  .messages-area {
    padding: 16px 15% env(safe-area-inset-bottom, 0px) 15%;
  }
}

/* Contenedor de mensajes */
.messages-container {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 32px 0 16px 0;
}

/* Input fijo cuando hay chat activo */
.input-area-fixed {
  flex-shrink: 0;
  background: transparent;
  z-index: 100;
  width: 100%;
  max-width: 95%;
  margin: 0 auto;
  box-shadow: #ffffff 0px -2px 6px -2px;
  /* CLAVE: Usamos 'safe-area' para el notch/barra de iPhone */
  padding-bottom: calc(env(safe-area-inset-bottom, 0px));
}

@media (min-width: 1000px) {
  .input-area-fixed {
    box-shadow: #ffffff 0px -2px 6px -2px;
    padding: 6px 6px calc(env(safe-area-inset-bottom, 0px)) 6px;
    max-width: 70%;
  }
}

@media (max-width: 768px) {
  .input-area-fixed {
    /* CORREGIDO: Solo safe-area */
    box-shadow: #160e0e 0px -2px 6px -2px;
    padding: 6px 6px calc(env(safe-area-inset-bottom, 0px)) 6px;
    margin-bottom: 0;
    max-width: 100%;
  }
}

/* Botón flotante para scroll */
.scroll-down-btn {
  position: fixed;
  bottom: 120px;
  right: 16px;
  z-index: 200;
  background: white !important;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2) !important;
}

@media (min-width: 1000px) {
  .scroll-down-btn {
    right: 17%;
  }
}

/* Soporte para dispositivos con notch/safe area */
@supports (padding: max(0px)) {
  .input-area-fixed {
    padding-bottom: max(12px, env(safe-area-inset-bottom));
  }

  @media (max-width: 768px) {
    .input-area-fixed {
      padding-bottom: max(12px, env(safe-area-inset-bottom));
      margin-bottom: 20px;
    }
  }
}

/* Modo oscuro */
@media (prefers-color-scheme: dark) {
  .input-container-modern {
    background: #2d3748;
    border-color: #4a5568;
  }

  .input-container-modern:hover {
    border-color: #718096;
  }

  .input-container-modern:focus-within {
    border-color: #667eea;
  }

  .connection-status-input .status-text {
    color: #a0aec0;
  }
}

/* Mejoras específicas para móviles */
@media (max-width: 768px) {
  .main-content {
    height: calc(100dvh - 56px);
  }

  .messages-area {
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom, 0px)) 12px;
  }

  .messages-container {
    padding: 12px 0;
  }

  .scroll-down-btn {
    bottom: 120px;
  }
}

/* Prevenir zoom en inputs en iOS */
@media screen and (max-width: 768px) {

  :deep(input[type="text"]),
  :deep(input[type="email"]),
  :deep(input[type="password"]),
  :deep(textarea) {
    font-size: 16px !important;
  }
}

@media screen and (max-device-width: 768px) {
  .chat-layout {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
  }
}
</style>