
import { defineStore } from 'pinia';

export type ClicablePlaning = {
  num_cell: number;
  nombre_item: string;
  mesa_id?: string;
};

export type NoClicablePlaning = {
  num_cell: number;
  nom_item: string;
  rotacion?: number;
};

export const useStorePlaning = defineStore('storePlaning', {
  state: () => ({
    clicables: [] as ClicablePlaning[],
    noClicables: [] as NoClicablePlaning[],
  }),
  actions: {
    cargar_planing(zona_id: string) {
      // Implementa la carga desde API, localStorage, etc.
      // this.clicables = ...
      // this.noClicables = ...
    },
    guardar_planing(zona_id: string) {
      // Implementa la persistencia
    },
    agregar_objeto_clicable(obj: ClicablePlaning) {
      this.clicables = this.clicables.filter(o => o.num_cell !== obj.num_cell);
      this.clicables.push(obj);
    },
    agregar_objeto_no_clicable(obj: NoClicablePlaning) {
      this.noClicables.push(obj);
    },
    borrar_objeto_clicable(num_cell: number) {
      this.clicables = this.clicables.filter(o => o.num_cell !== num_cell);
    },
    borrar_objeto_no_clicable(num_cell: number, nom_item?: string) {
      this.noClicables = this.noClicables.filter(o => o.num_cell !== num_cell || (nom_item && o.nom_item !== nom_item));
    }
  }
});
