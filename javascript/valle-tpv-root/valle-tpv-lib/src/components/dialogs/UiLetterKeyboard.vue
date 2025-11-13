<template>
  <UiKeyboardScaffold
    v-model="dialogModel"
    :title="title"
    :icon="showTitle ? 'mdi-keyboard' : ''"
    :width="width"
    :actions="dialogActions"
    @on_action="onDialogAction"
  >
    <div class="keyboard-content">
      <!-- Display -->
      <div class="display-container pa-4 mb-3">
        <div class="display-value">
          {{ displayValue || '' }}
        </div>
      </div>

      <!-- Teclado de letras -->
      <div class="keyboard-grid pa-2">
        <!-- Fila 1: Q - P -->
        <v-row dense>
          <v-col v-for="letter in firstRow" :key="letter" cols="auto">
            <v-btn
              size="large"
              variant="elevated"
              color="primary"
              class="keyboard-btn letter-btn"
              @click="onLetterClick(letter)"
            >
              {{ uppercase ? letter.toUpperCase() : letter }}
            </v-btn>
          </v-col>
        </v-row>

        <!-- Fila 2: A - Ñ -->
        <v-row dense class="justify-center">
          <v-col v-for="letter in secondRow" :key="letter" cols="auto">
            <v-btn
              size="large"
              variant="elevated"
              color="primary"
              class="keyboard-btn letter-btn"
              @click="onLetterClick(letter)"
            >
              {{ uppercase ? letter.toUpperCase() : letter }}
            </v-btn>
          </v-col>
        </v-row>

        <!-- Fila 3: Z - M -->
        <v-row dense class="justify-center">
          <v-col v-for="letter in thirdRow" :key="letter" cols="auto">
            <v-btn
              size="large"
              variant="elevated"
              color="primary"
              class="keyboard-btn letter-btn"
              @click="onLetterClick(letter)"
            >
              {{ uppercase ? letter.toUpperCase() : letter }}
            </v-btn>
          </v-col>
        </v-row>

        <!-- Fila 4: Controles -->
        <v-row dense class="mt-2">
          <v-col cols="3">
            <v-btn
              size="large"
              block
              variant="elevated"
              color="secondary"
              class="keyboard-btn control-btn"
              @click="toggleUppercase"
            >
              <v-icon>{{ uppercase ? 'mdi-alpha-a-box' : 'mdi-alpha-a-box-outline' }}</v-icon>
            </v-btn>
          </v-col>
          <v-col cols="6">
            <v-btn
              size="large"
              block
              variant="elevated"
              color="primary"
              class="keyboard-btn space-btn"
              @click="onSpaceClick"
            >
              <span>ESPACIO</span>
            </v-btn>
          </v-col>
          <v-col cols="3">
            <v-btn
              size="large"
              block
              variant="elevated"
              color="warning"
              class="keyboard-btn control-btn"
              @click="onBackspaceClick"
            >
              <v-icon>mdi-backspace-outline</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </div>
    </div>
  </UiKeyboardScaffold>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import UiKeyboardScaffold from './UiKeyboardScaffold.vue';

interface Props {
  modelValue?: boolean;
  title?: string;
  value?: string;
  maxLength?: number;
  showTitle?: boolean;
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: 'Introduce texto',
  value: '',
  maxLength: 100,
  showTitle: true,
  width: '800px'
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
const uppercase = ref(false);

// Distribución del teclado QWERTY español
const firstRow = ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'];
const secondRow = ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ñ'];
const thirdRow = ['z', 'x', 'c', 'v', 'b', 'n', 'm'];

const displayValue = computed(() => {
  return internalValue.value || '';
});

// Sincronizar valor inicial cuando se abre el diálogo
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    internalValue.value = props.value || '';
    uppercase.value = false;
  }
});

function onLetterClick(letter: string) {
  // Verificar longitud máxima
  if (internalValue.value.length >= props.maxLength) {
    return;
  }
  
  const letterToAdd = uppercase.value ? letter.toUpperCase() : letter;
  internalValue.value += letterToAdd;
}

function onSpaceClick() {
  // Verificar longitud máxima
  if (internalValue.value.length >= props.maxLength) {
    return;
  }
  
  internalValue.value += ' ';
}

function onBackspaceClick() {
  if (internalValue.value.length > 0) {
    internalValue.value = internalValue.value.slice(0, -1);
  }
}

function toggleUppercase() {
  uppercase.value = !uppercase.value;
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
  font-size: 1.8rem;
  font-weight: 600;
  text-align: left;
  color: white;
  min-height: 50px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  word-break: break-all;
}

.keyboard-grid {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 12px;
}

.keyboard-btn {
  border-radius: 8px !important;
  font-size: 1.2rem !important;
  font-weight: 600 !important;
  margin: 2px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.15s ease !important;
  min-width: 45px !important;
  height: 50px !important;
}

.keyboard-btn:active {
  transform: scale(0.95);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2) !important;
}

.letter-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
}

.space-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%) !important;
  font-size: 1rem !important;
}

.control-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
}

/* Mejorar táctil */
.keyboard-btn {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  touch-action: manipulation;
}

/* Responsive */
@media (max-width: 768px) {
  .keyboard-btn {
    min-width: 40px !important;
    height: 45px !important;
    font-size: 1rem !important;
  }
  
  .display-value {
    font-size: 1.5rem;
    min-height: 40px;
  }
}

@media (max-width: 600px) {
  .keyboard-btn {
    min-width: 35px !important;
    height: 40px !important;
    font-size: 0.9rem !important;
  }
}
</style>
