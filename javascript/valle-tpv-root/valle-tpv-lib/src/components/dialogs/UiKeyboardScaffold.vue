<template>
  <v-dialog
    :model-value="modelValue"
    :max-width="width"
    persistent
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <v-card class="keyboard-scaffold" elevation="12">
      <!-- Título opcional pequeño con fondo negro (sin botón X) -->
      <v-card-title v-if="title" class="keyboard-title pa-3">
        <v-icon v-if="icon" size="small" class="mr-2">{{ icon }}</v-icon>
        <span class="text-subtitle-2 font-weight-medium">{{ title }}</span>
      </v-card-title>

      <!-- Contenido del teclado -->
      <v-card-text class="pa-3">
        <slot />
      </v-card-text>

      <!-- Acciones táctiles grandes -->
      <v-card-actions v-if="actions.length > 0" class="pa-3">
        <v-btn
          v-for="action in actions"
          :key="action.id"
          :color="action.color"
          :variant="action.variant || 'elevated'"
          size="large"
          block
          class="mx-1 keyboard-action-btn"
          @click="$emit('on_action', action.id)"
        >
          <v-icon v-if="action.icon" class="mr-2">{{ action.icon }}</v-icon>
          <span class="text-button">{{ action.text }}</span>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
interface Action {
  id: string;
  text: string;
  icon?: string;
  color?: string;
  variant?: 'elevated' | 'flat' | 'tonal' | 'outlined' | 'text' | 'plain';
}

interface Props {
  modelValue: boolean;
  title?: string;
  icon?: string;
  width?: string;
  actions?: Action[];
}

withDefaults(defineProps<Props>(), {
  title: '',
  icon: '',
  width: 'auto',
  actions: () => []
});

defineEmits<{
  'update:modelValue': [value: boolean];
  'on_action': [id: string];
}>();
</script>

<style scoped>
.keyboard-scaffold {
  border-radius: 16px !important;
}

.keyboard-title {
  background: #1a1a1a;
  color: white;
  min-height: 36px !important;
  border-radius: 16px 16px 0 0;
  display: flex;
  align-items: center;
}

.keyboard-title .v-icon {
  color: white;
}

/* Botones de acción táctiles */
.keyboard-action-btn {
  min-height: 56px !important;
  height: 56px !important;
  border-radius: 12px !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px !important;
}

/* Mejorar área táctil */
.keyboard-action-btn {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  touch-action: manipulation;
}
</style>
