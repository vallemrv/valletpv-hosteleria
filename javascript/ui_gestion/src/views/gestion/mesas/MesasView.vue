<template>
  <ValleHeader title="Mesas" anchor="bottom end" :btns="btns"/>
    <v-container>
    <valle-editor-item-vue
      :tabla="tabla"
      :tb_name="tb_name"
      @click_tools="on_click_tools"
      :tools="tools"
      :filtro="filtro"
      @click_filter="on_click_filter"
    ></valle-editor-item-vue>

    <valle-dialogo-form-vue
      @close="() => (showDialogo = false)"
      :show="showDialogo"
      :title="titleDialogo"
      :item="itemSel"
      :form="formSel"
      :tb_name="tb_name"
      :tipo="tipo"
    ></valle-dialogo-form-vue>
    </v-container>
</template>

<script>
import ValleHeader from "@/components/ValleHeader.vue";
import ValleDialogoFormVue from "@/components/ValleDialogoForm.vue";
import ValleEditorItemVue from "@/components/ValleEditorItem.vue";

import { mapActions, mapGetters, mapState } from "vuex";

export default {
  components: { ValleDialogoFormVue, ValleEditorItemVue, ValleHeader },
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
      tipo:'md',
      titleDialogo: "Editar mesa",
      formSel:[],
      btns:[ {icon: "mdi-plus", op: "add", callback: this.op_btns}],
      tabla: {
        headers: ["Nombre", "Orden"],
        keys: [
          { col: "Nombre", float: true },
          { col: "Orden", float: true },
        ],
      },
      tools: [
        { op: "edit_zona", text: "Editar zona", icon: "mdi-table-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
      ],
      formMesa: [
        { col: "Nombre", label: "Nombre", tp: "text" },
        { col: "Orden", label: "Orden", tp: "number", default: 0 },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    op_btns(op){
        this.showDialogo = true;
        this.tipo = "add";
        this.formSel = this.formMesa;
        this.itemSel = {};
        this.titleForm = "Agregar mesas"
        this.tb_name = "mesas"
    },
    cargarRegistro() {
      var tablas = [];
      if (!this.mesas || this.mesas.length == 0) tablas.push("mesas");
      if (!this.zonas || this.zonas.length == 0) tablas.push("zonas");
      this.getListadoCompuesto({ tablas: tablas });
    },
    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
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
        case "edit_zona":
          this.titleDialogo = "Editar";
          this.formSel = this.formMZona;
          this.itemSel = {
            tb_name: "mesaszona",
            mesa: v.ID,
            zona: v.IDZona,
            filter: ["mesa"],
          };
          this.showDialogo = true;
          this.tipo = "md_teclados";
          break;
      }
      if (op != "edit_zona") {
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
    if (this.mesas && this.mesas.length > 0) {
      this.on_click_filter(this.localFilter);
    } else {
      this.cargarRegistro();
    }
  },
};
</script>
