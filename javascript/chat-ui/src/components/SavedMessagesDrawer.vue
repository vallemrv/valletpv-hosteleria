<template>
  <v-navigation-drawer
    v-model="isOpen"
    location="right"
    width="400"
    temporary
    class="saved-messages-drawer"
  >
    <!-- Header del drawer -->
    <v-toolbar flat color="primary" dark>
      <v-toolbar-title class="text-h6">
        <v-icon class="mr-2">mdi-bookmark-multiple</v-icon>
        Mensajes Guardados
      </v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="closeDrawer">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-toolbar>

    <!-- Contenido principal -->
    <v-container fluid class="pa-0">
      <!-- Indicador de carga -->
      <v-progress-linear
        v-if="savedMessagesStore.isLoading"
        indeterminate
        color="primary"
      ></v-progress-linear>

      <!-- Mensaje de error -->
      <v-alert
        v-if="savedMessagesStore.error"
        type="error"
        class="ma-4"
        closable
        @click:close="savedMessagesStore.clearError()"
      >
        {{ savedMessagesStore.error }}
      </v-alert>

      <!-- Pestañas por categorías -->
      <v-tabs
        v-model="selectedCategory"
        color="primary"
        show-arrows
        class="tabs-container"
      >
        <v-tab
          v-for="category in savedMessagesStore.categories"
          :key="category.id"
          :value="category.id"
          class="text-caption"
        >
          <v-icon :color="category.color" class="mr-1" size="small">
            {{ category.icon }}
          </v-icon>
          {{ category.name }}
          <v-chip
            v-if="getMessageCount(category.id) > 0"
            :color="category.color"
            size="x-small"
            class="ml-1"
          >
            {{ getMessageCount(category.id) }}
          </v-chip>
        </v-tab>
      </v-tabs>

      <!-- Contenido de las pestañas -->
      <v-tabs-window v-model="selectedCategory" class="tab-content">
        <v-tabs-window-item
          v-for="category in savedMessagesStore.categories"
          :key="category.id"
          :value="category.id"
        >
          <div class="pa-4">
            <!-- Descripción de la categoría -->
            <v-card class="mb-4" variant="tonal" :color="category.color">
              <v-card-text class="pa-3">
                <div class="d-flex align-center">
                  <v-icon :color="category.color" class="mr-2">{{ category.icon }}</v-icon>
                  <div>
                    <div class="font-weight-medium">{{ category.name }}</div>
                    <div class="text-caption">{{ category.description }}</div>
                  </div>
                </div>
              </v-card-text>
            </v-card>

            <!-- Lista de mensajes -->
            <div v-if="getCategoryMessages(category.id).length === 0" class="text-center pa-8">
              <v-icon size="64" color="grey-lighten-1">mdi-message-outline</v-icon>
              <div class="text-h6 mt-2 grey--text">No hay mensajes guardados</div>
              <div class="text-caption grey--text">
                Los mensajes guardados en esta categoría aparecerán aquí
              </div>
            </div>

            <v-card
              v-for="message in getCategoryMessages(category.id)"
              :key="message.id"
              class="mb-3"
              elevation="2"
            >
              <v-card-text class="pa-3">
                <!-- Header del mensaje -->
                <div class="d-flex align-center mb-2">
                  <v-chip
                    :color="getTypeColor(message.type)"
                    size="small"
                    class="mr-2"
                  >
                    <v-icon size="small" class="mr-1">{{ getTypeIcon(message.type) }}</v-icon>
                    {{ message.type }}
                  </v-chip>
                  <v-chip
                    :color="getSenderColor(message.sender)"
                    size="small"
                    variant="outlined"
                  >
                    {{ message.sender }}
                  </v-chip>
                  <v-spacer></v-spacer>
                  <v-menu>
                    <template v-slot:activator="{ props }">
                      <v-btn
                        icon="mdi-dots-vertical"
                        variant="text"
                        size="small"
                        v-bind="props"
                      ></v-btn>
                    </template>
                    <v-list>
                      <v-list-item @click="editMessage(message)">
                        <v-list-item-title>
                          <v-icon class="mr-2">mdi-pencil</v-icon>
                          Editar
                        </v-list-item-title>
                      </v-list-item>
                      <v-list-item @click="copyMessage(message.text)">
                        <v-list-item-title>
                          <v-icon class="mr-2">mdi-content-copy</v-icon>
                          Copiar
                        </v-list-item-title>
                      </v-list-item>
                      <v-list-item @click="useMessage(message.text)">
                        <v-list-item-title>
                          <v-icon class="mr-2">mdi-send</v-icon>
                          Usar en chat
                        </v-list-item-title>
                      </v-list-item>
                      <v-divider></v-divider>
                      <v-list-item @click="deleteMessage(message)" class="text-error">
                        <v-list-item-title>
                          <v-icon class="mr-2">mdi-delete</v-icon>
                          Eliminar
                        </v-list-item-title>
                      </v-list-item>
                    </v-list>
                  </v-menu>
                </div>

                <!-- Contenido del mensaje -->
                <div class="message-content">
                  {{ message.text }}
                </div>

                <!-- Tags -->
                <div v-if="message.tags && message.tags.length > 0" class="mt-2">
                  <v-chip
                    v-for="tag in message.tags"
                    :key="tag"
                    size="x-small"
                    class="mr-1"
                    variant="outlined"
                  >
                    {{ tag }}
                  </v-chip>
                </div>

                <!-- Timestamp -->
                <div class="text-caption grey--text mt-2">
                  {{ formatTimestamp(message.timestamp) }}
                </div>
              </v-card-text>
            </v-card>
          </div>
        </v-tabs-window-item>
      </v-tabs-window>
    </v-container>

    <!-- Dialog para editar mensaje -->
    <v-dialog v-model="editDialog" max-width="500">
      <v-card>
        <v-card-title>
          <v-icon class="mr-2">mdi-pencil</v-icon>
          Editar Mensaje
        </v-card-title>
        <v-card-text>
          <v-textarea
            v-model="editingMessage.text"
            label="Texto del mensaje"
            rows="3"
            outlined
          ></v-textarea>
          
          <v-select
            v-model="editingMessage.category"
            :items="categoryOptions"
            label="Categoría"
            outlined
          ></v-select>

          <v-combobox
            v-model="editingMessage.tags"
            label="Tags"
            multiple
            chips
            deletable-chips
            outlined
          ></v-combobox>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="editDialog = false">Cancelar</v-btn>
          <v-btn color="primary" @click="saveEditedMessage">Guardar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog de confirmación para eliminar -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6">
          <v-icon color="error" class="mr-2">mdi-delete</v-icon>
          Confirmar Eliminación
        </v-card-title>
        <v-card-text>
          ¿Estás seguro de que quieres eliminar este mensaje? Esta acción no se puede deshacer.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="deleteDialog = false">Cancelar</v-btn>
          <v-btn color="error" @click="confirmDelete">Eliminar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-navigation-drawer>
