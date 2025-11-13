<template>
  <v-snackbar
    v-model="isVisible"
    :timeout="timeout"
    :location="location"
    :color="colorConfig.background"
    rounded="xl"
    elevation="8"
    class="ui-snackbar"
  >
    <div class="snackbar-content">
      <v-icon 
        :icon="iconConfig.icon" 
        :color="iconConfig.color"
        size="large" 
        class="snackbar-icon"
      />
      
      <span class="snackbar-message">{{ message }}</span>
      
      <v-btn
        variant="text"
        icon="mdi-close"
        size="small"
        @click="close"
        class="close-btn"
        :color="iconConfig.color"
      />
    </div>
  </v-snackbar>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

export interface UiSnackbarProps {
  modelValue: boolean;
  message: string;
  type?: 'info' | 'success' | 'warning' | 'error';
  timeout?: number;
  location?: 'top' | 'top center' | 'bottom' | 'bottom center' | 'left' | 'right';
}

const props = withDefaults(defineProps<UiSnackbarProps>(), {
  type: 'info',
  timeout: 4000,
  location: 'top center'
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'close': [];
}>();

const isVisible = ref(props.modelValue);

watch(() => props.modelValue, (newValue) => {
  isVisible.value = newValue;
});

watch(isVisible, (newValue) => {
  emit('update:modelValue', newValue);
});

const typeConfigs = {
  info: {
    background: '#e3f2fd',
    icon: 'mdi-information',
    iconColor: '#1976d2'
  },
  success: {
    background: '#e8f5e8',
    icon: 'mdi-check-circle',
    iconColor: '#2e7d32'
  },
  warning: {
    background: '#fff8e1',
    icon: 'mdi-alert',
    iconColor: '#f57c00'
  },
  error: {
    background: '#ffebee',
    icon: 'mdi-alert-circle',
    iconColor: '#d32f2f'
  }
};

const colorConfig = computed(() => ({
  background: typeConfigs[props.type].background
}));

const iconConfig = computed(() => ({
  icon: typeConfigs[props.type].icon,
  color: typeConfigs[props.type].iconColor
}));

function close() {
  isVisible.value = false;
  emit('close');
}
</script>

<style scoped>
.ui-snackbar :deep(.v-snackbar__wrapper) {
  max-width: 600px;
  min-width: 300px;
}

.snackbar-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.snackbar-icon {
  flex-shrink: 0;
}

.snackbar-message {
  flex: 1;
  font-size: 1.25rem;
  font-weight: 500;
  line-height: 1.4;
}

.close-btn {
  flex-shrink: 0;
  margin-left: auto;
}

.close-btn :deep(.v-btn__overlay) {
  opacity: 0.1;
}

.close-btn:hover :deep(.v-btn__overlay) {
  opacity: 0.2;
}
</style>