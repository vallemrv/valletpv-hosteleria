<template>
    <div>
      <Pie :data="data"></Pie>
    </div>
  </template>
  
<script>
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PieController,
} from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale)


  export default {
    components:{
    Pie,
    PieController
},
    props:["data"]
    
  };
  </script>
s  