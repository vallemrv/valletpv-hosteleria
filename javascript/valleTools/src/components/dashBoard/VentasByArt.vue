<template>
    <v-card>
      <v-card-title>Los mas vendidos</v-card-title>
      <v-card-text>
        <line-chart :data="chartData" :options="chartOptions"></line-chart>
      </v-card-text>
    </v-card>
  </template>
  
  <script>
  import  LineChart from './graficos/LineChart.vue';
  import axios from 'axios';
  
  export default {
    components: {
      LineChart
    },
    props:[
        'empresa'
    ],
    data() {
      return {
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
        const url = this.empresa.url;
        const params = new FormData()
        params.append('token', this.empresa.token.token)
        params.append('user', this.empresa.token.user)
        const response = await axios.post(url+'/app/dashboard/articulos_vendidos/', params);
        const data = await response.data;
        
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
  