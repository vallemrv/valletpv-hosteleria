<template>
  <v-dialog v-model="isOpen" max-width="450px" persistent>
    <v-card elevation="12" rounded="xl" class="opciones-dialog">
      <!-- Header -->
      <v-card-title class="opciones-header">
        <v-avatar size="56" class="opciones-icon elevation-6">
          <v-icon size="38" color="primary">mdi-format-list-checks</v-icon>
        </v-avatar>
        <h3 class="opciones-title">{{ titulo }}</h3>
      </v-card-title>

      <v-card-text class="opciones-content">
        <!-- Lista de opciones -->
        <div class="opciones-lista">
          <div 
            v-for="(opcion, index) in opcionesInternas" 
            :key="opcion.id || index"
            class="opcion-item"
          >
            <v-checkbox
              v-model="opcion.seleccionada"
              class="opcion-checkbox"
              :label="opcion.nombre"
              color="primary"
              hide-details
              @change="onOpcionChange(opcion, index)"
            />
          </div>

          <!-- Mensaje cuando no hay opciones -->
          <div v-if="opcionesInternas.length === 0" class="empty-state">
            <v-icon size="48" color="grey-lighten-1" class="empty-icon">
              mdi-checkbox-blank-outline
            </v-icon>
            <p class="empty-text">No hay opciones disponibles</p>
          </div>
        </div>

        <!-- Contador de selecciones (opcional) -->
        <div v-if="mostrarContador" class="contador-selecciones">
          <v-chip 
            color="info" 
            size="small" 
            variant="outlined"
            class="contador-chip"
          >
            {{ opcionesSeleccionadas.length }} de {{ opcionesInternas.length }} seleccionadas
          </v-chip>
        </div>
      </v-card-text>

      <!-- Botones de acción -->
      <v-card-actions class="opciones-actions pa-6">
        <v-row no-gutters>
          <v-col cols="6" class="pr-2">
            <v-btn
              class="action-btn-secondary"
              variant="tonal"
              color="grey-darken-1"
              @click="cerrarDialog()"
              block
              size="large"
              height="72"
              elevation="2"
              rounded="lg"
            >
              <v-icon class="me-2">mdi-close</v-icon>
              {{ textoBotonSalir }}
            </v-btn>
          </v-col>
          <v-col v-if="mostrarBotonConfirmar" cols="6" class="pl-2">
            <v-btn
              class="action-btn-primary"
              variant="elevated"
              color="primary"
              @click="confirmarSeleccion()"
              block
              size="large"
              height="72"
              elevation="2"
              rounded="lg"
            >
              <v-icon class="me-2">mdi-check</v-icon>
              {{ textoBotonConfirmar }}
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

export interface OpcionSeleccion {
  id?: string | number;
  nombre: string;
  seleccionada: boolean;
}

const props = withDefaults(defineProps<{
  modelValue: boolean;
  titulo?: string;
  opciones: OpcionSeleccion[];
  mostrarContador?: boolean;
  mostrarBotonConfirmar?: boolean;
  textoBotonSalir?: string;
  textoBotonConfirmar?: string;
}>(), {
  titulo: 'Seleccionar opciones',
  mostrarContador: true,
  mostrarBotonConfirmar: false,
  textoBotonSalir: 'Salir',
  textoBotonConfirmar: 'Confirmar'
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'opcionChanged': [opcion: OpcionSeleccion, index: number];
  'seleccionConfirmada': [opcionesSeleccionadas: OpcionSeleccion[]];
}>();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const opcionesInternas = ref<OpcionSeleccion[]>([]);

const opcionesSeleccionadas = computed(() => {
  return opcionesInternas.value.filter(opcion => opcion.seleccionada);
});

// Sincronizar opciones cuando cambian las props
watch(() => props.opciones, (nuevasOpciones) => {
  opcionesInternas.value = nuevasOpciones.map(opcion => ({
    ...opcion,
    seleccionada: opcion.seleccionada || false
  }));
}, { immediate: true, deep: true });

function onOpcionChange(opcion: OpcionSeleccion, index: number) {
  emit('opcionChanged', opcion, index);
}

function confirmarSeleccion() {
  emit('seleccionConfirmada', opcionesSeleccionadas.value);
  cerrarDialog();
}

function cerrarDialog() {
  isOpen.value = false;
}
</script>

<style scoped>
.opciones-dialog {
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.opciones-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 24px 16px;
  background: linear-gradient(135deg, 
    rgba(var(--v-theme-primary), 0.1) 0%, 
    rgba(var(--v-theme-primary), 0.05) 100%);
  border-bottom: 1px solid rgba(var(--v-theme-primary), 0.1);
}

.opciones-icon {
  background: rgba(var(--v-theme-primary), 0.1);
  border: 2px solid rgba(var(--v-theme-primary), 0.3);
  margin-bottom: 12px;
}

.opciones-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--v-theme-on-surface);
  margin: 0;
  text-align: center;
}

