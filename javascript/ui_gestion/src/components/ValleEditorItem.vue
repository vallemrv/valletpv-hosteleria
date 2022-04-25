<template>
  <v-row class="pa-5">
    <v-col col="12" class="">
      <v-toolbar color="#cfb6d4">
        <v-toolbar-title>
          {{ title }}
          <v-progress-circular
            indeterminate
            color="primary"
            v-if="ocupado"
          ></v-progress-circular
        ></v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn stacked size="small" @click="on_click_add"
          ><v-icon>mdi-newspaper-plus</v-icon></v-btn
        >
      </v-toolbar>
    </v-col>
    <v-col cols="12">
      <valle-filtros :filtro="filtro" @on_filter="on_filter"></valle-filtros>
    </v-col>
    <v-col cols="12">
      <valle-listados-tb
        :items="itemsFiltered"
        :columns="columns"
        :headers="headers"
        :tools="tools"
        @on_click_tools="on_click"
      ></valle-listados-tb>
    </v-col>
    <valle-dialogo-form
      @on_show="on_show"
      :show="showDialog"
      :title="titleDialogo"
      :item="itemSel"
      :form="form"
      tb_name="camareros"
      :tipo="tipo"
    >
    </valle-dialogo-form>
  </v-row>
</template>

<script>
import { mapActions, mapState, mapGetters } from "vuex";
import ValleListadosTb from "@/components/ValleListadosTb.vue";
import ValleFiltros from "@/components/ValleFiltros.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";

export default {
  props: ["title", "filtro", "tb_name"],
  components: { ValleListadosTb, ValleFiltros, ValleDialogoForm },
  data() {
    const col = ["nombre", "apellidos", "permisos"];
    return {
      itemsFiltered: [],
      filterLocal: null,
      columns: col,
      headers: col,

      form: [],
      itemSel: {},
      showDialog: false,

      titleDialogo: "Editar",
      tipo: "md",
      form: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "apellidos", label: "1º Apellido", tp: "text" },
        {
          col: "permisos",
          label: "Permisos",
          choices: ["borrar_mesas", "borrar_lineas"],
          tp: "multiple",
        },
      ],
      tools_activos: [
        { op: "minus", text: "Desactivar", icon: "mdi-account-minus" },
        { op: "edit", text: "Editar", icon: "mdi-account-edit" },
        {
          op: "unlock",
          text: "Desbloquear",
          icon: "mdi-lock-open",
        },
      ],
      tools_borrados: [
        { op: "activar", text: "Activar", icon: "mdi-account-arrow-left" },
        { op: "rm", text: "Borrado definitivo", icon: "mdi-delete" },
      ],
      tools: [],
    };
  },
  computed: {
    ...mapState(["ocupado"]),
    ...mapGetters(["getItemsFiltered"]),
  },
  methods: {
    ...mapActions(["getListado", "addInstruccion"]),
    on_filter(f) {
      this.filterLocal = f;
      this.itemsFiltered = this.getItemsFiltered(f, this.tb_name);
    },
    on_click_add() {
      this.titleDialogo = "Agregar";
      this.itemSel = this.$tools.newItem(this.form);
      this.showDialog = true;
      this.tipo = "add";
    },
    on_click(v, op) {
      let inst = {};
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
            tb: "camareros",
            reg: { activo: 0, autorizado: 0 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 0;
          v.autorizado = 0;
          this.addInstruccion({ inst: inst });
          this.camarerosFiltrados = this.getfilterCamareros(this.filter);
          break;
        case "activar":
          inst = {
            tb: "camareros",
            reg: { activo: 1 },
            tipo: "md",
            id: v.id,
          };
          v.activo = 1;
          this.addInstruccion({ inst: inst });
          this.camarerosFiltrados = this.getfilterCamareros(this.filter);
          break;
        case "rm":
          inst = {
            tb: "camareros",
            tipo: "rm",
            id: v.id,
          };
          let cam = this.$store.state.camareros;
          this.$store.state.camareros = cam.filter((e) => {
            return e.id != v.id;
          });
          this.addInstruccion({ inst: inst });
      }
    },
    on_show(value) {
      this.showDialog = value;
    },
    cargar_reg() {
      let params = new FormData();
      params.append("tb", "camareros");
      this.getListado({ params: params });
      this.change_tools();
    },
    change_tools() {
      if (this.filter == "borrados") {
        this.tools = this.tools_borrados;
      } else {
        this.tools = this.tools_activos;
      }
    },
  },
  watch: {
    ocupado(v) {
      if (!v) {
        this.itemsFiltered = this.getItemsFiltered(this.filterLocal, this.tb_name);
      }
    },
  },
  mounted() {
    this.filterLocal = Object.values(this.filtro);
    this.filterLocal.filters = Object.values(this.filtro.all);
    if (this.$store.state[this.tb_name]) {
      this.itemsFiltered = this.getItemsFiltered(this.filterLocal, this.tb_name);
    } else {
      this.cargar_reg();
    }
  },
};
</script>
