<template>
    <v-card>
      <v-card-title>Los mas vendidos</v-card-title>
      <v-card-text>
        <line-chart v-if="!error" :data="chartData" :options="chartOptions"></line-chart>
        <v-alert v-else dense outlined type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
    </v-card>
  </template>
  
  <script>
  import  LineChart from './graficos/LineChart.vue';
  import { DashBoard } from '@/stores/dashBoard';
 
  
  export default {
    components: {
      LineChart
    },
    data() {
      return {
        error: null,
        chartData: {
          labels: ["Articulos vendidos"], // Aquí añadirás los nombres de los artículos
          datasets: [
            {
              label: 'Artículos vendidos',
              data: [], // Aquí añadirás las cantidades vendidas
              borderColor: '#3f51b5',
              fill: false,
            },
          ],
        },
        chartOptions: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
            },
          },
        },
      };
    },
    async mounted() {
      // Obtén los datos del endpoint y actualiza el gráfico
      try {
        const store = DashBoard();
        const {data, error} = await store.getVentasByArticulo();
        if (error) {
          this.error = error;
          return;
        }

        var datasets = []
        var labels = []
        for (const item of data) {
          labels.push(item.nombre);
          datasets.push(item.can);
        }
        

        this.chartData = {
          labels: labels,
          datasets:[{
             label: "Los " +datasets.length + " mas vendidos.",
             data: datasets,
             borderColor: '#3f51b5',
             fill: false,
        }]

        }

      } catch (error) {
        console.error(error);
      }
    },
  };
  </script>
  
  <style scoped>
  .v-card {
    width: 100%;
    min-height: 400px;
  }
  .v-card-text {
    height: 100%;
    position: relative;
  }
  canvas {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    right: 0;
  }
  </style>
  