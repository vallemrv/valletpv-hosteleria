import { defineStore } from "pinia";
import { fb_getAll, fb_create, fb_delete, fb_update } from "@/api";

// Definimos el store Receptores
export const ReceptoresStore = defineStore({
  // Id del store
  id: 'receptores',
  // Estado del store
  state: () => ({
    pathDoc: null,
    collectionName: "receptores",
    // Array de receptores
    items: [],
    // Titulo del store
    titulo: 'Receptores',
    // Cabecera de la tabla
    headers: ["Nombre"],
    // Array de objetos con los datos de la tabla
    showKeys: ["nombre"],
    switchKey: "activo",
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'descripcion', label: 'Descripción', type: 'text', rules: [v => !!v || "La descripción es requerida"] },
    ],
    // Nuevo receptor
    newItem: {
      nombre: "",
      descripcion: "",
      activo: true
    },
  }),
  // Acciones del store
  actions: {
    async switchCh(item) {
        await fb_update(this.pathDoc, item.id, { activo: item.activo });
    },
    async load(pathDoc) {
      this.pathDoc = pathDoc
      this.items = [];
      const { docs, error } = await fb_getAll(this.pathDoc);
      
      if (error) {
        console.error("Error al cargar los receptores:", error);
        return;
      }

      this.items = docs;
    },
    async add(item) {
      const { doc, error } = await fb_create(this.pathDoc, item);
      if (error) {
        return "Error al crear el receptor: " + error;
      }
      this.items.push(doc);
      return null;
    },
    async update(item) {
      const error = await fb_update(this.pathDoc, item.id, item);
      if (error) {
        return "Error al actualizar el receptor: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items[index] = item;
      return null;
    },
    async delete(item) {
      const error = await fb_delete(this.pathDoc, item.id);
      if (error) {
        return "Error al eliminar el receptor: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }
  }
});
