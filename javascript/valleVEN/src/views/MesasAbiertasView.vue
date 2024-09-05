<template>
  <valle-tool-bar :title="mesasabiertas.length + '  Mesas abiertas '"></valle-tool-bar>
  <v-container>
    <v-row>
      <v-col cols="12" v-for="(item, i) in mesasabiertas" :key="i">
        <v-card>
          <v-card-title>
            <v-row>
              <v-col cols="6"> Mesa: {{ item.nomMesa }}</v-col>
              <v-col cols="6" class="text-right"> Hora: {{ item.hora }}</v-col>
            </v-row>  
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
                Regalado: {{ item.total_regalado }} Ud.</v-col>
              <v-col class="text-center" cols="6">
                Cobrado: {{ decimalToStr(item.total_cobrado) }}
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn icon @click="borrarMesa(item)" class="float-right">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
            <v-btn icon @click="mostrarMesa(item)"><v-icon>mdi-eye</v-icon></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    

    <v-dialog v-model="showBorrarMesa">
      <v-card color="warning">
        <v-card-title> Atencion!! </v-card-title>
        <v-card-text>
          Esta apunto de borrar la mesa {{ nomMesa }} completa ¿esta seguro?</v-card-text
        >
        <v-card-actions>
          <v-btn @click="ejecutarBorrar()" color="primary">Aceptar</v-btn>
          <v-btn @click="showBorrarMesa = false">Cancelar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <ValleInfMesa @close="showMesa = false" :showMesa="showMesa" :mesa="itemSel" puedo_cobrar='true'/>
  </v-container>
</template>

<script>
import ValleToolBar from "@/components/ValleToolBar.vue";
import ValleInfMesa from "@/components/ValleInfMesa.vue";
import { mapState, mapActions } from "vuex";
export default {
  components: { ValleToolBar, ValleInfMesa },
  data() {
    return {
      itemSel: null,
      showBorrarMesa: false,
      showMesa: false,
      nomMesa: "",
      hora: "",
      showCobrarMesa: false,
    };
  },
  computed: {
    ...mapState(["mesasabiertas", "infmesa", "ocupado"]),
  },
  methods: {
    ...mapActions(["getListado", "getInfMesa", "rmMesa", "sendCobrarMesa"]),
    decimalToStr(v) {
      return parseFloat(v).toFixed(2) + " €";
    },
    mostrarMesa(m) {
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
    }
  },
  mounted() {
      this.getListado("mesasabiertas");
  },
};
</script>
