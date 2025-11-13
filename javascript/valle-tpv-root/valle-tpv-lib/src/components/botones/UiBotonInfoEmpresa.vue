<template>
  <v-card
    elevation="8"
    rounded="xl"
    class="info-empresa-card pa-4 mb-4 d-flex flex-column flex-md-row align-center justify-between info-empresa-clicable"
    @click="$emit('click', empresa)"
  >
    <div class="empresa-main d-flex flex-column flex-grow-1">
      <div class="d-flex align-center mb-1">
        <span class="empresa-nombre">{{ empresa.nombre }}</span>
        <v-chip
          :color="empresa.activa ? 'success' : 'grey'"
          size="small"
          class="ml-2"
          label
          style="font-size:0.95rem;font-weight:500;"
        >
          {{ empresa.activa ? 'Activo' : 'Inactivo' }}
        </v-chip>
      </div>
      <span class="empresa-descripcion text-grey-darken-1 mb-1">{{ empresa.descripcion }}</span>
      <span class="empresa-url text-primary" style="font-size:1.05rem;">{{ empresa.url_servidor }}</span>
    </div>
    <div class="empresa-actions d-flex flex-row align-center mt-3 mt-md-0">
  <v-btn icon color="primary" class="me-2" @click.stop="$emit('editar', empresa)">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>
  <v-btn icon color="error" @click.stop="$emit('borrar', empresa)">
        <v-icon>mdi-delete</v-icon>
      </v-btn>
    </div>
  </v-card>
</template>

<script setup lang="ts">

const props = defineProps({
  empresa: {
    type: Object as () => {
      nombre: string;
      descripcion: string;
      url_servidor: string;
      activa: boolean;
    },
    required: true
  }
});

defineEmits(['editar', 'borrar', 'click']);
</script>

<style scoped>
.info-empresa-card {
  min-width: 280px;
  max-width: 600px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.info-empresa-clicable:hover {
  box-shadow: 0 8px 32px rgba(21,101,192,0.12);
  transform: translateY(-2px) scale(1.01);
}
.empresa-nombre {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--v-theme-on-surface);
}
.empresa-descripcion {
  font-size: 1.05rem;
  font-weight: 400;
}
.empresa-url {
  font-size: 1.05rem;
  font-weight: 500;
  word-break: break-all;
}
</style>
