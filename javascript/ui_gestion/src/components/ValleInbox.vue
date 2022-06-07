<template>
  <v-menu v-model="showMod" :anchor="anchor" origin="auto">
    <template v-slot:activator="{ props }">
      <v-btn stacked v-bind="props">
        <v-badge color="info" :content="num_inst">
          <v-icon icon="mdi-inbox-arrow-down"></v-icon>
        </v-badge>
      </v-btn>
    </template>

    <v-card variant="outlined">
      <v-card-header> Actualizaciones </v-card-header>
      <v-spacer></v-spacer>
      <v-list border class="mx-auto">
        <v-list-item>
          <v-list-item-title class="mr-10"> Pendientes </v-list-item-title>
          <v-spacer></v-spacer>
          <template v-slot:append>
            <v-badge color="info" :content="num_inst" inline></v-badge>
          </template>
        </v-list-item>
      </v-list>
      <v-card-actions class="pl-16">
        <v-btn
          variant="outlined"
          @click="
            cancelar();
            showMod = false;
          "
          >Cancelar</v-btn
        >
        <v-btn
          variant="outlined"
          @click="
            actualizar();
            showMod = false;
          "
          >Actualizar</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script>
import { mapActions } from "vuex";

export default {
  props: ["num_inst", "anchor"],
  data() {
    return { showMod: false };
  },
  methods: {
    ...mapActions(["actualizar"]),
    cancelar() {
      this.$store.state.instrucciones = null;
      this.$router.go(this.$router.currentRoute);
    },
  },
};
</script>
