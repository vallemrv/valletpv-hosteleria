<template>
  <UiKeyboardScaffold
    v-model="dialogModel"
    :title="title"
    :icon="showTitle ? 'mdi-calculator' : ''"
    :width="width"
    :actions="dialogActions"
    @on_action="onDialogAction"
  >
    <div class="keyboard-content">
      <!-- Display -->
      <div class="display-container pa-4 mb-3">
        <div class="display-value">
          {{ displayValue || '0' }}
        </div>
      </div>

      <!-- Teclado numérico -->
      <v-row dense class="keyboard-grid">
        <!-- Fila 1: 7, 8, 9 -->
        <v-col cols="4" v-for="num in [7, 8, 9]" :key="num">
          <v-btn
            size="x-large"
            block
            variant="elevated"
            color="primary"
            class="keyboard-btn number-btn"
            @click="onNumberClick(num)"
          >
            <span class="btn-text">{{ num }}</span>
          </v-btn>
        </v-col>

        <!-- Fila 2: 4, 5, 6 -->
        <v-col cols="4" v-for="num in [4, 5, 6]" :key="num">
          <v-btn
            size="x-large"
            block
            variant="elevated"
            color="primary"
            class="keyboard-btn number-btn"
            @click="onNumberClick(num)"
          >
            <span class="btn-text">{{ num }}</span>
          </v-btn>
        </v-col>

        <!-- Fila 3: 1, 2, 3 -->
        <v-col cols="4" v-for="num in [1, 2, 3]" :key="num">
          <v-btn
            size="x-large"
            block
            variant="elevated"
            color="primary"
            class="keyboard-btn number-btn"
            @click="onNumberClick(num)"
          >
            <span class="btn-text">{{ num }}</span>
          </v-btn>
        </v-col>

        <!-- Fila 4: Decimal, 0, Backspace -->
        <v-col cols="4">
          <v-btn
            v-if="showDecimal"
            size="x-large"
            block
            variant="elevated"
            color="secondary"
            class="keyboard-btn special-btn"
            @click="onDecimalClick"
            :disabled="hasDecimal"
          >
            <span class="btn-text">{{ decimalSeparator }}</span>
          </v-btn>
        </v-col>

        <v-col cols="4">
          <v-btn
            size="x-large"
            block
            variant="elevated"
            color="primary"
            class="keyboard-btn number-btn zero-btn"
            @click="onNumberClick(0)"
          >
            <span class="btn-text">0</span>
          </v-btn>
        </v-col>

        <v-col cols="4">
          <v-btn
            size="x-large"
            block
            variant="elevated"
            color="warning"
            class="keyboard-btn special-btn"
            @click="onBackspaceClick"
          >
            <v-icon size="32">mdi-backspace-outline</v-icon>
          </v-btn>
        </v-col>
      </v-row>
    </div>
  </UiKeyboardScaffold>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import UiKeyboardScaffold from './UiKeyboardScaffold.vue';

interface Props {
  modelValue?: boolean;
  title?: string;
  value?: string | number;
  showDecimal?: boolean;
  decimalSeparator?: string;
  maxLength?: number;
  maxDecimals?: number;
  showTitle?: boolean;
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: 'Introduce un valor',
  value: '',
  showDecimal: true,
  decimalSeparator: ',',
  maxLength: 10,
  maxDecimals: 2,
  showTitle: true,
  width: '450px'
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'on_action': [action: { id: string; data?: string }];
}>();

const dialogModel = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

const internalValue = ref('');

const displayValue = computed(() => {
  return internalValue.value || '0';
});

const hasDecimal = computed(() => {
  return internalValue.value.includes(props.decimalSeparator);
});

// Sincronizar valor inicial cuando se abre el diálogo
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    internalValue.value = String(props.value || '');
  }
});

function onNumberClick(num: number) {
  const currentValue = internalValue.value;
  
  // Verificar longitud máxima
  if (currentValue.length >= props.maxLength) {
    return;
  }
  
  // Si hay decimales, verificar límite de decimales
  if (hasDecimal.value) {
    const parts = currentValue.split(props.decimalSeparator);
    if (parts[1] && parts[1].length >= props.maxDecimals) {
      return;
    }
  }
  
  internalValue.value = currentValue === '0' ? String(num) : currentValue + num;
}

function onDecimalClick() {
  if (hasDecimal.value) return;
  
  const currentValue = internalValue.value;
  internalValue.value = currentValue ? currentValue + props.decimalSeparator : '0' + props.decimalSeparator;
}

function onBackspaceClick() {
  if (internalValue.value.length > 0) {
    internalValue.value = internalValue.value.slice(0, -1) || '0';
  }
}

const dialogActions = ref([
  { id: 'cancelar', text: 'Cancelar', icon: 'mdi-close', color: 'grey' },
  { id: 'aceptar', text: 'Aceptar', icon: 'mdi-check', color: 'primary' }
]);

function onDialogAction(id: string) {
  if (id === 'aceptar') {
    emit('on_action', { id, data: internalValue.value });
  } else if (id === 'cancelar') {
    emit('on_action', { id });
  }
}
</script>

<style scoped>
.keyboard-content {
  padding: 16px 0;
}

.display-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
}

.display-value {
  font-size: 2.5rem;
  font-weight: bold;
  text-align: right;
  color: white;
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 16px;
  letter-spacing: 2px;
}

.keyboard-grid {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 12px;
}

.keyboard-btn {
  height: 80px !important;
  min-height: 80px !important;
  border-radius: 12px !important;
  font-size: 1.8rem !important;
  font-weight: bold !important;
  margin: 4px 0 !important;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.2s ease !important;
}

.keyboard-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2) !important;
}

.number-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
}

.zero-btn {
  font-size: 2rem !important;
}

.special-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
}

.btn-text {
  font-size: 2rem;
  font-weight: 700;
}

/* Mejorar táctil */
.keyboard-btn {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  touch-action: manipulation;
}

/* Responsive */
@media (max-width: 600px) {
  .keyboard-btn {
    height: 70px !important;
    min-height: 70px !important;
  }
  
  .display-value {
    font-size: 2rem;
    min-height: 50px;
  }
  
  .btn-text {
    font-size: 1.6rem;
  }
}
</style>
