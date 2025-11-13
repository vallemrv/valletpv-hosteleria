<template>
  <v-dialog v-model="isOpen" max-width="800px" persistent>
    <v-card elevation="16" rounded="xl" class="cobro-dialog-card">
      <v-card-title class="text-h5 font-weight-bold bg-surface-variant titulo-cobro">
        Confirmar Cobro
      </v-card-title>
      <v-card-text class="contenido-cobro">
        <v-row no-gutters class="dialog-row">
          <v-col cols="12" :md="mostrarTeclado ? 6 : 12" class="columna-info">
            <div class="info-wrapper">
              <div class="display-container" :class="{ 'sin-teclado': !mostrarTeclado }">
                <div class="display-item total-cobrar">
                  <span class="label">Total a Cobrar</span>
                  <span class="amount">{{ formatCurrency(totalVenta) }}</span>
                </div>
                <div v-if="mostrarTeclado" class="display-item efectivo-entregado">
                  <span class="label">Efectivo Entregado</span>
                  <span class="amount">{{ formatCurrency(efectivoParseado) }}</span>
                </div>
                <div 
                  v-if="mostrarTeclado"
                  class="display-item cambio-devolver" 
                  :class="{ 'falta-dinero': cambio < 0 }"
                >
                  <span class="label">{{ cambio < 0 ? 'Falta' : 'Cambio' }}</span>
                  <span class="amount">{{ formatCurrency(Math.abs(cambio) || 0) }}</span>
                </div>
              </div>

              <div class="acciones-principales" :class="{ 'sin-teclado': !mostrarTeclado }">
                <v-btn
                  class="accion-principal-btn"
                  color="success"
                  :disabled="!puedeConfirmarEfectivo"
                  @click="pagarConEfectivo()"
                >
                  <v-icon size="x-large">mdi-cash</v-icon>
                  <span>Efectivo</span>
                </v-btn>
                <v-btn class="accion-principal-btn" color="info" @click="pagarConTarjeta()">
                  <v-icon size="x-large">mdi-credit-card</v-icon>
                  <span>Tarjeta</span>
                </v-btn>
                <v-btn class="accion-principal-btn wide" color="grey-darken-1" variant="tonal" @click="cerrarDialog()">
                  <v-icon size="x-large">mdi-close-circle-outline</v-icon>
                  <span>Salir</span>
                </v-btn>
              </div>
            </div>
          </v-col>

          <v-col v-if="mostrarTeclado" cols="12" md="6" class="columna-teclado">
            <div class="teclado-wrapper">
              <div class="efectivo-display">
                <input
                  :value="formatCurrency(efectivoParseado)"
                  type="text"
                  class="efectivo-input"
                  readonly
                />
              </div>
              <div class="teclado-grid">
                <v-btn v-for="num in [1, 2, 3, 4, 5, 6, 7, 8, 9, 0, '.']" :key="num" class="tecla-num" @click="num === '.' ? agregarPunto() : agregarDigito(Number(num))">
                  {{ num }}
                </v-btn>
                <v-btn class="tecla-num" @click="borrarDigito()">
                  <v-icon>mdi-backspace-outline</v-icon>
                </v-btn>
              </div>
              <v-btn block color="error" class="restaurar-btn" @click="borrarTodo()">
                Restaurar
              </v-btn>
            </div>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import type { CuentaItem, InfoCobro } from '../../models/cuenta';
import { ref, computed, watch } from 'vue';

const props = defineProps<{
  modelValue: boolean;
  items: CuentaItem[];
  mostrarTeclado?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'pagoConfirmado': [infoCobro: InfoCobro, itemsCobrados: CuentaItem[]];
}>();

const totalVenta = computed(() => {
  return props.items.reduce((sum, item) => sum + item.total, 0);
});

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const efectivoRecibido = ref('');


const efectivoParseado = computed(() => {
    if (efectivoRecibido.value === '') return totalVenta.value;
    const valorNumerico = parseFloat(efectivoRecibido.value);
    return isNaN(valorNumerico) ? 0 : valorNumerico;
});

const cambio = computed(() => {
  return efectivoParseado.value - totalVenta.value;
});

const puedeConfirmarEfectivo = computed(() => {
  // Convertir a céntimos para evitar problemas de coma flotante
  const efectivoEnCentimos = Math.round(efectivoParseado.value * 100);
  const totalEnCentimos = Math.round(totalVenta.value * 100);
  return efectivoEnCentimos >= totalEnCentimos;
});

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    efectivoRecibido.value = '';
  }
});

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);
}

function agregarDigito(digito: number) {

  if (efectivoRecibido.value.includes('.') && efectivoRecibido.value.split('.')[1]?.length >= 2) return;
  if (efectivoRecibido.value.length >= 8) return;
  efectivoRecibido.value += digito.toString();
}

