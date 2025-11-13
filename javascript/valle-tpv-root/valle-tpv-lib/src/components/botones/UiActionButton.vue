<template>
  <v-btn class="ui-action-btn" elevation="6" rounded="lg" :style="btnStyle">
    <v-icon size="22" class="ui-action-icon">{{ icon }}</v-icon>
    <slot />
  </v-btn>
</template>

<script setup lang="ts">
import {  computed } from 'vue';
const props = defineProps({
  icon: { type: String, required: true }
});

const btnStyle = computed(() => ({
  backgroundColor: 'rgba(var(--v-theme-primary, 21,101,192), 0.12)',
  border: '1.5px solid var(--v-theme-primary)',
  minWidth: '44px',
  minHeight: '44px',
  padding: '2em 1em',
  boxSizing: 'border-box' as const,
}));
</script>

<style scoped>
.ui-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5em;
  transition: box-shadow 0.2s;
  font-size: 0.90rem;
}
.ui-action-icon {
  color: var(--v-theme-on-surface) !important;
  font-size: 1.1rem;
}
</style>
