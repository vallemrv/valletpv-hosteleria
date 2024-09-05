<template>
 <ValleHeader title="Secciones" anchor="bottom end" :btns='btns'/>
   <v-container>
  <valle-editor-item-vue
    title="Secciones"
    :tabla="tabla"
    :tb_name="tb_name"
    @click_tools="on_click_tools"
    :form="form"
    :tools="tools"
  ></valle-editor-item-vue>
  <valle-dialogo-form-vue
    @close="() => (showDialogo = false)"
    :show="showDialogo"
    :title="titleDialogo"
    :item="itemSel"
    :form="form"
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
  components: { ValleDialogoFormVue, ValleHeader, ValleEditorItemVue },
  computed: {
    ...mapState(["secciones", "itemsFiltrados"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  data() {
    return {
      showDialogo: false,
      itemSel: null,
      tb_name: "secciones",
      tipo:'md',
      titleDialogo: "Editar mesa",
      btns:[ {icon: "mdi-plus", op: "add", callback: this.op_btns}],
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "rgb", label: "Color", tp: "color", default: "255,0,255" },
        { col: "orden", label: "Orden", tp: "number", default: 0 },
      ],
      tabla: {
        headers: ["Nombre", "Color", "Orden"],
        keys: [
          { col: "nombre", float: true },
          { col: "rgb", float: true, tipo: "color" },
          { col: "orden", float: true },
        ],
      },
      tools: [
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
        { op: "add", text: "Agregar teclas", icon: "mdi-table-plus" },
        { op: "show", text: "Ver teclado", icon: "mdi-eye" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    op_btns(op){
        this.showDialogo = true;
        this.tipo = "add";
        this.itemSel = {};
        this.titleDialogo = "Agregar seccion"
        this.tb_name = "secciones"
    },
    cargarRegistro() {
      if (!this.secciones || this.secciones.length == 0) {
        this.getListadoCompuesto({ tablas: ["secciones"] });
      } else {
        this.$store.state.itemsFiltrados = this.secciones;
      }
    },
    on_click_tools(v, op) {
      var inst = {};
      switch (op) {
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
          break;
        case "show":
          var index = this.secciones.indexOf(v);
          this.$router.push({ name: "tecladostpv", params: { id: index } });
          break;
      }
      if (op != "add" && op != "show") {
        this.addInstruccion({ inst: inst });
      }
    },
  },
  watch: {
    secciones(v) {
      if (!v) {
        this.cargarRegistro();
      } else {
        this.$store.state.itemsFiltrados = this.secciones;
      }
    },
  },
  mounted() {
    this.cargarRegistro();
  },
};
</script>
