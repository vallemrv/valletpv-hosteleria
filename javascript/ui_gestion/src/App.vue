<template>
  <v-app>
      <v-main app>
          <router-view></router-view>
          
      </v-main>
      <valle-footer></valle-footer>
      <v-snackbar v-model="snackbar" multi-line> 
      {{error}}
      <template v-slot:actions>
        <v-btn
          color="blue"
          variant="text"
          @click="snackbar = false"
        >
          Close
        </v-btn>
      </template>
      
      </v-snackbar>

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
    return {
      snackbar: false,
   };
  },
  computed: {
    ...mapState(["ocupado", "empresa", "error"]),
  },
  methods:{
    ...mapActions(["cargarEmpresas"]),
  },
  watch:{
    error(v){
      this.snackbar = v != null;
    }
  },
  mounted(){
    let empresas = JSON.parse(localStorage.empresas);
    console.log(empresas)
    if (empresas && (typeof empresas) == 'object'){
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
