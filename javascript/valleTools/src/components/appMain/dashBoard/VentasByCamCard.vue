<template>
    <v-card>
      <v-card-title>
        Ventas por Camarero
      </v-card-title>
      <v-card-text>
        <BarChart 
         :data="camareroVentas" :options="options"/>
      </v-card-text>
    </v-card>
  </template>
  
<script>
import axios from "axios";
import BarChart from "./graficos/BarChart.vue";

export default {
  props:["empresa"],
  components: {
    BarChart,
  },
  methods:{
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
    }
  },
  data() {
    return {
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
  async mounted() {
    const fetchData = async () => {
        try {
          const params = new FormData();
          params.append("user", this.empresa.token.user);
          params.append("token", this.empresa.token.token);

          const response = await axios.post(
              this.empresa.url + "/app/dashboard/get_estado_ventas_by_cam",
              params
          );
        
        
          const datasets = response.data.map((element, i) => ({
              data: [element.total_vendido],
              label: [element.nombre],
              backgroundColor: (i < this.camareroVentas.datasets.length 
                      && this.camareroVentas.datasets[i].label[0] == element.nombre ) ? 
                      this.camareroVentas.datasets[i].backgroundColor :[this.randomHexColor()]
          
              }))

            this.camareroVentas = {
                labels: ["Ventas por camarero"],
                datasets: datasets,
            };
        
        } catch (error) {
          console.error(error);
        }
    };

    // Llamar a fetchData inmediatamente al montar el componente
    fetchData();

    // Actualizar datos cada 3 segundos (3000 ms)
    setInterval(fetchData, 20000);
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