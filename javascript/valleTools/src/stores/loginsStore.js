import { defineStore } from 'pinia';
import { getNewToken } from "@/api";

export const useEmpresaStore = defineStore('empresaStore', {
  state: () => ({
    empresa: null,
    empresas: [],
    error: null
  }),
  actions: {
    getParams() {
      const params = new FormData();
      params.append("user", this.empresa.user);
      params.append("token", this.empresa.token);
      return params;
    },
    cargarEmpresas() {
      const empresasJSON = localStorage.getItem('valleges_empresas');
      if (empresasJSON) {
        this.empresas = JSON.parse(empresasJSON);
      }

      const empresaActivaJSON = localStorage.getItem('valleges_empresa');
      if (empresaActivaJSON) {
        this.empresa = JSON.parse(empresaActivaJSON);
      }
    },
    async addEmpresa(nuevaEmpresa, username, password) {
      const token = await getNewToken(username, password, nuevaEmpresa.url);

      if (token) {
        nuevaEmpresa.token = token.token; 
        nuevaEmpresa.user = token.user;
        this.empresa = nuevaEmpresa;
        this.empresas.push(nuevaEmpresa);
        localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
        localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
        this.error = null;
      }else{
        this.error = "Datos incorrectos."
      }
    },
    async upEmpresa(username, password) {
      const token = await getNewToken(username, password, this.empresa.url);

      if (token) {
        this.empresa.token = token.token;
        this.empresa.user = token.user; 
        localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
        localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
        this.error = null;
      }else{
        this.error = "Datos incorrectos."
      }
    },
    rmEmpresa() {
        const index = this.empresas.findIndex((empresa) => empresa.id === this.empresa.id);

        if (index !== -1) {
          this.empresas.splice(index, 1);
          localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
  
          if (this.empresas.length > 0) {
            this.empresa = this.empresas[0];
            localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
          } else {
            this.empresa = null;
            localStorage.removeItem('valleges_empresa');
          }
        }
    },
  },
});
