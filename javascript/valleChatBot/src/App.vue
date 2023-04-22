<template>
  <v-app>
    <SQLInterface />
  </v-app>
</template>

<script>
import MainComponet from "./components/MainComponent.vue";
import SQLInterface from '@/components/SQLInterface.vue';

export default {
  name: "App",
  components: {
    MainComponet,
    SQLInterface
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

</style>
