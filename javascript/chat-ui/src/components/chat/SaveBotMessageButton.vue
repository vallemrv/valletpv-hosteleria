<!-- SaveBotMessageButton.vue -->
<template>
  <v-tooltip location="top">
    <template v-slot:activator="{ props }">
      <v-btn
        v-bind="props"
        icon
        size="small"
        variant="text"
        color="primary"
        @click="handleClick"
        class="save-button"
      >
        <v-icon>mdi-bookmark-plus-outline</v-icon>
      </v-btn>
    </template>
    <span>Guardar respuesta del bot</span>
  </v-tooltip>
</template>

<script setup lang="ts">
interface Props {
  htmlContent: string;  // El contenido HTML del bot
}

const props = defineProps<Props>();

// Emitir evento cuando se hace clic
const emit = defineEmits<{
  openSaveDialog: [htmlContent: string]
}>();

const handleClick = () => {
  console.log('SaveBotMessageButton clicked, emitting event');
  emit('openSaveDialog', props.htmlContent);
};
</script>

<style scoped>
.save-button {
  opacity: 0.7;
  transition: opacity 0.2s;
}

.save-button:hover {
  opacity: 1;
}
</style>
