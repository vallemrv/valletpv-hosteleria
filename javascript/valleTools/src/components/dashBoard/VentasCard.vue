<template>
    <v-card>
      <v-card-title class="green-text">Ventas</v-card-title>
      <v-card-text>
        <ventas-chart :data="chartData" ></ventas-chart>
        <p>Total cobrado y pedido: {{ parseFloat(totalCobradoYPedido).toFixed(2) }} €</p>
      </v-card-text>
    </v-card>
  </template>
  
<script>
import VentasChart from './graficos/PieChart.vue';
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
      interval: null,
      totalCobradoYPedido:0,
      chartData: {
          labels: [
            'Cobrado',
            'Borrado',
            'Pedido'
          ],
          datasets: [{
            label: 'Ventas',
            data: [0, 0, 0],
            backgroundColor: ['#4caf50', '#ff9800', '#f44336'],
            hoverOffset: 10
          }]
      }
    };
  },
  methods:{
    async fetchData() {
        const params = new FormData();
        params.append("user", this.empresa.token.user);
        params.append("token", this.empresa.token.token);

        const response = await axios.post(
          this.empresa.url + "/app/dashboard/get_estado_ventas",
          params
        );
        const { cobrado, pedido, borrado } = response.data;

        this.chartData = {
          labels: ["Cobrado", "Pedido", "Borrado"],
          datasets: [
            {
              data: [cobrado, pedido, borrado],
              backgroundColor: ["#4caf50", "#ff9800", "#f44336"],
            },
          ],
        };
        this.totalCobradoYPedido = cobrado + pedido;
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

    // Actualizar datos cada 10 segundos (10000 ms)
    this.interval = setInterval(this.fetchData, 10000);
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
    height: 200px;
}

</style>