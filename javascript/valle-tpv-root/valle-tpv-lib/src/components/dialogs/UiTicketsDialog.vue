<template>
  <v-dialog v-model="isOpen" max-width="800px" persistent>
    <v-card elevation="16" rounded="xl" class="tickets-dialog">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant">
        <v-icon class="mr-3">mdi-receipt</v-icon>
        {{ vistaTicket ? 'Detalle del Ticket' : 'Lista de Tickets' }}
      </v-card-title>

      <v-card-text class="pa-4">
        <div v-if="!vistaTicket">
          <div v-if="loading" class="text-center pa-6">
            <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
            <p class="mt-4">Cargando tickets...</p>
          </div>

          <div v-else-if="error" class="text-center pa-6">
            <v-icon color="error" size="48" class="mb-4">mdi-alert-circle</v-icon>
            <p class="text-error">{{ error }}</p>
          </div>

          <div v-else>
            <p class="text-medium-emphasis mb-4">Selecciona un ticket para ver su detalle:</p>
            
            <v-list class="mb-6 tickets-list" dense>
              <v-list-item
                v-for="ticket in tickets"
                :key="ticket.ID"
                class="ticket-item pa-4 mb-3"
                @click="verTicket(ticket)"
              >
                <template v-slot:prepend>
                  <v-avatar color="primary" size="40">
                    <v-icon color="white">mdi-receipt-text</v-icon>
                  </v-avatar>
                </template>
                
                <v-list-item-title class="font-weight-bold text-h5 mb-1">
                  Mesa {{ ticket.Mesa || 'Sin mesa' }}
                </v-list-item-title>
                
                <v-list-item-subtitle class="mt-1">
                  <div class="d-flex align-center mb-1">
                    <v-icon size="18" class="mr-2">mdi-clock-outline</v-icon>
                    <span class="text-h6 font-weight-medium">{{ ticket.Hora }}</span>
                    <span class="ml-2 text-medium-emphasis">{{ ticket.Fecha }}</span>
                  </div>
                  <div class="d-flex align-center mb-1">
                    <v-icon size="16" class="mr-2" :color="ticket.Entrega == 0 ? 'blue' : 'green'">
                      {{ ticket.Entrega == 0 ? 'mdi-credit-card' : 'mdi-cash' }}
                    </v-icon>
                    {{ ticket.Entrega == 0 ? 'Tarjeta' : `Efectivo: ${formatearPrecio(ticket.Entrega)}€` }}
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="14" class="mr-2">mdi-receipt-text-outline</v-icon>
                    <span class="text-caption">Ticket #{{ ticket.ID }}</span>
                  </div>
                </v-list-item-subtitle>

                <template v-slot:append>
                  <div class="text-right">
                    <div class="text-h6 font-weight-bold text-primary">
                      {{ formatearPrecio(ticket.Total) }}€
                    </div>
                    <v-icon color="grey-lighten-1">mdi-chevron-right</v-icon>
                  </div>
                </template>
              </v-list-item>
            </v-list>
          </div>
        </div>

        <div v-else-if="ticketSeleccionado">
          <div class="ticket-header mb-4 pa-4 bg-surface-variant rounded-lg">
            <div class="d-flex justify-space-between align-center mb-2">
              <h3 class="text-h5 font-weight-bold">Mesa {{ ticketSeleccionado.Mesa || 'Sin mesa' }}</h3>
              <div class="text-h6 font-weight-bold text-primary">
                {{ formatearPrecio(ticketSeleccionado.Total) }}€
              </div>
            </div>
            <div class="d-flex align-center text-medium-emphasis">
              <v-icon size="18" class="mr-2">mdi-clock-outline</v-icon>
              <span class="text-h6 font-weight-medium mr-3">{{ ticketSeleccionado.Hora }}</span>
              <span class="mr-3">{{ ticketSeleccionado.Fecha }}</span>
              <v-divider vertical class="mx-3"></v-divider>
              <v-icon size="16" class="mr-2" :color="ticketSeleccionado.Entrega === 0 ? 'blue' : 'green'">
                {{ ticketSeleccionado.Entrega === 0 ? 'mdi-credit-card' : 'mdi-cash' }}
              </v-icon>
              {{ ticketSeleccionado.Entrega === 0 ? 'Tarjeta' : `Efectivo: ${formatearPrecio(ticketSeleccionado.Entrega)}€` }}
              <v-divider vertical class="mx-3"></v-divider>
              <v-icon size="14" class="mr-2">mdi-receipt-text-outline</v-icon>
              <span class="text-caption">Ticket #{{ ticketSeleccionado.ID }}</span>
            </div>
          </div>

          <div v-if="loadingDetalle" class="text-center pa-6">
            <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
            <p class="mt-4">Cargando detalle del ticket...</p>
          </div>

          <div v-else-if="errorDetalle" class="text-center pa-6">
            <v-icon color="error" size="48" class="mb-4">mdi-alert-circle</v-icon>
            <p class="text-error">{{ errorDetalle }}</p>
          </div>

          <div v-else>
            <h4 class="text-subtitle-1 font-weight-bold mb-3">
              <v-icon class="mr-2">mdi-format-list-bulleted</v-icon>
              Líneas del ticket
            </h4>
            
            <v-table class="ticket-detalle-table mb-4" density="compact">
              <thead>
                <tr>
                  <th class="text-left font-weight-bold">Cant.</th>
                  <th class="text-left font-weight-bold">Descripción</th>
                  <th class="text-right font-weight-bold">P. Unit.</th>
                  <th class="text-right font-weight-bold">Total</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="linea in detalleTicket" :key="linea.idArt" class="ticket-linea">
                  <td class="text-center font-weight-medium">{{ linea.Can }}</td>
                  <td class="font-weight-medium">{{ linea.Nombre }}</td>
                  <td class="text-right">{{ formatearPrecio(linea.Precio) }}€</td>
                  <td class="text-right font-weight-bold">{{ formatearPrecio(linea.Total) }}€</td>
                </tr>
              </tbody>
            </v-table>

            <v-divider class="mb-4"></v-divider>
            
            <div class="total-section text-right">
              <div class="text-h6 font-weight-bold ">
                Total: {{ ticketSeleccionado ? formatearPrecio(ticketSeleccionado.Total) : '0.00' }}€
              </div>
            </div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions >
        <div v-if="!vistaTicket" class="w-100">
          <v-btn 
            variant="tonal" 
            color="grey-darken-1" 
            @click="cerrarDialog" 
            block 
            class="action-button"
            :disabled="loading"
            prepend-icon="mdi-close"
            rounded="xl"
          >
            Cerrar
          </v-btn>
        </div>

        <div v-else class="w-100">
          <v-row no-gutters>
            <v-col cols="4" class="pr-1">
              <v-btn 
                variant="tonal" 
                color="grey-darken-1" 
                @click="volverALista" 
                block 
                class="action-button-small"
                prepend-icon="mdi-arrow-left"
                rounded="lg" 
              >
                Volver
              </v-btn>
            </v-col>
            <v-col cols="4" class="px-1">
              <v-btn 
                variant="elevated" 
                color="success" 
                @click="imprimirTicket" 
                block 
                class="action-button-small"
                prepend-icon="mdi-printer"
                :disabled="!!(loadingDetalle || errorDetalle)"
                rounded="lg"
              >
                Imprimir
              </v-btn>
            </v-col>
            <v-col cols="4" class="pl-1">
              <v-btn 
                variant="elevated"
                color="info" 
                @click="imprimirFactura" 
                block 
                class="action-button-small"
                prepend-icon="mdi-file-document-outline"
                :disabled="!!(loadingDetalle || errorDetalle)"
                rounded="lg"
              >
                Factura
              </v-btn>
            </v-col>
          </v-row>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>


