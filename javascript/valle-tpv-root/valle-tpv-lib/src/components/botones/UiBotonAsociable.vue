<template>
  <v-btn
    :class="['zona-btn', { 'zona-selected': selected }]"
    :color="selected ? 'primary' : 'default'"
    :variant="selected ? 'elevated' : 'outlined'"
    elevation="2"
    rounded="lg"
    @click="$emit('click')"
  >
    <!-- Checkbox flotante para asociar -->
    <v-checkbox
      v-if="showCheckbox"
      :model-value="asociado"
      class="zona-checkbox-float"
      color="success"
      hide-details
      density="comfortable"
      @click.stop
      @update:model-value="(val: boolean | null) => $emit('toggle-asociacion', !!val)"
    />
    
    <div class="zona-btn-content">
      <!-- Imagen de internet si está disponible -->
      <img
        v-if="showImage"
        :src="imageUrl"
        :alt="texto"
        class="zona-image"
        @error="onImageError"
      />
      <!-- Icono MDI como fallback o por defecto -->
      <v-icon
        v-else
        class="zona-icon"
        :icon="icon || 'mdi-map-marker'"
      />
      <span class="zona-text">{{ texto }}</span>
    </div>
  </v-btn>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const props = withDefaults(defineProps<{
  texto: string;
  icon?: string;
  imageUrl?: string;
  selected?: boolean;
  asociado?: boolean;
  showCheckbox?: boolean;
}>(), {
  showCheckbox: true
});


defineEmits<{
  click: [];
  'toggle-asociacion': [value: boolean];
}>();

// Variable reactiva para controlar si la imagen falló al cargar
const imageError = ref(false);

// Función para manejar errores de carga de imagen
function onImageError() {
  imageError.value = true;
}

// Mostrar imagen solo si existe URL y no ha fallado
const showImage = computed(() => props.imageUrl && !imageError.value);
</script>

<style scoped>
.zona-btn {
  flex: 1;
  min-height: 0 !important;
  max-height: none !important;
  width: 100% !important;
  height: auto !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-width: 2px !important;
  aspect-ratio: unset;
  margin: 0;
  position: relative;
}

.zona-btn:not(.zona-selected) {
  background: rgba(var(--v-theme-surface), 0.8) !important;
  border-color: rgba(var(--v-theme-outline), 0.5) !important;
}

.zona-btn.zona-selected {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.3) !important;
}

.zona-btn:hover:not(.zona-selected) {
  transform: translateX(2px);
  border-color: rgba(var(--v-theme-primary), 0.5) !important;
  background: rgba(var(--v-theme-primary), 0.05) !important;
}

.zona-btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  height: 100%;
  padding: 6px 4px;
}

.zona-icon {
  font-size: 1.2rem !important;
  color: var(--v-theme-primary) !important;
}

.zona-image {
  width: 1.2rem;
  height: 1.2rem;
  object-fit: contain;
  object-position: center;
  filter: brightness(0) saturate(100%) invert(27%) sepia(51%) saturate(2878%) hue-rotate(346deg) brightness(104%) contrast(97%);
}

.zona-text {
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--v-theme-on-surface);
  text-align: center;
  line-height: 1.1;
  word-wrap: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  max-width: 100%;
  white-space: normal;
}

.zona-selected .zona-text {
  font-weight: 600;
}

/* Checkbox flotante para asociación */
.zona-checkbox-float {
  position: absolute !important;
  top: 2px !important;
  right: 2px !important;
  z-index: 1000;
  width: 36px !important;
  height: 36px !important;
  margin: 0 !important;
  padding: 0 !important;
  transform: scale(1.2);
}

.zona-checkbox-float :deep(.v-input__control) {
  min-height: 36px !important;
  width: 36px !important;
  justify-content: center !important;
  align-items: center !important;
}

.zona-checkbox-float :deep(.v-selection-control) {
  min-height: 36px !important;
  justify-content: center !important;
}

.zona-checkbox-float :deep(.v-selection-control__wrapper) {
  width: 36px !important;
  height: 36px !important;
  justify-content: center !important;
  align-items: center !important;
}

.zona-checkbox-float :deep(.v-icon) {
  font-size: 20px !important;
}

.zona-checkbox-float:hover {
  transform: scale(1.3);
}
</style>
