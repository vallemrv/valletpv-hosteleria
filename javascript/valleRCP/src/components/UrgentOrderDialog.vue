<template>
  <v-dialog v-model="showDialog" max-width="500" persistent>
    <v-card v-if="urgentOrder" class="border-urgent">
      <v-card-title class="bg-error text-white pa-4">
        <div class="d-flex align-center">
          <v-icon size="large" start>mdi-alert-circle-outline</v-icon>
          <span class="text-h5 font-weight-bold">¡NUEVO PEDIDO URGENTE!</span>
        </div>
      </v-card-title>

      <v-card-text class="pa-6">
        <div class="text-h3 font-weight-black text-center mb-4">
          Mesa {{ urgentOrder.mesa || urgentOrder.pedido_id }}
        </div>
        
        <div class="text-h5 text-center mb-6">
          <v-icon>mdi-account</v-icon> {{ urgentOrder.camarero || 'Sin camarero' }}
        </div>

        <v-divider class="mb-4"></v-divider>

        <div v-if="urgentOrder.lineas && urgentOrder.lineas.length > 0">
          <div class="text-h6 mb-2 font-weight-bold text-error">Artículos Urgentes:</div>
          <v-list density="compact">
            <v-list-item 
              v-for="linea in lineasUrgentes" 
              :key="linea.id"
              class="px-0"
            >
              <template v-slot:prepend>
                <v-avatar color="red-darken-4" size="50" class="mr-3 elevation-4">
                  <span class="text-h5 font-weight-black text-white">{{ linea.cantidad || 1 }}</span>
                </v-avatar>
              </template>
              <v-list-item-title class="text-h6">{{ linea.descripcion }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </div>
      </v-card-text>

      <v-card-actions class="pa-4 pt-0">
        <v-btn 
          block 
          color="error" 
          variant="elevated" 
          size="x-large" 
          @click="closeDialog"
          class="font-weight-bold"
        >
          ENTENDIDO
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useMainStore } from '@/stores/main';

const store = useMainStore();
const showDialog = ref(false);

const urgentOrder = computed(() => store.urgentOrderToShow);

const lineasUrgentes = computed(() => {
  if (!urgentOrder.value || !urgentOrder.value.lineas) return [];
  // Filtrar solo las líneas que son urgentes
  return urgentOrder.value.lineas.filter(l => l.urgente);
});

// Watch for changes in the store's urgent order
watch(urgentOrder, (newVal) => {
  if (newVal) {
    showDialog.value = true;
    // Reproducir sonido extra si es necesario
    store.playNotificationSound();
  } else {
    showDialog.value = false;
  }
});

const closeDialog = () => {
  showDialog.value = false;
  store.clearUrgentOrder();
};
</script>

<style scoped>
.border-urgent {
  border: 4px solid #D32F2F;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(211, 47, 47, 0.7);
  }
  70% {
    box-shadow: 0 0 0 20px rgba(211, 47, 47, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(211, 47, 47, 0);
  }
}
</style>
