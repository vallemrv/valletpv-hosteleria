<template>
  <valle-header :btns="btns" title="Precios"></valle-header>
 
  <v-container>
      <v-row>
      <v-col>
        <valle-filtros-vue
          :filtro="filtro"
          @click_filter="on_filter_change"
        ></valle-filtros-vue>
      </v-col>

      <valle-tecla-precio v-for="(tecla, i) in listTeclas" :key="i" :tecla="tecla">
      </valle-tecla-precio>
    </v-row>

    <valle-dialogo-form 
        @close="on_close_dialogo"
         :show="showDialogo" 
         :tb_name="tb_name" 
         :form="form"
         :item="{}"
         :title="titleForm"
         :tipo="tipo"></valle-dialogo-form>
</v-container>
 
</template>

<script>
import { mapState, mapActions, mapGetters } from "vuex";
import ValleHeader from "@/components/ValleHeader.vue";
import ValleTeclaPrecio from "@/comp_especificos/ValleTeclaPrecio";
import ValleFiltrosVue from "@/components/ValleFiltros.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  components: { ValleTeclaPrecio, ValleFiltrosVue, ValleHeader, ValleDialogoForm },
  data() {
    return {
      localFilter: null,
      tb_name: "teclas",
      showDialogo: false,
      tipo:"add",
      titleForm: "Agregar tecla",
      itemSel: null,
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto"]),
    on_click_tools(op){
      if (op=="add-tecla"){
        this.showDialogo = true;
        this.tb_name = "teclas",
        this.titleForm = "Agregar tecla",
        this.tipo = "add"
      }
    },
    on_close_dialogo(item){
      this.showDialogo = false;
    },
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.subteclas || this.subteclas.length <= 0) request.push("subteclas");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      if (!this.familias || this.familias.length <= 0) request.push("familias");
      if (request.length > 0) this.getListadoCompuesto({ tablas: request });
    },
    on_filter_change(f) {
      this.localFilter = f;
    },
  },
  computed: {
    ...mapState(["teclas", "subteclas", "secciones", "familias", "ocupado"]),
    ...mapGetters(["getFilters", "getListValues", "getItemsFiltered"]),
    btns(){
      let aux = [{icon:"mdi-plus", callback:this.on_click_tools, op:"add-tecla"}]
      if (this.itemSel){
        aux.push({icon:"mdi-tools", callback:this.on_click_tools, op:"tools"})
      }
      return aux;
    },
    listTeclas() {
      return this.localFilter
        ? this.getItemsFiltered(this.localFilter, this.tb_name)
        : [];
    },
    filtro() {
      return {
        caption: this.getListValues("secciones", "nombre"),
        filters: this.getFilters("secciones", "id", ["IDSeccion", "IDSec2"]),
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "tag"] }],
        all: [{ IDSeccion: -1 }],
        multiple: false,
      };
    },
    form(){
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
      ]
    }
  },
  watch: {
    teclas(v) {
      if (!v) {
        this.getTablas();
      }
    },
    subteclas(v) {
      if (!v) {
        this.getTablas();
      }
    },
  },
  mounted() {
    this.localFilter = Object.values(this.filtro);
    this.localFilter.filters = Object.values(this.filtro.all);
    this.getTablas();
  },
};
</script>


<style scoped>
  .float-button{
    position: absolute;
    z-index: 100;
    right: 10px;
    top: 30px;
  }
</style>