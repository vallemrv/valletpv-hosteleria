import { defineStore } from "pinia";

export const ConfigStore = defineStore({
    id: "config",
    state: () => ({
        componente: "ListaEmpresas",
        listaComponentes:[
            { icon: "mdi-store", titulo: "Empresas", name: "view", params: { view: "empresas" }},
            { icon: "mdi-account", titulo: "Camareros", name: "view", params: { view: "camareros" }},
            { icon: "mdi-keyboard", titulo: "Teclados", name: "TecladosView"},
            { icon: "mdi-package-variant-closed", titulo: "Familias", name: "view", params: {view: 'familias'} }
        ]
    }),
    actions: {
        setComponente(c) {
            this.componente = c.componente;
        }   
    }   
});
