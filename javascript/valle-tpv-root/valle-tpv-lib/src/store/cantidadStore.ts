import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useCantidadStore = defineStore('cantidad', () => {
  const cantidad = ref<number>(1);

  function setCantidad(nuevaCantidad: number) {
    if (nuevaCantidad >= 1 && nuevaCantidad <= 9) {
      cantidad.value = nuevaCantidad;
    }
  }

  function reset() {
    cantidad.value = 1;
  }

  return {
    cantidad,
    setCantidad,
    reset
  };
});
