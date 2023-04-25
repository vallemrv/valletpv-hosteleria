<template>
    <Bar
      id="bar_chart"
      :options="options"
      :data="data"
    />
  </template>
  
  <script>
  import { Bar } from 'vue-chartjs'
  import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'
  
  ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)
  
  export default {
    props: ["options", "data"],
    components: { Bar },
  }
  </script>