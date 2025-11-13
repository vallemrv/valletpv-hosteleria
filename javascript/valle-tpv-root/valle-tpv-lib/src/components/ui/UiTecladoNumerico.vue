<template>
  <div class="teclado-numerico">
    <div class="teclado-container">
      <v-btn
        v-for="numero in numeros"
        :key="numero"
        :class="['tecla-numero', { 'tecla-seleccionada': numero === cantidadStore.cantidad }]"
        :elevation="numero === cantidadStore.cantidad ? 0 : 4"
        rounded="lg"
        @click="onNumeroClick(numero)"
      >
        <span class="numero-text">{{ numero }}</span>
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useCantidadStore } from '../../store/cantidadStore';

const emit = defineEmits<{
  numeroClick: [numero: number];
}>();

const cantidadStore = useCantidadStore();
const numeros = [1, 2, 3, 4, 5, 6, 7, 8, 9];

function onNumeroClick(numero: number) {
  cantidadStore.setCantidad(numero);
  emit('numeroClick', numero);
}
</script>

<style scoped>
.teclado-numerico {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 16px;
  height: 100%;
}

.teclado-container {
  display: grid;
  grid-template-rows: repeat(9, 1fr); /* 9 filas de igual altura */
  gap: 15px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  max-width: 100%;
 
}

.tecla-numero {
  min-width: 60px !important;
  min-height: 60px !important;
  margin: 0 !important;
  padding: 0 !important;
  background-color: rgba(var(--v-theme-primary), 0.12) !important;
  border: 1.5px solid var(--v-theme-primary) !important;
  border-radius: 12px !important;
  display: grid !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-size: 1.1rem !important;
  cursor: pointer;
  user-select: none;
  position: relative;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

.tecla-numero:hover:not(.tecla-seleccionada) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2) !important;
  background-color: rgba(var(--v-theme-primary), 0.18) !important;
}

.tecla-numero:active:not(.tecla-seleccionada) {
  transform: translateY(0px) scale(0.95);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1) !important;
}

/* Estado seleccionado - parece pulsado */
.tecla-numero.tecla-seleccionada.v-btn {
  transform: translateY(2px) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  background-color: rgb(var(--v-theme-primary)) !important; /* <<-- CAMBIO AQUÍ */
  border: 2px solid var(--v-theme-primary) !important;
}

.tecla-numero.tecla-seleccionada.v-btn:hover {
  transform: translateY(2px) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  background-color: rgb(var(--v-theme-primary)) !important;
}

.tecla-numero.tecla-seleccionada .numero-text {
  color: var(--v-theme-on-primary) !important;
  font-weight: 800;
}

.numero-text {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--v-theme-on-surface);
  line-height: 1;
  transition: color 0.2s ease;
}

/* Efecto de pulso al hacer click */
.tecla-numero::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(var(--v-theme-primary), 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.3s ease, height 0.3s ease, opacity 0.3s ease;
  opacity: 0;
  pointer-events: none;
}

.tecla-numero:active::after {
  width: 100%;
  height: 100%;
  opacity: 1;
}

/* Responsive */
@media (max-width: 768px) {
  .teclado-container {
    gap: 8px;
  }
  
  .tecla-numero {
    min-width: 50px !important;
    min-height: 50px !important;
  }
  
  .numero-text {
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .teclado-numerico {
    padding: 12px 8px;
  }
  
  .teclado-container {
    gap: 6px;
  }
  
  .tecla-numero {
    min-width: 45px !important;
    min-height: 45px !important;
  }
  
  .numero-text {
    font-size: 1rem;
  }
}

/* Estados adicionales */
.tecla-numero:focus {
  outline: 2px solid var(--v-theme-primary);
  outline-offset: 2px;
}

.tecla-numero:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.tecla-numero:disabled:hover {
  transform: none !important;
  box-shadow: initial !important;
  background-color: rgba(var(--v-theme-primary), 0.12) !important;
}

/* Animación de entrada */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tecla-numero {
  animation: slideInUp 0.3s ease;
}

.tecla-numero:nth-child(1) { animation-delay: 0.05s; }
.tecla-numero:nth-child(2) { animation-delay: 0.1s; }
.tecla-numero:nth-child(3) { animation-delay: 0.15s; }
.tecla-numero:nth-child(4) { animation-delay: 0.2s; }
.tecla-numero:nth-child(5) { animation-delay: 0.25s; }
.tecla-numero:nth-child(6) { animation-delay: 0.3s; }
.tecla-numero:nth-child(7) { animation-delay: 0.35s; }
.tecla-numero:nth-child(8) { animation-delay: 0.4s; }
.tecla-numero:nth-child(9) { animation-delay: 0.45s; }

/* Tema oscuro */
@media (prefers-color-scheme: dark) {
  .numero-text {
    color: var(--v-theme-on-surface);
  }
}
</style>