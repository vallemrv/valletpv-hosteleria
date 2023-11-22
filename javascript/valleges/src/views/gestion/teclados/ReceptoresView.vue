<template>
   <ValleHeader title="Receptores" anchor="bottom end" :btns="btns"/>
   <v-container>
  <valle-editor-item-vue
    title="Receptores"
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
  components: { ValleDialogoFormVue, ValleEditorItemVue, ValleHeader },
  computed: {
    ...mapState(["receptores", "itemsFiltrados"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  data() {
    return {
      showDialogo: false,
      itemSel: null,
      tb_name: "receptores",
      tipo:'md',
      titleDialogo: "Editar receptor",
      btns:[ {icon: "mdi-plus", op: "add", callback: this.op_btns}],
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "nomimp", label: "Nombre impresora", tp: "text" },
        { col: "descripcion", label: "Descripción", tp: "text" },
      ],
      tabla: {
        headers: ["Nombre", "Impresora", "Descripción"],
        keys: ["nombre", "nomimp", "descripcion"],
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
     op_btns(op){
        this.showDialogo = true;
        this.tipo = "add";
        this.itemSel = {};
        this.titleDialogo = "Agregar receptor"
        this.tb_name = "receptores"
    },
    cargarRegistro() {
      if (!this.receptores || this.receptores.length == 0) {
        this.getListadoCompuesto({ tablas: ["receptores"] });
      } else {
        this.$store.state.itemsFiltrados = this.receptores;
      }
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
            id: v.id,
          };
          let ls = this.$store.state[this.tb_name];
          this.$store.state[this.tb_name] = ls.filter((e) => {
            return e.id != v.id;
          });
          break;
      }
      if (op != "edit") {
        this.addInstruccion({ inst: inst });
      }
    },
  },
  watch: {
    receptores(v) {
      if (!v) {
        this.cargarRegistro();
      } else {
        this.$store.state.itemsFiltrados = this.receptores;
      }
    },
  },
  mounted() {
    this.cargarRegistro();
  },
};
</script>
