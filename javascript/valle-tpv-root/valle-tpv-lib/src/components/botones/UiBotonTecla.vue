<template>
  <div class="btn-tecla-container">
    <!-- Botón flotante de bloqueo en la esquina superior derecha -->
    <v-btn 
      class="tecla-bloqueo-float"
      :icon="bloqueada ? 'mdi-lock' : 'mdi-lock-open'"
      size="small"
      :color="bloqueada ? 'error' : 'success'"
      elevation="4"
      rounded="pill"
      @click.stop="toggleBloqueo"
    />

    <!-- Botón principal de la tecla -->
    <v-btn 
      class="btn-tecla" 
      elevation="6" 
      rounded="lg" 
      :style="btnStyle"
      :disabled="bloqueada"
      @click="onTeclaClick"
    >
      <div class="tecla-content">
        <!-- Nombre del producto -->
        <div class="tecla-nombre">{{ nombre }}</div>
        
        <!-- Precio (solo si es mayor que 0) -->
        <div v-if="precio > 0" class="tecla-precio">{{ formatCurrency(precio) }}</div>
      </div>
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  nombre: string;
  precio: number;
  bloqueada: boolean;
}>();

const emit = defineEmits<{
  teclaClick: [];
  bloqueoChanged: [bloqueada: boolean];
}>();

const btnStyle = computed(() => ({
  backgroundColor: props.bloqueada 
    ? 'rgba(var(--v-theme-error), 0.1)' 
    : 'rgba(var(--v-theme-primary, 21,101,192), 0.12)',
  border: props.bloqueada 
    ? '1.5px solid var(--v-theme-error)' 
    : '1.5px solid var(--v-theme-primary)',
  padding: '1em',
  boxSizing: 'border-box' as const,
  opacity: props.bloqueada ? 0.6 : 1,
  minWidth: '100%',
  minHeight: '100%',
}));

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2
  }).format(amount);
}

function onTeclaClick() {
  if (!props.bloqueada) {
    emit('teclaClick');
  }
}

function toggleBloqueo() {
  // Solo emitir el evento, NO cambiar el estado local
  // El padre decidirá si cambia o no
  emit('bloqueoChanged', !props.bloqueada);
}
</script>

<style scoped>
.btn-tecla-container {
  position: relative;
  display: inline-block;
  width: 100%;
  height: 100%;
}

.tecla-bloqueo-float {
  position: absolute !important;
  bottom: 4px !important;
  right: 4px !important;
  z-index: 10;
  background-color: rgba(var(--v-theme-surface), 0.95) !important;
  backdrop-filter: blur(4px);
  min-width: 32px !important;
  width: 32px !important;
  height: 32px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.tecla-bloqueo-float:hover {
  transform: scale(1.1) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25) !important;
}

.btn-tecla {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-size: 0.9rem !important;
  position: relative;
  padding: 16px 5px !important;
}

.btn-tecla:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.btn-tecla:disabled {
  cursor: not-allowed;
}

.tecla-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  height: 100%;
  padding: 8px;
}

.tecla-nombre {
  font-size: 1rem;
  font-weight: 700;
  color: var(--v-theme-on-surface);
  text-align: center;
  line-height: 1.2;
  max-width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  white-space: normal;
  text-transform: none;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tecla-precio {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--v-theme-success);
  background: rgba(var(--v-theme-success), 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(var(--v-theme-success), 0.3);
}

/* Estados bloqueados */
.btn-tecla:disabled .tecla-nombre {
  color: var(--v-theme-error);
  opacity: 0.7;
}

.btn-tecla:disabled .tecla-precio {
  color: var(--v-theme-error);
  background: rgba(var(--v-theme-error), 0.1);
  border-color: rgba(var(--v-theme-error), 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  
  .tecla-nombre {
    font-size: 0.75rem;
  }
  
  .tecla-precio {
    font-size: 0.8rem;
    padding: 3px 10px;
  }
  
  .tecla-bloqueo-float {
    width: 28px !important;
    height: 28px !important;
  }
}

@media (max-width: 480px) {

  .tecla-content {
    gap: 6px;
    padding: 6px;
  }
  
  .tecla-nombre {
    font-size: 0.7rem;
  }
  
  .tecla-precio {
    font-size: 0.75rem;
    padding: 2px 8px;
  }
  
  .tecla-bloqueo-float {
    width: 24px !important;
    height: 24px !important;
    top: -6px !important;
    right: -6px !important;
  }
}

/* Animaciones */
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

.btn-tecla {
  animation: slideInUp 0.3s ease;
}

/* Efecto de pulso para teclas bloqueadas */
@keyframes pulse-error {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(var(--v-theme-error), 0.4);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(var(--v-theme-error), 0);
  }
}

.btn-tecla:disabled {
  animation: pulse-error 2s infinite;
}

/* Focus states */
.btn-tecla:focus {
  outline: 2px solid var(--v-theme-primary);
  outline-offset: 2px;
}

.btn-tecla:disabled:focus {
  outline-color: var(--v-theme-error);
}

/* Tema oscuro */
@media (prefers-color-scheme: dark) {
  .tecla-nombre {
    color: var(--v-theme-on-surface);
  }
}
</style>
