import { defineStore } from 'pinia';

export const useCompanyStore = defineStore('company', {
  state: () => ({
    companyDataList: JSON.parse(localStorage.getItem('companyDataList') || '[]') as {
      name: string;
      user: string;
      token: string;
      url: string;
      id: number;
    }[], // Lista de empresas
    companyData: JSON.parse(localStorage.getItem('companyData') || 'null') as {
      name: string;
      user: string;
      token: string;
      url: string;
      id: number;
    } | null, // Empresa activa
  }),
  actions: {
    addCompany(company: { name: string; user: string; token: string; url: string; id?: number }) {
      if (!company.id) {
        company.id = Date.now(); // Asignar un ID único si no tiene uno
      }
      
      console.log('addCompany called with:', company);
      console.log('Current company list:', this.companyDataList);
      
      // Buscar por ID para edición (el ID debe existir en la lista)
      const existingIndexById = this.companyDataList.findIndex((c) => c.id === company.id);
      
      if (existingIndexById !== -1) {
        // Es una edición: reemplazar la empresa existente por ID
        console.log(`Editando empresa existente en índice ${existingIndexById}`);
        this.companyDataList[existingIndexById] = company;
      } else {
        // Es una nueva empresa: verificar que no exista una con el mismo nombre
        const existingIndexByName = this.companyDataList.findIndex((c) => c.name === company.name);
        if (existingIndexByName !== -1) {
          // Ya existe una empresa con ese nombre, reemplazarla
          console.log(`Reemplazando empresa con mismo nombre en índice ${existingIndexByName}`);
          this.companyDataList[existingIndexByName] = company;
        } else {
          // Agregar nueva empresa
          console.log('Agregando nueva empresa');
          this.companyDataList.push(company);
        }
      }
      
      console.log('Updated company list:', this.companyDataList);
      localStorage.setItem('companyDataList', JSON.stringify(this.companyDataList));
    },
    setActiveCompany(company: { name: string; user: string; token: string; url: string; id: number } | null) {
      this.companyData = company;
      if (company) {
        localStorage.setItem('companyData', JSON.stringify(company));
      } else {
        localStorage.removeItem('companyData'); // Eliminar del localStorage si es null
      }
    },
    removeCompany(companyId: number) {
      this.companyDataList = this.companyDataList.filter((c) => c.id !== companyId);
      localStorage.setItem('companyDataList', JSON.stringify(this.companyDataList));

      if (this.companyData?.id === companyId) {
        if (this.companyDataList.length > 0) {
          // Establecer la primera empresa como activa
          this.companyData = this.companyDataList[0];
        } else {
          // No hay empresas, establecer en null
          this.companyData = null;
        }
        if (this.companyData) {
          localStorage.setItem('companyData', JSON.stringify(this.companyData));
        } else {
          localStorage.removeItem('companyData'); // Eliminar del localStorage si es null
        }
      }
    },
  },
});
