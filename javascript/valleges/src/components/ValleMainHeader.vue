<template>
  <v-app-bar app color="#cfb6d4">
    <v-app-bar-nav-icon
      v-if="empresa"
      @click.stop="drawer = !drawer; refs.menu.open_drawer(drawer)"
    ></v-app-bar-nav-icon>
    <v-toolbar-title v-if="empresa"> {{ empresa.nombre }} </v-toolbar-title>
    <v-toolbar-title v-else>ValleGES</v-toolbar-title>
    <v-progress-circular
      indeterminate
      color="primary"
      v-if="ocupado"
    ></v-progress-circular>
    <v-btn icon @click="showDialog()"><v-icon>mdi-home-plus</v-icon></v-btn>
    <v-btn icon v-if="empresas.length > 0">
      <v-icon>mdi-dots-vertical</v-icon>
      <v-menu location="bottom start" origin="end" activator="parent">
        <v-list>
          <v-list-item
            v-for="(item, index) in empresas"
            :key="index"
            :value="index"
            @click="selEmpresa(index)"
          >
            <v-list-item-title>{{ item.nombre }}   
              <v-icon v-if="item.nombre == empresa.nombre">mdi-check</v-icon>
              <v-icon @click.stop="borrarEmpresa(index)">mdi-delete</v-icon>
          </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-btn>
    
  </v-app-bar>
  <valle-menu :items="items" ref="menu" @click_item="on_click_menu"></valle-menu>
  <valle-dialogo-form
    @close="on_close_form"
    ref="dialgForm"
    :title="titledialog"
    :tipo="tipo"
    :item="itemEmpresa"
    :form="formEmpresa"
  >
  </valle-dialogo-form>
</template>

<script>
import { mapState, mapActions } from "vuex";
import ValleDialogoForm from "./ValleDialogoForm.vue";
import ValleMenu from "./ValleMenu.vue";
export default {
  props: {
    items: {
      default: [],
    },
  },
  components: { ValleDialogoForm, ValleMenu },
  data: () => ({
    titledialog: "Agregar empresa",
    tipo: "add_empresa",
    drawer: false,
    formEmpresa: [
      { col: "nombre", label: "Nombre", tp: "text" },
      { col: "url", label: "Url", tp: "text" },
      { col: "user", label: "Usuario", tp: "text" },
      { col: "pass", label: "Contraseña", tp: "password" },
    ],
    itemEmpresa: {},
  }),
  computed: {
    ...mapState(["empresa", "empresas", "ocupado"]),
  },
  methods: {
    ...mapActions(["addEmpresa", "selEmpresa", "borrarEmpresa"]),
    showDialog() {
      this.$refs.dialgForm.show_dialogo();
    },
    on_close_form(item) {
      if (item) {
        this.addEmpresa(item);
        this.itemEmpresa = {};
      }
    },
    on_click_menu(name) {
      this.$router.push({ name: name });
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
