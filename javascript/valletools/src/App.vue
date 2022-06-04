<template>
  <v-app>
    <v-main>
      <router-view></router-view>
      <valle-footer></valle-footer>
    </v-main>
  </v-app>
</template>

<script>
import ValleFooter from '@/components/ValleFooter.vue'
import { mapActions, mapState } from 'vuex';
export default {
  name: 'App',
  components:{ValleFooter },
  data: () => ({
    //
  }),
  computed:{
    ...mapState(["empresa", "isWSConnected"])
  },
  methods:{
    ...mapActions(["cargarEmpresas", "getListado", "getDatasets", "getAlertas"])
  },
  watch:{
    isWSConnected(v){
      if (v){
         this.getDatasets();
         this.getAlertas();
         this.getListado("mesasabiertas");
      }
    }
  },
  mounted(){
    if (localStorage.empresas){
      this.cargarEmpresas();
    }
  }
}
</script>
