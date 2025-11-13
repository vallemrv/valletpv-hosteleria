import { defineStore } from 'pinia';
import { Zona } from '../../models/mesaZona';
import db from '../../db/indexedDB';

export const useZonasStore = defineStore('zonas', {
  state: () => ({
    items: [] as Zona[],
    isLoadDB: false,
    zonaSel: null as number | null,
    zonaSeleccionada: null as Zona | null,
    zonaAsociada: null as Zona | null,
  }),
  getters: {
    zonasDisponibles: (state) => {
      return state.items;
    },
  },
  actions: {
    setZona(zonaId: number | null) {
      this.zonaSel = zonaId;
      if (zonaId === null) {
        this.zonaSeleccionada = null;
      } else {
        this.zonaSeleccionada = this.items.find(zona => zona.id === zonaId) || null;
      }
    },
    async insert(zona: Zona) {
      await db.add('zonas', zona);
      this.items.push(zona);
    },
    async rm(id: number) {
      await db.remove('zonas', id);
      this.items = this.items.filter((item: Zona) => item.id !== id);
    },
    
    async initStore()  {
      if (!this.isLoadDB) {
        const zonas = await db.getAll('zonas');
        this.items = zonas;
        this.isLoadDB = true;
      }
      this.cargarZonaAsociadaDelStorage();
      if (this.zonaAsociada) {
        this.zonaSeleccionada = this.zonaAsociada;
        this.zonaSel = this.zonaAsociada.id;
      }
      if (!this.zonaSeleccionada) {
        this.zonaSeleccionada = this.items[0] || null;
      }

    },
      
    async update(updated: Zona) {
      await db.update('zonas', updated.id, updated);
      const idx = this.items.findIndex((item: Zona) => item.id === updated.id);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
    
    // Nuevas acciones para zona asociada
    asociar(id: number) {
      const zona = this.items.find(item => item.id === id);
      if (zona) {
        this.zonaAsociada = zona;
        this.guardarZonaAsociadaEnStorage();
      }
    },
    
    desasociar() {
      this.zonaAsociada = null;
      this.guardarZonaAsociadaEnStorage();
    },
    
    guardarZonaAsociadaEnStorage() {
      if (this.zonaAsociada) {
        localStorage.setItem('zonaAsociada', JSON.stringify(this.zonaAsociada));
      } else {
        localStorage.removeItem('zonaAsociada');
      }
    },
    
    cargarZonaAsociadaDelStorage() {
      try {
        const zonaAsociadaStr = localStorage.getItem('zonaAsociada');
        if (zonaAsociadaStr) {
          const zonaAsociadaData = JSON.parse(zonaAsociadaStr);
          // Verificar que la zona todavía existe en los items actuales
          const zonaExistente = this.items.find(item => item.id === zonaAsociadaData.id);
          if (zonaExistente) {
            this.zonaAsociada = zonaExistente;
          } else {
            // Si la zona ya no existe, limpiar el localStorage
            localStorage.removeItem('zonaAsociada');
          }
        }
      } catch (error) {
        console.error('Error al cargar zona asociada del localStorage:', error);
        localStorage.removeItem('zonaAsociada');
      }
    },
  }
});
