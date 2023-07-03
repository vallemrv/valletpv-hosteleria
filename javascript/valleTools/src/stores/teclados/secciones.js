import { defineStore } from "pinia";


// Definimos el store Secciones
export const SeccionesStore = defineStore({
  // Id del store
  id: 'secciones',
  // Estado del store
  state: () => ({
    pathDoc: null,
    collectionName: "secciones",
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
      { key: 'orden', label: 'Orden', type: 'number',  },
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
    async load(pathDoc) {
      this.pathDoc = pathDoc;
      this.items = [];
      
      const { docs, error } = await fb_getAll(this.pathDoc);
      
      if (error) {
        console.error("Error al cargar las secciones:", error);
        return;
      }
      
      this.items = docs.sort((a, b) => a.orden - b.orden);
    },
    async add(item) {
      const { doc, error } = await fb_create(this.pathDoc, item);
      if (error) {
        return "Error al crear la sección: " + error;
      }
      
      this.items.push(doc);
      return null;
    },
    async update(item) {
      const error = await fb_update(this.pathDoc, item.id, item);
      if (error) {
        return "Error al actualizar la sección: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items[index] = item;
      this.items = this.items.sort((a, b) => a.orden - b.orden);
      return null;
    },
    async delete(item) {
      const error = await fb_delete(this.pathDoc, item.id);
      if (error) {
        return "Error al eliminar la sección: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    },
    async getByFilter(fieldName, operator, value) {
      const { docs, error } = await fb_getByFilter(this.pathDoc, fieldName, operator, value);
      if (error) {
        console.error("Error al cargar las secciones:", error);
        return;
      }
      return docs;
    }
  }
});
