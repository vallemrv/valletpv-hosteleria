<template>
  <v-dialog v-model="modelValue" :max-width="width" persistent>
    <v-card elevation="16" rounded="xl" class="ui-dialog-card">
      <v-card-title class="text-h5 font-weight-bold pa-6 bg-surface-variant d-flex flex-column align-center justify-center">
        <v-avatar v-if="icon" size="56" class="dialog-icon elevation-6 mb-2">
          <v-icon size="38" color="primary">{{ icon }}</v-icon>
        </v-avatar>
        <span class="dialog-title">{{ title }}</span>
      </v-card-title>
      <v-card-text class="dialog-content pa-4">
        <slot />
      </v-card-text>
      <v-card-actions class="dialog-actions pa-6" :class="{ 'block-actions': hasBlockActions }">
        <template v-if="hasBlockActions">
          <v-btn
            v-for="action in actions"
            :key="action.id"
            class="dialog-action-btn action-button-block"
            rounded="16"
            elevation="3"
            height="72"
            block
            :color="action.color || 'primary'"
            @click="handleAction(action.id)"
          >
            <v-icon v-if="action.icon" size="20" class="me-2">{{ action.icon }}</v-icon>
            {{ action.text }}
          </v-btn>
        </template>
        <template v-else>
          <v-spacer />
          <v-btn
            v-for="action in actions"
            :key="action.id"
            class="dialog-action-btn action-button-small"
            rounded="16"
            elevation="3"
            height="72"
            :color="action.color || 'primary'"
            @click="handleAction(action.id)"
          >
            <v-icon v-if="action.icon" size="20" class="me-2">{{ action.icon }}</v-icon>
            {{ action.text }}
          </v-btn>
        </template>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  icon: { type: String, required: false },
  title: { type: String, required: true },
  width: { type: String, default: '420px' },
  actions: {
    type: Array as () => Array<{ id: string, text: string, icon?: string, color?: string, block?: boolean }>,
    default: () => []
  }
});

const width = props.width; 
const emit = defineEmits(['update:modelValue', 'on_action']);

// Computed para verificar si alguna acción tiene la propiedad block
const hasBlockActions = computed(() => {
  return props.actions.some(action => action.block === true);
});

function handleAction(id: string) {
  emit('on_action', id);
  emit('update:modelValue', false); // Cierra el diálogo por defecto
}

const modelValue = ref(props.modelValue);
watch(() => props.modelValue, v => modelValue.value = v);
watch(modelValue, v => emit('update:modelValue', v));
</script>

<style scoped>
.ui-dialog-card {
  overflow: hidden;
}

.dialog-icon {
  background: rgba(var(--v-theme-primary, 21,101,192), 0.12);
}

.dialog-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--v-theme-on-surface);
  text-align: center;
}

.dialog-content {
  font-size: 1.08rem;
  color: var(--v-theme-on-surface);
  text-align: center;
}

.dialog-actions {
  justify-content: flex-end;
  gap: 0.5em;
}

/* Botones de acción */
.action-button-small {
  min-width: 110px;
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.01em;
}

.action-button-block {
  height: 72px !important;
  font-size: 1.1rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.01em;
  margin-bottom: 1rem;
}

.action-button-block:last-child {
  margin-bottom: 0;
}

.block-actions {
  flex-direction: column;
  gap: 0.5rem;
}
</style>
