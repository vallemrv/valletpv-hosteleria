<template>
  <valle-editor-item
    :title="title"
    :filtro="filtro"
    :tb_name="tb_name"
    :form="form"
    :tabla="tabla"
    :tools="tools"
    @click_filter="on_click_filter"
    @click_tools="on_click_tools"
  >
  </valle-editor-item>
  <valle-dialogo-form
    @close="() => (showDialog = false)"
    :show="showDialog"
    :title="titleForm"
    :item="itemSel"
    :form="formComp"
    tb_name="composicionteclas"
    :tipo="tipo"
  >
  </valle-dialogo-form>
</template>

<script>
import ValleEditorItem from "@/components/ValleEditorItem.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";
import { mapState, mapGetters, mapActions } from "vuex";
export default {
  components: { ValleEditorItem, ValleDialogoForm },
  data() {
    const colCom = ["nombre", "composicion", "cantidad"];
    const colTecla = ["nombre", "p1", "p2"];
    return {
      title: "Menú y ofertas",
      localFilter: null,
      tb_name: "teclas",
      showDialog: false,
      titleForm: "Agregar",
      tipo: "add",
      tabla: null,
      tablaComp: {
        headers: colCom,
        keys: colCom,
      },
      tablaTecla: {
        headers: colTecla,
        keys: colTecla,
      },
      itemSel: null,
      toolsComp: [
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
        { op: "md", text: "Editar", icon: "mdi-pencil" },
      ],
      toolsTecla: [{ op: "add_comp", text: "Agregar composicion", icon: "mdi-group" }],
      tools: null,
      filtro: {
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "tag"] }],
        all: [],
      },
    };
  },
  computed: {
    ...mapState(["ocupado", "familias", "teclas", "composicionteclas"]),
    ...mapGetters(["getListValues", "getItemsFiltered"]),
    formComp() {
      return [
        {
          col: "composicion",
          label: "Grupo de familias",
          tp: "multiple",
          choices: this.getListValues("familias", "nombre"),
        },
        {
          col: "cantidad",
          label: "Cantidad articulos",
          tp: "number",
        },
      ];
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
          col: "familia",
          label: "Familia",
          tp: "select",
          keys: this.getListValues("familias", "id"),
          choices: this.getListValues("familias", "nombre"),
        },
      ];
    },
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    mostrarTeclasComp() {
      this.$store.state.itemsFiltrados = this.getItemsFiltered(null, "composicionteclas");
      this.tools = this.toolsComp;
      this.tabla = this.tablaComp;
    },
    cargar_reg() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.familias || this.familias.length <= 0) request.push("familias");
      if (!this.composicionteclas || this.composicionteclas.length <= 0)
        request.push("composicionteclas");
      this.getListadoCompuesto({ tablas: request });
    },
    on_click_filter(lfilter) {
      this.localFilter = lfilter;
      this.$store.state.itemsFiltrados = this.getItemsFiltered(
        this.localFilter,
        this.tb_name
      );
      this.tools = this.toolsTecla;
      this.tabla = this.tablaTecla;
    },

    on_click_tools(v, op) {
      switch (op) {
        case "add_comp":
          this.itemSel = { tecla: v.id, composicion: [], cantidad: 0 };
          this.showDialog = true;
          this.titleForm = "Agregar composicion";
          this.tipo = "add";
          break;
        case "md":
          this.itemSel = v;
          this.showDialog = true;
          this.tipo = "md";
          this.titleForm = "Editar composicion";
          break;
        case "rm":
          var inst = {
            tb: "composicionteclas",
            id: v.id,
            tipo: "rm",
          };

          this.$store.state["composicionteclas"] = this.composicionteclas.filter((e) => {
            return e.id != v.id;
          });
          this.addInstruccion({ inst: inst });
          //this.mostrarTeclasComp();
          break;
      }
    },
  },
  watch: {
    composicionteclas(v) {
      if (v) {
        this.mostrarTeclasComp();
      } else {
        this.cargar_reg();
      }
    },
  },
  mounted() {
    if (this.composicionteclas) {
      this.mostrarTeclasComp();
    } else {
      this.cargar_reg();
    }
  },
};
</script>
