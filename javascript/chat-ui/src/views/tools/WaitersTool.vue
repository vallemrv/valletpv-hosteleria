<template>
  <div class="waiters-layout">
    <v-app-bar app class="app-bar" flat>
      <v-btn icon @click="$router.push('/tools')">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title class="app-title">Gestión de Camareros</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="loadData">
        <v-icon>mdi-refresh</v-icon>
      </v-btn>
    </v-app-bar>

    <div class="content-area">
      <v-container>
        <div v-if="waiterStore.isLoading" class="d-flex justify-center my-8">
          <v-progress-circular indeterminate color="primary"></v-progress-circular>
        </div>

        <div v-else-if="waiterStore.error" class="d-flex flex-column align-center my-8">
           <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
           <div class="text-h6 text-error">{{ waiterStore.error }}</div>
           <v-btn color="primary" class="mt-4" @click="loadData">Reintentar</v-btn>
        </div>

        <v-list v-else lines="two" rounded="lg" class="waiter-list">
          <v-list-item
            v-for="waiter in activeWaiters"
            :key="waiter.id"
            :title="`${waiter.nombre} ${waiter.apellidos || ''}`"
            :subtitle="waiter.autorizado ? 'Autorizado' : 'No autorizado'"
          >
            <template v-slot:prepend>
              <v-avatar color="surface-variant">
                <v-icon icon="mdi-account"></v-icon>
              </v-avatar>
            </template>

            <template v-slot:append>
              <v-switch
                :model-value="waiter.autorizado"
                @update:model-value="waiterStore.toggleAuthorization(waiter)"
                color="success"
                hide-details
                inset
              ></v-switch>
            </template>
          </v-list-item>
          
          <v-list-item v-if="activeWaiters.length === 0">
             <div class="text-center pa-4 text-medium-emphasis">
               Waiters: {{ waiterStore.waiters.length }} | Active: 0
               <br>
               No hay camareros activos registrados
             </div>
          </v-list-item>
        </v-list>
      </v-container>
    </div>
  </div>
</template>

<script>
import { useWaiterStore } from '@/stores/waiterStore';
import { onMounted, computed } from 'vue';

export default {
  name: 'WaitersTool',
  setup() {
    const waiterStore = useWaiterStore();

    const loadData = () => {
      waiterStore.loadWaiters();
    };

    const activeWaiters = computed(() => {
        return waiterStore.waiters.filter(w => {
            // Handle various truthy values just in case
            return w.activo === true || w.activo === 1 || w.activo === '1' || w.activo === 'true';
        });
    });

    onMounted(() => {
      loadData();
    });

    return { waiterStore, loadData, activeWaiters };
  },

};
</script>

<style scoped>
.waiters-layout {
  height: 100dvh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  overflow: hidden; /* Prevent body scroll */
}
.app-bar {
  background-color: white !important;
  border-bottom: 1px solid #e0e0e0;
  position: relative !important; /* Force it to be in flex flow */
}
.content-area {
  flex: 1;
  overflow-y: auto; /* Enable inner scroll */
  padding-top: 0; /* Remove padding since navbar is relative */
}
.waiter-list {
    background-color: transparent !important;
}
</style>
