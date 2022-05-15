<template>
  <v-row>
    <v-col cols="12">
      <v-toolbar color="#cfb6d4">
        <v-toolbar-title>Tecla por familias</v-toolbar-title>
      </v-toolbar>
    </v-col>
    <v-col cols="12">
      <v-card class="mt-5">
        <v-card-title> Familias </v-card-title>
        <v-card-text>
          <valle-select-vue
            :items="choices"
            :values="values"
            v-model="idfamilia"
          ></valle-select-vue>
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="6" v-for="(item, i) in items" :key="i">
      <v-card>
        <v-card-text>
          <v-row>
            <v-col cols="8" v-if="item.descripcion_r">{{ item.descripcion_r }}</v-col>
            <v-col cols="8" v-else>{{ item.nombre }}</v-col>
            <v-col cols="4"
              ><v-btn icon><v-icon @click="edit(item)">mdi-pencil</v-icon></v-btn></v-col
            >
          </v-row>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
  <valle-dialogo-form-vue
    @close="() => (showDialogo = false)"
    :show="showDialogo"
    :form="form"
    :item="itemSel"
    title="Editar familia"
    tipo="md"
    tb_name="teclas"
  ></valle-dialogo-form-vue>
</template>

<script>
import ValleSelectVue from "@/components/ValleSelect.vue";
import ValleDialogoFormVue from "@/components/ValleDialogoForm.vue";
import { mapGetters, mapActions, mapState } from "vuex";

export default {
  components: { ValleSelectVue, ValleDialogoFormVue },
  data() {
    return {
      idfamilia: null,
      showDialogo: false,
      itemSel: null,
    };
  },
  computed: {
    ...mapState(["teclas", "familias"]),
    ...mapGetters(["getListValues"]),
    form() {
      return [
        { col: "nombre", tp: "text" },
        {
          col: "descripcion_r",
          tp: "text",
        },
        {
          col: "familia",
          tp: "select",
          choices: this.choices,
          keys: this.values,
        },
      ];
    },
    choices() {
      return this.getListValues("familias", "nombre");
    },
    values() {
      return this.getListValues("familias", "id");
    },
    items() {
      if (this.idfamilia != null && this.teclas) {
        return Object.values(this.teclas).filter((e) => {
          return e.familia == this.idfamilia;
        });
      } else {
        this.getListadoCompuesto({ tablas: ["teclas"] });
        return [];
      }
    },
  },
  methods: {
    ...mapActions(["getListadoCompuesto"]),
    edit(item) {
      this.itemSel = item;
      this.showDialogo = true;
    },
  },
  mounted() {
    this.getListadoCompuesto({ tablas: ["teclas", "familias"] });
  },
};
</script>
