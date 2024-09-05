<template>
  <v-app>
    <v-main>
      <router-view></router-view>
      <valle-footer></valle-footer>
      <audio src="" id="eventAudio"></audio>
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
    ...mapState(["empresa", "isWSConnected", "new_men"])
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
    },
    new_men(v){
      if (v){
        this.$notification.playAudio();
        this.$store.state.new_men = null;
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
