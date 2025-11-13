<!-- SaveMessageDialog.vue -->
<template>
  <!-- Dialog para guardar mensaje -->
  <v-dialog v-model="showDialog" max-width="600" persistent>
    <v-card>
      <v-card-title>
        <v-icon class="mr-2">mdi-bookmark-plus</v-icon>
        Guardar respuesta del bot
      </v-card-title>
      
      <v-card-text>
        <!-- Previsualización del mensaje -->
        <v-card variant="outlined" class="mb-4">
          <v-card-subtitle class="pb-2">Vista previa:</v-card-subtitle>
          <v-card-text class="pt-0">
            <div class="message-preview" v-html="truncatedHtml"></div>
          </v-card-text>
        </v-card>

        <!-- Título del mensaje -->
        <v-text-field
          v-model="saveForm.titulo"
          label="Título para recordar"
          placeholder="Ej: Lista de familias, Ventas del mes, etc."
          variant="outlined"
          class="mb-3"
          :rules="[v => !!v || 'El título es requerido']"
        />
        
        <!-- Selección/Creación de categoría -->
        <div class="mb-3">
          <v-label class="text-subtitle-2 mb-2">Categoría</v-label>
          <v-radio-group v-model="categoryMode" inline class="mb-2">
            <v-radio 
              label="Usar categoría existente" 
              value="existing"
              :disabled="userCategories.length === 0"
            ></v-radio>
            <v-radio label="Crear nueva categoría" value="new"></v-radio>
          </v-radio-group>

          <!-- Seleccionar categoría existente -->
          <v-select
            v-if="categoryMode === 'existing'"
            v-model="saveForm.category_id"
            :items="categoryOptions"
            label="Seleccionar categoría"
            variant="outlined"
            :disabled="userCategories.length === 0"
          />

          <!-- Crear nueva categoría -->
          <v-text-field
            v-if="categoryMode === 'new'"
            v-model="newCategoryName"
            label="Nombre de la nueva categoría"
            placeholder="Ej: Familias, Ventas, Clientes..."
            variant="outlined"
            :rules="[v => !!v || 'El nombre de categoría es requerido']"
          />
        </div>

        <!-- Mensaje si no hay categorías -->
        <v-alert
          v-if="userCategories.length === 0"
          type="info"
          variant="outlined"
          class="mb-3"
        >
          <v-icon class="mr-2">mdi-information</v-icon>
          No tienes categorías creadas. Se creará una nueva categoría.
        </v-alert>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog">Cancelar</v-btn>
        <v-btn 
          color="primary" 
          @click="saveMessage"
          :disabled="!isFormValid"
          :loading="isLoading"
        >
          Guardar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useCompanyStore } from '@/stores/companyStore';

interface SaveForm {
  titulo: string;
  category_id: number | null;
}

interface Category {
  id: number;
  name: string;
  message_count: number;
}

const { showSuccess, showError } = useNotifications();
const companyStore = useCompanyStore();

const isLoading = ref(false);
const showDialog = ref(false);
const categoryMode = ref<'existing' | 'new'>('existing');
const newCategoryName = ref('');
const userCategories = ref<Category[]>([]);
const htmlContent = ref('');

// Formulario para guardar
const saveForm = ref<SaveForm>({
  titulo: '',
  category_id: null
});

// Computed properties
const truncatedHtml = computed(() => {
  // Truncar HTML para la previsualización
  const maxLength = 200;
  if (htmlContent.value.length <= maxLength) return htmlContent.value;
  return htmlContent.value.substring(0, maxLength) + '...';
});

const categoryOptions = computed(() => {
  return userCategories.value.map(cat => ({
    title: `${cat.name} (${cat.message_count} mensajes)`,
    value: cat.id,
    subtitle: `${cat.message_count} mensajes guardados`
  }));
});

const isFormValid = computed(() => {
  const hasTitle = saveForm.value.titulo.trim().length > 0;
  
  if (categoryMode.value === 'existing') {
    return hasTitle && (saveForm.value.category_id !== null || userCategories.value.length === 0);
  } else {
    return hasTitle && newCategoryName.value.trim().length > 0;
  }
});

// Methods
const openDialog = async (content: string) => {
  console.log('SaveMessageDialog openDialog called with content:', content);
  htmlContent.value = content;
  showDialog.value = true;
  
  // Cargar categorías cuando se abre el diálogo
  if (companyStore.companyData) {
    await loadUserCategories();
    suggestTitle();
    
    if (userCategories.value.length === 0) {
      categoryMode.value = 'new';
    }
  }
};

