import { defineStore } from 'pinia';
import { useCompanyStore } from '@/stores/companyStore';
import axios from 'axios';

// Interfaces que coinciden exactamente con el backend Django
interface Category {
  id: number;
  name: string;
  created_at: string;
  message_count: number;
}

interface SavedMessage {
  id: number;
  titulo: string;
  texto_html_raw: string;
  category: Category | null;
  created_at: string;
}

// Interfaces para crear nuevos elementos
interface CreateMessageData {
  titulo: string;
  texto_html_raw: string;
  category_id?: number;
}

interface CreateCategoryData {
  name: string;
}

export const useSavedMessagesStore = defineStore('savedMessages', {
  state: () => ({
    // Datos
    messages: [] as SavedMessage[],
    categories: [] as Category[],
    
    // Estados de carga
    isLoading: false,
    isLoadingCategories: false,
    isCreatingMessage: false,
    isCreatingCategory: false,
    
    // Errores
    error: null as string | null,
    
    // Paginación
    currentPage: 1,
    totalPages: 1,
    totalMessages: 0,
    pageSize: 20,
  }),

  getters: {
    // Mensajes por categoría
    getMessagesByCategory: (state) => (categoryId: number | 'all' | 'uncategorized') => {
      const messagesList = Array.isArray(state.messages) ? state.messages : [];
      if (categoryId === 'all') return messagesList;
      if (categoryId === 'uncategorized') {
        return messagesList.filter(msg => !msg.category);
      }
      return messagesList.filter(msg => msg.category?.id === categoryId);
    },

    // Categoría por ID
    getCategoryById: (state) => (categoryId: number) => {
      const categoriesList = Array.isArray(state.categories) ? state.categories : [];
      return categoriesList.find(cat => cat.id === categoryId);
    },

    // Mensajes sin categoría
    uncategorizedMessages: (state) => {
      const messagesList = Array.isArray(state.messages) ? state.messages : [];
      return messagesList.filter(msg => !msg.category);
    },

    // Buscar mensajes
    searchMessages: (state) => (searchTerm: string) => {
      const messagesList = Array.isArray(state.messages) ? state.messages : [];
      if (!searchTerm.trim()) return messagesList;
      
      const term = searchTerm.toLowerCase();
      return messagesList.filter(msg =>
        (msg.titulo || '').toLowerCase().includes(term) ||
        (msg.texto_html_raw || '').toLowerCase().includes(term)
      );
    },

    // Estadísticas
    stats: (state) => {
      const messagesList = Array.isArray(state.messages) ? state.messages : [];
      const categoriesList = Array.isArray(state.categories) ? state.categories : [];
      
      return {
        totalMessages: state.totalMessages || messagesList.length,
        totalCategories: categoriesList.length,
        uncategorizedCount: messagesList.filter(msg => !msg.category).length,
        categoriesWithCounts: categoriesList.map(cat => ({
          ...cat,
          count: messagesList.filter(msg => msg.category?.id === cat.id).length
        }))
      };
    }
  },

  actions: {
    // Método para crear FormData con user y token (igual que empresaStore)
    createFormData(obj: any = {}) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) return null;
      
      const formData = new FormData();
      formData.append('user', companyStore.companyData.user);
      formData.append('token', companyStore.companyData.token);
      
      for (const key in obj) {
        if (obj.hasOwnProperty(key) && obj[key] !== undefined && obj[key] !== null) {
          if (typeof obj[key] === 'object' && !(obj[key] instanceof File)) {
            formData.append(key, JSON.stringify(obj[key]));
          } else {
            formData.append(key, obj[key].toString());
          }
        }
      }
      
      return formData;
    },

    // Cargar mensajes usando POST con FormData
    async loadMessages(params: { 
      page?: number; 
      pageSize?: number; 
      categoryId?: number; 
      search?: string 
    } = {}) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      this.isLoading = true;
      this.error = null;

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/saved-messages/`;
        
        // Filtrar parámetros válidos
        const cleanParams: any = {
          action: 'list'
        };
        
        if (params.page && params.page > 0) {
          cleanParams.page = params.page;
        }
        
        if (params.pageSize && params.pageSize > 0) {
          cleanParams.page_size = params.pageSize;
        }
        
        if (params.categoryId && params.categoryId > 0) {
          cleanParams.category_id = params.categoryId;
        }
        
        if (params.search && params.search.trim()) {
          cleanParams.search = params.search.trim();
        }
        
        console.log('Sending params:', cleanParams);
        
        const formData = this.createFormData(cleanParams);
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        console.log('Response data:', data);
        
        if (data.success) {
          // Si la respuesta tiene paginación
          if (data.data && Array.isArray(data.data)) {
            this.messages = data.data;
            this.currentPage = data.current_page || 1;
            this.totalPages = data.num_pages || 1;
            this.totalMessages = data.count || data.data.length;
          } else if (data.count !== undefined) {
            // Formato con paginación separada
            this.messages = Array.isArray(data.data) ? data.data : [];
            this.currentPage = data.current_page || 1;
            this.totalPages = data.num_pages || 1;
            this.totalMessages = data.count || 0;
          } else {
            // Si es una lista simple
            this.messages = Array.isArray(data.data) ? data.data : [];
            this.totalMessages = this.messages.length;
          }
          return true;
        } else {
          this.error = data.error || 'Error al cargar mensajes';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al cargar mensajes';
        console.error('Error loading messages:', error);
        return false;
      } finally {
        this.isLoading = false;
        console.log('Mensajes cargados:', this.messages);
      }
    },

    // Cargar categorías usando POST con FormData
    async loadCategories() {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      this.isLoading = true;
      this.error = null;

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/categories/`;
        const formData = this.createFormData({
          action: 'list'
        });
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        
        if (data.success) {
          this.categories = Array.isArray(data.data) ? data.data : [];
          return true;
        } else {
          this.error = data.error || 'Error al cargar categorías';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al cargar categorías';
        console.error('Error loading categories:', error);
        return false;
      } finally {
        this.isLoading = false;
      }
    },

    // Crear mensaje usando POST con FormData
    async createMessage(messageData: CreateMessageData) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      this.isCreatingMessage = true;
      this.error = null;

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/saved-messages/`;
        const formData = this.createFormData({
          action: 'create',
          titulo: messageData.titulo,
          texto_html_raw: messageData.texto_html_raw,
          category_id: messageData.category_id
        });
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        
        if (data.success) {
          // Añadir el nuevo mensaje a la lista local
          if (data.data) {
            this.messages.unshift(data.data);
            this.totalMessages += 1;
          }
          return data.data;
        } else {
          this.error = data.error || 'Error al crear mensaje';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al crear mensaje';
        console.error('Error creating message:', error);
        return false;
      } finally {
        this.isCreatingMessage = false;
      }
    },

    // Crear categoría usando POST con FormData
    async createCategory(categoryData: CreateCategoryData) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      this.isCreatingCategory = true;
      this.error = null;

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/categories/`;
        const formData = this.createFormData({
          action: 'create',
          name: categoryData.name
        });
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        
        if (data.success) {
          // Añadir la nueva categoría a la lista local
          if (data.data) {
            this.categories.push(data.data);
          }
          return data.data;
        } else {
          this.error = data.error || 'Error al crear categoría';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al crear categoría';
        console.error('Error creating category:', error);
        return false;
      } finally {
        this.isCreatingCategory = false;
      }
    },

    // Eliminar mensaje usando POST con FormData
    async deleteMessage(messageId: number) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/saved-messages/`;
        const formData = this.createFormData({
          action: 'delete',
          id: messageId
        });
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        
        if (data.success) {
          // Eliminar de la lista local
          this.messages = this.messages.filter(msg => msg.id !== messageId);
          this.totalMessages -= 1;
          return true;
        } else {
          this.error = data.error || 'Error al eliminar mensaje';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al eliminar mensaje';
        console.error('Error deleting message:', error);
        return false;
      }
    },

    // Eliminar categoría usando POST con FormData
    async deleteCategory(categoryId: number) {
      const companyStore = useCompanyStore();
      if (!companyStore.companyData) {
        this.error = 'No hay empresa seleccionada';
        return false;
      }

      try {
        const url = `${companyStore.companyData.url}/api/chatbot/categories/`;
        const formData = this.createFormData({
          action: 'delete',
          id: categoryId
        });
        
        if (!formData) {
          this.error = 'Error creando FormData';
          return false;
        }

        const response = await axios.post(url, formData);
        const data = response.data;
        
        if (data.success) {
          // Eliminar de la lista local
          this.categories = this.categories.filter(cat => cat.id !== categoryId);
          return true;
        } else {
          this.error = data.error || 'Error al eliminar categoría';
          return false;
        }
      } catch (error: any) {
        this.error = 'Error de conexión al eliminar categoría';
        console.error('Error deleting category:', error);
        return false;
      }
    },

    // Limpiar errores
    clearError() {
      this.error = null;
    },

    // Reiniciar estado
    reset() {
      this.messages = [];
      this.categories = [];
      this.isLoading = false;
      this.isLoadingCategories = false;
      this.isCreatingMessage = false;
      this.isCreatingCategory = false;
      this.error = null;
      this.currentPage = 1;
      this.totalPages = 1;
      this.totalMessages = 0;
    }
  }
});
