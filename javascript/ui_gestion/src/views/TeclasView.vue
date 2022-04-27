<template>
  <valle-editor-item
    :title="title"
    :filtro="filtro"
    :tb_name="tb_name"
    :form="form"
    :tabla="tabla"
    :tools="tools"
    @on_click_filter="on_click_filter"
    @on_click_tools="on_click_tools"
  >
  </valle-editor-item>
  <valle-dialogo-form
    @on_visible_change="on_visible_change"
    :show="showDialog"
    title="Editar"
    :item="itemSel"
    :form="form"
    :tb_name="tb_name"
    tipo="md"
  >
  </valle-dialogo-form>
  <valle-secciones-tecla-vue
    :item="itemSel"
    :show="showEditSec"
    @close_edit_sec="on_close_edit_sec"
  ></valle-secciones-tecla-vue>
</template>

<script>
import { mapGetters, mapState, mapActions } from "vuex";
import ValleEditorItem from "@/components/ValleEditorItem.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";
import ValleSeccionesTeclaVue from "@/comp_especificos/ValleSeccionesTecla.vue";

export default {
  components: { ValleEditorItem, ValleDialogoForm, ValleSeccionesTeclaVue },
  computed: {
    ...mapGetters(["getItemsFiltered", "getListValues", "getFilters"]),
    ...mapState(["teclas", "familias", "secciones"]),
    filtro() {
      return {
        caption: this.getListValues("secciones", "nombre"),
        filters: this.getFilters("secciones", "id", ["IDSeccion", "IDSec2"]),
        tools: [],
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "p1"] }],
        all: [{ IDSeccion: -1 }, { IDSec2: -2 }],
        multiple: true,
      };
    },
    form() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "p1", label: "Precio 1", tp: "number" },
        {
          col: "p2",
          label: "Precio 2",
          tp: "number",
        },
        {
          col: "tag",
          label: "tag",
          tp: "text",
        },
        {
          col: "descripcion_r",
          label: "Texto para la recepción",
          tp: "text",
        },
        {
          col: "descripcion_t",
          label: "Texto para el ticket",
          tp: "text",
        },
        {
          col: "familia__nombre__familias",
          label: "Familia",
          tp: "select",
          choices: this.getListValues("familias", "nombre"),
        },
      ];
    },
  },
  data() {
    const col = ["nombre", "p1", "p2"];
    return {
      title: "Teclas",
      tb_name: "teclas",
      localFilter: [],
      showDialog: false,
      showEditSec: false,
      itemSel: null,
      tabla: {
        headers: col,
        keys: col,
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
        { op: "sec", text: "Modificar secciones", icon: "mdi-pencil" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    on_close_edit_sec() {
      this.showEditSec = false;
    },
    cargar_reg() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.familias || this.familias.length <= 0) request.push("familias");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      this.getListadoCompuesto({ tablas: request });
    },
    on_visible_change(value) {
      this.showDialog = value;
    },
    on_click_filter(lfilter) {
      this.localFilter = lfilter;
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.localFilter,
        this.tb_name
      );
    },
    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
        case "edit":
          this.titleDialogo = "Editar";
          v.permisos = this.$tools.stringToArray(v.permisos);
          this.itemSel = v;
          this.showDialog = true;
          this.tipo = "md";
          break;
        case "sec":
          this.showEditSec = true;
          this.itemSel = v;
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
      }
      if (op != "edit" && op != "sec") {
        this.addInstruccion({ inst: inst });
        this.on_click_filter(this.localFilter);
      }
    },
  },
  watch: {
    teclas(v) {
      if (v) {
        this.on_click_filter(this.localFilter);
      } else {
        this.cargar_reg();
      }
    },
  },
  mounted() {
    this.localFilter = Object.values(this.filtro);
    this.localFilter.filters = Object.values(this.filtro.all);
    if (this.teclas) {
      this.on_click_filter(this.localFilter);
    } else {
      this.cargar_reg();
    }
  },
};
</script>
