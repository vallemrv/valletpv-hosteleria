<template>
  <v-app-bar app>
    <router-link to="/">
      <v-toolbar-title> ValleTPV </v-toolbar-title>
    </router-link>
    <v-spacer></v-spacer>
    <v-menu v-model="menu" anchor="start" bottom>
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>

      <v-list>
        <v-list-item v-for="(item, index) in items" :key="index" @click="showData(item)">
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>
</template>

<script>
import router from "@/router";
export default {
  data: () => ({
    items: [
      { title: "Gestion", link: "gestion" },
      { title: "Ventas", link: "ventas" },
    ],
    menu: false,
  }),
  methods: {
    showData: function (item) {
      router.push(item.link);
      this.menu = false;
    },
  },
};
</script>

<style scoped>
a {
  text-decoration: none;
  color: #000;
}
</style>
