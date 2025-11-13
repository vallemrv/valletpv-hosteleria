<template>
  <div class="notifications-wrapper">
    <div v-for="notification in visibleNotifications" :key="notification.id" class="notification-toast"
      :class="`notification-${notification.type}`">
      <div class="notification-content">
        <div class="notification-icon">
          <v-icon>{{ getIcon(notification.type) }}</v-icon>
        </div>
        <span class="notification-message">{{ notification.message }}</span>
        <button class="notification-close" @click="hideNotification(notification.id)">
          <v-icon size="small">mdi-close</v-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';

export default {
  name: 'NotificationContainer',

  setup() {
    const { notifications } = useNotifications();
    const visibleNotifications = ref([]);

    // Escuchar nuevas notificaciones
    watch(notifications, (newNotifications) => {
      newNotifications.forEach(notification => {
        const exists = visibleNotifications.value.find(n => n.id === notification.id);
        if (!exists) {
          // Añadir nueva notificación
          visibleNotifications.value.push(notification);

          // Programar eliminación automática
          setTimeout(() => {
            hideNotification(notification.id);
          }, 5000);
        }
      });
    }, { deep: true, immediate: true });

    const hideNotification = (id) => {
      const index = visibleNotifications.value.findIndex(n => n.id === id);
      if (index > -1) {
        visibleNotifications.value.splice(index, 1);
      }
    };

    return {
      visibleNotifications,
      hideNotification
    };
  },

  methods: {
    getIcon(type) {
      const icons = {
        success: 'mdi-check-circle',
        error: 'mdi-alert-circle',
        info: 'mdi-information',
        warning: 'mdi-alert'
      };
      return icons[type] || 'mdi-information';
    }
  }
};
</script>

<style scoped>
.notifications-wrapper {
  position: fixed;
  top: 80px;
  right: 16px;
  z-index: 2000;
  pointer-events: none;
  max-width: 350px;
}

.notification-toast {
  pointer-events: auto;
  margin-bottom: 12px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease-out;
  min-width: 280px;
}

.notification-content {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
}

.notification-icon {
  flex-shrink: 0;
}

.notification-message {
  flex: 1;
  font-size: 14px;
  line-height: 1.4;
}

.notification-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.notification-close:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

/* Estilos por tipo */
.notification-success {
  background: #4caf50;
  color: white;
}

.notification-error {
  background: #f44336;
  color: white;
}

.notification-info {
  background: #2196f3;
  color: white;
}

.notification-warning {
  background: #ff9800;
  color: white;
}

.notification-success .notification-close:hover,
.notification-error .notification-close:hover,
.notification-info .notification-close:hover,
.notification-warning .notification-close:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* Animación de entrada */
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
