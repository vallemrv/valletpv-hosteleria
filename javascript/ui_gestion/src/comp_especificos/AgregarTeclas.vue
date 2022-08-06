<template>
  <v-card>
    <v-card-title>
      <v-toolbar class="w-100" dark color="primary">
        <v-btn icon dark @click="$emit('close')">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>Agregar Teclas</v-toolbar-title>
        <v-spacer></v-spacer>
      </v-toolbar>
    </v-card-title>
    <v-card-text>
      <v-row class="pa-2">
        <v-col>
          <valle-filtros-vue
            :filtro="filtro"
            @click_filter="on_filter_change"
          ></valle-filtros-vue>
        </v-col>
        <v-col class="ma-1" cols="12" v-for="(obj, i) in items" :key="i">
          <v-card class="pa-3">
            {{ obj.nombre }}
            <v-btn icon class="float-end" @click="add_item(obj)"
              ><v-icon>mdi-plus</v-icon></v-btn
            >
            <div class="clearfix"></div>
          </v-card>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>
<script>
import ValleFloatFormVue from "@/components/ValleFloatForm.vue";
import ValleFiltrosVue from "../components/ValleFiltros.vue";
import { mapGetters, mapActions } from "vuex";
export default {
  components: { ValleFloatFormVue, ValleFiltrosVue },
  props: ["tb_name", "tb_mod", "item", "col", "col_paren", "filter"],
  data() {
    return {
      items: [],
      filtro: {
        text_filters: [{ label: "Buscar tecla", fields: ["nombre", "tag"] }],
      },
    };
  },
  computed: {
    ...mapGetters(["getItemsFiltered"]),
  },
  methods: {
    ...mapActions(["addInstruccion"]),
    on_filter_change(f) {
      this.items = this.getItemsFiltered(f, "teclas");
    },
    add_item(v) {
      var inst = {
        tb: this.tb_name,
        tb_mod: this.tb_mod,
        reg: { orden: this.item.orden, seccion: this.item.seccion, tecla: v.id },
        tipo: "md_teclados",
        filter: this.filter,
      };
      this.addInstruccion({ inst: inst });
      this.$emit("change");
    },
  },
};
</script>

<style scoped>
.clearfix {
  clear: both;
}
</style>
