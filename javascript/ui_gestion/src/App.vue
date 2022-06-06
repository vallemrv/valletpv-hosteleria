<template>
  <v-app>
      <v-main app>
          <router-view></router-view>
      </v-main>
      <valle-footer></valle-footer>
  </v-app>
</template>

<script>
import { mapState, mapActions } from "vuex";
import ValleFooter from "./components/ValleFooter";
import ValleMainHeader from "./components/ValleMainHeader.vue";

export default {
  components: { ValleFooter, ValleMainHeader },
  name: "App",
  data() {
    const { offsetHeight } = document.documentElement;
    return {
      miToken: null,
      isOverflowing: 0,
      myHeight: "0",
      offsetHeight,
    };
  },
  computed: {
    ...mapState(["ocupado", "empresa"]),
  },
  methods:{
    ...mapActions(["cargarEmpresas"]),
  },
  mounted(){
    if (localStorage.empresas){
      this.cargarEmpresas();
    }
  }
};
</script>

<style scoped>
.v-container {
  min-height: 82vh;
}
</style>
