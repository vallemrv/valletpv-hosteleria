<template>
  <div id="app">
    <v-container  class="pt-16" v-if="!empresaStore.empresa" fluid fill-height>
      <FormEmpresa  title="Crear una empresa"  tipo="nuevo"/>
    </v-container>
    <router-view v-else />
  </div>
</template>
  
  <script>
  import FormEmpresa from "@/components/appMain/FormEmpresa.vue";
  import { useEmpresaStore } from "@/stores/empresaStore";
  
  export default {
    components: {
      FormEmpresa,
    },
    setup() {
      const empresaStore = useEmpresaStore();
  
      return {
        empresaStore
      };
    },
    mounted(){
      this.empresaStore.cargarEmpresas();
    }
  };
  </script>
  