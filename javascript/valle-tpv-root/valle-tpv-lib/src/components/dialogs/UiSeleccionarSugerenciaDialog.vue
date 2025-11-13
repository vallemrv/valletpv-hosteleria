<template>
  <UiDialogScaffold
    v-model="isOpen"
    title="Seleccionar Sugerencias"
    :actions="actions"
    @on_action="handleAction"
  >
    <div class="seleccionar-sugerencia-content">
      
    <!-- Vista previa del producto final -->
    <v-card-text v-if="selectedSugerencias.length > 0" class="pt-0 pb-0">
      <v-divider class="mb-3"></v-divider>
      <div class="text-h6 font-weight-bold text-primary">
        {{ productoFinal }}
      </div>
      <div class="text-h5 font-weight-bold text-success mt-2">
        {{ precioTotal }}€
      </div>
    </v-card-text>
      
      <div class="sugerencia-list">
        <div
          v-for="sugerencia in sugerencias"
          :key="sugerencia.id"
          class="sugerencia-item"
          :class="{ 'selected': isSugerenciaSelected(sugerencia) }"
          @click="toggleSugerencia(sugerencia)"
        >
          <div class="sugerencia-content">
            <div class="sugerencia-text">
              <span class="sugerencia-name">{{ sugerencia.sugerencia }}</span>
              <span class="sugerencia-price">+{{ sugerencia.incremento.toFixed(2) }}€</span>
            </div>
            <div class="sugerencia-check">
              <v-icon
                v-if="isSugerenciaSelected(sugerencia)"
                color="primary"
                size="24"
                class="check-icon"
              >
                mdi-check-circle
              </v-icon>
              <v-icon
                v-else
                color="grey-lighten-1"
                size="24"
                class="uncheck-icon"
              >
                mdi-circle-outline
              </v-icon>
            </div>
          </div>
        </div>
      </div>
      <div v-if="sugerencias.length === 0" class="no-sugerencias">
        <v-icon size="48" color="grey-lighten-1">mdi-information-outline</v-icon>
        <p>No hay sugerencias disponibles con incremento > 0</p>
      </div>
    </div>
  </UiDialogScaffold>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useSugerenciasStore } from '../../store/dbStore/sugerenciasStore';
import { useTeclasStore } from '../../store/dbStore/teclasStore';
import Sugerencia from '../../models/sugerencia';
import UiDialogScaffold from './UiDialogScaffold.vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  idTecla: { type: String, required: true }
});

const emit = defineEmits(['update:modelValue', 'sugerencia-seleccionada']);

const sugerenciasStore = useSugerenciasStore();
const teclasStore = useTeclasStore();

const isOpen = ref(props.modelValue);
const selectedSugerencias = ref<Sugerencia[]>([]);

watch(() => props.modelValue, (newVal) => {
  isOpen.value = newVal;
  if (newVal) {
    selectedSugerencias.value = [];
  }
});

watch(isOpen, (newVal) => {
  emit('update:modelValue', newVal);
});

const sugerencias = computed(() => {
  return sugerenciasStore.sugPorTeclaConIncremento(props.idTecla);
});

const tecla = computed(() => {
  return teclasStore.items.find(t => t.ID.toString() === props.idTecla);
});

// Computed para mostrar el producto final con modificadores
const productoFinal = computed(() => {
  const nombreBase = tecla.value?.descripcion_r || tecla.value?.nombre || 'Producto';
  
  if (selectedSugerencias.value.length === 0) {
    return nombreBase;
  }
  
  const sugerenciasTexto = selectedSugerencias.value.map(sug => sug.sugerencia).join(', ');
  return `${nombreBase} (${sugerenciasTexto})`;
});

// Computed para calcular el precio total
const precioTotal = computed(() => {
  const precioBase = tecla.value?.p1 || 0;
  const incrementoTotal = selectedSugerencias.value.reduce((sum, sug) => sum + sug.incremento, 0);
  return (precioBase + incrementoTotal).toFixed(2);
});

const actions = [
  { id: 'accept', text: 'Aceptar', icon: 'mdi-check', color: 'primary', block: true }
];

// Función para verificar si una sugerencia está seleccionada
function isSugerenciaSelected(sugerencia: Sugerencia): boolean {
  return selectedSugerencias.value.some(s => s.id === sugerencia.id);
}

// Función para alternar la selección de una sugerencia
function toggleSugerencia(sugerencia: Sugerencia) {
  const index = selectedSugerencias.value.findIndex(s => s.id === sugerencia.id);
  if (index > -1) {
    selectedSugerencias.value.splice(index, 1);
  } else {
    selectedSugerencias.value.push(sugerencia);
  }
}

function handleAction(actionId: string) {
  if (actionId === 'accept') {
    // Siempre emitir el evento, incluso si no hay sugerencias seleccionadas
    // Si no hay ninguna seleccionada, se devuelve un array vacío (producto sin modificadores)
    emit('sugerencia-seleccionada', selectedSugerencias.value);
  }
  isOpen.value = false;
}
</script>

<style scoped>
.seleccionar-sugerencia-content {
  text-align: center;
}

.sugerencia-label {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--v-theme-on-surface);
  margin-bottom: 1rem;
}

.producto-preview {
  background: rgba(var(--v-theme-primary), 0.08);
  border: 2px solid rgba(var(--v-theme-primary), 0.3);
  border-radius: 12px;
  padding: 1rem;
  margin: 1rem 0;
  text-align: center;
}

.preview-header {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--v-theme-primary);
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.preview-producto {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--v-theme-on-surface);
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.preview-precio {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--v-theme-primary);
}

.sugerencia-list {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.sugerencia-item {
  background: var(--v-theme-surface);
  border: 2px solid var(--v-theme-outline-variant);
  border-radius: 12px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  user-select: none;
}

.sugerencia-item:hover {
  background: rgba(var(--v-theme-primary), 0.04);
  border-color: rgba(var(--v-theme-primary), 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.sugerencia-item.selected {
  background: rgba(var(--v-theme-primary), 0.08);
  border-color: var(--v-theme-primary);
  box-shadow: 0 2px 8px rgba(var(--v-theme-primary), 0.2);
}

.sugerencia-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.sugerencia-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  flex: 1;
}

.sugerencia-name {
  font-size: 1rem;
  font-weight: 500;
  color: var(--v-theme-on-surface);
  margin-bottom: 0.25rem;
}

.sugerencia-price {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--v-theme-primary);
}

.sugerencia-check {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 1rem;
}

.check-icon {
  animation: checkIn 0.2s ease-in-out;
}

.uncheck-icon {
  opacity: 0.6;
  transition: opacity 0.2s ease-in-out;
}

.sugerencia-item:hover .uncheck-icon {
  opacity: 1;
}

@keyframes checkIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.sugerencia-checkbox {
  margin-bottom: 0.25rem;
}

.no-sugerencias {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 2rem;
  color: var(--v-theme-on-surface-variant);
}

.no-sugerencias p {
  margin-top: 0.5rem;
  font-size: 1rem;
}
</style>