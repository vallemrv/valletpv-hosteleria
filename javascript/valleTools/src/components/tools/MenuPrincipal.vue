<template>
  <v-navigation-drawer expand-on-hover rail v-model="drawer" app @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave">
    <v-list>
      <v-list-item v-if="empresa" prepend-icon="mdi-handshake" :title="empresa.nombre"
        :subtitle="empresa.nombre"></v-list-item>
    </v-list>

    <v-divider></v-divider>

    <v-list density="compact" nav>

      <v-list-item v-for="(item, index) in configStore.listaComponentes" :key="index" :prepend-icon="item.icon"
        :title="item.titulo" @click="navigateToRoute(item)"></v-list-item>

      <v-spacer></v-spacer>

    <template v-slot:append>
      <v-menu offset-y>
        <template v-slot:activator="{ props }">
          <div class="pa-2">
            <v-btn color="primary" block>
              <v-icon>mdi-account</v-icon>
              <div v-if="isExpanded" v-bind="props">{{ userStore.getDisplayName() }} </div>
            </v-btn>
          </div>
        </template>
        <v-list>
          <v-list-item @click="goToProfile">
            <v-list-item-title>Profile</v-list-item-title>
          </v-list-item>
          <v-list-item @click="logout">
            <v-list-item-title>Logout</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

    </template>
    </v-list>


  




  </v-navigation-drawer>

  <div style="position: fixed; bottom: 20px; right: 20px;" v-if="!drawer">
    <v-btn relative elevation="8" icon large color="primary" @click="drawer = true">
      <v-icon>mdi-menu</v-icon>
    </v-btn>
  </div>
</template>
  
<script>
import { EmpresaStore } from '@/stores/empresaStore';
import { ConfigStore } from '@/stores/configStore';


export default {
  setup() {
    const empresaStore = EmpresaStore();
    const configStore = ConfigStore();

    return {
      configStore,
      empresaStore,
      isExpanded: false
    };
  },
  data() {
    return {
      drawer: true,
    };
  },
  computed: {
    empresa() {
      return this.empresaStore.empresa
    }
  },
  methods: {
    navigateToRoute(item) {
      if (item.name) {
        this.$router.push({ name: item.name, params: item.params });
      }
      else this.configStore.setComponente(item)
    },
    handleMouseEnter() {
      this.isExpanded = true;
    },
    handleMouseLeave() {
      this.isExpanded = false;
    },
  },
};
</script>
  