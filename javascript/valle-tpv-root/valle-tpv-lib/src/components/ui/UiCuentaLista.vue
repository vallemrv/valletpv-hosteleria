<template>
  <div class="cuenta-lista-container">
    <!-- Header con total de la cuenta -->
    <div class="cuenta-total-header">
      <div class="total-content">
        <h3 class="total-label">Total de la cuenta:</h3>
        <div class="total-amount">{{ formatCurrency(totalCuenta) }}</div>
      </div>
    </div>

    <!-- Lista de items -->
    <div class="cuenta-items-list">
      <div 
        v-for="(item, index) in items" 
        :key="item.id || index"
        class="cuenta-item"
      >
        <div class="item-content">
          <!-- Cantidad -->
          <div class="item-cantidad">
            <span class="cantidad-value">{{ item.cantidad }}</span>
          </div>

          <!-- Descripción -->
          <div class="item-descripcion">
            <span class="descripcion-text">{{ item.descripcion }}</span>
          </div>

          <!-- Precio unitario -->
          <div class="item-precio">
            <span class="precio-value">{{ formatCurrency(item.precio) }}</span>
          </div>

          <!-- Total del item -->
          <div class="item-total">
            <span class="total-value">{{ formatCurrency(item.total) }}</span>
          </div>

          <!-- Botón borrar -->
          <div class="item-actions">
            <v-btn
              class="delete-btn"
              icon="mdi-delete"
              size="small"
              variant="text"
              color="error"
              @click="onDeleteItem(item, index)"
            />
          </div>
        </div>
     </div>

      <!-- Mensaje cuando no hay items -->
      <div v-if="items.length === 0" class="empty-state">
        <v-icon size="48" color="grey-lighten-1" class="empty-icon">
          mdi-receipt-text-outline
        </v-icon>
        <p class="empty-text">No hay items en la cuenta</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { CuentaItem } from '../../models/cuenta';

const props = defineProps<{
  items: CuentaItem[];
}>();

const emit = defineEmits<{
  deleteItem: [item: CuentaItem, index: number];
}>();

const totalCuenta = computed(() => {
  return props.items.reduce((sum, item) => sum + item.total, 0);
});

function formatCurrency(amount: number): string {
  // CAMBIO: Añadimos esta línea para evitar el error NaN
  if (typeof amount !== 'number' || isNaN(amount)) {
    // Devolvemos un valor por defecto si el dato no es un número
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(0);
  }

  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2
  }).format(amount);
}

function onDeleteItem(item: CuentaItem, index: number) {
  emit('deleteItem', item, index);
}
</script>

<style scoped>
.cuenta-lista-container {
  display: flex;
  flex-direction: column;
  background: rgba(var(--v-theme-on-surface), 0.03);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(var(--v-theme-on-surface), 0.1);
}

/* === Header (Ahora con fondo más sólido) === */
.cuenta-total-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--v-theme-surface);
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.1);
  padding: 16px 24px;
}

.total-content {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.total-label {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--v-theme-on-surface);
}

.total-amount {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--v-theme-primary);
}

/* === Lista de Items y Estilo 'Card' === */
.cuenta-items-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px; /* Espacio para las 'cards' */
}

.cuenta-item {
  /* Convertimos cada item en una card */
  background: var(--v-theme-surface);
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid rgba(var(--v-theme-on-surface), 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease-in-out;
  padding: 16px;
}

.cuenta-item:last-child {
  margin-bottom: 0;
}

.cuenta-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: rgba(var(--v-theme-primary), 0.3);
}

.item-content {
  display: grid;
  grid-template-columns: 50px 1fr auto auto 48px;
  gap: 10px;
  align-items: center;

}

/* === Cantidad con Círculo === */
.item-cantidad {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(var(--v-theme-primary), 0.1);
  border-radius: 50%; /* Círculo */
  color: var(--v-theme-primary);
  font-size: 1.1rem;
  font-weight: 600;
}
.item-cantidad .cantidad-value::after {
  content: 'x';
  font-size: 0.7rem;
  margin-left: 2px;
  font-weight: 400;
  opacity: 0.8;
}

/* Descripción y Precio Unitario */
.item-descripcion {
  display: flex;
  flex-direction: column; /* Apilamos nombre y precio */
  align-items: flex-start;
  gap: 2px;
}

.descripcion-text {
  font-size: 1.1rem;
  color: var(--v-theme-on-surface);
  font-weight: 600;
  line-height: 1.3;
}
.item-precio {
  font-size: 0.95rem;
  font-weight: 400;
  color: var(--v-theme-on-surface);
  opacity: 0.6;
}

/* Total del item */
.item-total {
  text-align: right;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--v-theme-on-surface);
}

/* === Botón de Borrar (Siempre Visible pero Sutil) === */
.item-actions {
  display: flex;
  justify-content: center;
}

.delete-btn {
  /* Visible por defecto, pero sutil */
  color: rgba(var(--v-theme-on-surface), 0.4) !important;
  transition: all 0.2s ease-in-out;
  transform: scale(1.1);
}

.cuenta-item:hover .delete-btn {
  /* Al pasar el ratón, se vuelve rojo y más grande */
  color: var(--v-theme-error) !important;
  transform: scale(1.4);
}

/* Scroll personalizado (sin cambios) */
.cuenta-items-list::-webkit-scrollbar {
  width: 6px;
}
.cuenta-items-list::-webkit-scrollbar-track {
  background: transparent;
}
.cuenta-items-list::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-on-surface), 0.15);
  border-radius: 3px;
}
.cuenta-items-list::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-on-surface), 0.3);
}
</style>