<template>
    <div>
      <canvas ref="chart"></canvas>
    </div>
  </template>
  
  <script>
  import { Doughnut } from 'vue-chartjs';
   
  export default {
    extends: Doughnut,
    mixins: [],
    props: {
      
      options: {
        type: Object,
        default: () => ({}),
      },
    },
    mounted() {
     
    },
  };
  </script>
s  