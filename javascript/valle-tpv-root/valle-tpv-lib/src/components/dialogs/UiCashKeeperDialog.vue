<template>
  <v-dialog 
    v-model="isOpen" 
    persistent 
    max-width="800px"
    class="cash-keeper-dialog"
  >
    <v-card class="dialog-card elevation-12 rounded-xl">
      <!-- Título con estado integrado -->
      <v-card-title class="dialog-title bg-primary pa-4">
        <div class="title-container">
          <div class="title-main">
            <v-icon size="40" class="mr-3">mdi-cash-register</v-icon>
            <span class="dialog-title-text">CashKeeper</span>
          </div>
          <div class="status-bar">
            <v-chip 
              v-if="!isConnected" 
              color="error" 
              size="small" 
              variant="flat"
              class="status-chip"
            >
              <v-icon size="small" class="mr-1">mdi-wifi-off</v-icon>
              Desconectado
            </v-chip>
            <v-chip 
              v-else-if="hasError" 
              color="error" 
              size="small" 
              variant="flat"
              class="status-chip"
            >
              <v-icon size="small" class="mr-1">mdi-alert-circle</v-icon>
              {{ errorMessage }}
            </v-chip>
            <v-chip 
              v-else-if="operationStatus === 'dispensing'" 
              color="warning" 
              size="small" 
              variant="flat"
              class="status-chip status-chip-animated"
            >
              <v-progress-circular 
                indeterminate 
                size="16" 
                width="2" 
                class="mr-1"
              />
              Dispensando cambio...
            </v-chip>
            <v-chip 
              v-else-if="operationStatus === 'canceling'" 
              color="orange" 
              size="small" 
              variant="flat"
              class="status-chip status-chip-animated"
            >
              <v-progress-circular 
                indeterminate 
                size="16" 
                width="2" 
                class="mr-1"
              />
              Cancelando operación...
            </v-chip>
            <v-chip 
              v-else-if="operationStatus === 'paying'" 
              color="info" 
              size="small" 
              variant="flat"
              class="status-chip"
            >
              <v-icon size="small" class="mr-1">mdi-cash-clock</v-icon>
              Esperando efectivo...
            </v-chip>
            <v-chip 
              v-else-if="operationStatus === 'finished'" 
              color="success" 
              size="small" 
              variant="flat"
              class="status-chip"
            >
              <v-icon size="small" class="mr-1">mdi-check-circle</v-icon>
              Operación finalizada
            </v-chip>
            <v-chip 
              v-else 
              color="success" 
              size="small" 
              variant="flat"
              class="status-chip"
            >
              <v-icon size="small" class="mr-1">mdi-check-circle</v-icon>
              Conectado
            </v-chip>
          </div>
        </div>
      </v-card-title>

      <v-card-text class="dialog-content pa-6">
        <!-- Alerta grande de cambio insuficiente -->
        <v-alert
          v-if="!puedeDarCambio && cambio > 0"
          type="warning"
          prominent
          variant="tonal"
          class="mb-4 cambio-insuficiente-alert"
          border="start"
          border-color="warning"
        >
          <template v-slot:prepend>
            <v-icon size="48" color="warning">mdi-cash-remove</v-icon>
          </template>
          <div class="alert-title">⚠️ CAMBIO INSUFICIENTE</div>
          <div class="alert-message">{{ mensajeCambioInsuficiente }}</div>
          <div class="alert-suggestion">Por favor, introduzca el importe exacto o contacte con el administrador para recargar los recicladores.</div>
        </v-alert>

        <!-- Cantidades en una fila horizontal -->
        <div class="amounts-row">
          <div class="amount-box total-box">
            <div class="amount-label">Total</div>
            <div class="amount-value">{{ formatCurrency(totalACobrar) }}</div>
          </div>
          <div class="amount-box introducido-box">
            <div class="amount-label">Introducido</div>
            <div class="amount-value">{{ formatCurrency(totalIntroducido) }}</div>
          </div>
          <div class="amount-box cambio-box" :class="{ 'cambio-positivo': cambio > 0, 'cambio-error': !puedeDarCambio && cambio > 0 }">
            <div class="amount-label">Cambio</div>
            <div class="amount-value">{{ formatCurrency(cambio) }}</div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="dialog-actions pa-4">
        <v-row class="action-buttons-row" no-gutters>
          <v-col cols="6" class="pr-2">
            <v-btn
              size="x-large"
              color="error"
              variant="elevated"
              block
              class="action-button cancel-button"
              @click="cancelar"
              :disabled="isProcessingPayment"
              :loading="operationStatus === 'canceling'"
            >
              <v-icon size="48">mdi-close-circle</v-icon>
              <span class="button-text">Cancelar</span>
            </v-btn>
          </v-col>
          <v-col cols="6" class="pl-2">
            <v-btn
              size="x-large"
              color="success"
              variant="elevated"
              block
              class="action-button accept-button"
              @click="aceptar"
              :disabled="totalIntroducido < totalACobrar || (!puedeDarCambio && cambio > 0) || isProcessingPayment"
              :loading="isProcessingPayment"
            >
              <template v-if="operationStatus === 'dispensing'">
                <v-icon size="48">mdi-cash-fast</v-icon>
                <span class="button-text">Dispensando...</span>
              </template>
              <template v-else>
                <v-icon size="48">mdi-check-circle</v-icon>
                <span class="button-text">Aceptar Pago</span>
              </template>
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {  computed, onMounted, onUnmounted, ref } from 'vue';
import { useCashKeeperStore } from '../../store/cashKeeperStore';

