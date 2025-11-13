<template>
  <div 
    ref="el" 
    class="mesa"  
    @click="onMesaClick"
  >
    <span>{{ mesa.Nombre }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Mesa } from '../../models/mesaZona'
import { useMesasStore } from '../../store/dbStore/mesasStore';

const props = defineProps<{
  mesa: Mesa;
  bounds: HTMLElement | null;
  esEditable: boolean;
}>();
const mesasStore = useMesasStore();

const el = ref<HTMLElement | null>(null);

// --- 2. LÓGICA DEL CLIC ---
// Esta función se llamará cada vez que se haga clic en la mesa.
function onMesaClick() {
  // Solo hacemos el console.log si NO estamos en modo edición.
  if (!props.esEditable) {
    console.log(`Has hecho click en la mesa: ${props.mesa.Nombre} (ID: ${props.mesa.ID})`);
  }
}


</script>
<style scoped>
.mesa {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #a3be8c;
  color: white;
  font-weight: bold;
  user-select: none;
  border: 2px solid rgba(0,0,0,0.2);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease; /* Añadimos una transición suave */

  /* --- NUEVA LÓGICA DE CURSOR --- */
  /* Por defecto, en modo normal, mostramos el cursor de clic (puntero) */
  cursor: pointer;
}

.mesa:hover {
  /* Un pequeño efecto visual al pasar por encima en modo normal */
  transform: scale(1.03);
}

/* --- ESTILOS SOLO PARA EL MODO EDICIÓN --- */

/* Cuando el SALÓN (padre) está en modo edición... */
.edit-mode .mesa {
  /* Cambiamos el cursor para indicar que se puede arrastrar */
  cursor: grab;
}

.edit-mode .mesa:hover {
  /* Anulamos el efecto de escala en modo edición */
  transform: none;
}

/* Y cuando se está arrastrando activamente en modo edición... */
.edit-mode .mesa:active {
  /* Mostramos la mano cerrada */
  cursor: grabbing;
}

.delete-btn {
  position: absolute;
  top: -10px;
  right: -10px;
  z-index: 10;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.rect {
  width: 100px;
  height: 60px;
  border-radius: 8px;
}
.circ {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}
</style>