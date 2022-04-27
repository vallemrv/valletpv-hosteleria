<template>
  <valle-editor-item-vue
    title="Secciones"
    :filtro="filter"
    :tabla="tabla"
    tb_name="secciones"
    @click_filter="on_click_filter"
    @click_tools="on_click_tools"
    :form="form"
    :tools="tools"
  ></valle-editor-item-vue>
  <valle-dialogo-form-vue
    @close_dialogo="() => (showDialogo = false)"
    :show="showDialogo"
    title="Editar"
    :item="itemSel"
    :form="form"
    :tb_name="tb_name"
    tipo="md"
  ></valle-dialogo-form-vue>
  <valle-teclados-dialogo
    :close="() => (showTeclados = false)"
    :show="showTeclados"
    :data="estado"
  ></valle-teclados-dialogo>
</template>

<script>
import ValleDialogoFormVue from "@/components/ValleDialogoForm.vue";
import ValleEditorItemVue from "@/components/ValleEditorItem.vue";
import ValleTecladosDialogo from "@/components/ValleTeclados.vue";
import { mapActions, mapGetters, mapState } from "vuex";

export default {
  components: { ValleDialogoFormVue, ValleEditorItemVue, ValleTecladosDialogo },
  computed: {
    ...mapState(["secciones", "teclas"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  data() {
    return {
      showDialogo: false,
      showTeclados: false,
      filterModel: [],
      itemSel: null,
      tb_name: "secciones",
      filter: {
        text_filters: [{ label: "Buscar secciones", fields: ["nombre"] }],
        all: [],
      },
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "rgb", label: "Color", tp: "color" },
        { col: "orden", label: "Orden", tp: "number" },
      ],
      tabla: {
        headers: ["Nombre", "Color", "Orden"],
        keys: ["nombre", "rgb", "orden"],
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
        { op: "add", text: "Agregar teclas", icon: "mdi-table-plus" },
        { op: "show", text: "Ver teclado", icon: "mdi-eye" },
      ],
      estado: {},
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    cargar_filtrados() {
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.filterModel,
        "secciones"
      );
    },
    cargarRegistro() {
      if (
        !this.teclas ||
        this.teclas.length == 0 ||
        !this.secciones ||
        this.secciones.length == 0
      ) {
        this.getListadoCompuesto({ tablas: ["secciones", "teclas"] });
      } else {
        this.cargar_filtrados();
      }
    },
    on_click_filter(lfilter) {
      this.filterModel = lfilter;
      this.cargar_filtrados();
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
        case "add":
          this.$router.push({ name: "teclas" });
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
        case "show":
          this.showTeclados = false;
          var f = { filters: [{ IDSeccion: v.id }, { IDSec2: v.id }] };
          this.estado.items = this.getItemsFiltered(f, "teclas");
          this.estado.cols = "3";
          this.estado.title = v.nombre;
          this.showTeclados = true;
          break;
      }
      if (op != "edit" && op != "add" && op != "show") {
        this.addInstruccion({ inst: inst });
        this.on_click_filter(this.filterModel);
      }
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
    showDialogo(v) {
      console.log(v);
    },
  },
  mounted() {
    this.filterModel = Object.values(this.filter);
    this.filterModel.filters = Object.values(this.filter.all);
    this.cargarRegistro();
  },
};
</script>