<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useEmpresasStore } from '../../store/dbStore/empresasStore';

interface Ticket {
  ID: number;
  Fecha: string;
  Hora: string;
  Entrega: number;
  Mesa: string;
  Total: number;
}

interface LineaTicket {
  idArt: number;
  Can: number;
  Nombre: string;
  Precio: number;
  Total: number;
}

type Props = {
  modelValue: boolean;
};

const props = defineProps<Props>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

const empresasStore = useEmpresasStore();

// Estado del componente
const tickets = ref<Ticket[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// Estado para vista de detalle
const vistaTicket = ref(false);
const ticketSeleccionado = ref<Ticket | null>(null);
const detalleTicket = ref<LineaTicket[]>([]);
const loadingDetalle = ref(false);
const errorDetalle = ref<string | null>(null);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Cargar tickets cuando se abre el diálogo
watch(() => props.modelValue, async (esVisible) => {
  if (esVisible) {
    await cargarTickets();
  } else {
    // Resetear estado al cerrar
    resetearEstado();
  }
});

function resetearEstado() {
  tickets.value = [];
  error.value = null;
  vistaTicket.value = false;
  ticketSeleccionado.value = null;
  detalleTicket.value = [];
  errorDetalle.value = null;
}

async function cargarTickets() {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva) {
    error.value = 'No hay empresa activa seleccionada';
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const formData = new FormData();
    formData.append('uid', empresaActiva.uid);

    const response = await fetch(`${empresaActiva.url_servidor}/api/cuenta/lsticket`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status}`);
    }

    const data = await response.json();
    tickets.value = data;
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Error al cargar los tickets';
    console.error('Error cargando tickets:', err);
  } finally {
    loading.value = false;
  }
}

async function verTicket(ticket: Ticket) {
  ticketSeleccionado.value = ticket;
  vistaTicket.value = true;
  await cargarDetalleTicket(ticket.ID);
}

async function cargarDetalleTicket(ticketId: number) {
  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva) {
    errorDetalle.value = 'No hay empresa activa seleccionada';
    return;
  }

  loadingDetalle.value = true;
  errorDetalle.value = null;

  try {
    const formData = new FormData();
    formData.append('uid', empresaActiva.uid);
    formData.append('id', ticketId.toString());

    const response = await fetch(`${empresaActiva.url_servidor}/api/cuenta/lslineas`, {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      throw new Error(`Error del servidor: ${response.status}`);
    }

    const data = await response.json();
    detalleTicket.value = data.lineas;
  } catch (err) {
    errorDetalle.value = err instanceof Error ? err.message : 'Error al cargar el detalle del ticket';
    console.error('Error cargando detalle del ticket:', err);
  } finally {
    loadingDetalle.value = false;
  }
}

function volverALista() {
  vistaTicket.value = false;
  ticketSeleccionado.value = null;
  detalleTicket.value = [];
  errorDetalle.value = null;
}

async function imprimirTicket() {
  if (!ticketSeleccionado.value) return;

  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva) {
    console.error('No hay empresa activa');
    return;
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('id', ticketSeleccionado.value.ID.toString());
  formData.append("receptor_activo", "True");
  formData.append("abrircajon", "False");
  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/impresion/imprimir_ticket`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      cerrarDialog();
    } else {
      console.error('Error al imprimir ticket:', response.statusText);
    }
  } catch (error) {
    console.error('Error en la petición de impresión de ticket:', error);
  }
}

