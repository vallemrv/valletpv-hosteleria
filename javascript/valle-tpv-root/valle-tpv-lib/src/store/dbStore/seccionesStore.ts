import { defineStore } from 'pinia';
import Seccion from '../../models/seccion';
import db from '../../db/indexedDB';

export const useSeccionesStore = defineStore('seccionescom', {
  state: () => ({
    items: [] as Seccion[],
    isLoadDB: false,
    seccSel: null as Seccion | null,
    seccionAsociada: null as Seccion | null,
  }),
  actions: {
    async insert(seccion: Seccion) {
      await db.add('seccionescom', seccion);
      this.items.push(seccion);
    },
    async rm(id: number) {
      await db.remove('seccionescom', id);
      this.items = this.items.filter((item: Seccion) => item.id !== id);
    },
    async initStore(){
      if (!this.isLoadDB) {
        const secciones = await db.getAll('seccionescom');
        this.items = secciones;
        this.isLoadDB = true;
      }
       
      // Cargar sección asociada del localStorage
      this.cargarSeccionAsociadaDelStorage();
      if (this.seccionAsociada) {
        this.seccSel = this.seccionAsociada;
      }
      if (!this.seccSel) {
        this.seccSel = this.items[0] || null;
      }

    },
    async update(updated: Seccion) {
      await db.update('seccionescom', updated.id, updated);
      const idx = this.items.findIndex((item: Seccion) => item.id === updated.id);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
    
    // Nuevas acciones para sección seleccionada y asociada
    setSeccion(seccion: Seccion | null) {
      this.seccSel = seccion;
    },
    
    asociarSeccion(id: number) {
      const seccion = this.items.find(item => item.id === id);
      if (seccion) {
        this.seccionAsociada = seccion;
        this.guardarSeccionAsociadaEnStorage();
      }
    },
    
    quitarAsociacion() {
      this.seccionAsociada = null;
      this.guardarSeccionAsociadaEnStorage();
    },
    
    guardarSeccionAsociadaEnStorage() {
      if (this.seccionAsociada) {
        localStorage.setItem('seccionAsociada', JSON.stringify(this.seccionAsociada));
      } else {
        localStorage.removeItem('seccionAsociada');
      }
    },
    
    cargarSeccionAsociadaDelStorage() {
      try {
        const seccionAsociadaStr = localStorage.getItem('seccionAsociada');
        if (seccionAsociadaStr) {
          const seccionAsociadaData = JSON.parse(seccionAsociadaStr);
          // Verificar que la sección todavía existe en los items actuales
          const seccionExistente = this.items.find(item => item.id === seccionAsociadaData.id);
          if (seccionExistente) {
            this.seccionAsociada = seccionExistente;
          } else {
            // Si la sección ya no existe, limpiar el localStorage
            localStorage.removeItem('seccionAsociada');
          }
        }
      } catch (error) {
        console.error('Error al cargar sección asociada del localStorage:', error);
        localStorage.removeItem('seccionAsociada');
      }
    },
  }
});
