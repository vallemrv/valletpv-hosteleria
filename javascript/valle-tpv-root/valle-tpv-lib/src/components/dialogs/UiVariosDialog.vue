<template>
  <UiDialogScaffold v-model="dialogVarios" title="Crear Producto Varios" icon="mdi-package-variant-plus" width="500px"
    :actions="dialogActions" @on_action="onDialogAction">
    <div class="varios-form">
      <v-form>
        <v-text-field v-model.number="variosForm.cantidad" label="Cantidad" hint="1" type="number" min="1" outlined
          dense readonly class="mb-4" @click="openKeyboard('cantidad')" />
        <v-text-field v-model="variosForm.descripcion" label="Descripción" hint="Varios" outlined dense readonly
          class="mb-4" @click="openKeyboard('descripcion')" />
        <v-text-field v-model.number="variosForm.precio" label="Precio" hint="Precio" type="number" min="0" step="0.01"
          outlined dense readonly @click="openKeyboard('precio')" />
      </v-form>
    </div>
  </UiDialogScaffold>

  <!-- Diálogo del Teclado Numérico -->
  <UiNumericKeyboard v-model="showNumericKeyboard" :title="keyboardTitle" :value="getActiveFieldValue()"
    :show-decimal="activeField === 'precio'" @on_action="onNumericAction" />

  <!-- Diálogo del Teclado de Letras -->
  <UiLetterKeyboard v-model="showLetterKeyboard" :title="keyboardTitle" :value="variosForm.descripcion || ''"
    :max-length="100" @on_action="onLetterAction" />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import UiDialogScaffold from './UiDialogScaffold.vue';
import UiNumericKeyboard from './UiNumericKeyboard.vue';
import UiLetterKeyboard from './UiLetterKeyboard.vue';

// Props
interface Props {
  modelValue?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false
});

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'on_action': [action: { id: string; data?: any }];
}>();

// Estado del diálogo
const dialogVarios = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

// Campo activo y estados de teclados
const activeField = ref<'cantidad' | 'descripcion' | 'precio' | null>(null);
const showNumericKeyboard = ref(false);
const showLetterKeyboard = ref(false);

// Formulario
const variosForm = ref({
  cantidad: null as number | null,
  descripcion: null as string | null,
  precio: null as number | null
});

const isProcessing = ref(false);

// Título del teclado
const keyboardTitle = computed(() => {
  if (activeField.value === 'cantidad') return 'Cantidad';
  if (activeField.value === 'descripcion') return 'Descripción';
  if (activeField.value === 'precio') return 'Precio';
  return '';
});

// Resetear formulario cuando se abre el diálogo
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    // Resetear a valores por defecto cuando se abre
    variosForm.value = {
      cantidad: null,
      descripcion: null,
      precio: null
    };
    activeField.value = null;
    showNumericKeyboard.value = false;
    showLetterKeyboard.value = false;
    isProcessing.value = false;
  }
});

// Abrir el teclado correspondiente
function openKeyboard(field: 'cantidad' | 'descripcion' | 'precio') {
  activeField.value = field;

  if (field === 'cantidad' || field === 'precio') {
    showNumericKeyboard.value = true;
    showLetterKeyboard.value = false;
  } else if (field === 'descripcion') {
    showLetterKeyboard.value = true;
    showNumericKeyboard.value = false;
  }
}

// Obtener el valor del campo activo
function getActiveFieldValue(): string {
  if (activeField.value === 'cantidad') {
    return variosForm.value.cantidad?.toString() || '';
  } else if (activeField.value === 'precio') {
    return variosForm.value.precio?.toString() || '';
  }
  return '';
}

// Manejar acción del teclado numérico
function onNumericAction(action: { id: string; data?: string }) {
  if (action.id === 'aceptar' && action.data) {
    const value = action.data.replace(',', '.');

    if (activeField.value === 'cantidad') {
      const num = parseFloat(value);
      variosForm.value.cantidad = isNaN(num) ? null : Math.max(1, Math.floor(num));
    } else if (activeField.value === 'precio') {
      const num = parseFloat(value);
      variosForm.value.precio = isNaN(num) ? null : Math.max(0, num);
    }
  }

  showNumericKeyboard.value = false;
  activeField.value = null;
}

// Manejar acción del teclado de letras
function onLetterAction(action: { id: string; data?: string }) {
  if (action.id === 'aceptar') {
    variosForm.value.descripcion = action.data || null;
  }

  showLetterKeyboard.value = false;
  activeField.value = null;
}

// Acciones del diálogo
const dialogActions = ref([
  { id: 'cancelar', text: 'Cancelar', icon: 'mdi-close', color: 'grey' },
  { id: 'aceptar', text: 'Aceptar', icon: 'mdi-check', color: 'primary' }
]);

// Manejar acciones del diálogo
function onDialogAction(id: string) {
  if (id === 'aceptar') {
    if (isProcessing.value) return;
    isProcessing.value = true;

    const varios = {
      cantidad: variosForm.value.cantidad || 1,
      descripcion: variosForm.value.descripcion || 'Varios',
      precio: variosForm.value.precio || 0
    };
    emit('on_action', { id, data: varios });
  } else if (id === 'cancelar') {
    emit('on_action', { id });
  }
}
</script>
