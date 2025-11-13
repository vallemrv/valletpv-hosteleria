import { defineStore } from 'pinia';
import { Mesa, Zona } from '../../models/mesaZona';
import db from '../../db/indexedDB';


export const useMesasStore = defineStore('mesas', {
  state: () => ({
    items: [] as Mesa[],
    isLoadDB: false,
    mesaSel: null as Mesa | null
  }),
  getters: {
    mesasPorZona: (state) => {
      return(zona: Zona | null)  => {
        if (zona === null) return [];
        return state.items.filter(mesa => mesa.IDZona === zona.id);
      }
    }
  },
  actions: {
   
    async abrirMesa(idMesa: number) {
      const mesa = this.items.find(mesa => mesa.ID === idMesa);
     
      if (mesa) {
        mesa.num = 0;
        mesa.abierta = 1;
        await db.update('mesas', mesa.ID, mesa);
      }
    },
    async cerrarMesa(idMesa: number) {
      const mesa = this.items.find(mesa => mesa.ID === idMesa);
     
      if (mesa) {
        mesa.num = 0;
        mesa.abierta = 0;
        await db.update('mesas', mesa.ID, mesa);
      }
    },
    async insert(mesa: Mesa) {
      await db.add('mesas', mesa);
      this.items.push(mesa);
    },
    
    async rm(id: number) {
      await db.remove('mesas', id);
      this.items = this.items.filter((item: Mesa) => item.ID !== id);
    },
    async initStore(){
      if (!this.isLoadDB) {
        const mesas = await db.getAll('mesas');
        this.items = mesas.sort((a, b) => b.Orden - a.Orden);
        this.isLoadDB = true;
      }
    },
    async update(updated: Mesa) {
      await db.update('mesas', updated.ID, updated);
      const idx = this.items.findIndex((item: Mesa) => item.ID === updated.ID);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
    setMesaSeleccionada(mesa: Mesa | null) {
      this.mesaSel = mesa;
    }
  }
});
