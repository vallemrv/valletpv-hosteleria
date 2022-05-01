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
          <v-btn icon @click="editar_sec"> <v-icon>mdi-pencil</v-icon></v-btn>
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
        <v-card class="mr-2" :color="$tools.rgbToHex(itemSel.RGB)">
          <v-card-text>
            {{ itemSel.nombre }}
            <v-btn icon @click="editar_tecla"> <v-icon>mdi-pencil</v-icon></v-btn>
            <v-btn icon @click="quitar_tecla"> <v-icon>mdi-delete</v-icon></v-btn>
          </v-card-text>
        </v-card>
      </v-toolbar>
    </v-col>

    <v-col cols="12" sm="5">
      <valle-teclados cols="6" @click_tecla="on_click_sec" :items="secciones">
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
    tb_name="teclaseccion"
    field_parent="seccion__id__secciones"
    field_item="IDSeccion"
    :secSel="secSel"
  ></agregar-teclas>
</template>

<script>
import ValleDialogoForm from "@/components/ValleDialogoForm.vue";
import ValleTeclados from "@/components/ValleTeclados.vue";
import OrdenarTeclas from "@/comp_especificos/OrdenarTeclas.vue";
import AgregarTeclas from "@/comp_especificos/AgregarTeclas.vue";
import { mapState, mapActions, mapGetters } from "vuex";

export default {
  components: { ValleTeclados, ValleDialogoForm, OrdenarTeclas, AgregarTeclas },
  data() {
    return {
      title: "Teclados TPV",
      titleForm: "Editar",
      tipo: "md",
      showForm: false,
      showOrdenarTeclas: false,
      showAgregarTeclas: false,
      localFiter: null,
      items: [],
      secSel: null,
      itemSel: null,
      itemSelEdit: null,
      selTbName: "",
      form: [],
      formTecla: [
        { col: "nombre", label: "Nombre", tp: "text" },
        { col: "tag", label: "Tag de busqueda", tp: "text" },
      ],
      formSec: [{ col: "nombre", label: "Nombre", tp: "text" }],
    };
  },
  computed: {
    ...mapState(["ocupado", "secciones", "teclas"]),
    ...mapGetters(["getItemsFiltered", "getItemsOrdered", "getListValues"]),
  },
  methods: {
    ...mapActions(["getListadoCompuesto", "addInstruccion"]),
    on_form_close() {
      this.titleForm = "Editar";
      this.tipo = "md";
      this.showForm = false;
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
    quitar_tecla() {
      if (this.itemSel) {
        var inst = {
          tb: "teclaseccion",
          tipo: "rm",
          filter: { tecla__pk: this.itemSel.id },
          id: this.itemSel.id,
        };
        if (this.itemSel.IDSeccion == this.secSel.id) this.itemSel.IDSeccion = -1;
        else this.itemSel.IDSec2 = -1;
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
    },
    on_click_tecla(t) {
      this.itemSel = t;
    },
    getTablas() {
      var request = [];
      if (!this.teclas || this.teclas.length <= 0) request.push("teclas");
      if (!this.secciones || this.secciones.length <= 0) request.push("secciones");
      this.getListadoCompuesto({ tablas: request });
    },
  },
  watch: {
    secciones(v) {
      if (v && v.length > 0 && this.items.length == 0) {
        var params = this.$route.params;
        if (params) this.on_click_sec(v[params.id]);
        else this.on_click_sec(v[0]);
      } else {
        this.getTablas();
      }
    },
    teclas(v) {
      if (!v) {
        this.getTablas();
      }
    },
  },
  mounted() {
    if (
      !this.teclas ||
      this.teclas.length == 0 ||
      !this.secciones ||
      this.secciones.length == 0
    ) {
      this.getTablas();
    } else {
      var params = this.$route.params;
      if (params) this.on_click_sec(this.secciones[params.id]);
      else this.on_click_sec(this.secciones[0]);
    }
  },
};
</script>
