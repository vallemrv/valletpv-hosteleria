import { defineStore } from "pinia";


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
    }
});