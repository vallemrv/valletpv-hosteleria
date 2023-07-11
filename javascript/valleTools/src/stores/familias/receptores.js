import { defineStore } from "pinia";
import { buildUrl } from "@/api";
import {
    LISTADO_SIMPLE,
    UPDATE_REG, ADD_REG, DELETE_REG
} from "@/endpoints";

import axios from "axios";

//definimos el store receptores
export const ReceptoresStore = defineStore({
    //id del store
    id: 'receptores',
    //estado del store
    state: () => ({
        empresaStore: null,
        modelo: "receptores",
        //array de receptores
        items: [],
        //titulo del store
        titulo: 'Receptores',
        //Cabecera de la tabla
        headers: ["Nombre"],
        switchKey: "activo",
        showKeys: ["nombre"],
        displayName: "nombre",
        //array de objetos con los datos de la tabla
        fields: [
            { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'descripcion', label: 'Descripción', type: 'text', rules: [v => !!v || "La descripción es requerida"], },
        ],
        newItem: {
            nombre: "",
            descripcion: "",
            activo: false,
        },
    }),
    //acciones del store
    actions: {
        async load(empresaStore) {
            this.empresaStore = empresaStore;
            let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
            let params = this.empresaStore.createFormData({ tb_name: this.modelo });
            let response = await axios.post(url, params);
            let data = response.data;

            if (data.success) {
                this.items = data.regs;
            }
        },
        async switchCh(item) {
            const obj = {
                filter: { id: item.id },
                reg: { activo: item.activo },
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            await axios.post(url, params);
        },
        async add(item) {
            const obj = {
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
            const response = await axios.post(url, params);
            if (response.data.error) {
                return "Error al añadir el receptor: " + response.data.error;
            }
            this.items.push(response.data);
        },
        async update(item) {
            const obj = {
                filter: { id: item.id },
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            const response = await axios.post(url, params);

            if (response.data.error) {
                return "Error al actualizar el receptor: " + error;
            }
            const index = this.items.findIndex((i) => i.id === item.id);
            this.items[index] = response.data;
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
            if (response.data.error) {
                return "Error al eliminar el receptor: " + error;
            }

            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});
