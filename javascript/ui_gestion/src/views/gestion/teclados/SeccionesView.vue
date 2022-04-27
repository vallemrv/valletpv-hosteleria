<template>
  <valle-editor-item-vue
    title="Secciones"
    :filtro="filter"
    :tabla="tabla"
    tb_name="secciones"
    @on_click_filter="on_click_filter"
    :form="form"
    :tools="tools"
  ></valle-editor-item-vue>
  <valle-dialogo-form-vue
    @on_visible_change="on_visible_change"
    :show="showDialog"
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
    ...mapState(["secciones"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  data() {
    return {
      showDialgo: false,
      filterModel: [],
      itemSel: null,
      filter: {
        text_filters: [{ label: "Buscar secciones", fields: ["nombre"] }],
        all: [],
      },
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "rgb", label: "Nombre", tp: "color" },
        { col: "orden", lable: "Orden", tp: "number" },
      ],
      tabla: {
        headers: ["Nombre", "Color", "Orden"],
        keys: ["nombre", "rgb", "orden"],
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListado", "addInstruccion"]),
    cargar_filtrados() {
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.filterModel,
        "secciones"
      );
    },
    cargarRegistro() {
      if (!this.secciones || this.secciones.length == 0) {
        this.getListado({ tabla: "secciones" });
      } else {
        this.cargar_filtrados();
      }
    },
    on_click_filter(lfilter) {
      this.filterModel = lfilter;
      this.cargar_filtrados();
    },
  },
  watch: {
    secciones(v) {
      if (v) {
        this.cargar_filtrados();
      } else {
        this.cargarRegistro();
      }
    },
  },
  mounted() {
    this.filterModel = Object.values(this.filter);
    this.filterModel.filters = Object.values(this.filter.all);
    this.cargarRegistro();
  },
};
</script>
