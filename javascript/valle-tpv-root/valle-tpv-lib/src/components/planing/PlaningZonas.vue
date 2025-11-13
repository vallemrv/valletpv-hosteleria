<template>
  <div class="planing-container">
    <div ref="container" class="grid-container" :style="gridStyle">
      <div
            v-for="idx in totalCells"
            :key="idx"
            class="grid-cell"
            :style="cellStyle"
            @click="handleCellClick(getCellIndex(idx-1))"
            @mousedown="startLongClick(getCellIndex(idx-1))"
            @mouseup="cancelLongClick"
            @mouseleave="cancelLongClick"
            @touchstart.prevent="startLongClick(getCellIndex(idx-1))"
            @touchend="cancelLongClick"
        ></div>

        <v-dialog v-model="longDialogVisible" persistent max-width="300">
          <v-card>
            <v-card-title class="text-h6">Opciones avanzadas</v-card-title>
            <v-card-text>
              <v-list>
                <v-list-item prepend-icon="mdi-swap-horizontal" @click="longMenuAction('cambiar')">Cambiar mesa</v-list-item>
                <v-list-item prepend-icon="mdi-table-plus" @click="longMenuAction('juntar')">Juntar mesa</v-list-item>
                <v-list-item prepend-icon="mdi-delete" @click="longMenuAction('borrar')">Borrar mesa</v-list-item>
              </v-list>
            </v-card-text>
            <v-card-actions>
              <v-spacer />
              <v-btn color="primary" text @click="longDialogVisible = false">Cerrar</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <ExpansionDialog 
          :visible="dialogVisible" 
          @update:visible="dialogVisible = $event"
          @select-svg="selectMenuItem" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue';
import ExpansionDialog from '../dialogs/ExpansionDialog.vue'; // Asegúrate que la ruta a este componente sea correcta

// --- Estado de rotación de celdas ---
const cellRotation = ref<{ [key: number]: number }>({}); // índice de celda -> grados

// --- Estado de los diálogos y menús ---
const dialogVisible = ref(false);
const longDialogVisible = ref(false);
const longClickActive = ref(false);
const longClickTimeout = ref<number | null>(null);
const selectedCell = ref<number | null>(null);
const longClickedCell = ref<number | null>(null);


// --- Lógica de la parrilla ---
const rows = ref(4);
const cols = ref(7);
const container = ref<HTMLElement | null>(null);
const containerWidth = ref(0);
const containerHeight = ref(0);

const totalCells = computed(() => rows.value * cols.value);

// La lógica de numeración sigue siendo la misma y funciona para ambas orientaciones
function getCellIndex(idx: number): number {
  if (cols.value > rows.value) { // Orientación horizontal 7x4
    return idx;
  }
  else { // Orientación vertical 4x7
    const visualRow = Math.floor(idx / cols.value);
    const visualCol = idx % cols.value;
    return ((cols.value - 1) - visualCol) * rows.value + visualRow;
  }
}

// Calcula el tamaño de la celda para que sea siempre un cuadrado
const cellSize = computed(() => {
  if (!containerWidth.value || !containerHeight.value) return 0;
  // La celda será tan grande como lo permita el espacio más restrictivo (ancho o alto)
  return Math.floor(Math.min(containerWidth.value / cols.value, containerHeight.value / rows.value));
});

// Estilo para el contenedor de la parrilla, se ajusta para mantener los cuadrados
const gridStyle = computed(() => {
  const size = cellSize.value;
  if (!size) return {};
  return {
    gridTemplateRows: `repeat(${rows.value}, ${size}px)`,
    gridTemplateColumns: `repeat(${cols.value}, ${size}px)`,
    // Se define un tamaño fijo para la parrilla para que las celdas no se deformen
    width: `${cols.value * size}px`,
    height: `${rows.value * size}px`,
  };
});

// Estilo para cada celda
const cellStyle = computed(() => {
  const size = cellSize.value;
  return {
    // Sin borde para que estén pegadas
    width: `${size}px`,
    height: `${size}px`,
  };
});


// --- Manejo de eventos de celda (sin cambios) ---
function handleCellClick(idx: number) {
  if (longClickActive.value) {
    longClickActive.value = false;
    return;
  }
  if (selectedCell.value === idx && dialogVisible.value) {
    dialogVisible.value = false;
    selectedCell.value = null;
  } else {
    selectedCell.value = idx;
    dialogVisible.value = true;
  }
}
function startLongClick(idx: number) {
  cancelLongClick();
  longClickTimeout.value = window.setTimeout(() => {
    longClickActive.value = true;
    longClickedCell.value = idx;
    longDialogVisible.value = true;
  }, 500);
}
function cancelLongClick() {
  if (longClickTimeout.value) {
    clearTimeout(longClickTimeout.value);
    longClickTimeout.value = null;
  }
}
function longMenuAction(action: string) {
  longDialogVisible.value = false;
  console.log('Long click en celda', longClickedCell.value, 'acción:', action);
  // Si la acción es 'rotar', rota la celda 90 grados
  if (action === 'rotar' && longClickedCell.value !== null) {
    cellRotation.value[longClickedCell.value] = ((cellRotation.value[longClickedCell.value] || 0) + 90) % 360;
    const cellElement = (container.value?.children[longClickedCell.value] as HTMLElement);
    if (cellElement) {
      cellElement.style.transform = `rotate(${cellRotation.value[longClickedCell.value]}deg)`;
    }
  }
}
function selectMenuItem(item: { label: string; url: string }) {
  if (selectedCell.value !== null) {
    const cellElement = (container.value?.children[selectedCell.value] as HTMLElement);
    if (cellElement) {
      cellElement.style.backgroundImage = `url('${item.url}')`;
      cellElement.style.backgroundSize = 'contain';
      cellElement.style.backgroundRepeat = 'no-repeat';
      cellElement.style.backgroundPosition = 'center';
      // Aplica rotación si existe
      const rotation = cellRotation.value[selectedCell.value] || 0;
      cellElement.style.transform = `rotate(${rotation}deg)`;
    }
  }
  dialogVisible.value = false;
  selectedCell.value = null;
}

// --- Medición y redimensionado ---
function measure() {
  // El elemento padre (.planing-container) es el que se mide
  const parentEl = container.value?.parentElement;
  if (parentEl) {
    const w = parentEl.clientWidth;
    const h = parentEl.clientHeight;

    // Decide la orientación y actualiza las filas y columnas
    if (w >= h) { // Horizontal o cuadrado
      rows.value = 4;
      cols.value = 7;
    } else { // Vertical
      rows.value = 7;
      cols.value = 4;
    }
    
    // Guarda las dimensiones del contenedor para los cálculos
    containerWidth.value = w;
    containerHeight.value = h;
  }
}

onMounted(() => {
  measure();
  window.addEventListener('resize', measure);
});

onUnmounted(() => {
  window.removeEventListener('resize', measure);
});
</script>

<style scoped>
/* Contenedor principal que ocupa todo el espacio y centra la parrilla */
.planing-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* La parrilla en sí, su tamaño se controla desde el script */
.grid-container {
  display: grid;
  gap: 0; /* Sin espacio entre celdas */
}

.grid-cell {
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid #ccc; /* <--- Añade esta línea */
  box-sizing: border-box;
  cursor: pointer;
  transition: background-color 0.2s;
  -webkit-user-select: none; 
  -ms-user-select: none; 
  user-select: none; 
}

.grid-cell:hover {
  background-color: rgba(var(--v-theme-primary), 0.15);
}

</style>