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
    </v-list>

    <template v-slot:append>
    
        
          <div class="pa-2">
            <v-btn color="primary" @click="update()" block>
              <v-icon>mdi-account</v-icon>
              <div v-if="isExpanded">{{ empresaStore.getUserName() }} </div>
            </v-btn>
          </div>
       
        
    </template>

  </v-navigation-drawer>
  <DialogFormDinamico ref="dialogFormDinamico" @save="save"/>
  <div style="position: fixed; bottom: 20px; right: 20px;" v-if="!drawer">
    <v-btn relative elevation="8" icon large color="primary" @click="drawer = true;">
      <v-icon>mdi-menu</v-icon>
    </v-btn>
  </div>
</template>
  
<script>

import { EmpresaStore } from '@/stores/empresaStore';
import { UserStore } from '@/stores/usuarios';
import { ConfigStore } from '@/stores/configStore';
import DialogFormDinamico from '@/components/dialogs/DialogFormDinamico.vue';

export default {
  components: {
    DialogFormDinamico
  },
  setup() {
    const empresaStore = EmpresaStore();
    const configStore = ConfigStore();
    const usuarios = UserStore();
    usuarios.empresaStore = empresaStore;

    return {
      configStore,
      empresaStore,
      isExpanded: false,
      usuarios,
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
    update(){
      this.$refs.dialogFormDinamico.openDialog(
        this.empresaStore.getUserProfile(),
        "Modifiar usuario",
        this.usuarios.fields
      )
    },
    async save(item){
      const data =  await this.usuarios.update(item);
      const profile = data.data.profile;
      const user = data.data.user; 
      const token = data.data.token;
      this.empresaStore.setUserProfile(profile, user, token);
    },
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
  