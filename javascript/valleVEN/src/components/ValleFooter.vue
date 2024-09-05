<template>
  <v-footer app bottom>
    <v-row class="pa-7">
      <v-col col="12" class="text-center pa-0 ma-0"> Manuel Rodriguez ValleAPP </v-col>
      <v-col class="text-center pa-0 ma0" cols="12">
        {{ new Date().getFullYear() }} — <strong>Vuetify & Vue</strong>
      </v-col>
      
      <v-col  v-if="!isWSConnected">
        <v-alert color="warning">No hay conexion. Reconectando....
        <v-progress-circular class="float-right" color="primary"
        indeterminate
        ></v-progress-circular>
        </v-alert>
      </v-col>
    </v-row>
  
  </v-footer>
</template>

<script>
import { mapState } from "vuex";
export default {
  computed:{
    ...mapState(["isWSConnected"]),
  }
}
</script>


<style scoped>
.v-footer {
  width: 100%;
}
</style>
