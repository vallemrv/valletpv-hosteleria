<template>
  <v-footer app bottom>
    <v-row class="pa-7">
      <v-col col="12" class="text-center pa-0 ma-0"> Manuel Rodriguez ValleAPP </v-col>
      <v-col class="text-center pa-0 ma0" cols="12">
        {{ new Date().getFullYear() }} — <strong>Vuetify & Vue</strong>
      </v-col>
    </v-row>
  </v-footer>
</template>

<style scoped>
.v-footer {
  width: 100%;
}
</style>
