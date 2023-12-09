<template>

    <v-row class="pa-5">  
      <v-col cols="12" v-if="filtro">
        <valle-filtros :filtro="filtro" @click_filter="on_click_filter"></valle-filtros>
      </v-col>

      <v-col cols="12" v-if="tabla">
        <valle-listados-tb
          :items="itemsFiltrados"
          :columns="tabla.keys"
          :headers="tabla.headers"
          :tools="tools"
          @click_tools="on_click_tools"
          :tb_name="tb_name"
        ></valle-listados-tb>
      </v-col>

      <valle-dialogo-form
        @close="() => (showDialog = false)"
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
import ValleHeader from "@/components/ValleHeader.vue";
import ValleListadosTb from "@/components/ValleListadosTb.vue";
import ValleFiltros from "@/components/ValleFiltros.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  props: ["title", "filtro", "tb_name", "form", "tabla", "tools"],
  components: { ValleListadosTb, ValleFiltros, ValleDialogoForm, ValleHeader },
  data() {
    return {
      itemSel: {},
      showDialog: false,
      titleDialogo: "Nuevo",
      tipo: "add",
      btns:[{icon:"mdi-plus", callback: this.on_click_new, op:"new"}]
    };
  },
  computed: {
    ...mapState(["ocupado", "itemsFiltrados"]),
  },
  methods: {
    on_click_filter(f) {
      this.$emit("click_filter", f);
    },
    on_click_new() {
      this.itemSel = this.$tools.newItem(this.form);
      this.showDialog = true;
      this.tipo = "add";
    },
    on_click_tools(value, op) {
      this.$emit("click_tools", value, op);
    },
  },
};
</script>
