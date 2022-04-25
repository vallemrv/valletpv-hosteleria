<template>
  <v-app-bar app>
    <router-link to="/">
      <v-toolbar-title> ValleTPV </v-toolbar-title>
    </router-link>
    <v-spacer></v-spacer>

    <valle-inbox :num_inst="num_inst" v-if="num_inst > 0"></valle-inbox>

    <v-menu v-model="menu" anchor="bottom end" origin="auto">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>

      <v-list>
        <v-list-item v-for="(item, index) in items" :key="index" @click="showData(item)">
          <v-list-item-title>
            {{ item.title }}
            <v-icon v-if="item.icon">{{ item.icon }}</v-icon>
          </v-list-item-title>
        </v-list-item>
        <v-list-item @click="salir()" v-if="user">
          {{ user[0].username }}
          <v-icon class="ml-3">mdi-location-exit</v-icon>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>
</template>

<script>
import router from "@/router";
import { mapState, mapActions } from "vuex";
import ValleInbox from "./ValleInbox.vue";
export default {
  components: { ValleInbox },
  data: () => ({
    items: [
      { title: "Gestion", link: "gestion" },
      { title: "Ventas", link: "ventas" },
    ],
    menu: false,
  }),
  computed: {
    ...mapState(["user", "token", "error", "instrucciones"]),
    num_inst() {
      return this.instrucciones ? this.instrucciones.length : 0;
    },
  },
  methods: {
    showData: function (item) {
      router.push(item.link);
      this.menu = false;
    },
    mostrarUser() {
      if (this.token != null && this.token != undefined) {
        if (localStorage.user) {
          this.$store.state.user = JSON.parse(localStorage.user);
        } else {
          let params = new FormData();
          params.append("app", "auth");
          params.append("tb", "user");
          params.append("filter", JSON.stringify({ id: this.token.id }));
          this.getListado({ params: params });
        }
      }
    },
    salir() {
      this.$tools.salir(this.$store);
    },
    ...mapActions(["getListado"]),
  },
  watch: {
    user(v) {
      if (v) {
        localStorage.user = JSON.stringify(v);
      }
    },
    error(v) {
      if (v) {
        if (v.request.status == 401) {
          this.salir();
        }
      }
    },
  },
  created() {
    this.mostrarUser();
  },
};
</script>

<style scoped>
a {
  text-decoration: none;
  color: #000;
}
</style>
