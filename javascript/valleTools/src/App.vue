<template>
  <div id="app">
    <FormEmpresa v-if="!empresaStore.empresa" title="Crear una empresa"  tipo="nuevo"/>
    <MainView v-else />
  </div>
</template>
  
  <script>
  import FormEmpresa from "@/components/appMain/FormEmpresa.vue";
  import MainView from "@/views/appMain/MainView.vue";
  import { useEmpresaStore } from "@/stores/empresaStore";
  
  export default {
    components: {
      FormEmpresa,
      MainView,
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
  