import { defineStore } from "pinia";
import axios from "axios";
import { ADD_REG, UPDATE_REG, DELETE_REG, LISTADO_SIMPLE } from '@/endpoints'; 
import { buildUrl } from '@/api';


// Definimos el store Secciones
export const SeccionesStore = defineStore({
    // Id del store
    id: 'secciones',
    // Estado del store
    state: () => ({
      empresaStore: null,
      modelo: "secciones",
      // Array de secciones
      items: [],
      // Titulo del store
      titulo: 'Secciones',
      // Cabecera de la tabla
      headers: ["Nombre", "Color", "Orden", "Icono"],
      showKeys: ["nombre", "color", "orden", "icono"],
      // Array de objetos con los datos de la tabla
      fields: [
        { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
        { key: 'color', label: 'Color', type: 'color', rules: [v => !!v || "El color es requerido"] },
        { key: 'orden', label: 'Orden', type: 'number' },
        { key: 'icono', label: 'Icono', type: 'file',  },
      ],
      // Nuevo elemento
      newItem: {
        nombre: "",
        color: "",
        orden: 0,
        icono: null,
      },
    }),
    // Acciones del store
    actions: {
        async load(empresaStore) {
            this.empresaStore = empresaStore;
            let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
            let params = this.empresaStore.createFormData({ tb_name: this.modelo });
            let response = await axios.post(url, params);
            let data = response.data;

            if (data.success) {
              this.items = data.regs.map((item) => {
                return {
                  ...item,
                  icono_url: buildUrl(this.empresaStore.empresa.url, item.icono_url),
                };
              });
            } else {
              console.error("Error al cargar las secciones:", data.error);
            }
        },
        async add(item) {
            const icono = item.icono[0];
            delete item.icono;
            const obj = {
              icono: icono,
              reg: item,
              tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
            const response = await axios.post(url, params,{
              headers: {
                'Content-Type': 'multipart/form-data'
              }}
            );
            if (response.data.error) {
              return "Error al añadir la sección: " + response.data.error;
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
              return "Error al actualizar la sección: " + response.data.error;
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
              return "Error al eliminar la sección: " + response.data.error;
            }

            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});