async function imprimirFactura() {
  if (!ticketSeleccionado.value) return;

  const empresaActiva = empresasStore.empresaActiva;
  if (!empresaActiva) {
    console.error('No hay empresa activa');
    return;
  }

  const formData = new FormData();
  formData.append('uid', empresaActiva.uid);
  formData.append('id', ticketSeleccionado.value.ID.toString());
  formData.append("receptor_activo", "True");
  formData.append("abrircajon", "False");
  try {
    const response = await fetch(`${empresaActiva.url_servidor}/api/impresion/imprimir_factura`, {
      method: 'POST',
      body: formData
    });

    if (response.ok) {
      cerrarDialog();
    } else {
      console.error('Error al imprimir factura:', response.statusText);
    }
  } catch (error) {
    console.error('Error en la petición de impresión de factura:', error);
  }
}

function formatearPrecio(precio: number): string {
    if (isNaN(precio)) {
        return '0.00';
    }
    return precio.toFixed(2);
}

function cerrarDialog() {
  isOpen.value = false;
}

</script>

<style scoped>
/* Layout principal del diálogo con Flexbox */
.tickets-dialog {
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.v-card-text {
  flex: 1 1 auto;
  overflow-y: auto;
}

/* Regla para arreglar el texto de la cabecera */
.ticket-header .text-medium-emphasis {
  color: rgba(255, 255, 255, 0.85) !important;
}
.ticket-header .text-medium-emphasis .v-icon {
  color: rgba(255, 255, 255, 0.85) !important;
}

/* Estilos originales */
.ticket-item {
  background-color: rgba(var(--v-theme-on-surface), 0.05) !important;
  border-radius: 20px !important;
  border: 100px solid rgba(var(--v-theme-outline), 0.2) !important;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ticket-item:hover {
  background-color: rgba(var(--v-theme-primary), 1);
  border-color: rgba(var(--v-theme-primary), 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.ticket-header {
  border-radius: 8px;
  border: 4px solid rgb(var(--v-theme-primary));
}

.ticket-detalle-table {
  background-color: rgba(var(--v-theme-on-surface), 0.02);
  border-radius: 8px;
  overflow: hidden;
}

.ticket-linea:nth-child(even) {
  background-color: rgba(var(--v-theme-on-surface), 0.03);
}

.total-section {
  padding: 16px;
  background-color: rgba(var(--v-theme-primary), 0.05);
  border-radius: 8px;
  border: 4px solid rgb(var(--v-theme-primary), 0.15);
}

.action-button {
  height: 80px !important;
  font-size: 1.2rem;
  border-radius: 16px !important;
  text-transform: none;
  font-weight: 600;
}

/* --- CAMBIOS AQUÍ --- */
.action-button-small {
  height: 72px !important;          /* AUMENTADO: La altura del botón */
  font-size: 1.1rem;                 /* AUMENTADO: El tamaño de la letra */
  border-radius: 16px !important;    /* AUMENTADO: El redondeo de las esquinas */
  text-transform: none;
  font-weight: 600;
}

.w-100 {
  width: 100%;
}
</style>