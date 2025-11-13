<template>
  <v-dialog 
    v-model="isOpen" 
    persistent 
    max-width="1000px"
    class="cambio-dialog"
  >
    <v-card class="dialog-card elevation-12 rounded-xl">
      <!-- Cabecera negra como en UiVariosDialog -->
      <v-card-title class="dialog-title bg-black text-white text-center pa-4">
        <div class="title-content">
          <v-icon size="32" class="mr-3">mdi-cash-multiple</v-icon>
          <span class="dialog-title-text">Dar Cambio - CashKeeper</span>
        </div>
      </v-card-title>

      <v-card-text class="dialog-content pa-6">
        <div class="cambio-container">
          <!-- Columna izquierda - Información -->
          <div class="info-column">
            <!-- Fila superior - Importes -->
            <div class="importes-section">
              <div class="importe-card">
                <div class="importe-label">Importe Admitido</div>
                <div class="importe-value success--text">{{ formatCurrency(cambioADar) }}</div>
              </div>
            </div>
            
            <!-- Fila inferior - Explicaciones -->
            <div class="explicaciones-section">
              <div class="explicacion-item" :class="{ 'intermittent-blink': isCanceling }">
                <v-icon color="primary" class="mr-2">{{ statusIcon }}</v-icon>
                <span class="status-text">{{ statusText }}</span>
              </div>
            </div>          </div>

          <!-- Columna derecha - Cuadrícula de denominaciones -->
          <div class="denominaciones-column">
            <div class="denominaciones-grid">
               <!-- 20 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn billete-btn" :disabled="!estaDenominacionDisponible(20) || isDispensing" @click="dispensarDenominacionDirecta(20)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/veinteeuros.png" alt="20€" width="40" height="25" contain>
                        <template v-slot:placeholder><div class="image-placeholder">20€</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">20€</div>
                    <v-chip v-if="getCantidadDenominacion(20) > 0" :color="getDenominacionStatus(20).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(20).showAmount">{{ getDenominacionStatus(20).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 10 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn billete-btn" :disabled="!estaDenominacionDisponible(10) || isDispensing" @click="dispensarDenominacionDirecta(10)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/diezeuros.png" alt="10€" width="40" height="25" contain>
                        <template v-slot:placeholder><div class="image-placeholder">10€</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">10€</div>
                    <v-chip v-if="getCantidadDenominacion(10) > 0" :color="getDenominacionStatus(10).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(10).showAmount">{{ getDenominacionStatus(10).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 5 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn billete-btn" :disabled="!estaDenominacionDisponible(5) || isDispensing" @click="dispensarDenominacionDirecta(5)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/cincoeuros.png" alt="5€" width="40" height="25" contain>
                        <template v-slot:placeholder><div class="image-placeholder">5€</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">5€</div>
                    <v-chip v-if="getCantidadDenominacion(5) > 0" :color="getDenominacionStatus(5).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(5).showAmount">{{ getDenominacionStatus(5).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 2 EUR -->
              <div class="denominacion-item">
                <!-- Si es <= 10€, botón normal. Si es > 10€, botón con menú -->
                <v-menu v-if="cashKeeperStore.totalAdmitido > 10" offset-y max-height="400">
                  <template v-slot:activator="{ props }">
                    <v-btn 
                      v-bind="props"
                      class="denominacion-btn moneda-btn" 
                      :disabled="!estaDenominacionDisponible(2) || isDispensing" 
                      size="large" 
                      variant="elevated"
                    >
                      <div class="btn-content">
                        <div class="denominacion-image">
                          <v-img src="/ic_euros/doseuros.png" alt="2€" width="35" height="35" contain>
                            <template v-slot:placeholder><div class="image-placeholder">2€</div></template>
                          </v-img>
                        </div>
                        <div class="denominacion-valor">2€</div>
                        <v-chip v-if="getCantidadDenominacion(2) > 0" :color="getDenominacionStatus(2).color" size="x-small" class="mt-1" variant="elevated">
                          <span v-if="getDenominacionStatus(2).showAmount">{{ getDenominacionStatus(2).amount }}</span>
                          <v-icon v-else>mdi-check</v-icon>
                        </v-chip>
                      </div>
                    </v-btn>
                  </template>
                  <v-list class="euro-menu-list">
                    <!-- Generar opciones para monedas de 2€: 5, luego de 10 en 10 -->
                    <v-list-item 
                      v-for="opcion in opcionesMenu2Euros" 
                      :key="opcion.cantidad"
                      @click="dispensarMonedas2Euros(opcion.cantidad)" 
                      class="menu-item-large"
                    >
                      <v-list-item-title class="menu-item-text">
                        {{ opcion.texto }}
                      </v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
                
                <!-- Botón normal cuando totalAdmitido <= 10 -->
                <v-btn 
                  v-else
                  class="denominacion-btn moneda-btn" 
                  :disabled="!estaDenominacionDisponible(2) || isDispensing" 
                  @click="dispensarDenominacionDirecta(2)" 
                  size="large" 
                  variant="elevated"
                >
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/doseuros.png" alt="2€" width="35" height="35" contain>
                        <template v-slot:placeholder><div class="image-placeholder">2€</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">2€</div>
                    <v-chip v-if="getCantidadDenominacion(2) > 0" :color="getDenominacionStatus(2).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(2).showAmount">{{ getDenominacionStatus(2).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 1 EUR -->
              <div class="denominacion-item">
                <!-- Si es <= 5€, botón normal. Si es > 5€, botón con menú -->
                <v-menu v-if="cashKeeperStore.totalAdmitido > 5" offset-y max-height="400">
                  <template v-slot:activator="{ props }">
                    <v-btn 
                      v-bind="props"
                      class="denominacion-btn moneda-btn" 
                      :disabled="!estaDenominacionDisponible(1) || isDispensing" 
                      size="large" 
                      variant="elevated"
                    >
                      <div class="btn-content">
                        <div class="denominacion-image">
                          <v-img src="/ic_euros/uneuro.png" alt="1€" width="35" height="35" contain>
                            <template v-slot:placeholder><div class="image-placeholder">1€</div></template>
                          </v-img>
                        </div>
                        <div class="denominacion-valor">1€</div>
                        <v-chip v-if="getCantidadDenominacion(1) > 0" :color="getDenominacionStatus(1).color" size="x-small" class="mt-1" variant="elevated">
                          <span v-if="getDenominacionStatus(1).showAmount">{{ getDenominacionStatus(1).amount }}</span>
                          <v-icon v-else>mdi-check</v-icon>
                        </v-chip>
                      </div>
                    </v-btn>
                  </template>
                  <v-list class="euro-menu-list">
                    <!-- Generar opciones de 10 en 10 hasta el total -->
                    <v-list-item 
                      v-for="opcion in opcionesMenu" 
                      :key="opcion.cantidad"
                      @click="dispensarMonedas(opcion.cantidad)" 
                      class="menu-item-large"
                    >
                      <v-list-item-title class="menu-item-text">
                        {{ opcion.texto }}
                      </v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
                
                <!-- Botón normal cuando totalAdmitido <= 5 -->
                <v-btn 
                  v-else
                  class="denominacion-btn moneda-btn" 
                  :disabled="!estaDenominacionDisponible(1) || isDispensing" 
                  @click="dispensarDenominacionDirecta(1)" 
                  size="large" 
                  variant="elevated"
                >
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/uneuro.png" alt="1€" width="35" height="35" contain>
                        <template v-slot:placeholder><div class="image-placeholder">1€</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">1€</div>
                    <v-chip v-if="getCantidadDenominacion(1) > 0" :color="getDenominacionStatus(1).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(1).showAmount">{{ getDenominacionStatus(1).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 0.50 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.5) || isDispensing" @click="dispensarDenominacionDirecta(0.5)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/cincuentacentimos.png" alt="0.50€" width="32" height="32" contain>
                        <template v-slot:placeholder><div class="image-placeholder">50c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.50€</div>
                    <v-chip v-if="getCantidadDenominacion(0.5) > 0" :color="getDenominacionStatus(0.5).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.5).showAmount">{{ getDenominacionStatus(0.5).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 0.20 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.2) || isDispensing" @click="dispensarDenominacionDirecta(0.2)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/veintecentimos.png" alt="0.20€" width="30" height="30" contain>
                        <template v-slot:placeholder><div class="image-placeholder">20c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.20€</div>
                    <v-chip v-if="getCantidadDenominacion(0.2) > 0" :color="getDenominacionStatus(0.2).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.2).showAmount">{{ getDenominacionStatus(0.2).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 0.10 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.1) || isDispensing" @click="dispensarDenominacionDirecta(0.1)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/diezcentimos.png" alt="0.10€" width="28" height="28" contain>
                        <template v-slot:placeholder><div class="image-placeholder">10c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.10€</div>
                    <v-chip v-if="getCantidadDenominacion(0.1) > 0" :color="getDenominacionStatus(0.1).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.1).showAmount">{{ getDenominacionStatus(0.1).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              <!-- 0.05 EUR -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.05) || isDispensing" @click="dispensarDenominacionDirecta(0.05)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/cincocentimos.png" alt="0.05€" width="26" height="26" contain>
                        <template v-slot:placeholder><div class="image-placeholder">5c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.05€</div>
                    <v-chip v-if="getCantidadDenominacion(0.05) > 0" :color="getDenominacionStatus(0.05).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.05).showAmount">{{ getDenominacionStatus(0.05).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>

              <!-- Fila 4: 0.02€, 0.01€ -->
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.02) || isDispensing" @click="dispensarDenominacionDirecta(0.02)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/doscentimos.png" alt="0.02€" width="24" height="24" contain>
                        <template v-slot:placeholder><div class="image-placeholder">2c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.02€</div>
                    <v-chip v-if="getCantidadDenominacion(0.02) > 0" :color="getDenominacionStatus(0.02).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.02).showAmount">{{ getDenominacionStatus(0.02).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
              
              <div class="denominacion-item">
                <v-btn class="denominacion-btn moneda-btn" :disabled="!estaDenominacionDisponible(0.01) || isDispensing" @click="dispensarDenominacionDirecta(0.01)" size="large" variant="elevated">
                  <div class="btn-content">
                    <div class="denominacion-image">
                      <v-img src="/ic_euros/uncentimo.png" alt="0.01€" width="22" height="22" contain>
                        <template v-slot:placeholder><div class="image-placeholder">1c</div></template>
                      </v-img>
                    </div>
                    <div class="denominacion-valor">0.01€</div>
                    <v-chip v-if="getCantidadDenominacion(0.01) > 0" :color="getDenominacionStatus(0.01).color" size="x-small" class="mt-1" variant="elevated">
                      <span v-if="getDenominacionStatus(0.01).showAmount">{{ getDenominacionStatus(0.01).amount }}</span>
                      <v-icon v-else>mdi-check</v-icon>
                    </v-chip>
                  </div>
                </v-btn>
              </div>
            </div>
          </div>
        </div>
      </v-card-text>

      <!-- Acciones del diálogo -->
      <v-card-actions class="dialog-actions pa-4">
        <v-spacer></v-spacer>
        <v-btn
          color="error"
          variant="elevated"
          @click="onDialogAction('cancelar')"
          size="x-large"
          rounded="lg"
          style="height: 80px;"
          :disabled="isCanceling || isDispensing"
        >
          <v-icon size="24" class="mr-2">mdi-close</v-icon>
          Cancelar
        </v-btn>
        <v-spacer></v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {  computed, watch } from 'vue';
import { useCashKeeperStore } from '../../store/cashKeeperStore';

// Props
interface Props {
  modelValue?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
});

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'cambio-confirmado': [denominaciones: any[]];
  'cambio-cancelado': [];
}>();

// Instancia del store de CashKeeper
const cashKeeperStore = useCashKeeperStore();

// Estado del diálogo
const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

// Watcher para iniciar el proceso de cambio cuando se abre el diálogo
watch(isOpen, (nuevoValor) => {
  if (nuevoValor) {
    iniciarCambio();
  } else {
    // Resetear el store cuando se cierra el diálogo
    cashKeeperStore.resetValues();
  }
});

// Watcher para cerrar el diálogo cuando la operación termine
watch(() => cashKeeperStore.operationStatus, (newStatus) => {
  if (newStatus === 'finished') {
    setTimeout(() => {
      isOpen.value = false;
    }, 1500);
  }
});



// Computadas
const isCanceling = computed(() => cashKeeperStore.operationStatus === 'canceling');
const isDispensing = computed(() => cashKeeperStore.operationStatus === 'dispensing');
const cambioADar = computed(() => cashKeeperStore.totalAdmitido);

// Generar opciones del menú: 5, luego de 10 en 10
const opcionesMenu = computed(() => {
  const total = Math.floor(cashKeeperStore.totalAdmitido);
  const opciones: Array<{ cantidad: number; texto: string }> = [];
  
  // Si el total es 5 o menos, solo mostrar TODO
  if (total <= 5) {
    opciones.push({
      cantidad: total,
      texto: `TODO (${formatCurrency(total)})`
    });
    return opciones;
  }
  
  // Agregar opción de 5 monedas si el total es mayor a 5
  if (total > 5) {
    opciones.push({
      cantidad: 5,
      texto: `5 MONEDAS (${formatCurrency(5)})`
    });
  }
  
  // Generar opciones de 10 en 10 hasta llegar al total
  for (let i = 10; i < total; i += 10) {
    opciones.push({
      cantidad: i,
      texto: `${i} MONEDAS (${formatCurrency(i)})`
    });
  }
  
  // Agregar opción TODO al final
  opciones.push({
    cantidad: total,
    texto: `TODO (${formatCurrency(total)})`
  });
  
  return opciones;
});

// Generar opciones del menú para monedas de 2€: 5, luego de 10 en 10
const opcionesMenu2Euros = computed(() => {
  const totalDisponible = Math.floor(cashKeeperStore.totalAdmitido / 2); // Máximo de monedas de 2€ que podemos dispensar
  const opciones: Array<{ cantidad: number; texto: string }> = [];
  
  // Si el total disponible es 5 o menos, solo mostrar TODO
  if (totalDisponible <= 5) {
    opciones.push({
      cantidad: totalDisponible,
      texto: `TODO (${totalDisponible} monedas = ${formatCurrency(totalDisponible * 2)})`
    });
    return opciones;
  }
  
  // Agregar opción de 5 monedas si el total disponible es mayor a 5
  if (totalDisponible > 5) {
    opciones.push({
      cantidad: 5,
      texto: `5 MONEDAS (${formatCurrency(5 * 2)})`
    });
  }
  
  // Generar opciones de 10 en 10 hasta llegar al total disponible
  for (let i = 10; i < totalDisponible; i += 10) {
    opciones.push({
      cantidad: i,
      texto: `${i} MONEDAS (${formatCurrency(i * 2)})`
    });
  }
  
  // Agregar opción TODO al final
  opciones.push({
    cantidad: totalDisponible,
    texto: `TODO (${totalDisponible} monedas = ${formatCurrency(totalDisponible * 2)})`
  });
  
  return opciones;
});

const statusText = computed(() => {
  if (cashKeeperStore.hasError) {
    return `Error: ${cashKeeperStore.errorMessage}`;
  }
  if (cashKeeperStore.successMessage) {
    return cashKeeperStore.successMessage;
  }
  if (isCanceling.value) {
    return 'Cancelando operación, devolviendo efectivo...';
  }
  if (isDispensing.value) {
    return 'Devolviendo cambio...';
  }
  if (cashKeeperStore.totalAdmitido > 0) {
    return 'Introduzca más o pulse una denominación.';
  }
  return 'Esperando efectivo.';
});

const statusIcon = computed(() => {
  if (cashKeeperStore.hasError) {
    return 'mdi-alert-circle';
  }
  if (cashKeeperStore.successMessage) {
    return 'mdi-check-circle';
  }
  if (isCanceling.value) {
    return 'mdi-cash-refund';
  }
  if (isDispensing.value) {
    return 'mdi-swap-horizontal-bold';
  }
  if (cashKeeperStore.totalAdmitido > 0) {
    return 'mdi-cash-plus';
  }
  return 'mdi-cash-check';
});

// Funciones
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
}

// Funciones para trabajar con CashKeeper
function iniciarCambio() {
  if (cashKeeperStore.isConnected) {
    cashKeeperStore.iniciarCambio();
  } else {
    console.warn('CashKeeper no está conectado');
  }
}

function onDialogAction(id: string) {
  if (id === 'cancelar') {
    cashKeeperStore.cancelarOperacion();
  }
}

// Funciones de utilidad para denominaciones
function estaDenominacionDisponible(euros: number): boolean {
  const totalAdmitido = cashKeeperStore.totalAdmitido;
  const centimos = Math.round(euros * 100);
  const denominacionInfo = Object.values(cashKeeperStore.denominacionesRecicladores).find(
    (denom: any) => denom.toCentimos === centimos
  ) as any;

  if (!denominacionInfo || denominacionInfo.cantidad <= 0) {
    return false;
  }

  if (totalAdmitido === 0) {
    return false;
  }

  return euros <= totalAdmitido;
}

function getCantidadDenominacion(euros: number): number {
  const centimos = Math.round(euros * 100);
  const denominacion = Object.values(cashKeeperStore.denominacionesRecicladores).find(
    (denom: any) => denom.toCentimos === centimos
  ) as any;
  return denominacion ? denominacion.cantidad : 0;
}

function getDenominacionStatus(euros: number) {
  const cantidad = getCantidadDenominacion(euros);
  const esBillete = euros >= 5;
  const umbralVerde = esBillete ? 5 : 50;

  if (cantidad >= umbralVerde) {
    return { color: 'green', showAmount: false, amount: cantidad };
  } else {
    return { color: 'amber', showAmount: true, amount: cantidad };
  }
}

function dispensarDenominacionDirecta(euros: number) {
  const centimos = Math.round(euros * 100);
  cashKeeperStore.finalizarEstadoDeCambio(centimos, 1);
}

function dispensarMonedas(cantidad: number) {
  // Dispensa la cantidad especificada en monedas de 1€ (100 céntimos)
  cashKeeperStore.finalizarEstadoDeCambio(100, cantidad);
}

function dispensarMonedas2Euros(cantidad: number) {
  // Dispensa la cantidad especificada en monedas de 2€ (200 céntimos)
  cashKeeperStore.finalizarEstadoDeCambio(200, cantidad);
}
</script>

<style scoped>
.dialog-card {
  border-radius: 20px !important;
  overflow: hidden;
  max-height: 90vh;
}

.dialog-title {
  background: #000000 !important;
  color: white !important;
  position: relative;
  border-radius: 0 !important;
}

.title-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.dialog-title-text {
  font-size: 20px;
  font-weight: bold;
}

.dialog-content {
  max-height: 70vh;
  overflow-y: auto;
  background: #f8f9fa;
}

.dialog-actions {
  background: rgba(0, 0, 0, 0.02);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.cambio-container {
  display: flex;
  gap: 24px;
  min-height: 500px;
  height: 100%;
}

.info-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.importes-section {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 20px;
}

.importe-card {
  text-align: center;
}

.importe-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.importe-value {
  font-size: 48px;
  font-weight: bold;
}

.explicaciones-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.explicacion-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #555;
}

.status-text {
  font-size: 18px;
  font-weight: 500;
}

.intermittent-blink {
  animation: blink-animation 1.5s infinite;
}

@keyframes blink-animation {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

.denominaciones-column {
  flex: 1.5;
}

.denominaciones-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  width: 100%;
  height: 100%;
  flex: 1;
  min-height: 400px;
}

.denominacion-item {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.denominacion-btn {
  width: 100% !important;
  height: 100% !important;
  min-height: 90px !important;
  font-weight: bold;
  position: relative;
  border-radius: 12px !important;
  transition: all 0.2s ease !important;
}

.denominacion-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.denominacion-btn:active {
  transform: scale(0.98);
}

.billete-btn {
  background: linear-gradient(135deg, #4caf50, #45a049) !important;
  color: white !important;
}

.moneda-btn {
  background: linear-gradient(135deg, #ff9800, #f57c00) !important;
  color: white !important;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  height: 100%;
  justify-content: center;
}

.denominacion-image {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.image-placeholder {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  font-size: 0.8rem;
  font-weight: bold;
  color: white;
}

.denominacion-valor {
  font-size: 16px;
  font-weight: bold;
}

.denominacion-count {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

/* Estilos para el menú desplegable del euro */
.euro-menu-list {
  min-width: 240px !important;
  border-radius: 12px !important;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2) !important;
}

.menu-item-large {
  min-height: 72px !important;
  padding: 16px 24px !important;
  transition: all 0.2s ease !important;
  cursor: pointer;
}

.menu-item-large:hover {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb) !important;
  transform: translateX(4px);
}

.menu-item-large:active {
  background: linear-gradient(135deg, #90caf9, #64b5f6) !important;
}

.menu-item-text {
  font-size: 1.3rem !important;
  font-weight: 600 !important;
  color: #1976d2 !important;
  letter-spacing: 0.5px;
}

.cantidad-baja-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(255, 152, 0, 0.9);
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}
</style>