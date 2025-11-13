<template>
  <v-container fluid  class="pa-0 fill-height d-flex align-center justify-center">
    <v-card elevation="10" rounded="xl" class="main-card">
      <v-card-title class="bg-primary d-flex align-center justify-space-between pa-4">
        
        <div class="d-flex align-center">
          <v-avatar v-if="icon" size="48" color="rgba(var(--v-theme-surface), 0.2)" class="me-4">
            <v-icon size="28" color="on-surface">{{ icon }}</v-icon>
          </v-avatar>
          <div class="title">{{ title }}</div>
        </div>

        <div class="actions">
          <slot name="actions" />
        </div>

      </v-card-title>

      <v-card-text class="content-area ">
        <slot />
      </v-card-text>
      
      <div class="status-bar d-flex align-center justify-space-between px-6 py-2">
        <div class="status-left">
          {{ camarerosSel ? `${camarerosSel.nombre} ${camarerosSel.apellidos}` : empresaActiva?.nombre }}
        </div>
        <div class="status-center">
          {{ tareasPendientes }} tareas pendientes
        </div>
        <div class="status-right">
          <v-chip
            :color="isConnected ? 'success' : 'error'"
            variant="flat"
            size="small"
            class="connection-chip"
          >
            {{ isConnected ? 'Conectado' : 'Desconectado' }}
          </v-chip>
        </div>
      </div>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
// Update the import path to the correct relative location if it's a local file
import { useConnectionStore } from '../../store/connectionStore'; // Ajusta la ruta según la ubicación real
import  { useEmpresasStore }  from '../../store/dbStore/empresasStore'; // Ajusta la ruta según la ubicación real
import { useInstruccionesStore } from '../../store/instruccionesStore';
import { useCamarerosStore } from '../../store/dbStore/camarerosStore';
import { storeToRefs } from 'pinia';
import { computed } from 'vue';

const connectionStore = useConnectionStore();
const empresasStore = useEmpresasStore();
const instruccionesStore = useInstruccionesStore();
const camarerosStore = useCamarerosStore();



const props = defineProps({
  title: { type: String, required: true },
  icon: { type: String, required: false },
});

const { isConnected } = storeToRefs(connectionStore);
const { empresaActiva } = storeToRefs(empresasStore);
const { enCola: tareasPendientes } = storeToRefs(instruccionesStore);
const camarerosSel = computed(() => camarerosStore.camarerosSel);

</script>

<style scoped>



.main-card {
  width: 98vw;
  height: 98vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.title {
  font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  font-size: 2.25rem;
  font-weight: 300;
  line-height: 1.2;
  letter-spacing: normal;
  color: rgb(var(--v-theme-on-surface));
  padding-top: 20px;
  padding-bottom: 20px;
}

.content-area {
  flex-grow: 1;
  overflow-y: auto;
  padding: 2px;
  height: 100%;
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-thumb {
  background-color: rgba(var(--v-theme-on-surface), 0.2);
  border-radius: 4px;
}

.content-area::-webkit-scrollbar-track {
  background: transparent;
}

.status-bar {
  width: 100%;
  height: 48px;
  background: linear-gradient(90deg, #F3F4FD 0%, #E9EAF7 100%);
  border-top: 1px solid #e0e0e0;
  font-size: 0.95rem;
  font-weight: 400;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding-top: 0;
  padding-bottom: 0;
}

.status-left {
  font-weight: 500;
  color: #3a3a3a;
}

.status-center {
  color: #6c6c6c;
}

.status-right {
  display: flex;
  align-items: center;
  height: 100%;
  justify-content: flex-start;

}

.status-right .connection-chip {
  font-weight: 500;
  min-width: 110px;
  justify-content: center;
  margin-bottom: 0;
  align-self: center;
  height: 32px;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  padding: 0 12px;
}
</style>