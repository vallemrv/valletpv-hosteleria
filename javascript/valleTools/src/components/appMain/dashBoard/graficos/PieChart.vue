<template>
    <div>
      <Pie :data="data"></Pie>
    </div>
  </template>
  
<script>
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  PieController,
  ArcElement,
  CategoryScale,
  LinearScale,
  Title,
  Tooltip,
} from 'chart.js'

ChartJS.register(PieController, ArcElement, CategoryScale, LinearScale, Title, Tooltip)


  export default {
    components:{
    Pie,
    PieController
},
    props:["data"]
    
  };
  </script>
s  