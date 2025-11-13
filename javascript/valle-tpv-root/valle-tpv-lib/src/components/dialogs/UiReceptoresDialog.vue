<template>
  <v-dialog v-model="isOpen" max-width="600px" persistent>
    <v-card elevation="16" rounded="xl" class="receptores-dialog">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">
        Lista de Receptores
      </v-card-title>

      <v-card-text class="pa-4">
        <div v-if="loading" class="text-center pa-8">
          <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
          <p class="mt-4 text-medium-emphasis">Cargando receptores...</p>
        </div>

        <div v-else-if="error" class="text-center pa-8">
          <v-icon color="error" size="48" class="mb-4">mdi-alert-circle-outline</v-icon>
          <p class="text-body-1 font-weight-bold">¡Vaya! Algo ha fallado</p>
          <p class="text-error">{{ error }}</p>
        </div>

        <div v-else>
          <p class="text-medium-emphasis mb-6 text-body-1 px-2">Configura qué receptores están activos:</p>
          
          <div class="receptores-container mb-4">
            <div
              v-for="receptor in receptores"
              :key="receptor.ID"
              class="receptor-row"
              :class="{ 'is-active': receptor.Activo }"
              @click="receptor.Activo = !receptor.Activo"
            >
              <div class="receptor-info">
                <div class="receptor-nombre">
                  {{ receptor.Nombre }}
                </div>
                <div v-if="receptor.nomimp" class="receptor-descripcion">
                  {{ receptor.nomimp }}
                </div>
              </div>
              
              <div class="receptor-switch">
                <v-switch
                  v-model="receptor.Activo"
                  color="success"
                  hide-details
                  density="comfortable"
                  class="elegant-switch"
                  @click.stop
                ></v-switch>
              </div>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="pa-6">
        <v-row no-gutters>
          <v-col cols="6" class="pr-2">
            <v-btn
              variant="tonal"
              @click="cerrarDialog" 
              block
              class="action-button-small"
              height="72"
              elevation="3"
              :disabled="saving"
              rounded="16"
            >
              Cancelar
            </v-btn>
          </v-col>
          <v-col cols="6" class="pl-2">
            <v-btn
              variant="elevated"
              @click="guardarCambios" 
              block
              class="action-button-small"
              height="72"
              :disabled="loading || !!error || saving"
              :loading="saving"
              color="primary"
              elevation="3"
              rounded="16"
            >
              Guardar
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useEmpresasStore } from '../../store/dbStore/empresasStore';

interface Receptor {
  Nombre: string;
  Activo: boolean;
  ID: number;
  nomimp: string;
}

type Props = {
  modelValue: boolean;
};

const props = defineProps<Props>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'cambiosGuardados': [];
}>();

const empresasStore = useEmpresasStore();

// Estado del componente
const receptores = ref<Receptor[]>([]);
const loading = ref(false);
const saving = ref(false);
const error = ref<string | null>(null);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Cargar receptores cuando se abre el diálogo
watch(() => props.modelValue, (esVisible) => {
  if (esVisible) {
    cargarReceptores();
  } else {
    // Resetear estado al cerrar para la próxima vez
    receptores.value = [];
    error.value = null;
  }
});

async function cargarReceptores() {
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

    const response = await fetch(`${empresaActiva.url_servidor}/api/receptores/get_lista`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    receptores.value = data;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Error desconocido al cargar los receptores.';
    console.error('Error cargando receptores:', err);
  } finally {
    loading.value = false;
  }
}

async function guardarCambios() {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva?.uid || !empresaActiva?.url_servidor) {
    error.value = 'No hay una empresa activa configurada correctamente.';
    return;
  }

  saving.value = true;

  try {
    const listaParaGuardar = receptores.value.map(receptor => ({
      ID: receptor.ID,
      Activo: receptor.Activo
    }));

    const formData = new FormData();
    formData.append('uid', empresaActiva.uid);
    formData.append('lista', JSON.stringify(listaParaGuardar));

    const response = await fetch(`${empresaActiva.url_servidor}/api/receptores/set_settings`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status} ${response.statusText}`);
    }

    emit('cambiosGuardados');
    cerrarDialog();
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Error desconocido al guardar los cambios.';
    console.error('Error guardando cambios:', err);
  } finally {
    saving.value = false;
  }
}

function cerrarDialog() {
  isOpen.value = false;
}
</script>

<style scoped>
.receptores-dialog {
  overflow: hidden;
}

.receptores-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}

.receptor-row {
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
  cursor: pointer;
}

.receptor-row:hover {
  border-color: rgba(var(--v-theme-primary), 0.3);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.receptor-row.is-active {
  background-color: rgba(var(--v-theme-success), 0.08);
  border-color: rgba(var(--v-theme-success), 0.3);
}

.receptor-info {
  flex: 1;
  min-width: 0;
}

.receptor-nombre {
  font-size: 1.2rem;
  font-weight: 700;
  color: rgba(var(--v-theme-on-surface), 0.9);
  line-height: 1.3;
  margin-bottom: 6px;
}

.receptor-descripcion {
  font-size: 0.9rem;
  color: rgba(var(--v-theme-on-surface), 0.65);
  line-height: 1.2;
}

.receptor-switch {
  flex-shrink: 0;
  margin-left: 20px;
}

.elegant-switch {
  --v-input-control-height: 48px;
}

.elegant-switch :deep(.v-switch__track) {
  width: 64px;
  height: 34px;
  border-radius: 17px;
  opacity: 0.5;
  transition: background-color 0.3s ease;
}

.elegant-switch :deep(.v-switch__thumb) {
  width: 28px;
  height: 28px;
  border-radius: 14px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: transform 0.3s ease;
}

/* Botones de acción */
.action-button-small {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
}
</style>