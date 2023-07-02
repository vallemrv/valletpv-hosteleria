import { defineStore } from "pinia";
import { fb_getAll, fb_create, fb_delete, fb_update } from "@/api";
import { fb_getByFilter } from "../../api";

// Definimos el store Familias
export const FamiliasStore = defineStore({
  // Id del store
  id: 'familias',
  // Estado del store
  state: () => ({
    pathDoc: null,
    collectionName: "familias",
    // Array de familias
    items: [],
    // Titulo del store
    titulo: 'Familias',
    // Cabecera de la tabla
    headers: ["Nombre"],
    showKeys: ["nombre"],
    receptores: [],
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'compuesta_por', label: 'Compuesta por', multiple:true, type: 'select', options:[]},
      { key: 'cantidad', label: 'Cantidad', type: 'number'},
      { key: 'color', label: 'Color', type: 'color'},
      { key: 'receptor_id', label: 'Receptor ID', type: 'select', options:[], rules: [v => !!v || "El ID del receptor es requerido"] },
    ],
    // Nuevo elemento
    newItem: {
      nombre: "",
      compuesta_por: [],
      cantidad: 0,
      receptor_id: null,
      // dame un color por defecto, rosita
      color: "#ffcccc",
      
    },
  }),
  // Acciones del store
  actions: {
    async load(pathDoc) {
      this.pathDoc = pathDoc;
      this.items = [];
      const { docs: receptores } = await fb_getAll(pathDoc.replace("familias", "receptores"));
      
      this.receptores = receptores.map(r => ({ value: r.id, text: r.nombre }));
      this.fields[4].options = this.receptores; 
      const { docs, error } = await fb_getAll(this.pathDoc);
      
      if (error) {
        console.error("Error al cargar las familias:", error);
        return;
      }
      this.fields[1].options = docs.map(f => ({ value: f.id, text: f.nombre }));

      this.items = docs;
    },
    async add(item) {
      
      const { doc, error } = await fb_create(this.pathDoc, item);
      if (error) {
        return "Error al crear la familia: " + error;
      }
      
      this.items.push(doc);
      return null;
    },
    async update(item) {
      const { docs } = await fb_getByFilter(this.pathDoc.replace("familias", "teclas"),
        [{  field: "familia.value", operator: "==", value: item.id}]); 
      docs.forEach(async (doc) => {
       doc.familia.color = item.color;
        await fb_update(this.pathDoc.replace("familias", "teclas"), doc.id, doc);
      });
      const error = await fb_update(this.pathDoc, item.id, item);
      if (error) {
        return "Error al actualizar la familia: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items[index] = item;
      return null;
    },
    async delete(item) {
      const error = await fb_delete(this.pathDoc, item.id);
      if (error) {
        return "Error al eliminar la familia: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    }
  }
});
