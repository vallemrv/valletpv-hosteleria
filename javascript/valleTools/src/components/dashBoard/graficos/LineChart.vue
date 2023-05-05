<template>
    <div>
      <Bar :data="data" :obtions="options"></Bar>
    </div>
  </template>
  
<script>
import { Bar, Line } from 'vue-chartjs'
import { Chart as ChartJS, LineController, LineElement, PointElement} from 'chart.js'

ChartJS.register(LineController, PointElement, LineElement)

export default {
    components:{
    Line,
    Bar
},
    props:["data", "options"]
    
  };
  </script>
s  