<template>
  <v-app-bar color="#cfb6d4" class="w-100" dense fixed>
     <v-btn icon @click="$router.go(-1)"> <v-icon>mdi-arrow-left</v-icon></v-btn>
     <v-toolbar-title >
        <v-row class="pa-2 pl-3">
          <v-col cols="12" class="pa-0 ma-0 text-uppercase">{{ title }}</v-col>
          <v-col v-if="empresa" cols="12" class="pa-0 ma-0 text-caption">{{ empresa.nombre }} </v-col>
        </v-row>
     </v-toolbar-title>
     <v-progress-circular
            indeterminate
            color="primary"
            v-if="ocupado"
      ></v-progress-circular>
    <v-spacer></v-spacer>
    <valle-inbox :anchor="anchor" :num_inst="num_inst" v-if="num_inst > 0"></valle-inbox>
     <v-btn v-for="(btn, i) in btns" :key="i" icon @click="btn.callback(btn.op)">
     <v-icon>{{btn.icon}}</v-icon>
     </v-btn>
  </v-app-bar>
</template>

<script>
import { mapState, mapActions } from "vuex";
import ValleInbox from "./ValleInbox.vue";
export default {
  components: { ValleInbox },
  props:["title", "btns", "anchor"],
  data: () => ({ }),
  computed: {
    ...mapState(["empresa",  "ocupado", "instrucciones"]),
    num_inst() {
      return this.instrucciones ? this.instrucciones.length : 0;
    },
  },
  methods: {
    ...mapActions(["getListado"]),
  },
};
</script>

<style scoped>
a {
  text-decoration: none;
  color: #000;
}
</style>
