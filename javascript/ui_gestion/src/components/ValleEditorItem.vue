<template>
  <v-row class="pa-5">
    <v-col cols="12" class="">
      <v-toolbar color="#cfb6d4">
        <v-toolbar-title>
          {{ title }}
          <v-progress-circular
            indeterminate
            color="primary"
            v-if="ocupado"
          ></v-progress-circular
        ></v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn v-if="form" stacked size="small" @click="on_click_new"
          ><v-icon>mdi-newspaper-plus</v-icon></v-btn
        >
      </v-toolbar>
    </v-col>
    <v-col cols="12">
      <valle-filtros :filtro="filtro" @on_filter="on_filter"></valle-filtros>
    </v-col>
    <v-col cols="12">
      <valle-listados-tb
        :items="itemsFiltrados"
        :columns="tabla.keys"
        :headers="tabla.headers"
        :tools="tools"
        @on_click_tools="on_click_tools"
      ></valle-listados-tb>
    </v-col>
    <valle-dialogo-form
      @on_visible_change="on_visible_change"
      :show="showDialog"
      :title="titleDialogo"
      :item="itemSel"
      :form="form"
      :tb_name="tb_name"
      :tipo="tipo"
    >
    </valle-dialogo-form>
  </v-row>
</template>

<script>
import { mapState } from "vuex";
import ValleListadosTb from "@/components/ValleListadosTb.vue";
import ValleFiltros from "@/components/ValleFiltros.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  props: ["title", "filtro", "tb_name", "form", "tabla", "tools"],
  components: { ValleListadosTb, ValleFiltros, ValleDialogoForm },
  data() {
    return {
      itemSel: {},
      showDialog: false,
      titleDialogo: "Nuevo",
      tipo: "add",
    };
  },
  computed: {
    ...mapState(["ocupado", "itemsFiltrados"]),
  },
  methods: {
    on_filter(f) {
      this.$emit("on_click_filter", f);
    },
    on_click_new() {
      this.itemSel = this.$tools.newItem(this.form);
      this.showDialog = true;
      this.tipo = "add";
    },
    on_visible_change(value) {
      this.showDialog = value;
    },
    on_click_tools(value, op) {
      this.$emit("on_click_tools", value, op);
    },
  },
};
</script>
