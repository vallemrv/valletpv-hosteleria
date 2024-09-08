import { defineStore } from "pinia";
import { buildUrl } from "@/api";
import {
    LISTADO_SIMPLE,
    UPDATE_REG,  DELETE_REG
} from "@/endpoints";

import axios from "axios";

// Definimos el store dispositivos
export const DispositivosStore = defineStore({
    id: 'dispositivos',
    state: () => ({
        empresaStore: null,
        modelo: "dispositivos",
        items: [],
        titulo: 'Dispositivos',
        headers: ["Nombre", "Código", "Descripción"],
        displayName: "nombre",
        showKeys: ["nombre", "codigo", "descripcion"],
        switchKey: "puede_enviar",
        fields: [
            { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'descripcion', label: 'Descripción', type: 'text', rules: [v => !!v || "La descripción es requerida"], },
         ],
        newItem: {
            nombre: "",
            codigo: "",
            descripcion: "",
            UID: "",
        },
    }),
    actions: {
        async switchCh(item) {
            const obj = {
                filter: { id: item.id },
                reg: {},
                tb_name: this.modelo,
            }
            obj.reg[this.switchKey] = item[this.switchKey];

            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            await axios.post(url, params);

        },
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
        async add(item) {
            return "No se puede añadir un dispositivo, esta funcion no está disponible"
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

            if (response.data.error || response.success === false) {
                error = response.data.error ? response.data.error : response.data.errors;
                return "Error al actualizar el dispositivo: " + error;
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
                return "Error al eliminar el dispositivo: " + error;
            }

            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});
