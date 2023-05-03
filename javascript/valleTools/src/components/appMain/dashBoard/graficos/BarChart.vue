<template>
    <Bar
      id="bar_chart"
      :options="options"
      :data="data"
    />
  </template>
  
  <script>
  import { Bar } from 'vue-chartjs'
  import { Chart as ChartJS, BarElement, Tooltip, Legend, Title} from 'chart.js'
  
  ChartJS.register(BarElement, Tooltip, Legend, Title)
  
  export default {
    props: ["options", "data"],
    components: { Bar },
  }
  </script>