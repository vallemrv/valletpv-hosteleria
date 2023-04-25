<template>
    <v-app>
      <v-navigation-drawer expand-on-hover
           rail v-model="drawer" >
        <v-list>
          <v-list-item
            prepend-icon="mdi-handshake"
            :title="empresaStore.empresa.nombre"
            :subtitle="empresaStore.empresa.url"
          ></v-list-item>
        </v-list>

        <v-divider></v-divider>

        <v-list density="compact" nav>
          <v-list-item prepend-icon="mdi-folder" title="My Files" value="myfiles"></v-list-item>
          <v-list-item prepend-icon="mdi-account-multiple" title="Shared with me" value="shared"></v-list-item>
          <v-list-item prepend-icon="mdi-star" title="Starred" value="starred"></v-list-item>
        </v-list>
      </v-navigation-drawer>
      <v-app-bar app>
        <v-btn v-if="!drawer" icon @click="drawer = true">
          <v-icon>mdi-menu</v-icon>
        </v-btn>
        <v-toolbar-title>{{ empresaStore.empresa.nombre }}</v-toolbar-title>
  
        <v-spacer></v-spacer>
  
        <v-btn icon @click="showConfirmarRmEmpresa">
          <v-icon>mdi-delete</v-icon>
        </v-btn>
  
        <v-btn icon @click="modificarEmpresa">
          <v-icon>mdi-pencil</v-icon>
        </v-btn>

        <v-btn icon @click="addEmpresa">
          <v-icon>mdi-plus</v-icon>
        </v-btn>
  
        <v-menu offset-y>
          <template v-slot:activator="{ props }">
            <v-btn icon v-bind="props">
              <v-icon>mdi-dots-vertical</v-icon>
            </v-btn>
          </template>
  
          <v-list>
            <v-list-item
              v-for="empresaItem in empresaStore.empresas"
              :key="empresaItem.id"
              @click="seleccionarEmpresa(empresaItem)"
            >
              <v-list-item-title>{{ empresaItem.nombre }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-app-bar>
  
      <!-- Aquí va el contenido principal de tu aplicación -->
      <v-main>
        <v-container fluid>
        <v-row>
          <v-col cols="12" sm="6" md="4">
            <VentasCard :empresa="empresaStore.empresa" />
          </v-col>
            <v-col cols="12" sm="6" md="4" >
              <VentasByCamCard :empresa="empresaStore.empresa" class="fill-height"/>
            </v-col>
          </v-row>
        </v-container>
        
        
        <EditEmpresaDialog ref="editEmpresaDialog" :tipo="tipo"  :title="titleDialog"/>
        <confirmation-dialog ref="rmEmpDialog" 
                       message="Esta seguro que quieres borrar esta empresa?" 
                       @result="borrarEmpresa"></confirmation-dialog>
      </v-main>
       
    </v-app>
  </template>
  
<script>
  
import { useEmpresaStore } from "@/stores/empresaStore";
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue';
import EditEmpresaDialog from "@/components/dialogs/EditEmpresaDialog.vue";
import VentasCard from "@/components/appMain/dashBoard/VentasCard.vue";
import VentasByCamCard from "../../components/appMain/dashBoard/VentasByCamCard.vue";


export default {
    components: {
    ConfirmationDialog,
    EditEmpresaDialog,
    VentasCard,
    VentasByCamCard
},
    data() {
      return {
        tipo: "editar",
        titleDialog: "Editar empresa",
        drawer: true,
      };
    },
    setup() {
        const empresaStore = useEmpresaStore();

        return {
        empresaStore,
        seleccionarEmpresa: (empresaItem) => {
            empresaStore.empresa = empresaItem;
            localStorage.setItem("valleges_empresa", JSON.stringify(empresaItem));
        },
      };
    },
    methods: {
      modificarEmpresa(){
        this.tipo = "editar";
        this.titleDialog = "Editar empresa";
        this.$refs.editEmpresaDialog.openDialog();
      },
      addEmpresa(){
        this.tipo = "nuevo";
        this.titleDialog = "Agregar empresa";
        this.$refs.editEmpresaDialog.openDialog();
      },
      borrarEmpresa(result){
        if (result){
          this.empresaStore.rmEmpresa();
        }
      },
      showConfirmarRmEmpresa(){
          this.$refs.rmEmpDialog.openDialog();
      },
    },
    };
    
  </script>
  