<template>
  <v-row v-show="!showOrdenarTeclas && !showAgregarTeclas">
    <v-col cols="12" class="pa-2">
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
        <div v-if="secSel">
          {{ secSel.nombre }}
          <v-btn icon @click="on_edit_sec"> <v-icon>mdi-pencil</v-icon></v-btn>
          <v-btn icon v-if="items.length < 18" @click="() => (showAgregarTeclas = true)">
            <v-icon>mdi-plus</v-icon></v-btn
          >
          <v-btn icon @click="() => (showOrdenarTeclas = true)">
            <v-icon>mdi-order-numeric-descending </v-icon></v-btn
          >
        </div>
      </v-toolbar>
      <v-toolbar color="#cfb6d4" v-if="itemSel">
        <v-toolbar-title></v-toolbar-title>
        <v-spacer></v-spacer>
        <v-card>
          <v-card-text>
            {{ itemSel.nombre }}
            <v-btn icon> <v-icon>mdi-pencil</v-icon></v-btn>
            <v-btn icon v-if="itemSel.tipo == 'ml'"> <v-icon>mdi-plus</v-icon></v-btn>
            <v-btn icon> <v-icon>mdi-delete</v-icon></v-btn>
          </v-card-text>
        </v-card>
      </v-toolbar>
    </v-col>

    <v-col cols="3">
      <valle-teclados cols="12" @click_tecla="on_click_sec" :items="seccionescom">
      </valle-teclados>
    </v-col>
    <v-col cols="9">
      <valle-teclados cols="4" :items="items" @click_tecla="on_click_tecla">
      </valle-teclados>
    </v-col>
  </v-row>
  <valle-form-seccom
    :item="itemSelEdit"
    title="Editar"
    tipo="md"
    :show="showSecform"
    :form="formSec"
    tb_name="seccionescom"
    @close_dialogo="on_close_dialogo"
  >
  </valle-form-seccom>

  <ordenar-teclas
    v-show="showOrdenarTeclas"
    @close="() => (showOrdenarTeclas = false)"
    @change="on_change"
    :items="items"
    tb_name="teclas"
    column="OrdenCom"
  ></ordenar-teclas>

  <agregar-teclas
    v-show="showAgregarTeclas"
    @close="() => (showAgregarTeclas = false)"
    @change="on_change"
    tb_name="teclas"
    :secSel="secSel"
  ></agregar-teclas>
</template>

<script>
import ValleFormSeccom from "@/comp_especificos/ValleFormSeccom.vue";
import ValleTeclados from "@/components/ValleTeclados.vue";
import OrdenarTeclas from "@/comp_especificos/OrdenarTeclas.vue";
import AgregarTeclas from "@/comp_especificos/AgregarTeclas.vue";
import { mapState, mapActions, mapGetters } from "vuex";

export default {
  components: { ValleTeclados, ValleFormSeccom, OrdenarTeclas, AgregarTeclas },
  data() {
    return {
      title: "Teclados comanda",
      showSecform: false,
      showOrdenarTeclas: false,
      showAgregarTeclas: false,
      localFiter: null,
      items: [],
      secSel: null,
      itemSel: null,
      itemSelEdit: null,
      formSec: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "icono", label: "Icono", tp: "select" },
        { col: "es_promocion", label: "Es promocion", tp: "switch" },
        { col: "descuento", label: "Descueto", tp: "number" },
      ],
    };
  },
  computed: {
    ...mapState(["ocupado", "seccionescom", "teclas", "subteclas"]),
    ...mapGetters(["getItemsFiltered", "getItemsOrdered"]),
  },
  methods: {
    ...mapActions(["getListadoCompuesto"]),
    on_change() {
      this.on_click_sec(this.secSel);
    },
    on_close_dialogo() {
      this.showSecform = false;
    },
    on_edit_sec() {
      this.itemSelEdit = this.secSel;
      this.showSecform = true;
    },
    on_click_sec(sec) {
      var f = {
        filters: [{ IDSeccionCom: sec.id }],
      };
      var it = this.getItemsFiltered(f, "teclas");
      this.items = this.getItemsOrdered(it, "OrdenCom");

      this.secSel = sec;
      this.itemSel = null;
    },
    on_click_tecla(t) {
      this.itemSel = t;
    },
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.subteclas || this.subteclas.length <= 0) request.push("subteclas");
      if (!this.seccionescom || this.seccionescom.length <= 0)
        request.push("seccionescom");
      this.getListadoCompuesto({ tablas: request });
    },
  },
  watch: {
    seccionescom(v) {
      if (v && v.length > 0 && this.items.length == 0) {
        this.on_click_sec(v[0]);
      } else {
        this.getTablas();
      }
    },
  },
  mounted() {
    if (
      !this.teclas ||
      this.teclas.length == 0 ||
      !this.subteclas ||
      this.subteclas.length == 0 ||
      !this.seccionescom ||
      this.seccionescom.length == 0
    ) {
      this.getTablas();
    } else this.on_click_sec(this.seccionescom[0]);
  },
};
</script>
