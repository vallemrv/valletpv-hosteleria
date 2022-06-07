<template>

  <ValleHeader :title="title" anchor="bottom end"/>
  <v-container>
  <v-row v-show="!showOrdenarTeclas && !showAgregarTeclas">
    <v-col cols="12" class="pa-2 mb-5">
      <v-toolbar class="mb-2" color="#cfb6d4">
        <v-toolbar-title v-if="secSel">
         {{ secSel.nombre }}
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <div v-if="secSel">
          
          <v-btn icon @click="editar_sec"> <v-icon>mdi-pencil</v-icon></v-btn>
          <v-btn icon v-if="items.length < 18" @click="() => (showAgregarTeclas = true)">
            <v-icon>mdi-plus</v-icon></v-btn
          >
          <v-btn v-if="click <= 1" icon @click="() => (showOrdenarTeclas = true)">
            <v-icon>mdi-order-numeric-descending </v-icon></v-btn
          >
        </div>
      </v-toolbar>
      <v-row>
        <v-col cols="12" sm="6" v-if="itemSel">
          <v-card :color="$tools.rgbToHex(itemSel.RGB)">
            <v-card-text>
              {{ itemSel.nombre }}
              <v-btn class="float-right" icon variant="text" @click="editar_tecla">
                <v-icon>mdi-pencil</v-icon></v-btn
              >
              <v-btn
                icon
                class="float-right"
                variant="text"
                @click="agregar_subteclas"
                v-if="itemSel.tipo == 'CM' && items.length < 18"
              >
                <v-icon>mdi-plus</v-icon></v-btn
              >
              <v-btn
                class="float-right"
                variant="text"
                icon
                @click="() => (showEditSec = true)"
              >
                <v-icon>mdi-table-edit</v-icon></v-btn
              >
              <v-btn class="float-right" variant="text" icon @click="quitar_tecla">
                <v-icon>mdi-delete</v-icon></v-btn
              >
              <div class="clearfix"></div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" v-if="subItemSel">
          <v-card elevation="2">
            <v-card-text>
              {{ itemSel.nombre + " " + itemSel.p1 }}
              <v-btn class="float-right" icon variant="text" @click="editar_subtecla">
                <v-icon>mdi-pencil</v-icon></v-btn
              >
              <v-btn class="float-right" icon variant="text" @click="quitar_subtecla">
                <v-icon>mdi-delete</v-icon></v-btn
              >
              <div class="clearfix"></div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-col>

    <v-col cols="12" sm="5">
      <valle-teclados
        cols="6"
        :is_collapsible="is_collapsible"
        @click_tecla="on_click_sec"
        :items="secciones"
      >
      </valle-teclados>
    </v-col>
    <v-col cols="12" sm="7">
      <valle-teclados cols="4" :items="items" @click_tecla="on_click_tecla">
      </valle-teclados>
    </v-col>
  </v-row>

  <valle-dialogo-form
    :item="itemSelEdit"
    :title="titleForm"
    :tipo="tipo"
    :show="showForm"
    :form="form"
    :tb_name="selTbName"
    @close="on_form_close"
  >
  </valle-dialogo-form>

  <ordenar-teclas
    v-show="showOrdenarTeclas"
    @close="() => (showOrdenarTeclas = false)"
    @change="on_change"
    :items="items"
    tb_name="teclas"
    column="orden"
  ></ordenar-teclas>

  <agregar-teclas
    v-show="showAgregarTeclas"
    @close="() => (showAgregarTeclas = false)"
    @change="on_change"
    tb_name="teclas"
    tb_mod="teclaseccion"
    col="tecla"
    col_parent="IDSeccion"
    :item="itemAddTecla"
    :filter="['tecla']"
  ></agregar-teclas>

  <valle-secciones-tecla-vue
    :item="itemSel"
    :show="showEditSec"
    @close_edit_sec="() => (showEditSec = false)"
  ></valle-secciones-tecla-vue>

  </v-container>
</template>

<script>
import ValleHeader from "@/components/ValleHeader.vue";
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";
import ValleTeclados from "@/components/ValleTeclados.vue";
import OrdenarTeclas from "@/comp_especificos/OrdenarTeclas.vue";
import AgregarTeclas from "@/comp_especificos/AgregarTeclas.vue";
import ValleSeccionesTeclaVue from "@/comp_especificos/ValleSeccionesTecla.vue";
import { mapState, mapActions, mapGetters } from "vuex";

