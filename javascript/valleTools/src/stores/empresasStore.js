import { defineStore } from "pinia";
import { onSnapshot, collection } from "firebase/firestore";
import { db, fb_pathDoc, fb_update } from "@/api";

export const useEmpresasStore = defineStore({
    id: 'empresas',
    state: () => ({
        userId : null,
        empresaSel: null,
        empresas: [],
        suscripcion: null,
    }),
    actions: {
        selEmpresa(empresa) {
            fb_update(fb_pathDoc("usuarios", this.userId, "empresas"), empresa.id,  {selected: true},);
            if (this.empresaSel){
                fb_update(fb_pathDoc("usuarios", this.userId, "empresas"), 
                   this.empresaSel.id, {selected: false});
                this.empresaSel.selected = false;
                this.empresaSel = empresa;
            }
        },
        getPathDoc(collectionName) {
           return ["usuarios", this.userId, "empresas", this.empresaSel.id, collectionName].join("/");
        },
        getDisplayName() {
            if(this.empresaSel) {
                return this.empresaSel.alias || this.empresaSel.empresa;
            }
            return "";
        },
        async suscribirAEmpresas() {
            if(!this.userId) return;
           
            const querySnapshot = await collection(db, fb_pathDoc("usuarios", this.userId, "empresas"));

            this.suscripcion =  onSnapshot(querySnapshot, (querySnapshot) => {
                this.empresas = [];
                querySnapshot.forEach((doc) => {
                    let empresa = {id:doc.id, ...doc.data()};
                    if (empresa.selected) this.empresaSel = empresa;
                    this.empresas.push(empresa);
                });
                if (!this.empresaSel && this.empresas.length > 0) {
                    this.selEmpresa(this.empresas[0]);
                }
            });
        },
        unsuscribirAEmpresas() {
            if(this.suscripcion) {
                this.suscripcion();
                this.suscripcion = null;
            }
        },
    }
});