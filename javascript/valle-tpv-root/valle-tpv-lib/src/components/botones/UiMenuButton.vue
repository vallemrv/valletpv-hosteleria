<template>
  <v-menu class="pa-4 pl-6 pr-6" >
    <template #activator="{ props }">
      <v-btn v-bind="props" class="ui-action-btn" elevation="6" rounded="lg" :style="btnStyle">
        <v-icon size="22" class="ui-action-icon">mdi-dots-vertical</v-icon>
      </v-btn>
    </template>
    <v-list class="ui-menu-list">
      <v-list-item v-for="item in items" :key="item.id" @click="select(item.id)" rounded="lg" class="ui-menu-item">
        <div class="item-content">
          <v-icon v-if="item.icon" size="22" class="ui-action-icon me-2">{{ item.icon }}</v-icon>
          <span class="item-text">{{ item.text }}</span>
        </div>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  items: {
    type: Array as () => Array<{ icon?: string; text: string; id: string | number }>,
    required: true
  }
});

const emit = defineEmits(['on_select']);

function select(id: string | number) {
  emit('on_select', id);
}

const btnStyle = computed(() => ({
  backgroundColor: 'rgba(var(--v-theme-primary, 21,101,192), 0.12)',
  border: '1.5px solid var(--v-theme-primary)',
  minWidth: '44px',
  minHeight: '44px',
  padding: '2em',
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
  font-size: 0.95rem;
}
.ui-action-icon {
  color: var(--v-theme-on-surface) !important;
  font-size: 1.1rem;
}
.ui-menu-list {
  min-width: 180px;
  background: var(--v-theme-surface);
  border-radius: 10px;
  box-shadow: 0 4px 24px rgba(16,24,40,0.12);
  padding: 0.5em 0.2em;
}
.ui-menu-item {
  cursor: pointer;
  padding: 0.7em 1.2em;
  border-radius: 7px;
  margin-bottom: 6px;
  background: rgba(var(--v-theme-primary, 21,101,192), 0.15);
  box-shadow: 0 2px 8px rgba(16,24,40,0.10);
  border: 1.5px solid var(--v-theme-primary);
  transition: background 0.18s, box-shadow 0.18s, border 0.18s;
  display: flex;
  align-items: center;
  padding: 2em;
}
.ui-menu-item:hover {
  background: rgba(var(--v-theme-primary, 21,101,192), 0.38);
  box-shadow: 0 4px 16px rgba(16,24,40,0.16);
  border-color: var(--v-theme-primary-darken-1, #0D47A1);
}
.item-content {
  display: flex;
  align-items: center;
  width: 100%;
}
.item-text {
  font-size: 1.08rem;
  color: var(--v-theme-on-surface);
  font-weight: 500;
  margin-left: 0.2em;
  padding: 1.3em;
}
</style>
