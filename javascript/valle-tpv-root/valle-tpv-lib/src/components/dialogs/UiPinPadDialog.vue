<template>
  <v-dialog 
    v-model="isOpen" 
    persistent 
    max-width="600px"
    class="pinpad-dialog"
  >
    <v-card class="dialog-card elevation-12 rounded-xl">
      <v-card-title class="dialog-title bg-info text-center pa-6">
        <div class="title-content">
          <v-icon size="48" class="mr-4">mdi-credit-card</v-icon>
          <span class="dialog-title-text">PinPad - Pago con Tarjeta</span>
        </div>
      </v-card-title>

      <v-card-text class="dialog-content pa-8">
        <v-container fluid>
          <!-- Mensaje de estado grande -->
          <v-row>
            <v-col cols="12" class="text-center">
              <div class="estado-mensaje" :class="getEstadoClass()">
                {{ mensajeEstado }}
              </div>
            </v-col>
          </v-row>

          <!-- Spinner para estados de procesamiento -->
          <v-row v-if="estaProcesando" class="mt-4">
            <v-col cols="12" class="text-center">
              <v-progress-circular 
                indeterminate 
                color="info" 
                size="80"
              ></v-progress-circular>
            </v-col>
          </v-row>

          <!-- Total a cobrar (solo mostrar cuando está cobrando) -->
          <v-row v-if="pinPadStore.estado === 'cobrando'" class="mt-6">
            <v-col cols="12" class="text-center">
              <div class="total-label">Total a cobrar</div>
              <div class="total-value">{{ formatCurrency(totalACobrar) }}</div>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <v-card-actions class="dialog-actions pa-6">
        <v-container fluid>
          <v-row justify="center">
            <v-col cols="12">
              <v-btn
                size="x-large"
                color="error"
                variant="elevated"
                block
                class="action-button cancel-button"
                @click="cancelar"
                :disabled="cancelando"
              >
                <v-icon left size="32">mdi-close-circle</v-icon>
                <span class="button-text">CANCELAR</span>
              </v-btn>
            </v-col>
          </v-row>
        </v-container>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { usePinPadStore } from '../../store/pinPadStore';

