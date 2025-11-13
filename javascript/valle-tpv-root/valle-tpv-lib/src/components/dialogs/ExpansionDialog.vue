
<template>
  <v-dialog v-model="esVisible" persistent max-width="500">
    <v-card elevation="16" rounded="xl">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">Opciones de elementos</v-card-title>
      <v-card-text>
        <v-expansion-panels>
          <v-expansion-panel>
            <v-expansion-panel-title>Mesas</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="item-grid">
                <ContentItemPlaning v-for="item in mesas" :key="item.label" :label="item.label" :svg="item.svg" @select="handleSelect" />
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-title>Barra</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="item-grid">
                <ContentItemPlaning v-for="item in barra" :key="item.label" :label="item.label" :svg="item.svg" @select="handleSelect" />
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-title>Mobiliario</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="item-grid">
                <ContentItemPlaning v-for="item in mobiliario" :key="item.label" :label="item.label" :svg="item.svg" @select="handleSelect" />
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-title>Construcción</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="item-grid">
                <ContentItemPlaning v-for="item in construccion" :key="item.label" :label="item.label" :svg="item.svg" @select="handleSelect" />
              </div>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>
      <v-card-actions class="pa-6">
        <v-spacer />
        <v-btn 
          color="primary" 
          @click="close"
          class="action-button-small"
          height="72"
          rounded="16"
          elevation="3"
        >
          Cerrar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import ContentItemPlaning from '../planing/ContentItemPlaning.vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits(['update:visible', 'select-svg']);

function close() {
  emit('update:visible', false);
}

function handleSelect(item: { label: string; url: string }) {
  emit('select-svg', item);
}

const esVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
});

// Listas de items con SVG
const mesas = [
  { label: 'Mesa cuadrada', svg: new URL('@/assets/planing/mesa_6.svg', import.meta.url).href },
  { label: 'Mesa alta', svg: new URL('@/assets/planing/mesa_alta.svg', import.meta.url).href },
  { label: 'Mesa redonda', svg: new URL('@/assets/planing/mesa_redonda.svg', import.meta.url).href },
];
const barra = [
  { label: 'Zona barra', svg: new URL('@/assets/planing/barra.svg', import.meta.url).href },
  { label: 'Esquina barra', svg: new URL('@/assets/planing/barra_esquina.svg', import.meta.url).href },
  { label: 'Taburete', svg: new URL('@/assets/planing/taburete.svg', import.meta.url).href },
  { label: 'Vitrina', svg: new URL('@/assets/planing/vitrina.svg', import.meta.url).href },
  { label: 'Barril', svg: new URL('@/assets/planing/barril.svg', import.meta.url).href },
];
const mobiliario = [
  { label: 'Planta', svg: new URL('@/assets/planing/planta.svg', import.meta.url).href },
  { label: 'Jardinera', svg: new URL('@/assets/planing/jardinera.svg', import.meta.url).href },
  { label: 'Columna', svg: new URL('@/assets/planing/columna.svg', import.meta.url).href },
];
const construccion = [
  { label: 'Muro', svg: new URL('@/assets/planing/muro.svg', import.meta.url).href },
  { label: 'Puerta', svg: new URL('@/assets/planing/puerta.svg', import.meta.url).href },
  { label: 'Ventana', svg: new URL('@/assets/planing/ventana.svg', import.meta.url).href },
];
</script>

<style scoped>
.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
  gap: 12px;
  padding: 8px 0;
}

/* Botones de acción */
.action-button-small {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
}
</style>
