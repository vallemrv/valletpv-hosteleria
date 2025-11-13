<template>
  <v-list class="mesa-list">
    <v-list-item
      v-for="mesa in mesasPorZona(props.zona)"
      :key="mesa.ID"
      :title="mesa.Nombre"
      subtitle="Libre"
      prepend-icon="mdi-table-chair"
      @click="seleccionarMesa(mesa)"
    >
      <template v-slot:append>
        <v-icon color="success">mdi-circle-outline</v-icon>
      </template>
    </v-list-item>
  </v-list>
</template>

<script setup lang="ts">

import { useMesasStore } from '../../store/dbStore/mesasStore';
import {  Mesa, Zona } from '../../models/mesaZona';
import { storeToRefs } from 'pinia';

const props = defineProps<{
  zona: Zona;
}>();

const mesasStore = useMesasStore();

const { mesasPorZona } = storeToRefs(mesasStore);



function seleccionarMesa(mesa: Mesa) {
  console.log('Mesa seleccionada:', mesa);
  // Aquí iría la lógica para abrir la mesa, añadir productos, etc.
}
</script>

<style scoped>
.mesa-list {
  width: 100%;
  background-color: transparent;
}
.v-list-item {
  margin-bottom: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: white;
}
</style>