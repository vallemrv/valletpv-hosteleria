<template>
 <valle-header anchor="bottom center" :btns="btns" title="Teclas"></valle-header>
  <v-container>
    <v-row>
        <v-col cols="12">
          <valle-filtros
            :filtro="filtro"
            @click_filter="on_filter_change"
          ></valle-filtros>
        </v-col>

        <vista-precio-tecla v-if="vista=='precio'" :listTeclas="listTeclas"></vista-precio-tecla>
        <vista-tecla-mod  v-else-if="vista=='teclas'" @click_tools="on_click_tools_tecla" :listTeclas="listTeclas"></vista-tecla-mod>
        <vista-tecla-vista v-else-if="vista=='vista'"  :listTeclas="listTeclas" ></vista-tecla-vista>
      </v-row>
  </v-container>

  <valle-dialogo-form 
        @close="on_close_dialogo"
         :show="showDialogo" 
         :tb_name="tb_name" 
         :form="form"
         :item="itemSel"
         :title="titleForm"
         :tipo="tipo"></valle-dialogo-form>

  <valle-secciones-tecla
    :item="itemSel"
    :show="showEditSec"
    @close_edit_sec="on_close_edit_sec"
  ></valle-secciones-tecla>
</template>

<script>
import { mapGetters, mapState, mapActions } from "vuex";

import ValleHeader from "@/components/ValleHeader";
import ValleFiltros from "@/components/ValleFiltros";
import ValleDialogoForm from "@/components/ValleDialogoForm";
import ValleSeccionesTecla from "@/comp_especificos/ValleSeccionesTecla";

import VistaPrecioTecla from "./components/VistaPrecioTecla";
import VistaTeclaMod from "./components/VistaTeclaMod";
import VistaTeclaVista from "./components/VistaTeclaVista";


export default {
  components: { ValleHeader, ValleFiltros, VistaPrecioTecla, 
                VistaTeclaMod, VistaTeclaVista, ValleDialogoForm,
                ValleSeccionesTecla },
  computed: {
    ...mapGetters(["getItemsFiltered", "getListValues", "getFilters"]),
    ...mapState(["teclas", "familias", "secciones", "subteclas"]),
    listTeclas() {
      return this.localFilter
        ? this.getItemsFiltered(this.localFilter, this.tb_name)
        : [];
    },
    filtro() {
      return {
        caption: this.getListValues("secciones", "nombre"),
        filters: this.getFilters("secciones", "id", ["IDSeccion", "IDSec2"]),
        tools: [],
        text_filters: [{ label: "Buscar teclas", fields: ["nombre", "tag"] }],
        all: [{ IDSeccion: -1 }],
        multiple: false,
      };
    },
    formTecla(){
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
    },
    
    btns(){
      let aux = [
        {icon:"mdi-dialpad", callback:this.on_click_tools, op:"ch-teclas"},
        {icon:"mdi-cash", callback:this.on_click_tools, op:"ch-precio"},
        {icon:"mdi-eye", callback:this.on_click_tools, op:"ch-vista"},
        {icon:"mdi-plus", callback:this.on_click_tools, op:"add-tecla"},
      ]
      
      return aux;
    },
  },
  data() {
    return {
       showDialogo: false,
       showEditSec: false,
       titleForm: "Agregar",
       tb_name: "teclas",
       tipo:"add",
       localFilter: [],
       vista: "teclas",
       itemSel: {},
       form: null,
       formTeclaSub:[
            {
              col: "tipo",
              label: "Tipo",
              tp: "select",
              keys: ["SP", "CM"],
              choices: ["SIMPLE", "COMPUESTA"],
            },
         ],
      formSubTecla: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "descripcion_t", label: "Texto ticket", tp: "text" },
        { col: "descripcion_r", label: "Texto recepcion", tp: "text" },
        { col: "incremento", label: "Incremento", tp: "number" },
      ],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.subteclas || this.subteclas.length <= 0) request.push("subteclas");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      if (!this.familias || this.familias.length <= 0) request.push("familias");
      if (request.length > 0){
        this.getListadoCompuesto({ tablas: request });
      }
    },
    on_filter_change(f) {
      this.tb_name = "teclas"
      this.localFilter = f;
    },
    on_click_tools(op){
      if (op=="add-tecla"){
        this.showDialogo = true;
        this.tb_name = "teclas",
        this.titleForm = "Agregar tecla",
        this.tipo = "add"
        this.itemSel = {}
        this.form = this.formTecla
      } else if (op == "ch-teclas"){
        this.vista = "teclas"
      } else if (op == "ch-precio"){
        this.vista = "precio"
      } else if (op == "ch-vista"){
        this.vista = "vista"
      }
    },
    on_click_tools_tecla(v, op){
        switch (op) {
          case "edit":
              this.titleForm = "Editar tecla";
              this.itemSel = v;
              this.showDialogo = true;
              this.tipo = "md";
              this.form = this.formTecla;
              this.tb_name = "teclas"
              break;
          case "sec":
              this.showEditSec = true;
              this.itemSel = v;
              break;
          case "add_sub":
               if (v.tipo == "SP"){
                 this.edit_tipo_tecla(v)
               }else{
                  this.add_subtecla(v)
               }
              break;
          case "rm":
              let inst = {
                  tb: this.tb_name,
                  tipo: "rm",
                  id: v.id,
              };
              let ls = this.$store.state[this.tb_name];
              this.$store.state[this.tb_name] = ls.filter((e) => {
                  return e.id != v.id;
              });
              add_instruccion(inst);
              break;
          }
      
    },
    add_subtecla(v){
      this.titleForm = "Agregar subtecla";
      this.itemSel = {tecla_id: v.id};
      this.showDialogo = true;
      this.tipo = "add";
      this.form = this.formSubTecla;
      this.tb_name = "subteclas"
    },
    edit_tipo_tecla(v){
      this.titleForm = "Editar tecla";
      this.itemSel = v;
      this.showDialogo = true;
      this.tipo = "md";
      this.form = this.formTeclaSub;
      this.tb_name = "teclas"
    },
    add_instruccion(inst){
      this.addInstruccion({ inst: inst });
      this.on_filter_change(this.localFilter);
    },
    on_close_dialogo(item){
      this.showDialogo = false;
      this.tb_name = "teclas"
    },
    on_close_edit_sec() {
      this.showEditSec = false;
      this.tb_name = "teclas"
    },
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
