<template>
  <v-dialog v-model="isOpen" max-width="700px" persistent>
    <v-card elevation="16" rounded="xl" class="autorizacion-dialog">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">
        Solicitar Autorización{{ parametros.accion ? ` - ${parametros.accion}` : '' }}
      </v-card-title>

      <v-card-text class="pa-4">
        <div v-if="loading" class="text-center pa-8">
          <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
          <p class="mt-4 text-medium-emphasis">Enviando solicitud...</p>
        </div>

        <div v-else-if="error" class="text-center pa-8">
          <v-icon color="error" size="48" class="mb-4">mdi-alert-circle-outline</v-icon>
          <p class="text-body-1 font-weight-bold">¡Vaya! Algo ha fallado</p>
          <p class="text-error">{{ error }}</p>
        </div>

        <div v-else>
          <p class="text-medium-emphasis mb-6 text-body-1 px-2">
            Selecciona un camarero autorizado para solicitar la autorización:
          </p>
          
          <div class="camareros-container mb-4">
            <div
              v-for="camarero in camareros"
              :key="camarero.id"
              class="camarero-row"
            >
              <div class="camarero-info">
                <div class="camarero-nombre">
                  {{ camarero.nombre }} {{ camarero.apellidos }}
                </div>
                <div class="camarero-estado">
                  {{ camarero.autorizado === 1 ? 'En el pase' : 'No esta en el pase' }}
                </div>
              </div>
              
              <div class="camarero-accion">
                <v-btn
                  color="primary"
                  variant="elevated"
                  class="solicitar-btn"
                  :disabled="loading"
                  @click="solicitarAutorizacion(camarero)"
                >
                  <v-icon class="me-2">mdi-hand-extended-outline</v-icon>
                  Solicitar
                </v-btn>
              </div>
            </div>

            <div v-if="camareros.length === 0" class="text-center pa-8">
              <v-icon color="warning" size="48" class="mb-4">mdi-account-alert-outline</v-icon>
              <p class="text-body-1 font-weight-bold">No hay camareros disponibles</p>
              <p class="text-medium-emphasis">No se encontraron camareros para esta autorización</p>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="pa-6">
        <v-spacer />
        <v-btn
          variant="tonal"
          @click="cerrarDialog" 
          class="action-button-small"
          height="72"
          elevation="3"
          :disabled="loading"
          rounded="16"
        >
          Cerrar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useEmpresasStore } from '../../store/dbStore/empresasStore';
import Camarero from '../../models/camarero';

interface Props {
  modelValue: boolean;
  camareros: Camarero[];
  parametros?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  camareros: () => [],
  parametros: () => ({})
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'autorizacionSolicitada': [camarero: Camarero];
  'autorizacionCancelada': [];
}>();

const empresasStore = useEmpresasStore();

// Estado del componente
const loading = ref(false);
const error = ref<string | null>(null);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Inicializar store cuando se abre el diálogo
watch(() => props.modelValue, async (esVisible) => {
  if (esVisible) {
    error.value = null;
  }
});

async function solicitarAutorizacion(camarero: Camarero) {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva?.uid || !empresaActiva?.url_servidor) {
    error.value = 'No hay una empresa activa configurada correctamente.';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const formData = new FormData();
    formData.append('uid', empresaActiva.uid);
    formData.append('idautorizado', camarero.id.toString());
    formData.append('accion', props.parametros.accion || '');
    formData.append('instrucciones', JSON.stringify(props.parametros.inst || {}));

    const response = await fetch(`${empresaActiva.url_servidor}/api/autorizaciones/pedir_autorizacion`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    
    // Emitir evento de éxito
    emit('autorizacionSolicitada', camarero);
    cerrarDialog();
    
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Error desconocido al solicitar autorización.';
    console.error('Error solicitando autorización:', err);
  } finally {
    loading.value = false;
  }
}

function cerrarDialog() {
  // Emitir evento de cancelación si no se ha enviado ninguna solicitud
  emit('autorizacionCancelada');
  emit('update:modelValue', false);
}
</script>

<style scoped>
.autorizacion-dialog {
  overflow: hidden;
}

.camareros-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}

.camarero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  margin-bottom: 12px;
  background-color: rgba(var(--v-theme-on-surface), 0.04);
  border-radius: 20px;
  border: 1px solid rgba(var(--v-theme-outline), 0.15);
  transition: all 0.3s ease;
  min-height: 90px;
}

.camarero-row:hover {
  border-color: rgba(var(--v-theme-primary), 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.camarero-info {
  flex: 1;
  min-width: 0;
}

.camarero-nombre {
  font-size: 1.2rem;
  font-weight: 700;
  color: rgba(var(--v-theme-on-surface), 0.9);
  line-height: 1.3;
  margin-bottom: 6px;
}

.camarero-estado {
  font-size: 0.9rem;
  color: rgba(var(--v-theme-on-surface), 0.65);
  line-height: 1.2;
}

.camarero-accion {
  flex-shrink: 0;
  margin-left: 20px;
}

.solicitar-btn {
  height: 56px !important;
  font-size: 1rem;
  font-weight: 600;
  text-transform: none;
  border-radius: 14px;
  min-width: 120px;
}

/* Botones de acción */
.action-button-small {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
}
</style>