<template>
    <v-card>
      <v-card-title class="green--text">Ventas</v-card-title>
      <v-card-text>
        <ventas-chart :data="chartData" :options="options"></ventas-chart>
        <p>Total cobrado y pedido: {{ parseFloat(totalCobradoYPedido).toFixed(2) }} €</p>
      </v-card-text>
    </v-card>
  </template>
  
<script>
import VentasChart from './graficos/VentasChart.vue';
import axios from 'axios';

export default {
  props:{
    empresa: Object
  },
  components: {
    VentasChart,
  },
  data() {
    return {
      totalCobradoYPedido:0,
      chartData: {
        labels: ['Cobrado', 'Pedido', 'Borrado'],
        datasets: [
          {
            data: [0,0,0],
            backgroundColor: ['#4caf50', '#ff9800', '#f44336'],
          },
        ],
        },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    };
  },
  async mounted() {
    const params = new FormData()
    params.append("user", this.empresa.token.user)
    
    params.append("token", this.empresa.token.token)

    const response = await axios.post(this.empresa.url+'/app/ventas/get_estado_ventas', params);
    const { cobrado, pedido, borrado } = response.data;

    this.chartData = {
      labels: ['Cobrado', 'Pedido', 'Borrado'],
      datasets: [
        {
          data: [cobrado, pedido, borrado],
          backgroundColor: ['#4caf50', '#ff9800', '#f44336'],
        },
      ],
    };
    this.totalCobradoYPedido = cobrado + pedido;
  },
};
</script>
  