function agregarPunto() {
  if (efectivoRecibido.value.includes('.') || efectivoRecibido.value.length >= 8) return;
  efectivoRecibido.value = efectivoRecibido.value === '' ? '0.' : efectivoRecibido.value + '.';
}

function borrarDigito() {
  efectivoRecibido.value = efectivoRecibido.value.slice(0, -1);
}

function borrarTodo() {
  efectivoRecibido.value = '';
}

function pagarConEfectivo() {
  const infoCobro: InfoCobro = {
    tipo: 'efectivo',
    totalEntregado: efectivoParseado.value,
    cambio: cambio.value,
    totalCobrado: totalVenta.value
  };
  emit('pagoConfirmado', infoCobro, props.items);
  cerrarDialog();
}

function pagarConTarjeta() {
  const infoCobro: InfoCobro = {
    tipo: 'tarjeta',
    totalEntregado: 0,
    cambio: 0,
    totalCobrado: totalVenta.value
  };
  emit('pagoConfirmado', infoCobro, props.items);
  cerrarDialog();
}

function cerrarDialog() {
  isOpen.value = false;
}
</script>

<style scoped>
.cobro-dialog-card {
  overflow: hidden;
  padding: 0;
}

/* Título sin márgenes - ocupa todo el ancho */
.titulo-cobro {
  padding: 24px !important;
  margin: 0 !important;
}

/* Contenido con padding */
.contenido-cobro {
  padding: 16px !important;
}

.dialog-row {
  min-height: 500px;
}

/* --- Columna Izquierda --- */
.columna-info {
  background-color: var(--v-theme-surface-variant);
  color: var(--v-theme-on-surface-variant);
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-radius: 16px;
}

.info-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.display-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.display-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  background-color: rgba(var(--v-theme-on-surface-variant), 0.05);
  border: 1px solid rgba(var(--v-theme-on-surface-variant), 0.1);
}

.display-item .label {
  font-size: 1rem;
  font-weight: 500;
  opacity: 0.8;
}
.display-item .amount {
  font-size: 1.5rem;
  font-weight: 700;
}

/* Tamaños más grandes cuando NO se muestra el teclado */
.display-container.sin-teclado .display-item {
  padding: 32px;
}

.display-container.sin-teclado .display-item .label {
  font-size: 1.8rem;
}

.display-container.sin-teclado .display-item .amount {
  font-size: 3.5rem;
}

.total-cobrar {
  background-color: rgba(var(--v-theme-info), 0.1);
  border-color: rgba(var(--v-theme-info), 0.2);
  color: var(--v-theme-info);
}

.cambio-devolver {
  background-color: rgba(var(--v-theme-success), 0.1);
  border-color: rgba(var(--v-theme-success), 0.2);
  color: var(--v-theme-success);
}
.cambio-devolver.falta-dinero {
  background-color: rgba(var(--v-theme-error), 0.1);
  border-color: rgba(var(--v-theme-error), 0.2);
  color: var(--v-theme-error);
}

/* Acciones Principales */
.acciones-principales {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.accion-principal-btn {
  height: 120px !important;
  width: 100%;
  border-radius: 16px !important;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 1rem;
  font-weight: 600;
  text-transform: none;
}
.accion-principal-btn.wide {
  grid-column: 1 / -1;
  height: 80px !important;
  flex-direction: row;
}

/* Botones más grandes cuando NO se muestra el teclado */
.acciones-principales.sin-teclado .accion-principal-btn {
  height: 160px !important;
  font-size: 1.5rem;
  gap: 16px;
}

.acciones-principales.sin-teclado .accion-principal-btn .v-icon {
  font-size: 4rem !important;
}

.acciones-principales.sin-teclado .accion-principal-btn.wide {
  height: 100px !important;
  font-size: 1.3rem;
}


/* --- Columna Derecha --- */
.columna-teclado {
  background-color: var(--v-theme-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px; /* Añadir padding interno */
}
.teclado-wrapper {
  width: 100%;
  max-width: 320px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.efectivo-display {
  margin-bottom: 8px;
}
.efectivo-input {
  width: 100%;
  background: none;
  border: none;
  outline: none;
  text-align: right;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--v-theme-primary);
  padding: 0;
}

.teclado-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tecla-num {
  height: 64px !important;
  font-size: 1.5rem;
  font-weight: 500;
  border-radius: 12px !important;
  background-color: rgba(var(--v-theme-on-surface), 0.05);
  color: var(--v-theme-on-surface);
  border: 1px solid rgba(var(--v-theme-on-surface), 0.1);
}
.tecla-num:active {
  background-color: rgba(var(--v-theme-primary), 0.1);
}

.restaurar-btn {
  height: 56px !important;
  font-size: 1.1rem !important;
  font-weight: 600;
  border-radius: 12px !important;
}
</style>