<template>
  <v-snackbar
    v-model="snackbarVisible"
    :timeout="6000"
    location="top center"
    color="#fce4ec"
    rounded="xl"
    elevation="8"
    class="custom-snackbar"
  >
    <v-icon size="x-large" class="success-icon">mdi-check-circle</v-icon>
    
    <div class="content-wrapper">
      <div v-if="info?.tipo === 'tarjeta'">
        <h4 class="title">¡Cobro Exitoso!</h4>
        <p class="message">El pago con tarjeta se ha realizado correctamente.</p>
      </div>

      <div v-else-if="info?.tipo === 'efectivo'" class="efectivo-details">
        <div class="detail-item">
          <span>Total Cobrado:</span>
          <span class="amount">{{ formatCurrency(info.totalCobrado) }}</span>
        </div>
        <div class="detail-item">
          <span>Entregado:</span>
          <span class="amount">{{ formatCurrency(info.totalEntregado) }}</span>
        </div>
        <v-divider class="my-1"></v-divider>
        <div class="detail-item cambio">
          <span>Cambio a devolver:</span>
          <span class="amount ml-5">{{ formatCurrency(info.cambio) }}</span>
        </div>
      </div>
    </div>

    <template v-slot:actions>
      <v-btn
        variant="text"
        icon="mdi-close"
        @click="snackbarVisible = false"
        class="close-btn"
      ></v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useCuentaStore } from '../../store/dbStore/cuentasStore' // Asegúrate que la ruta sea correcta

const cuentaStore = useCuentaStore();
const snackbarVisible = ref(false);

const info = computed(() => cuentaStore.infoCobro);

watch(info, (newInfo) => {
  if (newInfo) {
    snackbarVisible.value = true;
  }
});

watch(snackbarVisible, (isVisible) => {
  if (!isVisible) {
    setTimeout(() => {
      cuentaStore.limpiarInfoCobro();
    }, 300);
  }
});

function formatCurrency(amount: number = 0): string {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(amount);
}
</script>

<style scoped>
/* AÑADIDO: Margen superior para separarlo del borde de la ventana */
.custom-snackbar {
  margin-top: 24px;
  color: #4a4a4a;
  min-width: 350px;
}

:deep(.v-snackbar__content) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px !important;
}

.success-icon {
  color: var(--v-theme-success);
}

.content-wrapper {
  flex-grow: 1;
}

/* AUMENTADO: Tamaño del título */
.title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

/* AUMENTADO: Tamaño del mensaje normal */
.message {
  font-size: 1rem;
  opacity: 0.9;
  margin: 4px 0 0;
  line-height: 1.4;
}

/* AUMENTADO: Tamaño de los detalles de efectivo */
.efectivo-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* AUMENTADO: Tamaño de las cantidades */
.amount {
  font-weight: 700;
  font-size: 1.1rem;
}

/* AUMENTADO: Tamaño del texto del cambio */
.cambio {
  color: var(--v-theme-success);
  font-size: 1.2rem;
  margin-top: 4px;
}

/* AUMENTADO: Tamaño de la cantidad del cambio (el más grande) */
.cambio .amount {
  font-size: 1.35rem;
}

.close-btn {
  color: var(--v-theme-on-surface-variant);
  opacity: 0.7;
}
</style>