.opciones-content {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
  flex: 1 1 auto;
}

/* Lista de opciones */
.opciones-lista {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opcion-item {
  background: rgba(var(--v-theme-primary), 0.04);
  border: 1px solid rgba(var(--v-theme-primary), 0.1);
  border-radius: 8px;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

.opcion-item:hover {
  background: rgba(var(--v-theme-primary), 0.08);
  border-color: rgba(var(--v-theme-primary), 0.2);
  transform: translateX(2px);
}

.opcion-checkbox {
  width: 100%;
}

.opcion-checkbox :deep(.v-label) {
  font-size: 1rem;
  font-weight: 500;
  color: var(--v-theme-on-surface);
  opacity: 1;
}

.opcion-checkbox :deep(.v-selection-control__wrapper) {
  margin-right: 12px;
}

.opcion-checkbox :deep(.v-checkbox .v-selection-control__input) {
  transition: all 0.2s ease;
}

.opcion-checkbox :deep(.v-checkbox .v-selection-control__input:hover) {
  transform: scale(1.1);
}

/* Estado vacío */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 1rem;
  color: var(--v-theme-on-surface);
  opacity: 0.7;
  margin: 0;
}

/* Contador de selecciones */
.contador-selecciones {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(var(--v-theme-primary), 0.1);
}

.contador-chip {
  font-weight: 600;
}

/* Botones de acción */
.opciones-actions {
  padding: 16px 24px 24px;
  flex-shrink: 0;
}

.action-btn-secondary,
.action-btn-primary {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
}

.w-100 {
  width: 100%;
}

/* Scroll personalizado */
.opciones-content::-webkit-scrollbar {
  width: 6px;
}

.opciones-content::-webkit-scrollbar-track {
  background: rgba(var(--v-theme-on-surface), 0.05);
}

.opciones-content::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-primary), 0.3);
  border-radius: 3px;
}

.opciones-content::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-primary), 0.5);
}

/* Responsive */
@media (max-width: 600px) {
  .opciones-content {
    padding: 16px;
    max-height: 300px;
  }
  
  .opciones-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .action-btn-secondary,
  .action-btn-primary {
    width: 100%;
  }
}

/* Animaciones */
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.opcion-item {
  animation: slideInDown 0.3s ease;
}

.opcion-item:nth-child(1) { animation-delay: 0.05s; }
.opcion-item:nth-child(2) { animation-delay: 0.1s; }
.opcion-item:nth-child(3) { animation-delay: 0.15s; }
.opcion-item:nth-child(4) { animation-delay: 0.2s; }
.opcion-item:nth-child(5) { animation-delay: 0.25s; }

/* Estados de selección */
.opcion-item:has(.v-checkbox--checked) {
  background: rgba(var(--v-theme-primary), 0.12);
  border-color: rgba(var(--v-theme-primary), 0.4);
  box-shadow: 0 2px 8px rgba(var(--v-theme-primary), 0.15);
}

/* Focus states */
.opcion-checkbox:focus-within {
  outline: 2px solid var(--v-theme-primary);
  outline-offset: 2px;
  border-radius: 4px;
}
</style>
