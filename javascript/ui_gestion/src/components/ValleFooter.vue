<template>
    <v-footer
      app 
      bottom
      fixed 
    >
        <v-col
          class="text-center"
          cols="12"
        >
          {{ new Date().getFullYear() }} — <strong>Vuetify</strong>
        </v-col>
    </v-footer>
    
</template>

<style scoped>
    .v-footer{
      width: 100%;
    }
</style>
