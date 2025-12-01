<template>
  <UiMainWindows title="Gestión de Camareros" icon="mdi-account-group" nombre="Administrador" :tareasPendientes="0">
    <template #actions>
      <UiActionButton icon="mdi-plus" @click="dialog = true">Agregar Camarero</UiActionButton>
      <UiActionButton v-if="camarerosAuth.length > 0" icon="mdi-cash-register" @click="irAlTPV">Ir al TPV
      </UiActionButton>
    </template>
    <v-row class="h-100 ma-0" align="stretch">
      <v-col cols="12" md="6">
        <v-card elevation="2" class="mb-4 h-100">
          <v-card-title>
            <v-icon class="me-2" color="grey">mdi-account-off</v-icon>
            Camareros No Autorizados
          </v-card-title>
          <v-divider />
          <div class="scroll-list" ref="listNoAuth">
            <v-list>
              <v-list-item v-for="camarero in camarerosNoAuth" :key="camarero.id"
                @click="toggleAutorizado(camarero.id, 1)">
                <template v-slot:prepend>
                  <v-avatar color="grey-lighten-2">
                    <v-icon color="grey-darken-1">mdi-account-off</v-icon>
                  </v-avatar>
                </template>

                <v-list-item-title>{{ camarero.nombre }} {{ camarero.apellidos }}</v-list-item-title>

                <template v-slot:append>
                  <v-icon class="hint-icon">mdi-arrow-right</v-icon>
                </template>
              </v-list-item>
            </v-list>
          </div>
        </v-card>
      </v-col>
      <v-col cols="12" md="6">
        <v-card elevation="2" class="mb-4 h-100">
          <v-card-title>
            <v-icon class="me-2" color="success">mdi-check-circle</v-icon>
            Camareros Autorizados
          </v-card-title>
          <v-divider />
          <div class="scroll-list" ref="listAuth">
            <v-list>
              <v-list-item v-for="camarero in camarerosAuth" :key="camarero.id"
                @click="toggleAutorizado(camarero.id, 0)">
                <template v-slot:prepend>
                  <v-icon class="hint-icon me-3">mdi-arrow-left</v-icon>
                  <v-avatar color="primary">
                    <v-icon color="white">mdi-account</v-icon>
                  </v-avatar>
                </template>

                <v-list-item-title>{{ camarero.nombre }} {{ camarero.apellidos }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </div>
        </v-card>
      </v-col>
    </v-row>


    <UiDialogScaffold v-model="dialog" title="Nuevo Camarero" :actions="dialogActions" @on_action="handleDialogAction">
      <v-row>
        <v-col cols="12">
          <v-text-field v-model="nombre" label="Nombre" readonly @click="openKeyboard('nombre')">
            <template #append-inner>
              <v-icon>mdi-keyboard</v-icon>
            </template>
          </v-text-field>
        </v-col>
        <v-col cols="12">
          <v-text-field v-model="apellidos" label="Apellidos" readonly @click="openKeyboard('apellidos')">
            <template #append-inner>
              <v-icon>mdi-keyboard</v-icon>
            </template>
          </v-text-field>
        </v-col>
      </v-row>
    </UiDialogScaffold>

    <!-- Diálogo de Teclado de Letras -->
    <UiLetterKeyboard v-model="showKeyboard" :title="keyboardTitle" :value="activeFieldValue" :max-length="50"
      @on_action="onKeyboardAction" />
  </UiMainWindows>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCamarerosStore, useTouchScroll } from 'valle-tpv-lib';
import UiLetterKeyboard from '@/lib/components/dialogs/UiLetterKeyboard.vue';

const dialog = ref(false);
const nombre = ref('');
const apellidos = ref('');
const camarerosStore = useCamarerosStore();

// Estado del teclado
const showKeyboard = ref(false);
const activeField = ref<'nombre' | 'apellidos' | null>(null);

// Título y valor del teclado
const keyboardTitle = computed(() => {
  if (activeField.value === 'nombre') return 'Nombre';
  if (activeField.value === 'apellidos') return 'Apellidos';
  return '';
});

const activeFieldValue = computed(() => {
  if (activeField.value === 'nombre') return nombre.value;
  if (activeField.value === 'apellidos') return apellidos.value;
  return '';
});

// Referencias para las listas con scroll
const listNoAuth = ref<HTMLElement | null>(null);
const listAuth = ref<HTMLElement | null>(null);

// Habilitar scroll táctil en las listas (para Ubuntu/Linux con Xorg)
useTouchScroll(listNoAuth);
useTouchScroll(listAuth);

const dialogActions = [
  { id: 'cancel', text: 'Cancelar', color: 'grey' },
  { id: 'save', text: 'Guardar', color: 'primary' }
];

const router = useRouter();
const camarerosAuth = computed(() => {
  return camarerosStore.camarerosAuth;
});
const camarerosNoAuth = computed(() => {
  return camarerosStore.camarerosNoAuth;
});

onMounted(() => {
  camarerosStore.initStore();
});

function guardarCamarero() {
  camarerosStore.altaCamarero(nombre.value, apellidos.value);
  dialog.value = false;
  nombre.value = '';
  apellidos.value = '';
}

// Abrir teclado
function openKeyboard(field: 'nombre' | 'apellidos') {
  activeField.value = field;
  showKeyboard.value = true;
}

// Manejar acción del teclado
function onKeyboardAction(action: { id: string; data?: string }) {
  if (action.id === 'aceptar' && action.data !== undefined) {
    if (activeField.value === 'nombre') {
      nombre.value = action.data;
    } else if (activeField.value === 'apellidos') {
      apellidos.value = action.data;
    }
  }

  showKeyboard.value = false;
  activeField.value = null;
}

function handleDialogAction(actionId: string) {
  if (actionId === 'cancel') {
    dialog.value = false;
  } else if (actionId === 'save') {
    guardarCamarero();
  }
}

function irAlTPV() {
  router.replace({ name: 'CamarerosHome' });
}

function toggleAutorizado(id: number, autorizado: 0 | 1) {
  camarerosStore.setAuth(id, autorizado);
}
</script>

<style scoped>
/* Mejorar el scroll táctil */
.scroll-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  overflow-x: hidden;

  /* Activar scroll suave y táctil */
  -webkit-overflow-scrolling: touch;
  /* iOS */
  scroll-behavior: smooth;
  overscroll-behavior: contain;
  /* Prevenir scroll en cadena */

  /* Ocultar scrollbar pero mantener funcionalidad */
  scrollbar-width: thin;
  /* Firefox */
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}

/* Webkit browsers (Chrome, Safari, Edge) */
.scroll-list::-webkit-scrollbar {
  width: 6px;
}

.scroll-list::-webkit-scrollbar-track {
  background: transparent;
}

.scroll-list::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
  transition: background-color 0.2s;
}

.scroll-list::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.4);
}

/* Mejorar el área táctil de los items */
.v-list-item {
  cursor: pointer;
  touch-action: manipulation;
  /* Optimizar para táctil */
  user-select: none;
  /* Prevenir selección de texto */
  -webkit-tap-highlight-color: transparent;
  /* Quitar highlight en iOS */
}

/* Efecto visual al tocar */
.v-list-item:active {
  opacity: 0.7;
}

.hint-icon {
  opacity: 0.5;
  transition: opacity 0.2s;
}

.v-list-item:hover .hint-icon {
  opacity: 1;
}
</style>
