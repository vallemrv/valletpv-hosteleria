import { defineStore } from "pinia";

export const ConfigStore = defineStore({
    id: "config",
    state: () => ({
        componente: "ListaEmpresas",
        listaComponentes:[
            { icon: "mdi-store", titulo: "Dash Board", name: "dashBoard"},
            { icon: "mdi-account", titulo: "Camareros", name: "camareros"},
            { icon: "mdi-keyboard", titulo: "Teclados", name: "TecladosView"},
            { icon: "mdi-package-variant-closed", titulo: "Familias", name: "familias" },
            { 
                icon: "mdi-table-furniture", 
                titulo: "Mesas", 
                name: "MesasView" 
              }
              
        ]
    }),
    actions: {
        setComponente(c) {
            this.componente = c.componente;
        }   
    }   
});
