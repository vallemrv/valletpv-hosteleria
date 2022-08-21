<template>
  <valle-tool-bar :title="mesasabiertas.length + '  Mesas abiertas '"></valle-tool-bar>
  <v-container>
    <v-row>
      <v-col cols="12" v-for="(item, i) in mesasabiertas" key="i">
        <v-card>
          <v-card-title
            >Mesa: {{ item.nomMesa }}
            <div class="text-right w-75">Hora: {{ item.hora }}</div>
          </v-card-title>
          <v-card-text>
            <v-row>
              <v-col class="text-center" cols="6">
                Pedido: {{ decimalToStr(item.total_pedido) }}
              </v-col>
              <v-col class="text-center" cols="6">
                Borrado: {{ decimalToStr(item.total_anulado) }}
              </v-col>
              <v-col class="text-center" cols="6">
                Regalado: {{ item.total_regalado }} Ud.</v-col
              >
              <v-col class="text-center" cols="6">
                Cobrado: {{ decimalToStr(item.total_cobrado) }}
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn icon @click="borrarMesa(item)" class="float-right"
              ><v-icon>mdi-delete</v-icon></v-btn
            >
            <v-btn icon @click="mostrarMesa(item)"><v-icon>mdi-eye</v-icon></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-dialog v-model="showMesa" fullscreen>
      <v-card>
        <v-card-title class="bg-pink-lighten-3">
          <v-row>
            <v-col cols="6"> Mesa: {{ nomMesa }} </v-col>
            <v-col cols="6" class="text-right"> Hora: {{ hora }} </v-col>
          </v-row>
        </v-card-title>

        <v-card-text>
          <div class="text-center" v-show="showProgress">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="showMesa = false">Cerrar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showBorrarMesa">
      <v-card color="warning">
        <v-card-title> Atencion!! </v-card-title>
        <v-card-text>
          Esta apunto de borrar la mesa {{ nomMesa }} completa ¿ esta seguro
          ?</v-card-text
        >
        <v-card-actions>
          <v-btn @click="ejecutarBorrar()" color="primary">Aceptar</v-btn>
          <v-btn @click="showBorrarMesa = false">Cancelar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import ValleToolBar from "@/components/ValleToolBar.vue";
import { mapState, mapActions } from "vuex";
export default {
  components: { ValleToolBar },
  data() {
    return {
      itemSel: null,
      showBorrarMesa: false,
      showMesa: false,
      nomMesa: "",
      hora: "",
      showProgress: true,
    };
  },
  computed: {
    ...mapState(["mesasabiertas", "infmesa"]),
  },
  methods: {
    ...mapActions(["getListado", "getInfMesa", "rmMesa"]),
    decimalToStr(v) {
      return parseFloat(v).toFixed(2) + " €";
    },
    mostrarMesa(m) {
      this.showProgress = true;
      this.showMesa = true;
      this.nomMesa = m.nomMesa;
      this.hora = m.hora;
      this.itemSel = m;
      this.getInfMesa(this.itemSel.PK);
    },
    borrarMesa(m) {
      this.showBorrarMesa = true;
      this.nomMesa = m.nomMesa;
      this.itemSel = m;
    },
    ejecutarBorrar() {
      this.showBorrarMesa = false;
      if (this.itemSel) this.rmMesa(this.itemSel.ID);
    },
  },
  watch: {
    mesasabiertas(v) {
      if (!v) {
        this.getListado("mesasabiertas");
      }
    },
    infmesa(v) {
      if (v) {
        this.showProgress = false;
      }
    },
  },
  mounted() {
    if (!this.mesasabiertas || this.mesasabiertas.length == 0) {
      this.getListado("mesasabiertas");
    }
  },
};
</script>
