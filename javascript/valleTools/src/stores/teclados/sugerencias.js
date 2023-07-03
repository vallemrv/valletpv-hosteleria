import { defineStore } from "pinia";


// Definimos el store Sugerencias
export const SugerenciasStore = defineStore({
  // Id del store
  id: 'sugerencias',
  // Estado del store
  state: () => ({
    pathDoc: null,
    collectionName: "sugerencias",
    // Array de sugerencias
    items: [],
    // Titulo del store
    titulo: 'Sugerencias',
    // Cabecera de la tabla
    headers: ["Sugerencia", "Tecla", "Orden"],
    showKeys: ["sugerencia", "tecla", "orden"],
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'sugerencia', label: 'Sugerencia', type: 'text', rules: [v => !!v || "La sugerencia es requerida"] },
      { key: 'tecla', label: 'Tecla', type: 'text', rules: [v => !!v || "La tecla es requerida"] },
      { key: 'orden', label: 'Orden', type: 'number', rules: [v => !!v || "El orden es requerido"] },
    ],
    // Nuevo elemento
    newItem: {
      sugerencia: "",
      tecla: "",
      orden: 0,
    },
  }),
  // Acciones del store
  actions: {
    async load(pathDoc) {
      this.pathDoc = pathDoc;
      this.items = [];
      
      const { docs, error } = await fb_getAll(this.pathDoc);
      
      if (error) {
        console.error("Error al cargar las sugerencias:", error);
        return;
      }
      
      this.items = docs;
    },
    async add(item) {
      const { doc, error } = await fb_create(this.pathDoc, item);
      if (error) {
        return "Error al crear la sugerencia: " + error;
      }
      
      this.items.push(doc);
      return null;
    },
    async update(item) {
      const error = await fb_update(this.pathDoc, item.id, item);
      if (error) {
        return "Error al actualizar la sugerencia: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items[index] = item;
      return null;
    },
    async delete(item) {
      const error = await fb_delete(this.pathDoc, item.id);
      if (error) {
        return "Error al eliminar la sugerencia: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }
  }
});
