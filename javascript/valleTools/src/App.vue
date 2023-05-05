<template>
  <v-app>
      <v-container  class="pt-16" v-if="!empresaStore.empresa" fluid fill-height>
         <FormEmpresa  title="Crear una empresa"  tipo="nuevo"/>
      </v-container>
      <v-container v-else >
        <v-navigation-drawer expand-on-hover
          rail v-model="drawer" app >
            <v-list>
              <v-list-item
                  prepend-icon="mdi-handshake"
                  :title="empresaStore.empresa.nombre"
                  :subtitle="empresaStore.empresa.url"
                ></v-list-item>
            </v-list>

            <v-divider></v-divider>

            <v-list density="compact" nav>
              <v-list-item prepend-icon="mdi-account-multiple" title="Camareros" value="camareros" to="/gestion/camareros"></v-list-item>
            </v-list>
        </v-navigation-drawer>

        <!-- Floating Action Button -->
        <router-view />

      </v-container>
      <div class="div-fixed" v-if="!drawer">
          <v-btn
            relative
            elevation="8"
            icon
            large
            color="primary"
            @click="drawer = true"
          >
            <v-icon>mdi-menu</v-icon>
          </v-btn>
      </div>

  </v-app>
  
    
 
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
    data(){
      return{
        drawer: true,
      }
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