<!-- SavedMessagesPanel.vue -->
<template>
  <v-navigation-drawer
    v-model="isOpen"
    location="right"
    width="450"
    temporary
    class="saved-messages-drawer"
  >
    <v-toolbar density="compact" color="primary">
      <v-icon class="mr-2">mdi-bookmark-multiple</v-icon>
      <v-toolbar-title>Mensajes Guardados</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn 
        icon 
        @click="reloadData"
        :loading="isLoading || savedMessagesStore.isLoading"
        title="Recargar datos"
      >
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
      <v-btn icon @click="closeDrawer">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-toolbar>

    <!-- Búsqueda -->
    <v-card-text class="pb-2">
      <v-text-field
        v-model="searchTerm"
        label="Buscar mensajes..."
        variant="outlined"
        density="compact"
        prepend-inner-icon="mdi-magnify"
        clearable
        hide-details
      />
    </v-card-text>

    <!-- Loading -->
    <v-progress-linear
      v-if="isLoading || savedMessagesStore.isLoading"
      indeterminate
      color="primary"
    ></v-progress-linear>

    <!-- Contenido principal -->
    <v-container fluid class="pa-0">
      <!-- Tabs por categorías -->
      <v-tabs
        v-model="selectedCategoryId"
        color="primary"
        show-arrows
        class="tabs-container"
      >
        <v-tab value="all">
          <v-icon class="mr-1">mdi-all-inclusive</v-icon>
          Todos ({{ totalMessages }})
        </v-tab>
        <v-tab 
          v-for="category in savedMessagesStore.categories" 
          :key="category.id"
          :value="category.id"
        >
          {{ category.name }} ({{ category.message_count }})
        </v-tab>
        <v-tab value="uncategorized" v-if="uncategorizedCount > 0">
          <v-icon class="mr-1">mdi-help-circle-outline</v-icon>
          Sin categoría ({{ uncategorizedCount }})
        </v-tab>
      </v-tabs>

      <!-- Contenido de las tabs -->
      <div class="messages-content">
        <!-- Lista de mensajes -->
        <v-list v-if="filteredMessages.length > 0" lines="three" class="pa-0">
          <v-list-item
            v-for="message in filteredMessages"
            :key="message.id"
            @click="reuseMessage(message)"
            class="saved-message-item border-b"
          >
            <template v-slot:prepend>
              <v-avatar color="primary" size="small">
                <v-icon>mdi-robot</v-icon>
              </v-avatar>
            </template>

            <v-list-item-title class="text-subtitle-2 mb-1">
              {{ message.titulo || 'Sin título' }}
            </v-list-item-title>
            
            <v-list-item-subtitle class="text-caption mb-2">
              <div class="message-preview" v-html="getPreview(message.texto_html_raw)"></div>
            </v-list-item-subtitle>
            
            <v-list-item-subtitle class="text-caption text-grey">
              <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
              {{ formatDate(message.created_at) }}
              <span v-if="message.category" class="ml-2">
                <v-icon size="small" class="mr-1">mdi-folder-outline</v-icon>
                {{ getCategoryName(message.category.id) }}
              </span>
            </v-list-item-subtitle>

            <template v-slot:append>
              <div class="d-flex flex-column">
                <v-btn
                  icon
                  size="small"
                  variant="text"
                  @click.stop="copyMessage(message.texto_html_raw)"
                >
                  <v-icon size="small">mdi-content-copy</v-icon>
                  <v-tooltip activator="parent">Copiar</v-tooltip>
                </v-btn>
                <v-btn
                  icon
                  size="small"
                  variant="text"
                  color="error"
                  @click.stop="deleteMessage(message)"
                >
                  <v-icon size="small">mdi-delete-outline</v-icon>
                  <v-tooltip activator="parent">Eliminar</v-tooltip>
                </v-btn>
              </div>
            </template>
          </v-list-item>
        </v-list>

        <!-- Estado vacío -->
        <v-card v-else class="ma-4" variant="outlined">
          <v-card-text class="text-center py-8">
            <v-icon size="64" color="grey-lighten-1">mdi-bookmark-outline</v-icon>
            <p class="text-h6 mt-4 mb-2">
              {{ searchTerm ? 'No se encontraron mensajes' : 'No hay mensajes guardados' }}
            </p>
            <p class="text-body-2 text-grey">
              {{ searchTerm 
                ? 'Intenta con otros términos de búsqueda' 
                : 'Guarda respuestas del bot para acceder rápidamente a ellas' 
              }}
            </p>
            
            <!-- Error message si existe -->
            <div v-if="savedMessagesStore.error" class="text-error mt-4">
              <v-icon class="mr-2">mdi-alert-circle</v-icon>
              {{ savedMessagesStore.error }}
            </div>
          </v-card-text>
        </v-card>
      </div>
    </v-container>

    <!-- Panel de gestión de categorías -->
    <template v-slot:append>
      <v-divider></v-divider>
      <v-expansion-panels variant="accordion" class="ma-2">
        <v-expansion-panel>
          <v-expansion-panel-title>
            <v-icon class="mr-2">mdi-folder-cog</v-icon>
            Gestionar Categorías ({{ savedMessagesStore.categories.length }})
          </v-expansion-panel-title>
          <v-expansion-panel-text>
            <!-- Lista de categorías -->
            <v-list density="compact">
              <v-list-item
                v-for="category in savedMessagesStore.categories"
                :key="category.id"
                class="px-0"
              >
                <v-list-item-title>{{ category.name }}</v-list-item-title>
                <v-list-item-subtitle>{{ category.message_count }} mensajes</v-list-item-subtitle>
                <template v-slot:append>
                  <v-btn
                    icon
                    size="small"
                    variant="text"
                    color="error"
                    @click="deleteCategory(category)"
                    :disabled="category.message_count > 0"
                  >
                    <v-icon size="small">mdi-delete-outline</v-icon>
                    <v-tooltip activator="parent">
                      {{ category.message_count > 0 ? 'No se puede eliminar (tiene mensajes)' : 'Eliminar categoría' }}
                    </v-tooltip>
                  </v-btn>
                </template>
              </v-list-item>
            </v-list>

            <!-- Crear nueva categoría -->
            <v-divider class="my-2"></v-divider>
            <div class="d-flex gap-2">
              <v-text-field
                v-model="newCategoryName"
                label="Nueva categoría"
                density="compact"
                variant="outlined"
                hide-details
                @keyup.enter="createCategory"
              />
              <v-btn
                color="primary"
                @click="createCategory"
                :disabled="!newCategoryName.trim()"
                :loading="isCreatingCategory || savedMessagesStore.isCreatingCategory"
              >
                Crear
              </v-btn>
            </div>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
    </template>
  </v-navigation-drawer>

  <!-- Dialog de confirmación para eliminar -->
  <v-dialog v-model="showDeleteDialog" max-width="400">
    <v-card>
      <v-card-title>Confirmar eliminación</v-card-title>
      <v-card-text>
        ¿Estás seguro de que quieres eliminar este mensaje?
        <strong>{{ messageToDelete?.titulo }}</strong>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="showDeleteDialog = false">Cancelar</v-btn>
        <v-btn color="error" @click="confirmDelete">Eliminar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useSavedMessagesStore } from '@/stores/savedMessagesStore';
