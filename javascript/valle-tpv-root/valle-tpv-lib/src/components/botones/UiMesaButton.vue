<template>
  <v-btn
    class="mesa-btn"
    elevation="4"
    rounded="lg"
    @click="onMesaClick"
  >
    <!-- Menú dentro del botón -->
    <v-menu v-if="estado !== 'libre' && !props.mesaAccion" class="mesa-menu" rounded="lg">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          class="mesa-menu-btn"
          elevation="2"
          size="small"
          rounded="pill"
          icon="mdi-dots-vertical"
          @click.stop
        />
      </template>
      <v-list class="mesa-menu-list">
        <v-list-item
          v-for="item in menuItems"
          :key="item.id"
          @click="onMenuSelect(item.id)"
          class="mesa-menu-item"
          rounded="pill"
        
        >
          <div class="menu-item-content">
            <v-icon size="25" class="menu-item-icon">{{ item.icon }}</v-icon>
            <span class="menu-item-text">{{ item.text }}</span>
          </div>
        </v-list-item>
      </v-list>
    </v-menu>

    <!-- Contenido principal del botón -->
    <div class="mesa-content">
      <div class="mesa-icon-container">
        <img
          :src="iconUrl"
          alt="Mesa"
          class="mesa-icon"
        />
      </div>

      <div class="mesa-name">{{ nombre }}</div>

      <!-- Chip visible cuando no es libre -->
      <v-chip
        v-if="estado !== 'libre'"
        :color="chipColor"
        size="small"
        class="mesa-estado-chip"
        rounded="pill"
        variant="elevated"
      >
        {{ estadoText }}
      </v-chip>
      
      <!-- Spacer transparente cuando es libre para mantener la altura -->
      <div
        v-else
        class="mesa-estado-spacer"
      ></div>
    </div>
  </v-btn>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { MesaEstado, MesasAccion } from "../../models/mesaZona";


const props = defineProps<{
  nombre: string;
  id: number;
  estado: MesaEstado['estado'];
  iconUrl?: string;
  mesaAccion: MesasAccion | null;
}>();

const emit = defineEmits<{
  mesaClick: [];
  menuSelect: [action:string];
  error: [];
}>();

const menuItems = [
  { id: 'juntar', icon: 'mdi-table-merge-cells', text: 'Juntar mesa' },
  { id: 'mover', icon: 'mdi-cursor-move', text: 'Mover mesa' },
  { id: 'borrar', icon: 'mdi-delete', text: 'Borrar mesa' }
];

const chipColor = computed(() => {
  switch (props.estado) {
    case 'ocupada':
      return 'success';
    case 'impresa':
      return 'error';
    default:
      return 'grey';
  }
});

const estadoText = computed(() => {
  switch (props.estado) {
    case 'ocupada':
      return 'Ocupada';
    case 'impresa':
      return 'Impresa';
    default:
      return '';
  }
});

const iconUrl = computed(() =>
  props.iconUrl || '/planing/mesa_redonda.svg'
);

function onMesaClick() {
  const accion = props.mesaAccion;
  if (accion?.tipo === null || accion?.mesa.ID !== props.id) {
    emit('mesaClick');
  }else{
    emit('error')
  }
}

function onMenuSelect(action: string) {
  emit('menuSelect', action);
}
</script>


<style scoped>
.mesa-btn {
  background-color: rgba(var(--v-theme-primary, 21,101,192), 0.12) !important;
  border: 1.5px solid var(--v-theme-primary) !important;
  width: 100% !important;
  height: 100% !important;
  min-width: 120px !important;
  min-height: 120px !important;
  padding: 1em !important;
  box-sizing: border-box;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  position: relative !important;
  font-size: 0.9rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mesa-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.mesa-menu {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1000;
  pointer-events: auto;
}

.mesa-menu-btn {
  position: absolute !important;
  top: 8px !important;
  right: 8px !important;
  background-color: rgba(var(--v-theme-surface), 0.9) !important;
  border: 1px solid var(--v-theme-primary) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(4px);
  min-width: 36px !important;
  width: 36px !important;
  height: 36px !important;
  z-index: 1001;
}

.mesa-menu-btn:hover {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
  transform: scale(1.05);
}

.mesa-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  height: 100%;
  padding: 8px 0;
}

.mesa-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
}

.mesa-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: var(--v-theme-on-surface-filter, invert(0));
}

.mesa-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--v-theme-on-surface);
  text-align: center;
  line-height: 1.2;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mesa-estado-chip {
  font-size: 0.7rem !important;
  font-weight: 500 !important;
  height: 20px !important;
  min-width: 50px;
}

.mesa-estado-spacer {
  height: 20px !important;
  min-width: 50px;
  background: transparent;
  visibility: hidden;
}

.mesa-menu-list {
  min-width: 160px;
  background: var(--v-theme-surface);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(16, 24, 40, 0.15);
  border: 1px solid rgba(var(--v-theme-primary), 0.12);
  padding: 8px;
  backdrop-filter: blur(8px);
  z-index: 1002 !important;
}

.mesa-menu-item {
  cursor: pointer;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 4px;
  background: rgba(var(--v-theme-primary), 0.04);
  border: 1px solid transparent;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.mesa-menu-item:last-child {
  margin-bottom: 0;
}

.mesa-menu-item:hover {
  background: rgba(var(--v-theme-primary), 0.08);
  border-color: rgba(var(--v-theme-primary), 0.2);
  transform: translateX(2px);
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-item-icon {
  color: var(--v-theme-primary) !important;
  opacity: 0.8;
}

.menu-item-text {
  font-size: 0.875rem;
  color: var(--v-theme-on-surface);
  font-weight: 500;
}

/* Tema oscuro */
@media (prefers-color-scheme: dark) {
  .mesa-icon {
    filter: invert(1);
  }
}

/* Animaciones */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.mesa-estado-chip.v-chip--variant-elevated {
  animation: pulse 2s infinite;
}

/* Responsive */
@media (max-width: 768px) {
  .mesa-btn {
    min-width: 100px !important;
    min-height: 100px !important;
  }
  
  .mesa-icon-container {
    width: 40px;
    height: 40px;
  }
  
  .mesa-name {
    font-size: 0.8rem;
  }
}
</style>