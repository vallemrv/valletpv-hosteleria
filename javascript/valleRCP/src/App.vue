<template>
  <v-app>
    <v-main>
      <router-view />
      <audio src="" id="eventAudio" preload="auto"></audio>
    </v-main>
  </v-app>
</template>

<script>
export default {
  name: "App",

  data: () => ({
    //
  }),
};
</script>