import { useCompanyStore } from '@/stores/companyStore';

interface Props {
  modelValue: boolean;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'reuseMessage', htmlContent: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { showSuccess, showError } = useNotifications();
const savedMessagesStore = useSavedMessagesStore();
const companyStore = useCompanyStore();

// Estado reactivo local
const isLoading = ref(false);
const isCreatingCategory = ref(false);
const searchTerm = ref('');
const selectedCategoryId = ref<number | string>('all');
const newCategoryName = ref('');
const showDeleteDialog = ref(false);
const messageToDelete = ref<any>(null);

// Computed properties usando el store
const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const totalMessages = computed(() => savedMessagesStore.totalMessages);

const uncategorizedCount = computed(() => savedMessagesStore.uncategorizedMessages.length);

const filteredMessages = computed(() => {
  // Asegurar que messages sea un array
  const messagesList = Array.isArray(savedMessagesStore.messages) ? savedMessagesStore.messages : [];
  let filtered = messagesList;

  // Filtrar por categoría
  if (selectedCategoryId.value === 'uncategorized') {
    filtered = filtered.filter(msg => !msg.category);
  } else if (selectedCategoryId.value !== 'all') {
    filtered = filtered.filter(msg => msg.category?.id === selectedCategoryId.value);
  }

  // Filtrar por búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    filtered = filtered.filter(msg =>
      (msg.titulo || '').toLowerCase().includes(term) ||
      getPlainText(msg.texto_html_raw || '').toLowerCase().includes(term)
    );
  }

  return filtered.sort((a, b) => 
    new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
  );
});

// Methods
const closeDrawer = () => {
  isOpen.value = false;
};

const reloadData = async () => {
  await Promise.all([loadMessages(), loadCategories()]);
};

