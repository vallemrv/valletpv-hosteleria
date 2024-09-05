<template>
   <ValleHeader title="Familias" anchor="bottom end" :btns="btns"/>
   <v-container>
      <valle-editor-item-vue
        title="Familias"
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
import ValleTecladosDialogo from "@/components/ValleTeclados.vue";
import { mapActions, mapGetters, mapState } from "vuex";

export default {
  components: { ValleDialogoFormVue, ValleEditorItemVue,
                ValleHeader, ValleTecladosDialogo },
  computed: {
    ...mapState(["familias", "receptores", "itemsFiltrados"]),
    ...mapGetters(["getItemsFiltered", "getListValues"]),
    form() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        {
          col: "composicion",
          label: "Composicion",
          tp: "multiple",
          choices: this.getListValues("familias", "nombre"),
        },
        { col: "cantidad", label: "Cantidad Composicion", tp: "number" },
        {
          col: "receptor",
          label: "Receptor",
          tp: "select",
          required: true,
          choices: this.getListValues("receptores", "nombre"),
          keys: this.getListValues("receptores", "id"),
        },
      ];
    },
  },
  data() {
    return {
      showDialogo: false,
      itemSel: null,
      tb_name: "familias",
      tipo:'md',
      titleDialogo: "Editar familia",
      btns:[ {icon: "mdi-plus", op: "add", callback: this.op_btns}],
      tabla: {
        headers: ["Nombre", "Composicion", "Cantidad derivados", "Recetor"],
        keys: [
          "nombre",
          "composicion",
          "cantidad",
          { col: "receptor", key: "id", value: "nombre", tb_name: "receptores" },
        ],
      },
      tools: [
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        { op: "rm", text: "Borrar", icon: "mdi-delete" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    get_familias_mapeadas(){
    return this.familias.map((e) => {
        if (e.composicion) {
            e.composicion = e.composicion.replace(/'/g, '"');
            try {
                e.composicion = JSON.parse(e.composicion);
            } catch (error) {
                console.error('Error parsing JSON:', error);
                e.composicion = []; // asigna un array vacío si hay un error
            }
        } else {
            e.composicion = [];
        }
        return e;
    });
},

    op_btns(op){
        this.showDialogo = true;
        this.tipo = "add";
        this.itemSel = {};
        this.titleDialogo = "Agregar familia"
        this.tb_name = "familias"
    },
    cargarRegistro() {
      if (
        !this.receptores ||
        this.receptores.length == 0 ||
        !this.familias ||
        this.familias.length == 0
      ) {
        this.getListadoCompuesto({ tablas: ["familias", "receptores", "teclas"] });
      } else {
        //this.$store.state.itemsFiltrados = this.get_familias_mapeadas();
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
      if (op != "edit" && op != "add" && op != "show") {
        this.addInstruccion({ inst: inst });
      }
    },
  },
  watch: {
    familias(v) {
      if (!v) {
        this.cargarRegistro();
      } else {
        this.$store.state.itemsFiltrados =  this.get_familias_mapeadas() ;
      }
    },
    receptores(v) {},
  },
  
  mounted() {
    this.cargarRegistro();
  },
};
</script>
