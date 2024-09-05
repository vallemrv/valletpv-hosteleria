<template>
  <valle-header></valle-header>
  <v-container class="mt-4">
    <v-alert v-if="last_accion != ''" color="info">{{ last_accion }}</v-alert>

    <ReactiveChart class="mt-3" :chartData="chartSet" />

    <v-row class="mt-3">
      <v-col cols="6">
        <v-alert class="text-center" elevation="2">
          Mesas: {{ mesasabiertas.length }} abiertas</v-alert
        >
      </v-col>
      <v-col cols="6">
        <v-alert class="text-center" elevation="2">Total: {{ strTotal }}</v-alert>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import ValleHeader from "@/components/ValleHeader.vue";
import ReactiveChart from "@/components/ReactiveChart.vue";
import { mapState } from "vuex";
export default {
  components: { ValleHeader, ReactiveChart },
  computed: {
    ...mapState(["chartSet", "last_accion", "mesasabiertas", "total"]),
    strTotal() {
      return parseFloat(this.total).toFixed(2) + " €";
    },
  },
  
};
</script>
