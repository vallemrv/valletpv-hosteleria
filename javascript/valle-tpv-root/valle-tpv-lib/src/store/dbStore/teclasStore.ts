import { defineStore } from 'pinia';
import Tecla from '../../models/tecla';
import db from '../../db/indexedDB';

export const useTeclasStore = defineStore('teclas', {
  state: () => ({
    items: [] as Tecla[],
    isLoadDB: false,
    teclaPadreSel: null as Tecla | null,
    textoBusqueda: null as string | null,
    tipo: "seccion" as "seccion" | "hijos" | "find"
  }),
  getters: {
    // Getters reactivos que se actualizan automáticamente
    teclasPorSec: (state) => {
      return (idSeccion: number): Tecla[] => {
        return state.items.filter(tecla => tecla.IDSeccionCom === idSeccion);
      };
    },
    
    teclasPorTeclaPadre: (state) => {
      return state.items.filter(tecla => tecla.IDParentTecla === state.teclaPadreSel?.ID) || null;
    },
    teclasFiltradasPorTexto: (state) => {
      const filtroLower = state.textoBusqueda?.toLowerCase();
      if (!filtroLower) {
        return [];
      }
      
      return state.items.filter(tecla => {
        // Excluir teclas de tipo "CM"
        if (tecla.tipo === "CM") {
          return false;
        }
        
        // Buscar en múltiples campos
        const nombre = tecla.nombre?.toLowerCase() || '';
        const tag = tecla.tag?.toLowerCase() || '';
        const descripcionR = tecla.descripcion_r?.toLowerCase() || '';
        
        return nombre.includes(filtroLower) || 
               tag.includes(filtroLower) || 
               descripcionR.includes(filtroLower);
      });
    },
    
  },
  actions: {
    async insert(tecla: Tecla) {
      await db.add('teclas', tecla);
      this.items.push(tecla);
    },
    async rm(id: number) {
      await db.remove('teclas', id);
      this.items = this.items.filter((item: Tecla) => item.ID !== id);
    },
    async initStore() {
      if (!this.isLoadDB) {
        const teclas = await db.getAll('teclas');
        this.items = teclas.sort((a, b) => b.orden - a.orden);
        this.isLoadDB = true;
      }
    },
    async update(updated: Tecla) {
      await db.update('teclas', updated.ID, updated);
      const idx = this.items.findIndex((item: Tecla) => item.ID === updated.ID);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
    setTeclaPadreSeleccionada(tecla: Tecla | null) {
      this.teclaPadreSel = tecla;
      this.tipo = tecla ? "hijos" : "seccion";
    },
    
    
  }
});
