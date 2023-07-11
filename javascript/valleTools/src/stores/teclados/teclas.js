import { defineStore } from "pinia";
import axios from "axios";
import { ADD_REG, UPDATE_REG, DELETE_REG, LISTADO_SIMPLE } from '@/endpoints';
import { buildUrl } from '@/api';

// Definimos el store Teclas
export const TeclasStore = defineStore({
  // Id del store
  id: 'teclas',
  // Estado del store
  state: () => ({
    seccion_id: null,
    parent_id: null,
    modelo: "teclas",
    // Array de teclas
    items: [],
    // Titulo del store
    titulo: 'Teclas',
    // Cabecera de la tabla
    headers: ["Nombre",],
    showKeys: ["nombre",],
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'descripcion_receptor', label: 'Descripción Receptor', type: 'text', },
      { key: 'descripcion_ticket', label: 'Descripción Ticket', type: 'text', },
      { key: 'p1', label: 'P1', type: 'number' },
      { key: 'p2', label: 'P2', type: 'number' },
      { key: 'p3', label: 'P3', type: 'number' },
      { key: 'orden', label: 'Orden', type: 'number', },
      { key: 'familia', label: 'Familia', options: [], type: "select", rules: [v => !!v || "La familia es requerida"] },
      { key: 'tag', label: 'Tag', type: 'text', },
    ],
    // Nuevo elemento
    newItem: {
      p1: "",
      p2: "",
      p3: "",
      nombre: "",
      orden: 0,
      tag: "",
      descripcion_receptor: "",
      descripcion_ticket: "",
      child: 0,
    },
  }),
  // Acciones del store
  actions: {
    async loadFamilias(empresaStore)  {
      this.empresaStore = empresaStore;
      let url = buildUrl(empresaStore.empresa.url, LISTADO_SIMPLE);
      let params = empresaStore.createFormData({ tb_name: "familias" });
      let response = await axios.post(url, params);
      let data = response.data;
      this.fields[7].options = data.regs.map((item) => {
        return { text: item.nombre, value: item.id };
      });
    },
    async setParent(parent_id) {
      this.parent_id = parent_id;
      await this.load({ parent_id: parent_id });
    },
    async setSeccion(seccion_id) {
      this.seccion_id = seccion_id;
      await this.load({ seccion_id: seccion_id });
    },
    async load(filter) {
      let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
      let params = this.empresaStore.createFormData({
        tb_name: this.modelo,
        filter: filter
      });
      let response = await axios.post(url, params);
      let data = response.data;

      if (data.success) {
        this.items = data.regs;
      } else {
        console.error("Error al cargar las teclas:", data.error);
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
      if (response.data.error) {
        return "Error al añadir la tecla: " + response.data.error;
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

      if (response.data.error) {
        return "Error al actualizar la tecla: " + response.data.error;
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
        return "Error al eliminar la tecla: " + response.data.error;
      }

      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }
  }
});