export default {
  components: {
    ValleTeclados,
    ValleSeccionesTeclaVue,
    ValleDialogoForm,
    OrdenarTeclas,
    AgregarTeclas,
    ValleHeader,
  },
  data() {
    return {
      title: "Teclados TPV",
      titleForm: "Editar",
      tipo: "md",
      click: 0,
      showForm: false,
      showOrdenarTeclas: false,
      showAgregarTeclas: false,
      showEditSec: false,
      localFiter: null,
      is_collapsible: ["xs"],
      items: [],
      secSel: null,
      itemSel: null,
      subItemSel: null,
      itemSelEdit: null,
      selTbName: "",
      form: [],
      formTecla: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "descripcion_t", label: "Texto ticket", tp: "text" },
        { col: "descripcion_r", label: "Texto recepcion", tp: "text" },
        { col: "incremento", label: "Incremento", tp: "number", default: 0.0 },
      ],
      formGR: [
        {
          col: "tipo",
          label: "Tipo",
          tp: "select",
          keys: ["SP", "CM"],
          choices: ["SIMPLE", "COMPUESTA"],
        },
      ],
    };
  },
  computed: {
    ...mapState(["ocupado", "secciones", "teclas", "subteclas"]),
    ...mapGetters(["getItemsFiltered", "getItemsOrdered", "getListValues"]),
    itemAddTecla() {
      return {
        orden: 0,
        seccion: this.secSel ? this.secSel.id : null,
      };
    },
    formSec() {
      return [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "orden", label: "Orden", tp: "number" },
      ];
    },
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    on_form_close() {
      this.titleForm = "Editar";
      this.tipo = "md";
      this.showForm = false;
    },
    agregar_subteclas() {
      this.newSubTecla = this.$tools.newItem(this.formTecla);
      this.newSubTecla.tecla_id = this.itemSel.id;
      this.itemSelEdit = this.newSubTecla;
      this.titleForm = "Agregar subtecla";
      this.tipo = "add";
      this.showForm = true;
      this.selTbName = "subteclas";
      this.form = this.formTecla;
    },
    editar_tecla() {
      this.itemSelEdit = this.itemSel;
      this.titleForm = "Editar tecla";
      this.showForm = true;
      this.selTbName = "teclas";
      this.form = this.formTecla;
    },
    editar_sec() {
      this.itemSelEdit = this.secSel;
      this.showForm = true;
      this.titleForm = "Editar seccion";
      this.selTbName = "secciones";
      this.form = this.formSec;
    },
    editar_subtecla() {
      this.itemSelEdit = this.subItemSel;
      this.showForm = true;
      this.titleForm = "Editar subtecla";
      this.selTbName = "subteclas";
      this.form = this.formTecla;
    },
    quitar_subtecla() {
      if (this.itemSel) {
        var inst = {
          tb: "subteclas",
          tipo: "rm",
          id: this.subItemSel.id,
        };
        this.subItemSel.tecla = -1;
        this.subItemSel = null;
        this.addInstruccion({ inst: inst });
        this.items = this.getItemsFiltered(
          { filters: [{ tecla: this.itemSel.id }] },
          "subteclas"
        );
      }
    },
    quitar_tecla() {
      if (this.itemSel) {
        var inst = {
          tb: "teclaseccion",
          tipo: "rm",
          filter: { tecla__pk: this.itemSel.id },
          id: this.itemSel.id,
        };
        this.itemSel.IDSeccionCom = -1;
        this.subItemSel = null;
        this.itemSel = null;
        this.addInstruccion({ inst: inst });
        this.on_change();
      }
    },
    on_change() {
      this.on_click_sec(this.secSel);
    },
    on_click_sec(sec) {
      var f = {
        filters: [{ IDSeccion: sec.id }, { IDSec2: sec.id }],
      };
      var it = this.getItemsFiltered(f, "teclas");
      this.items = this.getItemsOrdered(it, "orden");

      this.secSel = sec;
      this.itemSel = null;
      this.subItemSel = null;
    },
    on_click_tecla(t) {
      if (t.IDSeccion) {
        this.itemSel = t;
        this.click++;
        if (t.tipo == "CM" && this.click == 2) {
          this.items = this.getItemsFiltered({ filters: [{ tecla: t.id }] }, "subteclas");
        } else if (t.tipo != "CM" && this.click == 2) {
          this.itemSelEdit = this.itemSel;
          this.titleForm = "Editar tecla";
          this.showForm = true;
          this.selTbName = "teclas";
          this.form = this.formGR;
        } else if (this.click > 2) this.click = 0;
      } else {
        this.subItemSel = t;
      }
    },
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.subteclas || this.subteclas.length <= 0) request.push("subteclas");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      this.getListadoCompuesto({ tablas: request });
    },
  },
  watch: {
    secciones(v) {
      if (v && v.length > 0 && this.items.length == 0) {
        this.on_click_sec(v[0]);
      } else {
        this.getTablas();
      }
    },
    teclas(v) {
      if (!v) {
        this.getTablas();
      } else {
        if (this.secSel) this.on_click_sec(this.secSel);
        else this.on_click_sec(v[0]);
      }
    },
    subteclas(v) {
      if (!v) {
        this.getTablas();
      } else if (this.itemSel) {
        this.items = this.getItemsFiltered(
          { filters: [{ tecla: this.itemSel.id }] },
          "subteclas"
        );
      }
    },
    itemSel(v) {
      this.click = 0;
    },
  },
  mounted() {
    if (
      !this.teclas ||
      this.teclas.length == 0 ||
      !this.subteclas ||
      this.subteclas.length == 0 ||
      !this.secciones ||
      this.secciones.length == 0
    ) {
      this.getTablas();
    } else {
      this.on_click_sec(this.secciones[0]);
    }
  },
};
</script>

<style>
.clearfix {
  clear: both;
}
</style>