interface Props {
  modelValue: boolean;
  totalCobro: number;
  mesaId: string;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'pago-completado', recibo: any): void;
  (e: 'pago-cancelado'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const pinPadStore = usePinPadStore();
const cancelando = ref(false);
const mensajeCancelando = ref('');

// Computed properties para el diálogo
const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

// Computed properties del store
const totalACobrar = computed(() => pinPadStore.totalACobrar);
const mensajeEstado = computed(() => {
  // Si estamos cancelando, mostrar mensaje de cancelación
  if (cancelando.value) {
    return mensajeCancelando.value;
  }
  
  const mensaje = pinPadStore.mensajeEstado;
  if (pinPadStore.error) {
    return `${mensaje}\n${pinPadStore.error}`;
  }
  return mensaje;
});
const estaProcesando = computed(() => cancelando.value || pinPadStore.estaProcesandoCobro);

// Métodos
const formatCurrency = (amount: number): string => {
  return `${amount.toFixed(2)} €`;
};

const getEstadoClass = (): string => {
  // Si estamos cancelando, mostrar clase de cancelación
  if (cancelando.value) return 'estado-cancelando';
  
  const estado = pinPadStore.estado;
  
  if (estado === 'cobro_aceptado') return 'estado-exito';
  if (estado === 'cobro_denegado' || estado === 'error') return 'estado-error';
  if (estado === 'cobro_cancelado') return 'estado-cancelado';
  if (pinPadStore.reconectandoAutomaticamente) return 'estado-reconectando';
  if (estaProcesando.value || estado === 'cobrando') return 'estado-procesando';
  
  return 'estado-normal';
};

const cancelar = async () => {
  // Marcar como cancelando y mostrar mensaje
  cancelando.value = true;
  mensajeCancelando.value = 'Cancelando operación...';
  
  // Si hay un cobro en proceso, cancelarlo
  if (pinPadStore.estaProcesandoCobro) {
    pinPadStore.cancelarCobro();
  }
  
  // Esperar 3 segundos antes de cerrar
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  // Limpiar completamente el estado del store
  pinPadStore.limpiarEstado();
  
  // Emitir evento de pago cancelado
  emit('pago-cancelado');
  
  // Cerrar el diálogo
  isOpen.value = false;
  
  // Resetear estado de cancelación
  setTimeout(() => {
    cancelando.value = false;
    mensajeCancelando.value = '';
  }, 300);
};

// Inicializar cuando se abre el diálogo
const inicializar = async () => {
  if (props.modelValue) {
    pinPadStore.conectar_ws();
    
    // Esperar a que se conecte y luego iniciar cobro automáticamente
    setTimeout(() => {
      pinPadStore.iniciarCobro(props.totalCobro, props.mesaId);
    }, 1500);
  }
};

// Watchers
watch(() => props.modelValue, (newValue, oldValue) => {
  if (newValue && !oldValue) {
    // Se está abriendo el diálogo
    inicializar();
  } else if (!newValue && oldValue) {
    // Se está cerrando el diálogo
    pinPadStore.desconectar_ws();
  }
});

// Watch para cerrar automáticamente cuando el cobro es aceptado
watch(() => pinPadStore.estado, (nuevoEstado, estadoAnterior) => {
  
  if (nuevoEstado === 'cobro_aceptado') {
   
    // Esperar un momento para que el usuario vea el mensaje de éxito
    setTimeout(() => {
      // Finalizar la transacción exitosa y obtener el recibo
      const recibo = pinPadStore.finalizarTransaccionExitosa();
      
      emit('pago-completado', recibo);
      isOpen.value = false;
    }, 1500);
  }
});

onMounted(() => {
  if (props.modelValue) {
    inicializar();
  }
});
</script>

<style scoped>
.pinpad-dialog {
  z-index: 9999;
}

.dialog-card {
  background: linear-gradient(135deg, #f0f2f5 0%, #d6e7ff 100%);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.dialog-title {
  color: white !important;
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%) !important;
  border-radius: 16px 16px 0 0;
  position: relative;
  overflow: hidden;
}

.dialog-title::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.1) 0%, transparent 100%);
}

.title-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-title-text {
  font-size: 2.2rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.dialog-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  min-height: 250px;
  display: flex;
  align-items: center;
}

/* Mensaje de estado grande */
.estado-mensaje {
  font-size: 2.5rem;
  font-weight: 600;
  padding: 32px 24px;
  border-radius: 16px;
  text-align: center;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.estado-normal {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
}

.estado-procesando {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
  color: #f57c00;
}

.estado-exito {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.estado-error {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  color: #c62828;
}

.estado-cancelado {
  background: linear-gradient(135deg, #fafafa 0%, #eeeeee 100%);
  color: #616161;
}

.estado-cancelando {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #f57c00;
  animation: pulseCancelar 1.5s infinite;
}

@keyframes pulseCancelar {
  0%, 100% { 
    opacity: 1;
    transform: scale(1);
  }
  50% { 
    opacity: 0.8;
    transform: scale(0.98);
  }
}

.estado-reconectando {
  background: linear-gradient(135deg, #fff3e0 0%, #ffcc80 100%);
  color: #e65100;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* Total a cobrar */
.total-label {
  font-size: 1.4rem;
  font-weight: 500;
  color: #666;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.total-value {
  font-size: 3rem;
  font-weight: 700;
  color: #1976d2;
  font-family: 'Roboto Mono', monospace;
}

.dialog-actions {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  border-radius: 0 0 16px 16px;
}

/* Botón de cancelar grande y táctil */
.action-button {
  height: 80px !important;
  border-radius: 16px !important;
  font-size: 1.6rem !important;
  font-weight: 600 !important;
  text-transform: uppercase !important;
  letter-spacing: 2px !important;
  box-shadow: 0 8px 16px rgba(0,0,0,0.2) !important;
  transition: all 0.3s ease !important;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3) !important;
}

.action-button:active {
  transform: translateY(0);
}

.cancel-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%) !important;
}

.button-text {
  margin-left: 12px;
  font-size: 1.4rem;
  font-weight: 700;
}
</style>