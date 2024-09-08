<template>
    <v-card>
      <v-card-title>
        Ventas por Camarero
      </v-card-title>
      <v-card-text>
        <BarChart v-if="!error"
         :data="camareroVentas" :options="options"/>
        <v-alert  v-else dense outlined type="error">
          {{error}}
        </v-alert>
      </v-card-text>
    </v-card>
  </template>
  
<script>
import { DashBoard } from "@/stores/dashBoard";
import BarChart from "./graficos/BarChart.vue";

export default {
  props:["empresa"],
  components: {
    BarChart,
  },
 
  data() {
    return {
      error: null,
      interval:null,
      camareroVentas: {
        labels: [],
        datasets:[],
       },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: {
             duration: 0
        }
      }
    };
  },
  methods: {
    randomHexColor() {
        const getRandomInt = (min, max) => {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };
        const toHex = (num) => {
        const hex = num.toString(16);
        return hex.length === 1 ? '0' + hex : hex;
    };

    const r = getRandomInt(0, 255);
    const g = getRandomInt(0, 255);
    const b = getRandomInt(0, 255);

    return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
    },
    async fetchData() {
      try {
        const store = DashBoard();
        const {data, error} = await store.getVentasByCam();
         if (error) {
          this.error = error;
          return;
        }
        const datasets = data.map((element, i) => ({
          data: [element.total_vendido],
          label: [element.nombre],
          backgroundColor:
            i < this.camareroVentas.datasets.length &&
            this.camareroVentas.datasets[i].label[0] == element.nombre
              ? this.camareroVentas.datasets[i].backgroundColor
              : [this.randomHexColor()],
        }));

        this.camareroVentas = {
          labels: ["Ventas por camarero"],
          datasets: datasets,
        };
      } catch (error) {
        console.error(error);
      }
    },
    destroyInterval() {
      if (this.interval) {
        clearInterval(this.interval);
        this.interval = null;
      }
    },
  },
  async mounted() {
    await this.fetchData();
    // Actualizar datos cada 20 segundos (20000 ms)
    //this.interval = setInterval(this.fetchData, 20000);
  },
  beforeDestroy() {
    this.destroyInterval();
  },
};
</script>
  
<style scoped>
canvas{
    margin: auto;
    width: 100%;
    height: 290px;
}

</style>