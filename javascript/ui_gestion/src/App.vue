<template>
  <v-app>
    <valle-login v-if="token == null"></valle-login>
    <div v-else>
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
    ...mapState(["ocupado", "token"]),
  },
  methods: {
    ...mapActions(["getListado"]),
  },
  created() {
    if (localStorage.token) {
      this.$store.state.token = JSON.parse(localStorage.token);
    }
  },
};
</script>

<style scoped>
.v-container {
  min-height: 82vh;
}
</style>
