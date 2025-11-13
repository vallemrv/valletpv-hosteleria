<template>
  <v-btn
    class="camarero-btn"
    variant="tonal"
    color="surface-variant"
    rounded="lg"
    height="100%"
    elevation="6"
  >
    <div class="content-wrapper">
      <v-avatar size="64" class="avatar-bg mb-3">
        <v-img v-if="avatar" :src="avatar" :alt="nombre_completo" cover></v-img>
        <v-icon v-else size="40" color="grey-darken-1">mdi-account-circle-outline</v-icon>
      </v-avatar>
      <span class="nombre-completo">{{ nombre_completo }}</span>
    </div>
  </v-btn>
</template>

<script setup lang="ts">
defineProps({
  avatar: { type: String, required: false },
  nombre_completo: { type: String, required: true },
});
</script>

<style scoped>
.camarero-btn {
  width: 100%;
  height: 100%;
  padding: 16px 8px;
  text-transform: none !important;
  transition: all 0.2s ease-in-out;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.camarero-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border-color: transparent;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.avatar-bg {
  background-color: rgba(0, 0, 0, 0.05);
}

.nombre-completo {
  white-space: normal;
  word-break: break-word;
  text-align: center;
  font-size: 0.95rem;
  font-weight: 500;
  line-height: 1.3;
  color: var(--v-theme-on-surface);
  max-height: 55px;
  overflow: hidden;
}
</style>