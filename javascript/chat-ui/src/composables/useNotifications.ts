// composables/useNotifications.ts

import { ref } from 'vue';

interface Notification {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  timeout?: number;
}

const notifications = ref<Notification[]>([]);

export function useNotifications() {
  const showNotification = (notification: Omit<Notification, 'id'>) => {
    const id = `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const newNotification: Notification = {
      id,
      timeout: 5000,
      ...notification
    };

    notifications.value.push(newNotification);

    // Auto-remover después del timeout
    if (newNotification.timeout) {
      setTimeout(() => {
        removeNotification(id);
      }, newNotification.timeout);
    }

    return id;
  };

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id);
    if (index > -1) {
      notifications.value.splice(index, 1);
    }
  };

  const clearAllNotifications = () => {
    notifications.value = [];
  };

  // Métodos de conveniencia
  const showSuccess = (message: string, timeout?: number) => {
    return showNotification({ message, type: 'success', timeout });
  };

  const showError = (message: string, timeout?: number) => {
    return showNotification({ message, type: 'error', timeout });
  };

  const showInfo = (message: string, timeout?: number) => {
    return showNotification({ message, type: 'info', timeout });
  };

  const showWarning = (message: string, timeout?: number) => {
    return showNotification({ message, type: 'warning', timeout });
  };

  return {
    notifications,
    showNotification,
    removeNotification,
    clearAllNotifications,
    showSuccess,
    showError,
    showInfo,
    showWarning
  };
}
