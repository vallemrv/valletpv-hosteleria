<template>
      <MainToolBar titulo="Dash Board" >
        <v-btn icon @click="addEmpresa">
          <v-icon>mdi-plus</v-icon>
        </v-btn>
        <v-btn icon @click="modificarEmpresa">
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
        <v-btn icon @click="showConfirmarRmEmpresa">
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </MainToolBar>
      <!-- Aquí va el contenido principal de tu aplicación -->
      <v-main>
        <v-container fluid>
        <v-row>
          <v-col  cols="12" sm="6" md="4" height="100">
            <VentasCard class="fill-height"   />
          </v-col>
            <v-col  cols="12" sm="6" md="4" >
              <VentasByCamCard class="fill-height" />
            </v-col>
            <v-col cols="12" sm="12" md="4" >
              <VentasByIntervalos class="fill-height" />
            </v-col>
            <v-col cols="12" >
              <VentasByArt class="fill-height"  />
            </v-col>
          </v-row>
        </v-container>
        
        
        <EditEmpresaDialog ref="editEmpresaDialog" :tipo="tipo"  :title="titleDialog"/>
        <DialogConfirm ref="rmEmpDialog" 
                       message="Esta seguro que quieres borrar esta empresa?" 
                       @result="borrarEmpresa" />
      </v-main>
       
   
  </template>
  
<script>
  
import { EmpresaStore } from "@/stores/empresaStore";
import { DashBoard } from "@/stores/dashBoard";
import DialogConfirm from "@/components/dialogs/DialogConfirm.vue";
import EditEmpresaDialog from "@/components/dialogs/EditEmpresaDialog.vue";
import VentasCard from "@/components/dashBoard/VentasCard.vue";
import VentasByCamCard from "@/components/dashBoard/VentasByCamCard.vue";
import VentasByIntervalos from "@/components/dashBoard/VentasByIntervalos.vue";
import VentasByArt from "@/components/dashBoard/VentasByArt.vue";
import MainToolBar from "@/components/tools/MainToolBar.vue";


export default {
    components: {
    DialogConfirm,
    EditEmpresaDialog,
    VentasCard,
    VentasByCamCard,
    VentasByIntervalos,
    VentasByArt,
    MainToolBar,
},
    data() {
      return {
        tipo: "editar",
        titleDialog: "Editar empresa",
      };
    },
    setup() {
        const empresaStore = EmpresaStore();
        const dashBoard = DashBoard();
        dashBoard.setStoreEmpresa(empresaStore);

        return {
        empresaStore,
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
  

 