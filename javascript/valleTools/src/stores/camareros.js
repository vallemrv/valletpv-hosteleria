import { defineStore } from "pinia";
import { buildUrl } from "@/api";
import {
    LISTADO_PERMISOS, LISTADO_SIMPLE,
    UPDATE_REG, ADD_REG, DELETE_REG
} from "@/endpoints";

import axios from "axios";

//definimos el store camareros
export const CamarerosStore = defineStore({
    //id del store
    id: 'camareros',
    //estado del store
    state: () => ({
        empresaStore: null,
        modelo: "camareros",
        //array de camareros
        items: [],
        permisos: [],
        //titulo del store
        titulo: 'Camareros',
        //Cabecera de la tabla
        headers: ["Nombre"],
        switchKey: "activo",
        showKeys: ["displayName"],
        displayName: "dipslayName",
        //array de objetos con los datos de la tabla
        fields: [
            { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
            { key: 'apellidos', label: 'Apellidos', type: 'text', rules: [v => !!v || "Los apellidos son requeridos"], },
            { key: 'permisos', label: 'Permisos', multiple: true, type: 'select', options: [] },
        ],
        extraAcitons: [
            { icon: "mdi-key", action: "clearPassword" },
        ],
        newItem: {
            nombre: "",
            apellidos: "",
            activo: false,
            permisos: [],
        },
    }),
    //acciones del store
    actions: {
        async execAction(item, action) {
            if (action === "clearPassword" && this.empresaStore) {
                const obj = {
                    filter: { id: item.id },
                    reg: { password: "" },
                    tb_name: this.modelo,
                }
                const params = this.empresaStore.createFormData(obj);
                const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
                await axios.post(url, params);
            }
        },
        async load(empresaStore) {
            this.empresaStore = empresaStore;
            this.loadPermisos();
            let url = buildUrl(this.empresaStore.empresa.url, LISTADO_SIMPLE);
            let params = this.empresaStore.createFormData({ tb_name: this.modelo });
            let response = await axios.post(url, params);
            let data = response.data;

            if (data.success) {
                this.items = data.regs.map((item) => {
                    item.displayName = item.nombre + " " + item.apellidos;
                    item.permisos = item.permisos.map((item) => {
                        return this.permisos.find((i) => i.value === item) ;
                    }).filter((item) => item !== undefined);
                    return item;
                });
            }
            this.fields[2].options = this.permisos;
        },
        async loadPermisos() {
            let url = buildUrl(this.empresaStore.empresa.url, LISTADO_PERMISOS);
            let params = this.empresaStore.createFormData();
            let response = await axios.post(url, params);
            this.permisos = response.data.map((item) => {
                return { text: item[1], value: item[0] };
            });
        },
        async switchCh(item) {
            const obj = {
                filter: { id: item.id },
                reg: { activo: item.activo },
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            const response  = await axios.post(url, params);
            
        },
        async add(item) {
            item.permisos = item.permisos.map((item) => item.value).join(",");
            const obj = {
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, ADD_REG);
            const response = await axios.post(url, params);
            if (response.data.error || response.success === false) {
                return "Error al añadir el camarero: " + response.data.error;
            }
            const newItem = { ...response.data, 
                              displayName: response.data.nombre + " " + response.data.apellidos,
                              permisos: response.data.permisos.map((item) => {
                                return this.permisos.find((i) => i.value === item) ;
                              }).filter((item) => item !== undefined)
                            };
            this.items.push(newItem);
        },
        async update(item) {
            item.permisos = item.permisos.map((item) => item.value).join(",");
            const obj = {
                filter: { id: item.id },
                reg: item,
                tb_name: this.modelo,
            }
            const params = this.empresaStore.createFormData(obj);
            const url = buildUrl(this.empresaStore.empresa.url, UPDATE_REG);
            const response = await axios.post(url, params);

            if (response.data.error) {
                return "Error al actualizar el camarero: " + error;
            }
            const index = this.items.findIndex((i) => i.id === item.id);
            const newItem = { ...response.data, 
                displayName: response.data.nombre + " " + response.data.apellidos,
                permisos: response.data.permisos.map((item) => {
                  return this.permisos.find((i) => i.value === item) ;
                }).filter((item) => item !== undefined)
              };
            this.items[index] = newItem;
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
                return "Error al eliminar el camarero: " + error;
            }

            const index = this.items.findIndex((i) => i.id === item.id);
            this.items.splice(index, 1);
        }
    }
});




