<template>
  <v-dialog :model-value="show" @update:model-value="salir" fullscreen transition="dialog-bottom-transition">
    <v-toolbar>
      <v-btn icon dark @click="salir()">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-toolbar-title>Pedidos</v-toolbar-title>
    </v-toolbar>
    <v-card>
      <v-card-text style="padding-bottom: 100px;">
        <v-card
          v-for="(item, i) in lineasPedidos"
          :key="i"
          class="mb-3 pa-2"
          elevation="2"
          rounded="lg"
          @click="recuperar(item)"
        >
          <v-card-title>Mesa: {{ item.mesa }}</v-card-title>
          <v-card-subtitle>Camarero: {{ item.camarero }}</v-card-subtitle>
          <v-card-text>Hora: {{ item.hora }}</v-card-text>
        </v-card>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
import { useMainStore } from "@/stores/main";
import { computed } from 'vue';

export default {
  props: ["show"],
  emits: ["close"],
  setup(props, { emit }) {
    const store = useMainStore();
    
    const lineasPedidos = computed(() => store.lineasPedidos);
    
    const salir = () => {
      emit("close");
    };
    
    const recuperar = (p) => {
      store.recuperarPedido(p);
      emit("close");
    };

    return {
      lineasPedidos,
      salir,
      recuperar
    };
  },
};
</script>
