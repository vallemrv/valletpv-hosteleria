import { defineStore } from "pinia";
import { buildUrl } from "@/api";
import { LISTADO_SIMPLE, ADD_REG, DELETE_REG, UPDATE_REG } from "@/endpoints";
import axios from "axios";

// Definimos el store Zonas
export const ZonasStore = defineStore({
  // Id del store
  id: 'zonas',
  // Estado del store
  state: () => ({
    empresaStore: null,
    modelo: "zonas",
    // Array de zonas
    items: [],
    // Titulo del store
    titulo: 'Zonas',
    // Cabecera de la tabla
    headers: ["Nombre", "Color", "Orden"],
    showKeys: ["nombre", "color", "orden"],
    displayName: "nombre",
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'color', label: 'Color', type: 'color', rules: [v => !!v || "El color es requerido"] },
      { key: 'orden', label: 'Orden', type: 'number' },
      { key: 'tarifa', label: 'Tarifa', type: 'number', rules: [v => [1, 2].includes(Number(v)) || 'La tarifa solo puede ser 1, 2'] },
    ],
    // Nuevo elemento
    newItem: {
      nombre: "",
      color: "",
      orden: 0,
      tarifa: 1,
    },
  }),

  actions: {
    async load(empresaStore) {
      this.empresaStore = empresaStore;
      let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
      let params = this.empresaStore.createFormData({ tb_name: this.modelo });
      let response = await axios.post(url, params);
      let data = response.data;

      if (data.success) {
        this.items = data.regs;
      } else {
        console.error("Error al cargar las zonas:", data.error);
      }
    },
    async add(item) {
      const obj = {
        reg: item,
        tb_name: this.modelo,
      }
      const params = this.empresaStore.createFormData(obj);
      const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
      const response = await axios.post(url, params);
      if (response.data.error || response.success === false) {
        error = response.data.error ? response.data.error : response.data.errors;
        return "Error al añadir la zona: " + response.data.error;
      }
      const newItems = [...this.items];
      newItems.push(response.data);
      this.items = newItems.sort((a, b) => b.orden - a.orden);
    },
    async update(item) {
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
        return "Error al actualizar la zona: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      const newItems = [...this.items];
      newItems[index] = response.data;
      this.items = newItems.sort((a, b) => b.orden - a.orden);
     
    },
    async delete(item) {
      const obj = {
        filter: { id: item.id },
        tb_name: this.modelo,
      }
      const params = this.empresaStore.createFormData(obj);
      const url = buildUrl(this.empresaStore.empresa.url, DELETE_REG);
      const response = await axios.post(url, params);
      if (response.data.error) {
        return "Error al eliminar la zona: " + response.data.error;
      }

      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }

  }
});