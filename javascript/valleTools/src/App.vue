<template>
  <v-app>
      <v-container  class="pt-16" v-if="!empresaStore.empresa" fluid fill-height>
         <FormEmpresa  title="Alta de una nueva empresa"  tipo="nuevo"/>
      </v-container>

      <v-main v-else>
        <router-view />
        <MenuPrincipal  v-if="empresaStore.empresa"/>
      </v-main>
      
  </v-app>
  
    
 
</template>
  
  <script>
  import FormEmpresa from "@/components/appMain/FormEmpresa.vue";
  import MenuPrincipal from "./components/tools/MenuPrincipal.vue";
  import { EmpresaStore } from "@/stores/empresaStore";
  
  export default {
    components: {
      FormEmpresa,
      MenuPrincipal
    },
    setup() {
      const empresaStore = EmpresaStore();
      return {
        empresaStore
      };
    },
    mounted(){
      this.empresaStore.cargarEmpresas();
    }
  };
  </script>
  

  <style scoped>
    .div-fixed {
      position: fixed;
      bottom: 10px;
      right: 20px;
      width: 50px;
      height: 50px;
      text-align: center;
    }
  </style>