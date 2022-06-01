<template>
  <ion-page>
    <valle-header @clickTools="on_click_tools"></valle-header>
    <ion-content>
        <ion-item color="medium" v-if="!isWsConnected">
          <ion-label>No hay conexion, o no hay receptores seleccionados</ion-label>
        </ion-item>
         <ion-item color="medium" v-if="!isHttpConnected">
          <ion-label>No hay una url especificada...</ion-label>
        </ion-item>
    </ion-content>
    <valle-dialog-server :show="showDialog" @close="on_close_dialog" @didDismiss="on_close_dialog()"></valle-dialog-server>
    <valle-receptores :items="receptores_mod" :show="showReceptores" @close="on_close_receptores" @didDismiss="on_close_receptores()"></valle-receptores>
   </ion-page>
   
</template>

<script lang="ts">
import { Websocket } from "@/websocket";
import  ValleHeader   from '@/components/ValleHeader.vue';
import ValleReceptores from '@/components/ValleReceptores.vue';
import ValleDialogServer from '@/components/ValleDialogServer.vue';
import { mapState, mapActions } from 'vuex';
import {  IonPage, IonContent, IonItem, IonLabel } from '@ionic/vue';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'HomePage',
  components: {
    IonPage,
    IonContent,
    IonItem,
    IonLabel,
    ValleHeader,
    ValleDialogServer,
    ValleReceptores,
  },
  data(){
    return{
      showReceptores: false,
      showDialog : false,
      server: null,
      ws: []
    }
  },
  computed:{
    ...mapState(["isHttpConnected", "isWsConnected", "receptores", "receptores_sel"]),
    receptores_mod(){
      let val  = [];
      this.receptores.forEach((e) => {
         e.isChecked = this.receptores_sel.includes(e.ID);
         val.push(e);
      })
      return val
    }
  },
  methods:{
    ...mapActions(["getEmpresa", "getListado", 
    "getReceptoresSel"]),
    getReceptores() {
      if (this.receptores_sel.length > 0) {
        return Object.values(this.receptores).filter((r:any) => {
          return this.receptores_sel.includes(r.ID);
        });
      }
      return [];
    },
    on_click_tools(op){
      if (op == "server"){
         this.showDialog = true;
      }else if ( op == "settings"){
        this.showReceptores = true;
      }
    },
    on_close_dialog(){
      this.showDialog = false;
      if (localStorage.server){
        this.server = localStorage.server;
        localStorage.receptores = null;
        this.$store.state.receptores_sel = []
        this.getEmpresa();
        this.getListado();
      }
    },
    on_close_receptores(){
     this.showReceptores = false;
    },
    connect_ws(){
      this.ws.forEach((w) => {
        w.disconnect();
      });
      const rec = this.getReceptores()
     
      rec.forEach((r) => {
        console.log(r)
        var ws_aux = new Websocket(this.server, r.nomimp, r.Nombre, this.$store.commit);
        this.ws.push(ws_aux);
        ws_aux.connect();
      });
    }
  },
  watch:{
    receptores_sel(v){
      if (v){
        localStorage.receptores = JSON.stringify(v);
        this.connect_ws();
      }
    },
    receptores(v){
      if (v){
        this.connect_ws();
      }
    }
  },
  mounted(){
    if (localStorage.server){
       this.server = localStorage.server;
       this.getEmpresa();
       this.getListado();
       this.getReceptoresSel();
    }

  }
});
</script>

<style scoped>
  ion-item{
    margin: 10px;
  }
</style>