interface Props {
  modelValue: boolean;
  totalCobro: number;
}

interface InfoCobro {
  tipo: 'efectivo' | 'tarjeta';
  totalEntregado: number;
  cambio: number;
  totalCobrado: number;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'cobro-completado', infoCobro: InfoCobro): void;
  (e: 'cobro-cancelado'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const cashKeeperStore = useCashKeeperStore();

// Ref local para bloquear botones inmediatamente al hacer clic
const isButtonClicked = ref(false);

// Ref para guardar el cambio original antes de que el store lo modifique al dispensar
const cambioOriginal = ref(0);

// Computed properties para el diálogo
const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

// Computed properties del store
const totalACobrar = computed(() => cashKeeperStore.totalCobro);
const totalIntroducido = computed(() => cashKeeperStore.totalAdmitido);
const cambio = computed(() => cashKeeperStore.cambio);
const isConnected = computed(() => cashKeeperStore.isConnected);
const hasError = computed(() => cashKeeperStore.hasError);
const errorMessage = computed(() => cashKeeperStore.errorMessage);
const operationStatus = computed(() => cashKeeperStore.operationStatus);
const puedeDarCambio = computed(() => cashKeeperStore.puedeDarCambio);
const mensajeCambioInsuficiente = computed(() => cashKeeperStore.mensajeCambioInsuficiente);

// Computed para saber si está procesando el pago (dispensando o cancelando)
const isProcessingPayment = computed(() => {
  return isButtonClicked.value || 
         operationStatus.value === 'dispensing' || 
         operationStatus.value === 'canceling' ||
         cashKeeperStore.isFinalizando;
});

// Métodos
const formatCurrency = (amount: number): string => {
  return `${amount.toFixed(2)} €`;
};

const clearError = () => {
  cashKeeperStore.hasError = false;
  cashKeeperStore.errorMessage = '';
};

const cancelar = () => {
  // Prevenir ejecución múltiple
  if (isButtonClicked.value || isProcessingPayment.value) {
    return;
  }
  
  // Bloquear botones inmediatamente
  isButtonClicked.value = true;
  
  cashKeeperStore.cancelarOperacion();
  // No cerramos aquí, esperamos a que operationStatus sea 'finished'
};

const aceptar = () => {
  // Prevenir ejecución múltiple
  if (isButtonClicked.value || isProcessingPayment.value) {
    return;
  }
  
  // Validar condiciones de pago
  if (totalIntroducido.value < totalACobrar.value) {
    return;
  }
  
  if (!puedeDarCambio.value && cambio.value > 0) {
    return;
  }
  
  // Bloquear botones inmediatamente
  isButtonClicked.value = true;
  
  // ⚠️ CRÍTICO: Guardar el cambio ANTES de finalizar el cobro
  // porque el store lo modificará al dispensar
  cambioOriginal.value = cashKeeperStore.cambio;
  
  // Solo permitir aceptar si el total introducido es mayor o igual que el total a cobrar
  if (totalIntroducido.value >= totalACobrar.value) {
    cashKeeperStore.finalizarCobro();
    // No cerramos aquí, esperamos a que operationStatus sea 'finished'
  }
};

// Inicializar cuando se abre el diálogo
const inicializar = () => {
  // Resetear el bloqueo de botones y el cambio original
  isButtonClicked.value = false;
  cambioOriginal.value = 0;
  
  if (props.modelValue && props.totalCobro > 0) {
    // Conectar al CashKeeper si no está conectado
    if (!isConnected.value) {
      cashKeeperStore.conectar();
    }
    
    // Secuencia: 1. Cargar denominaciones, 2. Iniciar cobro
    cashKeeperStore.cargarDenominaciones();
    
    // Pequeño delay para dar tiempo a cargar denominaciones antes de iniciar cobro
    setTimeout(() => {
      cashKeeperStore.iniciarCobro(props.totalCobro);
    }, 500);
  }
};

// Watchers
import { watch } from 'vue';

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    inicializar();
  }
});

watch(() => props.totalCobro, (newTotal) => {
  if (props.modelValue && newTotal > 0) {
    // Secuencia completa al cambiar el total
    cashKeeperStore.cargarDenominaciones();
    setTimeout(() => {
      cashKeeperStore.iniciarCobro(newTotal);
    }, 500);
  }
});

