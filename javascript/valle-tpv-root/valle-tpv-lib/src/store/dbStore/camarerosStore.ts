import { defineStore } from 'pinia';
import Camarero from '../../models/camarero';
import db from '../../db/indexedDB';
import { useInstruccionesStore } from '../instruccionesStore';


export const useCamarerosStore = defineStore('camareros', {
	state: () => ({
		items: [] as Camarero[],
		isLoadDB: false,
		camarerosSel: null as Camarero | null
	}),
	getters: {
		camarerosAuth: (state) => {
			return state.items.filter(camarero => 
				!!camarero.activo && !!camarero.autorizado
			);
		},
		camarerosNoAuth: (state) => {
			return state.items.filter(camarero => 
				!!camarero.activo && !camarero.autorizado
			);
		},
		camarerosPorPermiso: (state) => {
			return (permiso: string) => {
				return state.items.filter(camarero => 
					camarero.activo && 
					camarero.permisos.includes(permiso)
				);
			};
		}
	},
	actions: {
		setCamarerosSel(camarero: Camarero) {
			this.camarerosSel = camarero;
		},
		async altaCamarero(nombre: string, apellidos: string) {
			const nuevoCamarero = new Camarero({ nombre, apellidos });
			await this.insert(nuevoCamarero);

			// Obtenemos el store de instrucciones
			const instruccionesStore = useInstruccionesStore();

			instruccionesStore.encolarInstruccion(
				'api/camareros/camarero_add',
				{id_local: nuevoCamarero.id, nombre, apellidos },
			);
			
		},
		async setAuth(id: number, autorizado: 0 | 1) {
			const camarero = this.items.find(c => c.id === id);
			if (!camarero) return;

			// Actualización optimista
			camarero.autorizado = autorizado;

			// Obtenemos el store de instrucciones
			const instruccionesStore = useInstruccionesStore();

			await instruccionesStore.encolarInstruccion(
				'api/camareros/authorize_waiter',
				{ id, autorizado }
			);

		},
		async insert(camarero: Camarero) {
			await db.add('camareros', camarero);
			this.items.push(camarero);
		},
		async rm(id: number) {
			await db.remove('camareros', id);
			this.items = this.items.filter((item: Camarero) => item.id !== id);
		},
		async initStore() {
			if (!this.isLoadDB) {
				const camareros = await db.getAll('camareros');
				this.items = camareros;
				this.isLoadDB = true;
			}
		},
		async update(updated: Camarero) {
			await db.update('camareros', updated.id, updated);
			const idx = this.items.findIndex((item: Camarero) => item.id === updated.id);
			if (idx !== -1) {
				this.items[idx] = updated;
			}
		},
	}
});
