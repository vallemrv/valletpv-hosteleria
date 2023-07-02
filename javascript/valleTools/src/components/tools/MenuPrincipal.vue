<template>
  <v-navigation-drawer expand-on-hover rail v-model="drawer" app @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave">
    <v-list>
      <v-list-item v-if="empresa" prepend-icon="mdi-handshake" :title="empresa.empresa"
        :subtitle="empresa.alias"></v-list-item>
    </v-list>

    <v-divider></v-divider>

    <v-list density="compact" nav>

      <v-list-item v-for="(item, index) in configStore.listaComponentes" :key="index" :prepend-icon="item.icon"
        :title="item.titulo" @click="navigateToRoute(item)"></v-list-item>

      <v-spacer></v-spacer>


    </v-list>

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
  </v-navigation-drawer>


  <div class="div-fixed" v-if="!drawer">
    <v-btn relative elevation="8" icon large color="primary" @click="drawer = true">
      <v-icon>mdi-menu</v-icon>
    </v-btn>
  </div>
</template>
  
<script>
import { useEmpresasStore } from '@/stores/empresasStore';
import { useUserStore } from '@/stores/userStore';
import { ConfigStore } from '@/stores/configStore';
import { auth } from '@/firebase';

export default {
  setup() {
    const empresasStore = useEmpresasStore();
    const userStore = useUserStore();
    const configStore = ConfigStore();

    return {
      configStore,
      empresasStore,
      userStore,
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
      return this.empresasStore.empresaSel
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
    goToProfile() {
      this.$router.push({ name: "profile" });
    }, 
    logout() {
      auth.signOut();
      this.$router.push( "/login" );
    },
  },
};
</script>
  