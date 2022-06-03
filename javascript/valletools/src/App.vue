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
    ...mapState(["empresa"])
  },
  methods:{
    ...mapActions(["cargarEmpresas", "getListados", "getListado"])
  },
  watch:{
    empresa(v){
      if (v){
        this.getListados(["lineaspedido", "mesasabiertas"])
        this.getListado("")
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
