import { defineStore } from "pinia";
import { fb_getAll, fb_create, fb_delete, fb_get, 
         fb_update, fb_getByFilter } from "@/api";

// Definimos el store Teclas
export const TeclasStore = defineStore({
  // Id del store
  id: 'teclas',
  // Estado del store
  state: () => ({
    pathDoc: null,
    seccion_id: null,
    parent: null,
    collectionName: "teclas",
    // Array de teclas
    items: [],
    // Titulo del store
    titulo: 'Teclas',
    // Cabecera de la tabla
    headers: [ "Nombre", ],
    showKeys: [ "nombre", ],
    // Array de objetos con los datos de la tabla
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
      { key: 'descripcion_receptor', label: 'Descripción Receptor', type: 'text',  },
      { key: 'descripcion_ticket', label: 'Descripción Ticket', type: 'text',  },
      { key: 'p1', label: 'P1', type: 'number' },
      { key: 'p2', label: 'P2', type: 'number' },
      { key: 'p3', label: 'P3', type: 'number' },
      { key: 'orden', label: 'Orden', type: 'number', },
      { key: 'familia', label: 'Familia', options:[], type:"select", rules: [v => !!v || "La familia es requerida"] },
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
    async setSeccion(seccion_id){
      if (seccion_id == null){
        this.parent_id = null;
        this.seccion_id = null; 
        this.items =[]; 
        return;
      }
      this.seccion_id = seccion_id;
      this.parent = null;
      this.items = await this.getByFilter("seccion", "==", seccion_id);
    },
    async setParent(parent){
      if (parent == null){
        this.parent= null; 
        this.seccion_id = null; 
        this.items =[]; 
        return;
      }
      this.seccion_id = null;
      this.parent = parent;
      this.items = await this.getByFilter("parent", "==", parent.id);
    },
    async loadFamilias(pathDoc){
      this.pathDoc = pathDoc;
      const { docs, error } = await fb_getAll(this.pathDoc.replace("teclas", "familias"));
      if (error) {
        console.error("Error al cargar las familias:", error);
        return;
      }
      let familiaOptions = docs.map(f => ({ value: f.id, text: f.nombre, color: f.color }));
      this.fields[7].options = familiaOptions;
    },
    async load(pathDoc) {
      this.pathDoc = pathDoc;
      this.items = [];

      const { docs, error } = await fb_getAll(this.pathDoc);
      
      if (error) {
        console.error("Error al cargar las teclas:", error);
        return;
      }
      
      this.items = docs.sort((a, b) => a.orden - b.orden);
    },
    async add(item) {
      if (this.seccion_id != null){
        item.seccion = this.seccion_id;
      } 
      if (this.parent != null){
        item.parent = this.parent.id;
        this.parent.child += 1;
        await this.update(this.parent);
      }
  
      const { doc, error } = await fb_create(this.pathDoc, item);
      if (error) {
        return "Error al crear la tecla: " + error;
      }
  
      this.items = [...this.items, doc].sort((a, b) => a.orden - b.orden);
      return null;
    },
    async update(item) {
      
      const error = await fb_update(this.pathDoc, item.id, item);
      if (error) {
        return "Error al actualizar la tecla: " + error;
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items[index] = item;
      this.items = this.items.sort((a, b) => a.orden - b.orden);
      return null;

    },
    async delete(item) {

      if (item.child > 0){
        return "No se puede eliminar la tecla porque tiene subteclas";
      }
      const error = await fb_delete(this.pathDoc, item.id);
      if (error) {
        return "Error al eliminar la tecla: " + error;
      }
      if (this.parent != null){
        this.parent.child -= 1;
        await this.update(this.parent); 
      }
      const index = this.items.findIndex((i) => i.id === item.id);
      this.items.splice(index, 1);
    },
    async getByFilter(fieldName, operator, value) {
      const filter = { field:fieldName, operator: operator, value:value };
      const { docs, error } = await fb_getByFilter(this.pathDoc, [ filter ] );
      if (error) {
        console.error("Error al buscar las teclas:", error);
        return;
      }
      //devolvemos el array de documentos ordenados por orden
      return docs.sort((a, b) => a.orden - b.orden);
    },

    async getItemByID(id) {
      const { doc, error } = await fb_get(this.pathDoc, id);
      if (error) {
        console.error("Error al buscar la tecla:", error);
        return;
      }
      return doc;
    }
  }
});
