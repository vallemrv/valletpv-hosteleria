<template>
  <valle-editor-item-vue
    title="Mesas"
    :tabla="tabla"
    :tb_name="tb_name"
    @click_tools="on_click_tools"
    :form="form"
    :tools="tools"
    :filtro="filtro"
    @click_filter="on_click_filter"
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
    ...mapState(["mesas", "zonas", "itemsFiltrados"]),
    ...mapGetters(["getItemsFiltered", "getListValues", "getFilters", "getItemsOrdered"]),
    filtro() {
      return {
        caption: this.getListValues("zonas", "nombre"),
        filters: this.getFilters("zonas", "id", ["IDZona"]),
        all: [{ IDZona: -1 }],
      };
    },
    formMZona() {
      return [
        {
          col: "zona",
          label: "Zona",
          tp: "select",
          keys: this.getListValues("zonas", "id"),
          choices: this.getListValues("zonas", "nombre"),
        },
      ];
    },
  },
  data() {
    return {
      showDialogo: false,
      itemSel: null,
      tb_name: "mesas",
      localFilter: [],
      tabla: {
        headers: ["Nombre", "Orden"],
        keys: ["Nombre", { col: "Orden", float: true }],
      },
      form: [],
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "edit_zona", text: "Editar zona", icon: "mdi-table-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
      ],
      formMesa: [
        { col: "Nombre", label: "Nombre", tp: "text" },
        { col: "Orden", label: "Orden", tp: "number" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    cargarRegistro() {
      var tablas = [];
      if (!this.mesas || this.mesas.length == 0) tablas.push("mesas");
      if (!this.zonas || this.zonas.length == 0) tablas.push("zonas");
      this.getListadoCompuesto({ tablas: tablas });
    },
    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
        case "edit":
          this.titleDialogo = "Editar";
          this.itemSel = v;
          this.showDialogo = true;
          this.tipo = "md";
          break;
        case "rm":
          inst = {
            tb: this.tb_name,
            tipo: "rm",
            id: v.ID,
          };
          let ls = Object.values(this.$store.state.itemsFiltrados);
          this.$store.state.itemsFiltrados = ls.filter((e) => {
            return e.ID != v.ID;
          });
          break;
      }
      if (op != "edit") {
        this.addInstruccion({ inst: inst });
      }
    },
    on_click_filter(lfilter) {
      this.localFilter = lfilter;
      var filtrados = this.getItemsFiltered(this.localFilter, this.tb_name);
      this.$store.state.itemsFiltrados = this.getItemsOrdered(filtrados);
    },
  },
  watch: {
    mesas(v) {
      if (v) {
        this.on_click_filter(this.localFilter);
      } else {
        this.cargarRegistro();
      }
    },
  },
  mounted() {
    this.localFilter = Object.values(this.filtro);
    this.localFilter.filters = Object.values(this.filtro.all);
    this.cargarRegistro();
  },
};
</script>
