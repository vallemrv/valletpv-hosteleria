<template>
  <v-app-bar color="#cfb6d4" class="w-100" dense fixed>
     <v-btn icon @click="$router.go(-1)"> <v-icon>mdi-arrow-left</v-icon></v-btn>
     <v-toolbar-title >
        <v-row class="pa-2 pl-3">
          <v-col cols="12" class="pa-0 ma-0 text-uppercase">{{ title }}</v-col>
          <v-col cols="12" class="pa-0 ma-0 text-caption">{{ empresa.nombre }} </v-col>
        </v-row>
     </v-toolbar-title>

     <v-progress-circular
            indeterminate
            color="primary"
            v-if="ocupado"
          ></v-progress-circular>
    
    <valle-inbox :anchor="anchor" :num_inst="num_inst" v-if="num_inst > 0"></valle-inbox>
     <v-btn v-for="(btn, i) in btns" :key="i" icon @click="btn.callback(btn.op)">
        <v-icon>{{btn.icon}}</v-icon>
     </v-btn>
     <v-btn icon v-if="empresas.length > 0">
      <v-icon>mdi-dots-vertical</v-icon>
      <v-menu location="bottom start" origin="end" activator="parent">
        <v-list>
          <v-list-item
            v-for="(item, index) in empresas"
            :key="index"
            :value="index"
            @click="selEmpresa(index)"
          >
            <v-list-item-title>{{ item.nombre }}   
              <v-icon v-if="item.nombre == empresa.nombre">mdi-check</v-icon>
          </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
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
    ...mapState(["empresa", "empresas",  "ocupado", "instrucciones"]),
    num_inst() {
      return this.instrucciones ? this.instrucciones.length : 0;
    },
  },
  methods: {
    ...mapActions(["getListado", "selEmpresa"]),
  },
};
</script>

<style scoped>
a {
  text-decoration: none;
  color: #000;
}
</style>
