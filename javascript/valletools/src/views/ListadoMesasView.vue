<template>
  <valle-tool-bar title="Lista de mesas"></valle-tool-bar>
  <v-container>

    <v-row>
      <v-col cols="12">
        <v-sheet
          class="mx-auto"
          max-width="600"
        >
    <v-slide-group
      show-arrows
    >
      <v-slide-group-item
        v-for="n in 25"
        :key="n"
        v-slot="{ isSelected, toggle }"
      >
        <v-btn
          class="ma-2"
          rounded
          :color="isSelected ? 'primary' : undefined"
          @click="toggle"
        >
          Options {{ n }}
        </v-btn>
      </v-slide-group-item>
    </v-slide-group>
  </v-sheet>
      </v-col>
      <v-col cols="12" v-for="(item, i) in listadomesas" :key="i">
        <v-card>
          <v-card-title
            >
            <v-row>
              <v-col cols="6">
                Mesa: {{ item.nomMesa }}
              </v-col>
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
                Regalado: {{ item.total_regalado }} Ud.</v-col
              >
              <v-col class="text-center" cols="6">
                Cobrado: {{ decimalToStr(item.total_cobrado) }}
              </v-col>
            </v-row>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn icon @click="mostrarMesa(item)"><v-icon>mdi-eye</v-icon></v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>


    <ValleInfMesa @close="showMesa = false" :showMesa="showMesa" :mesa="itemSel" :puedo_cobrar='false'/>
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
      showMesa: false,
      nomMesa: "",
      hora: "",
    };
  },
  computed: {
    ...mapState(["listadomesas", "infmesa", "ocupado"]),
  },
  methods: {
    ...mapActions(["getListadoMesas", "getInfMesa"]),
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
  },
  mounted() {
      this.getListadoMesas();
  },
};
</script>