const closeDialog = () => {
  showDialog.value = false;
  // Resetear formulario
  saveForm.value = {
    titulo: '',
    category_id: null
  };
  newCategoryName.value = '';
  htmlContent.value = '';
};

const suggestTitle = () => {
  // Extraer texto del HTML para analizar
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = htmlContent.value;
  const textContent = tempDiv.textContent || tempDiv.innerText || '';
  
  const text = textContent.toLowerCase();
  
  // Sugerencias basadas en contenido
  if (text.includes('familia')) {
    saveForm.value.titulo = 'Lista de Familias';
    newCategoryName.value = 'Familias';
  } else if (text.includes('producto')) {
    saveForm.value.titulo = 'Lista de Productos';
    newCategoryName.value = 'Productos';
  } else if (text.includes('venta') || text.includes('factura')) {
    saveForm.value.titulo = 'Datos de Ventas';
    newCategoryName.value = 'Ventas';
  } else if (text.includes('cliente')) {
    saveForm.value.titulo = 'Lista de Clientes';
    newCategoryName.value = 'Clientes';
  } else if (text.includes('tecla')) {
    saveForm.value.titulo = 'Información de Teclas';
    newCategoryName.value = 'Teclas';
  } else {
    // Usar las primeras palabras del texto
    const words = textContent.split(' ').slice(0, 4).join(' ');
    saveForm.value.titulo = words.charAt(0).toUpperCase() + words.slice(1);
    newCategoryName.value = 'General';
  }
};

const loadUserCategories = async () => {
  if (!companyStore.companyData) return;

  try {
    const formData = new FormData();
    formData.append('action', 'list');
    formData.append('user', companyStore.companyData.user.toString());
    formData.append('token', companyStore.companyData.token);

    const response = await fetch(`${companyStore.companyData.url}/api/chatbot/categories/`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      const result = await response.json();
      if (result.success && Array.isArray(result.data)) {
        userCategories.value = result.data;
      }
    }
  } catch (error) {
    console.error('Error loading categories:', error);
  }
};

const createCategory = async (name: string): Promise<number | null> => {
  if (!companyStore.companyData) return null;

  try {
    const formData = new FormData();
    formData.append('action', 'create');
    formData.append('name', name);
    formData.append('user', companyStore.companyData.user.toString());
    formData.append('token', companyStore.companyData.token);

    const response = await fetch(`${companyStore.companyData.url}/api/chatbot/categories/`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      const result = await response.json();
      if (result.success && result.data) {
        return result.data.id;
      }
    }
  } catch (error) {
    console.error('Error creating category:', error);
  }
  
  return null;
};

const saveMessage = async () => {
  console.log('Saving message...');
  if (!companyStore.companyData) {
    showError('No hay compañía seleccionada');
    return;
  }

  isLoading.value = true;

  try {
    let categoryId = saveForm.value.category_id;

    // Crear nueva categoría si es necesario
    if (categoryMode.value === 'new') {
      categoryId = await createCategory(newCategoryName.value);
      if (!categoryId) {
        showError('Error al crear la categoría');
        return;
      }
    }

    // Preparar datos para guardar el mensaje
    const formData = new FormData();
    formData.append('action', 'create');
    formData.append('titulo', saveForm.value.titulo);
    formData.append('texto_html_raw', htmlContent.value);
    formData.append('user', companyStore.companyData.user.toString());
    formData.append('token', companyStore.companyData.token);

    if (categoryId) {
      formData.append('category_id', categoryId.toString());
    }

    // Guardar el mensaje
    const response = await fetch(`${companyStore.companyData.url}/api/chatbot/saved-messages/`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      const result = await response.json();
      if (result.success) {
        showSuccess('Mensaje guardado exitosamente');
        closeDialog();
      } else {
        showError(`Error al guardar: ${result.error}`);
      }
    } else {
      const errorData = await response.json();
      showError(`Error al guardar: ${JSON.stringify(errorData)}`);
    }
  } catch (error) {
    console.error('Error saving message:', error);
    showError('Error de conexión al guardar el mensaje');
  } finally {
    isLoading.value = false;
  }
};

// Exponer métodos para uso externo
defineExpose({
  openDialog
});
</script>

<style scoped>
.message-preview {
  font-size: 0.875rem;
  line-height: 1.4;
  max-height: 120px;
  overflow: hidden;
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
}
</style>
