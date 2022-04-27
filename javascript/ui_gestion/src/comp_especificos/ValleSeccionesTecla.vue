<template>
  <v-dialog v-model="show">
    <v-card title="Editar secciones" width="450px">
      <v-card-text>
        <v-select :items="itemsSecciones" label="Seccion principal" v-model="main_sec">
        </v-select>

        <v-select
          :items="itemsSecciones"
          label="Seccion secundaria"
          v-model="secundary_sec"
        ></v-select>
      </v-card-text>
      <v-card-actions>
        <v-btn @click="modificar">Modificar</v-btn>
        <v-btn @click="close">Cancelar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { mapState, mapGetters, mapActions } from "vuex";
export default {
  props: ["item", "show"],
  data() {
    return {
      main_sec: "",
      secundary_sec: "",
    };
  },
  computed: {
    ...mapGetters(["getItemById", "getListValues"]),
    ...mapState(["secciones"]),
    itemsSecciones() {
      return this.secciones ? this.getListValues("secciones", "nombre") : [];
    },
  },
  methods: {
    ...mapActions(["modificarSecciones"]),
    modificar() {
      this.item.main_sec = this.main_sec;
      this.item.secundary_sec = this.secundary_sec;
      this.modificarSecciones({ item: this.item });
      this.close();
    },
    close() {
      this.$emit("close_edit_sec");
    },
  },
  watch: {
    item(v) {
      if (v) {
        if (v.IDSeccion > 0) {
          this.main_sec = this.getItemById("secciones", v.IDSeccion).nombre;
        } else {
          this.main_sec = "";
        }

        if (v.IDSec2 > 0) {
          this.secundary_sec = this.getItemById("secciones", v.IDSec2).nombre;
        } else {
          this.secundary_sec = "";
        }
      }
    },
  },
};
</script>
