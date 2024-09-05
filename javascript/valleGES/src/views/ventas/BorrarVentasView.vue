<template>
  <v-row>
    <v-col cols="12">
      <v-toolbar color="#cfb6d4">
        <v-toolbar-title>Borrar ventas</v-toolbar-title>
      </v-toolbar>
    </v-col>

    <v-col cols="12">
      <v-card elevation="3">
        <v-card-text>
          <v-alert color="warning" title="Atencion">
            Todo el contenido de ventas será borrado definitivamente. Ya no podra
            recuperarse.
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="borrarVentas()"> borrar </v-btn>
        </v-card-actions>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
import { mapActions } from "vuex";
export default {
  methods: {
    ...mapActions(["borrarVentas"]),
  },
};
</script>
