<template>
  <v-app>
    <router-view />
  </v-app>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>
.v-application {
  height: 100% !important;
  /* Dejamos 100% (para que herede del body/html) */
  overflow: hidden !important;
}
</style>