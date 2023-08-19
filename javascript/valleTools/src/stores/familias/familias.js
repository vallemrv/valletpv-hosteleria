// Librerías
import { defineStore } from 'pinia';
import { buildUrl } from '@/api';
import { LISTADO_SIMPLE, UPDATE_REG, ADD_REG, DELETE_REG } from '@/endpoints';
import axios from 'axios';


// Definimos el store Familias
export const FamiliasStore = defineStore({
    // Id del store
    id: 'familias',
    // Estado del store
    state: () => ({
        empresaStore: null,
        modelo: "familias",
        // Array de familias
        items: [],
        // Titulo del store
        titulo: 'Familias',
        receptores: [],
        familias: [],
        // Cabecera de la tabla
        headers: ["Nombre"],
        showKeys: ["nombre"],
        displayName: "nombre",
        // Array de objetos con los datos de la tabla
        fields: [
            { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"] },
            { key: 'compuesto_por', label: 'Compuesto por', multiple: true, type: 'select', options: [] },
            { key: 'cantidad', label: 'Cantidad', type: 'number' },
            { key: 'color', label: 'Color', type: 'color' },
            { key: 'receptor_id', label: 'Receptor', type: 'select', options: [], rules: [v => !!v || "El  receptor es requerido"] },
        ],
        // Nuevo elemento
        newItem: {
            nombre: "",
            compuesto_por: [],
            cantidad: 0,
            receptor_id: null,
            // dame un color por defecto, rosita
            color: "#ffcccc",
        },
    }),
    // Acciones del store
    actions: {
        async loadFamilias() {
            this.familias = this.items.map(f => ({ value: f.id, text: f.nombre }));
            this.fields[1].options = this.familias;
        },
        loadReceptores(receptores) {
            this.receptores = receptores.map(r => ({ value: r.id, text: r.nombre }));
            this.fields[4].options = this.receptores;
        },
        async load(empresaStore, receptores) {
            if (receptores) 
                this.loadReceptores(receptores);
            this.empresaStore = empresaStore;
            this.items = [];
            let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);

            // Luego obtenemos las familias
            let params = this.empresaStore.createFormData({ tb_name: this.modelo });
            let response = await axios.post(url, params);
            let data = response.data;

            if (data.success) {
                this.items = data.regs.map(it => ({
                    ...it,
                    receptor_id: this.receptores.find(r => r.value === it.receptor),
                    compuesto_por: it.compuesto_por.
                        map(f => this.familias.find(i => i.text === f)).filter((item) => item !== undefined)
                }));
                this.loadFamilias();
               
            } else {
                console.error("Error al cargar las familias:", data.error);
            }
        },

        async add(item) {
            item.receptor_id = item.receptor_id.value;
            item.compuesto_por = item.compuesto_por.map(f => f.text).join(",");
            const obj = {
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
            const response = await axios.post(url, params);
            if (response.data.error || response.success === false) {
                error = response.data.error ? response.data.error : response.data.errors;
                return "Error al añadir la familia: " + error;
            }
            const it = response.data;
            this.items.push({
                ...it,
                receptor_id: this.receptores.find(r => r.value === it.receptor),
                compuesto_por: it.compuesto_por.
                    map(f => this.familias.find(i => i.text === f)).filter((item) => item !== undefined)
            });
            this.loadFamilias();
        },
        async update(item) {
        
            item.receptor_id = item.receptor_id.value;
            item.compuesto_por = item.compuesto_por.map(f => f.text).join(",");
            
            const obj = {
                filter: { id: item.id },
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            const response = await axios.post(url, params);

            if (response.data.error || response.success === false) {
                error = response.data.error ? response.data.error : response.data.errors;
                return "Error al actualizar la familia: " + error;
            }
            const index = this.items.findIndex((i) => i.id === item.id);
            const it = response.data;
            this.items[index] = {
                ...it,
                receptor_id: this.receptores.find(r => r.value === it.receptor),
                compuesto_por: it.compuesto_por.
                    map(f => this.familias.find(i => i.text === f)).filter((item) => item !== undefined)
            };
            this.loadFamilias();
            return null;
        },
        async delete(item) {
            const obj = {
                filter: { id: item.id },
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, DELETE_REG);
            const response = await axios.post(url, params);
            if (response.data.error || response.success === false) {
                error = response.data.error ? response.data.error : response.data.errors;
                return "Error al eliminar la familia: " + error;
            }

            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});
