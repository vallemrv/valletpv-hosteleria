<template>
  <v-card class="mb-3" variant="outlined">
    <v-card-text>
      <v-row align="center">
        <v-col cols="auto">
          <v-icon 
            :color="connectionStatus.color" 
            size="large">
            {{ connectionStatus.icon }}
          </v-icon>
        </v-col>
        <v-col>
          <div class="text-subtitle-1 font-weight-bold">
            {{ connectionStatus.title }}
          </div>
          <div class="text-body-2 text-medium-emphasis">
            {{ connectionStatus.subtitle }}
          </div>
        </v-col>
        <v-col cols="auto">
          <v-chip 
            :color="connectionStatus.color"
            variant="outlined"
            size="small">
            {{ connectionStatus.status }}
          </v-chip>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script>
import { computed } from 'vue';
import { useMainStore } from '@/stores/main';

export default {
  props: {
    isWsConnected: Boolean,
    isHttpConnected: Boolean,
    isSecure: Boolean
  },
  setup(props) {
    const store = useMainStore();

    const connectionStatus = computed(() => {
      // Si está reintentando conexión
      if (store.getIsReconnecting) {
        return {
          icon: 'mdi-sync',
          color: 'info',
          title: 'Reconectando...',
          subtitle: store.reconnectionStatus,
          status: 'RECONECTANDO'
        };
      }

      if (props.isWsConnected && props.isHttpConnected) {
        return {
          icon: 'mdi-check-circle',
          color: 'success',
          title: 'Conectado',
          subtitle: `Conexión ${props.isSecure ? 'segura' : 'no segura'} establecida`,
          status: 'ONLINE'
        };
      } else if (props.isHttpConnected) {
        return {
          icon: 'mdi-alert-circle',
          color: 'warning',
          title: 'Conexión parcial',
          subtitle: 'API conectada, WebSocket desconectado',
          status: 'PARCIAL'
        };
      } else {
        return {
          icon: 'mdi-close-circle',
          color: 'error',
          title: 'Sin conexión',
          subtitle: 'No se puede conectar al servidor',
          status: 'OFFLINE'
        };
      }
    });

    return {
      connectionStatus
    };
  }
};
</script>
