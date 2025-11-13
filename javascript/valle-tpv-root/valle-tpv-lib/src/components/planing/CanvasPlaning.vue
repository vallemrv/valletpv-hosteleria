<template>
  <div ref="salon" class="salon-container" :class="{ 'edit-mode': esEditable }">
    <MesaItem
      v-for="mesa in mesas"
      :key="mesa.ID"
      :mesa="mesa"
      :bounds="salon"
      :es-editable="esEditable"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useMesasStore } from '../../store/dbStore/mesasStore';
import MesaItem from './MesaItem.vue'; // Ajusta la ruta si es necesario

// Este componente acepta la propiedad para saber si es editable o no
defineProps({
  esEditable: {
    type: Boolean,
    default: false,
  },
});

const salon = ref<HTMLElement | null>(null);
const mesasStore = useMesasStore();

// Obtenemos las mesas directamente del store
const mesas = computed(() => mesasStore.items);
</script>

<style scoped>
.salon-container {
  width: 100%;
  height: 100%;
  position: relative;
  border: 2px dashed transparent;
  transition: all 0.3s ease;
  background-color: #f5f5f5;
}

/* Estilos que se aplican SÓLO cuando esEditable es true */
.salon-container.edit-mode {
  border-color: #42a5f5;
  background-image:
    linear-gradient(rgba(0,0,0,0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.1) 1px, transparent 1px);
  background-size: 20px 20px;
}
</style>