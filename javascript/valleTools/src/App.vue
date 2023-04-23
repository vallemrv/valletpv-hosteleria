<template>
    <div id="app">
      <FormEmpresas v-if="!empresa" title="Crear una empresa" />
      <MainView v-else />
    </div>
  </template>
  
  <script>
  import FormEmpresas from "@/components/appMain/FormEmpresa.vue";
  import MainView from "@/views/appMain/MainView.vue";
  import { useEmpresaStore } from "@/stores/empresaStore";
  
  export default {
    components: {
      FormEmpresas,
      MainView,
    },
    setup() {
      const empresaStore = useEmpresaStore();
  
      return {
        empresa: empresaStore.empresa,
      };
    },
  };
  </script>
  