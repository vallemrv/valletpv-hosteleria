<template>
    <div>
      <h3>{{ item.title }}</h3>
      <p>{{ item.description }}</p>
    </div>
  </template>
  
  <script>
  export default {
    props: {
      item: {
        type: Object,
        required: true
      }
    }
  };
  </script>
  
  <style scoped>
  h3 {
    margin: 0;
    font-size: 1.2em;
  }
  
  p {
    margin: 0.2em 0;
    font-size: 1em;
  }
  </style>
  