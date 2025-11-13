<template>
  <v-dialog v-model="isOpen" max-width="600px" persistent>
    <v-card elevation="16" rounded="xl" class="anulacion-dialog">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">
        <v-icon class="mr-3">mdi-delete</v-icon>
        Motivo de la Anulación
      </v-card-title>

      <v-card-text class="pa-4">
        <p class="text-medium-emphasis mb-4">Se anularán los siguientes productos:</p>
        <v-list class="mb-6 item-list" dense>
          <v-list-item-title v-if="props.item">
              <span  class="font-weight-bold">{{ props.item.cantidad }}x</span> {{ props.item.descripcion }}
            </v-list-item-title>
            <v-list-item-title v-if="props.nombre_mesa">
              <span v-if="!props.item"  class="font-weight-bold">Borrado de la mesa completa: </span>
              <span v-else class="font-weight-bold"> Borrado parcial de la mesa: </span>{{ props.nombre_mesa }}
            </v-list-item-title>
        </v-list>

        <div v-if="!modoEdicion">
          <v-row>
            <v-col cols="6">
              <v-btn 
                class="motivo-btn" 
                color="error" 
                @click="confirmarAnulacion('Error')"
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
              >
                Error
              </v-btn>
            </v-col>
            <v-col cols="6">
              <v-btn 
                class="motivo-btn" 
                color="orange-darken-2" 
                @click="confirmarAnulacion('Simpa')"
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
              >
                Simpa
              </v-btn>
            </v-col>
            <v-col cols="6">
              <v-btn 
                class="motivo-btn" 
                color="info" 
                @click="confirmarAnulacion('Invitación')"
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
              >
                Invitación
              </v-btn>
            </v-col>
            <v-col cols="6">
              <v-btn 
                class="motivo-btn" 
                variant="tonal" 
                @click="modoEdicion = true"
                size="large"
                height="72"
                elevation="2"
                rounded="lg"
              >
                Editar
              </v-btn>
            </v-col>
          </v-row>
        </div>

        <div v-else>
          <v-textarea
            v-model="motivoEditado"
            label="Escribe el motivo..."
            variant="outlined"
            rows="3"
            autofocus
            class="motivo-textarea"
          ></v-textarea>
          <v-btn 
            class="motivo-btn" 
            color="success" 
            @click="confirmarAnulacion(motivoEditado)"
            :disabled="!motivoEditado.trim()"
            size="large"
            height="72"
            elevation="2"
            rounded="lg"
          >
            Aceptar
          </v-btn>
        </div>
      </v-card-text>

      <v-card-actions class="pa-6">
        <slot name="extra-actions">
          <v-btn 
            variant="tonal" 
            color="grey-darken-1" 
            @click="cerrarDialog" 
            block 
            size="large"
            height="72"
            elevation="2"
            rounded="lg"
            class="action-button"
          >
            Cancelar
          </v-btn>
        </slot>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { CuentaItem }  from '../../models/cuenta';


type Props = {
  modelValue: boolean;
  item: CuentaItem | null;
  nombre_mesa: string | null;
};

const props = defineProps<Props>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'anulacionConfirmada': [motivo: string];
}>();

// Estado para cambiar entre la vista de botones y la de edición
const modoEdicion = ref(false);
// Estado para guardar el motivo personalizado
const motivoEditado = ref('');

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Resetea el estado del diálogo cada vez que se abre
watch(() => props.modelValue, (esVisible) => {
  if (esVisible) {
    modoEdicion.value = false;
    motivoEditado.value = '';
  }
});

function confirmarAnulacion(motivo: string) {
  // Nos aseguramos de no enviar un motivo vacío
  if (!motivo || !motivo.trim()) return;
  const motivo_final: string = (motivo == "Error") ? "Error de pedido" : motivo;
  emit('anulacionConfirmada', motivo_final.trim());
  cerrarDialog();
}

function cerrarDialog() {
  isOpen.value = false;
}
</script>

<style scoped>
.anulacion-dialog {
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.item-list {
  background-color: rgba(var(--v-theme-on-surface), 0.05);
  border-radius: 8px;
  padding: 4px 16px !important;
}

.motivo-btn {
  height: 72px !important;
  width: 100%;
  font-size: 1.1rem;
  border-radius: 16px !important;
  text-transform: none;
  font-weight: 600;
}

.motivo-textarea {
  margin-bottom: 16px;
}

.action-button {
  height: 72px !important;
  font-size: 1.1rem;
  border-radius: 16px !important;
  text-transform: none;
  font-weight: 600;
}
</style>