const loadMessages = async () => {
  const companyStore = useCompanyStore();
  if (!companyStore.companyData) {
    savedMessagesStore.error = 'No hay empresa seleccionada';
    return;
  }

  const { url, token, user } = companyStore.companyData;
  if (!url || !token || !user) {
    savedMessagesStore.error = 'Datos de empresa incompletos';
    return;
  }
  
  isLoading.value = true;
  try {
    const success = await savedMessagesStore.loadMessages({
      search: searchTerm.value,
      categoryId: selectedCategoryId.value !== 'all' && selectedCategoryId.value !== 'uncategorized' 
        ? Number(selectedCategoryId.value) 
        : undefined
    });
    
    if (!success && savedMessagesStore.error) {
      showError(savedMessagesStore.error);
    }
  } catch (error) {
    showError('Error inesperado al cargar mensajes');
  } finally {
    isLoading.value = false;
  }
};

const loadCategories = async () => {
  const companyStore = useCompanyStore();
  if (!companyStore.companyData) {
    savedMessagesStore.error = 'No hay empresa seleccionada';
    return;
  }

  const { url, token, user } = companyStore.companyData;
  if (!url || !token || !user) {
    savedMessagesStore.error = 'Datos de empresa incompletos';
    return;
  }
  
  try {
    const success = await savedMessagesStore.loadCategories();
    
    if (!success && savedMessagesStore.error) {
      showError(savedMessagesStore.error);
    }
  } catch (error) {
    showError('Error inesperado al cargar categorías');
  }
};

const createCategory = async () => {
  if (!newCategoryName.value.trim()) return;

  isCreatingCategory.value = true;
  try {
    const result = await savedMessagesStore.createCategory({
      name: newCategoryName.value.trim()
    });

    if (result) {
      showSuccess('Categoría creada exitosamente');
      newCategoryName.value = '';
      await loadCategories();
    } else if (savedMessagesStore.error) {
      showError(savedMessagesStore.error);
    }
  } finally {
    isCreatingCategory.value = false;
  }
};

const deleteCategory = async (category: any) => {
  if (category.message_count > 0) return;

  try {
    const result = await savedMessagesStore.deleteCategory(category.id);
    if (result) {
      showSuccess('Categoría eliminada exitosamente');
      await loadCategories();
    } else if (savedMessagesStore.error) {
      showError(savedMessagesStore.error);
    }
  } catch (error) {
    showError('Error al eliminar la categoría');
  }
};

const deleteMessage = (message: any) => {
  messageToDelete.value = message;
  showDeleteDialog.value = true;
};

const confirmDelete = async () => {
  if (!messageToDelete.value) return;

  try {
    const result = await savedMessagesStore.deleteMessage(messageToDelete.value.id);
    if (result) {
      showSuccess('Mensaje eliminado exitosamente');
      await Promise.all([loadMessages(), loadCategories()]);
    } else if (savedMessagesStore.error) {
      showError(savedMessagesStore.error);
    }
  } catch (error) {
    showError('Error al eliminar el mensaje');
  } finally {
    showDeleteDialog.value = false;
    messageToDelete.value = null;
  }
};

const reuseMessage = (message: any) => {
  emit('reuseMessage', message.texto_html_raw);
  closeDrawer();
  showSuccess(`Reutilizando: ${message.titulo}`);
};

const copyMessage = async (htmlContent: string) => {
  try {
    const plainText = getPlainText(htmlContent);
    await navigator.clipboard.writeText(plainText);
    showSuccess('Mensaje copiado al portapapeles');
  } catch (error) {
    showError('Error al copiar al portapapeles');
  }
};

const getPlainText = (html: string): string => {
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = html;
  return tempDiv.textContent || tempDiv.innerText || '';
};

const getPreview = (html: string): string => {
  const plainText = getPlainText(html);
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText;
};

const getCategoryName = (categoryId: number): string => {
  const category = savedMessagesStore.categories.find(cat => cat.id === categoryId);
  return category?.name || 'Sin categoría';
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// Watchers
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    Promise.all([loadMessages(), loadCategories()]);
  }
});

// Lifecycle
onMounted(() => {
  if (props.modelValue) {
    Promise.all([loadMessages(), loadCategories()]);
  }
});
</script>

<style scoped>
.saved-messages-drawer {
  z-index: 1000;
}

.tabs-container {
  border-bottom: 1px solid rgba(0,0,0,0.1);
}

.messages-content {
  height: calc(100vh - 250px);
  overflow-y: auto;
}

.saved-message-item {
  transition: background-color 0.2s;
  cursor: pointer;
}

.saved-message-item:hover {
  background-color: rgba(0,0,0,0.04);
}

.message-preview {
  font-size: 0.8rem;
  line-height: 1.3;
  color: #666;
}

.border-b {
  border-bottom: 1px solid rgba(0,0,0,0.1);
}
</style>
