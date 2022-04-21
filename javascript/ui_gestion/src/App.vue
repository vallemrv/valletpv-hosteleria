<template>
  <v-app>
    <valle-login v-if="token == null"></valle-login>
    <v-container fluid v-else>
      <valle-header></valle-header>
      <v-main>
        <v-container><router-view></router-view></v-container>
      </v-main>
      <v-footer app>
        <valle-footer></valle-footer>
      </v-footer>
    </v-container>
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

  data: () => ({
    //
  }),
  computed: {
    ...mapState(["ocupado", "token"]),
  },
  methods: {
    ...mapActions(["getListado"]),
  },
};
</script>
