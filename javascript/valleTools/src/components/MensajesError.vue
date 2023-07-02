<template>
    <v-alert 
      type="error" 
      class="text-h5"
    >
      {{ errorMessage }}
    </v-alert>
  </template>
  
  <script>
  export default {
    props: {
      errorMessage: {
        type: String,
        required: false,
        default: ''
      }
    },
  }
  </script>
  