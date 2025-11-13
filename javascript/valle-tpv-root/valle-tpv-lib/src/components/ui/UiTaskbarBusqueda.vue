<template>
  <v-slide-y-transition>
    <div v-if="visible" class="taskbar-busqueda">
      <v-container fluid class="taskbar-container">
        <v-row align="center" no-gutters>
          <v-col>
            <v-text-field
              v-model="textoBusqueda"
              placeholder="Buscar artículos..."
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-magnify"
              clearable
              hide-details
              autofocus
              readonly
              class="busqueda-input"
              @click="showKeyboard = true"
            />
          </v-col>
          <v-col cols="auto" class="ml-2">
            <v-btn
              icon="mdi-close"
              variant="text"
              size="small"
              @click="cerrar"
            />
          </v-col>
        </v-row>
      </v-container>
    </div>
  </v-slide-y-transition>

  <!-- Teclado flotante no modal -->
  <v-scale-transition>
    <div v-if="visible && showKeyboard" class="floating-keyboard">
      <v-card elevation="12" class="keyboard-card">
        <!-- Header del teclado -->
        <v-card-title class="keyboard-header pa-2">
          <v-icon size="small" class="mr-2">mdi-keyboard</v-icon>
          <span class="text-subtitle-2">Buscar</span>
          <v-spacer />
          <v-btn
            icon="mdi-close"
            size="x-small"
            variant="text"
            @click="showKeyboard = false"
          />
        </v-card-title>

        <!-- Display -->
        <v-card-text class="pa-3">
          <div class="display-container pa-3 mb-3">
            <div class="display-value">
              {{ textoBusqueda || '' }}
            </div>
          </div>

          <!-- Teclado de letras -->
          <div class="keyboard-grid pa-2">
            <!-- Fila 1: Q - P -->
            <v-row dense>
              <v-col v-for="letter in firstRow" :key="letter" cols="auto">
                <v-btn
                  size="small"
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
                  size="small"
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
                  size="small"
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
            <v-row dense class="mt-1">
              <v-col cols="3">
                <v-btn
                  size="small"
                  block
                  variant="elevated"
                  color="secondary"
                  class="keyboard-btn control-btn"
                  @click="toggleUppercase"
                >
                  <v-icon size="small">{{ uppercase ? 'mdi-alpha-a-box' : 'mdi-alpha-a-box-outline' }}</v-icon>
                </v-btn>
              </v-col>
              <v-col cols="6">
                <v-btn
                  size="small"
                  block
                  variant="elevated"
                  color="primary"
                  class="keyboard-btn space-btn"
                  @click="onSpaceClick"
                >
                  <span class="text-caption">ESPACIO</span>
                </v-btn>
              </v-col>
              <v-col cols="3">
                <v-btn
                  size="small"
                  block
                  variant="elevated"
                  color="warning"
                  class="keyboard-btn control-btn"
                  @click="onBackspaceClick"
                >
                  <v-icon size="small">mdi-backspace-outline</v-icon>
                </v-btn>
              </v-col>
            </v-row>
          </div>
        </v-card-text>
      </v-card>
    </div>
  </v-scale-transition>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useTeclasStore } from '../../store/dbStore/teclasStore';

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  close: [];
}>();

const teclasStore = useTeclasStore();

// Estado del teclado
const showKeyboard = ref(false);
const uppercase = ref(false);

// Distribución del teclado QWERTY español
const firstRow = ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'];
const secondRow = ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ñ'];
const thirdRow = ['z', 'x', 'c', 'v', 'b', 'n', 'm'];

const textoBusqueda = computed({
  get: () => teclasStore.textoBusqueda || '',
  set: (value: string) => {
    teclasStore.textoBusqueda = value;
  }
});

// Funciones del teclado
function onLetterClick(letter: string) {
  const letterToAdd = uppercase.value ? letter.toUpperCase() : letter;
  textoBusqueda.value += letterToAdd;
}

function onSpaceClick() {
  textoBusqueda.value += ' ';
}

function onBackspaceClick() {
  if (textoBusqueda.value.length > 0) {
    textoBusqueda.value = textoBusqueda.value.slice(0, -1);
  }
}

function toggleUppercase() {
  uppercase.value = !uppercase.value;
}

function cerrar() {
  showKeyboard.value = false;
  emit('close');
}
</script>

<style scoped>
.taskbar-busqueda {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  width: 50%;
  z-index: 1000;
  background: rgba(var(--v-theme-surface), 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(var(--v-theme-outline), 0.2);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 0 0 12px 12px;
}

.taskbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 16px;
}

.busqueda-input {
  font-size: 1.1rem;
}

.busqueda-input :deep(.v-field__input) {
  padding: 12px 16px;
}

/* Teclado flotante */
.floating-keyboard {
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 999;
  max-width: 600px;
}

.keyboard-card {
  border-radius: 12px !important;
}

.keyboard-header {
  background: #1a1a1a;
  color: white;
  min-height: 36px !important;
  border-radius: 12px 12px 0 0;
  display: flex;
  align-items: center;
}

.keyboard-header .v-icon {
  color: white;
}

.display-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.display-value {
  font-size: 1.2rem;
  font-weight: 600;
  text-align: left;
  color: white;
  min-height: 40px;
  display: flex;
  align-items: center;
  word-break: break-all;
}

.keyboard-grid {
  background: #f5f5f5;
  border-radius: 8px;
}

.keyboard-btn {
  border-radius: 6px !important;
  font-size: 0.9rem !important;
  font-weight: 600 !important;
  margin: 1px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.15s ease !important;
  min-width: 35px !important;
  height: 40px !important;
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

/* Adaptación para teclado virtual en tablets */
@media (max-height: 600px) {
  .taskbar-busqueda {
    top: 60px;
  }
}

/* Responsive para teclado flotante */
@media (max-width: 768px) {
  .floating-keyboard {
    left: 10px;
    right: 10px;
    max-width: none;
  }
  
  .keyboard-btn {
    min-width: 30px !important;
    height: 36px !important;
  }
}

/* Animación suave del taskbar */
.v-slide-y-transition-enter-active,
.v-slide-y-transition-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.v-slide-y-transition-enter-from {
  transform: translateY(-100%);
}

.v-slide-y-transition-leave-to {
  transform: translateY(-100%);
}

/* Animación del teclado */
.v-scale-transition-enter-active,
.v-scale-transition-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.v-scale-transition-enter-from,
.v-scale-transition-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
