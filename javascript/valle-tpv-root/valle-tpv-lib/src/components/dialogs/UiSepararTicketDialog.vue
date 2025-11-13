<template>
  <v-dialog v-model="isOpen" max-width="900px" persistent>
    <v-card elevation="16" rounded="xl" class="separar-ticket-dialog">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">
        Separar Ticket
      </v-card-title>

      <v-card-text class="pa-4">
        <v-row no-gutters>
          <v-col cols="12" md="6" class="pa-2">
            <div class="columna-ticket">
              <div class="ticket-header">
                <h3>Ticket Original</h3>
                <span class="total-display">{{ formatCurrency(totalOriginal) }}</span>
              </div>
              <v-divider class="mb-2"></v-divider>
              <v-list class="ticket-list">
                <v-list-item
                  v-for="item in ticketOriginal"
                  :key="item.descripcion"
                  @click="moverItem(item, 'aNuevo')"
                  class="touch-list-item"
                  lines="two"
                  elevation="2"
                >
                  <v-list-item-title class="item-title">
                    <span class="item-cantidad">{{ item.cantidad }}x</span> {{ item.descripcion }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ formatCurrency(item.precio) }} / ud.
                  </v-list-item-subtitle>
                  <template v-slot:append>
                    <span class="item-total">{{ formatCurrency(item.total) }}</span>
                  </template>
                </v-list-item>
              </v-list>
            </div>
          </v-col>

          <v-col cols="12" md="6" class="pa-2">
            <div class="columna-ticket">
              <div class="ticket-header">
                <h3>Nuevo Ticket</h3>
                <span class="total-display">{{ formatCurrency(totalNuevo) }}</span>
              </div>
              <v-divider class="mb-2"></v-divider>
              <v-list class="ticket-list">
                 <v-list-item
                  v-for="item in ticketNuevo"
                  :key="item.descripcion"
                  @click="moverItem(item, 'aOriginal')"
                  class="touch-list-item"
                  lines="two"
                  elevation="2"
                >
                  <v-list-item-title class="item-title">
                    <span class="item-cantidad">{{ item.cantidad }}x</span> {{ item.descripcion }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ formatCurrency(item.precio) }} / ud.
                  </v-list-item-subtitle>
                  <template v-slot:append>
                    <span class="item-total">{{ formatCurrency(item.total) }}</span>
                  </template>
                </v-list-item>
              </v-list>
            </div>
          </v-col>
        </v-row>
      </v-card-text>
      
      <v-card-actions class="pa-6">
        <v-row>
          <v-col cols="6">
            <v-btn 
              variant="tonal" 
              color="grey-darken-1" 
              @click="cerrarDialog" 
              block 
              class="action-button-small"
              height="72"
              rounded="16"
              elevation="3"
            >
              Cancelar
            </v-btn>
          </v-col>
          <v-col cols="6">
            <v-btn 
              color="success" 
              @click="confirmarSeparacion" 
              block 
              class="action-button-small" 
              :disabled="ticketNuevo.length === 0"
              height="72"
              rounded="16"
              elevation="3"
            >
              Confirmar
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
// --- SCRIPT ORIGINAL (EL QUE FUNCIONA) ---
import { ref, computed, watch } from 'vue';
import type { CuentaItem } from '../../models/cuenta';

const props = defineProps<{
  modelValue: boolean;
  items: CuentaItem[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'separacionConfirmada': [ itemsCobrados: CuentaItem[]];
}>();

const ticketOriginal = ref<CuentaItem[]>([]);
const ticketNuevo = ref<CuentaItem[]>([]);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

watch(() => props.modelValue, (esVisible) => {
  if (esVisible) {
    ticketOriginal.value = JSON.parse(JSON.stringify(props.items));
    ticketNuevo.value = [];
  }
});

const totalOriginal = computed(() => ticketOriginal.value.reduce((sum, item) => sum + item.total, 0));
const totalNuevo = computed(() => ticketNuevo.value.reduce((sum, item) => sum + item.total, 0));

function moverItem(itemSeleccionado: CuentaItem, direccion: 'aNuevo' | 'aOriginal') {
  const [listaOrigen, listaDestino] = direccion === 'aNuevo' 
    ? [ticketOriginal, ticketNuevo] 
    : [ticketNuevo, ticketOriginal];

  const itemOrigen = listaOrigen.value.find(i => i.descripcion === itemSeleccionado.descripcion);
  if (!itemOrigen) return;

  let itemDestino = listaDestino.value.find(i => i.descripcion === itemSeleccionado.descripcion);

  if (!itemDestino) {
    itemDestino = { ...itemOrigen, cantidad: 0, total: 0 };
    listaDestino.value.push(itemDestino);
  }
  
  itemOrigen.cantidad--;
  itemDestino.cantidad++;

  itemOrigen.total = itemOrigen.precio * itemOrigen.cantidad;
  itemDestino.total = itemDestino.precio * itemDestino.cantidad;

  if (itemOrigen.cantidad === 0) {
    listaOrigen.value = listaOrigen.value.filter(i => i.descripcion !== itemOrigen.descripcion);
  }
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
}

function cerrarDialog() {
  isOpen.value = false;
}

function confirmarSeparacion() {
  emit('separacionConfirmada', ticketNuevo.value);
  cerrarDialog();
}
</script>

<style scoped>
/* --- ESTILOS MEJORADOS PARA PANTALLA TÁCTIL --- */
.separar-ticket-dialog {
  overflow: hidden;
}

.columna-ticket {
  display: flex;
  flex-direction: column;
  height: 60vh;
  min-height: 400px;
  background-color: rgba(var(--v-theme-on-surface), 0.05);
  border-radius: 12px;
  padding: 16px;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px 8px 8px;
}

.ticket-header h3 {
  font-weight: 600;
}

.total-display {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--v-theme-primary);
}

.ticket-list {
  flex-grow: 1;
  overflow-y: auto;
  background-color: transparent;
  padding: 4px;
}

.touch-list-item {
  min-height: 72px;
  margin-bottom: 12px !important;
  background-color: var(--v-theme-surface);
  border-radius: 12px !important;
  cursor: pointer;
  transition: transform 0.1s ease-in-out, box-shadow 0.2s ease;
}
.touch-list-item:active {
  transform: scale(0.98);
}

.item-title {
  font-size: 1.1rem;
  font-weight: 500;
  white-space: normal;
  line-height: 1.2;
}
.item-cantidad {
  font-weight: 700;
  color: var(--v-theme-primary);
  margin-right: 8px;
}

.item-total {
  font-size: 1.1rem;
  font-weight: 700;
}

/* Botones de acción */
.action-button-small {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
}
</style>