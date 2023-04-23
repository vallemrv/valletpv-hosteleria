import { defineStore } from 'pinia';

export const useEmpresaStore = defineStore('empresaStore', {
  state: () => ({
    empresa: null,
    empresas: [],
  }),
  actions: {
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
    addEmpresa(nuevaEmpresa) {
      this.empresa = nuevaEmpresa;
      this.empresas.push(nuevaEmpresa);
      localStorage.setItem('valleges_empresas', JSON.stringify(this.empresas));
      localStorage.setItem('valleges_empresa', JSON.stringify(this.empresa));
    },
    rmEmpresa() {
        const index = this.empresas.findIndex((empresa) => empresa === this.empresa);

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
