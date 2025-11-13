
import { defineStore } from 'pinia';

export const useConnectionStore = defineStore('connection', {
  state: () => ({
    isConnected: false,
    hasError: false,
    errorMessage: ''
  }),
  actions: {
    setConnected(status: boolean) {
      this.isConnected = status;
      if (status) {
        this.hasError = false;
        this.errorMessage = '';
      }
    },
    setError(message: string) {
      this.hasError = true;
      this.errorMessage = message;
      this.isConnected = false;
    }
  }
});
