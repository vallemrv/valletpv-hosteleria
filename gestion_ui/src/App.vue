<template>
  <v-app>
    <valle-header></valle-header>
    <v-main>
      <v-container><router-view></router-view></v-container>
    </v-main>
    <v-footer app>
      <valle-footer></valle-footer>
    </v-footer>
  </v-app>
</template>

<script>
import ValleFooter from "./components/ValleFooter";
import ValleHeader from "./components/ValleHeader";

export default {
  components: { ValleFooter, ValleHeader },
  name: "App",

  data: () => ({
    //
  }),
};
</script>
