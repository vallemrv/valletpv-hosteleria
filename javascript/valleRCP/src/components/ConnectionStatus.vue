<template>
  <div class="status-bar" :class="`status-bar--${connectionStatus.color}`">
    <div class="status-bar__content">
      <v-icon 
        :color="connectionStatus.color" 
        size="small"
        class="status-bar__icon">
        {{ connectionStatus.icon }}
      </v-icon>
      <span class="status-bar__text">
        {{ connectionStatus.title }}
      </span>
      <span class="status-bar__chip">
        {{ connectionStatus.status }}
      </span>
    </div>
  </div>
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

<style scoped>
.status-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.status-bar--success {
  background-color: rgba(76, 175, 80, 0.08);
  border-top-color: rgba(76, 175, 80, 0.2);
}

.status-bar--warning {
  background-color: rgba(255, 152, 0, 0.08);
  border-top-color: rgba(255, 152, 0, 0.2);
}

.status-bar--error {
  background-color: rgba(244, 67, 54, 0.08);
  border-top-color: rgba(244, 67, 54, 0.2);
}

.status-bar--info {
  background-color: rgba(33, 150, 243, 0.08);
  border-top-color: rgba(33, 150, 243, 0.2);
}

.status-bar__content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  gap: 8px;
  min-height: 36px;
}

.status-bar__icon {
  opacity: 0.9;
}

.status-bar__text {
  font-size: 0.813rem;
  font-weight: 500;
  opacity: 0.87;
}

.status-bar__chip {
  font-size: 0.688rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  opacity: 0.75;
  letter-spacing: 0.5px;
}

.status-bar--success .status-bar__chip {
  background-color: rgba(76, 175, 80, 0.15);
  color: rgb(46, 125, 50);
}

.status-bar--warning .status-bar__chip {
  background-color: rgba(255, 152, 0, 0.15);
  color: rgb(230, 81, 0);
}

.status-bar--error .status-bar__chip {
  background-color: rgba(244, 67, 54, 0.15);
  color: rgb(198, 40, 40);
}

.status-bar--info .status-bar__chip {
  background-color: rgba(33, 150, 243, 0.15);
  color: rgb(13, 71, 161);
}

/* Animación sutil para el ícono cuando está reconectando */
.status-bar--info .status-bar__icon {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.9;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
