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
</template>

<script>
import { mapGetters, mapState, mapActions } from "vuex";
import ValleEditorItem from "@/components/ValleEditorItem.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  components: { ValleEditorItem, ValleDialogoForm },
  computed: {
    ...mapGetters(["getItemsFiltered", "getListValues", "getFilters"]),
    ...mapState(["teclas", "familias", "secciones"]),
    filtro() {
      return {
        caption: this.getListValues("secciones", "nombre"),
        filters: this.getFilters("secciones", "id", "IDSeccion"),
        tools: [],
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "p1"] }],
        all: [{ IDSeccion: -1 }, { IDSeccion: undefined }],
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
          tp: "number",
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
      itemSel: null,
      dddform: [
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
          tp: "number",
        },
        {
          col: "familia__nombre__familias",
          label: "Familia",
          tp: "select",
          choices: [],
        },
      ],
      extfiltro: {
        caption: [],
        filters: [],
        tools: [],
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "p1"] }],
        all: [{ IDSeccion: -1 }, { IDSeccion: undefined }],
        multiple: false,
      },
      tabla: {
        headers: col,
        keys: col,
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        {
          op: "rm",
          text: "Borrar",
          icon: "mdi-delete",
        },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    cargar_reg() {
      var request = ["teclas"];
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
      //this.form[4].choices = this.getListValues("familias", "nombre");
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
        case "minus":
          inst = {
            tb: this.tb_name,
            reg: { activo: 0, autorizado: 0 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 0;
          v.autorizado = 0;

          break;
        case "activar":
          inst = {
            tb: this.tb_name,
            reg: { activo: 1 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 1;

          break;
        case "unlock":
          inst = {
            tb: this.tb_name,
            reg: { pass_field: "" },
            tipo: "md",
            id: v.id,
          };
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
      if (op != "edit") {
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
