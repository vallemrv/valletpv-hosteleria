import { defineStore } from 'pinia';
import { getNewToken } from "@/api";
import { buildUrl } from "@/api";
import {
  UPDATE_REG, 
  LISTADO_SIMPLE,
  USER_PROFILE
} from "@/endpoints";
import axios from 'axios';

export const EmpresaStore = defineStore('empresaStore', {
  state: () => ({
    empresa: null,
    modelo: "empresa",
    fields: [
      { key: 'nombre', label: 'Nombre', type: 'text', rules: [v => !!v || "El nombre es requerido"], },
      { key: 'razonsocial', label: 'Razón Social', type: 'text', },
      { key: 'cif', label: 'CIF', type: 'text', },
      { key: 'direccion', label: 'Dirección', type: 'text', },
      { key: 'cp', label: 'Código Postal', type: 'text', },
      { key: 'localidad', label: 'Localidad', type: 'text', },
      { key: 'provincia', label: 'Provincia', type: 'text', },
      { key: 'telefono', label: 'Teléfono', type: 'text', },
      { key: 'email', label: 'Email', type: 'text', },
      { key: 'iva', label: 'IVA', type: 'number', },
      { key: 'logo', label: 'Logo', type: 'file', },
      { key: 'logo_small', label: 'Logo pequeño', type: 'file', },
    ],
    profile: {
      nombre: "",
      razonsocial: "",
      cif: "",
      direccion: "",
      cp: "",
      localidad: "",
      provincia: "",
      telefono: "",
      email: "",
      iva: 10,
    },
    userProfile: null,
    empresas: [],
    error: null
  }),
  actions: {
    upToken(token) {
      this.empresa.token = token.token;
      this.empresa.user = token.user;
      const index = this.empresas.findIndex((emp) => emp.id === this.empresa.id);
      this.empresas[index] = this.empresa;
      localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
      localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
    },
    getUserProfile(){
      return {
        ...this.userProfile,
        hora_ini: this.userProfile.horario.hora_ini ,
        hora_fin: this.userProfile.horario.hora_fin ,
        password: "",
      }
    },
    setUserProfile(item, user, token){ 
      this.userProfile = item;
      this.userProfile.horario = {
        hora_ini: item.horario.hora_ini,
        hora_fin: item.horario.hora_fin,
      }
      this.upToken({user, token});
    },
    getUserName(){
      let user_name = "";
       if (this.userProfile)
        user_name =  !this.userProfile.first_name ? this.userProfile.username :
                 this.userProfile.first_name + " " + this.userProfile.last_name;
        
       return user_name;
    },
    async loadUserProfile() {
      if (!this.empresa) return;
      let url = buildUrl(this.empresa.url, USER_PROFILE);
      let params = this.createFormData();
      let response = await axios.post(url, params);
      let data = response.data;
      if (data.success) {
        this.userProfile= {...data};
      }
      
    },

    async load() {
      if (!this.empresa) return;
      this.loadUserProfile();
      let url = buildUrl(this.empresa.url, LISTADO_SIMPLE);
      let params = this.createFormData({ tb_name: this.modelo });
      let response = await axios.post(url, params);
      let data = response.data;
      if (data.success) {
        this.profile = data.regs.length > 0 ? {
          ...data.regs[0],
          logo: data.regs[0].logo.url != "" ?
            [{
              name: data.regs[0].logo.name,
              url: buildUrl(this.empresa.url, data.regs[0].logo.url)
            }] : [],
          logo_small: data.regs[0].logo_small.url != "" ? [{
            name: data.regs[0].logo_small.name,
            url: buildUrl(this.empresa.url, data.regs[0].logo_small.url)
          }] : [],

        }
          : this.profile;
      }
    },
    async update(item) {
      if (!this.empresa) return;

      const obj = { tb_name: this.modelo, filter: {} };
      if (item.logo && item.logo.length > 0) {
        obj.logo = item.logo[0];
        delete item.logo;
      }
      if (item.logo_small && item.logo_small.length > 0) {
        obj.logo_small = item.logo_small[0];
        delete item.logo_small;
      }
      obj.reg = item;

      let url = buildUrl(this.empresa.url, UPDATE_REG);
      let params = this.createFormData(obj);
      let response = await axios.post(url, params);
      let data = response.data;
      if (data.success) {
        this.profile = data.length > 0 ? {
          ...data,
          logo: data.logo.url != "" ? [{
            name: data.logo.name,
            url: buildUrl(this.empresa.url, data.logo.url)
          }] : [],
          logo_small: data.logo_small.url != "" ? [{
            name: data.logo_small.name,
            url: buildUrl(this.empresa.url, data.logo_small.url)
          }] : [],
        }
          : this.profile;
      }
    },
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
      this.load();
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
      this.load();
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
        this.load();
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
