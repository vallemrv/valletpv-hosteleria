<template>
  <valle-editor-item-vue
    title="Zonas"
    :tabla="tabla"
    :tb_name="tb_name"
    @click_tools="on_click_tools"
    :form="form"
    :tools="tools"
  ></valle-editor-item-vue>
  <valle-dialogo-form-vue
    @close="() => (showDialogo = false)"
    :show="showDialogo"
    title="Editar"
    :item="itemSel"
    :form="form"
    :tb_name="tb_name"
    tipo="md"
  ></valle-dialogo-form-vue>
</template>

<script>
import ValleDialogoFormVue from "@/components/ValleDialogoForm.vue";
import ValleEditorItemVue from "@/components/ValleEditorItem.vue";

import { mapActions, mapGetters, mapState } from "vuex";

export default {
  components: { ValleDialogoFormVue, ValleEditorItemVue },
  computed: {
    ...mapState(["zonas", "itemsFiltrados"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  data() {
    return {
      showDialogo: false,
      itemSel: null,
      tb_name: "zonas",
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "rgb", label: "Color", tp: "color", default: "255,0,255" },
        {
          col: "tarifa",
          label: "Tarifa",
          tp: "select",
          choices: ["1", "2"],
          keys: [1, 2],
          default: 1,
        },
      ],
      tabla: {
        headers: ["Nombre", "Color", "Tarifa"],
        keys: [
          { col: "nombre", float: true },
          { col: "rgb", float: true, tipo: "color" },
          { col: "tarifa", float: true },
        ],
      },
      tools: [
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
        { op: "add", text: "Agregar mesas", icon: "mdi-table-plus" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    cargarRegistro() {
      if (!this.zonas || this.zonas.length == 0) {
        this.getListadoCompuesto({ tablas: ["zonas"] });
      } else {
        this.$store.state.itemsFiltrados = this.zonas;
      }
    },
    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
        case "add":
          this.$router.push({ name: "mesas" });
          break;
        case "rm":
          inst = {
            tb: this.tb_name,
            tipo: "rm",
            id: v.id,
          };
          let ls = this.$store.state[this.tb_name];
          this.$store.state[this.tb_name] = ls.filter((e) => {
            return e.id != v.id;
          });
          break;
      }
      if (op != "add") {
        this.addInstruccion({ inst: inst });
      }
    },
  },
  watch: {
    zonas(v) {
      if (!v) {
        this.cargarRegistro();
      } else {
        this.$store.state.itemsFiltrados = this.zonas;
      }
    },
  },
  mounted() {
    this.cargarRegistro();
  },
};
</script>
