<template>
  <v-menu v-model="menu" :close-on-content-click="false" offset-y>
    <template v-slot:activator="{ props }">
      <v-btn
        icon
        size="small"
        v-bind="props"
        :color="isSaved ? 'success' : 'grey'"
        :loading="loading"
        class="save-message-btn"
      >
        <v-icon size="small">
          {{ isSaved ? 'mdi-bookmark-check' : 'mdi-bookmark-plus-outline' }}
        </v-icon>
      </v-btn>
    </template>

    <v-card min-width="300">
      <v-card-title class="text-h6 pa-3">
        <v-icon class="mr-2">mdi-bookmark-plus</v-icon>
        Guardar Mensaje
      </v-card-title>

      <v-card-text class="pa-3">
        <!-- Previsualización del mensaje -->
        <v-card variant="outlined" class="mb-3">
          <v-card-text class="pa-2">
            <div class="text-caption grey--text mb-1">Mensaje a guardar:</div>
            <div class="message-preview">
              {{ truncateMessage(message.text) }}
            </div>
          </v-card-text>
        </v-card>

        <!-- Selección de categoría -->
        <v-select
          v-model="selectedCategory"
          :items="categoryOptions"
          label="Categoría"
          variant="outlined"
          density="compact"
          hide-details
          class="mb-3"
        >
          <template v-slot:item="{ props, item }">
            <v-list-item v-bind="props">
              <template v-slot:prepend>
                <v-icon :color="item.raw.color">{{ item.raw.icon }}</v-icon>
              </template>
            </v-list-item>
          </template>
          <template v-slot:selection="{ item }">
            <v-icon :color="item.raw.color" class="mr-2">{{ item.raw.icon }}</v-icon>
            {{ item.title }}
          </template>
        </v-select>

        <!-- Tags opcionales -->
        <v-combobox
          v-model="tags"
          label="Tags (opcional)"
          multiple
          chips
          deletable-chips
          variant="outlined"
          density="compact"
          hide-details
          placeholder="Agregar tags..."
        ></v-combobox>
      </v-card-text>

      <v-card-actions class="pa-3">
        <v-spacer></v-spacer>
        <v-btn text @click="menu = false">Cancelar</v-btn>
        <v-btn
          color="primary"
          :loading="loading"
          @click="saveMessage"
        >
          <v-icon class="mr-1">mdi-bookmark-plus</v-icon>
          Guardar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script>
import { useSavedMessagesStore } from '@/stores/savedMessagesStore';
import { useNotifications } from '@/composables/useNotifications';

export default {
  name: 'SaveMessageButton',
  
  props: {
    message: {
      type: Object,
      required: true,
      validator(value) {
        return value && typeof value.text === 'string' && 
               typeof value.sender === 'string';
      }
    }
  },

  emits: ['saved'],

  setup() {
    const savedMessagesStore = useSavedMessagesStore();
    const { showSuccess, showError } = useNotifications();
    
    return { 
      savedMessagesStore,
      showSuccess,
      showError
    };
  },

  data() {
    return {
      menu: false,
      loading: false,
      selectedCategory: 'general',
      tags: [],
      isSaved: false
    };
  },

  computed: {
    categoryOptions() {
      return this.savedMessagesStore.categories.map(cat => ({
        title: cat.name,
        value: cat.id,
        color: cat.color,
        icon: cat.icon
      }));
    }
  },

  watch: {
    'message.id': {
      handler() {
        this.checkIfSaved();
      },
      immediate: true
    }
  },

  methods: {
    checkIfSaved() {
      // Verificar si el mensaje ya está guardado
      this.isSaved = this.savedMessagesStore.savedMessages.some(
        saved => saved.text === this.message.text
      );
    },

    truncateMessage(text, maxLength = 100) {
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },

    getMessageType() {
      // Determinar el tipo de mensaje basado en el contenido o propiedades
      if (this.message.type) {
        return this.message.type;
      }
      
      // Inferir tipo basado en el contenido
      const text = this.message.text.toLowerCase();
      
      if (text.includes('error') || text.includes('❌')) {
        return 'error';
      }
      if (text.includes('bienvenido') || text.includes('conectado') || text.includes('✅')) {
        return 'welcome';
      }
      if (text.includes('pedido') || text.includes('🍽️') || text.includes('confirmado')) {
        return 'pedido_confirmation';
      }
      if (text.includes('procesando') || text.includes('analizando') || text.includes('🔍')) {
        return 'status';
      }
      
      return 'message';
    },

    suggestCategory() {
      const type = this.getMessageType();
      
      const categoryMap = {
        'error': 'errores',
        'welcome': 'bienvenida', 
        'pedido_confirmation': 'pedidos',
        'status': 'general',
        'message': 'general'
      };
      
      return categoryMap[type] || 'general';
    },

    suggestTags() {
      const text = this.message.text.toLowerCase();
      const suggestedTags = [];
      
      // Tags basados en palabras clave
      if (text.includes('pedido')) suggestedTags.push('pedido');
      if (text.includes('error')) suggestedTags.push('error');
      if (text.includes('conexión') || text.includes('conectado')) suggestedTags.push('conexión');
      if (text.includes('procesando')) suggestedTags.push('procesamiento');
      if (text.includes('confirmación') || text.includes('confirmado')) suggestedTags.push('confirmación');
      
      // Tag basado en el sender
      if (this.message.sender === 'bot') suggestedTags.push('bot');
      if (this.message.sender === 'user') suggestedTags.push('usuario');
      if (this.message.sender === 'status') suggestedTags.push('sistema');
      
      return suggestedTags;
    },

    async saveMessage() {
      if (this.isSaved) {
        this.menu = false;
        return;
      }

      this.loading = true;
      
      try {
        const messageToSave = {
          text: this.message.text,
          type: this.getMessageType(),
          sender: this.message.sender,
          category: this.selectedCategory,
          tags: this.tags.length > 0 ? this.tags : this.suggestTags()
        };

        const success = await this.savedMessagesStore.saveMessage(messageToSave);
        
        if (success) {
          this.isSaved = true;
          this.menu = false;
          this.$emit('saved', messageToSave);
          
          // Mostrar notificación de éxito
          this.showSuccess('Mensaje guardado exitosamente');
        } else if (this.savedMessagesStore.error) {
          this.showError(this.savedMessagesStore.error);
        }
      } catch (error) {
        console.error('Error saving message:', error);
        this.showError('Error inesperado al guardar el mensaje');
      } finally {
        this.loading = false;
      }
    }
  },

  mounted() {
    // Auto-sugerir categoría y tags cuando se abre el menú
    this.selectedCategory = this.suggestCategory();
    this.tags = this.suggestTags();
  }
};
</script>

<style scoped>
.save-message-btn {
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.save-message-btn:hover {
  opacity: 1;
}

.message-preview {
  font-size: 0.875rem;
  line-height: 1.4;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
