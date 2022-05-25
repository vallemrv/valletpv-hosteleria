<template>
  <v-row>
    <v-col cols="12" class="">
      <v-toolbar color="#cfb6d4">
        <v-toolbar-title>
          Visa de teclas
          <v-progress-circular
            indeterminate
            color="primary"
            v-if="ocupado"
          ></v-progress-circular
        ></v-toolbar-title>
        <v-spacer></v-spacer>
      </v-toolbar>
    </v-col>

    <v-col>
      <valle-filtros-vue
        :filtro="filtro"
        @click_filter="on_filter_change"
      ></valle-filtros-vue>
    </v-col>

    <v-col cols="12">
      <v-card>
        <v-card-text>
          <v-row class="text-center">
            <v-col cols="4" class="text-left">Texto tecla</v-col>
            <v-col cols="4">Texto ticket</v-col>
            <v-col cols="4">Texto recepcion</v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-col>

    <valle-listado-vista-tecla v-for="(tecla, i) in listTeclas" :key="i" :tecla="tecla">
    </valle-listado-vista-tecla>
  </v-row>
</template>

<script>
import { mapState, mapActions, mapGetters } from "vuex";
import ValleListadoVistaTecla from "@/comp_especificos/ValleListadoVistaTecla";
import ValleFiltrosVue from "@/components/ValleFiltros.vue";

export default {
  components: { ValleListadoVistaTecla, ValleFiltrosVue },
  data() {
    return {
      localFilter: null,
      tb_name: "teclas",
      tablas: ["teclas", "secciones", "subteclas"],
    };
  },
  methods: {
    ...mapActions(["getListadoCompuesto"]),
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.subteclas || this.subteclas.length <= 0) request.push("subteclas");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      this.getListadoCompuesto({ tablas: request });
    },
    on_filter_change(f) {
      this.localFilter = f;
    },
  },
  computed: {
    ...mapState(["ocupado", "teclas", "subteclas", "secciones"]),
    ...mapGetters(["getFilters", "getListValues", "getItemsFiltered"]),
    listTeclas() {
      return this.localFilter
        ? this.getItemsFiltered(this.localFilter, this.tb_name)
        : [];
    },
    filtro() {
      return {
        caption: this.getListValues("secciones", "nombre"),
        filters: this.getFilters("secciones", "id", ["IDSeccion", "IDSec2"]),
        all: [{ IDSeccion: -1 }],
        multiple: false,
      };
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
    if (
      !this.teclas ||
      this.teclas.length == 0 ||
      !this.subteclas ||
      this.subteclas.length == 0 ||
      !this.secciones ||
      this.secciones.length == 0
    ) {
      this.getTablas();
    }
  },
};
</script>
