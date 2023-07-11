import { defineStore } from 'pinia';
import { getNewToken } from "@/api";

export const EmpresaStore = defineStore('empresaStore', {
  state: () => ({
    empresa: null,
    empresas: [],
    error: null
  }),
  actions: {
    createFormData(obj) {
      if (!this.empresa) return;
      let formData = new FormData();
      formData.append('user', this.empresa.user);
      formData.append('token', this.empresa.token);
      for (let key in obj) {
        if (obj.hasOwnProperty(key)) {
           if (typeof obj[key] === 'object' && !(obj[key] instanceof File)) {
              obj[key] = JSON.stringify(obj[key]);
            } else if (obj[key] instanceof File) {
              console.log("Agregando archivo al FormData: ", obj[key]);
            }
           formData.append(key, obj[key]);
        }
      }
      return formData;
    },
    getDisplayName() {
      return this.empresa ? this.empresa.nombre : '';
    },
    selEmpresa(empresa) {
      this.empresa = empresa;
      localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
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
        nuevaEmpresa.user = token.user;
        nuevaEmpresa.token = token.token;
        nuevaEmpresa.id = this.empresas.length;
        this.empresa = nuevaEmpresa;
        this.empresas.push(nuevaEmpresa);
        localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
        localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
        this.error = null;
      } else {
        this.error = "Datos incorrectos."
      }
    },
    async upEmpresa(username, password) {
      let token = { user: this.empresa.user, token: this.empresa.token };
      if (username != "" && password != "") {
        token = await getNewToken(username, password, this.empresa.url);
        if (!token) {
          this.error = "Datos incorrectos."
          return;
        }
      }
      this.empresa.user = token.user;
      this.empresa.token = token.token;
      localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
      localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
      this.error = null;
    },
    rmEmpresa() {
      if (this.empresa) {
        this.empresas = this.empresas.filter((emp) => emp.id !== this.empresa.id);

        localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));

        if (this.empresas.length > 0) {
          this.empresa = this.empresas[0];
          localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
        } else {
          this.empresa = null;
          localStorage.removeItem('valleges_empresa');
          localStorage.removeItem('valleges_empresas');
        }
      }
    },
  },
});
