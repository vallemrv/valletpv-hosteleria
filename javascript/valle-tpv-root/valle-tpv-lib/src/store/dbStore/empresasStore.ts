import { defineStore } from 'pinia';
import Empresa from '../../models/empresa';
import db from '../../db/indexedDB';

export const useEmpresasStore = defineStore('empresas', {
  state: () => ({
    items: [] as Empresa[],
    isLoadDB: false,
  }),
  getters: {
    empresaActiva: (state): Empresa | null => {
      if (state.items.length === 0) {
        return null;
      }
      return state.items.find(empresa => empresa.activa === 1) || null;
    }
  },
  actions: {
    async setEmpresaActiva(id: number) {
      // Desactivar todas las empresas
      this.items.forEach(empresa => {
        empresa.activa = 0;
        // Actualizar en DB
        this.update(empresa);
      });
      
      // Activar la empresa seleccionada
      const empresa = this.items.find(e => e.id === id);
      if (empresa) {
        empresa.activa = 1;
        await this.update(empresa);
      }
    },
    
    async desactivarTodasEmpresas() {
      // Desactivar todas las empresas
      const promises = this.items.map(async (empresa) => {
        if (empresa.activa === 1) {
          empresa.activa = 0;
          await this.update(empresa);
        }
      });
      
      // Esperar a que todas las actualizaciones se completen
      await Promise.all(promises);
    },
    async insert(empresa: Empresa) {
      // Si la nueva empresa viene marcada como activa
      if (empresa.activa === 1) {
        // Primero desactivar todas las demás empresas
        await this.desactivarTodasEmpresas();
      }
      
      // Insertar la nueva empresa
      await db.add('empresas', empresa);
      this.items.push(empresa);
      
      // Si la nueva empresa es activa, forzar la reactividad
      // Esto asegura que el watch del WebSocketManager detecte el cambio
      if (empresa.activa === 1) {
        // Pequeño delay para asegurar que el DOM y los watchers se actualicen
        await new Promise(resolve => setTimeout(resolve, 100));
      }
    },
    async rm(id: number) {
      // Verificar si la empresa a borrar está activa
      const empresaABorrar = this.items.find(e => e.id === id);
      const eraActiva = empresaABorrar?.activa === 1;
      
      // Remover de DB y store
      await db.remove('empresas', id);
      this.items = this.items.filter((item: Empresa) => item.id !== id);
      
      // Si la empresa borrada era la activa, el getter empresaActiva retornará null
      // y el watch detectará el cambio automáticamente
    },
    async initStore() {
      if (!this.isLoadDB) {
        const empresas = await db.getAll('empresas');
        this.items = empresas;
        this.isLoadDB = true;
      }
    },
    async update(updated: Empresa) {
      await db.update('empresas', updated.id, updated);
      const idx = this.items.findIndex((item: Empresa) => item.id === updated.id);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
  }
});

export default useEmpresasStore;