// Watcher para cerrar el diálogo cuando la operación termine
watch(() => operationStatus.value, (newStatus, oldStatus) => {
  if (newStatus === 'finished' && oldStatus !== 'finished') {
    // Resetear el bloqueo de botones
    isButtonClicked.value = false;
    
    // Verificar si hubo error o fue cancelación
    if (hasError.value || cashKeeperStore.successMessage.includes('cancelada')) {
      emit('cobro-cancelado');
      isOpen.value = false;
      return;
    }
    
    // Operación exitosa: crear InfoCobro con valores correctos
    // Usar cambioOriginal guardado antes de dispensar, ya que el store
    // resta el cambio del totalAdmitido después de dispensarlo
    const infoCobro: InfoCobro = {
      tipo: 'efectivo',
      totalEntregado: cashKeeperStore.totalAdmitido + cambioOriginal.value,
      cambio: cambioOriginal.value,
      totalCobrado: cashKeeperStore.totalCobro
    };
    
    emit('cobro-completado', infoCobro);
    isOpen.value = false;
  }
});

</script>

<style scoped>
.cash-keeper-dialog {
  z-index: 9999;
}

.dialog-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.dialog-title {
  color: white !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
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

.title-container {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.title-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-title-text {
  font-size: 2rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.status-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-chip {
  font-size: 0.9rem !important;
  font-weight: 500 !important;
  padding: 4px 12px !important;
  height: 28px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(5px) !important;
  transition: all 0.3s ease;
}

.status-chip-animated {
  animation: pulse-status 1.5s infinite;
}

@keyframes pulse-status {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

.dialog-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.amounts-row {
  display: flex;
  gap: 16px;
  justify-content: space-around;
  margin-bottom: 16px;
}

.amount-box {
  flex: 1;
  text-align: center;
  padding: 20px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.total-box {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-color: rgba(102, 126, 234, 0.3);
}

.introducido-box {
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.1), rgba(41, 128, 185, 0.1));
  border-color: rgba(52, 152, 219, 0.3);
}

.cambio-box {
  background: linear-gradient(135deg, rgba(149, 165, 166, 0.1), rgba(127, 140, 141, 0.1));
  border-color: rgba(149, 165, 166, 0.3);
}

.cambio-positivo {
  background: linear-gradient(135deg, rgba(46, 204, 113, 0.15), rgba(39, 174, 96, 0.15)) !important;
  border-color: rgba(46, 204, 113, 0.4) !important;
  box-shadow: 0 4px 16px rgba(46, 204, 113, 0.2) !important;
}

.cambio-error {
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.15), rgba(192, 57, 43, 0.15)) !important;
  border-color: rgba(231, 76, 60, 0.5) !important;
  box-shadow: 0 4px 16px rgba(231, 76, 60, 0.3) !important;
  animation: pulse-error 2s infinite;
}

@keyframes pulse-error {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(231, 76, 60, 0.3);
  }
  50% {
    box-shadow: 0 4px 20px rgba(231, 76, 60, 0.5);
  }
}

.cambio-insuficiente-alert {
  border-left-width: 6px !important;
  font-size: 1.1rem;
  border-radius: 12px !important;
}

.alert-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #f57c00;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.alert-message {
  font-size: 1.3rem;
  font-weight: 600;
  color: #e65100;
  margin-bottom: 10px;
  line-height: 1.5;
}

.alert-suggestion {
  font-size: 1.1rem;
  font-weight: 500;
  color: #666;
  line-height: 1.4;
  margin-top: 8px;
  font-style: italic;
}

.amount-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.amount-value {
  font-size: 2.8rem;
  font-weight: 700;
  font-family: 'Roboto Mono', monospace;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
  line-height: 1.1;
  color: #333;
}

.dialog-actions {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  border-radius: 0 0 16px 16px;
}

.action-buttons-row {
  gap: 0;
}

.action-button {
  height: 120px !important;
  border-radius: 16px !important;
  font-size: 1.6rem !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: 1.5px !important;
  box-shadow: 0 8px 20px rgba(0,0,0,0.25) !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
  padding: 24px !important;
}

.action-button:not(:disabled):hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(0,0,0,0.35) !important;
}

.action-button:not(:disabled):active {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3) !important;
}

.action-button:disabled {
  opacity: 0.4 !important;
  cursor: not-allowed !important;
}

.cancel-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%) !important;
}

.cancel-button:not(:disabled):hover {
  background: linear-gradient(135deg, #ff5252 0%, #d84315 100%) !important;
}

.accept-button {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%) !important;
}

.accept-button:not(:disabled):hover {
  background: linear-gradient(135deg, #1db954 0%, #1e8449 100%) !important;
}

.button-text {
  font-size: 1.3rem;
  line-height: 1.2;
}
</style>