</template>

<script>
import { useSavedMessagesStore } from '@/stores/savedMessagesStore';
import { useWebSocketStore } from '@/stores/websocketStore';

export default {
  name: 'SavedMessagesDrawer',
  
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },

  emits: ['update:modelValue', 'use-message'],

  setup() {
    const savedMessagesStore = useSavedMessagesStore();
    const webSocketStore = useWebSocketStore();
    
    return {
      savedMessagesStore,
      webSocketStore
    };
  },

  data() {
    return {
      selectedCategory: 'general',
      editDialog: false,
      deleteDialog: false,
      editingMessage: {
        id: '',
        text: '',
        category: '',
        tags: []
      },
      messageToDelete: null,
    };
  },

  computed: {
    isOpen: {
      get() {
        return this.modelValue;
      },
      set(value) {
        this.$emit('update:modelValue', value);
      }
    },

    categoryOptions() {
      return this.savedMessagesStore.categories.map(cat => ({
        title: cat.name,
        value: cat.id,
        props: {
          prependIcon: cat.icon,
          color: cat.color
        }
      }));
    }
  },

  watch: {
    modelValue(newValue) {
      if (newValue) {
        this.loadMessages();
      }
    }
  },

  methods: {
    closeDrawer() {
      this.isOpen = false;
    },

    async loadMessages() {
      await this.savedMessagesStore.loadSavedMessages();
    },

    getCategoryMessages(categoryId) {
      return this.savedMessagesStore.getMessagesByCategory(categoryId);
    },

    getMessageCount(categoryId) {
      return this.getCategoryMessages(categoryId).length;
    },

    getTypeColor(type) {
      const colors = {
        message: 'blue',
        status: 'orange',
        error: 'red',
        pedido_confirmation: 'green',
        welcome: 'purple'
      };
      return colors[type] || 'grey';
    },

    getTypeIcon(type) {
      const icons = {
        message: 'mdi-message-text',
        status: 'mdi-information',
        error: 'mdi-alert-circle',
        pedido_confirmation: 'mdi-check-circle',
        welcome: 'mdi-hand-wave'
      };
      return icons[type] || 'mdi-message';
    },

    getSenderColor(sender) {
      const colors = {
        user: 'primary',
        bot: 'success',
        status: 'warning'
      };
      return colors[sender] || 'grey';
    },

    formatTimestamp(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    editMessage(message) {
      this.editingMessage = {
        id: message.id,
        text: message.text,
        category: message.category,
        tags: [...message.tags]
      };
      this.editDialog = true;
    },

    async saveEditedMessage() {
      const updates = {
        text: this.editingMessage.text,
        category: this.editingMessage.category,
        tags: this.editingMessage.tags
      };

      const success = await this.savedMessagesStore.updateMessage(
        this.editingMessage.id,
        updates
      );

      if (success) {
        this.editDialog = false;
        this.$nextTick(() => {
          // Cambiar a la categoría del mensaje editado si es diferente
          this.selectedCategory = this.editingMessage.category;
        });
      }
    },

    copyMessage(text) {
      navigator.clipboard.writeText(text).then(() => {
        // Aquí podrías mostrar un snackbar de confirmación
        console.log('Mensaje copiado al portapapeles');
      }).catch(err => {
        console.error('Error al copiar al portapapeles:', err);
      });
    },

    useMessage(text) {
      this.$emit('use-message', text);
      this.closeDrawer();
    },

    deleteMessage(message) {
      this.messageToDelete = message;
      this.deleteDialog = true;
    },

    async confirmDelete() {
      if (this.messageToDelete) {
        const success = await this.savedMessagesStore.deleteMessage(
          this.messageToDelete.id
        );
        
        if (success) {
          this.deleteDialog = false;
          this.messageToDelete = null;
        }
      }
    }
  }
};
</script>

<style scoped>
.saved-messages-drawer {
  z-index: 9999;
}

.tabs-container {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.tab-content {
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}

.message-content {
  line-height: 1.4;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* Estilos responsivos */
@media (max-width: 600px) {
  .saved-messages-drawer {
    width: 100% !important;
  }
}
</style>
