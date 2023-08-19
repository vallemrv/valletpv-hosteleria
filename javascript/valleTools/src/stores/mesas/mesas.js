import { defineStore } from 'pinia';
import { buildUrl } from '@/api';
import { LISTADO_SIMPLE, ADD_REG, DELETE_REG, UPDATE_REG } from '@/endpoints';
import axios from 'axios';


// Definimos el store Mesas
export const MesasStore = defineStore({
  // Id del store
  id: 'mesas',
  // Estado del store
  state: () => ({
    empresaStore: null,
    zona_id: null,
    modelo: "mesas",
    // Array de mesas
    items: [],
    // Titulo del store
    titulo: 'Mesas',
    // Cabecera de la tabla
    headers: ["Nombre", "Orden"],
    showKeys: ["nombre", "orden"],
    displayName: "nombre",
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'orden', label: 'Orden', type: 'number' },
    ],
    // Nuevo elemento
    newItem: {
      nombre: "",
      orden: 0,
    },
  }),

  // Acciones del store
  actions: {
    async setZona(zona_id) {
      this.zona_id = zona_id;
      if (this.zona_id)
         await this.load();
         else
          this.items = [];
    },

    async load() {
      if (!this.zona_id || !this.empresaStore) return;
      let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
      let params = this.empresaStore.createFormData({
        tb_name: this.modelo,
        filter: { zona_id: this.zona_id }
      });
      let response = await axios.post(url, params);
      let data = response.data;

      if (data.success) {
        this.items = data.regs;
      }
    },

    async add(item) {
      item.zona_id = this.zona_id;
      const obj = {
        reg: item,
        tb_name: this.modelo,
      }
      const params = this.empresaStore.createFormData(obj);
      const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
      const response = await axios.post(url, params);
      if (response.data.error || response.success === false) {
        error = response.data.error ? response.data.error : response.data.errors;
        return "Error al añadir la mesa: " + response.data.error;
      }
      const newItems = [...this.items];
      newItems.push(response.data);

      this.items = newItems.sort((a, b) => b.orden - a.orden);
    },

    async update(item) {
      item.zona_id = this.zona_id;
      const obj = {
        filter: { id: item.id },
        reg: item,
        tb_name: this.modelo,
      }
      const params = this.empresaStore.createFormData(obj);
      const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
      const response = await axios.post(url, params);

      if (response.data.error || response.success === false) {
        error = response.data.error ? response.data.error : response.data.errors;
        return "Error al actualizar la mesa: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      
      const newItems = [...this.items];
      newItems[index] = {...response.data, orden: Number(response.data.orden)};
      this.items = newItems.sort((a, b) => b.orden - a.orden);
      return null;
    },

    async delete(item) {
      const obj = {
        filter: { id: item.id },
        tb_name: this.modelo,
      }
      const params = this.empresaStore.createFormData(obj);
      const url = buildUrl(this.empresaStore.empresa.url, DELETE_REG);
      const response = await axios.post(url, params);
      if (response.data.error || response.success === false) {
        error = response.data.error ? response.data.error : response.data.errors;
        return "Error al eliminar la mesa: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }
  }
});
