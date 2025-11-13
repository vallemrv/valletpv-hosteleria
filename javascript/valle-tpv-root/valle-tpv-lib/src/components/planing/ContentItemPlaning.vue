<template>
  <div class="content-item-planing" @click="handleClick">
    <img :src="svg" alt="icono" class="item-svg" />
    <div class="item-label">{{ label }}</div>
  </div>
</template>

<script setup lang="ts">

const props = defineProps<{ svg: string; label: string }>();
const emit = defineEmits(['select']);

function handleClick() {
  emit('select', { label: props.label, url: props.svg });
}
</script>

<style scoped>
  .content-item-planing {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    margin: 8px;
    cursor: pointer;
    border-radius: 8px;
    transition: background 0.15s;
  }
  .content-item-planing:hover {
    background: rgba(var(--v-theme-primary, 21,101,192), 0.09);
  }
  .item-label {
    font-size: 0.95rem;
    margin-top: 6px;
    text-align: center;
    color: var(--v-theme-on-surface);
  }
  .item-svg {
    width: 38px;
    height: 38px;
    object-fit: contain;
    margin-bottom: 2px;
  }
</style>
