import { defineStore } from 'pinia';
import Sugerencia from '../../models/sugerencia';
import db from '../../db/indexedDB';
import { useTeclasStore } from './teclasStore';

export const useSugerenciasStore = defineStore('sugerencias', {
  state: () => ({
    items: [] as Sugerencia[],
    isLoadDB: false,
  }),
  getters: {
    // Getter reactivo para obtener sugerencias por tecla
    sugPorTecla: (state) => {
      return (idTecla: string): Sugerencia[] => {
        return state.items.filter(sugerencia => sugerencia.tecla === idTecla);
      };
    },
    // Getter reactivo para obtener sugerencias por tecla con incremento > 0
    // Incluye también las sugerencias de la tecla padre si existe
    sugPorTeclaConIncremento: (state) => {
      return (idTecla: string): Sugerencia[] => {
        const teclasStore = useTeclasStore();
        
        const idTeclaNormalizado = String(idTecla);
        
        const sugerenciasTecla = state.items.filter(
          sugerencia => String(sugerencia.tecla) === idTeclaNormalizado && sugerencia.incremento > 0
        );
        
        const teclaActual = teclasStore.items.find(t => String(t.ID) === idTeclaNormalizado);
        
        if (teclaActual?.IDParentTecla != null) {
          const idPadreNormalizado = String(teclaActual.IDParentTecla);
          
          const sugerenciasPadre = state.items.filter(
            sugerencia => String(sugerencia.tecla) === idPadreNormalizado && sugerencia.incremento > 0
          );
          
          const sugerenciasUnicas = [...sugerenciasTecla];
          sugerenciasPadre.forEach(sugPadre => {
            if (!sugerenciasUnicas.find(s => s.id === sugPadre.id)) {
              sugerenciasUnicas.push(sugPadre);
            }
          });
          
          return sugerenciasUnicas;
        }
        
        return sugerenciasTecla;
      };
    },
  },
  actions: {
    async insert(sugerencia: Sugerencia) {
      await db.add('sugerencias', sugerencia);
      this.items.push(sugerencia);
    },
    async rm(id: string) {
      await db.remove('sugerencias', id);
      this.items = this.items.filter((item: Sugerencia) => item.id !== id);
    },
    async initStore() {
      if (!this.isLoadDB) {
        const sugerencias = await db.getAll('sugerencias');
        this.items = sugerencias;
        this.isLoadDB = true;
      }
    },
    async update(updated: Sugerencia) {
      await db.update('sugerencias', updated.id, updated);
      const idx = this.items.findIndex((item: Sugerencia) => item.id === updated.id);
      if (idx !== -1) {
        this.items[idx] = updated;
      }
    },
    
    // Nueva acción para agregar sugerencia
    async agregarSug(sugerencia: Sugerencia) {
      await this.insert(sugerencia);
    },
  }
});
