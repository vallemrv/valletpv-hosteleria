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
        <v-col class="ma-1" cols="12" v-for="(item, i) in items" :key="i">
          <v-card class="pa-3">
            {{ item.nombre }}
            <v-btn icon class="float-end" @click="add_item(item)"
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
  props: ["tb_name", "secSel"],
  data() {
    return {
      items: [],
      filtro: {
        text_filters: [{ label: "Buscar tecla", fields: ["nombre"] }],
      },
    };
  },
  computed: {
    ...mapGetters(["getItemsFiltered"]),
  },
  methods: {
    ...mapActions(["addInstruccion"]),
    on_filter_change(f) {
      this.items = this.getItemsFiltered(f, this.tb_name);
    },
    add_item(item) {
      var inst = {
        tb: "teclascom",
        reg: {
          orden: 0,
          tecla__id__teclas: item.id,
          seccion__id__seccionescom: this.secSel.id,
        },
        tipo: "add",
      };
      console.log(this.secSel);
      item.IDSeccionCom = this.secSel.id;
      item.OrdenCom = 0;
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
