<template>
  <v-app>
    <valle-login v-if="token == null"></valle-login>
    <div  v-else>
      <valle-header></valle-header>
      <v-main app>
        <v-container><router-view></router-view></v-container>
      </v-main>
      <valle-footer></valle-footer>
    </div>
  </v-app>
</template>

<script>
import { mapState, mapActions } from "vuex";
import ValleFooter from "./components/ValleFooter";
import ValleHeader from "./components/ValleHeader";
import ValleLogin from "./components/ValleLogin";

export default {
  components: { ValleFooter, ValleHeader, ValleLogin },
  name: "App",

  data(){
     return {
        miToken:null
     }
  },
  computed: {
    ...mapState(["ocupado", "token"]),
  },
  methods: {
    ...mapActions(["getListado"]),
  },
  created() {
      if(localStorage.token){
        this.$store.state.token = localStorage.token;
      }
  }
};
</script>


<style scoped>
  .v-container{
    padding-bottom: 80px;
  }
</style>